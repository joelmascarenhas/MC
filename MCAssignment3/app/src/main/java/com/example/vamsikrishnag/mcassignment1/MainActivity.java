package com.example.vamsikrishnag.mcassignment1;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  {

    SQLiteDatabase dbCon;
    String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String name;
        final String age;
        final String id;
        final String gen;
        Button mButton = (Button) findViewById(R.id.start);


        mButton.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           EditText nameBox = (EditText) findViewById(R.id.inputname);
                                           EditText ageBox = (EditText) findViewById(R.id.inputage);
                                           EditText idBox = (EditText) findViewById(R.id.inputpatid);
                                           // gender = ((RadioButton)findViewById(R.id.male)).getCheckedRadioButtonId()

                                           final String name = nameBox.getText().toString();
                                           final String age = ageBox.getText().toString();
                                           final String id = idBox.getText().toString();
                                           if (name.equals("") || id.equals("") || age.equals("")) {
                                               Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                                               return;
                                           }
                                           String gen = "";
                                           RadioButton str = (RadioButton) findViewById(R.id.male);
                                           Boolean value = str.isChecked();
                                           if(value == true)
                                           {
                                               gen = "M";
                                           }
                                           else
                                           {
                                               gen = "F";
                                           }

                                           tableName=name+"_"+id+"_"+age+"_"+gen;
                                           Intent myIntent = new Intent(MainActivity.this, BeatDisplay.class);
                                           myIntent.putExtra("name", name);
                                           myIntent.putExtra("age", age);
                                           myIntent.putExtra("id", id);
                                           myIntent.putExtra("sex",gen);
                                           myIntent.putExtra("TableName",tableName);
                                           startActivity(myIntent);

                                       }
                                   }
        );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}