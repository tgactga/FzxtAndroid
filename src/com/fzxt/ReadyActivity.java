package com.fzxt;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.fzxt.common.NetWorkManager;
import com.fzxt.ftp.FTP;
import com.fzxt.ftp.Result;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ReadyActivity extends WantupBaseActivity {
	private String ip = "0.0.0.0";
	private TextView current_numView;
	private volatile ComputerInfo info = new ComputerInfo();
	private FTP ftp;
	private String TAG = "ReadyActivity";
	private static final int UPDATE_CLIENT =100;
	private static final int GET_UNDATAINFO_ERROR =101;
	private static final int DOWN_ERROR = 41;
	private String downloadPath = null;
	private String serverVersion = null;
	private String description = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ready);
		
		//ip = getLocalIpAddress();
        ip = getIPAddress(true,this);
        if(testConnect()){
        	postTask(new NetTask() {
    			@Override
    			public Object execute() throws Exception {
    				
    				try {
    		        	ftp = new FTP(HttpUtil.ftpHostName, HttpUtil.userName, HttpUtil.password);
    					ftp.openConnect();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    				
    				Map<String,String> paramsMap = new HashMap<String,String>();
    				paramsMap.put("ip", ip);
    				paramsMap.put("start", "0");
    				paramsMap.put("end", "10");
    				String result = HttpUtil.postRequest(HttpUtil.BASE_URL+"/fzxtAction!findComputerInfoByComputerIp.do", paramsMap);
    				
    				if(result != null && result.length()>0){
    					List<Map> listMap = CkxTrans.getList(result);
    					for(Map map :listMap){
    						info.setWait_ip(map.get("wait_ip")+"");
    						info.setView_model(map.get("view_model")+"");
    						info.setClinicid(map.get("clinicid")+"");
    					}
    				}
    				CheckVersionTask checkVersionTask = new CheckVersionTask();
    				Thread checkThread = new Thread(checkVersionTask);
    				checkThread.start();
    				return null;
    			}
    		}, new UiTask() {

    			@Override
    			public void execute(Exception e, Object result) {
    				if(info.getWait_ip() != null){
    					Toast.makeText(ReadyActivity.this, "111", 0).show();
    				}else{
    					AlertDialog.Builder builer = new Builder(ReadyActivity.this); 
    			        builer.setTitle("请与管理员联系");  
    			        builer.setMessage("未找到IP配置信息");  
    			        AlertDialog dialog = builer.create();  
    			        dialog.show();  
    				}
    			}
    		}, true);
        	
        }else{
        	new NetWorkManager().openWirelessSettings(this);
        }
        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }
	
	
	
	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}

	protected String execRootCmd(String paramString) {
		DataInputStream dis = null;
		Runtime r = Runtime.getRuntime();
		try {
			// r.exec("su"); // get root
			StringBuilder sb = new StringBuilder();
			Process p = r.exec(paramString);
			InputStream input = p.getInputStream();
			dis = new DataInputStream(input);
			String content = null;
			while ((content = dis.readLine()) != null) {
				sb.append(content).append("\n");
			}
			// r.exec("exit"); Log.i("UERY", "sb = " + sb.toString());
			// localVector.add(sb.toString());
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Get IP address from first non-localhost interface
	 * 
	 * @param ipv4
	 *            true=return ipv4, false=return ipv6
	 * @return address or empty string
	 */
	public String getIPAddress(boolean useIPv4, Context context) {
		WifiManager wifimanage = (WifiManager) context
				.getSystemService(context.WIFI_SERVICE);// 获取WifiManager
		// 检查wifi是否开启
		if (wifimanage.isWifiEnabled()) {
			WifiInfo wifiinfo = wifimanage.getConnectionInfo();
			String wifiip = intToIp(wifiinfo.getIpAddress());
			Log.i("MyTag", "-------wifiip----" + wifiip);
			return wifiip;
		} else {
			String comstr = "ifconfig eth0";
			String ip = execRootCmd(comstr);
			Log.i(TAG, "---process ifconfig eth0-----" + ip);
			final String myip = ip.substring(ip.indexOf("ip") + 2,
					ip.indexOf("mask")).trim();
			return myip;
		}

	}
	
	public boolean testConnect(){
		
		if(!new NetWorkManager().isOpenWireless(this)){
			Toast.makeText(this, "温馨提示：当前无可用网络", Toast.LENGTH_SHORT);
			
			return false;
		}else{
			Log.i("", "网络已经连接上");
			return true;
		}
	}
	
	public class CheckVersionTask implements Runnable{

		@Override
		public void run() {
			String serverPath = HttpUtil.updateServerUrl;
			
			try {
				URL url = new URL(serverPath);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(20000);
				InputStream is = conn.getInputStream();
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(is,"UTF-8");
				int type = parser.getEventType();
				
				while(type != XmlPullParser.END_DOCUMENT){
					switch(type){
					case XmlPullParser.START_TAG:
						
						if("version".equals(parser.getName())){
							serverVersion = parser.nextText();
						}else if ("url".equals(parser.getName())){
			                downloadPath = parser.nextText();
			            }else if ("description".equals(parser.getName())){
			                description = parser.nextText();
			            }
			            break;
					}
					type = parser.next();
				}
				
				PackageManager packageManager = getPackageManager();
				PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
				
				String versionName = packInfo.versionName;
				if(versionName.endsWith(serverVersion)){
					Log.d(TAG, "版本相同，无需升级");
					startActivityForMain();
				}else{
					Log.d(TAG, "版本号不同，需要升级");
					Message message = new Message();
					message.what = UPDATE_CLIENT;
					handlerUpdate.sendMessage(message);
					
				}
			} catch (MalformedURLException e) {
				Message message = new Message();
				message.what = UPDATE_CLIENT;
				handlerUpdate.sendMessage(message);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				Message message = new Message();
				message.what = DOWN_ERROR;
				handlerUpdate.sendMessage(message);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				Message message = new Message();
				message.what = GET_UNDATAINFO_ERROR;
				handlerUpdate.sendMessage(message);
			} catch (NameNotFoundException e) {
				Message message = new Message();
				message.what = UPDATE_CLIENT;
				handlerUpdate.sendMessage(message);
				e.printStackTrace();
			}
			
		}
		
	}
	
	Handler handlerUpdate = new Handler(){
	    @Override  
	    public void handleMessage(Message msg) {  
	        super.handleMessage(msg);  
	        switch (msg.what) {
		        case UPDATE_CLIENT:  
		            //自动下载升级包并安装
		            updatePackage();
		            break;  
		        case GET_UNDATAINFO_ERROR:  
		            //服务器超时
		            Log.e(TAG, "获取服务器更新信息失败");
		            startActivityForMain();
		            break;    
		        case DOWN_ERROR:  
		            //下载apk失败  
		            Log.e(TAG, "下载新版本失败");
		            startActivityForMain();
		            break;    
	        }  
	    }  
	};
	
	  private void updatePackage() {
		  
		  postTask(new NetTask() {
			
			@Override
			public Object execute() throws Exception {
				try {
					
			        
					ftp.download(FTP.REMOTE_PATH + info.getClinicid(), null, HttpUtil.localFilePath + info.getClinicid());
					Result downloadResult = ftp.download(FTP.REMOTE_PATH+"/androidApk", downloadPath , HttpUtil.localFilePath);
					if (downloadResult.isSucceed()) {
			              Log.d(TAG, "download ok");
			              String fileName = HttpUtil.localFilePath + downloadPath;
			              Intent intent = new Intent(Intent.ACTION_VIEW);
			              intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
			              startActivity(intent);
			              
			          } else {
			              Log.d(TAG, "download fail");
			          }
			          
				} catch (IOException e) {
					Log.e(TAG, "升级过程异常");
					e.printStackTrace();
				}finally{
					
				}
				
				return null;
			}
		}, new UiTask() {
			
			@Override
			public void execute(Exception e, Object result) {
				// TODO Auto-generated method stub
				
			}
		});
		
          
	  }
	  
	public void startActivityForMain(){
		//1:2个listView模式，2：一个listview模式，3：一半宣教一半视频模式
		Intent intent = new Intent();
		if("1".equals(info.getView_model())){
			intent.setClass(ReadyActivity.this, MainActivity.class);
		}
		else if("2".equals(info.getView_model())){
			intent.setClass(ReadyActivity.this, MainActivity2.class);
		}
		else if("3".equals(info.getView_model())){
			intent.setClass(ReadyActivity.this, MainActivity3.class);
			intent.putExtra("videoUrl", HttpUtil.localFilePath+info.getClinicid());
		}else{
			intent.setClass(ReadyActivity.this, MainActivity.class);
			
		}
		
		intent.putExtra("waitIp", info.getWait_ip());
		intent.putExtra("localIp", ip);
		intent.putExtra("clinicId", info.getClinicid());
		startActivity(intent);
		finish();
	}

}
