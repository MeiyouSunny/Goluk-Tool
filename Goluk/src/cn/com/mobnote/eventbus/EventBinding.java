package cn.com.mobnote.eventbus;

public class EventBinding {

	private int mCode;
	private boolean isBinding;

	public EventBinding(int code, boolean binding) {
		mCode = code;
		isBinding = binding;
	}

	public int getCode() {
		return mCode;
	}

	public boolean getBinding() {
		return isBinding;
	}

}
