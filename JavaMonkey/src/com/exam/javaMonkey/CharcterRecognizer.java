package com.exam.javaMonkey;

import java.io.*;
import java.util.*;

import javax.swing.text.html.parser.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import net.sourceforge.tess4j.*;

public class CharcterRecognizer {
	String language;
	static ArrayList<String> text;

	public static ArrayList<String> getText() {
		return text;
	}

	public static ArrayList<String> getTitle() {
		return title;
	}

	static ArrayList<String> title;



	public CharcterRecognizer( String language){

		this.language = language;
	}

	public void recog(){

		File imageFile = new File("C:/TessImage/tessImg.png");
		Tesseract instance = Tesseract.getInstance();  // JNA Interface Mapping
		//				Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping

		try {
			instance.setLanguage(language);
			instance.setHocr(true);
			String result = instance.doOCR(imageFile);
			System.out.println(result);
			
			htmlParse(result);
			
//			dom dom1 = new dom(result);
//			try {
//				dom1.makeDom();
//			} catch (ParserConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}



		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void htmlParse(String str){
		Reader reader = new StringReader(str);
		htmlPaserHandler parse = new htmlPaserHandler();

		try {
			new ParserDelegator().parse(reader, parse, true);
			text = parse.getText();
			title = parse.getTitle();
			System.out.println("title : " + title.size() + "  text : " + text.size());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}



