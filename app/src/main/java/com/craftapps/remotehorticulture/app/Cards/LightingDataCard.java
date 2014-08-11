package com.craftapps.remotehorticulture.app.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class LightingDataCard extends Card {

    protected TextView mScheduleValue;
    protected android.widget.ToggleButton mToggleButton;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public LightingDataCard(Context context) {
        this(context, R.layout.card_lighting_header);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public LightingDataCard(Context context, int innerLayout) {
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
        mScheduleValue = (TextView) parent.findViewById(R.id.textView_lightingSchedule);
        mToggleButton = (android.widget.ToggleButton) parent.findViewById(R.id.toggleButton_cardLighting);

        if (mScheduleValue !=null)
            mScheduleValue.setText("");
        if (mToggleButton !=null)
            mToggleButton.setChecked(false);

    }

    public void setScheduleValue(int scheduleValue) {
        switch (scheduleValue){
            case 0:
                mScheduleValue.setText("AUTO");
                break;
            case 1:
                mScheduleValue.setText("OVER RIDE:\n" +
                        "LIGHTS ON");
                break;
            case 2:
                mScheduleValue.setText("OVER RIDE:\n" +
                        "LIGHTS OFF");
                break;
        }
    }

    public void setToggleValue(boolean value) {
        mToggleButton.setChecked(value);
    }

}