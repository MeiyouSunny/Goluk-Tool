package com.mobnote.golukmain.internation.login;

import java.io.Serializable;

public class CountryBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 国家名称 */
	public String name;
	/** 国家代号 */
	public String code;
	/** 国家简称 **/
	public String area;

	@Override
	public String toString() {
		return " name:" + name + "  code:" + code + "  area:" + area;
	}

}
