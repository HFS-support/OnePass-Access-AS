package com.fgtit.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.app.ActivityList;
import com.fgtit.access.R;
import com.fgtit.utils.ToastUtil;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.os.Build;
import android.provider.Settings;

public class LoginActivity extends Activity{

	private EditText editText1,editText2;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logon);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
        
		Button btn01=(Button)findViewById(R.id.button1);
		btn01.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				if(editText1.getText().toString().equals("admin")){
					/*
					if(editText2.getText().toString().equals(ActivityList.getInstance().PassWord)){
						ToastUtil.showToastTop(LoginActivity.this,"Logon ...");
						Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
						startActivityForResult(intent,0);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}else{
						ToastUtil.showToastTop(LoginActivity.this, "Wrong password£¡");
					}
					*/
					ToastUtil.showToastTop(LoginActivity.this,"Login ...");
					Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
					startActivityForResult(intent,0);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					
				}else{
					ToastUtil.showToastTop(LoginActivity.this, "Administrator error");
				}
			}
		});
		
		editText1=(EditText)findViewById(R.id.editText1);
		editText2=(EditText)findViewById(R.id.editText2);
		editText1.setText("admin");
		
		/*
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
			View v = this.getWindow().getDecorView();
	        v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
		*/
		
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
			}
		});
		
		
		NotificationManager nm =(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
        
        ActivityList.getInstance().addActivity(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case R.id.action_return:
		case android.R.id.home:
			this.finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
	
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (resultCode){
		case 0:{
				this.setResult(0);
				this.finish();
			}
			break;
		case 1:{
				this.setResult(1);
				this.finish();
			}
			break;
		case 2:
			break;
		default:
			break;
		}
	}
    
}
