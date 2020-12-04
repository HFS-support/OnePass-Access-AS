package android.fpi;

import android.os.Build;
import android.util.Log;
import android.zyapi.CommonApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @hide
 * @author 1
 *
 */
public class MtGpio {
	
	private boolean mOpen = false;
	private CommonApi mCommonApi= new CommonApi();;
	private static MtGpio mMe = null;
	private MtGpio() {
	//	mOpen = openDev()>=0?true:false;
	//	Log.d("MtGpio","openDev->ret:"+mOpen);
	}
	
	public static MtGpio getInstance(){
		if (mMe == null){
			mMe = new MtGpio();
			mMe.InitGpio();
		}
		return mMe;
	}
	
	public void FPPowerSwitch(boolean bonoff){
		String devname = Build.MODEL;
		String version = Build.DISPLAY;
		if (devname.equals("FT06") || devname.equals("FT-06")) {
			String versionStr = version.substring(6, 7) + version.substring(8, 9) + version
					.substring(10, 11);
			Log.d("SerialPortManager", versionStr);
			Pattern p = Pattern.compile("[0-9]*");
			Matcher m = p.matcher(versionStr);
			if (m.matches()) {
				int versionInt = Integer.parseInt(versionStr);
				if (versionInt >= 201) {
					if(bonoff){
						//FP Power
						mCommonApi.setGpioMode(15,0);
						mCommonApi.setGpioDir(15,1);
						mCommonApi.setGpioOut(15,1);
					}else{
						mCommonApi.setGpioMode(15,0);
						mCommonApi.setGpioDir(15,1);
						mCommonApi.setGpioOut(15,0);
					}
				}
			}
			if(bonoff){
				//FP Power
				mCommonApi.setGpioMode(14,0);
				mCommonApi.setGpioDir(14,1);
				mCommonApi.setGpioOut(14,1);
			}else{
				mCommonApi.setGpioMode(14,0);
				mCommonApi.setGpioDir(14,1);
				mCommonApi.setGpioOut(14,0);
			}
		}
		if (devname.equals("HF-A5")) {
			if(bonoff){
				//FP Power
				mCommonApi.setGpioMode(15,0);
				mCommonApi.setGpioDir(15,1);
				mCommonApi.setGpioOut(15,1);
			}else{
				mCommonApi.setGpioMode(15,0);
				mCommonApi.setGpioDir(15,1);
				mCommonApi.setGpioOut(15,0);
			}
		}

	}

	public void InitGpio(){
		
		mCommonApi.setGpioMode(21,0);
		mCommonApi.setGpioDir(21,1);
		
		mCommonApi.setGpioMode(34,0);
		mCommonApi.setGpioDir(34,1);
	}
	
	public void LockSwitch(boolean bonoff){
		if(bonoff){			
		mCommonApi.setGpioOut(21,1);
		mCommonApi.setGpioOut(34,1);
		}else{
		mCommonApi.setGpioOut(21,0);
		mCommonApi.setGpioOut(34,0);
		}
	}
	
	public void AlarmSwitch(boolean bonoff){
		if(bonoff){
		//	sGpioOut(13,1);
		}else{
	    //	sGpioOut(13,0);
		}
	}
	
	public boolean ButtonIsPress(){
		if(mCommonApi.getGpioIn(19)==1)
			return true;
		else
			return false;
	}
	
	public boolean isOpen(){
		return mOpen;
	}
}
