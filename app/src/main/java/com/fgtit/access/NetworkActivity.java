package com.fgtit.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.app.ActivityList;
import com.fgtit.app.DeviceConfig;
import com.fgtit.access.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Build;
import android.provider.Settings;

public class NetworkActivity extends Activity{

	private EditText editText1,editText2,editText3,editText4,editText5,editText6,editText7; 
	 public static final String ACTION_ETHERNET_SETTINGS =
	            "android.settings.ETHERNET_SETTINGS";	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		final Button btn1=(Button) findViewById(R.id.button1);
		btn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			}
		});
		
		final Button btn2=(Button) findViewById(R.id.button2);
		btn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
			Intent intent = new Intent(ACTION_ETHERNET_SETTINGS);
			startActivity(intent);
			}
		});
		
		final CheckBox cb = (CheckBox)this.findViewById(R.id.checkBox1);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1)
					DeviceConfig.getInstance().ldhcp=1;
				else
					DeviceConfig.getInstance().ldhcp=0;
			}
        });
		if(DeviceConfig.getInstance().ldhcp==1)
			cb.setChecked(true);
		else
			cb.setChecked(false);
		
		editText1=(EditText) findViewById(R.id.editText1);
		editText2=(EditText) findViewById(R.id.editText2);
		editText3=(EditText) findViewById(R.id.editText3);
		editText4=(EditText) findViewById(R.id.editText4);
		editText5=(EditText) findViewById(R.id.editText5);
		editText6=(EditText) findViewById(R.id.editText6);
		editText7=(EditText) findViewById(R.id.editText7);
		
		editText1.setText(DeviceConfig.longToIP(DeviceConfig.getInstance().lip));
		editText2.setText(DeviceConfig.longToIP(DeviceConfig.getInstance().lgate));
		editText3.setText(DeviceConfig.longToIP(DeviceConfig.getInstance().lsub));
		editText4.setText(String.valueOf(DeviceConfig.getInstance().lport));
		editText5.setText(DeviceConfig.longToIP(DeviceConfig.getInstance().rip));
		editText6.setText(String.valueOf(DeviceConfig.getInstance().rport));
		editText7.setText(ActivityList.getInstance().WebAddr);
		
		ActivityList.getInstance().addActivity(this);
	}

	private void SaveNetworkSetting(){
		if(DeviceConfig.checkIP(editText1.getText().toString())){
			DeviceConfig.getInstance().lip=DeviceConfig.ipToLong(editText1.getText().toString());
		}
		if(DeviceConfig.checkIP(editText2.getText().toString())){
			DeviceConfig.getInstance().lgate=DeviceConfig.ipToLong(editText2.getText().toString());
		}
		if(DeviceConfig.checkIP(editText3.getText().toString())){
			DeviceConfig.getInstance().lsub=DeviceConfig.ipToLong(editText3.getText().toString());
		}
		if(DeviceConfig.checkNumber(editText4.getText().toString())){
			DeviceConfig.getInstance().lport=Integer.valueOf(editText4.getText().toString());
		}
		if(DeviceConfig.checkIP(editText5.getText().toString())){
			DeviceConfig.getInstance().rip=DeviceConfig.ipToLong(editText5.getText().toString());
		}
		if(DeviceConfig.checkNumber(editText6.getText().toString())){
			DeviceConfig.getInstance().rport=Integer.valueOf(editText6.getText().toString());
		}
	}
	
	private void SaveAndExit(){
		SaveNetworkSetting();
		DeviceConfig.getInstance().SaveConfig();
		this.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.system, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:
			SaveAndExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
	    	SaveAndExit();	    	
	    	return true;  
	    }
	    return super.onKeyDown(keyCode, event);  
	}
}
