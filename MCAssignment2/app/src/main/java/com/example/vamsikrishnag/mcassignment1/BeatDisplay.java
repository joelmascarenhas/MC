package com.example.vamsikrishnag.mcassignment1;
//Dummy comment
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.Random;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Created by vamsikrishnag on 2/2/17.
 */
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BeatDisplay extends Activity {


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private SQLiteDatabase dbCon;
    private SensorManager AcclManager;// = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    private Sensor Accelerometer;// = AcclManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    private LineGraphSeries<DataPoint> values;
    private LineGraphSeries<DataPoint> secondValues;
    private LineGraphSeries<DataPoint> thirdValues;
    private int counter = 0;
    private int counter2 = 0;
    private int counter3 = 0;

    private Button runButton;
    private Button stopButton;
    private boolean stopIt = false;
    private Thread keyThread;
    private GraphView gv;
    private Viewport vp;
    private boolean firstStart = true;
    String tableName;
    boolean flag=true;

    long previousTime=0;
    private SensorEventListener acclListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent acclEvent) {
            //Log.d("Sensor changed","Sensor Changed");
            Sensor AcclSensor = acclEvent.sensor;

            if (AcclSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = acclEvent.values[0];
                float y = acclEvent.values[1];
                float z = acclEvent.values[2];
                long currentTime = System.currentTimeMillis();
                String msg=Long.toString(currentTime)+","+Float.toString(x)+","+Float.toString(y)+","+Float.toString(z);
                //Log.d("Accelerometer Data",msg);
                if ((currentTime-previousTime)>1000) {
                    //dbCon=openOrCreateDatabase("Heart Beat",MODE_PRIVATE,null);
                    try {
                        dbCon.execSQL("INSERT INTO " + tableName + " VALUES (" + msg + ");");
                    }
                    catch (Exception e)
                    {
                        Log.d(e.getMessage()," Insert part");
                    }
                    previousTime=currentTime;
                    //dbCon.close();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("Sensor Accuracy changed","Sensor Accuracy Changed");
        }
    };


    private float[] randList(int n){
        float retArr [] = new float[n];
        Random rand = new Random();
        for (int i=0;i<n;i++){
            retArr[i] = rand.nextFloat();
        }
        return retArr;
    }

    private void registerAcclListener()
    {
        AcclManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = AcclManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        AcclManager.registerListener(acclListener,Accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Registered Listener","Registered Listener");
        return;
    }

    private void createTable(String t_name,SQLiteDatabase connection)
    {
        Log.d(t_name,t_name);
        try {
            connection.execSQL("CREATE TABLE " + t_name + " (Time_Stamp REAL, X_Value REAL, Y_Value REAL, Z_Value REAL);");
            Log.d("Table Created ", t_name);
        }
        catch (Exception e)
        {
            Log.d("Table Already exists: ",t_name);
        }
        return;
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String appName="Assignment2_Group10";
        setContentView(R.layout.activity_beat_display);
        TextView txtview = (TextView) findViewById(R.id.name);
        TextView txtview1 = (TextView) findViewById(R.id.age);
        TextView txtview2 = (TextView) findViewById(R.id.patid);
        TextView txtview3 = (TextView) findViewById(R.id.gen);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null)
        {
            String getName = (String) bd.get("name");
            String getAge = (String) bd.get("age");
            String getId = (String) bd.get("id");
            String gen = (String) bd.get("sex");
            tableName=(String) bd.get("TableName");
            txtview.setText(getName);
            txtview1.setText(getAge);
            txtview2.setText(getId);
            txtview3.setText(gen);

        }

        dbCon=openOrCreateDatabase(appName,MODE_PRIVATE,null);
        registerAcclListener();
        createTable(tableName,dbCon);

        gv = (GraphView) findViewById(R.id.graph);
        values = new LineGraphSeries<DataPoint>();
        values.setColor(Color.parseColor("#ff0000"));
        secondValues = new LineGraphSeries<DataPoint>();
        secondValues.setColor(Color.parseColor("#0000ff"));
        thirdValues = new LineGraphSeries<DataPoint>();
        thirdValues.setColor(Color.parseColor("#00ff00"));
        vp = gv.getViewport();
        //vp.setYAxisBoundsManual(true);
        vp.setMinX(0);
        vp.setMaxX(10);
        vp.setMinY(-30);
        vp.setMaxY(30);
        vp.setXAxisBoundsManual(true);
        vp.setScrollable(true);
        vp.scrollToEnd();
        this.runListener();
        this.stopListener();
        keyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0;!stopIt;i++){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendValues();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        Log.d("Interrupted",e.toString());
                    }
                }
            }
        });

    }


    public void runListener(){
        runButton = (Button) findViewById(R.id.run_button);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopIt = false;
                if(gv.getSeries().isEmpty())
                {
                    gv.addSeries(values);
                    gv.addSeries(secondValues);
                    gv.addSeries(thirdValues);
                    keyThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i=0;!stopIt;i++){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        appendValues();
                                    }
                                });
                                try {
                                    Thread.sleep(1000);
                                }catch (InterruptedException e){
                                    Log.d("Interrupted",e.toString());
                                }
                                //flag=false;
                            }
                        }
                    });
                    keyThread.start();
                }else {
                    return;
                }
            }
        });
    }

    public void stopListener(){
        stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                stopIt = true;
                flag=true;
                gv.removeAllSeries();
            }
        });
    }

    private void appendValues(){
        Log.d("Table's name",tableName);
        float fl;
        float x,y,z;
        String selectQuery="SELECT * FROM " + tableName + " ORDER BY Time_Stamp DESC LIMIT 1;";
        if(flag)
        {
            selectQuery="SELECT * FROM " + tableName + " ORDER BY Time_Stamp DESC LIMIT 10;";
            flag=false;
        }
        //Cursor sel=dbCon.rawQuery("SELECT * FROM "+tableName,null);
        try {
            Cursor sel = dbCon.rawQuery(selectQuery, null);
            int c=0;
            sel.moveToFirst();
            do {
                int timeStamp = sel.getInt(0);
                x = sel.getFloat(1);
                y = sel.getFloat(2);
                z = sel.getFloat(3);
                fl = (x + y + z);
                c++;
                String row = Integer.toString(c)+","+Float.toString(timeStamp) + "," + Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z);
                Log.d("Row Val: ", Float.toString(fl) + " " + row);
                values.appendData(new DataPoint(counter++, x), true, 12);
                secondValues.appendData(new DataPoint(counter2++, y), true, 12);
                thirdValues.appendData(new DataPoint(counter3++, z), true, 12);
                //values.appendData(new DataPoint(counter++, x), true, 12);
            } while (sel.moveToNext());
            Log.d("C: ", Integer.toString(c));
            //float fl = new Random().nextFloat() * (10f);

        }
        catch (Exception e)
        {
            Log.d("Append Value"," DB object closed");
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        dbCon.close();
        Log.d("Unregistering sensor","unregistering sensor");
        try {
            AcclManager.unregisterListener(acclListener);
        }
        catch (Exception e){
            Log.d(e.getMessage(),"Sensor not registered");
        }
    }
}
