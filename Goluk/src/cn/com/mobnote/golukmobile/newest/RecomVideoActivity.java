package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RecomVideoActivity extends Activity implements OnClickListener{
	EditText text;
	TextView number;
	private CharSequence temp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_video_dialog);
		
		text = (EditText)findViewById(R.id.text);
		number = (TextView)findViewById(R.id.number);
		findViewById(R.id.cancle).setOnClickListener(this);
		findViewById(R.id.tuijian).setOnClickListener(this);
		text.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				String str = text.getText().toString();
	            int len=str.length();
                if (temp.length() > 50) {
                	str = str.substring(0, 50); 
                	text.setText(str);
                	len=50;
                }
                
                len = 50 - len;
                number.setText(len+"");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				  temp = s;
			}      	
        }); 
		
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.cancle:
			finish();
			break;
		case R.id.tuijian:
			finish();
			
			
			break;

		default:
			break;
		}
	}
	
}
