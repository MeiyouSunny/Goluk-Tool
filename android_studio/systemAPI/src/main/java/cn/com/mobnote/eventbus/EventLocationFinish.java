package cn.com.mobnote.eventbus;

public class EventLocationFinish {
	int opCode;
	int locationType;
	double lat;
	double lon;
	float radius;
	float speed;
	float direction;
	String cityCode;
	String address;

	public EventLocationFinish(int opCode, int locationType, double lat,
			double lon, float radius, float speed, float direction, String address,
			String cityCode) {
		this.opCode = opCode;
		this.locationType = locationType;
		this.lat = lat;
		this.lon = lon;
		this.radius = radius;
		this.speed = speed;
		this.direction = direction;
		this.cityCode = cityCode;
		this.address = address;
	}

	public EventLocationFinish() {}

	public EventLocationFinish(int opCode, String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}

	public int getLocationType() {
		return locationType;
	}

	public void setLocationType(int locationType) {
		this.locationType = locationType;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
}
