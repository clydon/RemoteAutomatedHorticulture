package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class HumidityMinMaxCard extends Card {

    protected TextView mMinValue;
    protected TextView mMaxValue;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public HumidityMinMaxCard(Context context) {
        this(context, R.layout.card_humidity_minmax);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public HumidityMinMaxCard(Context context, int innerLayout) {
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
        mMinValue = (TextView) parent.findViewById(R.id.textView_minHumid);
        mMaxValue = (TextView) parent.findViewById(R.id.textView_maxHumid);

        if (mMinValue !=null)
            mMinValue.setText("0.00%");
        if (mMaxValue !=null)
            mMaxValue.setText("0.00%");

    }

    public void setValues(Number min, Number max) {
        if (min != null && max != null) {
            mMinValue.setText(min.toString() + "%");
            mMaxValue.setText(max.toString() + "%");
        }
    }

}