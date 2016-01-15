package project.metrodatacollector;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Megha on 12/21/2015.
 */
public class StationsList extends Activity
{
    final String[] metroStations = {"Botanical Garden","Central Secretariat","Chandni Chowk","Mandi House","New Delhi",
            "Noida City Center","Rajiv Chowk","Other"};
    ListView lv;
    File logFile;
    BufferedWriter stationBuf;
    boolean isBufferWriterOpen = false;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stations_list);
        lv =  (ListView)findViewById(R.id.stnList);

        //file to record the important stations
        logFile = new File(CommonUtils.getFilepath("StationLog.txt"));
        try {
            stationBuf = new BufferedWriter(new FileWriter(logFile, true));
            Log.i("File opened:", "Preparing to write data");
            isBufferWriterOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter myList = new ArrayAdapter(this,android.R.layout.simple_selectable_list_item,android.R.id.text1, metroStations);

        lv. setAdapter(myList);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                boolean valueTravel = getIntent().getBooleanExtra("Travel Type",false);
                // TODO Auto-generated method stub
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) lv.getItemAtPosition(position);
                String text = null;
                if(valueTravel == false)
                {
                    text = "Entry from :"+itemValue;
                }
                else
                {
                    text = "Exit on :"+itemValue;
                }
                saveDataInFile(text);
            }
        });
    }

    public void saveDataInFile(String line){
        Log.i("Saving Station Data", "line " + line);
        try{
            Log.i("Data Saved in:",logFile.getAbsolutePath());
            stationBuf.append(line);
            stationBuf.newLine();
            stationBuf.close();
        }
        catch (IOException e){
            Log.e("Saving Station Data", e.getLocalizedMessage());

        }
    }
}
