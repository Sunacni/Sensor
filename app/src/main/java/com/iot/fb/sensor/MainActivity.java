package com.iot.fb.sensor;

import android.content.Context;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.View;
import android.provider.Settings.Secure;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    public TextView view;//label under butto
    public TextView view2;//label to display system ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (TextView) findViewById(R.id.textView2);
        view2 = (TextView) findViewById(R.id.textView3);
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String android_id = tm.getDeviceId();
        view2.setText(android_id);
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

    //methode executed onClick button
    public void startLoad(View v) {
        float cpuUsage = readUsage();
        String s = Float.toString(cpuUsage);
        view.setText(s);

        new Thread(new Runnable() {
            @Override
            public void run() {
                insert();
            }
        }).start();
    }

    protected void insert(){
        try{
            Class.forName("con.mysql.jdbc.Driver");
            Connection c = DriverManager.getConnection("jdbc:mzsql://h17zdb.hcc.uni-magdeburg.de", "STUDENT04", "initial");
            PreparedStatement st = c.prepareStatement("insert into student values (?,?,?)");
            int time = (int) (System.currentTimeMillis());
            Timestamp tsTemp = new Timestamp(time);
            st.setTimestamp(1, tsTemp);//TIMESTAMP YYYY-MM-DD HH:SS.FF3
            st.setFloat(2, 33);//VALUE
            st.setInt(3, 99);//SYSTEM_ID
            st.execute();
            st.close();
            c.close();

        }
        catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }

    private float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

}
