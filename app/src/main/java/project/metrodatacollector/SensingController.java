package project.metrodatacollector;

import project.metrodatacollector.CommonThings.Constants;
import project.metrodatacollector.Uploader.LockHandler;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

public class SensingController extends BroadcastReceiver {
    private static String TAG = "SensingController";
    private static final String COLLECT_SENSOR_DATA = "collect_sensor_data";
    private static final String STOP_COLLECT_SENSOR_DATA = "stop_collect_sensor_data";

    private static final int ALARM_ID = 40791;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Alarm received for sensing controller");
        String action = intent.getAction();
        //Intent for motion sensor service
        final Intent motionSensorServiceIntent = new Intent(context, SensorService.class);
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        //final Intent WiFiServiceIntent = new Intent(context, WiFiService.class);
        final Intent locationSensorIntent = new Intent(context, LocationService.class);
        //final Intent activityRecognitionIntent = new Intent(context, ActivityService.class);
        if(action.equals(COLLECT_SENSOR_DATA))
        {

            context.startService(motionSensorServiceIntent);
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //GPS Provider disabled
                context.startService(locationSensorIntent);
            }
            //context.startService(activityRecognitionIntent);

            Log.d(TAG, "collect sensor data broadcast received");
        }
        else
            Log.d(TAG,"Ignoring alarm");
    }


    public static void registerSensingAlarm(Context context){
        Log.d(TAG, "Setting alarm for Sensing Controller");
        AlarmManager alarmMgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // use this class as the receiver
        Intent intent = new Intent(context, SensingController.class);
        intent.setAction(COLLECT_SENSOR_DATA);

        // create a PendingIntent that can be passed to the AlarmManager
        // Using an alarm ID ensures that the previous ones are cancelled before this one is set
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // create a repeating alarm, that goes of every 'frequency' seconds
        // AlarmManager.ELAPSED_REALTIME_WAKEUP wakes up the CPU only
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 2000, Constants.ALARM_TIME_INTERVAL_BETWEEN_CHECKS,
                pendingIntent);
        Log.d(TAG, "Alarm Set");

    }

    public static void unregisterAlarm(Context context){
        final Intent motionSensorServiceIntent = new Intent(context, SensorService.class);
        final Intent locationSensorIntent = new Intent(context, LocationService.class);
        //final Intent activityRecognitionIntent = new Intent(context, ActivityService.class);

        AlarmManager alarmMgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // use this class as the receiver
        Intent intent = new Intent(context, SensingController.class);
        intent.setAction(COLLECT_SENSOR_DATA);

        // create a PendingIntent that can be passed to the AlarmManager
        // Using an alarm ID ensures that the previous ones are cancelled before this one is set
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.cancel(pendingIntent);
        Log.d(TAG, "Alarm Stopped");

        context.stopService(motionSensorServiceIntent);
        //context.stopService(locationSensorIntent);
    }
   }
