package com.kvd.bubblelevel;
public final class SignalModeTest {
    private static int checks;
    private static void yes(boolean value) { checks++;if(!value)throw new AssertionError("check "+checks); }
    private static void target(String mode,double rawX,double rawY,String expected) { yes(SignalMode.resolve(mode,rawX,rawY).equals(expected)); }
    public static void main(String[] args) {
        target("auto",0,0,"both"); target("auto",0,90,"x");target("auto",0,-90,"x");
        target("auto",90,0,"y");target("auto",-90,0,"y");target("auto",0,44,"both");
        target("both",0,90,"both");target("x",90,0,"x");target("y",0,90,"y");target("broken",0,0,"both");
        yes(SignalMode.aligned("x",0,50,.3));yes(!SignalMode.aligned("both",0,50,.3));
        yes(SignalMode.aligned("y",50,0,.3));yes(!SignalMode.aligned("both",50,0,.3));
        yes(!SignalMode.aligned("x",1,0,.3));yes(!SignalMode.aligned("y",0,1,.3));
        yes(SignalMode.aligned("both",.2,-.2,.3));yes(!SignalMode.aligned("both",.2,-.4,.3));
        yes(!SignalMode.aligned("x",Double.NaN,0,.3));yes(!SignalMode.aligned("y",0,Double.POSITIVE_INFINITY,.3));
        yes(!SignalMode.aligned("both",0,0,-1));
        // Actual gravity samples: a standing handset and one lying on its long edge.
        double[][] gravity={{0,0,9.81},{0,9.81,0},{9.81,0,0},{0,-9.81,0},{-9.81,0,0}};
        for(double[] sample:gravity) {
            LevelModel model=new LevelModel();
            for(int i=0;i<=100;i++)model.sample(sample[0],sample[1],sample[2],0,1+i*.02);
            String active=SignalMode.resolve("auto",model.x()+model.zeroX(),model.y()+model.zeroY());
            yes(model.ready() && model.stable() && SignalMode.aligned(active,model.x(),model.y(),.3));
        }
        // Off-level upright positions must stay silent on the active axis.
        yes(!SignalMode.aligned(SignalMode.resolve("auto",2,88),2,88,.3));
        yes(!SignalMode.aligned(SignalMode.resolve("auto",88,2),88,2,.3));
        // Calibration changes displayed zero, but posture selection uses raw inclination.
        target("auto",0,90,"x");yes(SignalMode.aligned("x",0,0,.3));
        System.out.println("PASS: "+checks+" axis sound checks");
    }
}
