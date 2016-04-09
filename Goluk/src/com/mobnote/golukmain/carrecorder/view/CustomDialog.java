package com.mobnote.golukmain.carrecorder.view;

import com.mobnote.golukmain.R;

import android.app.Dialog; 
import android.content.Context;
import android.view.View;  
import android.widget.Button; 
import android.widget.TextView;

public class CustomDialog extends Dialog implements android.view.View.OnClickListener{ 
	private Context mContext;  
	private TextView title;
	private TextView message;
	private TextView vLine;
	private Button leftButton;
	private Button rightButton;
	private OnLeftClickListener leftListener;
	private OnRightClickListener rightListener;
	 
	public CustomDialog(Context context) { 
		super(context, R.style.CustomDialog);  
		this.mContext=context; 
		setContentView(R.layout.custom_dialog);
		initLayout();
	}
	
	private void initLayout(){ 
		this.title = (TextView)findViewById(R.id.title);
		this.message = (TextView)findViewById(R.id.message);
		this.vLine = (TextView)findViewById(R.id.vLine);
		this.leftButton = (Button)findViewById(R.id.leftButton);
		this.rightButton = (Button)findViewById(R.id.rightButton); 
	}
	
	public void setTitle(String titleText){ 
		this.title.setVisibility(View.VISIBLE);
		this.title.setText(titleText);
	}
	
	public void setTitle(String titleText, int gravity){ 
		this.title.setVisibility(View.VISIBLE);
		this.title.setText(titleText);
		this.title.setGravity(gravity);
	}
	
	public void setMessage(String messageText){ 
		this.message.setVisibility(View.VISIBLE);
		this.message.setText(messageText);
	}
	
	public void setMessage(String messageText, int gravity){ 
		this.message.setVisibility(View.VISIBLE);
		this.message.setText(messageText);
		this.message.setGravity(gravity);
	}
	
	public void setLeftButton(String text, OnLeftClickListener listener){ 
		this.leftButton.setVisibility(View.VISIBLE);
		this.leftButton.setText(text);
		this.leftListener=listener;
		this.leftButton.setOnClickListener(this); 
	}
	 
	public void setRightButton(String text, OnRightClickListener listener){ 
		this.rightButton.setVisibility(View.VISIBLE);
		this.vLine.setVisibility(View.VISIBLE);
		this.rightButton.setText(text);
		this.rightListener=listener;
		this.rightButton.setOnClickListener(this); 
	}
	 
	public interface OnLeftClickListener{
		public void onClickListener(); 
	}
	
	public interface OnRightClickListener{
		public void onClickListener(); 
	}
 
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.leftButton) {
			dismiss();
			if(this.leftListener != null)
				this.leftListener.onClickListener();
		} else if (id == R.id.rightButton) {
			dismiss();
			if(this.rightListener != null)
				this.rightListener.onClickListener();
		} else {
		}
	} 
	
}
