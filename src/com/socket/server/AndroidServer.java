package com.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.qr_codescan.PlayerFullScreenActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class AndroidServer extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
//		new SocketServer(getApplicationContext()).start();
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		new SocketServer(getApplicationContext()).start();
		super.onStart(intent, startId);
	}
	
	class SocketServer extends Thread {
		
		private Context mContext;
		
		SocketServer(Context context) {
			mContext = context;
		}

		@Override
		public void run() {
			try {
				ServerSocket serverSocket=new ServerSocket(1234);
				while(true)
				{
					System.out.println("SocketServer begin .... ");
					Message msgMessage1=new Message();  
		            msgMessage1.obj = "SocketServer begin .... ";  
		            handler.sendMessage(msgMessage1);  
		            Log.e("ThreadName", Thread.currentThread().getName());
					//���ܿͻ�������
					Socket client=serverSocket.accept();
					try
					{
						//���ܿͻ�����Ϣ
						BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
						String str=in.readLine();
						System.out.println("read:  "+str);
						//�������������Ϣ
//						PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
//						out.println("return	"+str);
						
//						String mp4name ="/mnt/sda/sda1/" + str;
						String mp4name ="" + str;
						Message msgMessage=new Message();  
			            msgMessage.obj = mp4name;  
			            handler.sendMessage(msgMessage);  
			            Log.e("ThreadName", Thread.currentThread().getName());
			            
//						Toast.makeText(getApplicationContext(), "准备播放文件路径：" + mp4name,
//							     Toast.LENGTH_LONG).show();
						
						Intent intent = new Intent();
						intent.setClass(mContext, PlayerFullScreenActivity.class);
						
//						String mp4name ="/mnt/sdcard/film/" + str;
						intent.putExtra(PlayerFullScreenActivity.PLAY_DATA, mp4name);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
						mContext.startActivity(intent);
						in.close();
//						out.close();
					} catch(Exception ex) {
						String mp4name ="" + ex.getMessage();
						Message msgMessage=new Message();  
			            msgMessage.obj = mp4name;  
			            handler.sendMessage(msgMessage); 
						ex.printStackTrace();
					}
					finally
					{
						String mp4name ="client close";
						Message msgMessage=new Message();  
			            msgMessage.obj = mp4name;  
			            handler.sendMessage(msgMessage);
						client.close();
						System.out.println("close");
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			super.run();
		}
		
	}
	
	private Handler handler=new Handler(){  
        public void handleMessage(Message msg){  
        	Toast.makeText(getApplicationContext(), "sendResult:" + msg.obj, Toast.LENGTH_LONG).show(); 
        	Intent intent = new Intent();  //Itent就是我们要发送的内容
            intent.putExtra("data", (String)(msg.obj));
            intent.setAction("com.qu.broadCastFlag");   //设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
            sendBroadcast(intent);   //发送广播
        }  
    }; 

}
