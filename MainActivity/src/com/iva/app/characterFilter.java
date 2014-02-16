package com.iva.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

public class characterFilter {

	Context context;
	ArrayList<String> Receive_result;
	ArrayList<String> split_result;
	SharedPreferences pref;
	final int param0_index = 7;
	final int param1_index = 14;
	final int param2_index = 15;

	String[] Buttons = {"홈","뒤로","메뉴","전원","볼륨 업","볼륨 다운"};
	String[] Scrolls = {"업", "다운",	"왼쪽","오른쪽"};

	String[] order_list_all = {"받기","거절","인식","진동", "무음","벨소리", "와이파이",	//0	
			"실행","버튼","통화","음악","검색","터치","스크롤",//1 
	"문자"};				//3

	public characterFilter(Context context,SharedPreferences pref){
		this.context = context;
		this.pref = pref;
	}

	public String setReceive_result(ArrayList<String> receive){	

		Receive_result = receive;
		split_result = new ArrayList<String>();

		for(int i=0;i<Receive_result.size();i++){
			String temp[] = Receive_result.get(i).split(" ");
			String str = "";
			for(int j = 0; j< temp.length;j++){
				str += temp[j];
			}
			split_result.add(str);
		}

		return excute_order(order());
	}

	public String without_(String str){
		String temp[] = str.split(" ");
		String result = "";

		for(int j = 0; j< temp.length;j++){
			result += temp[j];
		}

		return result;
	}

	public int order(){
		for(int i = 0; i<split_result.size();i++){
			for(int j = 0; j<order_list_all.length;j++){
				if(split_result.get(i).contains(order_list_all[j]))
					return j;
			}
		}
		return -1;		//error 명령어 없음.
	} 

	public String excute_order(int order_list_index){
		if(order_list_index == -1)
			//			return null;
			return split_result.toString();
		if(order_list_index < param0_index){
			return order_list_all[order_list_index];
		}else if(order_list_index < param1_index){
			if(order_list_all[order_list_index].equals("실행")){
				String result = search_app_name(split_result);
				if(result != null)
					return order_list_all[order_list_index]+ " " + result;
			}else if(order_list_all[order_list_index].equals("버튼")){
				String result = search_btn_name(split_result);
				if(result != null)
					return order_list_all[order_list_index]+ " " + result;
			}else if(order_list_all[order_list_index].equals("스크롤")){
				String result = search_scroll(split_result);
				if(result != null)
					return order_list_all[order_list_index]+ " " + result;
			}
			else if(order_list_all[order_list_index].equals("통화")){
				String result = search_contact_name(split_result);
				if(result != null)
					return order_list_all[order_list_index]+ " " + result;
			}else if(order_list_all[order_list_index].equals("음악")){
				String result = search_music_name(split_result);
				if(result != null)
					return order_list_all[order_list_index]+ " " + result;
			}else if(order_list_all[order_list_index].equals("검색")){
				String result = delet_target(best_match("검색"), Receive_result.get(0));
				if((result != null) && (!result.equals("")))
					return order_list_all[order_list_index]+ " " + result;
			}else if(order_list_all[order_list_index].equals("터치")){
				String result = delet_target(best_match("터치"), Receive_result.get(0));
				if((result != null) && (!result.equals("")))
					return order_list_all[order_list_index]+ " " + result;		
			}else
				return split_result.toString();
		}else if(order_list_index < param2_index){
			String name = search_contact_name(split_result); 
			String temp;

			int delete_index1=best_match("문자");
			int delete_index2 = best_match(name);

			if((delete_index1 != -1) && (delete_index2 != -1)){
				if(delete_index1 > delete_index2){ 
					temp = delet_target(delete_index1,Receive_result.get(0));
					temp = delet_target(delete_index2,temp);
				}else 
				{
					temp = delet_target(delete_index2,Receive_result.get(0));
					temp = delet_target(delete_index1,temp);
				}

				return order_list_all[order_list_index]+ " " +name +" " + temp;
			}
		}
		return split_result.toString();
	}

	private String search_scroll(ArrayList<String> list) {
		String str = "";
		for(int i =0;i<list.size();i++){
			str += list.get(i) + " ";
		}

		for(int i=0; i<Scrolls.length; i++){
			if(str.contains(Scrolls[i])){
				return Scrolls[i];
			}
		}
		return null;
	}

	public String search_btn_name(ArrayList<String> list){
		String str = "";

		for(int i =0;i<list.size();i++){
			str += list.get(i) + " ";
		}

		for(int i=0; i<Buttons.length; i++){
			if(str.contains(Buttons[i])){
				return Buttons[i];
			}
		}
		return null;
	}

	public String search_app_name(ArrayList<String> list){
		String str = "";

		for(int i =0;i<list.size();i++){
			str += list.get(i) + " ";
		}

		Intent intent = new Intent (Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> pkgAppsList = context.getPackageManager()
				.queryIntentActivities(intent, 0);

		for(int i=0; i<pkgAppsList.size(); i++){ 
			String tem = String.valueOf(pkgAppsList.get(i).activityInfo.loadLabel(context.getPackageManager()));
			if(str.contains(without_(tem).toLowerCase()))
				return without_(tem).toLowerCase();
		}

		for(int i = 0; i<Receive_result.size(); i++){
			String[] temp = Receive_result.get(i).split(" ");
			for(int j = 0; j< temp.length;j++){
				if(pref.contains(temp[j].toLowerCase())){
					return temp[j].toLowerCase();
				}

			}
		}
		//		for(int i = 0; i<list.size();i++){
		//			if(pref.contains(list.get(i))){
		//				return list.get(i);
		//			}
		//		}



		return null;


	}

	public String search_music_name(ArrayList<String> list){
		String str = "";
		String title = null;
		Cursor cursor;

		for(int i =0;i<list.size();i++){
			str += list.get(i) + " ";
		}

		cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
				,null
				,null, null);
		cursor.moveToFirst();
		while(!cursor.isLast()){
			title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

			if(str.contains(without_(title)))
			{
				return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			}	
			cursor.moveToNext();
		}
		return null;
	}

	public String search_contact_name(ArrayList<String> list){

		String str = "";
		String name = null;
		Cursor cursor;

		for(int i =0;i<list.size();i++){
			str += list.get(i) + " ";
		}

		cursor = context.getContentResolver()
				.query(ContactsContract.CommonDataKinds
						.Phone.CONTENT_URI, null, null
						,null, null);
		cursor.moveToFirst();

		while(!cursor.isLast()){
			name = cursor.getString(cursor.getColumnIndex(ContactsContract
					.CommonDataKinds.Phone.DISPLAY_NAME));

			if(str.contains(without_(name)))
			{
				return name;
			}
			cursor.moveToNext();
		}

		return null;
	}

	public int best_match(String caller_string){
		int index = -1;

		if(caller_string != null){
			for(int i =0; i<Receive_result.size();i++){		// 1 line
				String[] temp = Receive_result.get(i).split(" ");

				for(int j=0; j<temp.length;j++){				// splited line
					if(temp[j].contains(caller_string)){ 
						return j;
					}
				}
			}
		}
		return index;
	}

	public String delet_target(int target_index, String sentence){
		String[] temp = sentence.split(" ");
		String result_str = "";
		for(int i = 0; i<temp.length;i++){
			if((i != target_index) && (target_index != -1))
				result_str += temp[i] + " ";
		}

		return result_str;
	}


}
