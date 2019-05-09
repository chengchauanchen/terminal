package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualCallForAddressBookHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class SearchContactsAdapter extends BaseAdapter  {

	private Context context;
	private List<Member> searchMembersList;
	private int searchListPosition;
	private int activeMode;
	private int errorCode;
	private boolean interGroup;
	private String keyWords;

	private Handler handler = new Handler();

	public SearchContactsAdapter(Context context, List<Member> searchMembersList, int searchListPosition) {
		this.context = context;
		this.searchMembersList = searchMembersList;
		this.searchListPosition = searchListPosition;
	}

	public void refreshSearchContactsAdapter(int activeMode, int searchListPosition, int errorCode) {
		this.searchListPosition = searchListPosition;
		this.activeMode = activeMode;
		this.errorCode = errorCode;
		handler.post(() -> notifyDataSetChanged());
	}

	@Override
	public int getCount() {
		return searchMembersList.size();
	}

	@Override
	public Object getItem(int position) {
		return searchMembersList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_search_contacts, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		final Member searchContactsBean = searchMembersList.get(position);
		String name = searchContactsBean.getName();
		String id = HandleIdUtil.handleId(searchContactsBean.id);
		if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {

			int index = name.toLowerCase().indexOf(keyWords.toLowerCase());

			int len = keyWords.length();

			Spanned temp = Html.fromHtml(name.substring(0, index)
					+ "<u><font color=#eb403a>"
					+ name.substring(index, index + len) + "</font></u>"
					+ name.substring(index + len, name.length()));

			holder.tv_member_name.setText(temp);
		} else {
			holder.tv_member_name.setText(name);
		}
		if (!Util.isEmpty(id) && !Util.isEmpty(keyWords) && id.contains(keyWords)) {

			int index = id.indexOf(keyWords);

			int len = keyWords.length();

			Spanned temp = Html.fromHtml(id.substring(0, index)
					+ "<u><font color=#eb403a>"
					+ id.substring(index, index + len) + "</font></u>"
					+ id.substring(index + len, id.length()));

			holder.tv_member_id.setText(temp);
		} else {
			holder.tv_member_id.setText(id);
		}

		if(interGroup) {
			holder.iv_search_msg.setVisibility(View.VISIBLE);
		}
		else {
			holder.iv_search_msg.setVisibility(View.GONE);
		}
		if (DataUtil.isExistContacts(searchContactsBean)) {//当要显示五角星时，判断是否已经在个呼通讯录中；
			holder.iv_search_add_remove.setBackgroundResource(R.drawable.select_b);
		}else {
			holder.iv_search_add_remove.setBackgroundResource(R.drawable.popupwindow_add_contacts_gray);
		}

		if (searchContactsBean.id == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)) {
			holder.tv_me.setVisibility(View.VISIBLE);
			holder.iv_search_add_remove.setVisibility(View.GONE);
			holder.iv_search_call.setVisibility(View.GONE);
			holder.iv_search_msg.setVisibility(View.GONE);
		}
		else {
			holder.tv_me.setVisibility(View.GONE);
			holder.iv_search_add_remove.setVisibility(View.VISIBLE);
			holder.iv_search_call.setVisibility(View.VISIBLE);
			holder.iv_search_msg.setVisibility(View.VISIBLE);
		}

		if (TerminalMemberType.TERMINAL_PDT.getCode()== searchContactsBean.getType()||searchContactsBean.id == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
			holder.llDialTo.setVisibility(View.GONE);
		}else {
			holder.llDialTo.setVisibility(View.VISIBLE);
		}


		holder.iv_search_call.setOnClickListener(view -> OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualCallForAddressBookHandler.class, 2, position));
		holder.iv_search_msg.setOnClickListener(v -> {
			Intent intent = new Intent(context, IndividualNewsActivity.class);
			intent.putExtra("isGroup", false);
			intent.putExtra("userId", searchContactsBean.id);
			intent.putExtra("userName", searchContactsBean.getName());
			context.startActivity(intent);
		});

		holder.llDialTo.setOnClickListener(view -> {
			if (!TextUtils.isEmpty(searchContactsBean.phone)) {
				CallPhoneUtil.callPhone((Activity) context, searchContactsBean.phone);
			}else {
				ToastUtil.showToast(context,context.getString(R.string.text_has_no_member_phone_number));
			}
		});
		holder.iv_member_portrait.setOnClickListener(view -> {
			Intent intent = new Intent(context, UserInfoActivity.class);
			intent.putExtra("userId", searchContactsBean.getNo());
			intent.putExtra("userName", searchContactsBean.getName());
			context.startActivity(intent);
		});

		return convertView;
	}

	public void setFilterKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public void setInterGroup(boolean interGroup) {
		this.interGroup = interGroup;
	}

	public static class ViewHolder {
		@Bind(R.id.iv_member_portrait)
		ImageView iv_member_portrait;
		@Bind(R.id.tv_member_name)
		TextView tv_member_name;
		@Bind(R.id.tv_member_id)
		TextView tv_member_id;
		@Bind(R.id.iv_search_call)
		LinearLayout iv_search_call;
		@Bind(R.id.iv_search_msg)
		LinearLayout iv_search_msg;
		@Bind(R.id.iv_search_add_remove)
		ImageView iv_search_add_remove;
		@Bind(R.id.ll_item_search_contacts)
		LinearLayout ll_item_search_contacts;
		@Bind(R.id.ll_search_add_remove)
		LinearLayout ll_search_add_remove;
		@Bind(R.id.me)
		TextView tv_me;
		@Bind(R.id.shoutai_dial_to)
		LinearLayout llDialTo;

		public ViewHolder(View rootView) {
			ButterKnife.bind(this,rootView);
		}
	}


}
