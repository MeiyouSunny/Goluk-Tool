package dvr.oneed.com.ait_wifi_lib.bean;

/**
 * Created by Administrator on 2016/8/8 0008.
 */
public class TitleBrowser extends BaseBrowser {

    public String dateTitle;

    public TitleBrowser(String date) {
        this.dateTitle = date;
        this.itemType = TYPE_ITEM_TITLE;
    }

}
