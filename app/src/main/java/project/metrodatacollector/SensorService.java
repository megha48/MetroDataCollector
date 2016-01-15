package project.metrodatacollector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import project.metrodatacollector.CommonThings.Constants;
import project.metrodatacollector.Uploader.LockHandler;

public class SensorService extends Service implements SensorEventListener{
    private String TAG = "SensorService";
    int i =0;
    long tempTime=0;
    int sampleCounter =0;
    Sensor mAcce;
    BufferedWriter accBuf;
    boolean isBufferWriterOpen = false;
    SensorManager mSensorManager;
    Context context;
    boolean isServiceRunning;
    PowerManager.WakeLock wl;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        context = getApplicationContext();
        isServiceRunning = false;
        File logFile = new File(CommonUtils.getFilepath(Constants.ACCL_FILENAME));
        try {
            accBuf = new BufferedWriter(new FileWriter(logFile, true));
            isBufferWriterOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mAcce = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        CommonUtils.startTime = System.currentTimeMillis();
        //mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Log.i(TAG, Integer.toString(i));
        i++;
        saveLog(Integer.toString(i)+","+CommonUtils.unixTimestampToString(System.currentTimeMillis()));
        if(!isServiceRunning) {
            isServiceRunning = true;
            mSensorManager.registerListener(this, mAcce, SensorManager.SENSOR_DELAY_NORMAL);
            //mSensorManager.registerListener(this, mAcce, 66000);
            //mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        }
        wl =LockHandler.acquireWakeLock(context);
       // wl.acquire(Constants.ALARM_TIME_INTERVAL_BETWEEN_CHECKS);
        wl.acquire();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(this);
        Log.i("Sensor Service:","Stopped");
        if(isBufferWriterOpen) {
            try {
            	accBuf.flush();
                accBuf.close();
                Log.i(TAG, "BufferedWriter closed");
                isBufferWriterOpen = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if ( wl != null && wl.isHeld() == true) {
			try {
				wl.release();
				Log.d(TAG, "Wakelog SensorService realease");			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeInMillis = System.currentTimeMillis();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
           String msg = Float.toString(event.values[0]) +","+ Float.toString(event.values[1])+"," + Float.toString(event.values[2])+ ","+CommonUtils.unixTimestampToString(timeInMillis);
            findSamplingFreq(timeInMillis);
            saveDataInFile(msg);
            //Log.i(TAG," time: " + unixTimestampToString(timeInMillis));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //sampling frequency is 50 per second
    public void findSamplingFreq(long timestamp){
        if(tempTime!=0){
            float diff = (timestamp-tempTime)/1000;
            if(diff<1)
                sampleCounter +=1;
            else{
                Log.i("freq",Integer.toString(sampleCounter));
                tempTime = timestamp;
                sampleCounter = 0;
            }
        }
        else{
            tempTime = timestamp;
        }
        //Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();

    }

    /**
     * Saves line in filename file
     * ***/
    public void saveDataInFile(String line){
        try{

            accBuf.append(line);
            accBuf.newLine();
        }
        catch (IOException e){
            Log.e(TAG, e.getLocalizedMessage());

        }
    }
    public void saveLog(String line)
    {

        File logFile = new File(CommonUtils.getFilepath("log.txt"));
        try{
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(line);
            buf.newLine();
            buf.close();
        }
        catch (IOException e){
            Log.e(TAG, "Bufferwriter exception");

        }
    }
}
