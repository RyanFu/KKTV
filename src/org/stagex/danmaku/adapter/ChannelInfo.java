package org.stagex.danmaku.adapter;

import java.util.ArrayList;

public class ChannelInfo {
	private int id;
	private String name;
	private String icon_url;
	private String province_name;
	private String mode;
	private String url;
	private String[] second_url;
	private String types;
	private String program_path;

	public ChannelInfo() {

	}

	public ChannelInfo(int id, String name, String icon_url,
			String province_name, String mode, String url, String[] second_url,
			String types, String path) {
		this.id = id;
		this.name = name;
		this.icon_url = icon_url;
		this.province_name = province_name;
		this.mode = mode;
		this.url = url;
		this.second_url = second_url;
		this.types = types;
		this.program_path = path;
	}

	public ChannelInfo(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIcon_url() {
		return icon_url;
	}

	public void setIcon_url(String icon_url) {
		this.icon_url = icon_url;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public String getProvince_name() {
		return province_name;
	}

	public String[] getSecond_url() {
		return second_url;
	}

	public void setSecond_url(String[] second_url) {
		this.second_url = second_url;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public String getProgram_path() {
		return program_path;
	}

	public void setProgram_path(String program_path) {
		this.program_path = program_path;
	}

	public ArrayList<String> getAllUrl() {
		ArrayList<String> all_url_list = new ArrayList<String>();
		int size = 0;

		if (second_url != null)
			size = second_url.length + 1;
		else
			size = 1;

		all_url_list.add(url);
		for (int i = 1; i < size; i++)
			all_url_list.add(second_url[i - 1]);

		return all_url_list;
	}

}
