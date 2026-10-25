package com.kvd.bubblelevel;

/** Proper acceleration projects toward the raised edge. Canvas Y grows downward. */
public final class BubbleGeometry {
    private BubbleGeometry() {}
    public static double horizontal(double angleX) { return Math.max(-1,Math.min(1,angleX/25)); }
    public static double vertical(double angleY) { return Math.max(-1,Math.min(1,-angleY/25)); }
    public static double[] circle(double angleX,double angleY) {
        double x=angleX/25,y=-angleY/25;
        double magnitude=Math.hypot(x,y);
        if(magnitude>1) {x/=magnitude;y/=magnitude;}
        return new double[]{x,y};
    }
}
