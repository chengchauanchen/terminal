package cn.vsx.vc.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/5/18
 * 描述：
 * 修订历史：
 */

public class TemporaryGroupView extends LinearLayout{

    private LayoutInflater inflater;

    public TemporaryGroupView(@NonNull Context context){
        super(context);
        initView(context);
    }

    public TemporaryGroupView(@NonNull Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        initView(context);
    }

    public TemporaryGroupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        inflater = LayoutInflater.from(context);
        setOrientation(VERTICAL);


    }

    public void clearAllView(){
        int childCount = getChildCount();
        if(childCount > 1){
            removeAllViews();
        }
    }

    public void setTemporyGroup(List<Group> groupList){
        LinearLayout titleView = (LinearLayout) inflater.inflate(R.layout.tempory_group_title, null, false);
        addView(titleView);
        for(int i = 0; i < groupList.size(); i++){
            final Group group = groupList.get(i);
            View itemView = inflater.inflate(R.layout.tempory_group_item, null, false);
//            ImageView groupLogo = (ImageView) itemView.findViewById(R.id.iv_group_logo);
            TextView tvName = (TextView) itemView.findViewById(R.id.tv_name);
            TextView tvChangeGroup = (TextView) itemView.findViewById(R.id.tv_change_group);
            ImageView ivCurrentGroup = (ImageView) itemView.findViewById(R.id.iv_current_group);
            ImageView ivMessage = (ImageView) itemView.findViewById(R.id.iv_message);
            View bottom_diver = (View) itemView.findViewById(R.id.bottom_diver);

            addView(itemView);
            tvName.setText(group.getName());
            if (group.getNo()== MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)){
                tvChangeGroup.setVisibility(View.INVISIBLE);
//                ivMessage.setVisibility(View.INVISIBLE);
                ivCurrentGroup.setVisibility(View.VISIBLE);
            }else {
                tvChangeGroup.setVisibility(View.VISIBLE);
                ivMessage.setVisibility(View.VISIBLE);
                ivCurrentGroup.setVisibility(View.INVISIBLE);
            }
            if(i == groupList.size()-1){
                bottom_diver.setVisibility(VISIBLE);
            }else{
                bottom_diver.setVisibility(GONE);
            }

            ivMessage.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), GroupCallNewsActivity.class);


                getContext().startActivity(intent);
            });

            tvChangeGroup.setOnClickListener(view -> {
                MyTerminalFactory.getSDK().getGroupManager().changeGroup(group.getNo());
                requestLayout();
            });
        }
    }
}
