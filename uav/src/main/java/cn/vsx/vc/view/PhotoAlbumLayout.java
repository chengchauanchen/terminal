package cn.vsx.vc.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.vc.R;

/**
 * 会话聊天界面相册界面组件
 * Created by gt358 on 2017/8/11.
 */

public class PhotoAlbumLayout extends LinearLayout implements View.OnClickListener{
    private Context context;
    public PhotoAlbumLayout(Context context) {
        this(context, null);
    }

    public PhotoAlbumLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoAlbumLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView () {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_photoalbum, this, true);
        view.findViewById(R.id.ll_to_photo_album).setOnClickListener(this);
    }


    public void onClick (View view) {
        switch (view.getId()) {
            case R.id.ll_to_photo_album:
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM, true);
                break;
        }
    }

}
