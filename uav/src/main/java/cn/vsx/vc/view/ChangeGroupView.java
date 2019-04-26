package cn.vsx.vc.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

public class ChangeGroupView extends FrameLayout{
	private Logger logger = Logger.getLogger(getClass());
	private TextView left1;
	private TextView left2;
	private TextView middle;
	private TextView right2;
	private TextView right1;
	private List<TextView> textViewList = new ArrayList<>();
	private int currentDataIndex;
	private int width;
	private int height;
	private int textViewRadius;
	private float edge1Scale = 4.0f/6;
	private float edge2Scale = 5.0f/6;
	private static final int LEFT_ONE_COMMAND = 1;
	private static final int RIGHT_ONE_COMMAND = 2;
	private BlockingQueue<Integer> commandQueue = new LinkedBlockingQueue<>();
	private final int maxCommandTimes = 5;
	private int dataIndexSubtractionViewIndex = Integer.MAX_VALUE;
	private List<Group> data = new ArrayList<>();
	protected OnGroupChangedListener onGroupChangedListener;

	private Runnable worker = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					int command = commandQueue.take();
					switch (command) {
						case LEFT_ONE_COMMAND:
							ChangeGroupView.this.post(() -> leftOne());
							break;

						case RIGHT_ONE_COMMAND:
							ChangeGroupView.this.post(() -> rightOne());
							break;
						default:
							break;
					}
					synchronized (worker) {
						worker.wait();
					}
					if(commandQueue.size() == 0 && onGroupChangedListener != null){
						onGroupChangedListener.onGroupChanged(getGroupData(currentDataIndex).id, getGroupData(currentDataIndex).name);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public ChangeGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.change_group_view,this);
//		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
//			@Override
//			public boolean onPreDraw() {
//				getViewTreeObserver().removeOnPreDrawListener(this);
//				width = getMeasuredWidth();
//				height = getMeasuredHeight();
//				textViewRadius = middle.getWidth()/2;
//
//				middle.setTranslationX(width/2-textViewRadius);
//				left2.setTranslationX(middle.getTranslationX() - 3f*textViewRadius);
//				right2.setTranslationX(middle.getTranslationX() + 3f*textViewRadius);
//				left1.setTranslationX(middle.getTranslationX() - 5.5f*textViewRadius);
//				right1.setTranslationX(middle.getTranslationX() + 5.5f*textViewRadius);
//				left1.setScaleX(edge1Scale);
//				left1.setScaleY(edge1Scale);
//				left2.setScaleX(edge2Scale);
//				left2.setScaleY(edge2Scale);
//				right1.setScaleX(edge1Scale);
//				right1.setScaleY(edge1Scale);
//				right2.setScaleX(edge2Scale);
//				right2.setScaleY(edge2Scale);
//				return true;
//			}
//		});
		left1 = (TextView)findViewById(R.id.left1);
		left2 = (TextView)findViewById(R.id.left2);
		middle = (TextView)findViewById(R.id.middle);
		right2 = (TextView)findViewById(R.id.right2);
		right1 = (TextView)findViewById(R.id.right1);
		textViewList.add(left1);
		textViewList.add(left2);
		textViewList.add(middle);
		textViewList.add(right2);
		textViewList.add(right1);

//		new Thread(worker).start();
		MyTerminalFactory.getSDK().getThreadPool().execute(worker);
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (width == 0) {
			//获取MyTextView当前实例的高
			height = this.getHeight();
			//获取MyTextView当前实例的宽
			width = this.getWidth();

			logger.info("ChangeGroupView ------> width = "+width+"   height = "+height);

			textViewRadius = middle.getWidth()/2;
			middle.setTranslationX(width/2-textViewRadius);
			left2.setTranslationX(middle.getTranslationX() - 4f*textViewRadius);
			right2.setTranslationX(middle.getTranslationX() + 4f*textViewRadius);
			left1.setTranslationX(middle.getTranslationX() - 8f*textViewRadius);
			right1.setTranslationX(middle.getTranslationX() + 8f*textViewRadius);
//			left2.setTranslationX(middle.getTranslationX() - 3f*textViewRadius);
//			right2.setTranslationX(middle.getTranslationX() + 3f*textViewRadius);
//			left1.setTranslationX(middle.getTranslationX() - 5.5f*textViewRadius);
//			right1.setTranslationX(middle.getTranslationX() + 5.5f*textViewRadius);
//			left1.setScaleX(edge1Scale);
//			left1.setScaleY(edge1Scale);
//			left2.setScaleX(edge2Scale);
//			left2.setScaleY(edge2Scale);
//			right1.setScaleX(edge1Scale);
//			right1.setScaleY(edge1Scale);
//			right2.setScaleX(edge2Scale);
//			right2.setScaleY(edge2Scale);
		}
	}
	public void setData(List<Group> groups, int currentGroupId){
		data.clear();

		data.addAll(groups);

		int oldCurrentDataIndex = currentDataIndex;
		for(int i = 0 ; i < data.size() ; i++){
			if(data.get(i).id == currentGroupId){
				currentDataIndex = i;
				break;
			}
		}
		if(dataIndexSubtractionViewIndex == Integer.MAX_VALUE){
			dataIndexSubtractionViewIndex = currentDataIndex - textViewList.size()/2;
		}
		else{
			dataIndexSubtractionViewIndex = currentDataIndex - oldCurrentDataIndex + dataIndexSubtractionViewIndex;
		}

		resetData();
	}

	//	private void resetData(){
//		if(data.size() > 0){
//			for(int i = 0 ; i < textViewList.size() ; i++){
//				getTextView(currentDataIndex-dataIndexSubtractionViewIndex-textViewList.size()/2+i).setText(getSeqData(currentDataIndex-textViewList.size()/2+i).toString());
//			}
//		}
//	}
	private void resetData(){
		if(data.size() > 0){
			for(int i = 0 ; i < textViewList.size() ; i++){
				TextView tv = getTextView(currentDataIndex-dataIndexSubtractionViewIndex-textViewList.size()/2+i);
//				tv.setText(getSeqData(currentDataIndex-textViewList.size()/2+i).toString());
				if(i == 2){
//					tv.setTextColor(Color.parseColor("#0090ff"));
					tv.setBackgroundResource(R.drawable.change_group_circle_shape_middle);
				}else{
//					tv.setTextColor(Color.WHITE);
					tv.setBackgroundResource(R.drawable.change_group_circle_shape);
				}
			}
		}
	}



	private String getSeqData(int index){
		return (getDataRealIndex(index) + 1) + "";
	}

	private Group getGroupData(int index){
		return data.get(getDataRealIndex(index));
	}

	private int getDataRealIndex(int index){
		int seq = -1;
		if (data.size() > 0) {
			if (index < 0) {
				seq = index % data.size() == 0 ? 0 : data.size() + index
						% data.size();
			} else if (index >= data.size()) {
				seq = index % data.size();
			} else {
				seq = index;
			}
		}
		return seq;
	}

	private TextView getTextView(int index){
		if(index < 0){
			int check = index%textViewList.size();
			return textViewList.get(check==0?0:textViewList.size()+check);
		}
		else if(index >= textViewList.size()){
			return textViewList.get(index%textViewList.size());
		}
		else{
			return textViewList.get(index);
		}
	}

	private void addCommand(int times, int command){
		if(times > maxCommandTimes){
			times = maxCommandTimes;
		}
		else if(times <= 0){
			return;
		}
		int ramin = maxCommandTimes - commandQueue.size();
		if(ramin >= times){
			for(int i = 0 ; i < times ; i++){
				commandQueue.add(command);
			}
		}
		else{
			for(int i = 0 ; i < times - ramin ; i++){
				commandQueue.poll();
			}
			for(int i = 0 ; i < times ; i++){
				commandQueue.add(command);
			}
		}
	}

	public void addLeft(int times){
		addCommand(times, LEFT_ONE_COMMAND);
	}

	public void addRight(int times){
		addCommand(times, RIGHT_ONE_COMMAND);
	}

	private void animator2NewOne(final int command){
		AnimatorSet animatorSet = new AnimatorSet();
		for(int i = 0 ; i < textViewList.size() ; i++){
			TextView textView = textViewList.get(i);
			TextView newTextView;
			if(command == LEFT_ONE_COMMAND){
				newTextView = leftTextView(textView);
			}
			else if(command == RIGHT_ONE_COMMAND){
				newTextView = rightTextView(textView);
			}
			else{
				throw new IllegalArgumentException("不支持的command："+command);
			}
			ObjectAnimator oa1 = ObjectAnimator.ofFloat(textView, "translationX", newTextView.getTranslationX());
			ObjectAnimator oa3 = ObjectAnimator.ofFloat(textView, "scaleX", newTextView.getScaleX());
			ObjectAnimator oa4 = ObjectAnimator.ofFloat(textView, "scaleY", newTextView.getScaleY());
			animatorSet.playTogether(oa1, oa3, oa4);
		}
		animatorSet.setDuration(200);
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if(command == LEFT_ONE_COMMAND){
					currentDataIndex++;
					resetData();
				}
				else if(command == RIGHT_ONE_COMMAND){
					currentDataIndex--;
					resetData();
				}
				synchronized (worker) {
					worker.notify();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				synchronized (worker) {
					worker.notify();
				}
			}
		});
		animatorSet.start();
	}

	private void leftOne(){
		animator2NewOne(LEFT_ONE_COMMAND);
	}

	private void rightOne(){
		animator2NewOne(RIGHT_ONE_COMMAND);
	}

	private TextView leftTextView(TextView textView){
		int index = textViewList.indexOf(textView);
		if(index == 0){
			return textViewList.get(textViewList.size()-1);
		}
		else{
			return textViewList.get(index-1);
		}
	}

	private TextView rightTextView(TextView textView){
		int index = textViewList.indexOf(textView);
		if(index == textViewList.size()-1){
			return textViewList.get(0);
		}
		else{
			return textViewList.get(index+1);
		}
	}

	public void setOnGroupChangedListener(OnGroupChangedListener onGroupChangedListener) {
		this.onGroupChangedListener = onGroupChangedListener;
	}

	public interface OnGroupChangedListener{
		void onGroupChanged(int groupId, String groupName);
	}
}
