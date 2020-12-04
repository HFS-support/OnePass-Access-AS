package com.fgtit.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fgtit.access.R;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;
import android_serialport_api.AsyncFingerprint.OnPSGetIdCardNOListener;
import android_serialport_api.AsyncFingerprint.OnRS485Listener;
import android_serialport_api.AsyncFingerprint.OnRS485exListener;

public class Fingerprint {
	
	public static final int STATE_NONE = 0;
	public static final int STATE_PLACE = 1;	
	public static final int STATE_LIFT = 2;
	public static final int STATE_GETIMAGE = 3;
	public static final int STATE_UPIMAGE = 4;
	public static final int STATE_GENDATA = 5;
	public static final int STATE_UPDATA = 6;
	public static final int STATE_RS485 = 8;
	public static final int STATE_RS485ex = 9;
	public static final int STATE_GetCardNOex = 10;
	public static final int STATE_FAIL = 7;
	private byte[] bufferex;
	private static Fingerprint instance;	
	
	public static Fingerprint getInstance() {
    	if(null == instance) {
    		instance = new Fingerprint();
    	}
    	return instance;
    }
	
	private Context pcontext=null;
	private Handler phandler=null;
	
	private AsyncFingerprint vFingerprint;
    private boolean			 bfpWork=false;
    private boolean			 bIsUpImage=true;
    private boolean			 bIsCancel=false;
    	
	
    
    private final int CMDHEADERSIZE=7;
	private final int CMDIDPOS=3;
	private final int CMDPARPOS=4;
	private final int CMDDATAPOS=7;
	
	//����λ���ݶ���
	private final byte ALLPKG=0;	//������
	private final byte STARTPKG=1;	//��ʼ��
	private final byte NEXTPKG=2;	//���ݰ�
	private final byte ENDPKG=3;	//������
	private final byte PKGERR=4;	//�д�
	
	private final byte ZCMD_TESTLINK=0x00;
	private final byte ZCMD_GETVERSION=0x01; 
	private final byte ZCMD_ADJUSTTIME=0x02;
	private final byte ZCMD_DNUSERINFO=0x05;
	private final byte ZCMD_DELALLUSERS=0x09;
	private final byte ZCMD_UPTIMEZONE=0x0A;
	private final byte ZCMD_DNTIMEZONE=0x0B;
	private final byte ZCMD_OPENDOOR=0x0C;
	private final byte ZCMD_CLOSEALARM=0x0D;
	private final byte ZCMD_GETLOGSCOUNT=0x10;
	private final byte ZCMD_GETADMSCOUNT=0x11;
	private final byte ZCMD_CLEARALLLOGS=0x12;
	private final byte ZCMD_UPLOGRECORD1=0x17;
	private final byte ZCMD_UPADMRECORD1=0x18;
	private final byte ZCMD_DELSPECUSER=0x19;
	private final byte ZCMD_SETOPTION=0x20;
	private final byte ZCMD_GETOPTION=0x21;
	private final byte ZCMD_INITOPTION=0x22;
	private final byte ZCMD_RESETDEV=0x23;
	private final byte ZCMD_CLOSELINK=0x26;
	private final byte ZCMD_SETDEVSN=(byte) 0xB1;
	private final byte ZCMD_RELOADUSERS=(byte) 0xD0;
	public void setContext(Context context){
		pcontext=context;
    }
	
	public void setHandler(Handler handler){
		phandler=handler;
    }
	
	private void SendMessage(int msg,byte[] data){
		if(phandler!=null)
			phandler.obtainMessage(msg, data).sendToTarget();
	}
	
	public void SetUpImage(boolean isup){
		bIsUpImage=isup;
	}
	
	public boolean IsUpImage(){
		return bIsUpImage;
	}
	
	public void Open(){
		vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
		
		vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener() {
            @Override
            public void onGetImageSuccess() {
            	SendMessage(STATE_GETIMAGE,null);
                if(bIsUpImage){
                    vFingerprint.FP_UpImage();
                }else{
                    vFingerprint.FP_GenChar(1);
                }
            }

            @Override
            public void onGetImageFail() {
                if(!bIsCancel){
                	vFingerprint.FP_RS422(null);
                }else{
                	bIsCancel=false;
                	bfpWork=false;
                }
            }
        });

        vFingerprint.setOnUpImageListener(new AsyncFingerprint.OnUpImageListener() {
            @Override
            public void onUpImageSuccess(byte[] data) {
                SendMessage(STATE_UPIMAGE,data);
                vFingerprint.FP_GenChar(1);
            }

            @Override
            public void onUpImageFail() {
                bfpWork=false;
                SendMessage(STATE_FAIL,null);
            }
        });

        vFingerprint.setOnGenCharListener(new AsyncFingerprint.OnGenCharListener() {
            @Override
            public void onGenCharSuccess(int bufferId) {
            	SendMessage(STATE_GENDATA,null);
                vFingerprint.FP_UpChar();
            }

            @Override
            public void onGenCharFail() {
            	bfpWork=false;
                SendMessage(STATE_FAIL,null);
            }
        });

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {
            	bfpWork=false;
            	SendMessage(STATE_UPDATA,model);
            }

            @Override
            public void onUpCharFail() {
            	bfpWork=false;
                SendMessage(STATE_FAIL,null);
            }
        });
		vFingerprint.setOnRS485Listener(new OnRS485Listener(){
			@Override
			public void onRS485Success(byte[] data) {
			//	SendMessage(STATE_RS485,data);
				bufferex=data;
		    	String text="";    	
		    	for(int i=0;i<data.length;i++) {
		    		text=text+Integer.toHexString(data[i]&0xFF).toUpperCase();
		    	}
				Log.i("cylnhs", text);
				NetCommandProcess(data);	
			}

			@Override
			public void onRS485Fail() {
		//		SendMessage(STATE_PLACE,null);
		 //  		vFingerprint.FP_GetIdCardNO();
		//   		bfpWork=false;
		   		SendMessage(STATE_PLACE,null);
		   		vFingerprint.FP_GetImage();
		   		bfpWork=false;
			}
		});
		vFingerprint.setOnRS485exListener(new OnRS485exListener(){
			@Override
			public void onRS485exSuccess(byte[] data) {
				  NetCommandProcessex(bufferex);

			}

			@Override
			public void onRS485exFail() {
		   		try {
		   			Thread.currentThread();
		   			Thread.sleep(20);
		   		}catch (InterruptedException e)
		   		{
		   			e.printStackTrace();
		   		}
		   		SendMessage(STATE_PLACE,null);
		   		vFingerprint.FP_GetImage();
		   		bfpWork=false;
			}
		});
		vFingerprint.setOnGetIdCardNOListener(new OnPSGetIdCardNOListener() {
			@Override
			public void onPSGetIdCardNOSuccess(byte[] bufferId) {

		    	SendMessage(STATE_GetCardNOex,bufferId);
			}

			@Override
			public void onPSGetIdCardNOFail() {
			}
		});
	}

	public void OpenIo(){
		try {
			SerialPortManager.getInstance().setUpGpio();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void CloseIo(){
		try {
			SerialPortManager.getInstance().setDownGpio();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Close(){
		bIsCancel=true;
        bfpWork=false;
        SerialPortManager.getInstance().closeSerialPort();
	}
	public void CloseProcess(){
		bfpWork = false;
	}
	
	public void Process(){
		if(!bfpWork){
			bIsCancel=false;
			SendMessage(STATE_PLACE,null);
            vFingerprint.FP_GetImage();
            bfpWork=true;
        }
	}
	public void GetIdCardNO(){		
		vFingerprint.FP_GetIdCardNO();
	}
	
	public void Cancel(){
		bIsCancel=true;
	}

    public byte[] NetCommandProcess(byte[] revdata){
		switch((byte)revdata[0]){
		case ZCMD_TESTLINK:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETVERSION:{
			return FillRetData(revdata,ALLPKG,null,0);
			}		
		case ZCMD_CLOSELINK:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_OPENDOOR:{
			ActivityList.getInstance().OpenDoor();
		//	ShowMessage("Open Door ...");
		//	return FillRetData(revdata,ALLPKG,null,0);
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_CLOSEALARM:{
			ActivityList.getInstance().CloseAlarm();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_ADJUSTTIME:{
/*			byte[] st=new byte[14];
			System.arraycopy(revdata,1,st,0,14);
			int wYear = getShort(st[1],st[0]);
			int cMonth = getShort(st[3],st[2])-1;
			int cDate = getShort(st[5],st[4]);
			int cDay = getShort(st[7],st[6]);
			int cHour = getShort(st[9],st[8]);
			int cMin = getShort(st[11],st[10]);
			int cSec = getShort(st[13],st[12]);
			ActivityList.getInstance().SetCurrentTime(wYear,cMonth,cDay,cHour,cMin,cSec);*/
            return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_INITOPTION:{	//��ʼ���豸ѡ��
			DeviceConfig.getInstance().InitConfig();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_SETOPTION:{	//�����豸ѡ��
			int len=getShort(revdata[6],revdata[5]);
			byte[] cfg=new byte[len];
			System.arraycopy(revdata,1,cfg,0,len);
			DeviceConfig.getInstance().SetConfigBytes(cfg);
			DeviceConfig.getInstance().SaveConfig();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETOPTION:{	//��ȡ�豸ѡ��
			
			//SystemClock.sleep(1000);
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_UPTIMEZONE:
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_DNTIMEZONE:
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_DNUSERINFO:{	//�����û�		
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_DELALLUSERS:{	//ɾ�������û�
			UsersList.getInstance().ClearUsers();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_DELSPECUSER:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_RELOADUSERS:{
			UsersList.getInstance().LoadAll();
			return FillRetData(revdata,ALLPKG,null,0);
			}        
		case ZCMD_UPLOGRECORD1:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_UPADMRECORD1:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETLOGSCOUNT:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETADMSCOUNT:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_CLEARALLLOGS:
			LogsList.getInstance().Clear();
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_RESETDEV:
			ActivityList.getInstance().Reboot();
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_SETDEVSN:
			System.arraycopy(revdata,CMDDATAPOS,DeviceConfig.getInstance().devsn,0,4);			
			return FillRetData(revdata,ALLPKG,null,0);
		}
		return FillRetData(revdata,ALLPKG,null,0);
	}
    
    public byte[] NetCommandProcessex(byte[] revdata){
		switch((byte)revdata[0]){
		case ZCMD_TESTLINK:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x08,0x00,0x06,0x02,0x01,0x02,0x02,0x00,0x15};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_GETVERSION:{
			byte[] version=new byte[]{0x02,0x04,0x00,0x05,0x00,0x09,0x03};
			return FillRetDataex(version,version.length);
			}		
		case ZCMD_CLOSELINK:{
			byte[] version=new byte[]{0x02,0x04,0x00,0x05,0x00,0x09,0x03};
			return FillRetDataex(version,version.length);
			}
		case ZCMD_OPENDOOR:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_CLOSEALARM:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_ADJUSTTIME:{
			byte[] version=new byte[]{0x02,0x04,0x00,0x05,0x00,0x09,0x03};
			return FillRetDataex(version,version.length);
			}
		case ZCMD_INITOPTION:{	//��ʼ���豸ѡ��
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_SETOPTION:{	//�����豸ѡ��
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_GETOPTION:{	//��ȡ�豸ѡ��
			byte[] cfg=DeviceConfig.getInstance().GetConfigBytes();
			//SystemClock.sleep(1000);
			return FillRetDataex(cfg,cfg.length);
			}
		case ZCMD_UPTIMEZONE:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_DNTIMEZONE:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_DNUSERINFO:{	//�����û�
			switch((byte)revdata[CMDPARPOS]){
			case STARTPKG:{	//�û���Ϣ
				int len=getShort(revdata[6],revdata[5]);
				System.arraycopy(revdata,CMDDATAPOS,TempVals.getInstance().tempInfo,0,len);
				TempVals.getInstance().fpCount=0;
				}
				break;
			case ENDPKG:	//����
				UsersList.getInstance().AppendUser(TempVals.getInstance().tempInfo,TempVals.getInstance().tempFP);
				break;
			case NEXTPKG:	//ģ��
				int len=getShort(revdata[6],revdata[5]);
				System.arraycopy(revdata,CMDDATAPOS,TempVals.getInstance().tempFP,TempVals.getInstance().fpCount*512,len);
				TempVals.getInstance().fpCount++;
				break;
			}
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_DELALLUSERS:{	//ɾ�������û�
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_DELSPECUSER:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_RELOADUSERS:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}        
		case ZCMD_UPLOGRECORD1:{
			int count=getInt(revdata,1);			
			byte[] bi=LogsList.getInstance().LogItemToBytes(LogsList.getInstance().logsList.get(count));
			return FillRetDataex(bi,bi.length);
			}
		case ZCMD_UPADMRECORD1:{
			byte[] bi=new byte[10];
			return FillRetDataex(bi,bi.length);
			}
		case ZCMD_GETLOGSCOUNT:{
			LogsList.getInstance().Query(0,"","");
			int count=LogsList.getInstance().logsList.size();
			byte[] cb=new byte[4];
			cb[0]=(byte) (count&0xFF);
			cb[1]=(byte) ((count>>8)&0xFF);
			cb[2]=(byte) ((count>>16)&0xFF);
			cb[3]=(byte) ((count>>24)&0xFF);
			return FillRetDataex(cb,4);
			}
		case ZCMD_GETADMSCOUNT:{
			byte[] cb=new byte[4];
			return FillRetDataex(cb,4);
			}
		case ZCMD_CLEARALLLOGS:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_RESETDEV:{
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
			}
		case ZCMD_SETDEVSN:
			byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
			vFingerprint.FP_RS422ex(buffer);
			return null;
		}
		byte[] buffer={(byte)0xef,0x01,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x07,0x00,0x03,0x00,0x00,0x0a};
		vFingerprint.FP_RS422ex(buffer);
		return null;
	}
	public byte[] FillRetData(byte[] revdata,byte bret,byte[] indata,int insize){
		vFingerprint.FP_RS422ex(null);
		return null;
	}
	public byte[] FillRetDataex(byte[] indata,int insize){
		long CheckRet;		     //CRC16Ч��,�������ݺ���
		int CheckSize=3+insize;
		int sndsize=6+CheckSize+2;
		byte[] snddata=new byte[sndsize];
		
		snddata[0]=(byte)0xef;	    //����ͷ
		snddata[1]=(byte)0x01;      //����ͷ
		snddata[2]=(byte)0xff;		//����ͷ
		snddata[3]=(byte)0xff;		//����ͷ
		snddata[4]=(byte)0xff;      //����ͷ
		snddata[5]=(byte)0xff;      //����ͷ
		snddata[6]=(byte)0x08;      //����ʶ
		snddata[8]=(byte)((insize+2)&0xFF);		//�������ݳ���
		snddata[7]=(byte)(((insize+2)>>8)&0xFF);
		if(insize>0){	//��������
			System.arraycopy(indata, 0, snddata, 9, insize);
		}
		short sum = 0;
		for (int j = 0; j < CheckSize; j++) {
			sum += (snddata[j+6] & 0xff);								
		}
		byte[] size = short2byte(sum);
		snddata[sndsize - 2] = size[0];
		snddata[sndsize - 1] = size[1];
		vFingerprint.FP_RS422ex(snddata);
    	String text="";    	
    	for(int i=0;i<snddata.length;i++) {
    		text=text+Integer.toHexString(snddata[i]&0xFF).toUpperCase();
    	}
		Log.i("cylnhs", text);
		return null;
	}
	private byte[] short2byte(short s) {
		byte[] size = new byte[2];
		size[1] = (byte) (s & 0xff);
		size[0] = (byte) ((s >> 8) & 0xff);
		return size;
	}
	private short getShort(byte b1, byte b2) {
		short temp = 0;
		temp |= (b1 & 0xff);
		temp <<= 8;
		temp |= (b2 & 0xff);
		return temp;
	}
	
	private int getInt(byte[] data,int offset) {
		int temp = 0;
		temp |= (data[offset+3] & 0xff);
		temp <<= 24;
		temp |= (data[offset+2] & 0xff);
		temp <<= 16;
		temp |= (data[offset+1] & 0xff);
		temp <<= 8;
		temp |= (data[offset+0] & 0xff);
		return temp;
	}

	
	public int ZCheckSum(byte[] buffer,int size){
		int i;
		long uiCheckSum=0;
		for(i=0;i<size;i++)
		{
			uiCheckSum+=(byte)buffer[i+6]&0xFF;
		}
		uiCheckSum = (((~uiCheckSum)+1) & 0x0000FFFF);
		return (int) uiCheckSum;
	}
}
