package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class LightingScheduleCard extends Card {

    protected TextView mLightsOff;
    protected TextView mLightsOn;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public LightingScheduleCard(Context context) {
        this(context, R.layout.card_lighting_schedule);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public LightingScheduleCard(Context context, int innerLayout) {
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
        mLightsOff = (TextView) parent.findViewById(R.id.textView_lightsOff);
        mLightsOn = (TextView) parent.findViewById(R.id.textView_lightsOn);

        if (mLightsOn !=null)
            mLightsOn.setText("00:00 AM");
        if (mLightsOff !=null)
            mLightsOff.setText("00:00 PM");

    }

    public void setTime(String lightsOn, String lightsOff) {
        if (lightsOn != null && lightsOff != null) {
            mLightsOff.setText(lightsOff);
            mLightsOn.setText(lightsOn);
        }
    }

}