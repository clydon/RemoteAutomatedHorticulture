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
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaterFragment extends Fragment {

    private XYPlot waterPlot;
    private TextView textViewLatestWater;
    private TextView textViewLatestDate;
    private VerticalSeekBar seekBarCurrentWater;
    private ProgressDialog progressDialog;

    private Button buttonTimeIncrease;
    private Button buttonTimeDecrease;
    private Button buttonDurIncrease;
    private Button buttonDurDecrease;
    private TextView textViewTime;
    private TextView textViewDur;
    private TextView textViewDurTime;
    private MultiStateToggleButton multiToggleWater;

    final List<Double> parseSeries = new ArrayList<Double>();
    private Number currentWater;
    private String currentWaterDate;

    private int waterDuration = 5;
    private int waterTimePerDay = 4;
    private int toggleValue = 0;

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
                Toast.makeText(getActivity(), "Water action.", Toast.LENGTH_SHORT).show();
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
        waterPlot = (XYPlot) (view != null ? view.findViewById(R.id.waterPlot) : null);
        seekBarCurrentWater = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestWater = (TextView) (view != null ? view.findViewById(R.id.textView_latestWater) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestWaterDate) : null);
    }

    private void initializeDialogUIElements(View view) {
        textViewDur = (TextView) view.findViewById(R.id.textViewDuration);
        textViewDurTime = (TextView) view.findViewById(R.id.textViewDurationTime);
        textViewTime = (TextView) view.findViewById(R.id.textViewTime);
        buttonDurDecrease = (Button) view.findViewById(R.id.buttonDurationDecrease);
        buttonDurIncrease = (Button) view.findViewById(R.id.buttonDurationIncrease);
        buttonTimeDecrease = (Button) view.findViewById(R.id.buttonTimeDecrease);
        buttonTimeIncrease = (Button) view.findViewById(R.id.buttonTimeIncrease);
        multiToggleWater = (MultiStateToggleButton) view.findViewById(R.id.multiToggleWatering);
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
                            public void onClick(DialogInterface dialog, int whichButton) { //todo save schedule to Parse
                                /*ParseQuery<ParseObject> automationControlQuery = ParseQuery.getQuery("AutomationControl");
                                automationControlQuery.getInBackground("r16XRfo33u", new GetCallback<ParseObject>() {
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
                                });*/
                            }
                        }
                );
        alert.show();
    }

    private void setGlobalValues(String scheduleId, Number scheduleOverrideState, List<ParseObject> eventList, List<ParseObject> monitorDataList) {
        switch (scheduleOverrideState.intValue()){
            case 0: toggleValue = 1;
                break;
            case 1: toggleValue = 2;
                break;
            case 2: toggleValue = 0;
                break;
        }

        Date startDate = eventList.get(0).getDate("FirstOccurrence");
        Date endDate = eventList.get(1).getDate("FirstOccurrence");
        long duration  = startDate.getTime() - endDate.getTime();

        waterDuration = (int) TimeUnit.MILLISECONDS.toMinutes(duration);
        waterTimePerDay = 86400/eventList.get(0).getInt("IntervalSeconds");

        //12 times per day (every two hours == 7200 seconds)
        //duration 5 minutes (2 events made First Occurrence differs by 5 minutes)

        //9 times per day (Interval = 86400/<times per day>)
        //
        /*int interval = 7200;
        Calendar midnight = new GregorianCalendar();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        Date fireOn = midnight.getTime();

        midnight.add(Calendar.SECOND, interval);
        Date fireOff = midnight.getTime();*/




        currentWater = monitorDataList.get(0).getNumber("waterLevel");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentWaterDate = formatter.format(monitorDataList.get(0).getUpdatedAt());
        Log.i("currentWater", "= " + currentWater);
        Log.i("currentWaterDate", "= " + currentWaterDate);

        for (ParseObject water : monitorDataList) {
            Log.i("query", "= " + water.getDouble("waterLevel"));
            parseSeries.add(water.getDouble("waterLevel"));
        }
        applyValuesToUI();
    }

    private void applyValuesToUI() {
        setupGraph();

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
        textViewDur.setText(String.valueOf(waterDuration) + " mins");
        textViewTime.setText(String.valueOf(waterTimePerDay));
        multiToggleWater.setValue(toggleValue);

        buttonDurDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waterDuration > 0) waterDuration--;
                textViewDur.setText(String.valueOf(waterDuration) + " mins");
            }
        });
        buttonDurIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waterDuration++;
                textViewDur.setText(String.valueOf(waterDuration) + " mins");
            }
        });

        buttonTimeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(waterTimePerDay > 0) waterTimePerDay--;
                textViewTime.setText(String.valueOf(waterTimePerDay));
                textViewDurTime.setText("EVERY " + (1440/ waterTimePerDay)/60 + " HOURS " + (1440/ waterTimePerDay)%60 + " MINUTES");
            }
        });
        buttonTimeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waterTimePerDay++;
                textViewTime.setText(String.valueOf(waterTimePerDay));
                textViewDurTime.setText("EVERY " + (1440/ waterTimePerDay)/60 + " HOURS " + (1440/ waterTimePerDay)%60 + " MINUTES");
            }
        });
    }

    private void setupGraph() {
        XYSeries series3 = new SimpleXYSeries(parseSeries, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "ParseSeries");

        //waterPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        waterPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.WHITE);
        waterPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.WHITE);

        waterPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        waterPlot.getBorderPaint().setStrokeWidth(1);
        waterPlot.getBorderPaint().setAntiAlias(false);
        waterPlot.getBorderPaint().setColor(Color.WHITE);

        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.BLUE, Color.RED, Shader.TileMode.MIRROR));

        LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED, null);
        formatter.setFillPaint(lineFill);
        waterPlot.getGraphWidget().setPaddingRight(2);
        waterPlot.addSeries(series3, formatter);

        // customize our domain/range labels
        waterPlot.setDomainLabel("Interval");
        waterPlot.setRangeLabel("Water Level (%)");
        waterPlot.getLegendWidget().setVisible(false);

        // get rid of decimal points in our range labels:
        waterPlot.setRangeValueFormat(new DecimalFormat("0"));
        waterPlot.setDomainValueFormat(new DecimalFormat("0"));

        waterPlot.getGraphWidget().setGridBackgroundPaint(null);

        waterPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 20);
        waterPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
        waterPlot.setTicksPerDomainLabel(5);
        waterPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

        waterPlot.redraw();
        Log.i("success", ": setupGraph/redraw");
    }

    private void preParseQuery() {
        waterPlot.setVisibility(View.INVISIBLE);
        textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestWater.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Schedule");
        query.whereEqualTo("Type", 2);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> scheduleList, ParseException e) {
                if (e == null) {
                    final String scheduleId = scheduleList.get(0).getObjectId();
                    //final String scheduleId = "azc4J8lBGW";
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
                                            setGlobalValues(scheduleId, scheduleOverrideState, eventList, monitorDataList);
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
        waterPlot.setVisibility(View.VISIBLE);
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
