package com.craftapps.remotehorticulture.app.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OverviewFragment extends Fragment {

    private ProgressDialog progressDialog;

    private VerticalSeekBar seekBarTemp;
    private VerticalSeekBar seekBarHumid;
    private TextView textViewTemp;
    private TextView textViewHumid;
    private TextView textViewDate;
    private TextView textViewLightHoursOn;
    private TextView textViewLightHoursOff;
    private TextView textViewWaterCycleEvery;
    private TextView textViewWaterCycleDur;
    private ToggleButton toggleButtonLight;

    private Number currentTemp;
    private Number currentHumid;
    private String currentDate;
    private boolean currentLight;
    private double lightsOnTime;
    private double lightsOffTime;
    private int waterDuration;
    private double waterHourInterval;

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
        seekBarTemp = (VerticalSeekBar) (view != null ? view.findViewById(R.id.seekBarTemperature) : null);
        seekBarHumid = (VerticalSeekBar) (view != null ? view.findViewById(R.id.seekBarHumidity) : null);
        textViewTemp = (TextView) (view != null ? view.findViewById(R.id.textViewOverviewTemp) : null);
        textViewHumid = (TextView) (view != null ? view.findViewById(R.id.textViewOverviewHumid) : null);
        textViewDate = (TextView) (view != null ? view.findViewById(R.id.textViewDate) : null);
        textViewLightHoursOn = (TextView) (view != null ? view.findViewById(R.id.textViewLightHours) : null);
        textViewLightHoursOff = (TextView) (view != null ? view.findViewById(R.id.textViewDarkHours) : null);
        textViewWaterCycleEvery = (TextView) (view != null ? view.findViewById(R.id.textViewWateringCycle) : null);
        textViewWaterCycleDur = (TextView) (view != null ? view.findViewById(R.id.textViewWateringDuration) : null);
        toggleButtonLight = (ToggleButton) (view != null ? view.findViewById(R.id.toggleButtonLighting) : null);
    }

    private void setGlobalValues(List<ParseObject> monitorDataList,
                                 List<ParseObject> lightEventList, Number lightOverrideState,
                                 List<ParseObject> waterEventList, Number waterOverrideState) {

        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentDate = formatter.format(monitorDataList.get(0).getCreatedAt());

        currentTemp = monitorDataList.get(0).getNumber("fahrenheit");
        currentHumid = monitorDataList.get(0).getNumber("humidity");

        switch (lightOverrideState.intValue()){
            case 0: // Auto
                int LDR = monitorDataList.get(0).getNumber("LDR").intValue();
                currentLight = LDR >= 100;
                break;
            case 1: // On
                currentLight = true;
                break;
            case 2: // Off
                currentLight = false;
                break;
        }

        Date lightsOnDate = lightEventList.get(1).getDate("FirstOccurrence");
        Date lightsOffDate = lightEventList.get(0).getDate("FirstOccurrence");
        long lightsOnTimeInMillis = Math.abs(lightsOffDate.getTime() - lightsOnDate.getTime());
        long lightsOnTimeInHours = lightsOnTimeInMillis / 6120000 ;
        BigDecimal bd = new BigDecimal(lightsOnTimeInHours);
        lightsOnTime = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        lightsOffTime = 24 - lightsOnTime;

        Date waterStartDate = waterEventList.get(0).getDate("FirstOccurrence");
        Date waterEndDate = waterEventList.get(1).getDate("FirstOccurrence");
        long waterDurationInMillis  = Math.abs(waterStartDate.getTime() - waterEndDate.getTime());
        waterDuration = Math.abs((int) TimeUnit.MILLISECONDS.toMinutes(waterDurationInMillis));
        double waterInterval = 86400.0/waterEventList.get(0).getInt("IntervalSeconds"); //Divide by 86400.0 to get interval in Hours
        bd = new BigDecimal(waterInterval);
        waterHourInterval = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        textViewDate.setText(currentDate);
        toggleButtonLight.setChecked(currentLight);

        textViewTemp.setText(currentTemp.toString() + "° F");
        seekBarTemp.setProgress(currentTemp.intValue());
        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { seekBar.setProgress(currentTemp.intValue()); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { seekBar.setProgress(currentTemp.intValue()); }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { seekBar.setProgress(currentTemp.intValue()); }
        });

        textViewHumid.setText(currentHumid.toString() + "%");
        seekBarHumid.setProgress(currentHumid.intValue());
        seekBarHumid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { seekBar.setProgress(currentHumid.intValue()); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { seekBar.setProgress(currentHumid.intValue()); }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { seekBar.setProgress(currentHumid.intValue()); }
        });

        textViewLightHoursOn.setText(String.valueOf(lightsOnTime) + " hrs");
        textViewLightHoursOff.setText(String.valueOf(lightsOffTime) + " hrs");

        textViewWaterCycleEvery.setText(String.valueOf(waterHourInterval) + " hrs");
        textViewWaterCycleDur.setText(String.valueOf(waterDuration) + " mins");
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
        monitorDataQuery();

        //MonitorData -> Temperature~
        //MonitorData -> Humidity~
        //MonitorData -> Latest Monitor Date~
        //Schedule then Event -> Light Event On/Off - Light Hours
        //Schedule then Event -> Watering freq and duration
        //CLASSNAME -> water level?
        //CLASSNAME -> Light is On or Off~
        //CLASSNAME -> VALUENEEDED

    }

    private void monitorDataQuery(){
        ParseQuery<ParseObject> monitorDataQuery = ParseQuery.getQuery("MonitorData");
        monitorDataQuery.orderByDescending("createdAt");
        monitorDataQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> monitorDataList, ParseException e) {
                if (e == null) {
                    lightEventQuery(monitorDataList);
                }
            }
        });
    }

    private void lightEventQuery(final List<ParseObject> monitorDataList){
        ParseQuery<ParseObject> lightScheduleQuery = ParseQuery.getQuery("Schedule");
        lightScheduleQuery.whereEqualTo("Name", "Light");

        lightScheduleQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> lightScheduleList, ParseException e) {
                if (e == null) {
                    String lightScheduleId = lightScheduleList.get(0).getObjectId();
                    final Number lightOverrideState = lightScheduleList.get(0).getNumber("OverrideState");
                    ParseQuery<ParseObject> lightEventQuery = ParseQuery.getQuery("Event");
                    lightEventQuery.whereEqualTo("ScheduleId", lightScheduleId);

                    lightEventQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> lightEventList, ParseException e) {
                            if (e == null) {
                                waterEventQuery(monitorDataList,lightEventList, lightOverrideState);
                            }
                        }
                    });
                }
            }
        });
    }

    private void waterEventQuery(final List<ParseObject> monitorDataList, final List<ParseObject> lightEventList, final Number lightOverrideState) {
        ParseQuery<ParseObject> waterScheduleQuery = ParseQuery.getQuery("Schedule");
        waterScheduleQuery.whereEqualTo("Name", "Pump");

        waterScheduleQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> waterScheduleList, ParseException e) {
                if (e == null) {
                    String waterScheduleId = waterScheduleList.get(0).getObjectId();
                    final Number waterOverrideState = waterScheduleList.get(0).getNumber("OverrideState");
                    ParseQuery<ParseObject> waterEventQuery = ParseQuery.getQuery("Event");
                    waterEventQuery.whereEqualTo("ScheduleId", waterScheduleId);

                    waterEventQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> waterEventList, ParseException e) {
                            if (e == null) {
                                setGlobalValues(monitorDataList, lightEventList,lightOverrideState, waterEventList, waterOverrideState);
                                postParseQuery();
                            }
                        }
                    });
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
