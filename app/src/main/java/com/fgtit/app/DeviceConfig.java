package com.fgtit.app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import android.os.Environment;

public class DeviceConfig {
	
	private static DeviceConfig instance;
	public static DeviceConfig getInstance() {
    	if(null == instance) {
    		instance = new DeviceConfig();
    	}
    	return instance;
    }
	
	public byte[]	mark=new byte[4];
	public String	devname="FT-06";		//设备名称
	public byte[]	password=new byte[8];	//通讯密码
	public String	welcome="Welcome to";	//欢迎显示
	public byte		devid=1;				//机号
	public byte  	ldhcp=1;				//自动获取IP地址
	public byte[]	macaddr=new byte[6];	//MAC地址
	public long 	lip=0xc0a80112;			//本地IP地址
	public long 	lsub=0xffffff00;		//本地子网掩码
	public long 	lgate=0xc0a80101;		//本地网关
	public long		lport=5001;				//本地端口
	public long		rip=0xc0a80164;			//远程服务器IP地址
	public long		rport=5002;				//远程服务器端口
	public long 	baud=115200;			//RS485 波特率
	public byte		lang=1;					//语言
	public byte		commtype=0;				//通讯类型，0，使用全部方式
	public byte		thresholdn=50;			//1:N 安全级别
	public byte		threshold1=50;			//1:1 安全级别
	public byte		thresholdc=50;			//指纹卡安全级别
	public byte		locksignal1=1;			//锁延时		//SENSOR1
	public byte		locksignal2=0;			//锁信号		//SENSOR2
	public byte		wiegand=0;				//维根
	public byte		alarmdelay=10;			//报警延时
	public byte		doordelay=5;			//开门延时
	public byte		idlekey=15;				//多少秒后关按键等
	public byte		idledsp=30;				//多少秒后关屏幕背光
	public byte		workmode=0;				//本地安全方式，0完全，1，部分，只可以设置通讯等，2，禁用本地管理	
	public byte		remotemode=0;			//0，本地识别，1远程识别
	public byte		identifymode=0;			//识别方式
	public byte 	devicetype=0;			//0:标准指纹门禁机,1:指纹门禁控制器,2:指纹门禁读头
	public byte[]	devsn=new byte[4];		//设备唯一序列号，存放到FRAM或FLASH里，出厂前设置，
		
	public void LoadConfig(){
		String fileName=Environment.getExternalStorageDirectory() + "/OnePass/device.db";
		if(IsFileExists(fileName)){
			InitConfig();
		}else{
			try {
				byte[] cb=new byte[100];
				RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
				long fileLength = randomFile.length();
				if(fileLength==100){
					randomFile.read(cb);
					SetConfigBytes(cb);
				}else{
					InitConfig();
				}
				randomFile.close();
			} catch (IOException e) {
			}	
		}
	}
	
	public void InitConfig(){
		mark[0]=(byte)0x58;
		mark[1]=(byte)0x49;
		mark[2]=(byte)0x41;
		mark[3]=(byte)0x4F;
		
		macaddr[0]=(byte)0x20;			//MAC地址
		macaddr[1]=(byte)0x17;
		macaddr[2]=(byte)0x04;
		macaddr[3]=(byte)0x14;
		macaddr[4]=(byte)0x08;
		macaddr[5]=(byte)0x30;
		
		devsn[0]=(byte)0x17;	//设备唯一序列号，存放到FRAM或FLASH里，出厂前设置，以后不更改
		devsn[1]=(byte)0x04;
		devsn[2]=(byte)0x14;
		devsn[3]=(byte)0x01;
		
		password[0]=0x30;
		password[1]=0x30;
		password[2]=0x30;
		password[3]=0x30;
		password[4]=0x30;
		password[5]=0x30;
		password[6]=0x30;
		password[7]=0x30;
		
		devname="FT-06";
		welcome="Welcome to";
		devid=1;
		ldhcp=1;
		lip=0xc0a80112;
		lsub=0xffffff00;
		lgate=0xc0a80101;
		lport=5001;
		rip=0xc0a80164;
		rport=5002;
		baud=115200;
		lang=1;
		commtype=0;			
		thresholdn=50;	
		threshold1=50;	
		thresholdc=50;		
		locksignal1=1;	
		locksignal2=0;	
		wiegand=0;		
		alarmdelay=10;
		doordelay=5;	
		idlekey=15;	
		idledsp=30;	
		workmode=0;		
		remotemode=0;
		identifymode=0;	
		devicetype=0;
	}

	public byte[] GetConfigBytes(){
		byte[] cb=new byte[100];

		System.arraycopy(mark,0, cb, 0, 4);
		
		try {
			byte[] p1=devname.getBytes("gb2312");
			System.arraycopy(p1,0, cb, 4, p1.length);
		} catch (UnsupportedEncodingException e) {
		}
		
		System.arraycopy(password,0, cb, 20, 8);
		
		try {
			byte[] p2=welcome.getBytes("gb2312");
			System.arraycopy(p2,0, cb, 28, p2.length);
		} catch (UnsupportedEncodingException e) {
		}
		
		cb[44]=devid;
		cb[45]=ldhcp;
		
		System.arraycopy(macaddr,0, cb, 46, 6);
		
	
		cb[52]=(byte) (lip&0xFF);	cb[53]=(byte) ((lip>>8)&0xFF);		cb[54]=(byte) ((lip>>16)&0xFF);		cb[55]=(byte) ((lip>>24)&0xFF);
		cb[56]=(byte) (lsub&0xFF);	cb[57]=(byte) ((lsub>>8)&0xFF);		cb[58]=(byte) ((lsub>>16)&0xFF);	cb[59]=(byte) ((lsub>>24)&0xFF);
		cb[60]=(byte) (lgate&0xFF);	cb[61]=(byte) ((lgate>>8)&0xFF);	cb[62]=(byte) ((lgate>>16)&0xFF);	cb[63]=(byte) ((lgate>>24)&0xFF);
		cb[64]=(byte) (lport&0xFF);	cb[65]=(byte) ((lport>>8)&0xFF);	cb[66]=(byte) ((lport>>16)&0xFF);	cb[67]=(byte) ((lport>>24)&0xFF);
		cb[68]=(byte) (rip&0xFF);	cb[69]=(byte) ((rip>>8)&0xFF);		cb[70]=(byte) ((rip>>16)&0xFF);		cb[71]=(byte) ((rip>>24)&0xFF);
		cb[72]=(byte) (rport&0xFF);	cb[73]=(byte) ((rport>>8)&0xFF);	cb[74]=(byte) ((rport>>16)&0xFF);	cb[75]=(byte) ((rport>>24)&0xFF);
		cb[76]=(byte) (baud&0xFF);	cb[77]=(byte) ((baud>>8)&0xFF);		cb[78]=(byte) ((baud>>16)&0xFF);	cb[79]=(byte) ((baud>>24)&0xFF);
		
		cb[80]=lang;
		cb[81]=commtype;
		cb[82]=thresholdn;
		cb[83]=threshold1;
		cb[84]=thresholdc;
		cb[85]=locksignal1;
		cb[86]=locksignal2;
		cb[87]=wiegand;
		cb[88]=alarmdelay;
		cb[89]=doordelay;
		cb[90]=idlekey;
		cb[91]=idledsp;
		cb[92]=workmode;
		cb[93]=remotemode;
		cb[94]=identifymode;
		cb[95]=devicetype;			
				
		System.arraycopy(devsn,0, cb, 96, 4);
		
		return cb;
	}
		
	public void SetConfigBytes(byte[] cb){
		System.arraycopy(cb,0, mark, 0, 4);
		System.arraycopy(cb,46, macaddr, 0, 6);
		System.arraycopy(cb,96, devsn, 0, 4);
		System.arraycopy(cb,20, password, 0, 8);
		devname=new String(cb, 4, 16);
		devname=devname.replaceAll("\\s","");
		welcome=new String(cb, 28, 16);
		welcome=welcome.replaceAll("\\s","");
				
		devid=cb[44];
		ldhcp=cb[45];
		
		lip=(cb[52]&0xFF)|((cb[53]<<8)&0xFF00)|((cb[54]<<16)&0xFF0000)|((cb[55]<<24)&0xFF000000);	
		lsub=(cb[56]&0xFF)|((cb[57]<<8)&0xFF00)|((cb[58]<<16)&0xFF0000)|((cb[59]<<24)&0xFF000000);
		lgate=(cb[60]&0xFF)|((cb[61]<<8)&0xFF00)|((cb[62]<<16)&0xFF0000)|((cb[63]<<24)&0xFF000000);
		lport=(cb[64]&0xFF)|((cb[65]<<8)&0xFF00)|((cb[66]<<16)&0xFF0000)|((cb[67]<<24)&0xFF000000);		
		rip=(cb[68]&0xFF)|((cb[69]<<8)&0xFF00)|((cb[70]<<16)&0xFF0000)|((cb[71]<<24)&0xFF000000);	
		rport=(cb[72]&0xFF)|((cb[73]<<8)&0xFF00)|((cb[74]<<16)&0xFF0000)|((cb[75]<<24)&0xFF000000);		
		baud=(cb[76]&0xFF)|((cb[77]<<8)&0xFF00)|((cb[78]<<16)&0xFF0000)|((cb[79]<<24)&0xFF000000);
		
		lang=cb[80];			
		commtype=cb[81];			
		thresholdn=cb[82];	
		threshold1=cb[83];	
		thresholdc=cb[84];		
		locksignal1=cb[85];	
		locksignal2=cb[86];	
		wiegand=cb[87];		
		alarmdelay=cb[88];
		doordelay=cb[89];	
		idlekey=cb[90];	
		idledsp=cb[91];	
		workmode=cb[92];		
		remotemode=cb[93];
		identifymode=cb[94];	
		devicetype=cb[95];
	}
	
	public void SaveConfig(){
		String fileName=Environment.getExternalStorageDirectory() + "/OnePass/device.db";
		
		File f=new File(fileName);
		if(f.exists()){
			f.delete();
		}
		
		byte[] cb=GetConfigBytes();
		try {
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			randomFile.write(cb);
			randomFile.close();
		} catch (IOException e) {
		}	
	}
	
	public static boolean IsFileExists(String filename){
		File f=new File(filename);
		if(f.exists()){
			return true;
		}
		return false;
	}

	public static long ipToLong(String strIp) {
        String[]ip = strIp.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }
	
	public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf(((longIp & 0xFFFFFFFF) >> 24)& 0xFF));
        sb.append(".");
        sb.append(String.valueOf(((longIp & 0x00FFFFFF) >> 16)& 0xFF));
        sb.append(".");
        sb.append(String.valueOf(((longIp & 0x0000FFFF) >> 8)& 0xFF));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x000000FF)& 0xFF));
        return sb.toString();
    }
	
	public static boolean checkIP(String str) {
		Pattern pattern = Pattern
	                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
	                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
	        return pattern.matcher(str).matches();
	}
	
	public static boolean checkNumber(String str){
		//Pattern p = Pattern.compile("^[-+]?[0-9]");
		Pattern p = Pattern.compile("[0-9]*"); 
		return p.matcher(str).matches();
	}
}
