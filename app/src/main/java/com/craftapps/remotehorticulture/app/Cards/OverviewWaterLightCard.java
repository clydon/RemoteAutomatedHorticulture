package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.GaugeView;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;

import it.gmariotti.cardslib.library.internal.Card;

public class OverviewWaterLightCard extends Card {

    protected TextView mWaterValue;
    protected TextView mLightValue;
    protected VerticalSeekBar mWaterVerticalSeekBar;
    protected VerticalSeekBar mLightVerticalSeekBar;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public OverviewWaterLightCard(Context context) {
        this(context, R.layout.card_overview_waterlight);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public OverviewWaterLightCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    /**
     * Init
     */
    private void init(){
        //No Header

        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                //Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mWaterValue = (TextView) parent.findViewById(R.id.textView_Water);
        mLightValue = (TextView) parent.findViewById(R.id.textView_Lighting);
        mWaterVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.seekBar_Water);
        mLightVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.seekBar_Lighting);

        if (mWaterValue !=null)
            mWaterValue.setText("0.0 %");
        if (mLightValue !=null)
            mLightValue.setText("");
        if (mWaterVerticalSeekBar !=null)
            mWaterVerticalSeekBar.setProgress(0);
        if (mLightVerticalSeekBar !=null)
            mLightVerticalSeekBar.setProgress(0);

    }

    public void setWater(final Number value) {
        final int water = value.intValue();

        mWaterValue.setText(value.toString() + " %");

        mWaterVerticalSeekBar.setProgress(water);
        mWaterVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(water);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(water);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(water);
            }
        });
    }

    public void setLighting(final boolean toggleValue, int scheduleValue) {
        switch (scheduleValue){
            case 0:
                mLightValue.setText("AUTO");
                break;
            case 1:
                mLightValue.setText("OVER RIDE ON");
                break;
            case 2:
                mLightValue.setText("OVER RIDE OFF");
                break;
        }

        mLightVerticalSeekBar.setProgress(toggleValue?100:0);
        mLightVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(toggleValue?100:0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(toggleValue?100:0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(toggleValue?100:0);
            }
        });
    }
}