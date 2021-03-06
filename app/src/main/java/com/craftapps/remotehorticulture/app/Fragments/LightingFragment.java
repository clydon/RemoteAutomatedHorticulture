package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.Cards.LightingDataCard;
import com.craftapps.remotehorticulture.app.Cards.LightingScheduleCard;
import com.craftapps.remotehorticulture.app.widgets.MultiStateToggleButton;
import com.craftapps.remotehorticulture.app.widgets.RangeBar;
import com.craftapps.remotehorticulture.app.Cards.WebViewCard;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class LightingFragment extends Fragment {

    private LightingDataCard cardLightingData;
    private LightingScheduleCard cardLightingSchedule;
    private WebViewCard cardWebView;

    private TextView textViewDate;

    private RangeBar rangeBarLighting;
    private MultiStateToggleButton multiToggleLighting;
    private TextView textViewLightingOn;
    private TextView textViewLightingOff;
    private TextView textViewLightingDuration;

    private int currentLighting;
    private String currentLightingDate;

    private int lightingTimeOn;
    private int lightingTimeOff;
    private int lightingDuration;

    private boolean toggleValue = false;
    private int toggleValueDialog = 0;
    private int overrideState = 0;
    private Date currentDate;
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
                //Toast.makeText(getActivity(), "Lighting action.", Toast.LENGTH_SHORT).show();
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

        cardLightingData = new LightingDataCard(view.getContext(), R.layout.card_lighting_header);
        cardLightingSchedule = new LightingScheduleCard(view.getContext(), R.layout.card_lighting_schedule);
        cardWebView = new WebViewCard(view.getContext(), R.layout.card_webview);

        CardHeader cardHeader = new CardHeader(view.getContext());
        cardHeader.setTitle("Current Lighting");
        cardLightingData.addCardHeader(cardHeader);
        CardView cardViewLightingData = (CardView) (view != null ? view.findViewById(R.id.cardView_LightingData) : null);
        cardViewLightingData.setCard(cardLightingData);

        cardHeader.setTitle("Lighting Schedule");
        cardLightingSchedule.addCardHeader(cardHeader);
        CardView cardViewLightingSchedule = (CardView) (view != null ? view.findViewById(R.id.cardView_LightingSchedule) : null);
        cardViewLightingSchedule.setCard(cardLightingSchedule);

        cardHeader.setTitle("Light Hours");
        cardWebView.addCardHeader(cardHeader);
        CardView cardViewWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_LightingWebView) : null);
        cardViewWebView.setCard(cardWebView);
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
                                ParseQuery<ParseObject> onEventQuery = ParseQuery.getQuery("Event");
                                onEventQuery.getInBackground(onEventId, new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(final ParseObject onObject, ParseException e) {
                                        int leftIndex = rangeBarLighting.getLeftIndex();
                                        int hour = (leftIndex % 1440)/ 60;
                                        int min = (leftIndex % 1440) - (hour * 60);

                                        Calendar midnight = new GregorianCalendar();
                                        Log.i("Current Time: " , midnight.getTime().toString());
                                        midnight.add(Calendar.DAY_OF_YEAR, -2);
                                        midnight.set(Calendar.HOUR_OF_DAY, hour);
                                        midnight.set(Calendar.MINUTE, min);
                                        midnight.set(Calendar.SECOND, 0);
                                        midnight.set(Calendar.MILLISECOND, 0);
                                        Log.i("Scheduled Time: " , midnight.getTime().toString());
                                        final Date onEventDate = midnight.getTime();
                                        Log.i("FinalDate Time: " , onEventDate.toString());

                                        onObject.put("IntervalSeconds", 86400);
                                        onObject.put("FirstOccurrence", onEventDate);

                                        ParseQuery<ParseObject> offEventQuery = ParseQuery.getQuery("Event");
                                        offEventQuery.getInBackground(offEventId, new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(final ParseObject offObject, ParseException e) {
                                                int rightIndex = rangeBarLighting.getRightIndex();
                                                int hour = (rightIndex % 1440)/ 60;
                                                int min = (rightIndex % 1440) - (hour * 60);

                                                Calendar midnight = new GregorianCalendar();
                                                Log.i("Current Time: " , midnight.getTime().toString());
                                                midnight.add(Calendar.DAY_OF_YEAR, -2);
                                                midnight.set(Calendar.HOUR_OF_DAY, hour);
                                                midnight.set(Calendar.MINUTE, min);
                                                midnight.set(Calendar.SECOND, 0);
                                                midnight.set(Calendar.MILLISECOND, 0);
                                                Log.i("Scheduled Time: " , midnight.getTime().toString());
                                                final Date offEventDate = midnight.getTime();
                                                Log.i("FinalDate Time: " , offEventDate.toString());

                                                offObject.put("IntervalSeconds", 86400);
                                                offObject.put("FirstOccurrence", offEventDate);

                                                List<ParseObject> saveList = new ArrayList<ParseObject>() {
                                                    { add(offObject); add(onObject); }
                                                };

                                                try {
                                                    ParseObject.saveAll(saveList);
                                                } catch (ParseException e1) {
                                                    e1.printStackTrace();
                                                }
                                                try {
                                                    offObject.save();
                                                } catch (ParseException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }
                                        });
                                        try {
                                            onObject.save();
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });


                                ParseQuery<ParseObject> scheduleQuery = ParseQuery.getQuery("Schedule");
                                scheduleQuery.getInBackground(scheduleId, new GetCallback<ParseObject>() {
                                    public void done(ParseObject scheduleObject, ParseException e) {
                                        if (e == null) {
                                            scheduleObject.put("OverrideState", overrideState);
                                            scheduleObject.saveInBackground(new SaveCallback() {
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

    private void setGlobalValues(Number scheduleOverrideState, List<ParseObject> eventList, List<ParseObject> monitorDataList, List<ParseObject> automationStatusList) {
        switch (scheduleOverrideState.intValue()){
            case 0: toggleValueDialog = 1; overrideState = 0; //AUTO
                break;
            case 1: toggleValueDialog = 2; overrideState = 1; //FORCE ON
                break;
            case 2: toggleValueDialog = 0; overrideState = 2; //FORCE OFF
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
        Format formatter = new SimpleDateFormat("EEE MMMM d - hh:mm a");
        currentDate = monitorDataList.get(0).getCreatedAt();
        currentLightingDate = formatter.format(currentDate);

        int automationState = automationStatusList.get(0).getNumber("State").intValue();
        if(automationState>0)
            toggleValue = (automationState != 2);
        else{
            Date nowDate = new Date();

            Calendar start = new GregorianCalendar();
            start.setTimeInMillis(startDate.getTime());
            start.set(Calendar.DAY_OF_YEAR,0);

            Calendar end = new GregorianCalendar();
            end.setTimeInMillis(endDate.getTime());
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
            textViewDate.setText("Online:   " + currentLightingDate);
        }
        else {
            textViewDate.setBackgroundColor(Color.parseColor("#CC270E"));
            textViewDate.setText("Last Online:   " + currentLightingDate);
        }

        cardLightingData.setToggleValue(toggleValue);
        cardLightingData.setScheduleValue(overrideState);
        cardLightingSchedule.setTime(minutesToTimeString(lightingTimeOn), minutesToTimeString(lightingTimeOff));
        cardWebView.setWebView("lighting.html");

        Log.i("success", ": apply values to UI");
    }

    private void applyValuesToDialogUI() {
        final int[] lightingMinutesOn = {lightingTimeOn};
        final int[] lightingMinutesOff = {lightingTimeOff};

        textViewLightingOn.setText(minutesToTimeString(lightingMinutesOn[0]));
        textViewLightingOff.setText(minutesToTimeString(lightingMinutesOff[0]));

        String minutesString;

        int hours = lightingDuration / 60;
        int minutes = lightingDuration % 60;

        if(minutes == 0)    minutesString = "00";
        else if(minutes<10) minutesString = "0" + minutes;
        else                minutesString = String.valueOf(minutes);

        textViewLightingDuration.setText(hours + ":" + minutesString + " HOURS OF LIGHT");

        Log.i("lightingTimeOn", "= " + lightingMinutesOn[0]);
        Log.i("lightingTimeOff", "= " + lightingTimeOff);


        multiToggleLighting.setValue(toggleValueDialog);
        rangeBarLighting.setThumbIndices(lightingMinutesOn[0], lightingTimeOff);

        rangeBarLighting.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                textViewLightingOn.setText(minutesToTimeString(leftThumbIndex));
                textViewLightingOff.setText(minutesToTimeString(rightThumbIndex));

                lightingMinutesOn[0] = leftThumbIndex;
                lightingMinutesOff[0] = rightThumbIndex;

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
                    case 0: overrideState = 2; toggleValueDialog = 0; break;
                    case 1: overrideState = 0; toggleValueDialog = 1; break;
                    case 2: overrideState = 1; toggleValueDialog = 2; break;
                    default: overrideState = 0;
                }
            }
        });
        textViewLightingOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = lightingMinutesOn[0] / 60;
                int minute = lightingMinutesOn[0] % 60;
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        lightingMinutesOn[0] = (selectedHour*60)+selectedMinute;
                        rangeBarLighting.setThumbIndices(lightingMinutesOn[0], lightingTimeOff);
                        textViewLightingOn.setText( minutesToTimeString(lightingMinutesOn[0]));
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Lights On at:");
                mTimePicker.show();
            }
        });
        textViewLightingOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = lightingMinutesOff[0] / 60;
                int minute = lightingMinutesOff[0] % 60;
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        lightingMinutesOff[0] = (selectedHour*60)+selectedMinute;
                        rangeBarLighting.setThumbIndices(lightingMinutesOn[0], lightingMinutesOff[0]);
                        textViewLightingOff.setText( minutesToTimeString(lightingMinutesOff[0]));
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Lights Off at:");
                mTimePicker.show();
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

    private void mainQuery() {
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
                                monitorDataQuery.orderByDescending("createdAt");
                                monitorDataQuery.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(final List<ParseObject> monitorDataList, ParseException e) {
                                        if (e == null) {
                                            ParseQuery<ParseObject> automationStatusQuery = ParseQuery.getQuery("AutomationState");
                                            automationStatusQuery.whereEqualTo("Type", "Light");
                                            automationStatusQuery.orderByDescending("createdAt");
                                            automationStatusQuery.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> automationStatusList, ParseException e) {
                                                    setGlobalValues(scheduleOverrideState, eventList, monitorDataList, automationStatusList);
                                                    postParseQuery();
                                                }
                                            });
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
    }

    private void postParseQuery() {
        textViewDate.setGravity(Gravity.LEFT);
    }

    private void refreshFragment() {
        Fragment newFragment = new LightingFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
