package cn.vsx.vc.adapter;

import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.vc.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zectec.imageandfileselector.utils.DateUtils;
import java.util.ArrayList;
import skin.support.content.res.SkinCompatResources;

public class VideoMeetingListAdapter extends BaseQuickAdapter<VideoMeetingMessage, BaseViewHolder> {

  private OnItemClickListerner onItemClickListerner;

  public VideoMeetingListAdapter() {
    super(R.layout.item_video_meeting_list, new ArrayList<VideoMeetingMessage>());
  }

  @Override
  protected void convert(BaseViewHolder helper, VideoMeetingMessage item) {
    if (item != null){
      if(item.isMeeting()){
        //进行中
        //图标
        helper.setImageResource(R.id.iv_user_photo, R.drawable.video_meeting);
        helper.setTextColor(R.id.tv_content, SkinCompatResources.getColor(mContext, R.color.video_meeting_text_name));
        helper.setGone(R.id.iv_watch,true);
        helper.setGone(R.id.tv_end,false);
      }else{
        //已经结束
        //图标
        helper.setImageResource(R.id.iv_user_photo, R.drawable.video_meeting_end);
        helper.setTextColor(R.id.tv_content, SkinCompatResources.getColor(mContext, R.color.video_meeting_text_time));
        helper.setGone(R.id.iv_watch,false);
        helper.setGone(R.id.tv_end,true);
      }
      //内容
      helper.setText(R.id.tv_content,String.format(mContext.getString(R.string.text_create_video_meeting_by_user),item.getCreateTerminalName()));
      //时间
      helper.setText(R.id.tv_time, DateUtils.getNewChatTime(item.getSendTime()));
      //点击：观看
      helper.setOnClickListener(R.id.iv_watch, v -> {
        if(onItemClickListerner!=null){
          onItemClickListerner.goToWatch(item);
        }
      });
    }
  }

  public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner){
    this.onItemClickListerner = onItemClickListerner;
  }


  public interface  OnItemClickListerner{
    void goToWatch(VideoMeetingMessage item);
  }
}
