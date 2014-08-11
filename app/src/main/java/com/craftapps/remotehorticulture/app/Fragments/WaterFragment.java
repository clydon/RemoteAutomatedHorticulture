package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.MultiStateToggleButton;
import com.craftapps.remotehorticulture.app.widgets.ToggleButton;
import com.craftapps.remotehorticulture.app.Cards.WaterDataCard;
import com.craftapps.remotehorticulture.app.Cards.WaterScheduleCard;
import com.craftapps.remotehorticulture.app.Cards.WebViewCard;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class WaterFragment extends Fragment {

    private WaterDataCard cardWaterData;
    private WaterScheduleCard cardWaterSchedule;
    private WebViewCard cardCycleWebView;
    private WebViewCard cardLevelWebView;

    private TextView textViewDate;
    private Button buttonTimeIncrease;
    private Button buttonTimeDecrease;
    private Button buttonDurIncrease;
    private Button buttonDurDecrease;
    private TextView editTextTime;
    private TextView editTextDur;
    private TextView textViewTimeHours;
    private MultiStateToggleButton multiToggleWater;

    private Number currentWater;
    private String currentWaterDate;

    private Date currentDate;
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
        textViewDate = (TextView) (view != null ? view.findViewById(R.id.textView_Date) : null);

        cardWaterData = new WaterDataCard(view.getContext(), R.layout.card_water_header);
        cardWaterSchedule = new WaterScheduleCard(view.getContext(), R.layout.card_water_schedule);
        cardCycleWebView = new WebViewCard(view.getContext(), R.layout.card_webview);
        cardLevelWebView = new WebViewCard(view.getContext(), R.layout.card_webview);

        CardHeader cardHeader = new CardHeader(view.getContext());
        cardHeader.setTitle("Current Water Level");
        cardWaterData.addCardHeader(cardHeader);
        CardView cardViewWaterData = (CardView) (view != null ? view.findViewById(R.id.cardView_WaterData) : null);
        cardViewWaterData.setCard(cardWaterData);

        cardHeader.setTitle("Feeding Schedule");
        cardWaterSchedule.addCardHeader(cardHeader);
        CardView cardViewWaterSchedule = (CardView) (view != null ? view.findViewById(R.id.cardView_WaterSchedule) : null);
        cardViewWaterSchedule.setCard(cardWaterSchedule);

        cardHeader.setTitle("Water Cycle");
        cardCycleWebView.addCardHeader(cardHeader);
        CardView cardViewCycleWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_WaterCycleWebView) : null);
        cardViewCycleWebView.setCard(cardCycleWebView);

        cardHeader.setTitle("Water Level");
        cardLevelWebView.addCardHeader(cardHeader);
        CardView cardViewLevelWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_WaterLevelWebView) : null);
        cardViewLevelWebView.setCard(cardLevelWebView);

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


        Number waterLevel = monitorDataList.get(0).getNumber("waterLevel");
        double waterLevelDb = (Double.parseDouble(waterLevel.toString())/700.0) * 100.0;
        DecimalFormat df = new DecimalFormat("#.##");
        currentWater = Double.parseDouble(df.format(waterLevelDb));

        Format formatter = new SimpleDateFormat("EEE MMMM d - hh:mm a");
        currentDate = monitorDataList.get(0).getCreatedAt();
        currentWaterDate = formatter.format(currentDate);

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        long THIRTYMINUTES = 30 * 60 * 1000;
        if(currentDate.getTime() > System.currentTimeMillis() - THIRTYMINUTES) {
            textViewDate.setBackgroundColor(Color.parseColor("#D2FF57"));
            textViewDate.setText("Online:   " + currentWaterDate);
        }
        else {
            textViewDate.setBackgroundColor(Color.parseColor("#CC270E"));
            textViewDate.setText("Last Online:   " + currentWaterDate);
        }

        cardCycleWebView.setWebView("water.html");
        cardLevelWebView.setWebView("water_level.html");
        cardWaterData.setValue(currentWater);
        cardWaterData.setGauge(currentWater.floatValue());
        cardWaterSchedule.setDuration(String.valueOf(waterDuration));
        cardWaterSchedule.setWaterEvery((1440 / waterTimePerDay) / 60 + " hrs " + (1440 / waterTimePerDay) % 60 + " mins");

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
        textViewDate.setText("- - - LOADING PLEASE WAIT - - -");
        textViewDate.setGravity(Gravity.CENTER);
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
        textViewDate.setGravity(Gravity.LEFT);
    }

    private void refreshFragment() {
        Fragment newFragment = new WaterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
