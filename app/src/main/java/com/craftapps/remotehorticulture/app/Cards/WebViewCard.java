package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.craftapps.remotehorticulture.app.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.gmariotti.cardslib.library.internal.Card;

public class WebViewCard extends Card {

    protected WebView mWebView;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public WebViewCard(Context context) {
        this(context, R.layout.card_webview);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public WebViewCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    /**
     * Init
     */
    private void init(){
        //No Header

        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                //Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mWebView = (WebView) parent.findViewById(R.id.webView);

    }

    private void loadChart(String url) {
        String content = null;
        try {
            AssetManager assetManager = getContext().getAssets();
            InputStream in = assetManager.open(url);
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (IOException e){
            Log.e("loadChart", "An error occurred.", e);
        }
        mWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    public void setWebView(String url) {
        if (url != null) {
            mWebView.setVerticalScrollBarEnabled(false);
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            loadChart(url);
        }
    }
}