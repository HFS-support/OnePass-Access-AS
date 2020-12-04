package com.fgtit.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.access.R;
import com.fgtit.app.ActivityList;
import com.fgtit.app.UserItem;
import com.fgtit.app.UsersList;
import com.fgtit.utils.ExtApi;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.os.Build;

public class EmployeeActivity extends Activity {

	private ListView listView1,listView2;
	private List<Map<String, Object>> mData1,mData2;
	
	private ImageView photoImage; 
	
	private int 	pos=0;
	private UserItem person=null;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_employee);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        pos = bundle.getInt("pos");        
		//int pos=this.getIntent().getIntExtra("pos",0);
        if(pos<UsersList.getInstance().usersList.size())
        	person=UsersList.getInstance().usersList.get(pos);
		
        photoImage=(ImageView)findViewById(R.id.imageView1);
        if(person.photo.length()>1000){
        	photoImage.setImageBitmap(Bytes2Bimap(ExtApi.Base64ToBytes(person.photo)));
        }else{
        	byte[] photo=ActivityList.getInstance().LoadPhotoByID(String.valueOf(person.userid));
        	if(photo!=null)
        		photoImage.setImageBitmap(Bytes2Bimap(photo));
        	else
        		photoImage.setImageBitmap(ExtApi.LoadBitmap(getResources(),R.drawable.guest));
        }
        
		listView1=(ListView) findViewById(R.id.listView1);	
		SimpleAdapter adapter1 = new SimpleAdapter(this,getData1(),R.layout.listview_empitem,
				new String[]{"title","info"},
				new int[]{R.id.title,R.id.info});
		listView1.setAdapter(adapter1);
		
		listView2=(ListView) findViewById(R.id.listView2);	
		SimpleAdapter adapter2 = new SimpleAdapter(this,getData2(),R.layout.listview_empitem,
				new String[]{"title","info"},
				new int[]{R.id.title,R.id.info});
		listView2.setAdapter(adapter2);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.employee, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.action_delete:
			if(pos>=0){
				UsersList.getInstance().DeleteUser(person.userid);
				ActivityList.getInstance().DeleteUserByID(String.valueOf(person.userid));
				UsersList.getInstance().usersList.remove(pos);				
				//GlobalData.getInstance().SaveUsersList();				
				Intent resultIntent = new Intent();  
				//resultIntent.putExtra("reload", 1);  
				setResult(0, resultIntent);  
				this.finish();
			}
			//this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private List<Map<String, Object>> getData1() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		
		if(person!=null){			
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_username)+":");
			map.put("info", person.username);
			list.add(map);
		
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_userid)+":");
			map.put("info", person.userid);
			list.add(map);
		}
				
		mData1=list;		
		return list;
	}
	
	private List<Map<String, Object>> getData2() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		if(person!=null){
			
			Map<String, Object> map = new HashMap<String, Object>();
			/*
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_statu)+":");
			map.put("info", "Normal");
			list.add(map);
			*/
			
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_usertype)+":");
			switch(person.usertype){
			case 0:	map.put("info", getString(R.string.txt_selact));	break;
			case 1:	map.put("info", getString(R.string.txt_selict));	break;
			}
			list.add(map);
					
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_gender)+":");
			switch(person.gender){
			case 0:	map.put("info", getString(R.string.txt_male));	break;
			case 1:	map.put("info", getString(R.string.txt_female));	break;
			}			
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_phone)+":");
			map.put("info", person.phone);
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", getString(R.string.txt_remark)+":");
			map.put("info", "");
			list.add(map);
		
		}
		mData2=list;		
		return list;
	}
	
	private Bitmap Bytes2Bimap(byte[] b){  
        if(b.length!=0){  
            return BitmapFactory.decodeByteArray(b, 0, b.length);  
        }  
        else {  
            return null;  
        }  
	}
}
