package com.fzxt.service;

import java.io.File;

import com.fzxt.HttpUtil;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class UpdateService extends Service {
	private HttpUtil httpUtil = new HttpUtil();
	@Override
	public IBinder onBind(Intent intent) {
		String fileName = httpUtil.localFilePath + httpUtil.downloadPath;
        Intent intent1 = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
        startActivity(intent1);
		return null;
	}

}
