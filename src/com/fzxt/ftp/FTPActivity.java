/*package com.fzxt.ftp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

import com.fzxt.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

*//**
 * ����Activity.
 * 
 * @author cui_tao
 *
 *//*
public class FTPActivity extends Activity {
    *//**
     * ��ǩ.
     *//*
    static final String TAG = "FTPActivity";

    *//**
     * FTP.
     *//*
    private FTP ftp;

    *//**
     * FTP�ļ�����.
     *//*
    private List<FTPFile> remoteFile;

    *//**
     * �����ļ�����.
     *//*
    private List<File> localFile;

    *//**
     * ���ظ�Ŀ¼.
     *//*
    private static final String LOCAL_PATH = "/mnt/sdcard/";

    *//**
     * ��ǰѡ����.
     *//*
    private int position = 0;

    *//**
     * ListView.
     *//*
    private ListView listMain;

    *//**
     * �л������ذ�ť.
     *//*
    private Button buttonChangeLocal;

    *//**
     * �л���FTP��ť.
     *//*
    private Button buttonChangeRemote;

    *//**
     * ���ذ�ť.
     *//*
    private Button buttonDownload;

    *//**
     * �ϴ���ť.
     *//*
    private Button buttonUploading;

    *//**
     * �Ͽ����Ӱ�ť.
     *//*
    private Button buttonClose;

    *//**
     * ��������.
     *//*
    private String hostName;

    *//**
     * �û���.
     *//*
    private String userName;

    *//**
     * ����.
     *//*
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ��ʼ����ͼ
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // �رշ���
        try {
            ftp.closeConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    *//**
     * ��ʼ����ͼ.
     *//*
    private void initView() {
        // ��ʼ���ؼ�
        // ��ȡ��¼��Ϣ
        loginConfig();
        // ListView����
        listMain.setOnItemClickListener(listMainItemClick);
        // ListViewѡ����ı�
        listMain.setOnItemSelectedListener(listMainItemSelected);
        // �л�������
        buttonChangeLocal.setOnClickListener(buttonChangeLocalClick);
        // �л���FTP
        buttonChangeRemote.setOnClickListener(buttonChangeRemoteClick);
        // ����
        buttonDownload.setOnClickListener(buttonDownloadClick);
        // �ϴ�
        buttonUploading.setOnClickListener(buttonUploadingClick);
        // �Ͽ�FTP����
        buttonClose.setOnClickListener(buttonCloseClick);
        // ����FTP��ͼ
        loadRemoteView();
    }

    *//**
     * ��ȡ��¼��Ϣ.
     *//*
    private void loginConfig() {
        Intent intent = getIntent();
        hostName = intent.getStringExtra("hostName");
        userName = intent.getStringExtra("userName");
        password = intent.getStringExtra("password");
    }

    *//**
     * ����FTP��ͼ.
     *//*
    private void loadRemoteView() {
        try {
            if (ftp != null) {
                // �ر�FTP����
                ftp.closeConnect();
            }
            // ��ʼ��FTP
            Log.e("hostName", hostName);
            Log.e("userName", userName);
            Log.e("passwrod", password);
            ftp = new FTP(hostName, userName, password);
            // ��FTP����
            ftp.openConnect();
            // ��ʼ��FTP�б�
            remoteFile = new ArrayList<FTPFile>();
            // ���Ŀؼ��ɼ�
            buttonChangeLocal.setVisibility(Button.VISIBLE);
            buttonChangeRemote.setVisibility(Button.INVISIBLE);
            buttonDownload.setVisibility(Button.VISIBLE);
            buttonUploading.setVisibility(Button.INVISIBLE);
            // ����FTP�б�
            remoteFile = ftp.listFiles(FTP.REMOTE_PATH);
            // FTP�б�������
            RemoteAdapter adapter = new RemoteAdapter(this, remoteFile);
            // �������ݵ�ListView
            listMain.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    *//**
     * ���ر�����ͼ.
     *//*
    private void loadLocalView() {
        // ��ʼ�������б�
        localFile = new ArrayList<File>();
        // ���Ŀؼ��ɼ�
        buttonChangeLocal.setVisibility(Button.INVISIBLE);
        buttonChangeRemote.setVisibility(Button.VISIBLE);
        buttonDownload.setVisibility(Button.INVISIBLE);
        buttonUploading.setVisibility(Button.VISIBLE);
        // ���ر����б�
        getFileDir(LOCAL_PATH);
        // �����б�������
        LocalAdapter adapter = new LocalAdapter(this, localFile);
        // �������ݵ�ListView
        listMain.setAdapter(adapter);
    }

    *//**
     * ���ر����б�.
     * @param filePath �ļ�Ŀ¼
     *//*
    private void getFileDir(String filePath) {
        // ��ȡ��Ŀ¼
        File f = new File(filePath);
        // ��ȡ��Ŀ¼�������ļ�
        File[] files = f.listFiles();
        // ѭ����ӵ������б�
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isHidden() || file.getName().equals("LOST.DIR")) {
                continue;
            }
            localFile.add(file);
        }
    }

    *//**
     * ListView�����¼�.
     *//*
    private OnItemClickListener listMainItemClick = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int location, long arg3) {
            if (buttonChangeLocal.getVisibility() == Button.VISIBLE) {
                Toast.makeText(FTPActivity.this, remoteFile.get(location).getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FTPActivity.this, localFile.get(location).getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    *//**
     * ListViewѡ����ı��¼�.
     *//*
    private OnItemSelectedListener listMainItemSelected = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View view, int location, long arg3) {
            // ��ȡ��ǰѡ����
            FTPActivity.this.position = location;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    };

    *//**
     * �л�������.
     *//*
    private OnClickListener buttonChangeLocalClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // ���ر�����ͼ
            loadLocalView();
        }
    };

    *//**
     * �л���FTP.
     *//*
    private OnClickListener buttonChangeRemoteClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // ����FTP��ͼ
            loadRemoteView();
        }
    };

    *//**
     * ����.
     *//*
    private OnClickListener buttonDownloadClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Result result = null;
            try {
                // ����
                result = ftp.download(FTP.REMOTE_PATH, remoteFile.get(position).getName(), LOCAL_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result.isSucceed()) {
                Log.e(TAG, "download ok...time:" + result.getTime() + " and size:" + result.getResponse());
                Toast.makeText(FTPActivity.this, "���سɹ�", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "download fail");
                Toast.makeText(FTPActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
            }
        }
    };

    *//**
     * �ϴ�.
     *//*
    private OnClickListener buttonUploadingClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Result result = null;
            try {
                // �ϴ�
                result = ftp.uploading(localFile.get(position), FTP.REMOTE_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result.isSucceed()) {
                Log.e(TAG, "uploading ok...time:" + result.getTime() + " and size:" + result.getResponse());
                Toast.makeText(FTPActivity.this, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "uploading fail");
                Toast.makeText(FTPActivity.this, "�ϴ�ʧ��", Toast.LENGTH_SHORT).show();
            }
        }
    };

    *//**
     * �Ͽ�����.
     *//*
    private OnClickListener buttonCloseClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                // �ر�FTP����
                ftp.closeConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
    };

}*/