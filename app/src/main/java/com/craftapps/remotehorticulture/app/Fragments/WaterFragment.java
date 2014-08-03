package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
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
import android.widget.Button;
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
import com.craftapps.remotehorticulture.app.widgets.MultiStateToggleButton;
import com.craftapps.remotehorticulture.app.widgets.ToggleButton;
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
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WaterFragment extends Fragment {

    private WebView webViewWater;
    private TextView textViewLatestWater;
    private TextView textViewLatestDate;
    private VerticalSeekBar seekBarCurrentWater;
    private ProgressDialog progressDialog;

    private Button buttonTimeIncrease;
    private Button buttonTimeDecrease;
    private Button buttonDurIncrease;
    private Button buttonDurDecrease;
    private TextView editTextTime;
    private TextView editTextDur;
    private TextView textViewTimeHours;
    private MultiStateToggleButton multiToggleWater;

    final List<Double> parseSeries = new ArrayList<Double>();
    private Number currentWater;
    private String currentWaterDate;

    private int waterDuration;
    private int waterTimePerDay;
    private int toggleValue = 0;
    private int overrideState = 0;
    private String scheduleId;
    private String onEventId;
    private String offEventId;

    public WaterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_water, container, false);

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
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_water:
                startWaterDialog();
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
        webViewWater = (WebView) (view != null ? view.findViewById(R.id.webViewWater) : null);
        seekBarCurrentWater = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestWater = (TextView) (view != null ? view.findViewById(R.id.textView_latestWater) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestWaterDate) : null);
    }

    private void initializeDialogUIElements(View view) {
        editTextDur = (TextView) view.findViewById(R.id.editTextDuration);
        textViewTimeHours = (TextView) view.findViewById(R.id.textViewLightingDuration);
        editTextTime = (TextView) view.findViewById(R.id.editTextTime);
        buttonDurDecrease = (Button) view.findViewById(R.id.buttonDurationDecrease);
        buttonDurIncrease = (Button) view.findViewById(R.id.buttonDurationIncrease);
        buttonTimeDecrease = (Button) view.findViewById(R.id.buttonTimeDecrease);
        buttonTimeIncrease = (Button) view.findViewById(R.id.buttonTimeIncrease);
        multiToggleWater = (MultiStateToggleButton) view.findViewById(R.id.multiToggleLighting);
    }

    private void startWaterDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View waterDialog = factory.inflate(R.layout.dialog_water, null);

        initializeDialogUIElements(waterDialog);
        applyValuesToDialogUI();

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(waterDialog)
                .setPositiveButton("Set",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ParseQuery<ParseObject> scheduleQuery = ParseQuery.getQuery("Schedule");
                                scheduleQuery.getInBackground(scheduleId, new GetCallback<ParseObject>() {
                                    public void done(ParseObject scheduleObject, ParseException e) {
                                        if (e == null) {
                                            scheduleObject.put("OverrideState", overrideState);
                                            try {
                                                scheduleObject.save();
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                if(toggleValue == 1) {
                                    ParseQuery<ParseObject> onEventQuery = ParseQuery.getQuery("Event");
                                    onEventQuery.getInBackground(onEventId, new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject parseObject, ParseException e) {
                                            Calendar midnight = new GregorianCalendar();
                                            midnight.set(Calendar.HOUR_OF_DAY, 0);
                                            midnight.set(Calendar.MINUTE, 0);
                                            midnight.set(Calendar.SECOND, 0);
                                            midnight.set(Calendar.MILLISECOND, 0);
                                            final Date onEventDate = midnight.getTime();

                                            parseObject.put("IntervalSeconds", 86400 / waterTimePerDay);
                                            parseObject.put("FirstOccurrence", onEventDate);
                                            try {
                                                parseObject.save();
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                                    ParseQuery<ParseObject> offEventQuery = ParseQuery.getQuery("Event");
                                    offEventQuery.getInBackground(offEventId, new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject parseObject, ParseException e) {
                                            Calendar midnight = new GregorianCalendar();
                                            midnight.set(Calendar.HOUR_OF_DAY, 0);
                                            midnight.set(Calendar.MINUTE, 0);
                                            midnight.set(Calendar.SECOND, 0);
                                            midnight.set(Calendar.MILLISECOND, 0);
                                            midnight.add(Calendar.MINUTE, waterDuration);
                                            final Date offEventDate = midnight.getTime();

                                            parseObject.put("IntervalSeconds", 86400 / waterTimePerDay);
                                            parseObject.put("FirstOccurrence", offEventDate);
                                            parseObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Log.i("offEventQuery: ", offEventDate.toString());
                                                    refreshFragment();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                );
        alert.show();
    }

    private void setGlobalValues(Number scheduleOverrideState, List<ParseObject> eventList, List<ParseObject> monitorDataList) {
        switch (scheduleOverrideState.intValue()){
            case 0: toggleValue = 1; overrideState = 0; //AUTO
                break;
            case 1: toggleValue = 2; overrideState = 1; //FORCE ON
                break;
            case 2: toggleValue = 0; overrideState = 2; //FORCE OFF
                break;
        }

        Date startDate = eventList.get(0).getDate("FirstOccurrence");
        onEventId = eventList.get(0).getObjectId();
        Date endDate = eventList.get(1).getDate("FirstOccurrence");
        offEventId = eventList.get(1).getObjectId();
        long duration  = startDate.getTime() - endDate.getTime();

        waterDuration = Math.abs((int) TimeUnit.MILLISECONDS.toMinutes(duration));
        waterTimePerDay = 86400/eventList.get(0).getInt("IntervalSeconds");

        currentWater = monitorDataList.get(0).getNumber("waterLevel");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentWaterDate = formatter.format(monitorDataList.get(0).getCreatedAt());
        Log.i("currentWater", "= " + currentWater);
        Log.i("currentWaterDate", "= " + currentWaterDate);

        for (ParseObject water : monitorDataList) {
            Log.i("query", "= " + water.getDouble("waterLevel"));
            parseSeries.add(water.getDouble("waterLevel"));
        }
        applyValuesToUI();
    }

    private void applyValuesToUI() {
        webViewWater.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webViewWater.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        loadChart();

        seekBarCurrentWater.setProgress(currentWater.intValue());
        seekBarCurrentWater.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(currentWater.intValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentWater.intValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentWater.intValue());
            }
        });

        textViewLatestWater.setText(currentWater.toString() + "%");
        textViewLatestDate.setText(currentWaterDate);
        Log.i("success", ": apply values to UI");
    }

    private void applyValuesToDialogUI() {
        editTextDur.setText(String.valueOf(waterDuration));
        editTextTime.setText(String.valueOf(waterTimePerDay));
        textViewTimeHours.setText("EVERY " + (1440 / waterTimePerDay) / 60 + " HOURS " + (1440 / waterTimePerDay) % 60 + " MINUTES");
        multiToggleWater.setValue(toggleValue);

        editTextDur.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    waterDuration = Integer.parseInt(editTextDur.getText().toString());
                } catch (Exception e) {
                    editTextDur.setText("1");
                    waterDuration = 1;
                }
            }
        });

        editTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                try {
                    waterTimePerDay = Integer.parseInt(editTextTime.getText().toString());
                    textViewTimeHours.setText("EVERY " + (1440 / waterTimePerDay) / 60 + " HOURS " + (1440 / waterTimePerDay) % 60 + " MINUTES");
                } catch (Exception e){
                    editTextTime.setText("1");
                    waterTimePerDay = 1;
                    textViewTimeHours.setText("EVERY " + (1440 / waterTimePerDay) / 60 + " HOURS " + (1440 / waterTimePerDay) % 60 + " MINUTES");
                }
            }
        });

        buttonDurDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waterDuration > 1) waterDuration--;
                editTextDur.setText(String.valueOf(waterDuration));
            }
        });
        buttonDurIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waterDuration++;
                editTextDur.setText(String.valueOf(waterDuration));
            }
        });

        buttonTimeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waterTimePerDay > 1) waterTimePerDay--;
                editTextTime.setText(String.valueOf(waterTimePerDay));
                textViewTimeHours.setText("EVERY " + (1440 / waterTimePerDay) / 60 + " HOURS " + (1440 / waterTimePerDay) % 60 + " MINUTES");
            }
        });
        buttonTimeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waterTimePerDay++;
                editTextTime.setText(String.valueOf(waterTimePerDay));
                textViewTimeHours.setText("EVERY " + (1440 / waterTimePerDay) / 60 + " HOURS " + (1440 / waterTimePerDay) % 60 + " MINUTES");
            }
        });

        multiToggleWater.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                switch (value){
                    case 0: overrideState = 2; toggleValue = 0; break;
                    case 1: overrideState = 0; toggleValue = 1; break;
                    case 2: overrideState = 1; toggleValue = 2; break;
                    default: overrideState = 0;
                }
            }
        });
    }



    private void preParseQuery() {
        textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestWater.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void loadChart() {
        String content = null;
        try {
            AssetManager assetManager = getActivity().getAssets();
            InputStream in = assetManager.open("water.html");
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (IOException e){
            Log.e("loadChart", "An error occurred.", e);
        }
        webViewWater.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
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
        ParseQuery<ParseObject> scheduleQuery = ParseQuery.getQuery("Schedule");
        scheduleQuery.whereEqualTo("Name", "Pump");

        scheduleQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> scheduleList, ParseException e) {
                if (e == null) {
                    scheduleId = scheduleList.get(0).getObjectId();
                    final Number scheduleOverrideState = scheduleList.get(0).getNumber("OverrideState");
                    ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
                    eventQuery.whereEqualTo("ScheduleId", scheduleId);
                    eventQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> eventList, ParseException e) {
                            if (e == null) {
                                ParseQuery<ParseObject> monitorDataQuery = ParseQuery.getQuery("MonitorData");
                                monitorDataQuery.orderByDescending("createdAt");
                                monitorDataQuery.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(final List<ParseObject> monitorDataList, ParseException e) {
                                        if (e == null) {
                                            setGlobalValues(scheduleOverrideState, eventList, monitorDataList);
                                            postParseQuery();
                                        }
                                    }
                                });
                            }
                        }
                    });

                    Log.i("success", ": findInBackground");
                } else {
                    Log.i("error", ": findInBackground");
                }
            }
        });

        Log.i("success", ": parseQuery");
    }

    private void postParseQuery() {
        progressDialog.dismiss();
        textViewLatestDate.setVisibility(View.VISIBLE);
        textViewLatestWater.setVisibility(View.VISIBLE);
    }

    private void refreshFragment() {
        Fragment newFragment = new WaterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
