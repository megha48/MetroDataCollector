package project.metrodatacollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootStartUpReceiver extends BroadcastReceiver{
	private static String TAG = BootStartUpReceiver.class.getName();
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// Start Service On Boot Start Up
		Log.i(TAG,"Boot up : received");
		Intent i = new Intent(context, MainActivity.class);  
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);  
        
	}

}
