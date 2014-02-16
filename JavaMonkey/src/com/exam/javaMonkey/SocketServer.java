package com.exam.javaMonkey;

import java.io.*;
import java.net.*;
import java.util.*;

public class SocketServer {

	private ServerSocket serverSocket=null;
	private Socket socket=null;
	private InputStream is = null;
	private InputStreamReader reader = null;	
	private OutputStream writer = null;
	private int port;
	BufferedReader br;
	
	public void init(){
		try {

			serverSocket=new ServerSocket(port);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SocketServer(int port){
		this.port = port;
	}

	public String startSocket() throws IOException {

		socket=serverSocket.accept();
		is = socket.getInputStream();
		reader = new InputStreamReader(is);
		br = new BufferedReader(reader);

		String str = null;
		try {
			str = br.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		System.out.println("받은 데이터:"+str);

		return str;

	}

	public void write(String str){
		try {
			writer = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(writer);
			osw.write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy(){
		try {
			serverSocket.close();
			socket.close();
			reader.close();
			is.close();
			//			javaMonkey.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		String[] Buttons2 = {"HOME","BACK","MENU","POWER","VOLUME_UP","VOLUME_DOWN","SCROLL_UP","SCROLL_DOWN","SCROLL_LEFT","SCROLL_RIGHT","TAKE"};
		ArrayList<String> order_list = new ArrayList<String>(Arrays.asList(Buttons2));
		
		JavaMonkey javaMonkey;
		javaMonkey = new JavaMonkey();
		javaMonkey.init();
		System.out.println("start server");
		while(true){
			try {
				SocketServer ss = new SocketServer(5090);
				
				ss.init();
				String str = ss.startSocket().trim();
				
				if(order_list.contains(str)){
					javaMonkey.Key_Event(str);
				}
				else{
					handlePosition hp = new handlePosition(javaMonkey.getTitle(), javaMonkey.getText());
					if(hp.dor(str))
						javaMonkey.touchxy(hp.getPositionX(), hp.getPositionY());					
				}
					
				ss.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
//		CharcterRecognizer cr = new CharcterRecognizer("kor");
//		cr.recog();
	}
}