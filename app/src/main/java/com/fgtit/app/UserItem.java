package com.fgtit.app;

public class UserItem {
	
	public int 	userid;         			//�û����
	public byte usertype;       			//�û�����	0����ͨ�û�	1�������û�
	public byte groupid;        			//ʱ�����/������	ǰ4λʱ����飬��4λ������
	public String   username="";   			//�û�����	
	public byte[] 	expdate=new byte[3];	//��������
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

//��ά������ʱʶ��ʶ�����͹��ڣ���ά������json��ʽ�������������ڣ�����ֻ����һ��ʹ�õ����á�
