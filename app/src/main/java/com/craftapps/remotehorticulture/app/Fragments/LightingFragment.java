package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.MultiStateToggleButton;
import com.craftapps.remotehorticulture.app.widgets.RangeBar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class LightingFragment extends Fragment {

    private XYPlot lightingPlot;
    private TextView textViewLatestLightingDate;
    private ToggleButton toggleButtonLighting;
    private ProgressDialog progressDialog;

    private RangeBar rangeBarLighting;
    private MultiStateToggleButton multiToggleLighting;
    private TextView textViewLightingOn;
    private TextView textViewLightingOff;
    private TextView textViewLightingDuration;

    final List<Double> parseSeries = new ArrayList<Double>();
    private int currentLighting;
    private String currentLightingDate;

    private int lightingTimeOn;
    private int lightingTimeOff;
    private int lightingDuration;

    private int toggleValue = 0;
    private int overrideState = 0;
    private String scheduleId;
    private String onEventId;
    private String offEventId;

    public LightingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lighting, container, false);

        initializeUIElements(rootView);

        parseQuery();
        //setupGraph();

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
        if (mLightingMenuItem != null) mLightingMenuItem.setVisible(true);
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_lighting:
                startLightingDialog();
                Toast.makeText(getActivity(), "Lighting action.", Toast.LENGTH_SHORT).show();
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
        lightingPlot = (XYPlot) (view != null ? view.findViewById(R.id.lightingPlot) : null);
        toggleButtonLighting = (ToggleButton) (view != null ? view.findViewById(R.id.toggleButtonLighting) : null);
        textViewLatestLightingDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestLightingDate) : null);
    }

    private void initializeDialogUIElements(View view) {
        rangeBarLighting = (RangeBar) view.findViewById(R.id.rangeBarLighting);
        multiToggleLighting = (MultiStateToggleButton) view.findViewById(R.id.multiToggleLighting);

        textViewLightingOn = (TextView) view.findViewById(R.id.textViewLightingOn);
        textViewLightingOff = (TextView) view.findViewById(R.id.textViewLightingOff);
        textViewLightingDuration = (TextView) view.findViewById(R.id.textViewLightingDuration);
    }

    private void startLightingDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View lightingDialog = factory.inflate(R.layout.dialog_lighting, null);

        initializeDialogUIElements(lightingDialog);
        applyValuesToDialogUI();

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(lightingDialog)
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
                                            /*scheduleObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Log.i("scheduleQuery: ", "success");
                                                }
                                            });*/
                                        }
                                    }
                                });
                                if(toggleValue == 1) {
                                    ParseQuery<ParseObject> onEventQuery = ParseQuery.getQuery("Event");
                                    onEventQuery.getInBackground(onEventId, new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject parseObject, ParseException e) {
                                            int leftIndex = rangeBarLighting.getLeftIndex();
                                            int hour = (leftIndex % 1440)/ 60;
                                            int min = (leftIndex % 1440) - (hour * 60);

                                            Calendar midnight = new GregorianCalendar();
                                            Log.i("Current Time: " , midnight.getTime().toString());
                                            midnight.set(Calendar.HOUR_OF_DAY, hour);
                                            midnight.set(Calendar.MINUTE, min);
                                            midnight.set(Calendar.SECOND, 0);
                                            midnight.set(Calendar.MILLISECOND, 0);
                                            Log.i("Scheduled Time: " , midnight.getTime().toString());
                                            final Date onEventDate = midnight.getTime();
                                            Log.i("FinalDate Time: " , onEventDate.toString());

                                            parseObject.put("IntervalSeconds", 86400);
                                            parseObject.put("FirstOccurrence", onEventDate);
                                            try {
                                                parseObject.save();
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                            /*parseObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Log.i("onEventQuery: ", onEventDate.toString());
                                                }
                                            });*/
                                        }
                                    });
                                    ParseQuery<ParseObject> offEventQuery = ParseQuery.getQuery("Event");
                                    offEventQuery.getInBackground(offEventId, new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject parseObject, ParseException e) {
                                            int rightIndex = rangeBarLighting.getRightIndex();
                                            int hour = (rightIndex % 1440)/ 60;
                                            int min = (rightIndex % 1440) - (hour * 60);

                                            Calendar midnight = new GregorianCalendar();
                                            Log.i("Current Time: " , midnight.getTime().toString());
                                            midnight.set(Calendar.HOUR_OF_DAY, hour);
                                            midnight.set(Calendar.MINUTE, min);
                                            midnight.set(Calendar.SECOND, 0);
                                            midnight.set(Calendar.MILLISECOND, 0);
                                            Log.i("Scheduled Time: " , midnight.getTime().toString());
                                            final Date offEventDate = midnight.getTime();
                                            Log.i("FinalDate Time: " , offEventDate.toString());

                                            parseObject.put("IntervalSeconds", 86400);
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
                                //refreshFragment();
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

        onEventId = eventList.get(1).getObjectId();
        offEventId = eventList.get(0).getObjectId();

        Calendar startCal = Calendar.getInstance();
        Date startDate = eventList.get(1).getDate("FirstOccurrence");
        long startDateInMillis = startDate.getTime();
        startCal.setTimeInMillis(startDateInMillis);

        Calendar midStart = Calendar.getInstance();
        midStart.setTime(startCal.getTime());
        midStart.set(Calendar.HOUR_OF_DAY, 0);
        midStart.set(Calendar.MINUTE, 0);
        midStart.set(Calendar.SECOND, 0);
        midStart.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        Date endDate = eventList.get(0).getDate("FirstOccurrence");
        long endDateInMillis = endDate.getTime();
        endCal.setTimeInMillis(endDateInMillis);

        Calendar midEnd = Calendar.getInstance();
        midEnd.setTime(endCal.getTime());
        midEnd.set(Calendar.HOUR_OF_DAY, 0);
        midEnd.set(Calendar.MINUTE, 0);
        midEnd.set(Calendar.SECOND, 0);
        midEnd.set(Calendar.MILLISECOND, 0);

        Log.i("MidnightStart", "= " + midStart.getTimeInMillis()/1000/60 + " " + midStart.getTime());
        Log.i("StartTime", "= " + startCal.getTimeInMillis()/1000/60 + " " + startCal.getTime());
        Log.i("MidnightEnd", "= " + midEnd.getTimeInMillis()/1000/60 + " " + midEnd.getTime());
        Log.i("EndTime", "= " + endCal.getTimeInMillis()/1000/60 + " " + endCal.getTime());


        long startDiff = startCal.getTimeInMillis() - midStart.getTimeInMillis();
        long startTimeInMinutes = (startDiff/1000)/60;
        Log.i("Start Diff MLS: " , startDiff + "Minutes: " + startTimeInMinutes);
        lightingTimeOn = (int) startTimeInMinutes;

        long endDiff = endCal.getTimeInMillis() - midEnd.getTimeInMillis();
        long endTimeInMinutes = (endDiff/1000)/60;
        Log.i("End Diff MLS: " , endDiff + "Minutes: " + endTimeInMinutes);
        lightingTimeOff = (int) endTimeInMinutes;

        lightingDuration = Math.abs(lightingTimeOff - lightingTimeOn);

        currentLighting = monitorDataList.get(0).getInt("LDR");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentLightingDate = formatter.format(monitorDataList.get(0).getUpdatedAt());

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        setupGraph();

        toggleButtonLighting.setChecked((currentLighting > 100 ? true : false));

        textViewLatestLightingDate.setText(currentLightingDate);
        Log.i("success", ": apply values to UI");
    }

    private void applyValuesToDialogUI() {
        textViewLightingOn.setText(minutesToTimeString(lightingTimeOn));
        textViewLightingOff.setText(minutesToTimeString(lightingTimeOff));

        String minutesString;

        int hours = lightingDuration / 60;
        int minutes = lightingDuration % 60;

        if(minutes == 0)    minutesString = "00";
        else if(minutes<10) minutesString = "0" + minutes;
        else                minutesString = String.valueOf(minutes);

        textViewLightingDuration.setText(hours + ":" + minutesString + " HOURS OF LIGHT");

        Log.i("lightingTimeOn", "= " + lightingTimeOn);
        Log.i("lightingTimeOff", "= " + lightingTimeOff);


        multiToggleLighting.setValue(toggleValue);
        rangeBarLighting.setThumbIndices(lightingTimeOn, lightingTimeOff);

        rangeBarLighting.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                textViewLightingOn.setText(minutesToTimeString(leftThumbIndex));
                textViewLightingOff.setText(minutesToTimeString(rightThumbIndex));

                String minutesString;
                int hours = Math.abs(leftThumbIndex-rightThumbIndex) / 60;
                int minutes = Math.abs(leftThumbIndex-rightThumbIndex) % 60;

                if(minutes == 0)    minutesString = "00";
                else if(minutes<10) minutesString = "0" + minutes;
                else                minutesString = String.valueOf(minutes);

                textViewLightingDuration.setText(hours + ":" + minutesString + " HOURS OF LIGHT");
            }
        });
        multiToggleLighting.setOnValueChangedListener(new com.craftapps.remotehorticulture.app.widgets.ToggleButton.OnValueChangedListener() {
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

    private String minutesToTimeString(int minutes){
        String hourString;
        String minuteString;
        String AMPM;

        if(minutes/60 >= 12){
            hourString = String.valueOf(((minutes/60)-12));
            AMPM = "PM";
        }
        else {
            hourString = ((minutes/60 == 0) ? "12" : String.valueOf(minutes/60));
            AMPM = "AM";
        }

        int mins = minutes % 60;
        if(mins == 0)   minuteString = "00";
        else if(mins<10)minuteString = "0" + mins;
        else            minuteString = String.valueOf(mins);

        return hourString + ":" + minuteString + " " + AMPM;
    }

    private void setupGraph() {
        List<Double> pSeries = new ArrayList<Double>();

        for(int i=0; i<50; i++){
            int j = (int)(Math.random() * ((1 - 0) + 1));
            if (j==0) pSeries.add((double) 0);
            else pSeries.add((double) 100);
        }

        XYSeries series3 = new SimpleXYSeries(pSeries, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "ParseSeries");

        //lightingPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        lightingPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.WHITE);
        lightingPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.WHITE);

        lightingPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        lightingPlot.getBorderPaint().setStrokeWidth(1);
        lightingPlot.getBorderPaint().setAntiAlias(false);
        lightingPlot.getBorderPaint().setColor(Color.WHITE);

        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.BLUE, Color.RED, Shader.TileMode.MIRROR));

        LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED, null);
        formatter.setFillPaint(lineFill);
        lightingPlot.getGraphWidget().setPaddingRight(2);
        lightingPlot.addSeries(series3, formatter);

        // customize our domain/range labels
        lightingPlot.setDomainLabel("Interval");
        lightingPlot.setRangeLabel("Lighting Level (%)");
        lightingPlot.getLegendWidget().setVisible(false);

        // get rid of decimal points in our range labels:
        lightingPlot.setRangeValueFormat(new DecimalFormat("0"));
        lightingPlot.setDomainValueFormat(new DecimalFormat("0"));

        lightingPlot.getGraphWidget().setGridBackgroundPaint(null);

        lightingPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 20);
        lightingPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
        lightingPlot.setTicksPerDomainLabel(5);
        lightingPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

        lightingPlot.redraw();
        Log.i("success", ": setupGraph/redraw");
    }

    private void preParseQuery() {
        lightingPlot.setVisibility(View.INVISIBLE);
        textViewLatestLightingDate.setVisibility(View.INVISIBLE);
        toggleButtonLighting.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        ParseQuery<ParseObject> scheduleQuery = ParseQuery.getQuery("Schedule");
        scheduleQuery.whereEqualTo("Name", "Light");

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
                                monitorDataQuery.orderByDescending("updatedAt");
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

        lightingPlot.setVisibility(View.VISIBLE);
        textViewLatestLightingDate.setVisibility(View.VISIBLE);
        toggleButtonLighting.setVisibility(View.VISIBLE);
    }

    private void refreshFragment() {
        Fragment newFragment = new LightingFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
