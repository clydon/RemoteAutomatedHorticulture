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
import android.widget.CheckBox;
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
import java.util.List;

public class WaterFragment extends Fragment {

    private XYPlot waterPlot;
    private TextView textViewLatestWater;
    private TextView textViewLatestDate;
    private VerticalSeekBar seekBarCurrentWater;
    private ProgressDialog progressDialog;

    private CheckBox checkBoxSunday;
    private CheckBox checkBoxMonday;
    private CheckBox checkBoxTuesday;
    private CheckBox checkBoxWednesday;
    private CheckBox checkBoxThursday;
    private CheckBox checkBoxFriday;
    private CheckBox checkBoxSaturday;
    private CheckBox checkBoxRepeat;
    private Button buttonStartTime; //todo
    private Button buttonEndTime; //todo

    final List<Double> parseSeries = new ArrayList<Double>();
    private Number currentWater;
    private String currentWaterDate;

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
        checkBoxSunday = (CheckBox) view.findViewById(R.id.checkBox_Sunday);
        checkBoxMonday = (CheckBox) view.findViewById(R.id.checkBox_Monday);
        checkBoxTuesday = (CheckBox) view.findViewById(R.id.checkBox_Tuesday);
        checkBoxWednesday = (CheckBox) view.findViewById(R.id.checkBox_Wednesday);
        checkBoxThursday = (CheckBox) view.findViewById(R.id.checkBox_Thursday);
        checkBoxFriday = (CheckBox) view.findViewById(R.id.checkBox_Friday);
        checkBoxSaturday = (CheckBox) view.findViewById(R.id.checkBox_Saturday);

        checkBoxRepeat = (CheckBox) view.findViewById(R.id.checkBox_Repeat);

        buttonStartTime = (Button) view.findViewById(R.id.buttonStartTime);
        buttonEndTime = (Button) view.findViewById(R.id.buttonEndTime);
    }

    private void startWaterDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View waterDialog = factory.inflate(R.layout.dialog_water, null);

        initializeDialogUIElements(waterDialog);
        applyValuesToDialogUI();

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(waterDialog)
                .setPositiveButton("Add",
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

    private void setGlobalValues(List<ParseObject> waterList) {
        currentWater = waterList.get(0).getNumber("waterLevel");
        Format formatter = new SimpleDateFormat("hh:mm a - EEE MMMM d");
        currentWaterDate = formatter.format(waterList.get(0).getUpdatedAt());
        Log.i("currentWater", "= " + currentWater);
        Log.i("currentWaterDate", "= " + currentWaterDate);

        for (ParseObject water : waterList) {
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
        checkBoxSunday.setChecked(false);
        checkBoxMonday.setChecked(false);
        checkBoxTuesday.setChecked(false);
        checkBoxWednesday.setChecked(false);
        checkBoxThursday.setChecked(false);
        checkBoxFriday.setChecked(false);
        checkBoxSaturday.setChecked(false);

        checkBoxRepeat.setChecked(false);
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
        waterPlot.setRangeBoundaries(0, 160, BoundaryMode.FIXED);

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

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MonitorData");
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> waterList, ParseException e) {
                if (e == null) {
                    setGlobalValues(waterList);
                    postParseQuery();
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
