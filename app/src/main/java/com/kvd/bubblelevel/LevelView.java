package com.kvd.bubblelevel;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Locale;
import java.util.Random;

/** Original, resolution-independent instrument artwork. No assets from the reference app. */
public final class LevelView extends View {
    private final Paint p=new Paint(3);
    private final RectF rect=new RectF();
    private final Shader leather;
    private double x,y,rotation;
    private int rotationTitle=R.string.rotation_angle;
    private boolean ready,held,centered;
    private long lastDescription;
    private LevelTheme theme=LevelTheme.fromId("amber");
    public LevelView(Context context) {
        super(context);
        Bitmap tile=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(tile); Random r=new Random(47);
        c.drawColor(0xff101012);
        for(int i=0;i<1800;i++) {
            float px=r.nextInt(128),py=r.nextInt(128),radius=1+r.nextFloat()*2;
            int a=10+r.nextInt(10); p.setColor(Color.rgb(a,a,a+2));
            c.drawOval(px,py,px+radius*2,py+radius*3,p);
            p.setColor(0xff070709); p.setStrokeWidth(.7f);c.drawLine(px,py+radius*3,px+radius*2,py+radius*3,p);
        }
        leather=new BitmapShader(tile,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(context.getString(R.string.waiting));
    }
    public void setThemePalette(LevelTheme theme) { this.theme=theme;invalidate(); }
    public void setReading(double x,double y,double rotation,int rotationTitle,boolean ready,boolean held,boolean centered) {
        this.x=x;this.y=y;this.rotation=rotation;this.rotationTitle=rotationTitle;this.ready=ready;this.held=held;this.centered=centered;
        long now=android.os.SystemClock.elapsedRealtime();
        if(now-lastDescription>500) {
            setContentDescription(ready?(Double.isFinite(rotation)?getContext().getString(R.string.reading_rotation,x,y,rotation):getContext().getString(R.string.reading,x,y)):getContext().getString(R.string.waiting));lastDescription=now;
        }
        postInvalidateOnAnimation();
    }
    private void fill(int color) { p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setPathEffect(null); }
    private void stroke(int color,float width) {fill(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);}
    private void round(Canvas c,float l,float t,float r,float b,float rad) { rect.set(l,t,r,b);c.drawRoundRect(rect,rad,rad,p); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(theme.background);
        float s=Math.min(getWidth()/360f,getHeight()/594f);
        c.save();c.translate((getWidth()-360*s)/2,(getHeight()-594*s)/2);c.scale(s,s);
        fill(theme.background); if(!theme.light) p.setShader(leather);c.drawRect(0,0,360,594,p);p.setShader(null);
        if(!theme.light) p.setShader(new RadialGradient(180,230,380,new int[]{0x00000000,theme.background},new float[]{.35f,1f},Shader.TileMode.CLAMP));c.drawRect(0,0,360,594,p);p.setShader(null);
        vial(c,14,42,346,116,false);
        vial(c,14,160,74,470,true);
        circular(c,230,315,94);
        rotationScale(c,230,315);
        digitalPanel(c);
        if(held) { text(c,getContext().getString(R.string.hold),180,588,11,theme.accent,Typeface.DEFAULT); }
        c.restore();
    }
    private void bezel(Canvas c,float l,float t,float r,float b,float radius) {
        fill(0xff080908);round(c,l-2,t+2,r+2,b+3,radius+1);
        p.setShader(new LinearGradient(l,t,r,b,theme.frame,new float[]{0,.13f,.55f,1},Shader.TileMode.CLAMP));
        round(c,l,t,r,b,radius);p.setShader(null);
        stroke(0xff3e403b,2);round(c,l+2,t+2,r-2,b-2,radius-1);
        stroke(0xffc2c2b2,.65f);round(c,l+4,t+4,r-4,b-4,radius-2);
        stroke(0xff11150c,5);round(c,l+8,t+8,r-8,b-8,Math.max(2,radius-5));
        stroke(0xffb8b9a4,1.2f);p.setPathEffect(new DashPathEffect(new float[]{5,4},0));
        round(c,l+6,t+6,r-6,b-6,Math.max(2,radius-3));p.setPathEffect(null);
    }
    private void vial(Canvas c,float l,float t,float r,float b,boolean vertical) {
        bezel(c,l,t,r,b,8);
        float il=l+10,it=t+10,ir=r-10,ib=b-10;
        fill(0xffa5c810);
        p.setShader(new LinearGradient(il,it,vertical?ir:il,vertical?it:ib,theme.vial,null,Shader.TileMode.CLAMP));
        round(c,il,it,ir,ib,2);p.setShader(null);
        c.save();c.clipRect(il,it,ir,ib);
        float cx=(il+ir)/2,cy=(it+ib)/2;
        stroke(theme.line,1.3f);
        if(vertical) c.drawLine(il,cy,ir,cy,p);
        else c.drawLine(cx,it,cx,ib,p);
        if(ready) {
            float shift=(float)(vertical?BubbleGeometry.vertical(y):BubbleGeometry.horizontal(x));
            if(vertical) bubble(c,cx,cy+shift*((ib-it)/2-32),(ir-il)*.42f,31);
            else bubble(c,cx+shift*((ir-il)/2-32),cy,31,23);
        }
        fill(0x33ffffff);Path shine=new Path();shine.moveTo(il,it);shine.lineTo(ir,it);shine.lineTo(il,vertical?it+(ib-it)*.42f:ib);shine.close();c.drawPath(shine,p);
        c.restore();
        if(!ready) text(c,"—",cx,cy+7,24,theme.line,Typeface.DEFAULT);
        linearScale(c,l,t,r,b,vertical);
    }
    private void linearScale(Canvas c,float l,float t,float r,float b,boolean vertical) {
        float cx=(l+r)/2,cy=(t+b)/2;
        float travel=(vertical?(b-t-20):(r-l-20))/2-32;
        for(int d=-90;d<=90;d+=15) {
            boolean major=d%30==0;
            stroke(theme.muted,major?1.2f:.7f);
            if(vertical) {
                float pos=cy-d/90f*travel;
                c.drawLine(r+2,pos,r+(major?9:6),pos,p);
                if(major)text(c,degreeLabel(d),r+21,pos+3,10,theme.text,Typeface.DEFAULT);
            } else {
                float pos=cx+d/90f*travel;
                c.drawLine(pos,b+2,pos,b+(major?10:6),p);
                if(major)text(c,degreeLabel(d),pos,b+23,10,theme.text,Typeface.DEFAULT);
            }
        }
        String caption=(vertical?"Y: ":"X: ")+(ready?format(vertical?y:x)+"°":"—");
        text(c,caption,cx,t-(vertical?9:14),vertical?11:16,theme.text,Typeface.create("sans-serif-medium",Typeface.NORMAL));
    }
    private String degreeLabel(int degrees) { return (degrees<0?"−"+(-degrees):Integer.toString(degrees))+"°"; }
    private void rotationScale(Canvas c,float cx,float cy) {
        for(int d=0;d<360;d+=5) {
            double angle=Math.toRadians(d);
            float sin=(float)Math.sin(angle),cos=(float)Math.cos(angle);
            boolean major=d%30==0;
            stroke(theme.muted,major?1.2f:.65f);
            float inner=major?99:103;
            c.drawLine(cx+sin*inner,cy-cos*inner,cx+sin*107,cy-cos*107,p);
            if(major)text(c,d+"°",cx+sin*113,cy-cos*113+3,10,theme.text,Typeface.DEFAULT);
        }
        if(ready && Double.isFinite(rotation)) {
            double a=Math.toRadians(rotation);
            Path pointer=new Path();
            pointer.moveTo(cx+(float)Math.sin(a)*110,cy-(float)Math.cos(a)*110);
            pointer.lineTo(cx+(float)Math.sin(a-.055)*97,cy-(float)Math.cos(a-.055)*97);
            pointer.lineTo(cx+(float)Math.sin(a+.055)*97,cy-(float)Math.cos(a+.055)*97);
            pointer.close();fill(theme.accent);c.drawPath(pointer,p);
        }
        text(c,getContext().getString(rotationTitle),cx,452,12,theme.muted,Typeface.DEFAULT);
        String value=ready && Double.isFinite(rotation)?format((Math.round(rotation*10)/10.)%360)+"°":"—";
        text(c,value,cx,477,23,theme.accent,Typeface.create("sans-serif-medium",Typeface.NORMAL));
    }
    private void circular(Canvas c,float cx,float cy,float radius) {
        fill(0xff080908);c.drawCircle(cx,cy+3,radius+3,p);
        p.setShader(new LinearGradient(cx-radius,cy-radius,cx+radius,cy+radius,theme.frame,null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,radius,p);p.setShader(null);
        stroke(0xff20221e,2);c.drawCircle(cx,cy,radius-3,p);
        stroke(0xffc0c1b6,1.2f);p.setPathEffect(new DashPathEffect(new float[]{5,4},0));c.drawCircle(cx,cy,radius-5,p);p.setPathEffect(null);
        fill(0xff9ad405);p.setShader(new RadialGradient(cx-12,cy-24,radius,new int[]{theme.vial[2],theme.vial[1],theme.vial[3],theme.vial[4]},new float[]{0,.5f,.86f,1},Shader.TileMode.CLAMP));c.drawCircle(cx,cy,radius-10,p);p.setShader(null);
        c.save();Path clip=new Path();clip.addCircle(cx,cy,radius-10,Path.Direction.CW);c.clipPath(clip);
        stroke(theme.line,1.3f);c.drawLine(cx-radius,cy,cx+radius,cy,p);c.drawLine(cx,cy-radius,cx,cy+radius,p);
        if(ready) {
            double[] position=BubbleGeometry.circle(x,y);
            double bx=position[0],by=position[1];
            bubble(c,cx+(float)bx*(radius-35),cy+(float)by*(radius-35),21,21);
        }
        fill(0x18ffffff);Path sheen=new Path();sheen.moveTo(cx-radius,cy+30);sheen.quadTo(cx,cy-62,cx+radius,cy-45);sheen.lineTo(cx+radius,cy-radius);sheen.lineTo(cx-radius,cy-radius);sheen.close();c.drawPath(sheen,p);
        stroke(centered?theme.ring:LevelTheme.mix(theme.ring,theme.line,.3f),1.3f);c.drawCircle(cx,cy,39,p);
        stroke(theme.ring,.7f);c.drawLine(cx-46,cy,cx+46,cy,p);c.drawLine(cx,cy-46,cx,cy+46,p);
        c.restore();
    }
    private void bubble(Canvas c,float cx,float cy,float rx,float ry) {
        fill(0xffb5da30);p.setShader(new RadialGradient(cx-rx*.22f,cy-ry*.3f,rx*1.2f,theme.bubble,new float[]{0,.68f,1},Shader.TileMode.CLAMP));
        c.drawOval(cx-rx,cy-ry,cx+rx,cy+ry,p);p.setShader(null);
        stroke(theme.line,1.5f);c.drawOval(cx-rx,cy-ry,cx+rx,cy+ry,p);
        fill(0x55ffffff);c.drawOval(cx-rx*.32f,cy-ry*.78f,cx+rx*.12f,cy-ry*.5f,p);
    }
    private void digitalPanel(Canvas c) {
        bezel(c,14,500,346,566,8);
        fill(0xff243000);p.setShader(new LinearGradient(0,511,0,555,new int[]{theme.screenTop,theme.screenBottom},null,Shader.TileMode.CLAMP));round(c,26,512,334,554,3);p.setShader(null);
        stroke(theme.muted,1);c.drawLine(180,518,180,548,p);
        text(c,"X:",48,542,21,theme.accent,Typeface.create("monospace",Typeface.ITALIC));
        text(c,"Y:",204,542,21,theme.accent,Typeface.create("monospace",Typeface.ITALIC));
        digits(c,ready?format(x):"--.-",70,523);
        digits(c,ready?format(y):"--.-",225,523);
        text(c,"°",157,532,15,theme.accent,Typeface.DEFAULT);text(c,"°",312,532,15,theme.accent,Typeface.DEFAULT);
    }
    private String format(double n) { if(Math.abs(n)<.05)n=0;return String.format(Locale.US,"%.1f",n); }
    private void digits(Canvas c,String value,float x,float y) {
        float width=0;for(char ch:value.toCharArray()) width+=ch=='.'?5:14;
        x+=(80-width)/2;
        int[] masks={63,6,91,79,102,109,125,7,127,111};
        float[][] seg={{2,0,9,0},{10,2,10,9},{10,13,10,20},{2,22,9,22},{0,13,0,20},{0,2,0,9},{2,11,9,11}};
        for(char ch:value.toCharArray()) {
            if(ch=='.') {fill(theme.accent);c.drawCircle(x+1,y+22,1.3f,p);x+=5;continue;}
            int mask=ch=='-'?64:masks[ch-'0'];
            for(int i=0;i<7;i++) {
                stroke((mask&(1<<i))!=0?theme.accent:theme.digitOff,2.2f);p.setStrokeCap(Paint.Cap.ROUND);
                float[] a=seg[i];c.drawLine(x+a[0]+(22-a[1])*.1f,y+a[1],x+a[2]+(22-a[3])*.1f,y+a[3],p);
            }
            p.setStrokeCap(Paint.Cap.BUTT);x+=14;
        }
    }
    private void text(Canvas c,String text,float x,float y,float size,int color,Typeface font) {fill(color);p.setTextSize(size);p.setTypeface(font);p.setTextAlign(Paint.Align.CENTER);c.drawText(text,x,y,p);}
}
