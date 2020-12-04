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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Build;
import android.provider.Settings;

public class SystemActivity extends Activity{

	private EditText editText1,editText2;
	private Spinner spin1,spin2,spin3,spin4;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		editText1=(EditText)findViewById(R.id.editText1);
		editText2=(EditText)findViewById(R.id.editText2);
		
		String sn=Integer.toHexString(DeviceConfig.getInstance().devsn[0]&0xFF).toUpperCase()+
				Integer.toHexString(DeviceConfig.getInstance().devsn[1]&0xFF).toUpperCase()+
				Integer.toHexString(DeviceConfig.getInstance().devsn[2]&0xFF).toUpperCase()+
				Integer.toHexString(DeviceConfig.getInstance().devsn[3]&0xFF).toUpperCase();
		
		editText1.setText(sn);
		editText2.setText(DeviceConfig.getInstance().devname);
		
		spin1=(Spinner)findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource( this, R.array.array_devicemode, android.R.layout.simple_spinner_item); 
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin1.setAdapter(adapter1);
		spin1.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				DeviceConfig.getInstance().devicetype=(byte) pos;
			}

			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
			    //nothing to do 
			} 
		});
		spin1.setSelection(DeviceConfig.getInstance().devicetype);
						
		spin2=(Spinner)findViewById(R.id.spinner2); 
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this, R.array.array_matchmode, android.R.layout.simple_spinner_item); 
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin2.setAdapter(adapter2);
		spin2.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
		    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){
				DeviceConfig.getInstance().remotemode=(byte) pos;
		    }
			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
		    } 
		});
		spin2.setSelection(DeviceConfig.getInstance().remotemode);
		
		spin3=(Spinner)findViewById(R.id.spinner3); 
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource( this, R.array.array_lockdelay, android.R.layout.simple_spinner_item); 
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin3.setAdapter(adapter3);
		spin3.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
		    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				switch(pos){
				case 0:	DeviceConfig.getInstance().doordelay=3;	break;
				case 1:	DeviceConfig.getInstance().doordelay=6;	break;
				case 2:	DeviceConfig.getInstance().doordelay=9;	break;
				case 3:	DeviceConfig.getInstance().doordelay=12;	break;
				}
		    }
			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
		    } 
		});
		switch(DeviceConfig.getInstance().doordelay){
		case 3:	spin3.setSelection(0);	break;
		case 6:	spin3.setSelection(1);	break;
		case 9:	spin3.setSelection(2);	break;
		case 12:spin3.setSelection(3);	break;
		default:spin3.setSelection(1);	break;			
		}		
		
		spin4=(Spinner)findViewById(R.id.spinner4); 
		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource( this, R.array.array_alarmdelay, android.R.layout.simple_spinner_item); 
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin4.setAdapter(adapter4);
		spin4.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
		    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				switch(pos){
				case 0:	DeviceConfig.getInstance().alarmdelay=5;	break;
				case 1:	DeviceConfig.getInstance().alarmdelay=10;	break;
				case 2:	DeviceConfig.getInstance().alarmdelay=20;	break;
				case 3:	DeviceConfig.getInstance().alarmdelay=30;	break;
				}
		    }
			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
		    } 
		});
		switch(DeviceConfig.getInstance().alarmdelay){
		case 5:	spin4.setSelection(0);	break;
		case 10:spin4.setSelection(1);	break;
		case 20:spin4.setSelection(2);	break;
		case 30:spin4.setSelection(3);	break;
		default:spin4.setSelection(1);	break;			
		}
		
		
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
			DeviceConfig.getInstance().SaveConfig();
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
	    	DeviceConfig.getInstance().SaveConfig();
	    	this.finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	    	return true;  
	    }
	    return super.onKeyDown(keyCode, event);  
	}
}
