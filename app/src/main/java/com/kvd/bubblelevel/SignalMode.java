package com.kvd.bubblelevel;

/** Sound target is independent from the circular vial's two-axis alignment. */
public final class SignalMode {
    public static final String[] IDS={"auto","both","x","y"};
    private SignalMode() {}
    public static int index(String mode) {
        for(int i=0;i<IDS.length;i++)if(IDS[i].equals(mode))return i;
        return 0;
    }
    public static String resolve(String mode,double rawX,double rawY) {
        if("both".equals(mode)||"x".equals(mode)||"y".equals(mode))return mode;
        // Flat: circular level. Raised top/bottom: X vial. Raised side: Y vial.
        if(Math.max(Math.abs(rawX),Math.abs(rawY))<45)return "both";
        return Math.abs(rawY)>=Math.abs(rawX)?"x":"y";
    }
    public static boolean aligned(String target,double x,double y,double tolerance) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(tolerance)||tolerance<0)return false;
        if("x".equals(target))return Math.abs(x)<=tolerance;
        if("y".equals(target))return Math.abs(y)<=tolerance;
        return Math.abs(x)<=tolerance && Math.abs(y)<=tolerance;
    }
}
