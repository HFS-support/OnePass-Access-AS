package com.fgtit.utils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

public class DialogFactory {
	
	private static ProgressDialog progressDialog=null;
	
	public static Dialog createDialog(Context context,int layout,int theme) {
		Dialog dialog = new Dialog(context,theme);
		dialog.setContentView(layout);
		dialog.setCancelable(false);
		return  dialog;
	}
	
	public static ProgressDialog createWaitProgressDialog(Context context,int message){
		ProgressDialog progress = new ProgressDialog(context);
//		progress.setIcon(R.drawable.write_success);
		progress.setCancelable(false);
		progress.setMessage(context.getResources().getString(message));
		return progress;
	}
	
	public static void showProgressDialog(Context context,String message) {
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}

	public static void showProgressDialog(Context context,int resId) {
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(resId));
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public static void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}
}
