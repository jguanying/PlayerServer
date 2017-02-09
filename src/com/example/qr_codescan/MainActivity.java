package com.example.qr_codescan;


import com.socket.server.AndroidServer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static int SCANNIN_GREQUEST_CODE = 1;
	/**
	 * ��ʾɨ����
	 */
	private TextView mTextView ;
	/**
	 * ��ʾɨ���ĵ�ͼƬ
	 */
	private ImageView mImageView;
	
	public ReceiveBroadCast receiveBroadCast;  //广播实例

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// 注册广播接收
        receiveBroadCast = new ReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.qu.broadCastFlag");    //只有持有相同的action的接受者才能接收此广播
        registerReceiver(receiveBroadCast, filter);
		
		mTextView = (TextView) findViewById(R.id.result); 
		mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);
		
		//�����ť��ת����ά��ɨ����棬�����õ���startActivityForResult��ת
		//ɨ������֮������ý���
		Button mButton = (Button) findViewById(R.id.button1);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(MainActivity.this, PlayerFullScreenActivity.class);
//				intent.putExtra(PlayerFullScreenActivity.PLAY_DATA, "/mnt/sdcard/film/video.mp4");
//				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
				Toast.makeText(getApplicationContext(), "sendResult:" + "111111", Toast.LENGTH_LONG).show();
				Intent intent = new Intent();  //Itent就是我们要发送的内容
	            intent.putExtra("data", "rrrrrrrrrrrrrrrr");
	            intent.setAction("com.qu.broadCastFlag");   //设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
	            sendBroadcast(intent);   //发送广播
				
			}
		});
		
		Intent serviceIntent = new Intent(getApplicationContext(), AndroidServer.class);
		startService(serviceIntent);
		
//		Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);  
//		  
//		mHomeIntent.addCategory(Intent.CATEGORY_HOME);  
//		mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  
//		                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
//		startActivity(mHomeIntent);
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				//��ʾɨ�赽������
				mTextView.setText(bundle.getString("result"));
				//��ʾ
				mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
		}
    }
	
	public class ReceiveBroadCast extends BroadcastReceiver
	{
	 
	        @Override
	        public void onReceive(Context context, Intent intent)
	        {
	            //得到广播中得到的数据，并显示出来
	            String message = intent.getStringExtra("data");
	            mTextView.setText(message);
	        }
	 
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (receiveBroadCast != null) {
			unregisterReceiver(receiveBroadCast);
			receiveBroadCast = null;
		}
	}
	
	

}
