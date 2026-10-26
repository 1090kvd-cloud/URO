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
    private Sensor sensor, rotationSensor;
    private final RelativeRotation flatRotation=new RelativeRotation();
    private boolean rotationRegistered,flatPosture=true;
    private double frozenRotation=Double.NaN;
    private int frozenRotationTitle=R.string.rotation_angle;
    private LevelView level;
    private TextView status, title, badge;
    private LinearLayout root;
    private LevelTheme theme;
    private Button calibrate, sound, hold, settings;
    private android.content.SharedPreferences prefs;
    private ToneGenerator tone;
    private boolean soundEnabled, announced, registered;
    private float tolerance;
    private String soundMode, activeSoundTarget="";
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
        prefs=getSharedPreferences("level",MODE_PRIVATE);
        theme=LevelTheme.fromId(prefs.getString("theme","classic"));
        setTheme(themeStyle());
        super.onCreate(state);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        prefs=getSharedPreferences("level",MODE_PRIVATE);
        soundEnabled=prefs.getBoolean("sound",false); tolerance=prefs.getFloat("tolerance",.3f);
        soundMode=SignalMode.IDS[SignalMode.index(prefs.getString("soundMode","auto"))];
        model.setCalibration(prefs.getFloat("zeroX",0),prefs.getFloat("zeroY",0));
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensor==null) sensor=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(14),0,dp(14),dp(8));
        if(Build.VERSION.SDK_INT>=35) root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(dp(14)+bars.left,bars.top,dp(14)+bars.right,dp(8)+bars.bottom);
            return insets;
        });
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        title=new TextView(this); title.setText(R.string.app_name); title.setTextColor(Color.WHITE); title.setTextSize(26); title.setTypeface(null,1);
        header.addView(title,new LinearLayout.LayoutParams(0,dp(65),1)); title.setGravity(Gravity.CENTER_VERTICAL);
        badge=button("↻",R.string.rotation_zero_short);badge.setContentDescription(getString(R.string.rotation_zero));
        badge.setOnClickListener(v->zeroFlatRotation());
        header.addView(badge,new LinearLayout.LayoutParams(dp(64),dp(50))); root.addView(header);
        level=new LevelView(this); root.addView(level,new LinearLayout.LayoutParams(-1,0,1));
        status=new TextView(this); status.setTextColor(0xffb3b8a7); status.setTextSize(12); status.setGravity(Gravity.CENTER); status.setMinHeight(dp(38));
        root.addView(status,new LinearLayout.LayoutParams(-1,dp(38)));
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER);
        calibrate=button("⊙",R.string.calibrate); sound=button("♪",R.string.sound); hold=button("▣",R.string.hold); settings=button("⚙",R.string.settings);
        for(Button b:new Button[]{calibrate,sound,hold,settings}) controls.addView(b,new LinearLayout.LayoutParams(0,dp(72),1));
        root.addView(controls); setContentView(root);
        calibrate.setOnClickListener(v->{
            if(!model.ready() || model.isHeld()) return;
            dialog().setTitle(R.string.calibrate).setMessage(R.string.calibration_help)
                .setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.set_zero,(d,w)->{
                    if(model.stable() && model.calibrate()) { saveZero(); toast(R.string.calibrated); update(); }
                    else toast(R.string.keep_still);
                }).show();
        });
        sound.setOnClickListener(v->{
            soundEnabled=!soundEnabled;prefs.edit().putBoolean("sound",soundEnabled).apply();announced=false;
            if(soundEnabled) playLevelTone(); else if(tone!=null) tone.stopTone();
            update();
        });
        sound.setOnLongClickListener(v->{soundModeDialog();return true;});
        hold.setOnClickListener(v->{
            if(!model.isHeld()) {frozenRotation=displayRotation();frozenRotationTitle=rotationTitle();}
            model.toggleHold();announced=false;updatePosture();update();
        });
        settings.setOnClickListener(v->settings()); applyTheme(); update();
    }
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private Button button(String icon,int label) {
        Button b=new Button(this); b.setText(icon+"\n"+getString(label)); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(0xffd5d5d5); b.setGravity(Gravity.CENTER); b.setPadding(0,0,0,0); b.setMinWidth(0);
        b.setContentDescription(getString(label));
        styleButton(b);
        return b;
    }
    private int themeStyle() {
        switch(theme.id) {
            case "amber": return R.style.AmberTheme;
            case "blue": return R.style.BlueTheme;
            case "light": return R.style.LightTheme;
            default: return R.style.AppTheme;
        }
    }
    private AlertDialog.Builder dialog() { return new AlertDialog.Builder(new ContextThemeWrapper(this,themeStyle())); }
    private void styleButton(Button b) {
        b.setTextColor(theme.text);
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,new int[]{theme.buttonTop,theme.buttonBottom});
        bg.setCornerRadius(dp(5)); bg.setStroke(dp(1),theme.light?0xffa2b6c0:0xff5e5e5e);
        int ripple=0x55000000 | (theme.accent & 0x00ffffff);
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(ripple),bg,null));
    }
    private void applyTheme() {
        root.setBackgroundColor(theme.background); title.setTextColor(theme.text);styleButton((Button)badge);badge.setTextColor(theme.accent);
        level.setThemePalette(theme);
        for(Button b:new Button[]{calibrate,sound,hold,settings}) styleButton(b);
        getWindow().setStatusBarColor(theme.background);getWindow().setNavigationBarColor(theme.background);
        int flags=getWindow().getDecorView().getSystemUiVisibility();
        int lightFlags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(theme.light?flags|lightFlags:flags & ~lightFlags);
    }
    private void toast(int id) { Toast.makeText(this,id,Toast.LENGTH_SHORT).show(); }
    private void saveZero() { prefs.edit().putFloat("zeroX",(float)model.zeroX()).putFloat("zeroY",(float)model.zeroY()).apply(); }
    private void settings() {
        dialog().setTitle(R.string.settings).setItems(new String[]{getString(R.string.language),getString(R.string.theme),getString(R.string.tolerance),getString(R.string.sound_mode),getString(R.string.reset_calibration),getString(R.string.help),getString(R.string.sensor_info),getString(R.string.developer),getString(R.string.donate_title)},(d,w)->{
            if(w==0) dialog().setTitle(R.string.language).setSingleChoiceItems(new String[]{getString(R.string.system_language),"Русский","English"},languageIndex(),(a,i)->{
                prefs.edit().putString("language",new String[]{"","ru","en"}[i]).apply(); a.dismiss(); recreate();
            }).setNegativeButton(R.string.cancel,null).show();
            if(w==1) dialog().setTitle(R.string.theme).setSingleChoiceItems(new String[]{getString(R.string.theme_classic),getString(R.string.theme_amber),getString(R.string.theme_blue),getString(R.string.theme_light)},theme.index(),(a,i)->{
                theme=LevelTheme.fromId(LevelTheme.IDS[i]);prefs.edit().putString("theme",theme.id).apply();
                a.dismiss();applyTheme();update();
            }).setNegativeButton(R.string.cancel,null).show();
            if(w==2) dialog().setTitle(R.string.tolerance).setSingleChoiceItems(new String[]{"0.1°","0.3°","0.5°","1.0°"},toleranceIndex(),(a,i)->{
                tolerance=new float[]{.1f,.3f,.5f,1f}[i]; prefs.edit().putFloat("tolerance",tolerance).apply(); announced=false; a.dismiss(); update();
            }).setNegativeButton(R.string.cancel,null).show();
            if(w==3) soundModeDialog();
            if(w==4) { if(model.isHeld()) toast(R.string.unlock_first); else { model.clearCalibration();saveZero();toast(R.string.calibration_reset);update(); } }
            if(w==5) dialog().setTitle(R.string.app_name).setMessage(R.string.help_text).setPositiveButton(android.R.string.ok,null).show();
            if(w==6) sensorInfo();
            if(w==7) developerInfo();
            if(w==8) donationInfo();
        }).setNegativeButton(R.string.cancel,null).show();
    }
    private void developerInfo() {
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(24),dp(16),dp(24),dp(16));
        ImageView avatar=new ImageView(this);
        avatar.setImageResource(R.drawable.developer_avatar);
        avatar.setContentDescription(getString(R.string.developer_avatar));
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        content.addView(avatar,new LinearLayout.LayoutParams(dp(128),dp(128)));
        dialog().setTitle(R.string.developer).setView(content)
            .setPositiveButton(android.R.string.ok,null).show();
    }
    private void donationInfo() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24),dp(12),dp(24),dp(16));
        scroll.addView(content);
        TextView note=new TextView(this);
        note.setText(R.string.donate_note);
        note.setTextColor(theme.text);
        note.setTextSize(15);
        note.setGravity(Gravity.CENTER);
        content.addView(note,new LinearLayout.LayoutParams(-1,-2));
        ImageView qr=new ImageView(this);
        qr.setImageResource(R.drawable.donate_qr);
        qr.setContentDescription(getString(R.string.donate_qr_description));
        qr.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams qrLayout=new LinearLayout.LayoutParams(-1,dp(240));
        qrLayout.topMargin=dp(16);
        content.addView(qr,qrLayout);
        TextView hint=new TextView(this);
        hint.setText(R.string.donate_hint);
        hint.setTextColor(theme.text);
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0,dp(12),0,dp(8));
        content.addView(hint,new LinearLayout.LayoutParams(-1,-2));
        TextView address=new TextView(this);
        address.setText(R.string.donate_url);
        address.setTextColor(theme.text);
        address.setTextSize(12);
        address.setGravity(Gravity.CENTER);
        address.setTextIsSelectable(true);
        content.addView(address,new LinearLayout.LayoutParams(-1,-2));
        dialog().setTitle(R.string.donate_title).setView(scroll)
            .setNegativeButton(android.R.string.cancel,null)
            .setPositiveButton(R.string.donate_open,(d,w)->{
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(getString(R.string.donate_url))));
                } catch(ActivityNotFoundException e) {toast(R.string.donate_no_browser);}
            }).show();
    }
    private void soundModeDialog() {
        dialog().setTitle(R.string.sound_mode).setSingleChoiceItems(new String[]{getString(R.string.sound_auto),getString(R.string.sound_both),getString(R.string.sound_x),getString(R.string.sound_y)},SignalMode.index(soundMode),(d,i)->{
            soundMode=SignalMode.IDS[i];prefs.edit().putString("soundMode",soundMode).apply();
            announced=false;activeSoundTarget="";
            if(tone!=null)tone.stopTone();
            d.dismiss();update();
        }).setNegativeButton(R.string.cancel,null).show();
    }
    private int languageIndex() { String s=prefs.getString("language",""); return s.equals("ru")?1:s.equals("en")?2:0; }
    private int toleranceIndex() { return tolerance<=.1f?0:tolerance<=.3f?1:tolerance<=.5f?2:3; }
    private double sensorTime() {return SystemClock.elapsedRealtimeNanos()/1e9;}
    private void updatePosture() {
        if(!model.ready() || model.isHeld())return;
        double inclination=model.inclinationFromFlat();
        boolean next=flatPosture?inclination<45:inclination<35;
        if(next!=flatPosture) {flatPosture=next;if(next)flatRotation.resetReference();}
    }
    private double displayRotation() {
        if(model.isHeld())return frozenRotation;
        if(!model.ready())return Double.NaN;
        return flatPosture?(rotationRegistered?flatRotation.reading(sensorTime()):Double.NaN):model.rotation();
    }
    private int rotationTitle() {
        if(model.isHeld())return frozenRotationTitle;
        if(!flatPosture)return R.string.rotation_angle;
        if(!rotationRegistered)return R.string.rotation_missing;
        return Double.isFinite(flatRotation.reading(sensorTime()))?R.string.rotation_flat:R.string.rotation_waiting;
    }
    private void zeroFlatRotation() {
        if(model.isHeld()) {toast(R.string.unlock_first);return;}
        if(!flatPosture || !rotationRegistered || !flatRotation.zero(sensorTime())) {toast(R.string.rotation_flat_hint);return;}
        toast(R.string.rotation_zero_done);update();
    }
    private String sensorName(int type) {
        Sensor found=sensors.getDefaultSensor(type);
        return found==null?getString(R.string.sensor_absent):found.getName();
    }
    private void sensorInfo() {
        String source=!rotationRegistered?getString(R.string.sensor_absent):getString(rotationSensor.getType()==Sensor.TYPE_GAME_ROTATION_VECTOR?R.string.rotation_fused:R.string.rotation_gyro);
        String message=getString(R.string.sensor_details,source,sensorName(Sensor.TYPE_GYROSCOPE),sensorName(Sensor.TYPE_MAGNETIC_FIELD));
        dialog().setTitle(R.string.sensor_info).setMessage(message).setPositiveButton(android.R.string.ok,null).show();
    }
    @Override protected void onResume() {
        super.onResume();model.resetFilter();flatRotation.reset();flatPosture=true;announced=false;
        if(sensor!=null) registered=sensors.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
        rotationSensor=sensors.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationRegistered=rotationSensor!=null && sensors.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_GAME);
        if(!rotationRegistered) {
            rotationSensor=sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            rotationRegistered=rotationSensor!=null && sensors.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_GAME);
        }
        try { tone=new ToneGenerator(AudioManager.STREAM_MUSIC,ToneGenerator.MAX_VOLUME); } catch(RuntimeException ignored) { tone=null; }
        update();
    }
    @Override protected void onPause() {
        sensors.unregisterListener(this);registered=false;rotationRegistered=false;flatRotation.reset();
        if(tone!=null) {tone.release();tone=null;}
        super.onPause();
    }
    @Override public void onSensorChanged(SensorEvent event) {
        double time=event.timestamp/1e9;
        if(rotationSensor!=null && event.sensor.getType()==rotationSensor.getType()) {
            if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE)flatRotation.gyroscope(event.values[2],time);
            else {
                double x=event.values[0],y=event.values[1],z=event.values[2];
                double w=event.values.length>=4?event.values[3]:Math.sqrt(Math.max(0,1-x*x-y*y-z*z));
                flatRotation.quaternion(x,y,z,w,time);
            }
        } else if(sensor!=null && event.sensor.getType()==sensor.getType()) {
            int rotation=getWindowManager().getDefaultDisplay().getRotation();
            model.sample(event.values[0],event.values[1],event.values[2],rotation,time);
            updatePosture();
        }
        update();
    }
    @Override public void onAccuracyChanged(Sensor s,int value) {if(sensor!=null && s.getType()==sensor.getType()) {accuracy=value;update();}}
    private boolean playLevelTone() {
        if(tone==null) { toast(R.string.audio_unavailable); return false; }
        // A sustained dual-frequency tone carries better than the old short, attenuated beep.
        boolean started=tone.startTone(ToneGenerator.TONE_DTMF_5,400);
        lastBeep=SystemClock.elapsedRealtime();
        if(!started) toast(R.string.audio_unavailable);
        return started;
    }
    private void update() {
        if(level==null) return;
        boolean available=sensor!=null && registered;
        boolean good=model.ready() && model.stable() && accuracy!=SensorManager.SENSOR_STATUS_UNRELIABLE;
        boolean centered=model.ready() && Math.abs(model.x())<=tolerance && Math.abs(model.y())<=tolerance;
        String target=SignalMode.resolve(soundMode,model.x()+model.zeroX(),model.y()+model.zeroY());
        if(!target.equals(activeSoundTarget)) {
            activeSoundTarget=target;announced=false;
            if(tone!=null)tone.stopTone();
        }
        boolean signalAligned=model.ready() && SignalMode.aligned(target,model.x(),model.y(),tolerance);
        level.setReading(model.x(),model.y(),displayRotation(),rotationTitle(),model.ready(),model.isHeld(),centered);
        badge.setEnabled(flatPosture && rotationRegistered && model.ready() && !model.isHeld() && Double.isFinite(flatRotation.reading(sensorTime())));
        badge.setAlpha(badge.isEnabled()?1f:.4f);
        calibrate.setEnabled(available && model.ready() && !model.isHeld()); calibrate.setAlpha(calibrate.isEnabled()?1f:.4f);
        hold.setEnabled(available && model.ready()); hold.setAlpha(hold.isEnabled()?1f:.4f);
        sound.setText("♪\n"+getString(soundEnabled?R.string.sound_on:R.string.sound_off)); sound.setTextColor(soundEnabled?theme.accent:theme.muted);
        sound.setContentDescription(getString(soundEnabled?R.string.sound_on:R.string.sound_off));
        hold.setText((model.isHeld()?"▣":"□")+"\n"+getString(model.isHeld()?R.string.release:R.string.hold)); hold.setContentDescription(getString(model.isHeld()?R.string.release:R.string.hold));
        int alignedText="x".equals(target)?R.string.level_x:"y".equals(target)?R.string.level_y:R.string.level_ok;
        status.setText(!available?R.string.no_sensor:!model.ready()?R.string.waiting:model.isHeld()?R.string.frozen:accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE?R.string.low_accuracy:!model.stable()?R.string.keep_still:signalAligned?alignedText:R.string.measuring);
        if(soundEnabled && available && model.ready()) {
            int control="x".equals(target)?R.string.control_x:"y".equals(target)?R.string.control_y:R.string.control_both;
            status.append(" · "+getString(control));
        }
        status.setTextColor(signalAligned && good?theme.accent:theme.muted);
        if(!signalAligned || !good || model.isHeld()) announced=false;
        long now=SystemClock.elapsedRealtime();
        if(soundEnabled && signalAligned && good && !model.isHeld() && !announced && now-lastBeep>1000 && tone!=null) {
            announced=playLevelTone();
        }
    }
}
