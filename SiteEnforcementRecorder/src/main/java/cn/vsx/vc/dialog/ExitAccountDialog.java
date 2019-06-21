package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import cn.vsx.vc.R;

public class ExitAccountDialog extends Dialog {
    private Context context;
    private LinearLayout llMoveTaskToBack;
    private LinearLayout llExitAccount;
    private OnClickListener mOnClickListener;

    public ExitAccountDialog(Context context,OnClickListener mOnClickListener) {
		super(context, R.style.exit_account_dialog);
		this.context = context;
		this.mOnClickListener = mOnClickListener;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.exit_account_dialog, null);
        llMoveTaskToBack = (LinearLayout)view.findViewById(R.id.ll_move_task_to_back);
        llExitAccount = (LinearLayout) view.findViewById(R.id.ll_exit_account);
        llMoveTaskToBack.setOnClickListener(v -> {
            if(mOnClickListener!=null){
                mOnClickListener.onMoveTaskToBack();
            }
            dismiss();
        });
        llExitAccount.setOnClickListener(v -> {
            if(mOnClickListener!=null){
                mOnClickListener.onExitAccount();
            }
            dismiss();
        });
		setContentView(view);
		setCancelable(true);
    }



    public interface OnClickListener{
        public void onMoveTaskToBack();
        public void onExitAccount();
    }
}