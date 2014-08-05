package com.craftapps.remotehorticulture.app.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class TemperatureMinMaxCard extends Card {

    protected TextView mMinValue;
    protected TextView mMaxValue;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public TemperatureMinMaxCard(Context context) {
        this(context, R.layout.card_temperature_minmax);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public TemperatureMinMaxCard(Context context, int innerLayout) {
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
        mMinValue = (TextView) parent.findViewById(R.id.textView_minTemp);
        mMaxValue = (TextView) parent.findViewById(R.id.textView_maxTemp);

        if (mMinValue !=null)
            mMinValue.setText("0.0° F");
        if (mMaxValue !=null)
            mMaxValue.setText("0.0° F");

    }

    public void setValues(Number min, Number max) {
        if (min != null && max != null) {
            mMinValue.setText(min.toString() + "° F");
            mMaxValue.setText(max.toString() + "° F");
        }
    }

}