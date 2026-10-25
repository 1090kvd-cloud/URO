package com.kvd.bubblelevel;
public final class RelativeRotationTest {
    static int count;
    static void near(double a,double b) {count++;if(!Double.isFinite(a)||Math.abs(a-b)>.02)throw new AssertionError(a+" != "+b);}
    static void yes(boolean x) {count++;if(!x)throw new AssertionError("rotation check "+count);}
    static void yaw(RelativeRotation r,double clockwise,double t) {double h=Math.toRadians(-clockwise)/2;r.quaternion(0,0,Math.sin(h),Math.cos(h),t);}
    public static void main(String[] args) {
        RelativeRotation r=new RelativeRotation();yes(Double.isNaN(r.reading(1)));yes(!r.zero(1));
        yaw(r,37,1);near(r.reading(1),0);
        yaw(r,127,1.1);near(r.reading(1.1),90);
        yaw(r,217,1.2);near(r.reading(1.2),180);
        yaw(r,307,1.3);near(r.reading(1.3),270);
        yaw(r,397,1.4);near(r.reading(1.4),0);
        yes(r.zero(1.4));near(r.reading(1.4),0);
        yaw(r,487,1.5);near(r.reading(1.5),90);
        yes(Double.isNaN(r.reading(2.1)));yes(!r.zero(2.1));
        r.reset();yaw(r,0,3);r.quaternion(0,0,Math.sqrt(.5),-Math.sqrt(.5),3.1);near(r.reading(3.1),90);
        r.quaternion(Double.NaN,0,0,1,3.2);yes(Double.isNaN(r.reading(3.2)));
        r.reset();yaw(r,0,4);r.quaternion(Math.sin(Math.PI/8),0,0,Math.cos(Math.PI/8),4.1);near(r.reading(4.1),0);
        r.resetReference();yes(Double.isNaN(r.reading(4.1)));yaw(r,100,4.2);near(r.reading(4.2),0);
        r.reset();for(int i=0;i<=100;i++)r.gyroscope(-Math.PI/2,5+i*.01);near(r.reading(6),90);
        yes(r.zero(6));near(r.reading(6),0);r.gyroscope(-Math.PI/2,6.1);near(r.reading(6.1),9);
        r.gyroscope(-Math.PI/2,5.5);near(r.reading(6.1),9);
        r.gyroscope(-Math.PI/2,7);near(r.reading(7),0);
        r.gyroscope(Double.NaN,7.1);yes(Double.isNaN(r.reading(7.1)));
        r.reset();for(int i=0;i<=100;i++)r.gyroscope(Math.PI/2,8+i*.01);near(r.reading(9),270);
        LevelModel m=new LevelModel();m.sample(0,0,9.81,0,1);near(m.inclinationFromFlat(),0);
        m.resetFilter();m.sample(0,9.81,0,0,2);near(m.inclinationFromFlat(),90);
        m.calibrate();near(m.inclinationFromFlat(),90);
        m.resetFilter();yes(Double.isNaN(m.inclinationFromFlat()));
        r.reset();r.quaternion(0,Math.sin(Math.PI/12),0,Math.cos(Math.PI/12),10);
        double a=Math.sqrt(.5),b=Math.sin(Math.PI/12),c=Math.cos(Math.PI/12);
        r.quaternion(a*b,a*b,-a*c,a*c,10.1);near(r.reading(10.1),90);
        System.out.println("PASS: "+count+" relative rotation checks");
    }
}
