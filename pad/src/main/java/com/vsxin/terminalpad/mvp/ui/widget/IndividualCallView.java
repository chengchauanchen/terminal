package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vsxin.terminalpad.R;

/**
 * Created by jamie on 2017/10/24.
 */

public class IndividualCallView extends LinearLayout {
    private TextView timerTextView;
    private int second = 0;
    private static final int UPDATE_TIME = 1;
    private static final int UPDATE_PERIOD = 1000;
    @SuppressWarnings("handlerLeak")
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_TIME:
                    myHandler.removeMessages(UPDATE_TIME);
                    second++;
                    timerTextView.setText(getTwoNumber(second/60)+":"+getTwoNumber(second%60));
                    invalidate();
                    myHandler.sendEmptyMessageDelayed(UPDATE_TIME,UPDATE_PERIOD);
                break;
            }
            super.handleMessage(msg);
        }
    };
    public IndividualCallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.individual_call_view,this);
        timerTextView = (TextView)findViewById(R.id.call_timer);
    }

    public void onStart(){
        second = 0;
        myHandler.sendEmptyMessageDelayed(UPDATE_TIME,UPDATE_PERIOD);
    }

    public void onStart(int color) {
        onStart();
        timerTextView.setTextColor(color);
    }

    public void onStop(){
        second = 0;
        myHandler.removeMessages(UPDATE_TIME);
        String defaultText = "00:00";
        timerTextView.setText(defaultText);
    }

    public void onPause(){
        myHandler.removeMessages(UPDATE_TIME);
    }

    public void onContinue(){
        myHandler.sendEmptyMessageDelayed(UPDATE_TIME,UPDATE_PERIOD);
    }

    private String getTwoNumber(int number){
        if(number <= 0){
            return "00";
        }
        else if(number < 10){
            return "0" + number;
        }
        else if(number < 100){
            return "" + number;
        }
        else{
            return "" + number%100;
        }
    }
}
