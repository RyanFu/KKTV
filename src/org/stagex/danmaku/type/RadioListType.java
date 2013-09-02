package org.stagex.danmaku.type;

import java.io.Serializable;
import java.util.List;

public class RadioListType implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private List<RadioType> list;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<RadioType> getList() {
		return list;
	}

	public void setList(List<RadioType> list) {
		this.list = list;
	}

}
