package sec.vpn;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * tf cardinfo
 * 
 * jiangxuefang
 * 
 */
public final class ICertService_TfInfo implements Parcelable{
	private static final String LOG_TAG = "ICertService_TfInfo";

	public ICertService_TfInfo() {
	}

	private final static ICertService_TfInfo mInstance = new ICertService_TfInfo();
	
	private String tfSn;

	private String certSn;
	
	private String certOu;
	
	private String userID;
	
	private String userName;
	
	private String medium;
	
	private String notBefore;
	
	private String notAfter;
	
	private String result;



	protected static ICertService_TfInfo getInstance() {
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


	public String getCertOu() {
		return certOu;
	}
	
	public void setCertOu(String certOu) {
		this.certOu = certOu;
	}

	public String getUserID() {
		return userID;
	}



	public void setUserID(String userID) {
		this.userID = userID;
	}



	public String getUserName() {
		return userName;
	}



	public void setUserName(String userName) {
		this.userName = userName;
	}



	public String getMedium() {
		return medium;
	}



	public void setMedium(String medium) {
		this.medium = medium;
	}



	public String getNotBefore() {
		return notBefore;
	}



	public void setNotBefore(String notBefore) {
		this.notBefore = notBefore;
	}



	public String getNotAfter() {
		return notAfter;
	}



	public void setNotAfter(String notAfter) {
		this.notAfter = notAfter;
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



	public static ICertService_TfInfo getMinstance() {
		return mInstance;
	}



	public static Creator<ICertService_TfInfo> getCreator() {
		return CREATOR;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(tfSn);
		dest.writeString(certSn);
		dest.writeString(certOu);
		dest.writeString(userID);
		dest.writeString(userName);
		dest.writeString(medium);
		dest.writeString(notBefore);
		dest.writeString(notAfter);
		dest.writeString(result);
	}

	public static final Creator<ICertService_TfInfo> CREATOR = new Creator<ICertService_TfInfo>() {
		@Override
		public ICertService_TfInfo[] newArray(int size) {
			return new ICertService_TfInfo[size];
		}

		@Override
		public ICertService_TfInfo createFromParcel(Parcel source) {

			String tf_sn = source.readString();
			String cert_sn = source.readString();
			String cert_ou = source.readString();
			String user_id = source.readString();
			String user_name = source.readString();
			String medium_mode = source.readString();
			String not_before = source.readString();
			String not_after = source.readString();
			String ret = source.readString();

			ICertService_TfInfo info = new ICertService_TfInfo();
			
			info.setTfSn(tf_sn);
			info.setCertSn(cert_sn);
			info.setCertOu(cert_ou);
			info.setUserID(user_id);
			info.setUserName(user_name);
			info.setMedium(medium_mode);
			info.setNotBefore(not_before);
			info.setNotAfter(not_after);
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
