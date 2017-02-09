package com.example.qr_codescan;

import com.socket.server.AndroidServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();  
//        Toast.makeText(context, "mobile:"+mobileInfo.isConnected()+"\n"+"wifi:"+wifiInfo.isConnected()  
//                        +"\n"+"active:"+activeInfo.getTypeName(), 1).show(); 
        Log.d("TAG", "mobile:"+mobileInfo.isConnected()+"\n"+"wifi:"+wifiInfo.isConnected()  
        +"\n"+"active:"+activeInfo.getTypeName());
        
        if (wifiInfo.isConnected()) {
    		Intent serviceIntent = new Intent(context, AndroidServer.class);
    		context.startService(serviceIntent);
        }
	}

}
