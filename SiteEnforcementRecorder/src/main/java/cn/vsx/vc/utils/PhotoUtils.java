package cn.vsx.vc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;

/**
 * author: zjx.
 * data:on 2018/7/17
 */

public class PhotoUtils {

    public static void openCamera(Activity activity, int requestCode) {
        //调用系统相机, 并将拍摄的图片保存
        Intent intentCamera = new Intent();
        int photoNum = TerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
        photoNum++;
        TerminalFactory.getSDK().putParam(Params.PHOTO_NUM, photoNum);
        File file = new File(TerminalFactory.getSDK().getPhotoRecordDirectory(), "image_" + TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" +photoNum+".jpg");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        Uri imageUri = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //兼容Android7.0
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imageUri= FileProvider.getUriForFile(activity, "cn.zectec.ptt.fileprovider", file);
        }
        else {
            imageUri = Uri.fromFile(file);
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intentCamera, requestCode);
    }

    public static void savePictureForByte (byte[] data, File file, Context context) {
        if(data.length<3)
            return;//判断输入的byte是否为空
        try{
//            file.getParentFile().getParentFile().delete();
//            SDCardUtil.fileScan(context, SDCardUtil.getExternStoragePath(context) + File.separator + "Android/data/");
//            File folder1 = new File(SDCardUtil.getStoragePath(context));
//            if (!folder1.exists()) {
//                folder1.mkdirs();
//            }
//            File folder2 = new File(SDCardUtil.getStoragePath(context) +  File.separator + "photoRecord");
//            if (!folder2.exists()) {
//                folder2.mkdirs();
//            }
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        } catch(Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }

    public static File saveBitmap (Bitmap bitmap) {
        int photoNum = TerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
        photoNum++;
        TerminalFactory.getSDK().putParam(Params.PHOTO_NUM, photoNum);
        File file = new File(TerminalFactory.getSDK().getPhotoRecordDirectory(), "image_" + TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" +photoNum+".jpg");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return file;
        }

    }

    /**
     * 发送图片
     */
    public static void sendPhotoFromCamera (File file) {
        if(file.exists()) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put(JsonParam.SEND_STATE, SendState.SENDING);
//            jsonObject.put(JsonParam.PICTURE_NAME,  file.getName());
//            jsonObject.put(JsonParam.PICTURE_SIZE, DataUtil.getFileSize(file)+"");
//            jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getShortSeq());
//            jsonObject.put(JsonParam.PATH, file.getPath());
//            jsonObject.put(JsonParam.PHOTO_CREATE_TIME, System.currentTimeMillis());
//            TerminalMessage mTerminalMessage = new TerminalMessage();
//            mTerminalMessage.sendMemberId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
//            mTerminalMessage.sendMemberName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
//            mTerminalMessage.groupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
//            mTerminalMessage.groupName = DataUtil.getGroupByGroupId(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name;
//            mTerminalMessage.messageUrl = file.getPath();
//            mTerminalMessage.sendTime = System.currentTimeMillis();
//            mTerminalMessage.messageType = MessageType.PICTURE.getCode();
//            List<Integer> toIds = new ArrayList<>();
//            toIds.add(NoCodec.encodeGroupNo(mTerminalMessage.groupId));
//            mTerminalMessage.receiveMemberId = toIds.get(0);
//            mTerminalMessage.messageBody = jsonObject;
//            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.IMAGE_UPLOAD_URL, ""), file, mTerminalMessage, true);
//            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveSendMessageHandler.class, mTerminalMessage);
        }
    }
}
