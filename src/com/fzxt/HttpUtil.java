package com.fzxt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;


@SuppressLint("NewApi")
public class HttpUtil {

	
	//����HttpClient����
	public static HttpClient httpClient = new DefaultHttpClient();
	//������ת��URL������ǲ���

	public int socketServerPort = 0;
	public String webServerPort = null;
	
//	public static String hostName = "192.168.1.103";
//	public static String userName = "vs2008";
//	public static String password = "1";
//	public static String serverIp = "192.168.1.103";
	
	public String ftpHostName = null;
	public String userName = null;
	public String password = null;
	public String webServerIp = null;
	
	public String localFilePath = "/mnt/sdcard/";
	public String BASE_URL = null;
	public String updateServerUrl = null;
	public String downloadPath = null;
	
	public HttpUtil(){
		Properties props = new Properties();     
        try {     
            props.load(HttpUtil.class.getResourceAsStream("netconfig.properties"));
            setFtpHostName( props.getProperty("ftpHostName"));
            setUserName(props.getProperty("userName"));
            setPassword(props.getProperty("password"));
            setWebServerIp(props.getProperty("webServerIp"));
            setSocketServerPort(Integer.parseInt(props.getProperty("socketServerPort")));
            setWebServerPort(props.getProperty("webServerPort"));
            
            BASE_URL = "http://"+getWebServerIp()+":"+getWebServerPort()+"/fzxt_tj/";
            updateServerUrl = "http://"+getWebServerIp()+":"+getWebServerPort()+"/fzxt_tj/update.xml";
        } catch (Exception e) {     
            e.printStackTrace();     
        }
	}
	
	
	
	
	
	
//	public final String BASE_URL = "http://"+getWebServerIp()+":"+getWebServerPort()+"/fzxt_tj/";
//	public final String updateServerUrl = "http://"+getWebServerIp()+":"+getWebServerPort()+"/fzxt_tj/update.xml";
	
	public static final String key = "1111111111111111";
	//����ʱʱ��20��
	public static final long timeout = 20000;
	//ϵͳ�汾��
	public static final int OS_VERSION = android.os.Build.VERSION.SDK_INT;	//15����ʹ�����߳�ͬ������
	//�Զ����δ��¼ʱresponse���ص�״̬�룬�����˵� filter.LoginCheckFilter��Ӧ
	private static final int noLoginErrorCode = 99;
	public static final int limit = 10;
	/**
	 * 
	 *  url ���������URL
	 *  ��������Ӧ�ַ���
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 *  
	 */
	public static String getRequest(String url) throws ClientProtocolException, IOException{
		//����HttpGet����
		HttpGet get = new HttpGet(url);
		// ����GET����
		HttpResponse httpResponse =  httpClient.execute(get);
		// ����������ɹ��ط�����Ӧ
		if(httpResponse.getStatusLine().getStatusCode()==200){
			// ��ȡ��������Ӧ�ַ���
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		}
		return null;
	}
	
	private static String doPostRequest(String url,Map<String,String> rawParams) throws Exception  {
		// ����HttpPost����
		HttpPost post = new HttpPost(url);
		// ������ݲ��������Ƚ϶�Ļ����ԶԴ��ݵĲ������з�װ
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(String key:rawParams.keySet()){
			//��װ�������
			params.add(new BasicNameValuePair(key,rawParams.get(key)));
		}
		// �����������
		post.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
		// ����POST����
		HttpResponse httpResponse = httpClient.execute(post);
		int returnCode = httpResponse.getStatusLine().getStatusCode();
		if(returnCode==200){
			// ��ȡ��������Ӧ�ַ���
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		}else if(returnCode==noLoginErrorCode){
			throw new Exception();
		}
		return null;
	}
	
	/**
	 * ����ͬ��http����
	 * @param url ���������URL
	 * @param params �������
	 * @return ��������Ӧ�ַ���
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String postRequest(String url,Map<String,String> rawParams) throws Exception     {
		if(OS_VERSION < 15){
			return doPostRequest(url, rawParams);
		}
		final Map retMap = new HashMap();
		final Object lock = new Object();
		postRequestAsyn(url, rawParams, new Callback(){
			
			@Override
			public void call(String result, Exception e) {
				retMap.put("result", result);
				retMap.put("exception", e);
				synchronized(lock){
					lock.notifyAll();
				}
			}
		});
		synchronized(lock){
			lock.wait(timeout);	//20�볬ʱ
		}
		if(!retMap.containsKey("result")){
			throw new Exception("����ʱ");
		}
		Exception e = (Exception)retMap.get("exception");
		if(e != null){
			throw e;
		}
		Object result = retMap.get("result");
		return result == null ? null : result.toString();
	}
	
	/**
	 * �첽����http����Ļص��ӿ�
	 * 
	 */
	public static interface Callback{
		/**
		 * �ص�����
		 * @param result	��������Ӧ���ַ���
		 * @param e		����������׳����쳣��Ϊnull��ʾ���쳣
		 */
		public void call(String result, Exception e);
	}
	/**
	 * �첽����http����
	 * @param url	����url
	 * @param rawParams		�������
	 * @param callback	�ص�����
	 */
	public static void postRequestAsyn(final String url, final Map<String,String> rawParams, 
			final Callback callback) {
		new Thread(){
			public void run() {
				try {
					String result = doPostRequest(url, rawParams);
					callback.call(result, null);
				} catch (Exception e) {
					callback.call("", e);
				}
			}
		}.start();
	}
	
	
	
	/***************************************�ύ�첽���񹤾�******************************************/
	/**
	 * �첽�����������ʱû�õ�
	 */
	private static Object[] taskParam = new Object[]{};
	
	/**
	 * �ύ�첽����
	 * @author chris
	 * @param context
	 * @param netTask	���繤������
	 * @param uiTask	UI��������
	 * @param loadMask	�Ƿ�ʹ������
	 * @param checkLogin �Ƿ����¼״̬
	 */
	@SuppressWarnings("unchecked")
	public static void postTask(Context context,  NetTask netTask, 
			UiTask uiTask, boolean loadMask){
		WantupAsyncTask  task = new WantupAsyncTask (context, netTask, uiTask, loadMask);
		task.execute(taskParam);
	}
	
	@SuppressWarnings("rawtypes")
	public static class WantupAsyncTask  extends AsyncTask{
		private NetTask netTask;
		private UiTask uiTask;
		private boolean loadMask = false;
		private MyDialog proDia;	//��������
		private Exception exception;	//�쳣����
		private Context context;
		
		WantupAsyncTask(Context context, NetTask netTask, UiTask uiTask, 
				boolean loadMask){
			this.context = context;
			this.netTask = netTask;
			this.uiTask = uiTask;
			this.loadMask = loadMask;
		}
		@Override
		protected Object doInBackground(Object... arg0) {
			//��ִ̨����������
			try{
				if(netTask != null){
					return this.netTask.execute();
				}
				return null;
			}catch(Exception e){
				if(proDia != null){
					proDia.dismiss();
				}
				this.exception = e;
				return null;
			}
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
			//��̨��������ִ����ɺ�Ķ���
			if(uiTask != null){
				this.uiTask.execute(result, exception);
			}
			if(proDia != null){
				try{
					proDia.dismiss();
				}catch(IllegalArgumentException e){
					//��Ļ��תʱ���ܲ������쳣
					if(e.getMessage() != null){
						Log.w("onPostExecute", e.getMessage());
					}
				}
			}
		}
		@Override
		protected void onPreExecute() {
			//��������֮ǰ�Ķ���
			if(loadMask && context != null){
				try {
					proDia = new MyDialog(context, R.style.MyDialogStyle, R.layout.loading, false).create();
					proDia.setCancelable(true);
					proDia.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							WantupAsyncTask.this.cancel(true);
						}
					});
					proDia.show();
				} catch (Exception e) {
					if(proDia != null){
						proDia.dismiss();
					}
				}
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}
	}
	
	public static interface NetTask<T>{
		/**
		 * ��������
		 * @author chris
		 * @return	����ֵ���Զ����ݸ�ui����
		 * @throws Exception
		 */
		public T execute() throws Exception;
	}
	
	public static interface UiTask<T>{
		/**
		 * ui��������
		 * @author chris
		 * @param result	�������񷵻ص�ֵ
		 * @param e			���������׳����쳣����Ϊnull�����ʾ���쳣
		 */
		public void execute(T result, Exception e);
	}
	public int getSocketServerPort() {
		return socketServerPort;
	}

	public void setSocketServerPort(int socketServerPort) {
		this.socketServerPort = socketServerPort;
	}

	public String getWebServerPort() {
		return webServerPort;
	}

	public void setWebServerPort(String webServerPort) {
		this.webServerPort = webServerPort;
	}

	public String getFtpHostName() {
		return ftpHostName;
	}

	public void setFtpHostName(String ftpHostName) {
		this.ftpHostName = ftpHostName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWebServerIp() {
		return webServerIp;
	}

	public void setWebServerIp(String webServerIp) {
		this.webServerIp = webServerIp;
	}
	

	
	
	
	
}
