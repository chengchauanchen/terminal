package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.utils.HandleIdUtil;

public class TempGroupSearchAdapter extends BaseAdapter {

	private Context context;
	private List<Member> searchMembersList;
	private int searchListPosition;
	private int activeMode;
	private int errorCode;
	private boolean interGroup;
	private String keyWords;

	private Handler handler = new Handler();

	public TempGroupSearchAdapter(Context context, List<Member> searchMembersList, int searchListPosition) {
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
		SearchContactsAdapter.ViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.fragment_temp_group_item, null);
			holder = new SearchContactsAdapter.ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (SearchContactsAdapter.ViewHolder) convertView.getTag();
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
