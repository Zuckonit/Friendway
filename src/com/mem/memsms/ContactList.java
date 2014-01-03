package com.mem.memsms;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactList extends Activity {
	Context mContext = null;
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;
	private static final int PHONES_NUMBER_INDEX = 1;
	private static final int PHONES_PHOTO_ID_INDEX = 2;
	private static final int PHONES_CONTACT_ID_INDEX = 3;
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID, Phone.CONTACT_ID };

	private List<HashMap<String, Object>> mData;
	private MyAdapter adapter;
	private ListView listView;
	private TextView textView;
	private CheckBox antiCheckBox;
	private CheckBox checkBox;
	private CharSequence titleSend;
	private CharSequence titleSelectOne;
	private AlertDialog.Builder changeNameDialog;
	private EditText newNameEditText;
	private TextView newNameTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		changeNameDialog = new AlertDialog.Builder(ContactList.this);
		mData = getData();
		adapter = new MyAdapter(this, mData);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_contact_list);
		titleSend = getResources().getText(R.string.send);
		titleSelectOne = getResources().getText(R.string.send);
		listView = (ListView) this.findViewById(R.id.listView1);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter1, View v, int pos,
					long arg) {
				CheckBox cbox = (CheckBox) v.findViewById(R.id.checkbox);
				cbox.toggle();
				final boolean flag = cbox.isChecked();
				cbox.setChecked(flag);
				adapter.selectItem(pos, flag);
				Log.i("Contact", "clicked");
				textView.setText(titleSend + "(" + adapter.getCheckedCount()
						+ ")");

				if (adapter.getCheckedCount() == 0) {
					textView.setText(titleSelectOne);
				}
			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter1, View v,
					int pos, long arg) {
				// TODO Auto-generated method stub
				Log.i("msg", "long clicked");

				Drawable db = bitmap2Drawable((Bitmap) mData.get(pos).get(
						"head"));
				final int pos1 = pos;
				newNameTextView = (TextView) v.findViewById(R.id.name);
				newNameEditText = new EditText(ContactList.this);
				changeNameDialog.setTitle((String) mData.get(pos).get("name"))
						.setIcon(db).setView(newNameEditText)
						.setPositiveButton("确定", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								adapter.updateName(pos1, newNameEditText
										.getText().toString());
							}
						}).setNegativeButton("取消", null).show();

				return false;
			}

		});

		textView = (TextView) this.findViewById(R.id.title);
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HashMap<String, String> data = getSelected();
				if (data.isEmpty()) {
					textView.setText(R.string.select_one);
				} else {
					textView.setText(R.string.send);
					Intent intent = new Intent();
					intent.setClass(ContactList.this, SendMessage.class);
					intent.putExtra("data", data);
					startActivityForResult(intent, 1);
				}

			}
		});

		checkBox = (CheckBox) this.findViewById(R.id.select_all);
		antiCheckBox = (CheckBox) this.findViewById(R.id.anti_select_all);

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				adapter.setSelectAll(isChecked);
				textView.setText(titleSend + "(" + adapter.getCheckedCount()
						+ ")");
				if (isChecked) {
					antiCheckBox.setChecked(false);
				}
				if (adapter.getCheckedCount() == 0) {
					textView.setText(R.string.select_one);
				}
			}
		});

		antiCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				adapter.antiSelect();
				textView.setText(titleSend + "(" + adapter.getCheckedCount()
						+ ")");
				if (isChecked) {
					checkBox.setChecked(false);
				}
				if (adapter.getCheckedCount() == 0) {
					textView.setText(titleSelectOne);
				}
			}
		});

	}

	public Drawable bitmap2Drawable(Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
		Drawable d = (Drawable) bd;
		return d;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			@SuppressWarnings("unchecked")
			HashMap<String, Integer> d = (HashMap<String, Integer>) data
					.getSerializableExtra("data2");

			adapter.changeColor(d);
			break;

		default:
			break;
		}
	}

	public HashMap<String, String> getSelected() {
		HashMap<String, String> selected = new HashMap<String, String>();
		for (int i = 0; i < mData.size(); i++) {
			if (mData.get(i).get("cbox") == (Boolean) true) {
				selected.put(mData.get(i).get("number").toString(), mData
						.get(i).get("name").toString());
			}
		}
		return selected;
	}

	private class ViewHolder {
		ImageView head;
		TextView number;
		TextView name;
		CheckBox cBox;
	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<HashMap<String, Object>> listData;
		private ViewHolder holder = null;

		public MyAdapter(Context context, List<HashMap<String, Object>> listData) {
			super();
			this.mInflater = LayoutInflater.from(context);
			this.listData = listData;
		}

		private void updateName(int pos, CharSequence charSequence) {
			this.listData.get(pos).put("name", charSequence);
			dataChanged();
		}

		@Override
		public int getCount() {
			return this.listData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return this.listData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		private void changeColor(HashMap<String, Integer> data) {
			int size = getCount();
			String number;
			for (int i = 0; i < size; i++) {
				number = this.listData.get(i).get("number").toString();
				if (data.containsKey(number)) {
					this.listData.get(i).put("color", data.get(number));
					// Log.i("msg", "we have this number" + data.get("number"));
				}
			}
			dataChanged();
		}

		private void dataChanged() {
			notifyDataSetChanged();
		}

		private void selectItem(int pos, boolean flag) {
			this.listData.get(pos).put("cbox", flag);
			dataChanged();
		}

		private int getCheckedCount() {
			int count = 0;
			int size = getCount();
			for (int i = 0; i < size; i++) {
				if (this.listData.get(i).get("cbox") == (Boolean) true) {
					count++;
				}
			}
			return count;
		}

		private void setSelectAll(boolean flag) {
			int count = this.listData.size();
			for (int i = 0; i < count; i++) {
				this.listData.get(i).put("cbox", flag);
			}
			dataChanged();
		}

		private void antiSelect() {
			int count = this.listData.size();
			for (int i = 0; i < count; i++) {
				boolean flag = (Boolean) this.listData.get(i).get("cbox");
				this.listData.get(i).put("cbox", !flag);
			}
			dataChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final HashMap<String, Object> entry = this.listData.get(position);

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.contact_list_item,
						null);
				holder.head = (ImageView) convertView.findViewById(R.id.head);
				holder.number = (TextView) convertView
						.findViewById(R.id.number);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.cBox = (CheckBox) convertView
						.findViewById(R.id.checkbox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.name.setText(entry.get("name").toString());
			holder.number.setText(entry.get("number").toString());
			holder.head.setImageBitmap((Bitmap) entry.get("head"));
			holder.cBox.setChecked((Boolean) entry.get("cbox"));
			convertView.setBackgroundColor((Integer) entry.get("color"));
			return convertView;
		}
	}

	private List<HashMap<String, Object>> getData() {
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = null;
		ContentResolver contentResolver = this.getContentResolver();
		Cursor cursor = contentResolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, "sort_key asc");
		while (cursor.moveToNext()) {
			String phone = cursor.getString(PHONES_NUMBER_INDEX);
			if (TextUtils.isEmpty(phone)) {
				continue;
			}

			String name = cursor.getString(PHONES_DISPLAY_NAME_INDEX);
			Long contactid = cursor.getLong(PHONES_CONTACT_ID_INDEX);
			Long photoid = cursor.getLong(PHONES_PHOTO_ID_INDEX);
			Bitmap contactPhoto = null;
			if (photoid > 0) {
				Uri uri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI, contactid);
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(contentResolver, uri);
				contactPhoto = BitmapFactory.decodeStream(input);
			} else {
				contactPhoto = BitmapFactory.decodeResource(getResources(),
						R.drawable.contact_photo);
			}

			map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("number", phone);
			map.put("head", contactPhoto);
			map.put("cbox", false);
			map.put("color", Color.WHITE);
			list.add(map);
		}

		cursor.close();
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

}
