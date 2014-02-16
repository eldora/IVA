package com.iva.app;

import java.util.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class Tab2_Applist extends Activity {
	/**
	 * @uml.property  name="pref"
	 * @uml.associationEnd  
	 */
	SharedPreferences pref; 
	/**
	 * @uml.property  name="editor"
	 * @uml.associationEnd  
	 */
	SharedPreferences.Editor editor;
	/**
	 * @uml.property  name="intent"
	 * @uml.associationEnd  
	 */
	Intent intent;
	/**
	 * @uml.property  name="listView"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="android.content.pm.ResolveInfo"
	 */
	ListView listView;
	/**
	 * @uml.property  name="context"
	 * @uml.associationEnd  readOnly="true"
	 */
	Context context;
	/**
	 * @uml.property  name="editText"
	 * @uml.associationEnd  
	 */
	private EditText editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_applist);

		Toast.makeText(Tab2_Applist.this, "어플이름을 부르기 쉬운 " +
				"이름으로 바꿀 수 있습니다"
				,Toast.LENGTH_LONG).show();

		pref = getSharedPreferences(getResources().getString(R.string.save)
				,MODE_MULTI_PROCESS); 
		editor = pref.edit();

		intent = new Intent (Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> pkgAppsList = getPackageManager()
				.queryIntentActivities(intent, 0);

		for(int i=0; i<pkgAppsList.size(); i++){
			editor.putString(String.valueOf(pkgAppsList.get(i)
					.activityInfo.loadLabel(getPackageManager())).toLowerCase() 
					,pkgAppsList.get(i).activityInfo.packageName);
		}
		editor.commit();	

		MyAdapter adapter = new MyAdapter(this, R.layout.voice_applistitem
				,pkgAppsList);
		listView = (ListView) findViewById(R.id.launcherList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0
					,View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				context = Tab2_Applist.this;
				AlertDialog.Builder builder;
				AlertDialog dialog;
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.voice_applistdialog, 
						(ViewGroup)findViewById(R.id.customdialog_layout));	
				editText = (EditText)layout.findViewById(R.id.edittext1);

				builder = new AlertDialog.Builder(context);
				builder.setView(layout);
				dialog = builder.create();
				dialog.setTitle("입력");

				dialog.setButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						editor = pref.edit(); 
						String AppName = editText.getText().toString(); 
						editor.putString(AppName, pkgAppsList.get(arg2)
								.activityInfo.packageName);
						editor.commit();
						Toast.makeText(Tab2_Applist.this
								,"이름이 변경 되었습니다."
								, Toast.LENGTH_LONG).show();
					}
				});
				dialog.setButton2("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
	}
	//
	public class MyAdapter extends ArrayAdapter<ResolveInfo> {
		List<ResolveInfo> child;

		public MyAdapter(Context context, int textViewResourceId
				,List<ResolveInfo> objects) {
			super(context, textViewResourceId, objects);
			child = objects;
		}
		@Override
		public int getCount() {
			return child.size();
		}
		@Override
		public ResolveInfo getItem(int position) {
			return child.get(position);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getApplicationContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.voice_applistitem, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.ivIcon);
			TextView name = (TextView) convertView.findViewById(R.id.tvName);

			ResolveInfo temp = child.get(position);
			icon.setImageDrawable(temp.loadIcon(getPackageManager()));
			name.setText(temp.loadLabel(getPackageManager()));

			return convertView;
		}
	}
}