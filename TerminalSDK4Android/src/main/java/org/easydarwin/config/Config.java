/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package org.easydarwin.config;

/**
 * 类Config的实现描述：
 */
public class Config{

    public static final String SERVER_IP = "serverIp";
    public static final String SERVER_PORT = "serverPort";
    public static final String STREAM_ID = "streamId";
    public static final String STREAM_ID_PREFIX = "";
    public static final String DEFAULT_SERVER_IP = "cloud.easydarwin.org";
    public static final String DEFAULT_SERVER_PORT = "554";
    public static final String DEFAULT_STREAM_ID = STREAM_ID_PREFIX + String.valueOf((int) (Math.random() * 1000000 + 100000));
    public static final String PREF_NAME = "easy_pref";
    public static final String K_RESOLUTION = "k_resolution";



    public static final String SERVER_URL = "serverUrl";
    public static final String DEFAULT_SERVER_URL = "rtmp://www.easydss.com:10085/live/stream_"+String.valueOf((int) (Math.random() * 1000000 + 100000));


    public static final String PLAYKEY = "79393674363536526D3432414D7435517279476D63505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";
    public static final String RTMPPLAYKEY = "59617A414C5A36526D3432415A657462704253614A654676636D63755A57467A65575268636E64706269356C59584E3563477868655756794C6E4A306258416A567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
    /*这是永久的推流rtsp的key*/
    public static final String PUSHKEY = "6A36334A743536526D343041676C394C744B425738505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";
}
