package com.craftapps.remotehorticulture.app.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craftapps.remotehorticulture.app.R;
import it.gmariotti.cardslib.library.internal.Card;

public class DateCard extends Card {

    protected TextView mDate;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public DateCard(Context context) {
        this(context, R.layout.card_date);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public DateCard(Context context, int innerLayout) {
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
        mDate = (TextView) parent.findViewById(R.id.textView_Date);

        if (mDate !=null)
            mDate.setText("00:00 AM");

    }

    public void setDate(String date) {
        if (date != null)
            mDate.setText(date);
    }
}