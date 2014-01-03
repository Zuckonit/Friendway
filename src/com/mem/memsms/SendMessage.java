package com.mem.memsms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SendMessage extends Activity {
	private EditText editText;
	private Button button;
	private Intent intent;
	private SendBroadcast mSendReceiver;

	private HashMap<String, String> hashMap;
	private HashMap<String, String> statusMap = new HashMap<String, String>();
	private HashMap<String, Integer> colorMap = new HashMap<String, Integer>();

	private Queue<String> numbers;
	private int MAX_COUNT = 70;
	private TextView hasnumTV;

	String SENT_SMS_ACTION = "SENT_SMS_ACTION";

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_send_message);
		editText = (EditText) this.findViewById(R.id.message);
		intent = getIntent();
		hashMap = (HashMap<String, String>) intent.getSerializableExtra("data");
		numbers = new LinkedList<String>();
		hasnumTV = (TextView) this.findViewById(R.id.rest_num);
		button = (Button) this.findViewById(R.id.sendmessage);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				numbers.clear();
				final String text = editText.getText().toString();

				if (TextUtils.isEmpty(text.trim())) {
					editText.setHint(R.string.msg_null);
				} else {
					Iterator<Entry<String, String>> iter = hashMap.entrySet()
							.iterator();
					while (iter.hasNext()) {
						Map.Entry<String, String> entry = (Map.Entry<String, String>) iter
								.next();
						String number = entry.getKey();
						String content = entry.getValue();
						numbers.offer(number);
						colorMap.put(number, Color.GRAY);
						Sendmsg(number, content + text);
					}

					Log.i("msg", "colormap size: " + colorMap.size());
					intent.putExtra("data2", colorMap);
					setResult(RESULT_OK, intent);
					SendMessage.this.finish();
				}
			}
		});

		editText.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				temp = s;
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				int number = MAX_COUNT - s.length();
				hasnumTV.setText("剩余" + "" + number + "个字");
				selectionStart = editText.getSelectionStart();
				selectionEnd = editText.getSelectionEnd();
				if (temp.length() > MAX_COUNT) {
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionEnd;
					editText.setText(s);
					editText.setSelection(tempSelection);// 设置光标在最后
				}

			}
		});
	}

	private class SendBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String n = numbers.poll();
			switch (getResultCode()) {
			case RESULT_OK:
				Log.i("msg", "c:ok" + n);
				statusMap.put(n, "0");
				break;

			default:
				Log.i("msg", "c:failed" + n);
				colorMap.put(n, Color.GRAY); // 发送不成功的颜色
				break;
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(mSendReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		mSendReceiver = new SendBroadcast();
		IntentFilter mSendFilter = new IntentFilter(SENT_SMS_ACTION);
		this.registerReceiver(mSendReceiver, mSendFilter);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_message, menu);
		return true;
	}

	private void Sendmsg(String number, String content) {

		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,
				0);
		// sentIntent.putExtra("status", statusMap);
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(number, null, content, sentPI, null);
	}
}
