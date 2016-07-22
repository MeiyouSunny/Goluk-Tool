package cn.com.mobnote.eventbus;

public class EventShortLocationFinish {

    String shortAddress;
    Double lat;
    Double lon;
    public EventShortLocationFinish(String address,double lat ,double lon){
        this.shortAddress = address;
        this.lat = lat;
        this.lon = lon;
    }
    public String getShortAddress() {
        return shortAddress;
    }
    public void setShortAddress(String shortAddress) {
		this.shortAddress = shortAddress;
	}

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
