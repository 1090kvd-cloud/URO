package com.kvd.bubblelevel;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.*;
import android.media.*;
import android.view.*;
import android.widget.*;
import java.util.Locale;

public final class MainActivity extends Activity implements SensorEventListener {
    private final LevelModel model=new LevelModel();
    private SensorManager sensors;
    private Sensor sensor;
    private LevelView level;
    private TextView status;
    private Button calibrate, sound, hold;
    private android.content.SharedPreferences prefs;
    private ToneGenerator tone;
    private boolean soundEnabled, announced, registered;
    private float tolerance;
    private long lastBeep;
    private int accuracy=SensorManager.SENSOR_STATUS_ACCURACY_HIGH;

    @Override protected void attachBaseContext(Context base) {
        String language=base.getSharedPreferences("level",MODE_PRIVATE).getString("language","");
        if(!language.isEmpty()) {
            Configuration config=new Configuration(base.getResources().getConfiguration());
            config.setLocale(Locale.forLanguageTag(language));
            base=base.createConfigurationContext(config);
        }
        super.attachBaseContext(base);
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        prefs=getSharedPreferences("level",MODE_PRIVATE);
        soundEnabled=prefs.getBoolean("sound",false); tolerance=prefs.getFloat("tolerance",.3f);
        model.setCalibration(prefs.getFloat("zeroX",0),prefs.getFloat("zeroY",0));
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensor==null) sensor=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(14),0,dp(14),dp(8));
        if(Build.VERSION.SDK_INT>=35) root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(dp(14)+bars.left,bars.top,dp(14)+bars.right,dp(8)+bars.bottom);
            return insets;
        });
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this); title.setText(R.string.app_name); title.setTextColor(Color.WHITE); title.setTextSize(26); title.setTypeface(null,1);
        header.addView(title,new LinearLayout.LayoutParams(0,dp(65),1)); title.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge=new TextView(this); badge.setText("⊕"); badge.setTextColor(0xffd7fa09); badge.setTextSize(42); badge.setGravity(Gravity.CENTER);
        badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        header.addView(badge,new LinearLayout.LayoutParams(dp(48),dp(56))); root.addView(header);
        level=new LevelView(this); root.addView(level,new LinearLayout.LayoutParams(-1,0,1));
        status=new TextView(this); status.setTextColor(0xffb3b8a7); status.setTextSize(12); status.setGravity(Gravity.CENTER); status.setMinHeight(dp(38));
        root.addView(status,new LinearLayout.LayoutParams(-1,dp(38)));
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER);
        calibrate=button("⊙",R.string.calibrate); sound=button("♪",R.string.sound); hold=button("▣",R.string.hold); Button settings=button("⚙",R.string.settings);
        for(Button b:new Button[]{calibrate,sound,hold,settings}) controls.addView(b,new LinearLayout.LayoutParams(0,dp(72),1));
        root.addView(controls); setContentView(root);
        calibrate.setOnClickListener(v->{
            if(!model.ready() || model.isHeld()) return;
            new AlertDialog.Builder(this).setTitle(R.string.calibrate).setMessage(R.string.calibration_help)
                .setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.set_zero,(d,w)->{
                    if(model.stable() && model.calibrate()) { saveZero(); toast(R.string.calibrated); update(); }
                    else toast(R.string.keep_still);
                }).show();
        });
        sound.setOnClickListener(v->{soundEnabled=!soundEnabled; prefs.edit().putBoolean("sound",soundEnabled).apply(); announced=false; update();});
        hold.setOnClickListener(v->{model.toggleHold(); announced=false; update();});
        settings.setOnClickListener(v->settings()); update();
    }
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private Button button(String icon,int label) {
        Button b=new Button(this); b.setText(icon+"\n"+getString(label)); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(0xffd5d5d5); b.setGravity(Gravity.CENTER); b.setPadding(0,0,0,0); b.setMinWidth(0);
        b.setContentDescription(getString(label));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff393939,0xff131313});
        bg.setCornerRadius(dp(5)); bg.setStroke(dp(1),0xff5e5e5e);
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x55c7e807),bg,null));
        return b;
    }
    private void toast(int id) { Toast.makeText(this,id,Toast.LENGTH_SHORT).show(); }
    private void saveZero() { prefs.edit().putFloat("zeroX",(float)model.zeroX()).putFloat("zeroY",(float)model.zeroY()).apply(); }
    private void settings() {
        new AlertDialog.Builder(this).setTitle(R.string.settings).setItems(new String[]{getString(R.string.language),getString(R.string.tolerance),getString(R.string.reset_calibration),getString(R.string.help)},(d,w)->{
            if(w==0) new AlertDialog.Builder(this).setTitle(R.string.language).setSingleChoiceItems(new String[]{getString(R.string.system_language),"Русский","English"},languageIndex(),(a,i)->{
                prefs.edit().putString("language",new String[]{"","ru","en"}[i]).apply(); a.dismiss(); recreate();
            }).setNegativeButton(R.string.cancel,null).show();
            if(w==1) new AlertDialog.Builder(this).setTitle(R.string.tolerance).setSingleChoiceItems(new String[]{"0.1°","0.3°","0.5°","1.0°"},toleranceIndex(),(a,i)->{
                tolerance=new float[]{.1f,.3f,.5f,1f}[i]; prefs.edit().putFloat("tolerance",tolerance).apply(); announced=false; a.dismiss(); update();
            }).setNegativeButton(R.string.cancel,null).show();
            if(w==2) { if(model.isHeld()) toast(R.string.unlock_first); else { model.clearCalibration();saveZero();toast(R.string.calibration_reset);update(); } }
            if(w==3) new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.help_text).setPositiveButton(android.R.string.ok,null).show();
        }).setNegativeButton(R.string.cancel,null).show();
    }
    private int languageIndex() { String s=prefs.getString("language",""); return s.equals("ru")?1:s.equals("en")?2:0; }
    private int toleranceIndex() { return tolerance<=.1f?0:tolerance<=.3f?1:tolerance<=.5f?2:3; }
    @Override protected void onResume() {
        super.onResume(); model.resetFilter(); announced=false;
        if(sensor!=null) registered=sensors.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
        try { tone=new ToneGenerator(AudioManager.STREAM_MUSIC,35); } catch(RuntimeException ignored) { tone=null; }
        update();
    }
    @Override protected void onPause() {
        sensors.unregisterListener(this); registered=false;
        if(tone!=null) { tone.release(); tone=null; }
        super.onPause();
    }
    @Override public void onSensorChanged(SensorEvent event) {
        int rotation=getWindowManager().getDefaultDisplay().getRotation();
        model.sample(event.values[0],event.values[1],event.values[2],rotation,event.timestamp/1e9);
        update();
    }
    @Override public void onAccuracyChanged(Sensor s,int value) { accuracy=value; update(); }
    private void update() {
        if(level==null) return;
        boolean available=sensor!=null && registered;
        boolean good=model.ready() && model.stable() && accuracy!=SensorManager.SENSOR_STATUS_UNRELIABLE;
        boolean centered=model.ready() && Math.abs(model.x())<=tolerance && Math.abs(model.y())<=tolerance;
        level.setReading(model.x(),model.y(),model.ready(),model.isHeld(),centered);
        calibrate.setEnabled(available && model.ready() && !model.isHeld()); calibrate.setAlpha(calibrate.isEnabled()?1f:.4f);
        hold.setEnabled(available && model.ready()); hold.setAlpha(hold.isEnabled()?1f:.4f);
        sound.setText("♪\n"+getString(soundEnabled?R.string.sound_on:R.string.sound_off)); sound.setTextColor(soundEnabled?0xffd8f446:0xffb4b4b4);
        sound.setContentDescription(getString(soundEnabled?R.string.sound_on:R.string.sound_off));
        hold.setText((model.isHeld()?"▣":"□")+"\n"+getString(model.isHeld()?R.string.release:R.string.hold)); hold.setContentDescription(getString(model.isHeld()?R.string.release:R.string.hold));
        status.setText(!available?R.string.no_sensor:!model.ready()?R.string.waiting:model.isHeld()?R.string.frozen:accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE?R.string.low_accuracy:!model.stable()?R.string.keep_still:centered?R.string.level_ok:R.string.measuring);
        status.setTextColor(centered && good?0xffd2ee59:0xffb3b8a7);
        if(!centered || !good || model.isHeld()) announced=false;
        long now=SystemClock.elapsedRealtime();
        if(soundEnabled && centered && good && !model.isHeld() && !announced && now-lastBeep>1000 && tone!=null) {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP,100); lastBeep=now; announced=true;
        }
    }
}
