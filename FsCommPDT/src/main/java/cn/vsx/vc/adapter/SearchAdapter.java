package cn.vsx.vc.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;

/**
 * 通讯录组搜索adapter
 * Created by gt358 on 2017/10/21.
 */

public class SearchAdapter extends BaseMultiItemQuickAdapter<ContactItemBean, BaseViewHolder>{

    private OnItemClickListener onItemClickListener;
    private String keyWords;

    public SearchAdapter(List<ContactItemBean> mData){
        super(mData);
        addItemType(Constants.TYPE_CONTRACT_GROUP, R.layout.item_group_search);
        addItemType(Constants.TYPE_CONTRACT_MEMBER, R.layout.item_search_contacts);
        addItemType(Constants.TYPE_CONTRACT_PDT, R.layout.item_search_contacts);
        addItemType(Constants.TYPE_CHECK_SEARCH_GROUP, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_PC, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_POLICE, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_HDMI, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_UAV, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_RECODER, R.layout.layout_item_user);
    }

    public void setFilterKeyWords(String keyWords){
        this.keyWords = keyWords;
    }


    @Override
    protected void convert(BaseViewHolder holder, ContactItemBean item){
        switch(item.getType()){
            case Constants.TYPE_CONTRACT_GROUP:
                break;
            case Constants.TYPE_CONTRACT_MEMBER:
                break;
            case Constants.TYPE_CONTRACT_PDT:
                break;
            case Constants.TYPE_CHECK_SEARCH_GROUP:
                break;
            case Constants.TYPE_CHECK_SEARCH_PC:
            case Constants.TYPE_CHECK_SEARCH_POLICE:
            case Constants.TYPE_CHECK_SEARCH_RECODER:
            case Constants.TYPE_CHECK_SEARCH_HDMI:
            case Constants.TYPE_CHECK_SEARCH_UAV:
                Member member = (Member) item.getBean();
                holder.setText(R.id.shoutai_tv_member_name, member.getName());
                holder.setText(R.id.shoutai_tv_member_id, String.valueOf(member.getNo()));
                holder.setChecked(R.id.checkbox,member.isChecked());
                holder.addOnClickListener(R.id.checkbox);
                break;
            default:
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick();
    }
}
