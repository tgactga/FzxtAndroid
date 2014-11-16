package com.fzxt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends WantupBaseActivity {
	private ListView listView1;
	private ListView listView2;
	private DoctorQueueListAdapter adapter1;
	private DoctorQueueListAdapter adapter2;
	private List<Doctor> list1 = new ArrayList<Doctor>();
	private List<Doctor> list2 = new ArrayList<Doctor>();
	private int[] colors = new int[]{R.drawable.line_background, R.drawable.line_background2};
	private int[] textColors = new int[]{0xff000000,0xffffffff};
	public  String mStrMSG = "";
	private String split = "&";
	private String localIp = "0.0.0.0";
	private TextView current_numView;
	private Socket mSocket;
	private String waitIp;
	private String clinicId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView1 = (ListView) findViewById(R.id.doctorquereList1);
		listView2 = (ListView) findViewById(R.id.doctorquereList2);
		
		localIp = this.getIntent().getStringExtra("localIp");
		waitIp = this.getIntent().getStringExtra("waitIp");
		clinicId = this.getIntent().getStringExtra("clinicId");
		postTask(new NetTask() {

			@Override
			public Object execute() throws Exception {
				
				Map<String,String> paramsMap = new HashMap<String,String>();
				paramsMap.put("ip", localIp);
				paramsMap.put("clinicId", clinicId);
				paramsMap.put("start", "0");
				paramsMap.put("end", "10");
				String result = HttpUtil.postRequest(HttpUtil.BASE_URL+"/fzxtAction!getDoctorQueueForAndroid.do", paramsMap);
				
				if(result != null && result.length()>0){
					List<Map> listMap = CkxTrans.getList(result);
					for(Map map :listMap){
						Doctor doctor = new Doctor();
						String doctorName = map.get("doctorName")+"";
						String roomName = map.get("roomName")+"";
						String currentNum = map.get("currentNum")+"";
						doctor.setDoctorName(doctorName);
						doctor.setRoomName(roomName);
						doctor.setCurrentNum(currentNum);
						list1.add(doctor);
					}
				}
				
				Map<String,String> paramsMap2 = new HashMap<String,String>();
				paramsMap2.put("ip", localIp);
				paramsMap2.put("start", "10");
				paramsMap2.put("end", "20");
				String result2 = HttpUtil.postRequest(HttpUtil.BASE_URL+"/fzxtAction!getDoctorQueue.do", paramsMap2);
				
				if(result2 != null && result2.length()>0){
					List<Map> listMap2 = CkxTrans.getList(result2);
					for(Map map :listMap2){
						Doctor doctor = new Doctor();
						String doctorName = map.get("doctorName")+"";
						String roomName = map.get("roomName")+"";
						String currentNum = map.get("currentNum")+"";
						doctor.setDoctorName(doctorName);
						doctor.setRoomName(roomName);
						doctor.setCurrentNum(currentNum);
						list2.add(doctor);
					}
				}
				
				return null;
			}
		}, new UiTask() {

			@Override
			public void execute(Exception e, Object result) {
				if(e != null){
					Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}else{
					adapter1 = new DoctorQueueListAdapter(list1);
					listView1.setAdapter(adapter1);
					
					adapter2 = new DoctorQueueListAdapter(list2);
					listView2.setAdapter(adapter2);
				}
			}
		}, true);
		
		Thread receiveThread = new Thread(receiveRunnable);
		receiveThread.start();
		
		Thread heartbeatThread = new Thread(heartbeatRunnable);
		heartbeatThread.start();
	}
	
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event){
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			exitSystem();
			return true;
		}
		return super.dispatchKeyEvent(event);
    }
	private void exitSystem(){
		MessageSocket.sendMessageToServer("exit");
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
	private String intToIp(int i) {       
        return (i & 0xFF ) + "." +       
	      ((i >> 8 ) & 0xFF) + "." +       
	      ((i >> 16 ) & 0xFF) + "." +       
	      ( i >> 24 & 0xFF) ;  
   }   
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class DoctorQueueListAdapter extends WantupAdapter<Doctor>{

		private List<Doctor> list;
		public DoctorQueueListAdapter(List<Doctor> tList) {
			super(MainActivity.this, R.layout.doctor_queue_sub, tList);
			this.list = tList;
		}

		@Override
		public View getRowView(int position, View convertView, ViewGroup parent) {
			TextView doctor_nameView = (TextView) convertView.findViewById(R.id.doctor_name);
			doctor_nameView.setText(list.get(position).getDoctorName());
			
			TextView roomnameView = (TextView) convertView.findViewById(R.id.roomname);
			roomnameView.setText(list.get(position).getRoomName());
			
			current_numView = (TextView) convertView.findViewById(R.id.current_num);
			current_numView.setText(list.get(position).getCurrentNum());
			
			int colorPos = position%colors.length;  
//			convertView.setBackgroundColor(); 
			convertView.setBackgroundResource(colors[colorPos]);
			
			doctor_nameView.setTextColor(textColors[colorPos]);
			roomnameView.setTextColor(textColors[colorPos]);
			current_numView.setTextColor(textColors[colorPos]);
			return convertView;
		}
		
		
	}
	
	
	//线程:监听服务器发来的消息
		private Runnable receiveRunnable = new Runnable() {
			@Override
			public void run(){
				while (true){
					try {
						if(MessageSocket.getmSocket() != null){ // 如果socket信息丢失，在这里再次向服务器发起请求
							if(MessageSocket.mBufferedReader != null){
								MessageSocket.mBufferedReader.mark(8192);
								if ( (mStrMSG = MessageSocket.mBufferedReader.readLine()) != null ) {
										//消息换行
										mStrMSG += "\n";
										mHandler.sendMessage(mHandler.obtainMessage());
								}
							}
						}else{
							mSocket = new Socket(waitIp, HttpUtil.socketServerPort);
			    			//取得输入、输出流
			    			BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(),"UTF-8"));
			    			PrintWriter mPrintWriter = new PrintWriter(mSocket.getOutputStream(), true);
			    			MessageSocket.setmSocket(mSocket);
			    			MessageSocket.setmBufferedReader(mBufferedReader);
			    			MessageSocket.setmPrintWriter(mPrintWriter);
							
			    			MessageSocket.sendMessageToServer("login ");
						}
						Thread.sleep(3000);
					}catch (Exception e){
						
					}
				}
			}
		};
			
		public Handler mHandler	= new Handler(){
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				// 刷新
				try{
					if(mStrMSG != null && !mStrMSG.isEmpty() && mStrMSG.length()>1){
						System.out.println("收到来自服务器信息："+mStrMSG);
						String messageTmp = mStrMSG;
						String[] msgArr = messageTmp.split(split);
						if(msgArr.length > 3){
							
							String currentNum = msgArr[3];
							Doctor doc = list1.get(2);
							doc.setCurrentNum(currentNum);
							adapter1.notifyDataSetChanged();
							
							Intent intent = new Intent();
							intent.setClass(MainActivity.this, ShowNameActivity.class);
							intent.putExtra("userinfo", messageTmp);
							startActivity(intent);
						}else if(messageTmp.contains("close")){
							ShowNameActivity.instance.finish();
						}
					}
					
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		};
			
			
		//向服务器发送心跳包
		private Runnable heartbeatRunnable = new Runnable() {
			@Override
			public void run(){
				Boolean reconnect = false;
				while (true){
					
					try {
						BufferedReader mBufferedReader = null;
						PrintWriter mPrintWriter;
		        		//连接服务器
						Socket mSocket1 = new Socket(waitIp, HttpUtil.socketServerPort);
						if(mSocket1 != null){
			    			//取得输入、输出流
			    			mBufferedReader = new BufferedReader(new InputStreamReader(mSocket1.getInputStream(),"UTF-8"));
			    			mPrintWriter = new PrintWriter(mSocket1.getOutputStream(), true);
			    			if(mPrintWriter != null){
			    				mPrintWriter.print("heartbeat \n");
			    		    	mPrintWriter.flush();
			    		    	
			    			}
			    			if(reconnect){
								String heartBeat;
								/*if(mBufferedReader != null){
									mBufferedReader.mark(8192);
									if ( (heartBeat = mBufferedReader.readLine()) != null ) {
										System.out.println("自动重连成功...");
						    			MessageSocket.setmSocket(mSocket1);
						    			MessageSocket.setmBufferedReader(mBufferedReader);
						    			MessageSocket.setmPrintWriter(mPrintWriter);
									}
								}*/
								mSocket = new Socket(waitIp, HttpUtil.socketServerPort);
				    			//取得输入、输出流
				    			BufferedReader mBufferedReader1 = new BufferedReader(new InputStreamReader(mSocket.getInputStream(),"UTF-8"));
				    			PrintWriter mPrintWriter1 = new PrintWriter(mSocket.getOutputStream(), true);
				    			MessageSocket.setmSocket(mSocket);
				    			MessageSocket.setmBufferedReader(mBufferedReader1);
				    			MessageSocket.setmPrintWriter(mPrintWriter1);
								
				    			MessageSocket.sendMessageToServer("login ");
			    			}
			    			mSocket1.close();
						}
						
						Thread.sleep(30000);
						reconnect = false;
					}catch (Exception e){
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							
						}
						reconnect = true;
					}
				}
			}
		};
		
}
