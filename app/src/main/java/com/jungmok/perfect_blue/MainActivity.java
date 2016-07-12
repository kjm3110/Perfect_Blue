package com.jungmok.perfect_blue;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button bConnect, bDisconnect, bXmius, bXplus;
    ToggleButton tbLock, tbScroll, tbStream;
    static boolean Lock, AutoScrollX, Stream;

    // member variables
    private LinearLayout mMainLinearLayout;
    private GraphView mRightGraphView;
    private GraphViewSeries mSeries;
    private static double graph2LastXValue = 0;
    private static int xView = 10;





    @Override
    public void onBackPressed() {
        if (Mybluetooth.connectedThread != null)
            Mybluetooth.connectedThread.write("Q");
        super.onBackPressed();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Mybluetooth.SUCCESS_CONNECT:
                    Mybluetooth.connectedThread = new Mybluetooth.ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    String s = "Successfully connected";
                    Mybluetooth.connectedThread.start();
                    break;
                case Mybluetooth.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, 5);

                    if (strIncom.indexOf('s') == 0 && strIncom.indexOf('.') == 2) {
                        strIncom = strIncom.replace("s", "");
                        if (isFloatNumber(strIncom)) {
                            mSeries.appendData(new GraphView.GraphViewData(graph2LastXValue, Double.parseDouble(strIncom)), AutoScrollX);

                            if (graph2LastXValue >= xView && Lock == true) {
                                mSeries.resetData(new GraphView.GraphViewData[]{});
                                graph2LastXValue = 0;
                            } else {
                                graph2LastXValue += 0.1;
                            }
                            if (Lock == true) mRightGraphView.setViewPort(0, xView);
                            else mRightGraphView.setViewPort(graph2LastXValue - xView, xView);

                            //refresh
                            mMainLinearLayout.removeView(mRightGraphView);
                            mMainLinearLayout.addView(mRightGraphView);
                        }
                    }
                    break;

            }
        }

        public boolean isFloatNumber(String num) {
            try {
                Double.parseDouble(num);

            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    };



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mMainLinearLayout = (LinearLayout)findViewById(R.id.bg);
        mSeries = new GraphViewSeries("Signal",
                new GraphViewSeries.GraphViewStyle(Color.YELLOW, 2),
                new GraphView.GraphViewData[]{new GraphView.GraphViewData(0, 0)});

        mMainLinearLayout.setBackgroundColor(Color.WHITE);

        initRightGraphView("rightGraph", mSeries);
        mMainLinearLayout.addView(mRightGraphView);

        Buttoninit();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    protected void initRightGraphView(String graphTitle, GraphViewSeries series)
    {
        mRightGraphView = (LineGraphView)new LineGraphView(this, graphTitle);
        mRightGraphView.setViewPort(0, xView);
        mRightGraphView.setScrollable(true);
        mRightGraphView.setScalable(true);
        mRightGraphView.setShowLegend(true);
        mRightGraphView.setLegendAlign(GraphView.LegendAlign.BOTTOM);
        mRightGraphView.setManualYAxis(true);
        mRightGraphView.setManualYAxisBounds(5, 0);

        mRightGraphView.addSeries(series);
    }

    void Buttoninit() {
        bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnect = (Button) findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);
        bXmius = (Button) findViewById(R.id.bXminus);
        bXmius.setOnClickListener(this);
        bXplus = (Button) findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);

        tbLock = (ToggleButton) findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton) findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton) findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);

        Lock = true;
        AutoScrollX = true;
        Stream = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bConnect:
                startActivity(new Intent("android.intent.action.BT"));
                break;
            case R.id.bDisconnect:
                Mybluetooth.disconnect();
                break;
            case R.id.bXminus:
                if (xView > 1) xView--;
                break;
            case R.id.bXplus:
                if (xView > 30) xView++;
                break;
            case R.id.tbLock:
                if (tbLock.isChecked()) {
                    Lock = true;
                } else {
                    Lock = false;
                }
                break;
            case R.id.tbScroll:
                if (tbScroll.isChecked()) {
                    AutoScrollX = true;
                } else {
                    AutoScrollX = false;
                }
                break;
            case R.id.tbStream:
                if (tbStream.isChecked()) {
                    if (Mybluetooth.connectedThread != null)
                        Mybluetooth.connectedThread.write("E");
                } else {
                    if (Mybluetooth.connectedThread != null)
                        Mybluetooth.connectedThread.write("Q");
                }
                break;
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jungmok.perfect_blue/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jungmok.perfect_blue/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
