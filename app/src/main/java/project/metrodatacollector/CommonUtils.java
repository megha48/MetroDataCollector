package project.metrodatacollector;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import project.metrodatacollector.CommonThings.Constants;

public class CommonUtils {
    private static final String TAG = "CommonUtils";
    public static long TotalTime = 0L;
    public static long startTime = 0L;
    public static long endTime = 0L;
    public static long totaltime = 0L;
    public static int Rank = 100;
    public static String lastRankTime ="" ;
    private static File externalStorageAccl = Environment.getExternalStorageDirectory();
    private static String sensorDataFolderPath = externalStorageAccl.getAbsolutePath()
            + File.separator + Constants.MEDIA_DIR_NAME + File.separator;
    private  static  String filePath = sensorDataFolderPath + "accLog.txt";
    /**
     * getFilename
     * ***/
    public static String getFilepath(String filename){
        File logFile = new File(sensorDataFolderPath + filename);
        if (!logFile.exists()){
            try
            {
                File f = new File(sensorDataFolderPath);
                if(!f.exists()) f.mkdir();
                logFile.createNewFile();
            }
            catch (IOException e){
                Log.e(TAG, "Exception in file creation");
            }
        }
        return  sensorDataFolderPath+filename;
    }

    public static String getSensorDataFolderPath(){
        return  sensorDataFolderPath;
    }

    /**
     * Convert unixtime in milliseconds to human readable data time of format dd/MM/yyyy HH:mm:ss
     * @param t unixtime
     * @return corresponding human readable datetime
     * */
    public static String unixTimestampToString(long t){
        String str = null;
        str = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(t));
        return str;
    }

    public static int isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            //If internet is available from wifi return 2
            if(isWiFi)
                return 2;
                //If internet is available from Mobile Data return 1
            else
                return 1;
        }
        //If internet connection is unavailable, return -1

        return  -1;
    }
    
    public static String setDuration(long duration){
        duration=duration/1000L;
        long hours=duration/3600;
        long minutes=(duration%3600)/60;
        long seconds= ((duration%3600)%60);

        String valueString = hours+"h "+minutes+"m "+seconds+"s";
        return valueString;
    }
}
