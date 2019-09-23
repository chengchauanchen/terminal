package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 *
 */

public class BandDevicesAdapter extends BaseAdapter {


    private Context context;
    private List<String> list;
    public BandDevicesAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        ViewHoder viewHoder = null;
        if (view == null) {
            viewHoder = new ViewHoder();
            view = LayoutInflater.from(context).inflate(R.layout.item_band_device, null);
            viewHoder.iv_device_type = view.findViewById(R.id.iv_device_type);
            viewHoder.tv_device_no = view.findViewById(R.id.tv_device_no);
            viewHoder.bnt_un_band = view.findViewById(R.id.bnt_un_band);
            view.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) view.getTag();
        }
        String s = list.get(position);
        viewHoder.tv_device_no.setText(s);
        return view;
    }

    class ViewHoder {
        ImageView iv_device_type;
        TextView tv_device_no;
        Button bnt_un_band;
    }
}
