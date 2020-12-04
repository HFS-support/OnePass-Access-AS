package com.fgtit.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.access.R;
import com.fgtit.app.ActivityList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MenuActivity extends Activity{

	private ListView listView;
	private List<Map<String, Object>> mData;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView=(ListView) findViewById(R.id.listView1);	
		SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.listview_menuitem,
				new String[]{"title","info","img"},
				new int[]{R.id.title,R.id.info,R.id.img});
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override  
			public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {  
				//Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);  
				switch(pos){
				case 0:{
						Intent intent = new Intent(MenuActivity.this, RecordsActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
					break;
				case 1:{
						Intent intent = new Intent(MenuActivity.this, EmployeesActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
					break;				
				case 2:{
						Intent intent = new Intent(MenuActivity.this, SystemActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
					break;
				case 3:{
						Intent intent = new Intent(MenuActivity.this, NetworkActivity.class);
						startActivity(intent);	
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
					break;
				case 4:{
						Intent intent = new Intent(MenuActivity.this, UtilitiesActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);						
					}
					break;
				case 5:{
						Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
					break;
				case 6:{						
						ActivityList.getInstance().exit();
					}
				}
			}             
		});
        
	/*	ActivityList.getInstance().setNavigationBarState(false);
		ActivityList.getInstance().setStatusBarDisable(false);
		*/
		ActivityList.getInstance().addActivity(this);
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();

		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_01));
		map.put("info", getString(R.string.txt_info_01));
		map.put("img", R.drawable.menu_details);
		list.add(map);
				
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_02));
		map.put("info", getString(R.string.txt_info_02));
		map.put("img", R.drawable.menu_users);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_03));
		map.put("info", getString(R.string.txt_info_03));
		map.put("img", R.drawable.menu_option);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_04));
		map.put("info", getString(R.string.txt_info_04));
		map.put("img", R.drawable.menu_network);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_05));
		map.put("info", getString(R.string.txt_info_05));
		map.put("img", R.drawable.menu_updown);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_06));
		map.put("info", getString(R.string.txt_info_06));
		map.put("img", R.drawable.menu_about);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_07));
		map.put("info", getString(R.string.txt_info_07));
		map.put("img", R.drawable.menu_exit);
		list.add(map);
		
		mData=list;		
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.system, menu);
		return true;
	}

	private void ReturnMain(){
/*		ActivityList.getInstance().setNavigationBarState(true);
		ActivityList.getInstance().setStatusBarDisable(true);*/
		this.setResult(1);
		this.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:			
			ReturnMain();
			return true;
		case R.id.action_settings:
			//Intent intent = new Intent(this, SettingsActivity.class);
			//startActivity(intent);			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override  
 	public boolean onKeyDown(int keyCode, KeyEvent event) {  
 	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
 	    	ReturnMain();
 	    	return true;  
 	    }
 	    return super.onKeyDown(keyCode, event);  
 	}
}
