package com.craftapps.remotehorticulture.app.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class HumidityDataCard extends Card {

    protected TextView mValue;
    protected VerticalSeekBar mVerticalSeekBar;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public HumidityDataCard(Context context) {
        this(context, R.layout.card_humidity_header);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public HumidityDataCard(Context context, int innerLayout) {
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
        mValue = (TextView) parent.findViewById(R.id.textView_HumidValue);
        mVerticalSeekBar = (VerticalSeekBar) parent.findViewById(R.id.verticalSeekBar);

        if (mValue !=null)
            mValue.setText("0.0%");
        if (mVerticalSeekBar !=null)
            mVerticalSeekBar.setProgress(0);

    }

    public void setValue(Number value) {
        if (value != null) {
            mValue.setText(value.toString() + "%");
        }
    }

    public void setSeekBar(final Number value) {
        final int humid = value.intValue();
        mVerticalSeekBar.setProgress(humid);
        mVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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