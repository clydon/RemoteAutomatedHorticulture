package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class WaterScheduleCard extends Card {

    protected TextView mWaterEveryHour;
    protected TextView mWaterDuration;
    protected TextView mNextFeeding;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public WaterScheduleCard(Context context) {
        this(context, R.layout.card_water_schedule);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public WaterScheduleCard(Context context, int innerLayout) {
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
        mWaterDuration = (TextView) parent.findViewById(R.id.textView_waterDuration);
        mWaterEveryHour = (TextView) parent.findViewById(R.id.textView_waterEvery);
//        mNextFeeding = (TextView) parent.findViewById(R.id.textView_waterNextFeeding);

        if (mWaterDuration !=null)
            mWaterDuration.setText("00 mins");
        if (mWaterEveryHour !=null)
            mWaterEveryHour.setText("00 hrs");
        if (mNextFeeding !=null)
            mNextFeeding.setText("12:00 PM");

    }

    public void setDuration(String waterDuration) {
        if (waterDuration != null) {
            mWaterDuration.setText(waterDuration + " mins");
        }
    }

    public void setWaterEvery(String waterEvery) {
        if (waterEvery != null) {
            mWaterEveryHour.setText(waterEvery);
        }
    }

    public void setNextFeeding(String nextFeeding) {
        if (nextFeeding != null) {
            mNextFeeding.setText(nextFeeding);
        }
    }

}