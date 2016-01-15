package project.metrodatacollector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import project.metrodatacollector.CommonThings.CommonFunctions;

import project.metrodatacollector.Uploader.BatteryBroadcastHandler;
import project.metrodatacollector.Uploader.FileUploaderService;


public class MainActivity extends Activity implements OnClickListener,ConnectionCallbacks, OnConnectionFailedListener {
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "MainActivity";

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;
    private SignInButton btnSignIn;
    private Button btnSignOut,btnOnTime,btnOffTime,btnEntryStn,btnExitStn,btnStart;
    private ImageView imgProfilePic,imgAppName,imgAppIcon;
    private TextView txtName, txtFileUpload;
    //private TextView txtEmail,txtDatacollected;
    private static TextView txtDatasent;
    private TextView txtDataTarget;
    private LinearLayout llProfileLayout;
    Context context;

    File logFile;
    BufferedWriter timeBuf;
    boolean isBufferWriterOpen = false;


    SharedPreferences sharedPref;
    private int notifyID = 1555;
    NotificationManager mNotificationManager;
    private ProgressDialog ringProgressDialog;
    BatteryBroadcastHandler b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gplus_signin);
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnStart = (Button) findViewById(R.id.btn_launch_app);
        btnOnTime = (Button) findViewById(R.id.btn_note_on_time);
        btnOffTime = (Button) findViewById(R.id.btn_note_off_time);
        btnEntryStn = (Button) findViewById(R.id.btn_entry_station);
        btnExitStn = (Button) findViewById(R.id.btn_exit_station);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);

        txtName = (TextView) findViewById(R.id.txtName);
        txtFileUpload = (TextView) findViewById(R.id.txt_file_upload);
        txtDatasent = (TextView) findViewById(R.id.txtDatasent);


        imgAppName =(ImageView) findViewById(R.id.imgappname);
        imgAppIcon = (ImageView) findViewById(R.id.imgicon);
        context = getApplicationContext();

        // Button click listeners
        btnStart.setOnClickListener(this);
        btnEntryStn.setOnClickListener(this);
        btnExitStn.setOnClickListener(this);
        btnOnTime.setOnClickListener(this);
        btnOffTime.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);


        sharedPref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //opening file to record time of entry and exit
        logFile = new File(CommonUtils.getFilepath("TimeLog.txt"));
        try {
            timeBuf = new BufferedWriter(new FileWriter(logFile, true));
            Log.i("File opened:","Preparing to write data");
            isBufferWriterOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG,"OnPause called of GooglePlusActivity");

//        if (sharedPref.getString("USERID", "").isEmpty()){
//            killnotification();
//        }
//        else{
//            killnotification();
//            startnotification();}
    }


    @Override
    public void onResume() {
        super.onResume();
        //launchStartDialog();
//        Log.d(TAG,"On Resume called of GooglePlusActivity");
//        if(internetConnectionCheck() && !sharedPref.getString("USERID", "").isEmpty()){
//            killnotification();
//            startnotification();
//        }
//
//        else if (sharedPref.getString("USERID", "").isEmpty()){
//            killnotification();
//        }
    }

    private boolean internetConnectionCheck(){
        if(CommonUtils.isInternetAvailable(context)<0){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("File Upload Failed");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("No internet connection. Please switch on your Wi-Fi or Mobile Data for Uploading Data");
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog
                            //Toast.makeText(getApplicationContext(),"You clicked on YES", Toast.LENGTH_SHORT).show();
                        }
                    });
            alertDialog.show();
            return false;
        }
        else{
            return true;
        }
    }
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG,"disconnected");
            mGoogleApiClient.disconnect();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        //mGoogleApiClient.disconnect();
        if(isBufferWriterOpen) {
            try {
                timeBuf.close();
                Log.i(TAG, "BufferedWriter closed");
                isBufferWriterOpen = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("After sign in:","Im here");
        mSignInClicked = false;
        checkuser();
        // Get user's information
        getProfileInformation();
    }

    public void checkuser(){

        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        String personName = currentPerson.getDisplayName();

        try {

            long timeInMillis = System.currentTimeMillis();

            final String PhoneModel = android.os.Build.MODEL;
            JSONObject userData = new JSONObject();
            userData.put("phoneModel", PhoneModel);
            userData.put("username", personName);
            userData.put("email", email);
            Log.d(TAG, userData.toString());
            String response = CommonFunctions.sendRequestToServer(
                    userData.toString(), "login");
            if (response.contains("SUCCESS") || response.contains("ALREADY")) {
                Log.d("testing", response);
                String userid = response.subSequence(13, 17)
                        .toString();
                //Log.d("testing", userid);
                saveSharedString("USERID", userid);
                //initialsetup();
                //SensingController.unregisterAlarm(getApplicationContext());
            }

        }

        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void initialsetup(){

        SensingController.registerSensingAlarm(getApplicationContext());
        startnotification();

        //Collecting wifidata when it is connected for the 1st time
        //Thread sw = new WiFiScanning(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {

        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Log.i(TAG,"onConnectSuspended");
        updateUI(false);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            txtFileUpload.setVisibility(View.VISIBLE);
            txtDatasent.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);
            imgAppIcon.setVisibility(View.GONE);
            imgAppName.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
            btnOnTime.setVisibility(View.VISIBLE);
            btnOffTime.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnEntryStn.setVisibility(View.VISIBLE);
            btnExitStn.setVisibility(View.VISIBLE);
        } else {
            txtFileUpload.setVisibility(View.GONE);
            txtDatasent.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
            imgAppIcon.setVisibility(View.VISIBLE);
            imgAppName.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            btnOnTime.setVisibility(View.GONE);
            btnOffTime.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.GONE);
            btnEntryStn.setVisibility(View.GONE);
            btnExitStn.setVisibility(View.GONE);
        }
    }

    /**
     * Sign-in into google
     */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
            // launchLoginDialog();
            Log.i(TAG, "you just signed in using Google plus");
        }
    }

    /**
     * Sign-out from google
     */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            //mGoogleApiClient.connect();
            //updateUI(false);
            SensingController.unregisterAlarm(getApplicationContext());
        }
    }
    /**
     * Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     ** Fetching user's information name, email, profile pic
     **
     */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                Log.e(TAG, "Name: " + personName + ", plusProfile: " + personGooglePlusProfile + ", email: " + email + ", Image: " + personPhotoUrl);

                Toast.makeText(getApplicationContext(), "Signed In as:"+currentPerson.getDisplayName(), Toast.LENGTH_LONG).show();
                if(!sharedPref.contains("UID")){
                    int Applicationid = getUID();
                    saveSharedint("UID", Applicationid);}

                long DS = getDataValue(context);
                long DC = getFolderSize() + DS;
                String DataS = l2bytes(DS,false);
                String DataC = l2bytes(DC, false);

                long hours=CommonUtils.totaltime/3600;
                long minutes=(CommonUtils.totaltime%3600)/60;
                long seconds= (long) ((CommonUtils.totaltime%3600)%60);

                txtDatasent.setText("Data Sent :" + DataS);
                txtDatasent.setAnimation(null);

            } else {
                Toast.makeText(getApplicationContext(), "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.layout.about:
                Log.i(TAG, "you pressed about");
                Intent intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchStartDialog() {

        if(ringProgressDialog != null && ringProgressDialog.isShowing()){
            ringProgressDialog.dismiss ( ) ;
        }

        ringProgressDialog = ProgressDialog.show(this, "Trying to connect ...",	"Checking for session", true);
        ringProgressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4555);
                } catch (Exception e) {
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }



    public void launchLoginDialog() {

        if(ringProgressDialog != null && ringProgressDialog.isShowing()){
            ringProgressDialog.dismiss ( ) ;
        }
        ringProgressDialog = ProgressDialog.show(this, "Please wait ...",	"Connecting to Server", true);
        ringProgressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2555);
                } catch (Exception e) {
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    public void startnotification(){

        int pid = android.os.Process.myPid();
        Intent notiintent = new Intent(this, MainActivity.class);

        notiintent.setAction(Long.toString(System.currentTimeMillis()));
        int requestID = (int) System.currentTimeMillis();
        PendingIntent pIntent = PendingIntent.getActivity(this, requestID, notiintent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Sets an ID for the notification, so it can be updated
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("MetroSense")
                .setContentIntent(pIntent)
                .setOngoing(true);

        mNotifyBuilder.setSmallIcon(R.drawable.imgicon);

        //Log.d(TAG,"PID = " +  android.os.Process.myPid() + " " + String.valueOf(pid));

        mNotifyBuilder.setContentText("Collecting Data");

        Notification temp = mNotifyBuilder.build();
        temp.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(notifyID,temp);
    }

    public void killnotification(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public int getUID(){
        int UID = 0;

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(
                PackageManager.GET_META_DATA);

        //loop through the list of installed packages and see if the selected
        //app is in the list
        for (ApplicationInfo packageInfo : packages) {
            System.out.println(packageInfo.uid);
            if(packageInfo.packageName.equals("project.metrodatacollector")){
                //get the UID for the selected app	             
                UID = packageInfo.uid;
                break; //found a match, don't need to search anymore
            }
        }
        return UID;
    }


    public void launchRingDialog() {

        final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Exiting...", true);
        ringProgressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SensingController.unregisterAlarm(context.getApplicationContext());
                    //b.unregisterChargerPlugIn(context.getApplicationContext());
                    killnotification();
                    disableBatteryBroadcastReceiver();
                    //disableBroadcastReceiver();
                    //Intent stopar = new Intent(getApplicationContext(),ActivityRecognitionIntentService.class);
                    //stopService(stopar);
                    Intent stopdatacol = new Intent(getApplicationContext(),SensorService.class);
                    stopService(stopdatacol);
                    Intent stopLocLog = new Intent(getApplicationContext(),LocationService.class);
                    stopService(stopLocLog);

                    //BatteryBroadcastHandler b = new BatteryBroadcastHandler();
                    //b.unregisterChargerPlugIn(getApplicationContext());
                    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    Editor editor = sharedPrefs.edit();
                    editor.clear();
                    editor.commit();
                    Thread.sleep(3555);
                } catch (Exception e) {
                }

                ringProgressDialog.dismiss();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                Log.d("logout", "Called stopping services");
                // clear top clears all activities in stack - only if we have
                // not cleared explicitly
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(i);
                finish();
            }
        }).start();
    }

    private void saveSharedString(String key,String value){
        Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void exitApp()
    {
        updateUI(false);
        try
        {
            if(isBufferWriterOpen==true)
            {
                timeBuf.close();
            }
            /*SensingController.unregisterAlarm(context.getApplicationContext());
            killnotification();
            disableBatteryBroadcastReceiver();
            Intent stopdatacol = new Intent(getApplicationContext(),SensorService.class);
            stopService(stopdatacol);*/

        }catch(IOException ie) {
            ie.printStackTrace();
        }
    }

    public boolean requestlogout(){
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        String response = null;
        try {

            JSONObject userData = new JSONObject();
            userData.put("email", email);
            Log.d(TAG, userData.toString());
            response = CommonFunctions.sendRequestToServer(
                    userData.toString(), "logout");
            if (response.contains("DONE")) {
                return true;
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void launchApp()
    {
        updateUI(true);
        initialsetup();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.btn_launch_app:
                launchApp();
                break;

            case R.id.btn_sign_in:
                //Force File Upload
                if (internetConnectionCheck()) {
                    Log.i(TAG, "sign in clicked to upload files");
                    signInWithGplus();
                    txtDatasent.setText("Signed In!");

                    String FILE_UPL = "fileupload";
                    Editor editor = sharedPref.edit();
                    editor.putBoolean(FILE_UPL, true);
                    editor.commit();

                    try {
                        timeBuf.close();
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }

                    CommonFunctions.CompressandSend();
                    SensingController.unregisterAlarm(context.getApplicationContext());
                    Log.i("File Upload Option:", "Zip Folder created!");

                    final Runnable runnable = new Runnable() {
                        public void run() {
                            // TODO Auto-generated method stub
                            {
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                Log.d(TAG, "Going to start File upload service");

                                String FILE_UPL = "fileupload";
                                Editor editor = sharedPref.edit();
                                editor.putBoolean(FILE_UPL, true);
                                editor.commit();
                                CommonFunctions.CompressandSend();
                                startFileUpload();
                            }
                        }
                    };

                    new Thread(runnable).start();
                    txtDatasent.setText("Uploading Data");
                    txtDatasent.setAnimation(getBlinkAnimation());
                    boolean valUpload = sharedPref.getBoolean("FILE_UPL", false);
                    //txtDatasent.setText("Data Uploaded!");

                    String text = txtDatasent.getText().toString();
                    if (!valUpload && text.startsWith("Data Sent:")) {
                        if(requestlogout()) {
                            signOutFromGplus();
                            Toast.makeText(getApplicationContext(),"Logged out!",Toast.LENGTH_LONG).show();
                        }
                        //launchRingDialog();
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(),"Network Not available...",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btn_sign_out:
                // Signout button clicked
                exitApp();
                launchRingDialog();
                break;

            /*case R.id.btn_check_updates:
                // Checking for updates
                try{
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse("https://play.google.com/store/apps/details?id=sense.routinenew"));
                    startActivity(viewIntent);
                }

                catch(Exception e) {
                    Toast.makeText(getApplicationContext(),"Unable to Connect Try Again...",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                break;
             */

            case R.id.btn_note_on_time:
                long timeInMillis = System.currentTimeMillis();
                String newMsg = "Entry Time:" + CommonUtils.unixTimestampToString(timeInMillis);
                Toast.makeText(getApplicationContext(),"Entry time to be recorded!",
                        Toast.LENGTH_LONG).show();
                saveDataInFile(newMsg);


                break;

            case R.id.btn_note_off_time:
                long etimeInMillis = System.currentTimeMillis();
                String enewMsg = "Exit Time:" + CommonUtils.unixTimestampToString(etimeInMillis);
                Toast.makeText(getApplicationContext(),"Exit time to be recorded!",
                        Toast.LENGTH_LONG).show();
                saveDataInFile(enewMsg);

                break;

            case R.id.btn_entry_station:
                Intent entryIntent = new Intent(this, StationsList.class);
                entryIntent.putExtra("Travel Type", false);
                startActivity(entryIntent);

                break;

            case R.id.btn_exit_station:
                Intent exitIntent = new Intent(this, StationsList.class);
                exitIntent.putExtra("Travel Type", true);
                startActivity(exitIntent);

                break;
        }
    }

    public static long getDataValue(Context context){
        SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(context);
        int UID = sharedPref.getInt("UID", 0);
        return(TrafficStats.getUidTxBytes(UID));
    }

    public static long getFolderSize() {
        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/MetroSenApp/");
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                // System.out.println(file.getName() + " " + file.length());
                size += file.length();
            }
        }
        return size;
    }

    public static String l2bytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void saveSharedint(String key, int value) {
        // TODO Auto-generated method stub
        Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /*public void enableBroadcastReceiver(){

        ComponentName receiver1 = new ComponentName(getApplicationContext(), WiFiBroadcastListener.class);

        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver1,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void disableBroadcastReceiver(){

        ComponentName receiver1 = new ComponentName(getApplicationContext(), WiFiBroadcastListener.class);

        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver1,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }*/

    public void disableBatteryBroadcastReceiver(){

        ComponentName receiver1 = new ComponentName(getApplicationContext(),project.metrodatacollector.Uploader.BatteryBroadcastHandler.class);

        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver1,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }


    private void startFileUpload() {
        Intent intent = new Intent(context, FileUploaderService.class);
        startService(intent);
    }

    public Animation getBlinkAnimation(){
        Animation animation = new AlphaAnimation(1, 0);         // Change alpha from fully visible to invisible
        animation.setDuration(600);                             // duration - half a second
        animation.setInterpolator(new LinearInterpolator());    // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE);                            // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);             // Reverse animation at the end so the button will fade back in

        return animation;
    }


    public void saveDataInFile(String line){
        Log.i(TAG, "line " + line);
        try{
            Log.i("Data Saved in:",logFile.getAbsolutePath());
            timeBuf.append(line);
            timeBuf.newLine();

        }
        catch (IOException e){
            Log.e(TAG, e.getLocalizedMessage());

        }
    }
}
