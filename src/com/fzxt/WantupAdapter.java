package com.fzxt;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * ����һЩʵ�÷�����adapter
 * @param <T>
 */
public abstract class WantupAdapter<T> extends ArrayAdapter<T>{
	private LayoutInflater inflater;
	private boolean haveLoadAllDatas = false;
	
	protected List<T> dataList;
	protected Context context;
	protected int rowLayout;
	
	/**
	 * �Ƿ�ÿ�ж�ʹ��ͬһ��view
	 */
	private boolean singleRowView = true;
	
	public WantupAdapter(Context context, int rowLayout, List<T> tList){
		super(context, 0, tList);
		dataList = tList;
		this.context = context;
		this.rowLayout = rowLayout;
		Activity activity = (Activity) getContext();
		inflater = activity.getLayoutInflater();
	}
	
	/**
	 * ���췽��
	 * @param context
	 * @param rowLayout
	 * @param tList
	 * @param singleRowView	�Ƿ�ÿ�ж�ʹ��ͬһ��view
	 */
	public WantupAdapter(Context context, int rowLayout, List<T> tList, boolean singleRowView){
		this(context, rowLayout, tList);
		this.singleRowView = singleRowView;
	}
	
	/**
	 * ������д�ķ������൱��adapter��getView
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public abstract View getRowView(int position, View convertView, ViewGroup parent);

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(singleRowView){
			if(convertView == null){
				convertView = inflater.inflate(rowLayout, null);
			}
		}else{
			convertView = inflater.inflate(rowLayout, null);
		}
		return getRowView(position, convertView, parent);
	}
	
	/**
	 * ����һҳ����,����������list��СС��һҳ�Ĵ�С������Ϊ���������Ѽ�����
	 * @param pageLimit һҳ���ݵ�������Ϊ0��ʾ������
	 * @param sms ��������list
	 * @throws Exception
	 */
	public void loadList(int pageLimit, List<T> list){
		dataList.addAll(list);
		this.notifyDataSetChanged();
		if(pageLimit == 0 || list.size() < pageLimit){
			//�Ѿ�������ȫ������
			haveLoadAllDatas = true;
		}
	}
	
	/**
	 * �Ƴ�����
	 * @param list
	 */
	public void removeList(List<T> list){
		dataList.removeAll(list);
		this.notifyDataSetChanged();
	}
	
	public void removeAll() {
		dataList.clear();
		this.notifyDataSetChanged();
	}
	
	/**
	 * �Ƿ��Ѽ�������������
	 * @return
	 */
	public boolean hasLoadAllDatas(){
		return haveLoadAllDatas;
	}
}
