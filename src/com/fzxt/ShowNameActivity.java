package com.fzxt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class ShowNameActivity extends WantupBaseActivity {
	static Activity instance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_name);
		instance = this;
		String userinfo = this.getIntent().getStringExtra("userinfo");
		
		TextView patientNumView = (TextView) findViewById(R.id.patient_num);
		TextView patientNameView = (TextView) findViewById(R.id.patient_name);
		TextView roomNameView = (TextView) findViewById(R.id.room_name);
		String[] userInfoArr = {};
		if(userinfo != null && userinfo.length() >0){
			userInfoArr = userinfo.split("&");
			if(userInfoArr.length >= 3){
				patientNumView.setText(userInfoArr[0]+"�Ż���");
				patientNameView.setText(userInfoArr[1]);
				roomNameView.setText(userInfoArr[2]);
			}else{
				patientNumView.setText(" �Ż���");
				patientNameView.setText(" ");
				roomNameView.setText(" ");
			}
		}else{
			patientNumView.setText(" �Ż���");
			patientNameView.setText(" ");
			roomNameView.setText(" ");
		}
		
		/*Thread timeoutThread = new Thread(timeoutRunnable);
		timeoutThread.start();*/
		
		
	}
	
	private Runnable timeoutRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			closeSelf();
		}
		
	};

	public void closeSelf(){
		this.finish();
	}
}
