package org.stagex.danmaku.adapter;

public class ProgramInfo {

	private String time;
	private String program;
	private Boolean curProgram;

	public ProgramInfo() {

	}

	public ProgramInfo(String time, String program, Boolean flag) {

		this.time = time;
		this.program = program;
		this.curProgram = flag;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String time) {
		this.time = time;
	}

	public Boolean getCurProgram() {
		return curProgram;
	}

	public void SetProgram(Boolean flag) {
		this.curProgram = flag;
	}
}
