package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TemperatureFragment extends Fragment {

    private TextView textViewLatestTemp;
    private TextView textViewLatestDate;
    private TextView textViewMinTemp;
    private TextView textViewMaxTemp;
    private VerticalSeekBar seekBarCurrentTemp;

    private VerticalSeekBar seekBarDialogMin;
    private VerticalSeekBar seekBarDialogMax;
    private TextView textViewDialogCurrentTemp;
    private EditText editTextDialogMinTemp;
    private EditText editTextDialogMaxTemp;
    private WebView webViewTemp;

    final List<Double> parseSeries = new ArrayList<Double>();
    private ProgressDialog progressDialog;
    private Number currentTemp;
    private Number minTemp;
    private Number maxTemp;
    private Number lowTemp;
    private Number highTemp;
    private String currentTempDate;
    private String automationControlId;


    public TemperatureFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);

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
        MenuItem mRefreshMenuItem = menu.findItem(R.id.action_refresh);

        if (mTemperatureMenuItem != null) mTemperatureMenuItem.setVisible(true);
        if (mHumidityMenuItem != null) mHumidityMenuItem.setVisible(false);
        if (mLightingMenuItem != null) mLightingMenuItem.setVisible(false);
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(false);
        if (mRefreshMenuItem != null) mRefreshMenuItem.setVisible(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_temperature:
                startTemperatureDialog();
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
        seekBarCurrentTemp = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestTemp = (TextView) (view != null ? view.findViewById(R.id.textView_latestTemp) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textViewDate) : null);
        textViewMinTemp = (TextView) (view != null ? view.findViewById(R.id.textView_minTemp) : null);
        textViewMaxTemp = (TextView) (view != null ? view.findViewById(R.id.textView_maxTemp) : null);
        webViewTemp = (WebView) (view != null ? view.findViewById(R.id.webView) : null);
    }

    private void initializeDialogUIElements(View view) {
        seekBarDialogMin = (VerticalSeekBar) view.findViewById(R.id.seekBar_min);
        seekBarDialogMax = (VerticalSeekBar) view.findViewById(R.id.seekBar_max);
        textViewDialogCurrentTemp = (TextView) view.findViewById(R.id.textViewCurrent);
        editTextDialogMinTemp = (EditText) view.findViewById(R.id.editTextMin);
        editTextDialogMaxTemp = (EditText) view.findViewById(R.id.editTextMax);
    }

    private void startTemperatureDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View temperatureDialog = factory.inflate(R.layout.dialog_temperature, null);

        initializeDialogUIElements(temperatureDialog);
        applyValuesToDialogUI();

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(temperatureDialog)
                .setPositiveButton("SAVE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ParseQuery<ParseObject> automationControlQuery = ParseQuery.getQuery("AutomationControl");
                                automationControlQuery.getInBackground(automationControlId, new GetCallback<ParseObject>() {
                                    public void done(ParseObject automationControl, ParseException e) {
                                        if (e == null) {
                                            automationControl.put("TempMin", seekBarDialogMin.getProgress());
                                            automationControl.put("TempMax", seekBarDialogMax.getProgress());
                                            automationControl.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    refreshFragment();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                );
        alert.show();
    }

    private void setGlobalValues(List<ParseObject> monitorDataList, List<ParseObject> automationControlList) {
        automationControlId = automationControlList.get(0).getObjectId();
        currentTemp = monitorDataList.get(0).getNumber("fahrenheit");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentTempDate = formatter.format(monitorDataList.get(0).getCreatedAt());
        minTemp = automationControlList.get(0).getNumber("TempMin");
        maxTemp = automationControlList.get(0).getNumber("TempMax");

        double high = monitorDataList.get(0).getNumber("fahrenheit").doubleValue();
        double low = monitorDataList.get(0).getNumber("fahrenheit").doubleValue();
        for(ParseObject object : monitorDataList){
            Date now = new Date();
            long yesterday = now.getTime() - 86400000;
            Date createdAt = object.getCreatedAt();
            if(createdAt.getTime() >= yesterday) {
                double temp = object.getNumber("fahrenheit").doubleValue();
                if(temp > high)
                    high = temp;
                if(temp < low)
                    low = temp;
            }
        }
        highTemp = high;
        lowTemp = low;


        for (ParseObject temp : monitorDataList) {
            Log.i("query", "= " + temp.getNumber("fahrenheit"));
            parseSeries.add(temp.getDouble("fahrenheit"));
        }

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        webViewTemp.setVerticalScrollBarEnabled(false);
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

        textViewMinTemp.setText(lowTemp + "° F");
        textViewMaxTemp.setText(highTemp + "° F");
    }

    private void loadChart() {
        String content = null;
        try {
            AssetManager assetManager = getActivity().getAssets();
            InputStream in = assetManager.open("temperature.html");
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (IOException e){
            Log.e("loadChart", "An error occurred.", e);
        }
        webViewTemp.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    private void applyValuesToDialogUI() {
        textViewDialogCurrentTemp.setText(currentTemp + "° F");
        editTextDialogMinTemp.setText(minTemp + "° F");
        editTextDialogMaxTemp.setText(maxTemp + "° F");
        seekBarDialogMin.setProgress((Integer) minTemp);
        seekBarDialogMax.setProgress((Integer) maxTemp);

        seekBarDialogMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                minTemp = this.progress;
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarDialogMin.setProgress(progress);
                if (progress > 120)
                    this.progress = 120;
                else if (progress < 0)
                    this.progress = 0;
                else
                    this.progress = progress;
                editTextDialogMinTemp.setText(String.valueOf(this.progress) + "° F");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBarDialogMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                maxTemp = this.progress;
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarDialogMax.setProgress(progress);
                if (progress > 120)
                    this.progress = 120;
                else if (progress < 0)
                    this.progress = 0;
                else
                    this.progress = progress;
                editTextDialogMaxTemp.setText(String.valueOf(this.progress) + "° F");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void preParseQuery() {
        textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestTemp.setVisibility(View.INVISIBLE);
        textViewMaxTemp.setVisibility(View.INVISIBLE);
        textViewMinTemp.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mainQuery();
            }
        }, 2500);
    }

    private void mainQuery(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonitorData");
        query.orderByDescending("createdAt");

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
        textViewLatestDate.setVisibility(View.VISIBLE);
        textViewLatestTemp.setVisibility(View.VISIBLE);
        textViewMaxTemp.setVisibility(View.VISIBLE);
        textViewMinTemp.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void refreshFragment(){
        Fragment newFragment = new TemperatureFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
