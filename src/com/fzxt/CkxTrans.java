package com.fzxt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CkxTrans {
	/**
     * ��json ת��ΪArrayList ��ʽ
     * @return
     */ 
    public static List<Map> getList(String jsonString) {
      List<Map> list = null;
      try{
    	  if (jsonString != null && jsonString.startsWith("\ufeff")) {  
    		  jsonString = jsonString.substring(1);  
    	  }  
    	  JSONArray jsonArray = new JSONArray(jsonString);
    	  JSONObject jsonObject;
    	  list = new ArrayList<Map>();
    	  for (int i = 0; i < jsonArray.length(); i++){
    		  jsonObject = jsonArray.getJSONObject(i);
    		  list.add(getMap(jsonObject.toString()));
    	  } 
      }catch (Exception e){ 
    	  e.printStackTrace(); 
      } 
      return list; 
	}
    
    /**
     * ��json ת��ΪList<String> ��ʽ
     * @param jsonString
     * @return
     */
    public static List<String> getStringList(String jsonString) {
        List<String> list = null;
        try{
      	  JSONArray jsonArray = new JSONArray(jsonString);
      	  JSONObject jsonObject;
      	  list = new ArrayList<String>();
      	  for (int i = 0; i < jsonArray.length(); i++){
      		  list.add(jsonArray.getString(i));
      	  } 
        }catch (Exception e){ 
      	  e.printStackTrace(); 
        } 
        return list; 
  	}
    
    /**
     * ��json���ת��ΪArrayList��ʽ,��:���ݹ�ת��Ƕ��json
     * @author chris
     * @param jsonString
     * @return
     */
    public static List<Map> getDeepList(String jsonString){
    	List<Map> list = null;
        try{
      	  JSONArray jsonArray = new JSONArray(jsonString);
      	  JSONObject jsonObject;
      	  list = new ArrayList<Map>();
      	  for (int i = 0; i < jsonArray.length(); i++){
      		  jsonObject = jsonArray.getJSONObject(i);
      		  list.add(getDeepMap(jsonObject.toString()));
      	  } 
        }catch (Exception e){ 
      	  e.printStackTrace(); 
        } 
        return list; 
    }
    
    /**
     *   ��json ����ת��ΪMap ����
     * @param jsonString
     * @return
     */ 
	@SuppressWarnings("unchecked")
	public static Map getMap(String jsonString)  {
		JSONObject jsonObject;
		Map valueMap = new HashMap();
		try{
			jsonObject = new JSONObject(jsonString);
			Iterator<String> keyIter = jsonObject.keys();
			String key;
			Object value;
			
			while (keyIter.hasNext()){
				key = keyIter.next();
				value = jsonObject.get(key);
				valueMap.put(key, value);
			}
			return valueMap;
		}catch (JSONException e){
			e.printStackTrace();
		}
		return valueMap;
	}
	
	/**
	 * ��json���ת��ΪMap��ʽ,��:���ݹ�ת��Ƕ��json
	 * @author chris
	 * @param jsonString
	 * @return
	 */
	public static Map getDeepMap(String jsonString)  {
		JSONObject jsonObject;
		Map valueMap = new HashMap();
		try{
			jsonObject = new JSONObject(jsonString);
			Iterator<String> keyIter = jsonObject.keys();
			String key;
			Object value;
			
			while (keyIter.hasNext()){
				key = keyIter.next();
				value = jsonObject.get(key);
				if(value instanceof JSONObject){
					value = getDeepMap(value.toString());
				}else if(value instanceof JSONArray){
					value = getDeepList(value.toString());
				}
				valueMap.put(key, value);
			}
			return valueMap;
		}catch (JSONException e){
			e.printStackTrace();
		}
		return valueMap;
	}
	
	/**
	 * û4���ַ�,��һ������
	 * @param appName
	 * @return
	 */
	public static String coincide(String appName,int count) {
		if(appName == null){
			return null;
		}
		char[] charArray = appName.toCharArray();
		String str = "";
		int n=0;
		for(int i=0;i<charArray.length;i++){
			str += charArray[i];
			n++;
			if(n==count){
				str += "<br>";
				n=0;
			}
		}
		return str;
	}
	
	/**
	 * get value from map,if the value if "null",then return null
	 * @param map
	 * @param columnName
	 * @return
	 */
	public static String getValue(Map map, String columnName){
		if(map == null){
			return null;
		}
		String value = map.get(columnName) + "";
		if("null".equals(value)){
			return null;
		}
		return value;
	}
}
