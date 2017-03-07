package com.example.vamsikrishnag.mcassignment1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
    ProgressDialog progressDialogObject = null;
    SSLContext context = null;
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
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String appName=Constants.uploadFileName;
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
        values.setColor(Color.parseColor(Constants.redCC));
        secondValues = new LineGraphSeries<DataPoint>();
        secondValues.setColor(Color.parseColor(Constants.greenCC));
        thirdValues = new LineGraphSeries<DataPoint>();
        thirdValues.setColor(Color.parseColor(Constants.blueCC));
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
        this.uploadListener();
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
                row = Float.toString(timeStamp) + "," + Float.toString(x) + "," + Float.toString(y) + "," + Float.toString(z);
                //Log.d("Row: ", Float.toString(fl) + " " + row);
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

    public void uploadListener(){
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
            Button uploadButton =    (Button) findViewById(R.id.upload_button);
            final Upload uploadAsync = new Upload(BeatDisplay.this);
            uploadButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    progressDialogObject = ProgressDialog.show(BeatDisplay.this,"","Uploading Database",true);
                    //Toast.makeText(BeatDisplay.this,"File upload started",Toast.LENGTH_LONG).show();
                    //addCertificate();
                    //uploadToServer();
                    uploadAsync.execute();
                    progressDialogObject.dismiss();
                }
            });
        }

    }

    public void addCertificate(){
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        InputStream caInput = null;
        try {

            Log.d("getFilesDir()",getFilesDir().getAbsolutePath());
            AssetManager assetManagerObject = getAssets();
            caInput = new BufferedInputStream(assetManagerObject.open("impactasuedu.crt"));
        } catch(IOException e){
            e.printStackTrace();
        }
        Certificate ca = null;
        try {
            ca = cf.generateCertificate(caInput);
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            try {
                caInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        try {
            keyStore.setCertificateEntry("ca", ca);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            tmf.init(keyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // Create an SSLContext that uses our TrustManager

        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            context.init(null, tmf.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }

    class Upload extends AsyncTask{
        private Context context1;
        public Upload(Context context) {
            this.context1 = context;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection conn = null;
            int serverResponseCode = 0;
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            }};

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                //URL url = new URL(sUrl[0]);

                DataOutputStream dataOutputStreamObject = null;
                String lineEnd = "\r\n";
                String hyphens = "--";
                String boundaryMarker = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(Constants.uploadFilePath + "" + Constants.uploadFileName);
                if (!sourceFile.isFile()) {
                    //dialog.dismiss();
                    Log.e("uploadFile", "Source File not exist :"
                            + Constants.uploadFilePath + "" + Constants.uploadFileName);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Source File not exist :" + Constants.uploadFilePath + "" + Constants.uploadFileName, Toast.LENGTH_LONG).show();
                        }
                    });
                    return "Source File not exist";
                } else {
                    try {
                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(Constants.uploadURIinServer);
                        conn = (HttpsURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundaryMarker);
                        conn.setRequestProperty("uploaded_file", Constants.uploadFilePath + "" + Constants.uploadFileName);
                        dataOutputStreamObject = new DataOutputStream(conn.getOutputStream());

                        dataOutputStreamObject.writeBytes(hyphens + boundaryMarker + lineEnd);
                        dataOutputStreamObject.writeBytes("Content-Disposition: form-data; name=" + "uploaded_file;filename="
                                + Constants.uploadFilePath + "" + Constants.uploadFileName + "" + lineEnd);

                        dataOutputStreamObject.writeBytes(lineEnd);

                        // create a buffer of  maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            dataOutputStreamObject.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        dataOutputStreamObject.writeBytes(lineEnd);
                        dataOutputStreamObject.writeBytes(hyphens + boundaryMarker + hyphens + lineEnd);
                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();
                        conn.connect();
                        serverResponseCode = conn.getResponseCode();
                        if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                            return "Server returned HTTP " + conn.getResponseCode()
                                    + " " + conn.getResponseMessage();
                        }
                        Log.i("uploadFile", "HTTP Response is : "
                                + serverResponseMessage + ": " + serverResponseCode);
                        if (serverResponseCode == HttpsURLConnection.HTTP_OK) {
                            runOnUiThread(new Runnable() {
                                              public void run() {
                                                  Toast.makeText(getApplicationContext(),
                                                          "File Upload Completed."
                                                                  + Constants.uploadFileName, Toast.LENGTH_LONG).show();
                                              }
                                          }
                            );
                        }

                        //close the streams //
                        fileInputStream.close();
                        dataOutputStreamObject.flush();
                        dataOutputStreamObject.close();
                    } catch (MalformedURLException ex) {
                        //dialog.dismiss();
                        ex.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "MalformedURLException Exception : check script url.", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    } catch (Exception e) {
                        //dialog.dismiss();
                        e.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Got Exception : see logcat", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("Upload file to server", "Exception : "
                                + e.getMessage(), e);
                    }
                    return ("String response code:" + serverResponseCode);

                }
            } catch (Exception ex) {
                Log.d("Exception",""+ex.toString());
            }
            return "success";
        }
        }
    }

