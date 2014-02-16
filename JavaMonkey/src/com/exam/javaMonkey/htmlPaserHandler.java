package com.exam.javaMonkey;

import java.util.*;

import javax.swing.text.*;
import javax.swing.text.html.*;

public class htmlPaserHandler extends HTMLEditorKit.ParserCallback {
	ArrayList<String> text = new ArrayList<String>();
	ArrayList<String> title = new ArrayList<String>();
	boolean tf = true;

	
	public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
		if(String.valueOf(a.getAttribute(javax.swing.text.html.HTML.Attribute.CLASS)).equals("ocrx_word")){
			title.add(String.valueOf(a.getAttribute(javax.swing.text.html.HTML.Attribute.TITLE)));
			
			System.out.println(String.valueOf(a.getAttribute(javax.swing.text.html.HTML.Attribute.TITLE)));
//			System.out.println(tag.toString());
//			HTML.Attribute[] at = javax.swing.text.html.HTML.getAllAttributeKeys();
//			for(int i =0;i<at.length;i++){
//				System.out.println(at[i].toString() + " : " + a.getAttribute(at[i]));
//			}
			
			System.out.println("title size:" + title.size());
//			handleText("strong".toCharArray(), 0);
//			System.out.println(javax.swing.text.html.HTML.getTag("span").CONTENT.toString());
//			HTML.Tag tag2=javax.swing.text.html.HTML.getTag("strong");
//			HTML.Tag tag3=tag2.CONTENT;
//			System.out.println(tag3.toString());
			//System.out.println(String.valueOf(a.getAttribute(javax.swing.text.html.HTML.getTag("strong"))));
		}
	}                                        

	public void handleText(char[] data, int pos) {
		if((title.size() - text.size()) > 1){
			for(int i =0; i<(title.size() - text.size());i++)
				text.add("");
		}
			
		String str = String.valueOf(data);
//		System.out.println("nulle       :" + str + "pos :"+pos);
		if(tf){
			text.add(str);
			System.out.println("nulle       :" + str + "size :"+text.size());
			tf=false;
		}else
			tf=true;
		
		
		
	}
	
	public ArrayList<String> getText(){
		return text;
	}
	public ArrayList<String> getTitle(){
		return title;
	}
}

