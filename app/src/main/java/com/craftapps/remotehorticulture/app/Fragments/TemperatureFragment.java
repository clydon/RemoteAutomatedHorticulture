package com.craftapps.remotehorticulture.app.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TemperatureFragment extends Fragment {

    private XYPlot plot;
    private TextView textViewLatestTemp;
    private TextView textViewLatestDate;
    private TextView textViewMinTemp;
    private TextView textViewMaxTemp;
    private VerticalSeekBar seekBarCurrentTemp;

    private VerticalSeekBar seekBarDialogMin;
    private VerticalSeekBar seekBarDialogMax;
    private TextView textViewDialogCurrentTemp;
    private EditText editTextDialogMinTemp;
    private EditText editTextDialogMaxTemp;

    final List<Double> parseSeries = new ArrayList<Double>();
    private ProgressDialog progressDialog;
    private Number currentTemp;
    private Number minTemp;
    private Number maxTemp;
    private String currentTempDate;


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

        GetParseValues backgroundTask = new GetParseValues();
        backgroundTask.execute();

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
        plot = (XYPlot) (view != null ? view.findViewById(R.id.mySimpleXYPlot) : null);
        seekBarCurrentTemp = (VerticalSeekBar) (view != null ? view.findViewById(R.id.verticalSeekBar) : null);
        textViewLatestTemp = (TextView) (view != null ? view.findViewById(R.id.textView_latestTemp) : null);
        textViewLatestDate = (TextView) (view != null ? view.findViewById(R.id.textView_latestDate) : null);
        textViewMinTemp = (TextView) (view != null ? view.findViewById(R.id.textView_minTemp) : null);
        textViewMaxTemp = (TextView) (view != null ? view.findViewById(R.id.textView_maxTemp) : null);
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
                                automationControlQuery.getInBackground("r16XRfo33u", new GetCallback<ParseObject>() { //todo hardcoded objectID
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
                        });
        alert.show();
    }

    private void setGlobalValues(List<ParseObject> tempList, List<ParseObject> automationControlList) {
        currentTemp = tempList.get(0).getNumber("Temperature");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentTempDate = formatter.format(tempList.get(0).getUpdatedAt());
        minTemp = automationControlList.get(0).getNumber("TempMin");
        maxTemp = automationControlList.get(0).getNumber("TempMax");

        for (ParseObject temp : tempList) {
            Log.i("query", "= " + temp.getNumber("Temperature"));
            parseSeries.add(temp.getDouble("Temperature"));
        }

        applyValuesToUI();
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
        plot.setRangeLabel("Temperature (F)");
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

    private void applyValuesToUI() {
        setupGraph();

        seekBarCurrentTemp.setProgress(currentTemp.intValue());
        seekBarCurrentTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(currentTemp.intValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentTemp.intValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentTemp.intValue());
            }
        });

        textViewLatestTemp.setText(currentTemp.toString() + "° F");
        textViewLatestDate.setText(currentTempDate);

        textViewMinTemp.setText(minTemp + "° F");
        textViewMaxTemp.setText(maxTemp + "° F");
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

    //------------------------------------------------------ ASYNC TASK -------------------------------------------------------------
    private class GetParseValues extends
            AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            plot.setVisibility(View.INVISIBLE);
            textViewLatestDate.setVisibility(View.INVISIBLE);
            textViewLatestTemp.setVisibility(View.INVISIBLE);
            textViewMaxTemp.setVisibility(View.INVISIBLE);
            textViewMinTemp.setVisibility(View.INVISIBLE);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please Wait..");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Temperature");
            query.orderByDescending("updatedAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(final List<ParseObject> tempList, ParseException e) {
                    if (e == null) {
                        ParseQuery<ParseObject> automationControlQuery = ParseQuery.getQuery("AutomationControl");
                        automationControlQuery.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> automationControlList, ParseException e) {
                                if (e == null) {
                                    setGlobalValues(tempList, automationControlList);
                                }
                            }
                        });
                    } else {
                        Log.i("error", ": findInBackground");
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            plot.setVisibility(View.VISIBLE);
            textViewLatestDate.setVisibility(View.VISIBLE);
            textViewLatestTemp.setVisibility(View.VISIBLE);
            textViewMaxTemp.setVisibility(View.VISIBLE);
            textViewMinTemp.setVisibility(View.VISIBLE);
        }
    }

    private void refreshFragment(){
        Fragment newFragment = new TemperatureFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
