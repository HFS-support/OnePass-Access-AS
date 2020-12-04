package com.fgtit.app;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

public class LocatorServer {
	
	private final byte TAG_CMD = (byte) 0xff;
	private final byte TAG_STATUS = (byte) 0xfe;
	private final byte CMD_DISCOVER_TARGET = 0x02;
	
	private static LocatorServer instance;	
	private Context pcontext=null;
	private Handler phandler=null;
	private byte[] LocatorData=new byte[84];
	
	public static LocatorServer getInstance() {
    	if(null == instance) {
    		instance = new LocatorServer();
    	}
    	return instance;
    }
	
	public void setContext(Context context){
		pcontext=context;
    }
	
	public void setHandler(Handler handler){
		phandler=handler;
    }
	
	public void ShowMessage(final String msg){
		if(phandler!=null){
			phandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(pcontext, msg, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public void startUdpServer() {
		
		for(int ulIdx = 0; ulIdx < 84; ulIdx++){
	     LocatorData[ulIdx]=0;
		}
	    // Fill in the header for the response data.
		LocatorData[0] = TAG_STATUS;
		LocatorData[1] = 84;
		LocatorData[2] = CMD_DISCOVER_TARGET;
	    // Save the board type in the response data.
		LocatorData[3] = (byte) 0xAC;
		// Save the board ID in the response data.
		LocatorData[4] = (byte) 0x0C;
		// Save the MAC address.
		LocatorData[9] = DeviceConfig.getInstance().macaddr[0];
		LocatorData[10] = DeviceConfig.getInstance().macaddr[1];
		LocatorData[11] = DeviceConfig.getInstance().macaddr[2];
		LocatorData[12] = DeviceConfig.getInstance().macaddr[3];
		LocatorData[13] = DeviceConfig.getInstance().macaddr[4];
		LocatorData[14] = DeviceConfig.getInstance().macaddr[5];
		// Save the firmware version number in the response data.
		LocatorData[15] = (byte) ((DeviceConfig.getInstance().lport) & 0xff);
		LocatorData[16] = (byte) ((DeviceConfig.getInstance().lport >> 8) & 0xff);
		LocatorData[17] = (byte) ((DeviceConfig.getInstance().lport >> 16) & 0xff);
		LocatorData[18] = (byte) ((DeviceConfig.getInstance().lport >> 24) & 0xff);

	    String szDevSn=String.format("%02X%02X%02X%02X", DeviceConfig.getInstance().devsn[0],
	    		DeviceConfig.getInstance().devsn[1],
	    		DeviceConfig.getInstance().devsn[2],
	    		DeviceConfig.getInstance().devsn[3]);
	    String szDevMac=String.format("%02X%02X%02X%02X%02X%02X", DeviceConfig.getInstance().macaddr[0],
	    		DeviceConfig.getInstance().macaddr[1],
	    		DeviceConfig.getInstance().macaddr[2],
	    		DeviceConfig.getInstance().macaddr[3],
	    		DeviceConfig.getInstance().macaddr[4],
	    		DeviceConfig.getInstance().macaddr[5]);
	    String szTmp="|";
	    byte[] szTitle=new byte[64];
		try {
			byte[] p1 = DeviceConfig.getInstance().devname.getBytes("gb2312");
			byte[] p2=szDevSn.getBytes("gb2312");
		    byte[] p3=szDevMac.getBytes("gb2312");
		    byte[] p4=szTmp.getBytes("gb2312");
		    System.arraycopy(p1,0, szTitle,0,p1.length);
		    System.arraycopy(p4,0, szTitle,p1.length,p4.length);
		    System.arraycopy(p2,0, szTitle,p1.length+p4.length,p2.length);
		    System.arraycopy(p4,0, szTitle,p1.length+p4.length+p2.length,p4.length);
		    System.arraycopy(p3,0, szTitle,p1.length+p4.length+p2.length+p4.length,p3.length);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	    // Copy the application title string into the response data.
	    for(int ulCount = 0; ulCount < 64; ulCount++){
	    	LocatorData[ulCount + 19] = szTitle[ulCount];
	    }
	    
        new Thread(){
            @Override  
            public void run() { 
                try{  
                	//InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
                    DatagramSocket ds = new DatagramSocket(1024);
                    ShowMessage("Start UDP Server ...");
                    while(true){
                    	byte[] revdata = new byte[128];
                    	DatagramPacket dpr = new DatagramPacket(revdata,128);
                    	ds.receive(dpr);
                    	if((revdata[0] == TAG_CMD) && (revdata[1] == 4) &&
                    		       (revdata[2] == CMD_DISCOVER_TARGET) &&
                    		       (revdata[3] == (byte)((0 - TAG_CMD - 4 - CMD_DISCOVER_TARGET) & 0xff))){
                    		ShowMessage("Finder...");
                    		SystemClock.sleep(100);
                    		InetAddress addr = dpr.getAddress();  
                        	int port = dpr.getPort();                      	  
                        	DatagramPacket dps = new DatagramPacket(LocatorData,LocatorData.length,addr,port);  
                        	ds.send(dps);
                    	}else{
                    		//InetAddress addr = dpr.getAddress();  
                        	//int port = dpr.getPort();                      	  
                        	//DatagramPacket dps = new DatagramPacket(LocatorData,LocatorData.length,addr,port);  
                        	//ds.send(dps);
                    	}
                    }
                    //ds.close();  
                }catch (Exception e){  
                	ShowMessage("UDP Error");
                }  
            }  
        }.start();  
    }  
}
