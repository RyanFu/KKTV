package org.stagex.danmaku.adapter;

public class ProvinceInfo {
	private String provinceName;
	private String icon;

	public ProvinceInfo() {

	}

	public ProvinceInfo(String provinceName, String icon) {
		this.provinceName = provinceName;
		this.icon = icon;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
