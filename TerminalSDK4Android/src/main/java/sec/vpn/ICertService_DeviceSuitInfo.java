package sec.vpn;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * device info
 * 
 * jiangxuefang
 * 
 */
public final class ICertService_DeviceSuitInfo implements Parcelable{
	private static final String LOG_TAG = "ICertService_DeviceSuitInfo";

	public ICertService_DeviceSuitInfo() {
	}

	private final static ICertService_DeviceSuitInfo mInstance = new ICertService_DeviceSuitInfo();
	
	private String tfSn;

	private String certSn;
	
	private String imei;
	
	private String iccid;
	
	private String result;



	protected static ICertService_DeviceSuitInfo getInstance() {
		return mInstance;
	}

	
	public String getTfSn() {
		return tfSn;
	}


	public void setTfSn(String tfSn) {
		this.tfSn = tfSn;
	}


	public String getCertSn() {
		return certSn;
	}

	public void setCertSn(String certSn) {
		this.certSn = certSn;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}


	public String getIccid() {
		return iccid;
	}


	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public static String getLogTag() {
		return LOG_TAG;
	}

	public static ICertService_DeviceSuitInfo getMinstance() {
		return mInstance;
	}

	public static Creator<ICertService_DeviceSuitInfo> getCreator() {
		return CREATOR;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(tfSn);
		dest.writeString(certSn);
		dest.writeString(imei);
		dest.writeString(iccid);
		dest.writeString(result);
	}

	public static final Creator<ICertService_DeviceSuitInfo> CREATOR = new Creator<ICertService_DeviceSuitInfo>() {
		@Override
		public ICertService_DeviceSuitInfo[] newArray(int size) {
			return new ICertService_DeviceSuitInfo[size];
		}

		@Override
		public ICertService_DeviceSuitInfo createFromParcel(Parcel source) {

			String tf_sn = source.readString();
			String cert_sn = source.readString();
			String imei_num = source.readString();
			String iccid_num = source.readString();
			String ret = source.readString();

			ICertService_DeviceSuitInfo info = new ICertService_DeviceSuitInfo();
			
			info.setTfSn(tf_sn);
			info.setCertSn(cert_sn);
			info.setImei(imei_num);
			info.setIccid(iccid_num);
			info.setResult(ret);

			return info;
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
