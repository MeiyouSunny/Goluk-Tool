package cn.com.tiros.baidu;

public class LocationAddressDetailBean {

    public int state;

    /**
     * 国家
     */
    public String countryName;
    /**
     * 城市
     */
    public String cityName;
    /**
     * 区县
     */
    public String subLocatity;
    /**
     * 街道
     */
    public String throughFrare;
    /**
     * 详细地址
     */
    public String detail;
    /**
     * 地区
     **/
    public String adminArea;

    @Override
    public String toString() {
        return "LocationAddressDetailBean{" +
                "state=" + state +
                ", countryName='" + countryName + '\'' +
                ", cityName='" + cityName + '\'' +
                ", subLocatity='" + subLocatity + '\'' +
                ", throughFrare='" + throughFrare + '\'' +
                ", detail='" + detail + '\'' +
                ", adminArea='" + adminArea + '\'' +
                '}';
    }
}
