package com.craftapps.remotehorticulture.app.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {

    private ProgressDialog progressDialog;
    final List<Double> parseSeries = new ArrayList<Double>();

    public OverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

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
        if (mHumidityMenuItem != null) mHumidityMenuItem.setVisible(false);
        if (mLightingMenuItem != null) mLightingMenuItem.setVisible(false);
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
                refreshFragment();
                Toast.makeText(getActivity(), "Refreshed...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeUIElements(View view){
        /*seekBarCurrentTemp = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestTemp = (TextView) (view != null ? view.findViewById(R.id.textView_latestTemp) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestTempDate) : null);
        textViewMinTemp = (TextView) (view != null ? view.findViewById(R.id.textView_minTemp) : null);
        textViewMaxTemp = (TextView) (view != null ? view.findViewById(R.id.textView_maxTemp) : null);
        webViewTemp = (WebView) (view != null ? view.findViewById(R.id.webView) : null);*/
    }

    private void initializeDialogUIElements(View view) {
        /*seekBarDialogMin = (VerticalSeekBar) view.findViewById(R.id.seekBar_min);
        seekBarDialogMax = (VerticalSeekBar) view.findViewById(R.id.seekBar_max);
        textViewDialogCurrentTemp = (TextView) view.findViewById(R.id.textViewCurrent);
        editTextDialogMinTemp = (EditText) view.findViewById(R.id.editTextMin);
        editTextDialogMaxTemp = (EditText) view.findViewById(R.id.editTextMax);*/
    }

    private void setGlobalValues(List<ParseObject> monitorDataList, List<ParseObject> automationControlList) {
        /*currentTemp = monitorDataList.get(0).getNumber("fahrenheit");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentTempDate = formatter.format(monitorDataList.get(0).getUpdatedAt());
        minTemp = automationControlList.get(0).getNumber("TempMin");
        maxTemp = automationControlList.get(0).getNumber("TempMax");

        for (ParseObject temp : monitorDataList) {
            Log.i("query", "= " + temp.getNumber("fahrenheit"));
            parseSeries.add(temp.getDouble("fahrenheit"));
        }

        applyValuesToUI();*/
    }

    private void applyValuesToUI() {
        /*webViewTemp.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webViewTemp.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        loadChart();

        seekBarCurrentTemp.setProgress(currentTemp.intValue());
        seekBarCurrentTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(currentTemp.intValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentTemp.intValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentTemp.intValue());
            }
        });

        textViewLatestTemp.setText(currentTemp.toString() + "° F");
        textViewLatestDate.setText(currentTempDate);

        textViewMinTemp.setText(minTemp + "° F");
        textViewMaxTemp.setText(maxTemp + "° F");*/
    }

    private void preParseQuery() {
        /*textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestTemp.setVisibility(View.INVISIBLE);
        textViewMaxTemp.setVisibility(View.INVISIBLE);
        textViewMinTemp.setVisibility(View.INVISIBLE);*/

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonitorData");
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> monitorDataList, ParseException e) {
                if (e == null) {
                    ParseQuery<ParseObject> automationControlQuery = ParseQuery.getQuery("AutomationControl");
                    automationControlQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> automationControlList, ParseException e) {
                            if (e == null) {
                                setGlobalValues(monitorDataList, automationControlList);
                                postParseQuery();
                            }
                        }
                    });
                } else {
                    Log.i("error", ": findInBackground");
                }
            }
        });
    }

    private void postParseQuery() {
        /*textViewLatestDate.setVisibility(View.VISIBLE);
        textViewLatestTemp.setVisibility(View.VISIBLE);
        textViewMaxTemp.setVisibility(View.VISIBLE);
        textViewMinTemp.setVisibility(View.VISIBLE);*/
        progressDialog.dismiss();
    }

    private void refreshFragment(){
        Fragment newFragment = new OverviewFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
