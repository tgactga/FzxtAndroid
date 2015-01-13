package com.fzxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fzxt.common.MarqueeText;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity4 extends WantupBaseActivity {
	private ListView listView1;
	private DoctorQueueListAdapter adapter1;
	private List<Doctor> list1 = new ArrayList<Doctor>();
	private int[] colors = new int[]{R.drawable.line_background, R.drawable.line_background2};
	private int[] textColors = new int[]{0xff000000,0xffffffff};
	public  String mStrMSG = "";
	private String split = "&";
	private String localIp = "0.0.0.0";
	private TextView clinicNameView;
	private TextView currentPnameView;
	private TextView prePnameView;
	private Socket mSocket;
	private String waitIp;
	private String clinicId;
	
	private VideoView videoView; 
    private int play_progress; 
	
    private String progress_Title; 
    private Dialog progress; 
    private String[] files;
    private int index= 0;
    private String videoPath;
    private MediaController controller;
    private HttpUtil httpUtil = new HttpUtil();
    private MarqueeText marqueeText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main4);
		listView1 = (ListView) findViewById(R.id.doctorquereList1);
		videoView = (VideoView) findViewById(R.id.videoView);
		
		localIp = this.getIntent().getStringExtra("localIp");
		waitIp = this.getIntent().getStringExtra("waitIp");
		clinicId = this.getIntent().getStringExtra("clinicId");
		clinicNameView = (TextView) findViewById(R.id.clinic_name);
		marqueeText = (MarqueeText) findViewById(R.id.marqueeText);
		currentPnameView = (TextView) findViewById(R.id.current_pname);
		prePnameView = (TextView) findViewById(R.id.pre_pname);
		postTask(new NetTask() {

			@Override
			public Object execute() throws Exception {
				Map<String,String> paramsMap = new HashMap<String,String>();
				paramsMap.put("clinicId", clinicId);
				String result2 = HttpUtil.postRequest(httpUtil.BASE_URL+"/fzxtAction!getClinicNameByClinicId.do", paramsMap);
				List<Map> listMap2 = CkxTrans.getList(result2);
				for(Map map :listMap2){
					clinicNameView.setText(map.get("clinicname")+"");
				}
				
				return null;
			}
		}, new UiTask() {

			@Override
			public void execute(Exception e, Object result) {
				if(e != null){
					Toast.makeText(MainActivity4.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}else{
					int size=0;
					while(size<6){
						Doctor doctor = new Doctor();
						list1.add(doctor);
						size++;
					}
					adapter1 = new DoctorQueueListAdapter();
					listView1.setAdapter(adapter1);
					
				}
			}
		}, true);
		
		Thread receiveThread = new Thread(receiveRunnable);
		receiveThread.start();
		
		Thread heartbeatThread = new Thread(heartbeatRunnable);
		heartbeatThread.start();
		
		Intent intent = getIntent(); 
        videoPath = intent.getStringExtra("videoUrl"); 
        progress_Title = intent.getStringExtra("title"); 
 
        File filePath = new File(videoPath);
        
        files = filePath.list();
 
        if (this.progress_Title == null){
            this.progress_Title = "Loading";
        }
        play_progress = intent.getIntExtra("play_progress", 0); 
        videoView = (VideoView) findViewById(R.id.videoView); 
        progress = ProgressDialog.show(this, "loading", progress_Title); 
        progress.setOnKeyListener(new OnKeyListener() { 
            @Override 
            public boolean onKey(DialogInterface dialog, int keyCode, 
                    KeyEvent event) { 
                if (keyCode == KeyEvent.KEYCODE_BACK) { 
                    dialog.dismiss(); 
                    MainActivity4.this.finish(); 
                } 
                return false; 
            } 
        });
        
        controller = new MediaController(this){
        	@Override
        	public void setMediaPlayer(MediaPlayerControl player) {
        		super.setMediaPlayer(player);
        		this.hide();
        	}
        	@Override
        	public void show(int timeout) {
        		this.hide();
        	}
        };
        
        videoView.setVideoURI(Uri.parse(videoPath+"/"+files[index])); 
        videoView.setMediaController(controller); 
        videoView.requestFocus();
 
        
         
        videoView.setOnPreparedListener(new OnPreparedListener() { 
            @Override 
            public void onPrepared(MediaPlayer mp) { 
                if (progress != null) 
                    progress.dismiss(); 
            } 
        }); 
 
        videoView.setOnCompletionListener(new OnCompletionListener() { 
            @Override 
            public void onCompletion(MediaPlayer mp) {
            	//播放完成后播放下一个，如果没有下一个，播放第一个
            	
            	if(index == (files.length-1)){
            		index = 0;
            	}else{
            		index ++;
            	}
            	
            	videoView.setVideoURI(Uri.parse(videoPath+"/"+files[index])); 
            	videoView.setMediaController(controller);
            	videoView.requestFocus();
            	videoView.start(); 
                if (progress != null){
                    progress.dismiss();
                }
            } 
        }); 
		
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {  
            @Override  
            public boolean onError(MediaPlayer mp, int what, int extra) {  
            	index = 0;
                return false;  
            }  
        });
        
        marqueeText.startFor0();
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

		public DoctorQueueListAdapter() {
			super(MainActivity4.this, R.layout.doctor_queue_sub4,list1);
		}

		@Override
		public View getRowView(int position, View convertView, ViewGroup parent) {
			currentPnameView = (TextView) convertView.findViewById(R.id.current_pname);
			currentPnameView.setText(list1.get(position).getDoctorName());
			
			prePnameView = (TextView) convertView.findViewById(R.id.pre_pname);
			prePnameView.setText(list1.get(position).getRoomName());
			
//			current_numView = (TextView) convertView.findViewById(R.id.current_num);
//			current_numView.setText(list.get(position).getCurrentNum());
			
			int colorPos = position%colors.length;  
//			convertView.setBackgroundColor(); 
			convertView.setBackgroundResource(colors[colorPos]);
			
			currentPnameView.setTextColor(textColors[colorPos]);
			prePnameView.setTextColor(textColors[colorPos]);
//			current_numView.setTextColor(textColors[colorPos]);
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
								String tmpMsg = "";
								if ( (tmpMsg = MessageSocket.mBufferedReader.readLine()) != null && !tmpMsg.isEmpty() ) {
										//消息换行
										mStrMSG = new String(tmpMsg);
										mHandler.sendMessage(mHandler.obtainMessage());
								}
							}
						}else{
							mSocket = new Socket(waitIp, httpUtil.socketServerPort);
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
						if(msgArr.length > 1){
							
							/*String currentNum = msgArr[3];
							for(Doctor doctor : list1){
								String roomname = doctor.getRoomName();
								if(msgArr[2].trim().equals(roomname)){
									doctor.setCurrentNum(currentNum+"号"+msgArr[1]);
									adapter1.notifyDataSetChanged();
								}
							}*/
							String currentPname = msgArr[0]+"号"+msgArr[1]+"到"+msgArr[2];
							String prePname = "";
							if(msgArr[5].isEmpty() && msgArr[7].isEmpty()){
								
							}else if(msgArr[5].isEmpty() | msgArr[7].isEmpty()){
								prePname = msgArr[5]+msgArr[7]+"准备";
							}else{
								prePname = msgArr[5]+","+msgArr[7]+"准备";
							}
							Doctor doctor = new Doctor();
							doctor.setDoctorName(currentPname);
							doctor.setRoomName(prePname);
							
							list1.add(0,doctor);
							if(list1.size()>6){
								list1.remove(list1.size()-1);
							}
							adapter1.notifyDataSetChanged();
							//宣教模式下叫号不显示大屏幕
							
							/*if (videoView != null) { 
					            videoView.pause(); 
					            play_progress = videoView.getCurrentPosition(); 
					        } 
					        if (progress != null) { 
					            progress.dismiss(); 
					        } 
						        
							Intent intent = new Intent();
							intent.setClass(MainActivity3.this, ShowNameActivity.class);
							intent.putExtra("userinfo", messageTmp);
							startActivity(intent);
							*/
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
						Socket mSocket1 = new Socket(waitIp, httpUtil.socketServerPort);
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
								mSocket = new Socket(waitIp, httpUtil.socketServerPort);
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
		
		
        
		@Override 
	    protected void onResume() { 
	        super.onResume(); 
	        videoView.seekTo(play_progress); 
	        videoView.start(); 
	    }
		
		 @Override 
	    protected void onStop() {
	        super.onStop();
	        if (videoView != null) {
	            videoView.pause();
	            play_progress = videoView.getCurrentPosition();
	        } 
	        if (progress != null) {
	            progress.dismiss();
	        }
	    }
}
