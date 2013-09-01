package org.stagex.danmaku.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;

public class HttpUtil {
	// 通过url获得HttpGet对象
	public static HttpGet getHttpGet(String url) {
		// 实例化HttpGet
		HttpGet request = new HttpGet(url);
		return request;
	}

	// 通过URL获得HttpPost对象
	public static HttpPost getHttpPost(String url) {
		// 实例化HttpPost
		HttpPost request = new HttpPost(url);
		return request;
	}

	// 通过HttpGet获得HttpResponse对象
	public static HttpResponse getHttpResponse(HttpGet request)
			throws ClientProtocolException, IOException {
		// 实例化HttpResponse
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}

	// 通过HttpPost获得HttpResponse对象
	public static HttpResponse getHttpResponse(HttpPost request)
			throws ClientProtocolException, IOException {
		// 实例化HttpResponse
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}

	// 通过url发送post请求，返回请求结果
	public static String queryStringForPost(String url) {

		// 获得HttpPost实例
		HttpPost request = HttpUtil.getHttpPost(url);
		String result = null;
		try {
			// 获得HttpResponse实例
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			// if(response.getStatusLine().getStatusCode() ==200){
			// 获得返回结果
			result = EntityUtils.toString(response.getEntity());
			return result;
			// }

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		}
		// return null;
	}

	// 通过url发送get请求，返回请求结果
	public static String queryStringForGet(String url) {
		// 获得HttpGet实例
		HttpGet request = HttpUtil.getHttpGet(url);
		String result = null;
		try {
			// 获得HttpResponse实例
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			// if(response.getStatusLine().getStatusCode()==200){
			// 获得返回结果
			result = EntityUtils.toString(response.getEntity());
			return result;
			// }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		}
		// return null;
	}

	// 通过HttpPost发送Post请求，返回请求结果
	public static String queryStringForPost(HttpPost request) {
		String result = null;
		try {
			// 获得HttpResponse实例
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			// if(response.getStatusLine().getStatusCode()==200){
			// 获得请求结果
			result = EntityUtils.toString(response.getEntity());
			return result;
			// }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		}
		// return null;
	}

	// 通过HttpGet发送Get请求，返回请求结果
	public static String queryStringForGet(HttpGet request) {
		String result = null;
		try {

			// 获得HttpResponse实例
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			// if(response.getStatusLine().getStatusCode()==200){
			// 获得请求结果
			result = EntityUtils.toString(response.getEntity());
			return result;
			// }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			result = "网络异常!";
			return result;
		}
		// return null;
	}

	public static InputStream getInputStreamFromLocal(Context context){
		InputStream stream = null;
		try {
			stream = context.getAssets().open("home_video.xml");
		} catch (Exception e) {
		}
		return stream;
	}
	public static InputStream GetInputStreamFromURL(String urlstr) {
		HttpURLConnection connection;
		URL url;
		InputStream stream = null;
		try {
			url = new URL(urlstr);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			stream = connection.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return stream;
	}
}