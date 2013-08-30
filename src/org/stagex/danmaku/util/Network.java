package org.stagex.danmaku.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class Network {
	private ConnectivityManager connManager = null;
	private NetworkInfo networkInfo = null;
	/**
	 * 构造函数
	 */
	public Network(Context context) {
		connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager != null)
			networkInfo = connManager.getActiveNetworkInfo();
	}
	/**
	 * 对网络连接状态进行判断
	 * 
	 * @return true, 可用； false， 不可用
	 */
	public boolean isOpenNetwork() {
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}

		return false;
	}
	
	/**
	 * 对移动网络类型进行判断
	 * 
	 * @return true, 可用； false， 不可用
	 */
	public boolean isMobileNetwork() {
		if (connManager != null) {
			State mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
	        	return true;
		}

		return false;
	}
}
