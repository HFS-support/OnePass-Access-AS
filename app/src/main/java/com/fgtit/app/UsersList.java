package com.fgtit.app;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.fgtit.fpcore.FPMatch;

public class UsersList {
	
	private static UsersList instance;
	public static UsersList getInstance() {
    	if(null == instance) {
    		instance = new UsersList();
    	}
    	return instance;
    }
	
	public List<UserItem> usersList=new ArrayList<UserItem>();
		
	private SQLiteDatabase db;
	
	public static boolean IsFileExists(String filename){
		File f=new File(filename);
		if(f.exists()){
			return true;
		}
		return false;
	}
	
	public void LoadAll(){
		usersList.clear();
		
		if(IsFileExists(Environment.getExternalStorageDirectory() + "/OnePass/users.db")){
			db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/OnePass/users.db",null);
		}else{
			db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/OnePass/users.db",null);
			String sql="CREATE TABLE TB_USERS(userid INTEGER PRIMARY KEY ASC,"
					+ "usertype INTEGER,"
					+ "groupid INTEGER,"
					+ "username CHAR[24],"					
					+ "expdate BLOB,"
					+ "enlcon1 BLOB,"
					+ "enlcon2 BLOB,"
					+ "enlcon3 BLOB,"
					+ "fp1 BLOB,"
					+ "fp2 BLOB,"
					+ "fp3 BLOB,"
					+ "enllNO BLOB);";
			
			db.execSQL(sql);
		}
		///*
		Cursor cursor = db.query ("TB_USERS",null,null,null,null,null,null);
		if(cursor!=null){
			if(cursor.moveToFirst()){
				for(int i=0;i<cursor.getCount();i++){
					//cursor.move(i);
					UserItem ui=new UserItem();	
					ui.userid=cursor.getInt(0);
					ui.usertype=(byte) cursor.getInt(1);
					ui.groupid=(byte) cursor.getInt(2);
					ui.username=cursor.getString(3);
					ui.expdate=cursor.getBlob(4);
					ui.enlcon1=cursor.getBlob(5);
					ui.enlcon2=cursor.getBlob(6);
					ui.enlcon3=cursor.getBlob(7);
					ui.fp1=cursor.getBlob(8);
					ui.fp2=cursor.getBlob(9);
					ui.fp3=cursor.getBlob(10);
					ui.enllNO=cursor.getBlob(11);
					usersList.add(ui);
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		///*/
	}
	
	private short getShort(byte b1, byte b2) {
		short temp = 0;
		temp |= (b1 & 0xff);
		temp <<= 8;
		temp |= (b2 & 0xff);
		return temp;
	}
	
	public void ClearUsers(){
		usersList.clear();
		String sql = "delete from TB_USERS"; 
		db.execSQL(sql); 
	}
	
	public void DeleteUser(int 	userid){
		String sql = "delete from TB_USERS where userid="+String.valueOf(userid); 
		db.execSQL(sql); 
	}
	
	public void AppendUser(UserItem ui){
		usersList.add(ui);
		String sql="insert into TB_USERS(userid,usertype,groupid,username,expdate,enlcon1,enlcon2,enlcon3,fp1,fp2,fp3,enllNO) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] args = new Object[]{ui.userid,ui.usertype,ui.groupid,ui.username,
							ui.expdate,
							ui.enlcon1,ui.enlcon2,ui.enlcon3,
							ui.fp1,ui.fp2,ui.fp3,ui.enllNO};
		db.execSQL(sql,args); 
	}
	
	public void AppendUser(byte[] tempInfo,byte[] tempFP){
		UserItem ui=new UserItem();
		
		ui.userid=getShort(tempInfo[1],tempInfo[0]);
		ui.usertype=tempInfo[2];
		ui.groupid=tempInfo[3];
		ui.username=new String();
		try {
			ui.username=new String(tempInfo, 4, 16,"gb2312");
		} catch (UnsupportedEncodingException e) {
		}
		ui.username=ui.username.replaceAll("\\s","");
		System.arraycopy(tempInfo, 20, ui.expdate,0, 3);
		System.arraycopy(tempInfo, 23, ui.enlcon1,0, 5);
		System.arraycopy(tempInfo, 28, ui.enlcon2,0, 5);
		System.arraycopy(tempInfo, 33, ui.enlcon3,0, 5);
		
		int fpcount=0;
		if(ui.enlcon1[0]==1){
			System.arraycopy(tempFP, 512*fpcount, ui.fp1,0, 512);
			fpcount++;
		}
		if(ui.enlcon2[0]==1){
			System.arraycopy(tempFP, 512*fpcount, ui.fp2,0, 512);
			fpcount++;
		}
		if(ui.enlcon3[0]==1){
			System.arraycopy(tempFP, 512*fpcount, ui.fp3,0, 512);
			fpcount++;
		}
		usersList.add(ui);
		
		String sql="insert into TB_USERS(userid,usertype,groupid,username,expdate,enlcon1,enlcon2,enlcon3,fp1,fp2,fp3) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?)";
		Object[] args = new Object[]{ui.userid,ui.usertype,ui.groupid,ui.username,
							ui.expdate,
							ui.enlcon1,ui.enlcon2,ui.enlcon3,
							ui.fp1,ui.fp2,ui.fp3};
		db.execSQL(sql,args); 
	}
	
	public UserItem FindUserItemByFP(byte[] model){
		for(int i=0;i<usersList.size();i++){
			if(usersList.get(i).enlcon1[0]==1){
				if(FPMatch.getInstance().MatchTemplate(model, usersList.get(i).fp1)>60){
					return usersList.get(i);
				}
			}
			if(usersList.get(i).enlcon2[0]==1){
				if(FPMatch.getInstance().MatchTemplate(model, usersList.get(i).fp2)>60){
					return usersList.get(i);
				}
			}
			if(usersList.get(i).enlcon3[0]==1){
				if(FPMatch.getInstance().MatchTemplate(model, usersList.get(i).fp3)>60){
					return usersList.get(i);
				}
			}
		}
		return null;
	}
	
	
	private boolean bytesEquals(byte[] src,int spos,byte[] dst,int dpos,int size){
		for(int i=0;i<size;i++){
			if(src[spos+i]!=dst[dpos+i])
				return false;
		}
		return true;
	}
	
	public UserItem FindUserItemByCard(byte[] cardsn){
		for(int i=0;i<usersList.size();i++){
			if(usersList.get(i).enlcon1[0]==2){
				if(bytesEquals(cardsn,0,usersList.get(i).enlcon1,1,4)){
					return usersList.get(i);
				}
			}
			if(usersList.get(i).enlcon2[0]==2){
				if(bytesEquals(cardsn,0,usersList.get(i).enlcon2,1,4)){
					return usersList.get(i);
				}
			}
			if(usersList.get(i).enlcon3[0]==2){
				if(bytesEquals(cardsn,0,usersList.get(i).enlcon3,1,4)){
					return usersList.get(i);
				}
			}
		}
		return null;
	}
	
	public UserItem FindUserItemByCardex(byte[] cardsn){
		for(int i=0;i<usersList.size();i++){
				if(bytesEquals(cardsn,0,usersList.get(i).enllNO,0,4)){
					return usersList.get(i);
				}
		}
		return null;
	}
	
	public boolean UserIsExists(int id){
		for(int i=0;i<usersList.size();i++){
			if(usersList.get(i).userid==id)
				return true;
		}
		return false;
	}
}
