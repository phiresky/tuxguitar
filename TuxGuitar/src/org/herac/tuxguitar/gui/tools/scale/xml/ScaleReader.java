package org.herac.tuxguitar.gui.tools.scale.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.herac.tuxguitar.gui.tools.scale.ScaleInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ScaleReader {
	private static final String SCALE_TAG = "scale";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String KEYS_ATTRIBUTE = "keys";
	
	public void loadScales(List scales,String fileName){
		try{
			File file = new File(fileName);
			if (file.exists()){
				Document doc = getDocument(file);
				loadScales(scales,doc.getFirstChild());
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	private static Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException {
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);
		
		return document;
	}
	
	private static void loadScales(List scales,Node node){
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			String nodeName = child.getNodeName();
			
			if (nodeName.equals(SCALE_TAG)) {
				NamedNodeMap params = child.getAttributes();
				
				String name = params.getNamedItem(NAME_ATTRIBUTE).getNodeValue();
				String keys = params.getNamedItem(KEYS_ATTRIBUTE).getNodeValue();
				
				if (name == null || keys == null || name.trim().equals("") || keys.trim().equals("")){
					throw new RuntimeException("Invalid Scale file format.");
				}
				
				scales.add(new ScaleInfo(name,keys));
			}
		}
	}
}
