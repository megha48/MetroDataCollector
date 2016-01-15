package project.metrodatacollector.CommonThings;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Constants {

	//public static String URL ="http://192.168.53.211:800/";
	public static final int ALARM_TIME_INTERVAL_BETWEEN_CHECKS = 10000;
	public static String URL ="http://muc.iiitd.edu.in:9060/";
	//public static String URL ="http://subtle-champion-817.appspot.com/";
    public static String MEDIA_DIR_NAME ="MetroSenApp";
    public static String ACCL_FILENAME = "acclLog.txt";
    public static String GPS_FILENAME = "locationLog.txt";
    public static String TIME_FILENAME = "TimeLog.txt";
    public static String WIFI_FILENAME = "wifiLog.txt";

    public static String COLLEGEWIFIAP[]={"STUDENTS-M","STUDENTS-N","FACULTY-STAFF-N","GUEST-N","SENSOR"};
    //public static String COLLEGEWIFIAP[]={"beetel1","predator","Nextra FREE","inet"};
}
