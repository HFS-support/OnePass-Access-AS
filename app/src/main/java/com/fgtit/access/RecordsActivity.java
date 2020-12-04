package com.fgtit.access;

import com.fgtit.app.ActivityList;
import com.fgtit.app.LogsList;
import com.fgtit.access.R;
import com.fgtit.utils.ToastUtil;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.os.Build;

public class RecordsActivity extends Activity {

	private int querytype=0;
	private TextView txCount;
	private EditText qyText;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_records);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		txCount=(TextView)findViewById(R.id.textView2);
		qyText=(EditText)findViewById(R.id.editText1);
		
		final Spinner spin1=(Spinner)findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource( this, R.array.quy_array, android.R.layout.simple_spinner_item); 
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin1.setAdapter(adapter1);
		spin1.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				querytype=pos;
			}

			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
				    //nothing to do 
			} 
		});
		spin1.setSelection(querytype);
				
		//addWegit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.records, menu);
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
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		case R.id.action_query:
			addWegit();
			return true;
		case R.id.action_clear:
			LogsList.getInstance().Clear();
			clearWegit();
			break;
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

	public void clearWegit() {
		TableLayout table = (TableLayout) findViewById(R.id.tablelayout);  
        table.setStretchAllColumns(true);          
        table.removeAllViews();
	}
	
	public void addWegit() {  
        TableLayout table = (TableLayout) findViewById(R.id.tablelayout);  
        table.setStretchAllColumns(true);  
        
        table.removeAllViews();
        
        LogsList.getInstance().Query(0, "","");	//Load All
        
        TableRow tablecap = new TableRow(RecordsActivity.this);  
        tablecap.setBackgroundColor(Color.rgb(180, 180, 180));  
		for (int j = 0; j < 3; j++) {  
			TextView tv = new TextView(RecordsActivity.this);
			tv.setBackgroundColor(Color.rgb(255, 255,255));
			switch(j){
			case 0:	
				tv.setText(R.string.txt_datetime);
				break;
			case 1:
				tv.setText(R.string.txt_userid);
				break;
			case 2:
				tv.setText(R.string.txt_username);
				break;
			}
			TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			lp.setMargins(1, 1, 1, 1);
			tablecap.addView(tv,lp);  
       	}  
		table.addView(tablecap, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
        
		int count=0;		
        if(LogsList.getInstance().logsList!=null){
        	for (int i = 0; i <LogsList.getInstance().logsList.size(); i++) {
        		if(querytype==1){
        			String id=String.valueOf(LogsList.getInstance().logsList.get(i).userid);
        			if(id.indexOf(qyText.getText().toString())>=0){
        				count++;
        			}else{
        				continue;
        			}
        		}else if(querytype==2){
        			if(LogsList.getInstance().logsList.get(i).username.indexOf(qyText.getText().toString())>=0){
        				count++;
        			}else{
        				continue;
        			}
        		}else{
        			count++;
        		}
        		
        		TableRow tablerow = new TableRow(RecordsActivity.this);  
        		tablerow.setBackgroundColor(Color.rgb(222, 220, 210));  
        		for (int j = 0; j < 3; j++) {  
        			TextView tv = new TextView(RecordsActivity.this);
        			tv.setBackgroundColor(Color.rgb(255, 255,255));
        			switch(j){
        			case 0:	
        				tv.setText(LogsList.getInstance().logsList.get(i).datetime);
        				break;
        			case 1:
        				tv.setText(String.valueOf(LogsList.getInstance().logsList.get(i).userid));
        				break;
        			case 2:
        				tv.setText(LogsList.getInstance().logsList.get(i).username);
        				break;
        			}
        			TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        			lp.setMargins(1, 1, 1, 1);
        			tablerow.addView(tv,lp);  
               	}  
        		table.addView(tablerow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
        	}  
        }
        
        txCount.setText("Records:"+String.valueOf(count));
    } 
}
