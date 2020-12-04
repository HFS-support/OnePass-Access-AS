package com.fgtit.app;

public class UserItem {
	
	public int 	userid;         			//用户编号
	public byte usertype;       			//用户类型	0，普通用户	1，管理用户
	public byte groupid;        			//时间段组/开锁组	前4位时间段组，后4位开锁组
	public String   username="";   			//用户姓名	
	public byte[] 	expdate=new byte[3];	//到期日期
	public byte[]   enlcon1=new byte[5];
	public byte[]   enlcon2=new byte[5];
	public byte[]   enlcon3=new byte[5];
	public byte[]	fp1=new byte[512];
	public byte[]	fp2=new byte[512];
	public byte[]	fp3=new byte[512];
	public byte[]   enllNO=new byte[4];
	public String	photo="";
	public String	phone="";
	public int 	gender=0;
}

//二维码搞成临时识别，识别过后就过期，二维码数据json格式，包含过期日期，或者只允许一次使用等设置。
