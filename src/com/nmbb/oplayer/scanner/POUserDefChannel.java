package com.nmbb.oplayer.scanner;

import java.util.ArrayList;

import org.stagex.danmaku.adapter.ChannelInfo;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 节目列表PO类
 * 
 */
@DatabaseTable(tableName = "UserDefChannel")
public class POUserDefChannel {
	@DatabaseField(generatedId = true)
	public long poId;
	/** 频道ID */
	@DatabaseField
	public int id;
	/** 频道名称 */
	@DatabaseField
	public String name;
	/** 电视台台标 */
	@DatabaseField
	public String icon_url;
	/** 省名称 */
	@DatabaseField
	public String province_name;
	/** 节目源模式 */
	@DatabaseField
	public String mode;
	/** 节目源首选地址 */
	@DatabaseField
	public String url;
	/** 节目源候选地址 */
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	public String[] second_url;
	/** 节目频道分类 */
	@DatabaseField
	public String types;
	/** 节目预告地址 */
	@DatabaseField
	public String program_path;
	/** 是否收藏 */
	@DatabaseField
	public Boolean save;
	/** 收藏的日期 */
	@DatabaseField
	public String date;

	public POUserDefChannel() {

	}

	public POUserDefChannel(ChannelInfo info, Boolean isSave) {
		id = info.getId();
		name = info.getName();
		icon_url = info.getIcon_url();
		province_name = info.getProvince_name();
		mode = info.getMode();
		url = info.getUrl();
		second_url = info.getSecond_url();
		types = info.getTypes();
		program_path = info.getProgram_path();
		save = isSave;
	}

	// just for test
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name=").append(name);
		return sb.toString();
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
