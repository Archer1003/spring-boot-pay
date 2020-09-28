package com.cserver.saas.modules.wechatpay.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmlUtil {
    private static final Log logger = LogFactory.getLog(XmlUtil.class);

    public static Map<String, Object> parseXml(InputStream inputStream) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();

            for (Element e : elements) {
                map.put(e.getName(), e.getText());
            }

            return map;
        } catch (Exception e) {
            logger.error(e.getCause());
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error(e.getCause());
                return null;
            }
        }
    }

    public static JSONObject parseXmlJson(String xml) {
        try {
            SAXReader builder = new SAXReader();
            Document document = builder.read(new StringReader(xml));
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            JSONObject json = new JSONObject();

            for (Element e : elements) {
                if (e.isTextOnly()) {
                    json.put(e.getName(), e.getText());
                } else {
                    json.put(e.getName(), parseXmlJson(e));
                }
            }
            return json;
        } catch (Exception e) {
            logger.error(e.getCause());
            return null;
        }
    }

    public static JSONObject parseXmlJson(Element root) {
        try {
            List<Element> elements = root.elements();
            JSONObject json = new JSONObject();
            for (Element e : elements) {
                if (e.isTextOnly()) {
                    json.put(e.getName(), e.getText());
                } else {
                    json.put(e.getName(), parseXmlJson(e));
                }
            }
            return json;
        } catch (Exception e) {
            logger.error(e.getCause());
            return null;
        }
    }

    public static Map<String, Object> parseXml(String xml) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            SAXReader builder = new SAXReader();
            Document document = builder.read(new StringReader(xml));
            Element root = document.getRootElement();
            List<Element> elements = root.elements();

            for (Element e : elements) {
                map.put(e.getName(), e.getText());
            }

            return map;
        } catch (Exception e) {
            logger.error(e.getCause());
            return null;
        }
    }

    public static String toXml(Map<String, Object> data) {
        Document document = DocumentHelper.createDocument();
        Element nodeElement = document.addElement("xml");
        for (String key : data.keySet()) {
            Element keyElement = nodeElement.addElement(key);
            keyElement.setText(String.valueOf(data.get(key)));
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat("   ", true, "UTF-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            return out.toString("UTF-8");
        } catch (Exception ex) {
            logger.error(ex.getCause());
        }
        return null;
    }

    public static String toXml(Map<String, String> headData, Map<String, String> bodyData) {
        Document document = DocumentHelper.createDocument();
        Element message = document.addElement("message");
        Element head = message.addElement("head");
        for (String key : headData.keySet()) {
            Element keyElement = head.addElement(key);
            keyElement.setText(headData.get(key));
        }
        Element body = message.addElement("body");
        for (String key : bodyData.keySet()) {
            Element keyElement = body.addElement(key);
            keyElement.setText(bodyData.get(key));
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat("", true, "UTF-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            return out.toString("UTF-8");
        } catch (Exception ex) {
            logger.error(ex.getCause());
        }
        return null;
    }

    /**
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
     *
     * @param strxml
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map doXMLParse(String strxml) throws JDOMException, IOException {
        //过滤关键词，防止XXE漏洞攻击
        strxml = filterXXE(strxml);
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

        if (null == strxml || "".equals(strxml)) {
            return null;
        }

        Map m = new HashMap();

        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        org.jdom.Document doc = builder.build(in);
        org.jdom.Element root = doc.getRootElement();
        List list = root.getChildren();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            org.jdom.Element e = (org.jdom.Element) it.next();
            String k = e.getName();
            String v = "";
            List children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = XmlUtil.getChildrenText(children);
            }

            m.put(k, v);
        }

        // 关闭流
        in.close();

        return m;
    }

    /**
     * 获取子结点的xml
     *
     * @param children
     * @return String
     */
    @SuppressWarnings({ "rawtypes" })
    public static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                org.jdom.Element e = (org.jdom.Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(XmlUtil.getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }

        return sb.toString();
    }
    /**
     * 通过DOCTYPE和ENTITY来加载本地受保护的文件、替换掉即可
     * 漏洞原理：https://my.oschina.net/u/574353/blog/1841103
          * 防止 XXE漏洞 注入实体攻击
          * 过滤 过滤用户提交的XML数据
          * 过滤关键词：<!DOCTYPE和<!ENTITY，或者SYSTEM和PUBLIC。
         */
    public static String filterXXE(String xmlStr){
        xmlStr = xmlStr.replace("DOCTYPE", "").replace("SYSTEM", "").replace("ENTITY", "").replace("PUBLIC", "");
        return xmlStr;
    }

    /**
     * 微信给出的 XXE漏洞方案
     * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=23_5
     * @param strXML
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map doXMLParse2(String strXML) throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        String FEATURE = null;
        try {
            FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            documentBuilderFactory.setFeature(FEATURE, true);

            FEATURE = "http://xml.org/sax/features/external-general-entities";
            documentBuilderFactory.setFeature(FEATURE, false);

            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            documentBuilderFactory.setFeature(FEATURE, false);

            FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            documentBuilderFactory.setFeature(FEATURE, false);

            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        org.w3c.dom.Document doc = documentBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        for (int idx=0; idx<nodeList.getLength(); ++idx) {
            Node node = nodeList.item(idx);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                m.put(element.getNodeName(), element.getTextContent());
            }
        }
        stream.close();
        return m;
    }

}
