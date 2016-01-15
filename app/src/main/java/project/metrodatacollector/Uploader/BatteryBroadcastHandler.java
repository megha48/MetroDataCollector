package project.metrodatacollector.Uploader;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import project.metrodatacollector.CommonUtils;
import project.metrodatacollector.SensingController;
import project.metrodatacollector.CommonThings.CommonFunctions;

public class BatteryBroadcastHandler extends BroadcastReceiver {
    private final static String TAG = "BatteryBroadcastHandler";
    
    SharedPreferences sharedPref;
    @Override
    public void onReceive(Context context, Intent intent) {

    	String FILE_UPL = "fileupload";
        if(intent.getAction()==Intent.ACTION_POWER_CONNECTED){
            Log.i(TAG, "power connected");
            //Check if the phone is connected to internet using Wi-Fi
            // Start File uploading service
            int internetStatus = CommonUtils.isInternetAvailable(context);
            //Getting userid from preferences and checking whether userid is not null
            sharedPref= PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            String userid = sharedPref.getString("USERID", "");
            if(internetStatus>1 && !userid.isEmpty()){

         	    Editor editor = sharedPref.edit();
        		editor.putBoolean(FILE_UPL, true);
        		editor.commit();
            	//CommonFunctions.CompressandSend();
            	setRecurringAlarmForFileUpload(context.getApplicationContext());
            	SensingController.unregisterAlarm(context.getApplicationContext());
                //Intent uploadServiceIntent = new Intent(context, FileUploaderService.class);
                //context.startService(uploadServiceIntent);
            }
            else{
                Log.i(TAG,"no internet connection");
            }
        }
        else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED)
        {
            Log.i(TAG,"power disconnected");
            // Check if the file uploading service is running
            // If yes, check battery status and decide whether to keep uploading files or not
            //for time being stopping it need to work on this
            sharedPref= PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            Editor editor = sharedPref.edit();
    		editor.putBoolean(FILE_UPL, false);
    		editor.commit();
        	SensingController.registerSensingAlarm(context.getApplicationContext());
            killAlarm(context.getApplicationContext());
        }

    }

    public void registerChargerPlugIn(Context context){
        IntentFilter connectedFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        IntentFilter isDisconnectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(BatteryBroadcastHandler.this, connectedFilter);
        context.registerReceiver(BatteryBroadcastHandler.this, isDisconnectedFilter);
    }
    
    
    public void unregisterChargerPlugIn(Context context){
        context.unregisterReceiver(BatteryBroadcastHandler.this);
        killAlarm(context);
    }
    
    private void setRecurringAlarmForFileUpload(Context context) {

		Calendar uploadTime = Calendar.getInstance();
		uploadTime.add(Calendar.SECOND, 10);
		Intent intent = new Intent(context, FileUploaderService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		am.setRepeating(
				AlarmManager.RTC_WAKEUP,
				uploadTime.getTimeInMillis(),
				300 * 1000,
				pi);
	}

	private void killAlarm(Context context) {
		final Intent intent = new Intent(context.getApplicationContext(),
				FileUploaderService.class);
		final PendingIntent pending = PendingIntent.getBroadcast(context.
				getApplicationContext(), 0, intent, 0);
		AlarmManager alarmMgr = (AlarmManager) context.getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(pending); // cancel others.
		context.stopService(intent);
	}
}
