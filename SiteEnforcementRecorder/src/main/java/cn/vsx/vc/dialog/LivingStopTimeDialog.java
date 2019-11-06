package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.vc.R;
import ptt.terminalsdk.tools.StringUtil;

public class LivingStopTimeDialog extends Dialog {
    private Context context;
    private OnClickListener mOnClickListener;

    public LivingStopTimeDialog(Context context, long livingTime,long surplusTime, OnClickListener mOnClickListener) {
		super(context, R.style.my_progress_dialog);
		this.context = context;
		this.mOnClickListener = mOnClickListener;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.living_stop_time_dialog, null);
        TextView  txTempt = (TextView)view.findViewById(R.id.tx_tempt);
        TextView btCancel = (TextView)view.findViewById(R.id.bt_cancel);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close);
        txTempt.setText(String.format(context.getString(R.string.text_tempt_living_stop_time),
                StringUtil.secondsToHour(livingTime),StringUtil.secondsToMinute(surplusTime)));
        ivClose.setOnClickListener(v -> {
            dismiss();
        });
        btCancel.setOnClickListener(v -> {
            if(mOnClickListener!=null){
                mOnClickListener.onRefuse();
            }
            dismiss();
        });
		setContentView(view);
		setCancelable(true);
    }



    public interface OnClickListener{
        public void onRefuse();
    }
}