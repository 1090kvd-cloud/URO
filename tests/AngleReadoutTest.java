package com.kvd.bubblelevel;
public final class AngleReadoutTest {
    static int count;
    static void near(double value,double expected) {count++;if(!Double.isFinite(value)||Math.abs(value-expected)>.02)throw new AssertionError(value+" != "+expected);}
    static void yes(boolean value) {count++;if(!value)throw new AssertionError("angle check "+count);}
    static LevelModel at(double x,double y,double z) {LevelModel m=new LevelModel();m.sample(x,y,z,0,1);return m;}
    public static void main(String[] args) {
        near(at(0,9.81,0).rotation(),0);
        near(at(-9.81,0,0).rotation(),90);
        near(at(0,-9.81,0).rotation(),180);
        near(at(9.81,0,0).rotation(),270);
        near(at(-6.93672,6.93672,0).rotation(),45);
        near(at(6.93672,6.93672,0).rotation(),315);
        yes(Double.isNaN(at(0,0,9.81).rotation()));
        yes(Double.isNaN(at(.1,.1,9.81).rotation()));
        LevelModel m=at(-9.81,0,0);m.calibrate();near(m.rotation(),90);
        m.toggleHold();m.sample(0,9.81,0,0,3);near(m.rotation(),90);
        m.sample(Double.NaN,0,9.81,0,4);near(m.rotation(),90);
        m.toggleHold();yes(Double.isNaN(m.rotation()));
        m.sample(0,9.81,0,0,5);near(m.rotation(),0);m.resetFilter();yes(Double.isNaN(m.rotation()));
        near(BubbleGeometry.horizontal(45),.5);near(BubbleGeometry.vertical(45),-.5);
        near(BubbleGeometry.horizontal(90),1);near(BubbleGeometry.vertical(-90),1);
        double[] p=BubbleGeometry.circle(90,90);near(p[0],Math.sqrt(.5));near(p[1],-Math.sqrt(.5));
        System.out.println("PASS: "+count+" degree scale and rotation checks");
    }
}
