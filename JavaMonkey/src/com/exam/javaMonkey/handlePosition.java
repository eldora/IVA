package com.exam.javaMonkey;

import java.util.*;

public class handlePosition {

	ArrayList<String> title;
	ArrayList<String> text;
	
	private int positionX, positionY; 
	
	public handlePosition(ArrayList<String> title, ArrayList<String> text) {
		this.title = title;
		this.text = text;
		// TODO Auto-generated constructor stub
	}
	
	public boolean dor(String str){
		int index = -1;
		for(int i = 0; i < text.size();i++){
			if(text.get(i).contains(str)){
				index = i;
				System.out.println(index+"    aaaa");
				break;				
			}
		}
		String forSplit;
		String[] temp;
		if(index != -1)
		{
			forSplit = title.get(index);
			
			temp = forSplit.split(" ");
			
			positionX = (Integer.valueOf(temp[1])+Integer.valueOf(temp[3])) / 2 ;
			positionY = (Integer.valueOf(temp[2])+Integer.valueOf(temp[4])) / 2 ;
			return true;
		}else
			return false;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}

}
