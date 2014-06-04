package com.craftapps.remotehorticulture.app.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.craftapps.remotehorticulture.app.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {

    View rootView;
    private XYPlot plot;
    ProgressDialog progressDialog;
    final List<Double> parseSeries = new ArrayList<Double>();

    public OverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.main, menu);

        MenuItem mTemperatureMenuItem = menu.findItem(R.id.action_temperature);
        MenuItem mHumidityMenuItem = menu.findItem(R.id.action_humidity);
        MenuItem mLightingMenuItem = menu.findItem(R.id.action_lighting);
        MenuItem mWaterMenuItem = menu.findItem(R.id.action_water);

        mTemperatureMenuItem.setVisible(false);
        mHumidityMenuItem.setVisible(false);
        mLightingMenuItem.setVisible(false);
        mWaterMenuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_temperature) {
            Toast.makeText(getActivity(), "Temperature action.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_overview, container, false);
        plot = (XYPlot) rootView.findViewById(R.id.mySimpleXYPlot);

        getItemLists gfl = new getItemLists();
        gfl.execute();

        return rootView;
    }

    //--------------------------------------- ASYNC TASK ----------------------------------------------
    private class getItemLists extends
            AsyncTask<Void, String, List<Double>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            plot.setVisibility(View.INVISIBLE);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please Wait..");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<Double> doInBackground(final Void... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Temperature");
            query.orderByAscending("updatedAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> tempList, ParseException e) {
                    if (e == null) {
                        for (ParseObject temp : tempList) {
                            Log.i("query", "= " + temp.getNumber("Temperature"));
                            parseSeries.add(temp.getDouble("Temperature"));
                        }
                        setupGraph();
                    } else {
                        Log.i("error", ": findInBackground");
                    }
                }
            });

            try { Thread.sleep(500); }
            catch (InterruptedException e) { e.printStackTrace(); }

            return parseSeries;
        }

        @Override
        protected void onPostExecute(List<Double> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            plot.setVisibility(View.VISIBLE);
        }
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

}
