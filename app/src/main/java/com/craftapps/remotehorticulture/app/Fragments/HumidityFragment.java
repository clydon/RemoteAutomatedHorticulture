package com.craftapps.remotehorticulture.app.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;

public class HumidityFragment extends Fragment {

    public HumidityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_humidity, container, false);
        final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) rootView.findViewById(R.id.verticalSeekBar);
        final TextView textView = (TextView) rootView.findViewById(R.id.textView2);

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
                textView.setText(String.valueOf(progress));

            }
        });

        return rootView;
    }

}
