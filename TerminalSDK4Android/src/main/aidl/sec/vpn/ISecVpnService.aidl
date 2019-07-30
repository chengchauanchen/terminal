package sec.vpn;
import sec.vpn.ICertService_TfInfo;
import sec.vpn.ICertService_DeviceSuitInfo;

interface ISecVpnService {

	/*
	设置服务器地址
	[IN]ip： 服务器地址
	[IN]port：服务器端口
	*/
	void sv_setServerAddr(String ip, String port);

	/*
	启动服务
	*/
	void sv_start();

	/*
	停止服务
	*/
	void sv_stop();
	
	/*
	退出程序
	*/
	void sv_quit();
	
	/*
	服务是否已启动
	返回值：true=已启动，false=未启动
	*/
	boolean sv_isStarted();
	
	/*
	VPN状态查询
	返回值：未启动、已建立、连接中、发生错误,重试中、已断开  （UTF-8编码）
	*/
	String sv_serviceState();
	
	/*
	获取签名证书
	[IN]isBase64： 返回的证书内容字符串是否进行Base64编码
		true :Base64编码字符串
		false:普通字符串
	返回值：成功：Base64编码的证书内容；失败：""
	*/
	String sv_getCertBase64(boolean isBase64);
	
	/*
	获取TF信息
	成员变量：
	tfSn      TF安全卡编号
	certSn    证书序列号
	certOu	     证书持有者的组织机构
	userID    证书持有人身份证号码（TODO）
	userName  证书持有人姓名
	medium    证书存储介质（默认为“tf”）
	notBefore 有效期起始时间
	notAfter  有效期终止时间
	返回值：
	result  0：查询成功
			1：未知异常   （not happened）
			2：VPN服务异常 （TDOD）
			3：证书信息为空或未找到匹配信息
			4：TF卡号读取错误
	如果 result 为 0，则其余数据 有效；否则其余数据无效。
	*/
    ICertService_TfInfo getTfInfo();
    
    /*
    获取三卡信息
    成员变量：
    tfSn      TF安全卡编号
    certSn    证书序列号
    imei      终端IMEI/MEID（如双卡，为主卡对应的设备IMEI）
    iccid     终端SIM卡ICCID（如双卡，为主卡对应的ICCID）
    返回值：
    result  0：查询成功
			1：证书序列号读取错误
			2：TF卡读取错误
			3：IMEI获取错误
			4：ICCID获取错误
	如果 result 为 0，则其余数据 有效；否则其余数据无效。
    */
    ICertService_DeviceSuitInfo getDeviceSuitInfo();  

}