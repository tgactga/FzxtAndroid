package com.fzxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSocket {
	public static volatile Socket mSocket = null;
	public static volatile BufferedReader mBufferedReader	= null;
	public static volatile PrintWriter mPrintWriter = null;
	
	public static Boolean messageActivityStart = false;
	synchronized public static Socket getmSocket() {
		return mSocket;
	}

	synchronized public static void setmSocket(Socket mSocket1) {
		mSocket = mSocket1;
	}

	public BufferedReader getmBufferedReader() {
		return mBufferedReader;
	}

	public static void setmBufferedReader(BufferedReader mBufferedReader1) {
		mBufferedReader = mBufferedReader1;
	}

	public PrintWriter getmPrintWriter() {
		return mPrintWriter;
	}

	public static void setmPrintWriter(PrintWriter mPrintWriter1) {
		mPrintWriter = mPrintWriter1;
	}
	/**
	 * ������Ϣ��������
	 * @param message
	 */
	public static void sendMessageToServer(String message){
		if(mPrintWriter != null){
			mPrintWriter.print(message+ "\n");
	    	mPrintWriter.flush();
	    	
		}
		
	}

//	public static Boolean getMessageActivityStart() {
//		return messageActivityStart;
//	}
//
//	public static void setMessageActivityStart(Boolean messageActivityStart1) {
//		messageActivityStart = messageActivityStart1;
//	}
	
	
	
}
