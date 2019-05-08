package cn.vsx.vc.application;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ParseXmlService
{
	public static HashMap<String, String> parseXml(InputStream inStream) throws Exception
	{
		HashMap<String, String> hashMap = new HashMap<String, String>();
		
		// ʵ����һ���ĵ�����������
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// ͨ���ĵ�������������ȡһ���ĵ�������
		DocumentBuilder builder = factory.newDocumentBuilder();
		// ͨ���ĵ�ͨ���ĵ�����������һ���ĵ�ʵ��
		Document document = builder.parse(inStream);
		//��ȡXML�ļ����ڵ�
		Element root = document.getDocumentElement();
		//��������ӽڵ�
		NodeList childNodes = root.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++)
		{
			//�����ӽڵ�
			Node childNode = (Node) childNodes.item(j);
			if (childNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element childElement = (Element) childNode;
				//�汾��
				if ("version".equals(childElement.getNodeName()))
				{
					hashMap.put("version",childElement.getFirstChild().getNodeValue());
				}
				//�������
				else if (("name".equals(childElement.getNodeName())))
				{
					hashMap.put("name",childElement.getFirstChild().getNodeValue());
				}
				//���ص�ַ
				else if (("url".equals(childElement.getNodeName())))
				{
					hashMap.put("url",childElement.getFirstChild().getNodeValue());
				}
				//�������
				else if (("name_f32".equals(childElement.getNodeName())))
				{
					hashMap.put("name_f32",childElement.getFirstChild().getNodeValue());
				}
				//���ص�ַ
				else if (("url_f32".equals(childElement.getNodeName())))
				{
					hashMap.put("url_f32",childElement.getFirstChild().getNodeValue());
				}
			}
		}
		return hashMap;
	}

}
