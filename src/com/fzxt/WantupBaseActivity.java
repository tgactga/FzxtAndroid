package com.fzxt;

import java.net.ConnectException;


import android.app.Activity;
import android.os.AsyncTask;
import com.fzxt.R;

public class WantupBaseActivity extends Activity{

	/*
	 * ��������
	 */
	public interface NetTask{
		public Object execute() throws Exception;
	}
	
	/*
	 * UI���񣬽����쳣��Ϣ����������
	 */
	public interface UiTask{
		public void execute(Exception e,Object result);
	}
	
	public void postTask(NetTask netTask,UiTask uiTask){
		WantupAsyns task = new WantupAsyns(netTask,uiTask);
		task.execute();
	}
	
	public void postTask(NetTask netTask,UiTask uiTask,Boolean isLoadMask){
		WantupAsyns task = new WantupAsyns(netTask,uiTask,isLoadMask);
		task.execute();
	}
	
	public class WantupAsyns extends AsyncTask{
		private UiTask uiTask;
		private NetTask netTask;
		private Boolean isLoadMask = false;
		private Exception exception;
		private MyDialog proDia;	//��������
		
		public WantupAsyns(NetTask netTask, UiTask uiTask) {
			this.netTask = netTask;
			this.uiTask = uiTask;
		}
		
		public WantupAsyns(NetTask netTask, UiTask uiTask,Boolean isLoadMask) {
			this.netTask = netTask;
			this.uiTask = uiTask;
			this.isLoadMask = isLoadMask;
		}

		@Override
		protected Object doInBackground(Object... params) {
			try{
				return this.netTask.execute();
			}catch(Exception e){
				if(proDia != null){
					proDia.dismiss();
				}
				this.exception = e;
			}
			return null;
		}
		
		@Override
		protected void onCancelled() {
			if(proDia != null){
				proDia.dismiss();
			}
			super.onCancelled();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			
			if(exception != null && exception instanceof ConnectException){
				exception = new NoNetWorkException("���ӷ�����ʧ��");
			}else if(exception != null ){
				
			}
			
			this.uiTask.execute(exception, result);
			if(proDia != null){
				proDia.dismiss();
			}
			super.onPostExecute(result);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(isLoadMask){
				//proDia = ProgressDialog.show(WantupBaseActivity.this, "��ʾ", "���ڼ��أ����Ժ�...");
				proDia = new MyDialog(WantupBaseActivity.this, R.style.MyDialogStyle, R.layout.loading, false).create();
				proDia.setCanceledOnTouchOutside(false);
				proDia.setCancelable(false);
				proDia.show();
			}
		}
	}
	
}
