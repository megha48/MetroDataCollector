package project.metrodatacollector.CommonThings;

import android.os.Environment;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CommonFunctions {
    private static String URL = Constants.URL;
    private static String filenames[]={"/acclLog.txt","/TimeLog.txt","/locationLog.txt","/StationLog.txt"};

	private static final int BUFFER = 1024;
    /**
     * This function is used to check if the Server side code is ready to accept request from the client
     * **/
    public static String sendRequestToServer(String userData, String key){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 5000000);
        HttpConnectionParams.setSoTimeout(httpParameters, 100000000);
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(URL+ key);
        String serverResponse = "null";
        try {
            httppost.setEntity(new StringEntity(userData));
            HttpResponse response = httpclient.execute(httppost);
            Log.i("info", "sending request to server");

            BufferedReader BuffRead = new BufferedReader( new InputStreamReader(response.getEntity().getContent(),"UTF-8") );
            serverResponse = BuffRead.readLine();
        }
        catch(ConnectTimeoutException e){
            Log.e("Timeout Exception: ", e.toString());
        }
        catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            Log.e("UnsupportedEncoding: ", e.toString());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e("ClientProtocol EX: ",e.toString());
        }
        catch(HttpHostConnectException e){
            Log.e("CommonUtils ",e.toString());
            //Toast.makeText(context, "Unable to connect to server. Please try after sometime", Toast.LENGTH_LONG).show();
            serverResponse ="serverDown";
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("IOException: ",e.toString());
        }
        return serverResponse;

    }
    public static String[] listFilesToUpload(){
        File externalStorageAccl = Environment.getExternalStorageDirectory();
        String sensorDataFilePath = externalStorageAccl.getAbsolutePath()+ File.separator + Constants.MEDIA_DIR_NAME + File.separator;
        File dir = new File(sensorDataFilePath);
        FilenameFilter uploadFilter = new FilenameFilter() {
            File f;
            public boolean accept(File dir, String name) {
                if(name.startsWith("wifiUpload"))
                    return true;
                f = new File(dir.getAbsolutePath()+"/"+name);
                return f.isDirectory();
            }
        };
        String files[] = dir.list(uploadFilter);
        return files;
    }
    
    public static String ConvertTimeStampFile(long milliseconds)
	 {		 
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_H_mm_ss"); //SS is for fraction second
		 String date = sdf.format(milliseconds);
		 return date;
	 }
    
    public static void CompressandSend(){
		 //String filename = "wifiCall.zip";
		 long currentTime = System.currentTimeMillis();
		 File externalStorageAccl = Environment.getExternalStorageDirectory();
	     String sensorDataFilePath = externalStorageAccl.getAbsolutePath()+ File.separator + Constants.MEDIA_DIR_NAME + File.separator;
	      
    	 String filename = "wifiUpload"+ConvertTimeStampFile(currentTime)+".zip";   		 
		 String inputPath = sensorDataFilePath;
			
		// declare an array for storing the files i.e the path
		// of your source files
		String[] s = new String[filenames.length];
		int strl = 0;
		Log.i("Files to be stored:",""+filenames.length);
		for(int i=0;i<filenames.length;i++){
			String filepath = inputPath + filenames[i];
			File logFile = new File(filepath);
			if (!logFile.exists()){
				//do nothing
			}
		
			else{
				s[strl++] = filepath;
			}
		}
		
		//Base case in case no file exists
		if(strl==0){return;}
		
		if(zip(s, inputPath + filename,strl)){
			deleteWifiandCall(s,strl);
		}		
	 }

	 public static boolean zip(String[] _files, String zipFileName,int len) {
			try {
				BufferedInputStream origin = null;
				FileOutputStream dest = new FileOutputStream(zipFileName);
				ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
						dest));
				byte data[] = new byte[BUFFER];

				for (int i = 0; i < len; i++) {
					Log.v("Compress", "Adding: " + _files[i]);
					FileInputStream fi = new FileInputStream(_files[i]);
					origin = new BufferedInputStream(fi, BUFFER);

					ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;

					while ((count = origin.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					origin.close();
				}
			  out.close();
			  return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	 
	public static void deleteWifiandCall(String[] _files,int len){
		for (int i = 0; i <len; i++) {
			File delfile = new File(_files[i]);
	        delfile.delete();
	        Log.d("Delete","Deleting call and wifi txt's");				
		} 		
	 }		
    
	public static long getTimeDiff(String start,String end){
    	java.text.DateFormat df = new SimpleDateFormat("HH:mm:ss");
        java.util.Date date1 = null,date2 = null;
		try {
			date1 = df.parse(start);
			date2 = df.parse(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return ((date2.getTime() - date1.getTime())/1000L);
    }
}
