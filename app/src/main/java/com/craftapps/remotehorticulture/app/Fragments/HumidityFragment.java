package com.craftapps.remotehorticulture.app.Fragments;


import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
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
import android.widget.EditText;
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
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HumidityFragment extends Fragment {

    private XYPlot plot;
    private TextView textViewLatestHumid;
    private TextView textViewLatestDate;
    private TextView textViewMinHumid;
    private TextView textViewMaxHumid;
    private VerticalSeekBar seekBarCurrentHumid;

    final List<Double> parseSeries = new ArrayList<Double>();
    private ProgressDialog progressDialog;
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
                Toast.makeText(getActivity(), "Humidity action.", Toast.LENGTH_SHORT).show();
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
        plot = (XYPlot) (view != null ? view.findViewById(R.id.humidityPlot) : null);
        seekBarCurrentHumid = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestHumid = (TextView) (view != null ? view.findViewById(R.id.textView_latestHumid) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestHumidDate) : null);
        textViewMinHumid = (TextView) (view != null ? view.findViewById(R.id.textView_minHumid) : null);
        textViewMaxHumid = (TextView) (view != null ? view.findViewById(R.id.textView_maxHumid) : null);
    }

    private void setGlobalValues(List<ParseObject> humidList) {
        currentHumid = humidList.get(0).getNumber("Humidity");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentHumidDate = formatter.format(humidList.get(0).getUpdatedAt());

        for (ParseObject humid : humidList) {
            Log.i("query", "= " + humid.getNumber("Humidity"));
            parseSeries.add(humid.getDouble("Humidity"));
        }
        maxHumid = Collections.max(parseSeries);
        minHumid = Collections.min(parseSeries);

        applyValuesToUI();
    }

    private void applyValuesToUI() {
        setupGraph();

        seekBarCurrentHumid.setProgress(currentHumid.intValue());
        seekBarCurrentHumid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(currentHumid.intValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentHumid.intValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentHumid.intValue());
            }
        });

        textViewLatestHumid.setText(currentHumid.toString() + "%");
        textViewLatestDate.setText(currentHumidDate);


        Log.i("minHumid", "= " + minHumid);
        Log.i("maxHumid", "= " + maxHumid);

        textViewMinHumid.setText(minHumid + "%");
        textViewMaxHumid.setText(maxHumid + "%");
    }

    private void setupGraph() {
        XYSeries series3 = new SimpleXYSeries(parseSeries, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "ParseSeries");

        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.WHITE);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.WHITE);

        plot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        plot.getBorderPaint().setStrokeWidth(1);
        plot.getBorderPaint().setAntiAlias(false);
        plot.getBorderPaint().setColor(Color.WHITE);

        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.BLUE, Color.RED, Shader.TileMode.MIRROR));

        LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED, null);
        formatter.setFillPaint(lineFill);
        plot.getGraphWidget().setPaddingRight(2);
        plot.addSeries(series3, formatter);

        // customize our domain/range labels
        plot.setDomainLabel("Interval");
        plot.setRangeLabel("Humidity (%)");
        plot.getLegendWidget().setVisible(false);

        // get rid of decimal points in our range labels:
        plot.setRangeValueFormat(new DecimalFormat("0"));
        plot.setDomainValueFormat(new DecimalFormat("0"));

        plot.getGraphWidget().setGridBackgroundPaint(null);

        plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 20);
        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
        plot.setTicksPerDomainLabel(5);
        plot.setRangeBoundaries(0, 160, BoundaryMode.FIXED);

        plot.redraw();
    }

    private void preParseQuery() {
        plot.setVisibility(View.INVISIBLE);
        textViewLatestDate.setVisibility(View.INVISIBLE);
        textViewLatestHumid.setVisibility(View.INVISIBLE);
        textViewMaxHumid.setVisibility(View.INVISIBLE);
        textViewMinHumid.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void parseQuery() {
        preParseQuery();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Humidity");
        query.orderByDescending("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> humidList, ParseException e) {
                if (e == null) {
                    setGlobalValues(humidList);
                        /*ParseQuery<ParseObject> automationControlQuery = ParseQuery.getQuery("AutomationControl");
                        automationControlQuery.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> automationControlList, ParseException e) {
                                if (e == null) {
                                    setGlobalValues(humidList, automationControlList);
                                }
                            }
                        });*/
                    postParseQuery();
                } else {
                    Log.i("error", ": findInBackground");
                }
            }
        });
    }

    private void postParseQuery() {
        plot.setVisibility(View.VISIBLE);
        textViewLatestDate.setVisibility(View.VISIBLE);
        textViewLatestHumid.setVisibility(View.VISIBLE);
        textViewMaxHumid.setVisibility(View.VISIBLE);
        textViewMinHumid.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void refreshFragment() {
        Fragment newFragment = new HumidityFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
