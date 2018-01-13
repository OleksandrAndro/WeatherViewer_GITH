package com.carrotholding.CENMiner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.chromium.base.Log;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkGetBitmapCallback;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.util.Timer;
import java.util.TimerTask;

public class WebView extends XWalkActivity {

    XWalkView mXWalkView;                                    //initialization XWalkView
    public static final String TAG = "XWalkViewCallbacks";
    ProgressBar progressBar;
    ImageButton imageButton, home;
    TextView textView;
    LinearLayout linearWeb;
    LinearLayout linearShow;
    int loadUrlCount = 0;
    Bundle msavedInstanceState;
    //Timer timer;                                            //uncomment when need to used page loading timeout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //window = getWindow();
        //window.requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_web_view);
        home = (ImageButton) findViewById(R.id.home);

        msavedInstanceState = savedInstanceState;

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);   //its from example, simply Float Button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }

    public void initXWalkView() {

            //mXWalkView.load("http://statistics.carrot.network/carrot-miner-58-dashboard.html", null);
            Intent intent = getIntent();
            String URL = intent.getStringExtra("URL");            //getting URL from MainActivity via intent
            //Toast toast = Toast.makeText(getApplicationContext(), URL, Toast.LENGTH_LONG);  //it was for debuging
            //toast.show();
            mXWalkView.load(URL, null);
    }

    @Override
    protected void onXWalkReady() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        imageButton = (ImageButton) findViewById(R.id.refreshButton);
        textView = (TextView) findViewById(R.id.textView2);
        linearWeb = (LinearLayout) findViewById(R.id.linearWeb);
        linearShow = (LinearLayout) findViewById(R.id.linerShow);
        mXWalkView = (XWalkView) findViewById(R.id.xwalkview);

        MyUIClient myUIClient = new MyUIClient(mXWalkView);    //connecting UIClient to view
        mXWalkView.setUIClient(myUIClient);

        if (msavedInstanceState != null)
            mXWalkView.restoreState(msavedInstanceState);
        else
        initXWalkView();
    }

    class MyUIClient extends XWalkUIClient {             //client used for control start/stop/success/failed page loading

        MyUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, java.lang.String url) {
            Log.w(TAG, "onPageLoadStarted: " + url);
            imageButton.setVisibility(View.INVISIBLE);   //hide refresh button
            textView.setVisibility(View.VISIBLE);        //show progress text
            progressBar.setVisibility(View.VISIBLE);     //show progress Bar
            home.setVisibility(View.INVISIBLE);
            //timer = new Timer();                       //uncomment when nessesary to used page loading timeout
            //timer.schedule(new TimeoutLoad(), 40000);  //uncomment when nessesary to used page loading timeout
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, final LoadStatus status) {
            Log.w(TAG, "onPageLoadStopped: " + url + ", status: " + status);

            if (status == LoadStatus.FINISHED) {
                view.captureBitmapAsync(new XWalkGetBitmapCallback() {
                    @Override
                    public void onFinishGetBitmap(Bitmap bitmap, int i) {
                        Log.w(TAG, "onFinishGetBitmap: " + bitmap);
                        loadUrlCount = 0;                         //skip to zero reload count
                        //timer.cancel();                         //uncomment when nessesary to used page loading timeout
                        textView.setVisibility(View.INVISIBLE);   //hide progress text
                        progressBar.setVisibility(View.INVISIBLE); //hide progressBar
                        imageButton.setVisibility(View.VISIBLE);   //show refresh button
                        home.setVisibility(View.VISIBLE);
                    }
                });
            }

            if (status == LoadStatus.FAILED) {
                if (loadUrlCount < 3) {                 //if page can't loading for 3 times - skip loading and closing activity
                    mXWalkView.reload(XWalkView.RELOAD_IGNORE_CACHE);
                    loadUrlCount++;                     //increment reload count if failed
                } else {
                    Toast toast = Toast.makeText(WebView.this, R.string.webToastErrorURL, Toast.LENGTH_LONG);
                    toast.show();
                    WebView.this.finish();
                }
            }
        }
    }

    public void onClick (View v) {                       //handler for onClick

        switch (v.getId()) {

            case R.id.refreshButton:                     // handler for refresh button
                mXWalkView.reload(XWalkView.RELOAD_NORMAL);
                break;

            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mXWalkView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    private void ifFailure() {                           // it can used for add page loading timeout too
        if (loadUrlCount == 0) {
            mXWalkView.stopLoading();
            mXWalkView.reload(XWalkView.RELOAD_IGNORE_CACHE);
            loadUrlCount++;
        } else {
            Toast toast = Toast.makeText(WebView.this, R.string.webToastErrorURL, Toast.LENGTH_LONG);
            toast.show();
            WebView.this.finish();
        }
    }

    class TimeoutLoad extends TimerTask {              // it can used for add page loading timeout

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ifFailure();
                }
            });
        }
    }

}
