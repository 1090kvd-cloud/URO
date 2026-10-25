package com.kvd.bubblelevel;

/** Screen X points right, screen Y points toward the top edge. Angles are degrees. */
public final class LevelModel {
    private boolean ready, held, stable;
    private double gx, gy, gz, rawX, rawY, zeroX, zeroY, heldX, heldY, heldRotation, lastTime, quietSince;
    public void sample(double x, double y, double z, int rotation, double time) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || !Double.isFinite(time)) { invalidate(); return; }
        double norm=Math.sqrt(x*x+y*y+z*z);
        if(norm<1 || !Double.isFinite(norm)) { invalidate(); return; }
        double sx=x, sy=y;
        switch(rotation) {
            case 1: sx=y; sy=-x; break;
            case 2: sx=-x; sy=-y; break;
            case 3: sx=-y; sy=x; break;
            default: break;
        }
        double oldX=rawX, oldY=rawY;
        if(!ready || time<=lastTime) { gx=sx; gy=sy; gz=z; quietSince=time; }
        else {
            double alpha=1-Math.exp(-(time-lastTime)/.12);
            gx+=alpha*(sx-gx); gy+=alpha*(sy-gy); gz+=alpha*(z-gz);
        }
        rawX=Math.toDegrees(Math.atan2(gx,Math.hypot(gy,gz)));
        rawY=Math.toDegrees(Math.atan2(gy,Math.hypot(gx,gz)));
        double elapsed=time-lastTime;
        double speed=elapsed>0?Math.hypot(rawX-oldX,rawY-oldY)/elapsed:Double.POSITIVE_INFINITY;
        if(!ready || speed>.8 || Math.abs(norm-9.80665)>1.0) quietSince=time;
        stable=time-quietSince>=.45 && Math.abs(norm-9.80665)<=1.0;
        lastTime=time; ready=true;
    }
    public double x() { return held?heldX:rawX-zeroX; }
    public double y() { return held?heldY:rawY-zeroY; }
    /** Clockwise handset rotation relative to gravity, 0 = top edge upward. Undefined near flat. */
    public double rotation() {
        if(held) return heldRotation;
        if(!ready || Math.hypot(gx,gy)/Math.hypot(Math.hypot(gx,gy),gz)<Math.sin(Math.toRadians(5))) return Double.NaN;
        return (Math.toDegrees(Math.atan2(-gx,gy))+360)%360;
    }
    public boolean ready() { return held || ready; }
    public boolean stable() { return ready && stable; }
    public boolean isHeld() { return held; }
    public boolean toggleHold() {
        if(held) { held=false; return false; }
        if(!ready) return false;
        if(!held) { heldX=x(); heldY=y(); heldRotation=rotation(); }
        held=!held; return held;
    }
    public boolean calibrate() {
        if(!ready || held) return false;
        zeroX=rawX; zeroY=rawY; return true;
    }
    public void clearCalibration() { if(!held) { zeroX=0; zeroY=0; } }
    public void setCalibration(double x,double y) {
        if(Double.isFinite(x) && Double.isFinite(y) && !held) { zeroX=x; zeroY=y; }
    }
    public double zeroX() { return zeroX; }
    public double zeroY() { return zeroY; }
    private void invalidate() { ready=false;stable=false; }
    public void resetFilter() { ready=false; stable=false; held=false; }
}
