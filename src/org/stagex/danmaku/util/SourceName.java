package org.stagex.danmaku.util;

public class SourceName {

	public static boolean mHd;
	public static boolean mHot;
	
	// TODO节目源甄别
	public final static String letv = "letv";
	public final static String veryhd = "veryhd";
	public final static String hdplay = "hdplay";
	public final static String cntv = "cntv";
	public final static String itv = "52itv";
	public final static String ku6 = "ku6";
	public final static String ucatv = "ucatv";
	public final static String jsntv = "jsntv";
	public final static String pptv = "pptv";
	public final static String sohu = "sohu";
	public final static String qq = "qq";
	public final static String cutv = "cutv";
	public final static String wasu = "wasu";
	public final static String cztv = "cztv";
	public final static String ifeng = "ifeng";
	public final static String smgbb = "smgbb";
	public final static String xwei = "xwei";
	public final static String thmz = "thmz";
	public final static String ntjoy = "ntjoy";

	public final static String hdfans = "hdfans";
	public final static String cqnews = "cqnews";
	public final static String yntv = "yntv";
	public final static String hoolo = "hoolo";
	public final static String ywcity = "ywcity";
	public final static String gz36tv = "36tv";
	public final static String ahtv = "ahtv";
	public final static String hfbtv = "hfbtv";
	public final static String hljtv = "125.211.216.198";
	public final static String gxtv = "222.216.111.87";
	public final static String nbtv = "nbtv";
	public final static String ijntv = "ijntv";
	public final static String hbtv = "hbtv";
	public final static String hntv = "hvtv";
	
	/**
	 * 判别节目候选地址的名称
	 * @return
	 */
	public static String whichName(String url1) {
		String url;
		/* FIXME if no xxx:// ??? */
		int offset1 = url1.indexOf(':') + 3;
		/* FIXME bug#0018 */
		int offset2 = url1.indexOf('/', offset1);
		if (offset2 == -1) {
			url = url1;
		} else {
			url = url1.substring(offset1, offset2);
		}
		// Log.d(LOGTAG, "hostname ===>" + url);

		String urlName = "未知源";
		mHd = false;
		mHot = false;

		if (url.indexOf(letv) >= 0) {
			urlName = "乐视网[letv]";
			mHd = true;
		} else if (url.indexOf(veryhd) >= 0)
			urlName = "veryhd";
		else if (url.indexOf(hdplay) >= 0)
			urlName = "VST全聚合[hdplay]";
		else if (url.indexOf(cntv) >= 0) {
			urlName = "央视网[cntv]";
			mHot = true;
		} else if (url.indexOf(itv) >= 0)
			urlName = "VST全聚合[52itv]";
		else if (url.indexOf(ku6) >= 0)
			urlName = "酷6网[ku6]";
		else if (url.indexOf(ucatv) >= 0)
			urlName = "天山云电视[ucatv]";
		else if (url.indexOf(jsntv) >= 0)
			urlName = "建始网络电台[jsntv]";
		else if (url.indexOf(pptv) >= 0)
			urlName = "PPTV";
		else if (url.indexOf(sohu) >= 0) {
			urlName = "搜狐视频[sohu]";
			mHd = true;
			mHot = true;
		} else if (url.indexOf(qq) >= 0) {
			urlName = "腾讯视频[qqlive]";
			mHd = true;
		} else if (url.indexOf(cutv) >= 0) {
			urlName = "城视网[cutv]";
			mHot = true;
		} else if (url.indexOf(wasu) >= 0)
			urlName = "华数TV";
		else if (url.indexOf(cztv) >= 0)
			urlName = "新蓝网[cztv]";
		else if (url.indexOf(ifeng) >= 0)
			urlName = "凤凰视频[ifeng]";
		else if (url.indexOf(smgbb) >= 0)
			urlName = "东方宽频电视[smgbb]";
		else if (url.indexOf(xwei) >= 0)
			urlName = "小微视频[xwei]";
		else if (url.indexOf(thmz) >= 0)
			urlName = "太湖明珠网[thmz]";
		else if (url.indexOf(ntjoy) >= 0)
			urlName = "江海明珠网[ntjoy]";

		else if (url.indexOf(hdfans) >= 0)
			urlName = "hdfans";
		else if (url.indexOf(cqnews) >= 0)
			urlName = "华龙视频[cqnews]";
		else if (url.indexOf(yntv) >= 0)
			urlName = "云视网[yntv]";
		else if (url.indexOf(hoolo) >= 0)
			urlName = "葫芦网[hoolo]";
		else if (url.indexOf(ywcity) >= 0)
			urlName = "义务城市网[ywcity]";
		else if (url.indexOf(gz36tv) >= 0)
			urlName = "广众网[36tv]";
		else if (url.indexOf(ahtv) >= 0)
			urlName = "安徽网络电视台[ahtv]";
		else if (url.indexOf(hfbtv) >= 0)
			urlName = "安徽网络电视台[ahtv]";
		else if (url.indexOf(hljtv) >= 0)
			urlName = "黑龙江网络广播电视台[hljtv]";
		else if (url.indexOf(gxtv) >= 0)
			urlName = "广西电视网[gxtv]";
		else if (url.indexOf(nbtv) >= 0)
			urlName = "宁波广电网[nbtv]";
		else if (url.indexOf(ijntv) >= 0)
			urlName = "济南网络广播电台[ijntv]";
		else if (url.indexOf(hbtv) >= 0)
			urlName = "湖北网台[hbtv]";
		else if (url.indexOf(hntv) >= 0)
			urlName = "河南网络电视台[hntv]";

		return urlName;
	}
}
