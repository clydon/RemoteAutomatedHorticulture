package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;

import it.gmariotti.cardslib.library.internal.Card;

public class OverviewDataCard extends Card {

    protected TextView mTempValue;
    protected TextView mHumidValue;
    protected VerticalSeekBar mTempVerticalSeekBar;
    protected VerticalSeekBar mHumidVerticalSeekBar;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public OverviewDataCard(Context context) {
        this(context, R.layout.card_overview_header);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public OverviewDataCard(Context context, int innerLayout) {
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
        mTempValue = (TextView) parent.findViewById(R.id.textView_Temp);
        mHumidValue = (TextView) parent.findViewById(R.id.textView_Humid);
        mTempVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.seekBar_Temperature);
        mHumidVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.seekBar_Humidity);

        if (mTempValue !=null)
            mTempValue.setText("0.0° F");
        if (mHumidValue !=null)
            mHumidValue.setText("0.0 %");
        if (mTempVerticalSeekBar !=null)
            mTempVerticalSeekBar.setProgress(0);
        if (mHumidVerticalSeekBar !=null)
            mHumidVerticalSeekBar.setProgress(0);
    }


    public void setTemp(final Number value) {
        final int temp = value.intValue();

        mTempValue.setText(value.toString() + "° F");

        mTempVerticalSeekBar.setProgress(temp);
        mTempVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(temp);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(temp);
            }
        });
    }

    public void setHumid(final Number value) {
        final int humid = value.intValue();

        mHumidValue.setText(value.toString() +  "%");

        mHumidVerticalSeekBar.setProgress(humid);
        mHumidVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress(humid);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(humid);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(humid);
            }
        });
    }

}