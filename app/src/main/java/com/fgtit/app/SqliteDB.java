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
		//创建表SQL语句 
		String stu_table="create table usertable(_id integer primary key autoincrement,sname text,snumber text)"; 
		//执行SQL语句 
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
		//实例化常量值 
		ContentValues cValue = new ContentValues(); 
		//添加用户名 
		cValue.put("sname","xiaoming"); 
		//添加密码 
		cValue.put("snumber","01005"); 
		//调用insert()方法插入数据 
		db.insert("stu_table",null,cValue); 
	} 
	
	public void insert(){ 
		//插入数据SQL语句 
		String stu_sql="insert into stu_table(sname,snumber) values('xiaoming','01005')"; 
		//执行SQL语句 
		db.execSQL(stu_sql); 
	} 
	
	public void deleteData() { 
		//删除条件 
		String whereClause = "id=?"; 
		//删除条件参数 
		String[] whereArgs = {String.valueOf(2)}; 
		//执行删除 
		db.delete("stu_table",whereClause,whereArgs); 
	} 
	
	public void delete() { 
		//删除SQL语句 
		String sql = "delete from stu_table where _id = 6"; 
		//执行SQL语句 
		db.execSQL(sql); 
	} 
	
	public void updateData() { 
		//实例化内容值 
		ContentValues values = new ContentValues(); 
		//在values中添加内容 
		values.put("snumber","101003"); 
		//修改条件 
		String whereClause = "id=?"; 
		//修改添加参数 
		String[] whereArgs={String.valueOf(1)}; 
		//修改 
		db.update("usertable",values,whereClause,whereArgs); 
	}
	
	public void update(){ 
		//修改SQL语句 
		String sql = "update stu_table set snumber = 654321 where id = 1"; 
		//执行SQL 
		db.execSQL(sql); 
	}
	
	public void drop(){ 
		//删除表的SQL语句 
		String sql ="DROP TABLE stu_table"; 
		//执行SQL 
		db.execSQL(sql); 
	} 
	
	/*
	getCount()	获得总的数据项数
 	isFirst()	判断是否第一条记录
 	isLast()	判断是否最后一条记录
 	moveToFirst()	移动到第一条记录
 	moveToLast()	移动到最后一条记录
 	move(int offset)	移动到指定记录
 	moveToNext()	移动到下一条记录
 	moveToPrevious()	移动到上一条记录
 	getColumnIndexOrThrow(String  columnName)	根据列名称获得列索引
 	getInt(int columnIndex)	获得指定列索引的int类型值
 	getString(int columnIndex)	获得指定列缩影的String类型值 
	 */
	public void query(SQLiteDatabase db) { 
		//查询获得游标 
		Cursor cursor = db.query ("usertable",null,null,null,null,null,null); 
		
		//判断游标是否为空 
		if(cursor.moveToFirst()){ 
			//遍历游标 
			for(int i=0;i<cursor.getCount();i++){ 
				cursor.move(i); 
				//获得ID 
				int id = cursor.getInt(0); 
				//获得用户名 
				String username=cursor.getString(1); 
				//获得密码 
				String password=cursor.getString(2); 
				//输出用户信息 System.out.println(id+":"+sname+":"+snumber); 
			} 
		} 
	}
}
