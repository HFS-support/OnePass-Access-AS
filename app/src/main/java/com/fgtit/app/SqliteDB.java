package com.fgtit.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class SqliteDB {
	
	private SQLiteDatabase db;
	
	public void open(){
		db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/OnePass/onepass.db",null);
	}
	
	public void createTable(){ 
		//������SQL��� 
		String stu_table="create table usertable(_id integer primary key autoincrement,sname text,snumber text)"; 
		//ִ��SQL��� 
		db.execSQL(stu_table); 
		
		String sql="CREATE TABLE TB_USERS(userid CHAR[16] PRIMARY KEY ASC,"
				+ "cardsn	CHAR[32],"
				+ "username CHAR[24],"
				+ "password CHAR[16],"
				+ "userstyle INTEGER,"
				+ "identifytype INTEGER,"
				+ "groupid INTEGER,"
				+ "jobtype INTEGER,"
				+ "depttype INTEGER,"
				+ "leveltype INTEGER,"
				+ "usetype INTEGER,"
				+ "enroldate DATE,"
				+ "expdate DATE,"
				+ "remarks VARCHAR(100),"
				+ "finger1 INTEGER,"
				+ "finger2 INTEGER,"
				+ "photo BLOB,"
				+ "template1 BLOB,"
				+ "template2 BLOB);";
		
		db.execSQL(sql);
	}
	
	public void insertData(){ 
		//ʵ��������ֵ 
		ContentValues cValue = new ContentValues(); 
		//����û��� 
		cValue.put("sname","xiaoming"); 
		//������� 
		cValue.put("snumber","01005"); 
		//����insert()������������ 
		db.insert("stu_table",null,cValue); 
	} 
	
	public void insert(){ 
		//��������SQL��� 
		String stu_sql="insert into stu_table(sname,snumber) values('xiaoming','01005')"; 
		//ִ��SQL��� 
		db.execSQL(stu_sql); 
	} 
	
	public void deleteData() { 
		//ɾ������ 
		String whereClause = "id=?"; 
		//ɾ���������� 
		String[] whereArgs = {String.valueOf(2)}; 
		//ִ��ɾ�� 
		db.delete("stu_table",whereClause,whereArgs); 
	} 
	
	public void delete() { 
		//ɾ��SQL��� 
		String sql = "delete from stu_table where _id = 6"; 
		//ִ��SQL��� 
		db.execSQL(sql); 
	} 
	
	public void updateData() { 
		//ʵ��������ֵ 
		ContentValues values = new ContentValues(); 
		//��values��������� 
		values.put("snumber","101003"); 
		//�޸����� 
		String whereClause = "id=?"; 
		//�޸���Ӳ��� 
		String[] whereArgs={String.valueOf(1)}; 
		//�޸� 
		db.update("usertable",values,whereClause,whereArgs); 
	}
	
	public void update(){ 
		//�޸�SQL��� 
		String sql = "update stu_table set snumber = 654321 where id = 1"; 
		//ִ��SQL 
		db.execSQL(sql); 
	}
	
	public void drop(){ 
		//ɾ�����SQL��� 
		String sql ="DROP TABLE stu_table"; 
		//ִ��SQL 
		db.execSQL(sql); 
	} 
	
	/*
	getCount()	����ܵ���������
 	isFirst()	�ж��Ƿ��һ����¼
 	isLast()	�ж��Ƿ����һ����¼
 	moveToFirst()	�ƶ�����һ����¼
 	moveToLast()	�ƶ������һ����¼
 	move(int offset)	�ƶ���ָ����¼
 	moveToNext()	�ƶ�����һ����¼
 	moveToPrevious()	�ƶ�����һ����¼
 	getColumnIndexOrThrow(String  columnName)	���������ƻ��������
 	getInt(int columnIndex)	���ָ����������int����ֵ
 	getString(int columnIndex)	���ָ������Ӱ��String����ֵ 
	 */
	public void query(SQLiteDatabase db) { 
		//��ѯ����α� 
		Cursor cursor = db.query ("usertable",null,null,null,null,null,null); 
		
		//�ж��α��Ƿ�Ϊ�� 
		if(cursor.moveToFirst()){ 
			//�����α� 
			for(int i=0;i<cursor.getCount();i++){ 
				cursor.move(i); 
				//���ID 
				int id = cursor.getInt(0); 
				//����û��� 
				String username=cursor.getString(1); 
				//������� 
				String password=cursor.getString(2); 
				//����û���Ϣ System.out.println(id+":"+sname+":"+snumber); 
			} 
		} 
	}
}
