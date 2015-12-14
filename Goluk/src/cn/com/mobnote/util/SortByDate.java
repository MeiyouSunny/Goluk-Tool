package cn.com.mobnote.util;

import java.util.Comparator;

public class SortByDate implements Comparator<String> {

	@Override
	public int compare(String s1, String s2) {
		// TODO Auto-generated method stub
		String[] videos1 = s1.split("_");
		String[] videos2 = s2.split("_");
		String date1 = "";
		String date2 = "";
		if (videos1.length == 3) {
			date1 = videos1[1];
			date1 = "20" + date1;
		} else if (videos1.length > 3) {
			if (videos1.length > 3) {
				date1 = videos1[2];
			}
		}
		
		if (videos2.length == 3) {
			date2 = videos2[1];
			date2 = "20" + date2;
		} else if (videos2.length > 3) {
			if (videos2.length > 3) {
				date2 = videos2[2];
			}
		}
		return (date2.compareTo(date1));
	}

}
