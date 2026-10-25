package com.kvd.bubblelevel;

/** Proper acceleration projects toward the raised edge. Canvas Y grows downward. */
public final class BubbleGeometry {
    private BubbleGeometry() {}
    public static double horizontal(double angleX) { return Math.max(-1,Math.min(1,angleX/90)); }
    public static double vertical(double angleY) { return Math.max(-1,Math.min(1,-angleY/90)); }
    public static double[] circle(double angleX,double angleY) {
        double x=angleX/90,y=-angleY/90;
        double magnitude=Math.hypot(x,y);
        if(magnitude>1) {x/=magnitude;y/=magnitude;}
        return new double[]{x,y};
    }
}
