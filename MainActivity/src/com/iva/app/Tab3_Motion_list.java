package com.iva.app;

import java.util.ArrayList;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class Tab3_Motion_list extends Activity {

	String[] Buttons = {"홈","뒤로","메뉴","전원","볼륨 업","볼륨 다운","스크롤 업","스크롤 다운","스크롤 왼쪽","스크롤 오른쪽","인식"};
	String[] Buttons2 = {"HOME","BACK","MENU","POWER","VOLUME_UP","VOLUME_DOWN","SCROLL_UP","SCROLL_DOWN","SCROLL_LEFT","SCROLL_RIGHT","TAKE"};
	ListView listView;
	SharedPreferences pref; 
	SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("진동");
		list.add("무음");
		list.add("벨소리");
		list.add("와이파이");
		list.add("받기");
		list.add("거절");
		list.add("인식");
		list.add("실행 <어플리케이션 이름>");
		list.add("버튼 <버튼 이름> -(홈, 뒤로, 메뉴, 전원, 볼륨 업, 볼륨 다운)");
		list.add("스크롤 <방향> -(업, 다운, 왼쪽, 오른쪽)");
		list.add("통화 <상대 이름>");
		list.add("음악 <음악 제목>");
		list.add("검색 <검색어>");
		list.add("터치 <현재 화면의 문자열>");
		list.add("문자 <상대 이름> <내용>");

		pref = getSharedPreferences(getResources().getString(R.string.save)
				,MODE_MULTI_PROCESS); 
		editor = pref.edit();

		listView = new android.widget.ListView(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		setContentView(listView); 
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.vibration), Toast.LENGTH_SHORT).show();			
					break;
				case 1:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.silent), Toast.LENGTH_SHORT).show();			
					break;
				case 2:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.normal), Toast.LENGTH_SHORT).show();			
					break;
				case 3:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.wifi), Toast.LENGTH_SHORT).show();			
					break;
				case 4:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.accept), Toast.LENGTH_SHORT).show();			
					break;
				case 5:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.reject), Toast.LENGTH_SHORT).show();			
					break;
				case 6:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.recognition), Toast.LENGTH_SHORT).show();			
					break;
				case 7:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.action), Toast.LENGTH_SHORT).show();			
					break;
				case 8:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.button), Toast.LENGTH_SHORT).show();			
					break;
				case 9:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.scroll), Toast.LENGTH_SHORT).show();			
					break;
				case 10:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.call), Toast.LENGTH_SHORT).show();			
					break;
				case 11:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.music), Toast.LENGTH_SHORT).show();			
					break;
				case 12:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.search), Toast.LENGTH_SHORT).show();			
					break;
				case 13:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.touch), Toast.LENGTH_SHORT).show();			
					break;
				case 14:
					Toast.makeText(Tab3_Motion_list.this, getString(R.string.sms), Toast.LENGTH_SHORT).show();			
					break;
				}
			}
		});

		for(int i =0; i<Buttons.length; i++){
			editor.putString(Buttons[i], Buttons2[i]);
		}
		editor.commit();
	}
}
