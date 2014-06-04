package com.craftapps.remotehorticulture.app.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.Format;
import java.text.SimpleDateFormat;

public class TemperatureFragment extends Fragment {

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

        Button button = (Button) rootView.findViewById(R.id.button);
        Button button2 = (Button) rootView.findViewById(R.id.button2);
        final EditText editText = (EditText) rootView.findViewById(R.id.editText);
        final TextView textView2 = (TextView) rootView.findViewById(R.id.textView2);
        final TextView textView3 = (TextView) rootView.findViewById(R.id.textView3);

        final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) rootView.findViewById(R.id.verticalSeekBar);

        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                verticalSeekBar.setProgress(progress);
                editText.setText(String.valueOf(progress));

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                ParseObject temperature = new ParseObject("Temperature");
                temperature.put("Temperature", Float.parseFloat(String.valueOf(verticalSeekBar.getProgress())));
                temperature.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            editText.setText("");
                        } else {
                            Log.d("Temperature", "The save request failed.");
                        }
                    }
                });
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Temperature");
                query.orderByDescending("updatedAt");
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (object == null) {
                            Log.d("Temperature", "The getFirst request failed.");
                        } else {
                            textView2.setText((String.valueOf(object.getNumber("Temperature"))));
                        }
                    }
                });

                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Temperature");
                query2.orderByDescending("updatedAt");
                query2.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (object == null) {
                            Log.d("Temperature", "The getFirst request failed.");
                        } else {
                            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String s = formatter.format(object.getUpdatedAt());
                            textView3.setText(s);
                        }
                    }
                });
            }
        });


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

        mTemperatureMenuItem.setVisible(true);
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


}
