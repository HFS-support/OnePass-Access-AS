package com.fgtit.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;



import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class SocketServer {
	
	private final int CMDHEADERSIZE=7;
	private final int CMDIDPOS=3;
	private final int CMDPARPOS=4;
	private final int CMDDATAPOS=7;
	
	//参数位数据定义
	private final byte ALLPKG=0;	//单个包
	private final byte STARTPKG=1;	//开始包
	private final byte NEXTPKG=2;	//数据包
	private final byte ENDPKG=3;	//结束包
	private final byte PKGERR=4;	//有错
	
	private final byte ZCMD_TESTLINK=0x00;
	private final byte ZCMD_GETVERSION=0x01; 
	private final byte ZCMD_ADJUSTTIME=0x02;
	private final byte ZCMD_UPUSERLIST=0x04;
	private final byte ZCMD_DNUSERINFO=0x05;
	private final byte ZCMD_UPUSERINFO=0x06;
	private final byte ZCMD_UPLOGRECORD=0x07;
	private final byte ZCMD_UPADMRECORD=0x08;
	private final byte ZCMD_DELALLUSERS=0x09;
	private final byte ZCMD_UPTIMEZONE=0x0A;
	private final byte ZCMD_DNTIMEZONE=0x0B;
	private final byte ZCMD_OPENDOOR=0x0C;
	private final byte ZCMD_CLOSEALARM=0x0D;
	private final byte ZCMD_DELTYPEUSERS=0x0F;
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
	private final byte ZCMD_GETSCREEN=0x25;
	private final byte ZCMD_CLOSELINK=0x26;

	private final byte ZCMD_ENROLFINGER=0x30;

	private final byte ZCMD_RFREADDATA=0x37;
	private final byte ZCMD_RFWRITEDATA=0x38;

	private final byte ZCMD_UPUSERLISTEX=0x44;

	private final byte ZCMD_SENDFONT=0x70;
	private final byte ZCMD_CLEARFONT=0x71;
	private final byte ZCMD_RTKEYSEND=0x75;

	private final byte ZCMD_FLASHCLEAR=(byte) 0xA0;
	private final byte ZCMD_FLASHREAD=(byte) 0xA1;
	private final byte ZCMD_FLASHWRITE=(byte) 0xA2;

	private final byte ZCMD_UPDATESOFT=(byte) 0xB0;
	private final byte ZCMD_SETDEVSN=(byte) 0xB1;

	private final byte ZCMD_GETTPREF=(byte) 0xC0;
	private final byte ZCMD_GETTPMAT=(byte) 0xC1;
	private final byte ZCMD_SETRESULT=(byte) 0xC2;
	private final byte ZCMD_SETVOICE=(byte) 0xC3;
	private final byte ZCMD_SETTOUCH=(byte) 0xC4;

	private final byte ZCMD_RELOADUSERS=(byte) 0xD0;
	
	
	private static SocketServer instance;
	
	private int SERVER_PORT = 5001;
	private String SERVER_IP;
		
	private ServerSocket serverSocket;
	
	private Context pcontext=null;
	private Handler phandler=null;
	
	public static SocketServer getInstance() {
    	if(null == instance) {
    		instance = new SocketServer();
    	}
    	return instance;
    }
	
	public void setContext(Context context){
		pcontext=context;
    }
	
	public void setHandler(Handler handler){
		phandler=handler;
    }
	
	public void Start(){
		//---get the IP address of itself---
		SERVER_IP = getLocalIpv4Address();
		//---start the server---
		Thread serverThread = new Thread(new ServerThread());
		serverThread.start();
	}
	
	public void Stop(){
		try {
			if(serverSocket!=null)
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//---get the local IPv4 address---
	public String getLocalIpv4Address() {
		try {
			for (Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface
					.getNetworkInterfaces(); networkInterfaceEnum
					.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaceEnum
						.nextElement();
				for (Enumeration<InetAddress> ipAddressEnum = networkInterface
						.getInetAddresses(); ipAddressEnum.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) ipAddressEnum
							.nextElement();
					// ---check that it is not a loopback address and
					// it is IPv4---
					if (!inetAddress.isLoopbackAddress()&&(inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			//Log.e("IPAddress", ex.toString());
		}
		return null;
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

	private byte[] short2byte(short s) {
		byte[] size = new byte[2];
		size[1] = (byte) (s & 0xff);
		size[0] = (byte) ((s >> 8) & 0xff);
		return size;
	}
	
	public int ZCheckSum(byte[] buffer,int size){
		int i;
		long uiCheckSum=0;
		for(i=0;i<size;i++)
		{
			uiCheckSum+=(byte)buffer[i]&0xFF;
		}
		uiCheckSum = (((~uiCheckSum)+1) & 0x0000FFFF);
		return (int) uiCheckSum;
	}
	
	public byte[] FillRetData(byte[] revdata,byte bret,byte[] indata,int insize){
		long CheckRet;			//CRC16效验,加在数据后面
		int CheckSize=CMDHEADERSIZE+insize;
		int sndsize=CheckSize+2;
		byte[] snddata=new byte[sndsize];
		
		snddata[0]=(byte)0x46;			    //报文头
		snddata[1]=(byte)0x43;
		snddata[2]=(byte)revdata[2];		//设备号
		snddata[3]=(byte)revdata[3];		//命令
		snddata[4]=(byte)bret;				//参数
		snddata[5]=(byte)(insize&0xFF);		//附加数据长度
		snddata[6]=(byte)((insize>>8)&0xFF);	
		if(insize>0){	//附加数据
			System.arraycopy(indata, 0, snddata, 7, insize);
		}
		CheckRet=ZCheckSum(snddata,CheckSize);
		snddata[CheckSize]=(byte)(CheckRet&0xFF);
		snddata[CheckSize+1]=(byte)((CheckRet>>8)&0xFF);
		return snddata;
	}
	
	public byte[] NetCommandProcess(byte[] revdata){
		switch((byte)revdata[CMDIDPOS]){
		case ZCMD_TESTLINK:{
			/*
			int len=getShort(revdata[6],revdata[5]);
			byte[] pw=new byte[8];
			System.arraycopy(revdata,CMDDATAPOS,pw,0,len);
            if(Arrays.equals(ActivityList.getInstance().password,pw)){
            	ActivityList.getInstance().islinkpw=true;
                FillRetData(revdata,ALLPKG,null,0);
            }else{
                FillRetData(revdata,PKGERR,null,0);
            }
            */
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETVERSION:{
			byte[] version=new byte[4];
			return FillRetData(revdata,ALLPKG,version,4);
			}		
		case ZCMD_CLOSELINK:{
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_OPENDOOR:{
			ActivityList.getInstance().OpenDoor();
		//	ShowMessage("Open Door ...");			
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_CLOSEALARM:{
			ActivityList.getInstance().CloseAlarm();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_ADJUSTTIME:{
			byte[] st=new byte[14];
			System.arraycopy(revdata,CMDDATAPOS,st,0,14);
			int wYear = getShort(st[1],st[0]);
			int cMonth = getShort(st[3],st[2])-1;
			int cDate = getShort(st[5],st[4]);
			int cDay = getShort(st[7],st[6]);
			int cHour = getShort(st[9],st[8]);
			int cMin = getShort(st[11],st[10]);
			int cSec = getShort(st[13],st[12]);
			ActivityList.getInstance().SetCurrentTime(wYear,cMonth,cDay,cHour,cMin,cSec);
            return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_INITOPTION:{	//初始化设备选项
			DeviceConfig.getInstance().InitConfig();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_SETOPTION:{	//设置设备选项
			int len=getShort(revdata[6],revdata[5]);
			byte[] cfg=new byte[len];
			System.arraycopy(revdata,CMDDATAPOS,cfg,0,len);
			DeviceConfig.getInstance().SetConfigBytes(cfg);
			DeviceConfig.getInstance().SaveConfig();
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_GETOPTION:{	//获取设备选项
			byte[] cfg=DeviceConfig.getInstance().GetConfigBytes();
			//SystemClock.sleep(1000);
			return FillRetData(revdata,ALLPKG,cfg,cfg.length);
			}
		case ZCMD_UPTIMEZONE:
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_DNTIMEZONE:
			return FillRetData(revdata,ALLPKG,null,0);
		case ZCMD_DNUSERINFO:{	//下载用户
			switch((byte)revdata[CMDPARPOS]){
			case STARTPKG:{	//用户信息
				int len=getShort(revdata[6],revdata[5]);
				System.arraycopy(revdata,CMDDATAPOS,TempVals.getInstance().tempInfo,0,len);
				TempVals.getInstance().fpCount=0;
				}
				break;
			case ENDPKG:	//保存
				UsersList.getInstance().AppendUser(TempVals.getInstance().tempInfo,TempVals.getInstance().tempFP);
				break;
			case NEXTPKG:	//模板
				int len=getShort(revdata[6],revdata[5]);
				System.arraycopy(revdata,CMDDATAPOS,TempVals.getInstance().tempFP,TempVals.getInstance().fpCount*512,len);
				TempVals.getInstance().fpCount++;
				break;
			}			
			return FillRetData(revdata,ALLPKG,null,0);
			}
		case ZCMD_DELALLUSERS:{	//删除所有用户
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
			int count=getInt(revdata,CMDDATAPOS);			
			byte[] bi=LogsList.getInstance().LogItemToBytes(LogsList.getInstance().logsList.get(count));
			return FillRetData(revdata,ALLPKG,bi,bi.length);
			}
		case ZCMD_UPADMRECORD1:{
			byte[] bi=new byte[10];
			return FillRetData(revdata,ALLPKG,bi,bi.length);
			}
		case ZCMD_GETLOGSCOUNT:{			
			LogsList.getInstance().Query(0,"","");
			int count=LogsList.getInstance().logsList.size();
			byte[] cb=new byte[4];
			cb[0]=(byte) (count&0xFF);
			cb[1]=(byte) ((count>>8)&0xFF);
			cb[2]=(byte) ((count>>16)&0xFF);
			cb[3]=(byte) ((count>>24)&0xFF);
			return FillRetData(revdata,ALLPKG,cb,4);
			}
		case ZCMD_GETADMSCOUNT:{
			byte[] cb=new byte[4];
			return FillRetData(revdata,ALLPKG,cb,4);
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
		return null;
	}
	
	public class ServerThread implements Runnable {
		public void run() {
			try {
				if (SERVER_IP != null) {
					ShowMessage("Server listening on IP: " + SERVER_IP);
					//---create an instance of the server socket---
					serverSocket = new ServerSocket(SERVER_PORT);
					while (true) {
						//---wait for incoming clients---
						Socket client = serverSocket.accept();
						//---the above code is a blocking call;
						// i.e. it will block until a client connects---
						//---client has connected---
						ShowMessage("Connected to client.");
						/*
						//多线程
						ClientThread ct=new ClientThread(client);
						ct.start();
						*/
						///*
						//单线程
						try {
							//---get an InputStream object to read from the
							// socket---
							InputStream inputStream = client.getInputStream();
							OutputStream outputStream =	client.getOutputStream();
							//---read all incoming data terminated with a \n
							
							while(true){
								int revsize=524;//inputStream.read();
								byte[] revdata=new byte[revsize];
								inputStream.read(revdata);
								byte[] snddata=NetCommandProcess(revdata);
								outputStream.write(snddata);
								//outputStream.write(revdata);
								if(revdata[CMDIDPOS]==ZCMD_CLOSELINK){
									break;
								}
							}
							//---client has disconnected from the server---
							ShowMessage("Client disconnected.");
						} catch (Exception e) {
							final String error = e.getLocalizedMessage();
							ShowMessage("Server Error:"+error);
						}
						//*/
					}
				} else {
				//	ShowMessage("No internet connection on device.");
				}
			} catch (Exception e) {
				final String error = e.getLocalizedMessage();
				ShowMessage("Server Error:"+error);
			}
			ShowMessage("Server exited");
		}
	}
	
	class ClientThread extends Thread
	{
		Socket client = null;
		public ClientThread(Socket sk){
			this.client = sk;
		}
		
		public void run(){
			while(true){
			try{
				InputStream inputStream = client.getInputStream();
				OutputStream outputStream =	client.getOutputStream();
				int size=524;//inputStream.read();
				byte[] revdata=new byte[size];
				inputStream.read(revdata);
				byte[] snddata=NetCommandProcess(revdata);
				outputStream.write(snddata);
				//outputStream.write(revdata);
				//ShowMessage("Test ...");
            }  
            catch (IOException e){
                e.printStackTrace();
            }  
			}
		}  
    } 
}
