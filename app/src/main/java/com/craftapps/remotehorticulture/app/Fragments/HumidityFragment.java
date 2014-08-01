package com.craftapps.remotehorticulture.app.Fragments;


import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HumidityFragment extends Fragment {

    private TextView textViewLatestHumid;
    private TextView textViewLatestDate;
    private TextView textViewMinHumid;
    private TextView textViewMaxHumid;
    private VerticalSeekBar seekBarCurrentHumid;
    private WebView webViewHumid;

    final List<Double> parseSeries = new ArrayList<Double>();
    private ProgressDialog progressDialog;
    private Number currentHumid;
    private Number minHumid;
    private Number maxHumid;
    private String currentHumidDate;

    public HumidityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_humidity, container, false);

        initializeUIElements(rootView);

        parseQuery();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.main, menu);

        MenuItem mTemperatureMenuItem = menu.findItem(R.id.action_temperature);
        MenuItem mHumidityMenuItem = menu.findItem(R.id.action_humidity);
        MenuItem mLightingMenuItem = menu.findItem(R.id.action_lighting);
        MenuItem mWaterMenuItem = menu.findItem(R.id.action_water);

        if (mTemperatureMenuItem != null) mTemperatureMenuItem.setVisible(false);
        if (mHumidityMenuItem != null) mHumidityMenuItem.setVisible(true);
        if (mLightingMenuItem != null) mLightingMenuItem.setVisible(false);
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_humidity:
                Toast.makeText(getActivity(), "Humidity action.", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_refresh:
                refreshFragment();
                Toast.makeText(getActivity(), "Refreshed...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeUIElements(View view){
        seekBarCurrentHumid = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestHumid = (TextView) (view != null ? view.findViewById(R.id.textView_latestHumid) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestHumidDate) : null);
        textViewMinHumid = (TextView) (view != null ? view.findViewById(R.id.textView_minHumid) : null);
        textViewMaxHumid = (TextView) (view != null ? view.findViewById(R.id.textView_maxHumid) : null);
        webViewHumid = (WebView) (view != null ? view.findViewById(R.id.webView) : null);
    }

    private void setGlobalValues(List<ParseObject> humidList) {
        currentHumid = humidList.get(0).getNumber("humidity");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentHumidDate = formatter.format(humidList.get(0).getCreatedAt());

        double high = humidList.get(0).getNumber("humidity").doubleValue();
        double low = humidList.get(0).getNumber("humidity").doubleValue();
        for(ParseObject object : humidList){
            Date now = new Date();
            long yesterday = now.getTime() - 86400000;
            Date createdAt = object.getCreatedAt();
            if(createdAt.getTime() >= yesterday) {
                double temp = object.getNumber("humidity").doubleValue();
                if(temp > high)
                    high = temp;
                if(temp < low)
                    low = temp;
            }
        }

        /*for (ParseObject humid : humidList) {
            Log.i("query", "= " + humid.getNumber("humidity"));
            parseSeries.add(humid.getDouble("humidity"));
        }*/

        maxHumid = high;
        minHumid = low;

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        webViewHumid.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webViewHumid.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        loadChart();

        seekBarCurrentHumid.setProgress(currentHumid.intValue());
        seekBarCurrentHumid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(currentHumid.intValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentHumid.intValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentHumid.intValue());
            }
        });

        textViewLatestHumid.setText(currentHumid.toString() + "%");
        textViewLatestDate.setText(currentHumidDate);


        Log.i("minHumid", "= " + minHumid);
        Log.i("maxHumid", "= " + maxHumid);

        textViewMinHumid.setText(minHumid + "%");
        textViewMaxHumid.setText(maxHumid + "%");
    }

    private void loadChart() {
        String content = null;
        try {
            AssetManager assetManager = getActivity().getAssets();
            InputStream in = assetManager.open("humidity.html");
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (IOException e){
            Log.e("loadChart", "An error occurred.", e);
        }
        webViewHumid.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    private void preParseQuery() {
        textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestHumid.setVisibility(View.INVISIBLE);
        textViewMaxHumid.setVisibility(View.INVISIBLE);
        textViewMinHumid.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonitorData");
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> humidList, ParseException e) {
                if (e == null) {
                    setGlobalValues(humidList);
                    postParseQuery();
                } else {
                    Log.i("error", ": findInBackground");
                }
            }
        });
    }

    private void postParseQuery() {
        textViewLatestDate.setVisibility(View.VISIBLE);
        textViewLatestHumid.setVisibility(View.VISIBLE);
        textViewMaxHumid.setVisibility(View.VISIBLE);
        textViewMinHumid.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void refreshFragment() {
        Fragment newFragment = new HumidityFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
