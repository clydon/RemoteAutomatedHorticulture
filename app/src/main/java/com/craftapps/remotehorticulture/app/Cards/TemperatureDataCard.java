package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import com.craftapps.remotehorticulture.app.widgets.VerticalSeekBar;

import it.gmariotti.cardslib.library.internal.Card;

public class TemperatureDataCard extends Card {

    protected TextView mValue;
    protected VerticalSeekBar mVerticalSeekBar;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public TemperatureDataCard(Context context) {
        this(context, R.layout.card_temperature_header);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public TemperatureDataCard(Context context, int innerLayout) {
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
        mValue = (TextView) parent.findViewById(R.id.textView_TempValue);
        mVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.verticalSeekBar);

        if (mValue !=null)
            mValue.setText("0.0° F");
        if (mVerticalSeekBar !=null)
            mVerticalSeekBar.setProgress(0);

    }

    public void setValue(Number value) {
        if (value != null) {
            mValue.setText(value.toString() + "° F");
        }
    }

    public void setSeekBar(final Number value) {
        final int temp = value.intValue();
        mVerticalSeekBar.setProgress(temp);
        mVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

}