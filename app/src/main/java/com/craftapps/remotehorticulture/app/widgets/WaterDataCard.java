package com.craftapps.remotehorticulture.app.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class WaterDataCard extends Card {

    protected TextView mValue;
    protected GaugeView mGaugeView;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public WaterDataCard(Context context) {
        this(context, R.layout.card_date);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public WaterDataCard(Context context, int innerLayout) {
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
        mValue = (TextView) parent.findViewById(R.id.textView_waterValue);
        mGaugeView = (GaugeView) parent.findViewById(R.id.gaugeView);

        if (mValue !=null)
            mValue.setText("0.0%");
        if (mGaugeView !=null)
            mGaugeView.setTargetValue(0);

    }

    public void setValue(Number value) {
        if (value != null)
            mValue.setText(value.toString() + "%");
    }

    public void setGauge(float value) {
        mGaugeView.setTargetValue(value);
    }
}