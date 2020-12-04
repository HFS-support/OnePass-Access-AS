package com.fgtit.app;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.utils.ExtApi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.fpi.MtGpio;
//import android.ismart.common.DeviceControl;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.Toast;

//public class ActivityList extends Application{
public class ActivityList {	
	private List<Activity> activityList = new LinkedList<Activity>();
	private static ActivityList instance;
	private Context pcontext;

	public String	DeviceSN="";
	public String	WebAddr;
	public String	UpdateUrl;
	public String	WebService;
	public boolean	isonline=true;
	public String 	DefaultUser;

	private Timer AutoResultTimer;
	private TimerTask AutoResultTask;
	private Handler AutoResultHandler;
	private int AutoResultCount=0;
	//private int iLight=0;
	private float iLight=0.0f;
	
	public boolean islinkpw=false;
	
	//Timer Check Control
	private int iLockCount=0;
    private int iAlarmCount=0;
    private boolean bLockEnable=false;
    private boolean bAlarmEnable=false;
    
    private Timer CheckTimer=null;
	private TimerTask CheckTask=null;
	private Handler CheckHandler=null;	
			
	private ActivityList(){ 
    }
    
    public static ActivityList getInstance() {
    	if(null == instance) {
    		instance = new ActivityList();
    		instance.CheckStart();
    	}
    	return instance;
    }

    public void setMainContext(Context context){
    	pcontext=context;
    }

    public void SetCurrentTime(int year,int month,int day,int hour,int min,int sec){

    		Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, min, sec);
			Intent time = new Intent("ismart.intent.action_set_curtime_millis");
			time.putExtra("millis", calendar.getTimeInMillis());
			pcontext.sendBroadcast(time);

    }
    
    public void Reboot(){
		Intent intent= new Intent("ismart.intent.action_reboot");
		pcontext.sendBroadcast(intent);
    }
    
    public void Relogon(){
    	for(Activity activity:activityList) {
    		activity.finish();
    	}
    }
        
    public void addActivity(Activity activity){
    	activityList.add(activity);
    }
    
    public void removeActivity(Activity activity){
    	activityList.remove(activity);
    }
    
    public void setNavigationBarState(boolean isHide){
		Intent intent= new Intent("ismart.intent.action_hide_navigationview");
		intent.putExtra("hide", isHide);
		pcontext.sendBroadcast(intent);
	}
    
    public void setStatusBarDisable(boolean isdisable){
	//	Intent i = new Intent("ismart.action.disable_statusbar");
	//	i.putExtra("isDisableStatusbar", isdisable);
	//	pcontext.sendBroadcast(i);
    }
    
    public void exit(){
    	for(Activity activity:activityList) {
    		activity.finish();
    	}
    	System.exit(0);
    }
    
    public void CreateDir(){
    	String	sDir=Environment.getExternalStorageDirectory() + "/OnePass";
        File destDir = new File(sDir);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		destDir = new File(sDir+"/data");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		destDir = new File(sDir+"/logo");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
    }
    
    public byte[] LoadPhotoByID(String id){
    	String	sDir=Environment.getExternalStorageDirectory() + "/OnePass";
    	String filename=sDir+"/data/"+id+".jpg";
    	if(ExtApi.IsFileExists(filename)){
    		return ExtApi.LoadDataFromFile(filename);
    	}
    	return null;
    }
    
    public void DeleteUserByID(String id){
    	String	sDir=Environment.getExternalStorageDirectory() + "/OnePass";
    	ExtApi.DeleteFile(sDir+"/data/"+id+".xml");
    	ExtApi.DeleteFile(sDir+"/data/"+id+".jpg");
    }
    
	public void SetConfigByVal(String name,String val){
		SharedPreferences sp;
		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);
		Editor edit=sp.edit();
		edit.putString(name,val);
		edit.commit();
	}

	public String GetConfigByVal(String name){
		SharedPreferences sp;
		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);
		return sp.getString(name,"");
	}

	public void SaveConfig(){
		SharedPreferences sp;
		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);
		Editor edit=sp.edit();
		edit.putString("WebAddr",WebAddr);
		edit.putString("UpdateUrl",UpdateUrl);
		edit.putString("DefaultUser",DefaultUser);
		edit.putBoolean("IsOnline", isonline);
		edit.commit();
	}

	public void LoadConfig(){
		SharedPreferences sp;
		sp = PreferenceManager.getDefaultSharedPreferences(pcontext);
		WebAddr=sp.getString("WebAddr","http://www.biofgt.con/OnePass/");
		UpdateUrl=WebAddr+"apk/update.xml";
		WebService=WebAddr+"OnePassService.asmx";
		DefaultUser=sp.getString("DefaultUser","admin");
		isonline=sp.getBoolean("IsOnline", true);

		DeviceSN=((TelephonyManager) pcontext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		
		DeviceConfig.getInstance().LoadConfig();
	}

	public void AutoResultStart() {
		AutoResultCount=0;
		/*
		InitLightness();
		iLight=GetLightness();
		*/
		if(AutoResultTimer!=null)
			return;
		AutoResultTimer = new Timer();
		AutoResultHandler = new Handler() {
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				AutoResultCount++;
				if (AutoResultCount >= 120) {
					AutoResultCount = 0;
					AutoResultStop();
					Relogon();
				}
				super.handleMessage(msg);
			}
		};
		AutoResultTask = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				AutoResultHandler.sendMessage(message);
			}
		};
		AutoResultTimer.schedule(AutoResultTask, 2000, 2000);
	}

	public void AutoResultStop() {
		if (AutoResultTimer!=null) {
			AutoResultTimer.cancel();
			AutoResultTimer = null;
			AutoResultTask.cancel();
			AutoResultTask=null;
		}
	}

	public void SetAutoResult(){
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		pcontext.registerReceiver(mBatInfoReceiver, filter);
	}

	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if(Intent.ACTION_SCREEN_ON.equals(action)){
			}else if(Intent.ACTION_SCREEN_OFF.equals(action)){
				Relogon();
			}
		}
	};

	float GetLightness() {
		WindowManager.LayoutParams localLayoutParams = ((Activity)pcontext).getWindow().getAttributes();
		float light = localLayoutParams.screenBrightness;
		return light;
	}

	public void InitLightness(){
		WindowManager.LayoutParams lp = ((Activity)pcontext).getWindow().getAttributes();
		lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
		((Activity)pcontext).getWindow().setAttributes(lp);
	}
	
    public void OpenDoor(){
    	bLockEnable=true;
		MtGpio.getInstance().LockSwitch(true);
		iLockCount=DeviceConfig.getInstance().doordelay;
    }
    
    public void CloseAlarm(){
    	bLockEnable=false;
		MtGpio.getInstance().AlarmSwitch(false);
    }
    
    private void CheckLock(){
    	if(iLockCount>0){
    		iLockCount--;
    	}
    	if(iAlarmCount>0){
    		iAlarmCount--;
    	}
    	if(bLockEnable){
    		bLockEnable=false;
    		MtGpio.getInstance().LockSwitch(false);
    	}
    	if(bAlarmEnable){
    		bAlarmEnable=false;
    		MtGpio.getInstance().AlarmSwitch(true);
    	}
    	if(MtGpio.getInstance().ButtonIsPress()){
    		bLockEnable=true;
    		MtGpio.getInstance().LockSwitch(true);
    		iLockCount=DeviceConfig.getInstance().doordelay;
    	}
    }
    
    public void CheckStart() {
		if(CheckTimer!=null)
			return;
		CheckTimer = new Timer();
		CheckHandler = new Handler() {
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				CheckLock();
				super.handleMessage(msg);
			}
		};
		CheckTask = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				CheckHandler.sendMessage(message);
			}
		};
		CheckTimer.schedule(CheckTask, 1000, 1000);
	}

	public void CheckStop() {
		if (CheckTimer!=null) {
			CheckTimer.cancel();
			CheckTimer = null;
			CheckTask.cancel();
			CheckTask=null;
		}
	}
}
