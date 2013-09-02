package org.stagex.danmaku.type;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioType implements Parcelable {

	private String url;
	private String name;

	@Override
	public int describeContents() {
		return 0;
	}

	public RadioType() {

	}

	private RadioType(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		url = in.readString();
		name = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeString(name);
	}

	public static final Creator<RadioType> CREATOR = new Creator<RadioType>() {

		@Override
		public RadioType createFromParcel(Parcel source) {
			return new RadioType(source);
		}

		@Override
		public RadioType[] newArray(int size) {
			return new RadioType[size];
		}

	};

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
