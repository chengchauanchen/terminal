package cn.vsx.vc.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import cn.vsx.vc.R;

/**
 * Created by jamie on 2017/10/24.
 */

public class IndividualCallView extends LinearLayout {
    private String defaultText = "00:00";
    private TextView timerTextView;
    private Timer timer = new Timer();
    private int second = 0;
    private TimerTask timerTask;
    private Handler myHandler = new Handler();
    public IndividualCallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.individual_call_view,this);

        timerTextView = (TextView)findViewById(R.id.call_timer);
    }

    public void start(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                second++;
                if(second >= 0){
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            timerTextView.setText(getTwoNumber(second/60)+":"+getTwoNumber(second%60));
                            invalidate();
                        }
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    public void start(int color) {
        start();
        timerTextView.setTextColor(color);
    }

    public void stop(){
        second = 0;
        myHandler.post(new Runnable() {

            @Override
            public void run() {
                timerTextView.setText(defaultText);
            }
        });
        if(timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
    }
    public void pause(){
        second = 0;
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                timerTextView.setText("00:00");
            }
        });
        if(timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
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
