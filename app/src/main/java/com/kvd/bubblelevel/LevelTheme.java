package com.kvd.bubblelevel;

/** Stable preference IDs; palettes contain no measurement state. */
public final class LevelTheme {
    public static final String[] IDS={"classic","amber","blue","light"};
    public final String id;
    public final boolean light;
    public final int background,text,muted,accent,line,ring,screenTop,screenBottom,digitOff,buttonTop,buttonBottom;
    public final int[] vial,bubble,frame;
    private LevelTheme(String id,boolean light,int accent,int[] vial,int[] bubble) {
        this.id=id;this.light=light;this.accent=accent;this.vial=vial;this.bubble=bubble;
        background=light?0xffeef1f4:0xff000000;
        text=light?0xff182a35:0xffeeeeee;
        muted=light?0xff465c68:0xffb9c2c7;
        line=light?0xff224c55:mix(vial[0],0xff000000,.45f);
        ring=light?0xff205966:mix(accent,0xffffffff,.45f);
        screenTop=light?0xfff8fcfe:mix(accent,0xff000000,.94f);
        screenBottom=light?0xffdce8ec:mix(accent,0xff000000,.83f);
        digitOff=light?0xffb9cdd2:mix(accent,0xff000000,.84f);
        buttonTop=light?0xfffcfefe:0xff393939;
        buttonBottom=light?0xffd8e1e6:0xff131313;
        frame=light?new int[]{0xfff9fbfc,0xff9baeb8,0xffd9e3e8,0xff7a909b}:new int[]{0xff99998d,0xff383936,0xff696962,0xff151613};
    }
    public static LevelTheme fromId(String id) {
        if("amber".equals(id)) return new LevelTheme("amber",false,0xffffcb55,new int[]{0xff875409,0xffef9d15,0xffffd75b,0xffdc921a,0xff583506},new int[]{0xffffedb0,0xffedb947,0xff9d621a});
        if("blue".equals(id)) return new LevelTheme("blue",false,0xff73e3ff,new int[]{0xff145778,0xff36b9ec,0xffa1efff,0xff309ecf,0xff0a334b},new int[]{0xffe4fbff,0xff8adbf2,0xff2e7c9b});
        if("light".equals(id)) return new LevelTheme("light",true,0xff006e7d,new int[]{0xff398a94,0xff8ad9df,0xffd3f8f6,0xff78c9cf,0xff286c77},new int[]{0xfff9ffff,0xffb6edeb,0xff4b9da7});
        return new LevelTheme("classic",false,0xffddfa17,new int[]{0xff658a05,0xffbde908,0xffd8f732,0xffa9ce0a,0xff506204},new int[]{0xffe0f780,0xffbfdc3f,0xff729524});
    }
    public int index() { for(int i=0;i<IDS.length;i++) if(IDS[i].equals(id)) return i;return 0; }
    public static int mix(int first,int second,float amount) {
        int r=Math.round(((first>>16)&255)*(1-amount)+((second>>16)&255)*amount);
        int g=Math.round(((first>>8)&255)*(1-amount)+((second>>8)&255)*amount);
        int b=Math.round((first&255)*(1-amount)+(second&255)*amount);
        return 0xff000000|(r<<16)|(g<<8)|b;
    }
}
