package cn.vsx.uav.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.R;
import cn.vsx.uav.activity.UavFileListActivity;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.receiveHandler.ReceiveShowPreViewHandler;
import cn.vsx.vc.activity.TransponActivity;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.TransponSelectedBean;
import cn.vsx.vc.model.TransponToBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.MyDataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static android.app.Activity.RESULT_OK;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/22
 * 描述：
 * 修订历史：
 */
public class PrePhotoFragment extends BaseFragment implements View.OnClickListener{

    private static final String FILE_BEAN = "fileBean";

    private ImageView mIvClose;
    private TextView mTvFileName;
    private TextView mTvUavForward;
    private ImageView mIvPhoto;
    private FileBean fileBean;

    public static PrePhotoFragment newInstance(FileBean fileBean){
         Bundle args = new Bundle();
         args.putParcelable(FILE_BEAN,fileBean);
         PrePhotoFragment fragment = new PrePhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_pre_photo;
    }

    @Override
    public void initView(){
        mIvClose = mRootView.findViewById(R.id.iv_close);
        mTvFileName = mRootView.findViewById(R.id.tv_file_name);
        mTvUavForward = mRootView.findViewById(R.id.tv_uav_forward);
        mIvPhoto = mRootView.findViewById(R.id.iv_photo);
    }

    @Override
    public void initListener(){
        mIvClose.setOnClickListener(this);
        mTvUavForward.setOnClickListener(this);
    }

    @Override
    public void initData(){
        if(getArguments() != null){
            fileBean = getArguments().getParcelable(FILE_BEAN);
            mTvFileName.setText(fileBean.getName());
            Glide.with(this)
                    .load(fileBean.getPath())
                    .into(mIvPhoto);
        }
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.iv_close){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowPreViewHandler.class,false,fileBean);
        }else if(v.getId() == R.id.tv_uav_forward){
            Intent intent = new Intent(getActivity(), TransponActivity.class);
            intent.putExtra(Constants.TRANSPON_TYPE, Constants.TRANSPON_TYPE_ONE);
            startActivityForResult(intent, UavFileListActivity.CODE_TRANSPON_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UavFileListActivity.CODE_TRANSPON_REQUEST){
            if(resultCode == RESULT_OK){
                //转发返回结果
                TransponSelectedBean bean = (TransponSelectedBean) data.getSerializableExtra(cn.vsx.vc.utils.Constants.TRANSPON_SELECTED_BEAN);
                if(bean != null && bean.getList() != null && !bean.getList().isEmpty()){
                    int type = data.getIntExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, cn.vsx.vc.utils.Constants.TRANSPON_TYPE_ONE);
                    if(type == Constants.TRANSPON_TYPE_ONE){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                        jsonObject.put(JsonParam.PICTURE_NAME, fileBean.getName());
                        jsonObject.put(JsonParam.PICTURE_SIZE, fileBean.getFileSize());
                        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
                        //                            jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                        TerminalMessage mTerminalMessage = new TerminalMessage();
                        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");

                        mTerminalMessage.messagePath = fileBean.getPath();
                        mTerminalMessage.sendTime = System.currentTimeMillis();
                        mTerminalMessage.messageType = MessageType.PICTURE.getCode();

                        mTerminalMessage.messageBody = jsonObject;
                        //单个转发
                        transponMessage(mTerminalMessage,bean.getList());
                    }
                }
            }
        }
    }

    public void transponMessage(TerminalMessage transponMessage, ArrayList<ContactItemBean> list) {

        logger.info("转发消息，type:" + transponMessage);
        //单个转发
        List<Integer> toIds = MyDataUtil.getToIdsTranspon(list);
        TransponToBean bean = MyDataUtil.getToNamesTranspon(list);
        List<Long> toUniqueNos = MyDataUtil.getToUniqueNoTranspon(list);
        if(bean!=null){
            transponMessage.messageToId = bean.getNo();
            transponMessage.messageToName = bean.getName();
        }
        transponMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        transponMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        transponMessage.messageBody.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());

        if (transponMessage.messageType == MessageType.PICTURE.getCode()) {
            transponPhotoMessage(transponMessage, toIds,toUniqueNos);
        }
    }

    /**
     * 转发图片消息
     **/
    private void transponPhotoMessage(TerminalMessage terminalMessage, List<Integer> list , List<Long> toUniqueNos) {
        terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        File file = new File(terminalMessage.messagePath);
        MyTerminalFactory.getSDK().upload(list,toUniqueNos, file, terminalMessage, true);
    }
}
