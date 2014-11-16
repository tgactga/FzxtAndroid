package com.fzxt.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

@SuppressLint("NewApi")
public class NetWorkManager {
	private ConnectivityManager connectivityManager;
	
	public boolean isOpenWireless(Context context){
		if(connectivityManager == null){
			Context contexts = context.getApplicationContext();
			connectivityManager = (ConnectivityManager) contexts.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		if(connectivityManager != null){
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();
			if(info != null){
				State state = info.getState();
				if(state == NetworkInfo.State.CONNECTED){
					return true;
				}
			}
		}
		return false;
	}
	
	public void openWirelessSettings(final Activity context){
		connectivityManager.getActiveNetworkInfo();
		Builder b = new AlertDialog.Builder(context, 0).setTitle("没有可用网络").setMessage("请检查网络连接状态,并重新启动程序");
		b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				System.exit(0);
				
			}
		});
		
	}
}
