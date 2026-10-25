package com.kvd.bubblelevel;
public final class LevelModelTest {
  static int checks;
  static void near(double got,double expected) { checks++; if(Math.abs(got-expected)>0.02) throw new AssertionError(got+" != "+expected); }
  static void yes(boolean b) { checks++; if(!b) throw new AssertionError("condition failed"); }
  public static void main(String[] args) {
    LevelModel m=new LevelModel(); yes(!m.ready()); yes(!m.calibrate()); yes(!m.toggleHold());
    m.sample(0,0,9.81,0,1); near(m.x(),0); near(m.y(),0); yes(m.ready());
    m.resetFilter(); m.sample(4.905,0,8.4957,0,2); near(m.x(),30); near(m.y(),0);
    yes(m.calibrate()); near(m.x(),0); near(m.y(),0);
    yes(m.toggleHold()); m.sample(0,4.905,8.4957,0,3); near(m.x(),0); near(m.y(),0); yes(!m.calibrate());
    yes(!m.toggleHold()); near(m.x(),-30); near(m.y(),30);
    m.clearCalibration(); near(m.x(),0); near(m.y(),30);
    m.resetFilter(); m.sample(4.905,0,8.4957,1,4); near(m.x(),0); near(m.y(),-30);
    m.resetFilter(); m.sample(4.905,0,8.4957,2,5); near(m.x(),-30); near(m.y(),0);
    m.resetFilter(); m.sample(4.905,0,8.4957,3,6); near(m.x(),0); near(m.y(),30);
    m.resetFilter(); m.sample(0,0,-9.81,0,7); near(m.x(),0); near(m.y(),0);
    m.resetFilter(); m.sample(9.81,0,0,0,8); near(m.x(),90); near(m.y(),0);
    m.resetFilter(); m.sample(Double.NaN,0,9.81,0,9); yes(!m.ready());
    m.sample(0,0,0,0,10); yes(!m.ready());
    LevelModel a=new LevelModel(),b=new LevelModel();
    a.sample(0,0,9.81,0,1); b.sample(0,0,9.81,0,1);
    for(int i=1;i<=50;i++) a.sample(1,0,9.7589,0,1+i*.02);
    for(int i=1;i<=100;i++) b.sample(1,0,9.7589,0,1+i*.01);
    near(a.x(),b.x()); yes(a.stable());
    b.sample(0,0,20,0,3); yes(!b.stable());
    LevelModel c=new LevelModel(); c.sample(0,0,9.81,0,1); yes(!c.stable());
    c.sample(0,0,9.81,0,1.6); yes(c.stable());
    c.setCalibration(1,2); near(c.x(),-1); near(c.y(),-2);
    c.resetFilter(); yes(!c.ready()); yes(!c.isHeld());
    LevelModel invalid=new LevelModel(); invalid.sample(0,0,9.81,0,1); invalid.sample(0,0,9.81,0,2);
    yes(invalid.stable()); invalid.sample(0,0,0,0,3); yes(!invalid.ready()); yes(!invalid.stable()); yes(!invalid.calibrate());
    invalid.sample(0,0,9.81,0,4); invalid.toggleHold(); invalid.sample(Double.NaN,0,9,0,5);
    yes(invalid.ready()); yes(invalid.isHeld()); near(invalid.x(),0); yes(!invalid.toggleHold()); yes(!invalid.ready());
    for(double interval:new double[]{.01,.02,.1}) {
      LevelModel moving=new LevelModel();
      for(double t=0;t<=3;t+=interval) {
        double angle=Math.toRadians(t*5);moving.sample(9.81*Math.sin(angle),0,9.81*Math.cos(angle),0,t+1);
      }
      yes(!moving.stable());
    }
    near(BubbleGeometry.horizontal(10),.4); near(BubbleGeometry.vertical(10),-.4);
    near(BubbleGeometry.horizontal(-90),-1); near(BubbleGeometry.vertical(-90),1);
    double[] point=BubbleGeometry.circle(25,25); near(point[0],Math.sqrt(.5)); near(point[1],-Math.sqrt(.5));
    System.out.println("PASS: "+checks+" measurement checks");
  }
}
