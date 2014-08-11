package com.craftapps.remotehorticulture.app.Fragments;


import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.Cards.HumidityDataCard;
import com.craftapps.remotehorticulture.app.Cards.HumidityMinMaxCard;
import com.craftapps.remotehorticulture.app.Cards.WebViewCard;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

public class HumidityFragment extends Fragment {

    private HumidityDataCard cardHumidityData;
    private HumidityMinMaxCard cardHumidityMinMax;
    private WebViewCard cardWebView;

    private TextView textViewDate;
    private Date currentDate;
    private Number currentHumid;
    private Number minHumid;
    private Number maxHumid;
    private String currentHumidDate;

    public HumidityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_humidity, container, false);

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
        if (mHumidityMenuItem != null) mHumidityMenuItem.setVisible(true);
        if (mLightingMenuItem != null) mLightingMenuItem.setVisible(false);
        if (mWaterMenuItem != null) mWaterMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_humidity:
                //Toast.makeText(getActivity(), "Humidity action.", Toast.LENGTH_SHORT).show();
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

        cardHumidityData = new HumidityDataCard(view.getContext(), R.layout.card_humidity_header);
        cardHumidityMinMax = new HumidityMinMaxCard(view.getContext(), R.layout.card_humidity_minmax);
        cardWebView = new WebViewCard(view.getContext(), R.layout.card_webview);

        CardHeader cardHeader = new CardHeader(view.getContext());
        cardHeader.setTitle("Current Humidity");
        cardHumidityData.addCardHeader(cardHeader);
        CardView cardViewHumidityData = (CardView) (view != null ? view.findViewById(R.id.cardView_HumidityData) : null);
        cardViewHumidityData.setCard(cardHumidityData);

        cardHeader.setTitle("Past Week");
        cardHumidityMinMax.addCardHeader(cardHeader);
        CardView cardViewHumidityMinMaxData = (CardView) (view != null ? view.findViewById(R.id.cardView_HumidityMinMax) : null);
        cardViewHumidityMinMaxData.setCard(cardHumidityMinMax);

        cardHeader.setTitle("Trends");
        cardWebView.addCardHeader(cardHeader);
        CardView cardViewWebView = (CardView) (view != null ? view.findViewById(R.id.cardView_HumidityWebView) : null);
        cardViewWebView.setCard(cardWebView);
    }

    private void setGlobalValues(List<ParseObject> humidList) {
        currentHumid = humidList.get(0).getNumber("humidity");
        currentDate = humidList.get(0).getCreatedAt();
        Format formatter = new SimpleDateFormat("EEE MMMM d - hh:mm a");
        currentHumidDate = formatter.format(currentDate);

        double high = humidList.get(0).getNumber("humidity").doubleValue();
        double low = humidList.get(0).getNumber("humidity").doubleValue();
        for(ParseObject object : humidList){
            Date now = new Date();
            long yesterday = now.getTime() - 86400000;
            Date createdAt = object.getCreatedAt();
            if(createdAt.getTime() >= yesterday) {
                double temp = object.getNumber("humidity").doubleValue();
                if(temp > high)
                    high = temp;
                if(temp < low)
                    low = temp;
            }
        }

        maxHumid = high;
        minHumid = low;

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        long THIRTYMINUTES = 30 * 60 * 1000;
        if(currentDate.getTime() > System.currentTimeMillis() - THIRTYMINUTES) {
            textViewDate.setBackgroundColor(Color.parseColor("#D2FF57"));
            textViewDate.setText("Online:   " + currentHumidDate);
        }
        else {
            textViewDate.setBackgroundColor(Color.parseColor("#CC270E"));
            textViewDate.setText("Last Online:   " + currentHumidDate);
        }

        cardHumidityData.setSeekBar(currentHumid);
        cardHumidityData.setValue(currentHumid);
        cardHumidityMinMax.setValues(minHumid, maxHumid);
        cardWebView.setWebView("humidity.html");
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
            public void done(final List<ParseObject> humidList, ParseException e) {
                if (e == null) {
                    setGlobalValues(humidList);
                    postParseQuery();
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
        Fragment newFragment = new HumidityFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
