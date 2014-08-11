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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.Cards.TemperatureDataCard;
import com.craftapps.remotehorticulture.app.Cards.TemperatureMinMaxCard;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class TemperatureFragment extends Fragment {

    private TemperatureDataCard cardTemperatureData;
    private TemperatureMinMaxCard cardTemperatureMinMax;
    private WebViewCard cardWebView;

    private TextView textViewDate;
    private VerticalSeekBar seekBarDialogMin;
    private VerticalSeekBar seekBarDialogMax;
    private TextView textViewDialogCurrentTemp;
    private EditText editTextDialogMinTemp;
    private EditText editTextDialogMaxTemp;

    final List<Double> parseSeries = new ArrayList<Double>();
    private Number currentTemp;
    private Date currentDate;
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
        textViewDate = (TextView) (view != null ? view.findViewById(R.id.textView_Date) : null);

        cardTemperatureData = new TemperatureDataCard(view.getContext(), R.layout.card_temperature_header);
        cardTemperatureMinMax = new TemperatureMinMaxCard(view.getContext(), R.layout.card_temperature_minmax);
        cardWebView = new WebViewCard(view.getContext(), R.layout.card_webview);

        CardHeader cardHeader = new CardHeader(view.getContext());
        cardHeader.setTitle("Current Temperature");
        cardTemperatureData.addCardHeader(cardHeader);
        CardView cardViewTemperatureData = (CardView) (view != null ? view.findViewById(R.id.cardView_TemperatureData) : null);
        cardViewTemperatureData.setCard(cardTemperatureData);

        cardHeader.setTitle("Past Week");
        cardTemperatureMinMax.addCardHeader(cardHeader);
        CardView cardViewTemperatureMinMaxData = (CardView) (view != null ? view.findViewById(R.id.cardView_TemperatureMinMax) : null);
        cardViewTemperatureMinMaxData.setCard(cardTemperatureMinMax);

        cardHeader.setTitle("Trends");
        cardWebView.addCardHeader(cardHeader);
        CardView cardViewWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_TemperatureWebView) : null);
        cardViewWebView.setCard(cardWebView);
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
        Format formatter = new SimpleDateFormat("EEE MMMM d - hh:mm a");
        currentDate = monitorDataList.get(0).getCreatedAt();
        currentTempDate = formatter.format(currentDate);
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
        long THIRTYMINUTES = 30 * 60 * 1000;
        if(currentDate.getTime() > System.currentTimeMillis() - THIRTYMINUTES) {
            textViewDate.setBackgroundColor(Color.parseColor("#D2FF57"));
            textViewDate.setText("Online:   " + currentTempDate);
        }
        else {
            textViewDate.setBackgroundColor(Color.parseColor("#CC270E"));
            textViewDate.setText("Last Online:   " + currentTempDate);
        }

        cardTemperatureData.setSeekBar(currentTemp);
        cardTemperatureData.setValue(currentTemp);
        cardTemperatureMinMax.setValues(lowTemp, highTemp);
        cardWebView.setWebView("temperature.html");
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
        textViewDate.setGravity(Gravity.LEFT);
    }

    private void refreshFragment(){
        Fragment newFragment = new TemperatureFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
