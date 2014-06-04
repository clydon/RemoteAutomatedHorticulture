package com.craftapps.remotehorticulture.app.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

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
                ParseObject gameScore = new ParseObject("Temperature");
                gameScore.put("Temperature", Integer.parseInt(String.valueOf(verticalSeekBar.getProgress())));
                gameScore.saveInBackground(new SaveCallback() {
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
                            textView2.setText((String.valueOf(object.getInt("Temperature"))));
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

}
