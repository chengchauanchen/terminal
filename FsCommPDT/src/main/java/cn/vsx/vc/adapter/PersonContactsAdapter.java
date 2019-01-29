package cn.vsx.vc.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualCallForAddressBookHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualMsgForAddressBookHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.HandleIdUtil;

public class PersonContactsAdapter extends BaseAdapter {

	private Context context;
	private List<Member> allMembersExceptMe;
	private Logger logger = Logger.getLogger(PersonContactsAdapter.class);
    private Member memberExceptMe;// 上一个人
	Handler handler = new Handler();
	private int longClickPos = -1;
    public PersonContactsAdapter(Context context, List<Member> allMembersExceptMe) {
        this.context = context;
        this.allMembersExceptMe = allMembersExceptMe;
    }

	@Override
	public int getCount() {
		return allMembersExceptMe.size();
	}

	@Override
	public Object getItem(int position) {
		return allMembersExceptMe.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.fragment_person_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        
		Member personContactsBean = allMembersExceptMe.get(position);//当前位置的人
		// 获取上一个人
		if (position== 0){
			memberExceptMe = null;
		}else{
			memberExceptMe = allMembersExceptMe.get(position - 1);
		}
		// 当前人  拼音的首字母  和 上一个人的拼音的首字母  如果相同  隐藏
		boolean isShow = true; // 是否显示拼音
		if ( memberExceptMe == null){
			isShow = true;
		}else{
			logger.info(personContactsBean.pinyin);

			if (personContactsBean.pinyin.charAt(0)  == memberExceptMe.pinyin.charAt(0)){
				isShow = false;
			}
		}
		holder.tv_pinyin.setVisibility(isShow ? View.VISIBLE : View.GONE);
		holder.tv_pinyin.setText(TextUtils.isEmpty(personContactsBean.pinyin) ? "#" : personContactsBean.pinyin.charAt(0)+"");
		holder.tv_member_name.setText(personContactsBean.getName());
		holder.tv_member_id.setText(HandleIdUtil.handleId(personContactsBean.id));
		holder.iv_individual_call.setOnClickListener(view -> OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualCallForAddressBookHandler.class, 1, position));
		holder.iv_individual_msg.setOnClickListener(v -> OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualMsgForAddressBookHandler.class, 1, position));

		if (longClickPos == position) {
			holder.ll_person_search_item.setBackgroundResource(R.color.group_person_catagory_gray);
		}
		else {
			holder.ll_person_search_item.setBackgroundResource(R.color.white);
		}

        return convertView;
	}

	public static class ViewHolder {
		@Bind(R.id.ll_person_search_item)
		LinearLayout ll_person_search_item;
		@Bind(R.id.catagory)
		LinearLayout ll_item_person_contacts;
        @Bind(R.id.tv_catagory)
        TextView tv_pinyin;
        @Bind(R.id.tv_member_name)
        TextView tv_member_name;
        @Bind(R.id.tv_member_id)
        TextView tv_member_id;
        @Bind(R.id.message_to)
		LinearLayout iv_individual_msg;
		@Bind(R.id.call_to)
		LinearLayout iv_individual_call;
        public ViewHolder(View rootView) {
        	ButterKnife.bind(this,rootView);
        }
    }
	
}
