package org.stagex.danmaku.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.stagex.danmaku.type.Home_List_type;
import org.stagex.danmaku.util.HttpUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;

public class HomeListDomParse {

	public static List<Home_List_type> parseXml(Context context) {
		List<Home_List_type> cutvList = new ArrayList<Home_List_type>();
		InputStream stream = HttpUtil.getInputStreamFromLocal(context);
		try {
			// 得到Dom解析对象工厂
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// 通过工厂创建Dom解析对象实例
			DocumentBuilder db = factory.newDocumentBuilder();
			// 将xml文件的输入流交给Dom解析对象进行解析，并将Dom树返回
			Document document = db.parse(stream);
			// 通过Dom树接收到根元素
			Element rootElement = document.getDocumentElement();
			// 通过根元素获得下属的所有名字为video节点
			NodeList nodeList = rootElement.getElementsByTagName("video");
			int nodeListcount = nodeList.getLength();
			// 遍历取出来的video节点集合
			for (int i = 0; i < nodeListcount; i++) {
				// 得到一个video节点
				Element videoElement = (Element) nodeList.item(i);
				// 新建一个video对象
				Home_List_type videoType = new Home_List_type();
				// 取得video标签的下属所有节点
				NodeList tvChildList = videoElement.getChildNodes();
				int countChild = tvChildList.getLength();
				for (int j = 0; j < countChild; j++) {
					// 创建一个引用，指向循环的标签
					Node node = tvChildList.item(j);
					// 如果此循环出来的元素是Element对象，即标签元素，那么执行以下代码
					if (Node.ELEMENT_NODE == node.getNodeType()) {
						if ("id".equals(node.getNodeName())) {
							int id = Integer.parseInt(node.getFirstChild().getNodeValue());
							videoType.setId(id);
						}else if ("type".equals(node.getNodeName())) {
							String type = node.getFirstChild().getNodeValue().trim();
							videoType.setType(type);
							Log.e("test", type);
						}else if ("url".equals(node.getNodeName())) {
							String url_1 = node.getFirstChild().getNodeValue().trim();
							videoType.setUrl(url_1);
						}else if ("name".equals(node.getNodeName())) {
							String name = node.getFirstChild().getNodeValue().trim();
							videoType.setName(name);
						}else if ("img".equals(node.getNodeName())) {
							String img = node.getFirstChild().getNodeValue().trim();
							videoType.setImg(img);
						}
					}
				}
				cutvList.add(videoType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cutvList;
	}
}
