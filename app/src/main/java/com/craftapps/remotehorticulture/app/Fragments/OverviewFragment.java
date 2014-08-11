package com.craftapps.remotehorticulture.app.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.Cards.OverviewDataCard;
import com.craftapps.remotehorticulture.app.Cards.OverviewWaterLightCard;
import com.craftapps.remotehorticulture.app.Cards.WebViewCard;
import com.craftapps.remotehorticulture.app.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class OverviewFragment extends Fragment {

    private OverviewDataCard cardOverviewData;
    private OverviewWaterLightCard cardOverviewWaterLight;
    private WebViewCard cardWebView;
    private TextView textViewDate;

    private boolean toggleValue;
    private Number currentTemp;
    private Number currentHumid;
    private Number currentWater;
    private Date currentDate;
    private String currentOverviewDate;
    private int scheduleOverrideState;
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
        textViewDate = (TextView) (view != null ? view.findViewById(R.id.textView_Date) : null);

        cardOverviewData = new OverviewDataCard(view.getContext(), R.layout.card_overview_header);
        cardOverviewWaterLight = new OverviewWaterLightCard(view.getContext(), R.layout.card_overview_waterlight);
        cardWebView = new WebViewCard(view.getContext(), R.layout.card_webview);

        CardHeader cardHeader = new CardHeader(view.getContext());
        cardOverviewData.addCardHeader(cardHeader);
        CardView cardViewOverviewData = (CardView) (view != null ? view.findViewById(R.id.cardView_OverviewData) : null);
        cardViewOverviewData.setCard(cardOverviewData);

        cardOverviewWaterLight.addCardHeader(cardHeader);
        CardView cardViewOverviewWaterLight = (CardView) (view != null ? view.findViewById(R.id.cardView_OverviewWaterLight) : null);
        cardViewOverviewWaterLight.setCard(cardOverviewWaterLight);

        cardHeader.setTitle("Trends");
        cardWebView.addCardHeader(cardHeader);
        CardView cardViewWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_OverviewWebView) : null);
        cardViewWebView.setCard(cardWebView);
    }

    private void setGlobalValues(List<ParseObject> monitorDataList,
                                 List<ParseObject> lightEventList, Number lightOverrideState,
                                 List<ParseObject> waterEventList, Number waterOverrideState, List<ParseObject> automationStatusList) {

        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentDate = monitorDataList.get(0).getCreatedAt();
        currentOverviewDate = formatter.format(currentDate);

        currentTemp = monitorDataList.get(0).getNumber("fahrenheit");
        currentHumid = monitorDataList.get(0).getNumber("humidity");
        Number waterLevel = monitorDataList.get(0).getNumber("waterLevel");
        double waterLevelDb = (Double.parseDouble(waterLevel.toString())/700.0) * 100.0;
        DecimalFormat df = new DecimalFormat("#.##");
        currentWater = Double.parseDouble(df.format(waterLevelDb));

        scheduleOverrideState = lightOverrideState.intValue();

        Date lightsOnDate = lightEventList.get(1).getDate("FirstOccurrence");
        Date lightsOffDate = lightEventList.get(0).getDate("FirstOccurrence");
        double lightsOnTimeInMillis = Math.abs(lightsOffDate.getTime() - lightsOnDate.getTime() );
        double lightsOnTimeInHours = lightsOnTimeInMillis / (1000 * 60 * 60.0);
        BigDecimal bd = new BigDecimal(lightsOnTimeInHours);
        lightsOnTime = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();
        bd = new BigDecimal(24 - lightsOnTime);
        lightsOffTime = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();

        Date waterStartDate = waterEventList.get(0).getDate("FirstOccurrence");
        Date waterEndDate = waterEventList.get(1).getDate("FirstOccurrence");
        long waterDurationInMillis  = Math.abs(waterStartDate.getTime() - waterEndDate.getTime());
        waterDuration = (int) (waterDurationInMillis / (1000 * 60));
        double waterInterval = waterEventList.get(0).getInt("IntervalSeconds") / 3600.0; //Divide by 3600.0.0 to get interval in Hours
        bd = new BigDecimal(waterInterval);
        waterHourInterval = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();

        int automationState = automationStatusList.get(0).getNumber("State").intValue();
        if(automationState>0)
            toggleValue = (automationState != 2);
        else{
            Date nowDate = new Date();

            Calendar start = new GregorianCalendar();
            start.setTimeInMillis(lightsOnDate.getTime());
            start.set(Calendar.DAY_OF_YEAR,0);

            Calendar end = new GregorianCalendar();
            end.setTimeInMillis(lightsOffDate.getTime());
            end.set(Calendar.DAY_OF_YEAR,0);

            Calendar now = new GregorianCalendar();
            now.setTimeInMillis(nowDate.getTime());
            now.set(Calendar.DAY_OF_YEAR,0);

            toggleValue = (now.after(start) && now.before(end));
        }

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        long THIRTYMINUTES = 30 * 60 * 1000;
        if(currentDate.getTime() > System.currentTimeMillis() - THIRTYMINUTES) {
            textViewDate.setBackgroundColor(Color.parseColor("#D2FF57"));
            textViewDate.setText("Online:   " + currentOverviewDate);
        }
        else {
            textViewDate.setBackgroundColor(Color.parseColor("#CC270E"));
            textViewDate.setText("Last Online:   " + currentOverviewDate);
        }

        cardOverviewData.setTemp(currentTemp);
        cardOverviewData.setHumid(currentHumid);

        cardOverviewWaterLight.setWater(currentWater.floatValue());
        cardOverviewWaterLight.setLighting(toggleValue, scheduleOverrideState);
        cardWebView.setWebView("water_lighting.html");
    }

    private void preParseQuery() {
        textViewDate.setText("- - - LOADING PLEASE WAIT - - -");
        textViewDate.setGravity(Gravity.CENTER);
    }

    private void parseQuery() {
        preParseQuery();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                monitorDataQuery();
            }
        }, 2500);
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
                                automationStateQuery(monitorDataList, lightEventList, lightOverrideState, waterEventList, waterOverrideState);
                                postParseQuery();
                            }
                        }
                    });
                }
            }
        });
    }

    private void automationStateQuery(final List<ParseObject> monitorDataList, final List<ParseObject> lightEventList, final Number lightOverrideState, final List<ParseObject> waterEventList, final Number waterOverrideState) {
        ParseQuery<ParseObject> automationStatusQuery = ParseQuery.getQuery("AutomationState");
        automationStatusQuery.whereEqualTo("Type", "Light");
        automationStatusQuery.orderByDescending("createdAt");
        automationStatusQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> automationStatusList, ParseException e) {
                setGlobalValues(monitorDataList, lightEventList,lightOverrideState, waterEventList, waterOverrideState, automationStatusList);
                postParseQuery();
            }
        });

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

                                postParseQuery();
                            }
                        }
                    });
                }
            }
        });
    }

    private void postParseQuery() {
        textViewDate.setGravity(Gravity.LEFT);
    }

    private void refreshFragment(){
        Fragment newFragment = new OverviewFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
