package com.kvd.bubblelevel;

/** Relative clockwise heading for a near-horizontal handset; no magnetic north. */
public final class RelativeRotation {
    private final double[] current=new double[4],reference=new double[4];
    private boolean sample,referenceSet,quaternionMode;
    private double angle=Double.NaN,lastTime=Double.NaN,lastRate;
    public void reset() {sample=false;referenceSet=false;angle=Double.NaN;lastTime=Double.NaN;}
    public void resetReference() {reset();}
    public void quaternion(double x,double y,double z,double w,double time) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Double.isFinite(w)||!Double.isFinite(time)) {reset();return;}
        double norm=Math.sqrt(x*x+y*y+z*z+w*w);
        if(!Double.isFinite(norm)||norm<1e-6) {reset();return;}
        if(sample && time<=lastTime)return;
        if(sample && time-lastTime>.5)referenceSet=false;
        current[0]=x/norm;current[1]=y/norm;current[2]=z/norm;current[3]=w/norm;
        quaternionMode=true;sample=true;lastTime=time;
        if(!referenceSet) {System.arraycopy(current,0,reference,0,4);referenceSet=true;}
        double origin=heading(reference),now=heading(current);
        angle=Double.isFinite(origin)&&Double.isFinite(now)?normalize(-Math.toDegrees(now-origin)):Double.NaN;
    }
    private static double heading(double[] q) {
        // Horizontal projection of the device X axis in the gravity-aligned game frame.
        double x=1-2*(q[1]*q[1]+q[2]*q[2]);
        double y=2*(q[0]*q[1]+q[3]*q[2]);
        return Math.hypot(x,y)<.1?Double.NaN:Math.atan2(y,x);
    }
    public void gyroscope(double zRadiansPerSecond,double time) {
        if(!Double.isFinite(zRadiansPerSecond)||!Double.isFinite(time)) {reset();return;}
        if(sample && time<=lastTime)return;
        if(!sample || quaternionMode || time-lastTime>.5)angle=0;
        else angle=normalize(angle-Math.toDegrees((lastRate+zRadiansPerSecond)*.5*(time-lastTime)));
        quaternionMode=false;sample=true;lastRate=zRadiansPerSecond;lastTime=time;
    }
    public double reading(double now) {return fresh(now)?angle:Double.NaN;}
    public boolean zero(double now) {
        if(!fresh(now))return false;
        if(quaternionMode) {if(!Double.isFinite(heading(current)))return false;System.arraycopy(current,0,reference,0,4);referenceSet=true;}
        angle=0;return true;
    }
    private boolean fresh(double now) {return sample && Double.isFinite(now) && now>=lastTime && now-lastTime<=.5;}
    private static double normalize(double degrees) {double a=degrees%360;if(a<0)a+=360;return a>=360-1e-9?0:a;}
}
