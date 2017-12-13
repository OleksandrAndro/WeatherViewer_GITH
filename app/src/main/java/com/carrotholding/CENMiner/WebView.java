package com.carrotholding.CENMiner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.chromium.base.Log;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkGetBitmapCallback;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class WebView extends XWalkActivity {

    XWalkView mXWalkView;   //initialization XWalkView
    public static final String TAG = "XWalkViewCallbacks";
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    ImageButton imageButton;
    TextView textView;
    LinearLayout linearWeb;
    LinearLayout linearShow;
    Window window;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        window.requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_web_view);


        //InitializationWebView();
        /*myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                activity.setTitle("Loading...");
                activity.setProgress(progress * 100);


                if(progress == 100)
                    activity.setTitle(R.string.app_name);
            }
        });*/
//setMyWebView("http://statistics.carrot.network/carrot-miner-58-dashboard.html");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }

    public void initXWalkView() {
        //mXWalkView = new XWalkView(this)

        mXWalkView = (XWalkView) findViewById(R.id.xwalkview);
        //Ll.addView(mXWalkView);

        MyUIClient myUIClient = new MyUIClient(mXWalkView);
        mXWalkView.setUIClient(myUIClient);

        //mXWalkView.load("http://statistics.carrot.network/carrot-miner-58-dashboard.html", null);
        initializationWebView();
    }

    @Override
    protected void onXWalkReady() {
        initXWalkView();
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        imageButton = (ImageButton) findViewById(R.id.refreshButton);
        textView = (TextView) findViewById(R.id.textView2);
        linearWeb = (LinearLayout) findViewById(R.id.linearWeb);
        linearShow = (LinearLayout) findViewById(R.id.linerShow);

    }

    private void initializationWebView (){
        Intent intent = getIntent();

        String URL = intent.getStringExtra("URL");
        mXWalkView.load(URL, null);
        /*myUIClient.onPageLoadStarted(mXWalkView, );
        myUIClient.onPageLoadStopped(mXWalkView,URL, XWalkUIClient.LoadStatus.FINISHED);*/
    }

    class MyUIClient extends XWalkUIClient {

        MyUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, java.lang.String url) {
            Log.w(TAG, "onPageLoadStarted: " + url);
            imageButton.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            //linearShow.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.VISIBLE);
            /*progressDialog.show();
            progressDialog.setCancelable(false);*/
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            Log.w(TAG, "onPageLoadStopped: " + url + ", status: " + status);

            if (status == LoadStatus.FINISHED) {
                view.captureBitmapAsync(new XWalkGetBitmapCallback() {
                    @Override
                    public void onFinishGetBitmap(Bitmap bitmap, int i) {
                        Log.w(TAG, "onFinishGetBitmap: " + bitmap);
                        textView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        imageButton.setVisibility(View.VISIBLE);
                        //linearShow.setVisibility(View.INVISIBLE);
                        //progressBar.setVisibility(View.INVISIBLE);

                    }
                });
            }
        }
    }

    private void setMyWebView (String URL){
        mXWalkView.load(URL, null);
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.refreshButton:

                mXWalkView.reload(XWalkView.RELOAD_NORMAL);
                break;

        }


    }

}
