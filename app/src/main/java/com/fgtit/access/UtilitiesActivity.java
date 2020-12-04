package com.fgtit.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.app.ActivityList;
import com.fgtit.access.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.os.Build;

public class UtilitiesActivity extends Activity {

	private ListView listView;
	private List<Map<String, Object>> mData;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_utilities);

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
				switch(pos)
				{
				case 0:
					{
					}
					break;
				case 1:
					{
					}
					break;
				case 2:
					{
					}
					break;
				}
			}             
		});
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();

		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_netup1));
		map.put("info", getString(R.string.txt_netup2));
		map.put("img", R.drawable.upload);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_netdn1));
		map.put("info", getString(R.string.txt_netdn2));
		map.put("img", R.drawable.download);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_netup3));
		map.put("info", getString(R.string.txt_netup4));
		map.put("img", R.drawable.upload);
		list.add(map);
				
		mData=list;		
		return list;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.utilities, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:
			this.finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		case R.id.action_settings:
			//Intent intent = new Intent(this, NetworkActivity.class);
			//startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
	    	this.finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	    	return true;  
	    }
	    return super.onKeyDown(keyCode, event);  
	}
}
