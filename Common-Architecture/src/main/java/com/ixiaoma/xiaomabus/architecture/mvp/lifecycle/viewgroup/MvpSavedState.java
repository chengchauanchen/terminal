package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.viewgroup;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class MvpSavedState extends View.BaseSavedState {

	public static final Parcelable.Creator<MvpSavedState> CREATOR = new Parcelable.Creator<MvpSavedState>() {

		@Override
		public MvpSavedState createFromParcel(Parcel in) {
			return new MvpSavedState(in);
		}

		@Override
		public MvpSavedState[] newArray(int size) {
			return new MvpSavedState[size];
		}
	};

	//我要保存的数据
	private int mvpbyViewId = 0;
	
	public MvpSavedState(Parcelable superState) {
		super(superState);
	}

	protected MvpSavedState(Parcel in) {
		super(in);
		this.mvpbyViewId = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeInt(mvpbyViewId);
	}

	public int getMosbyViewId() {
		return mvpbyViewId;
	}

	public void setMosbyViewId(int mosbyViewId) {
		this.mvpbyViewId = mosbyViewId;
	}

}
