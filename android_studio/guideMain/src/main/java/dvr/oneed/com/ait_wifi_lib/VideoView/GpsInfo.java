package dvr.oneed.com.ait_wifi_lib.VideoView;

/**
 * Created by hyde on 2016/9/30 0030.
 */
public class GpsInfo {


    public double dwLat  ;//经度
    public double dwLon  ;//纬度
    public double usSpeed ;// 速度
    public long  lAltitude;
    public String byDirection;
    public String byValid;
    public String byVersion;
    public String bReserved;
    public VTime sUTC;

    public GpsInfo(){

    }
    public GpsInfo(double lat,
                   double lon,
                   short speed,
                   long altitude,
                   String direction,
                   String valid,
                   String version,
                   String reserved){
        dwLat =lat;
        dwLon =lon;
        usSpeed =speed;
        lAltitude=altitude;
        byDirection=direction;
        byValid=valid;
        byVersion=version;
        bReserved=reserved;

    }

    public GpsInfo(double lat,
                   double lon,
                   double speed,
                   long altitude,
                   String direction,
                   String valid,
                   String version,
                   String reserved){
        dwLat =lat;
        dwLon =lon;
        usSpeed = speed;
        lAltitude=altitude;
        byDirection=direction;
        byValid=valid;
        byVersion=version;
        bReserved=reserved;

    }


    public GpsInfo(double lat,
                   double lon,
                   short speed,
                   long altitude,
                   String direction,
                   String valid,
                   String version,
                   String reserved,
                   VTime time){
        dwLat =lat;
        dwLon =lon;
        usSpeed =speed;
        lAltitude=altitude;
        byDirection=direction;
        byValid=valid;
        byVersion=version;
        bReserved=reserved;
        sUTC=time;
    }

    public double getDwLat() {
        return dwLat;
    }

    public void setDwLat(Double dwLat) {
        this.dwLat = dwLat;
    }

    public double getDwLon() {
        return dwLon;
    }

    public void setDwLon(Double dwLon) {
        this.dwLon = dwLon;
    }

    public double getUsSpeed() {
        return usSpeed;
    }

    public void setUsSpeed(double usSpeed) {
        this.usSpeed = usSpeed;
    }

    public long getlAltitude() {
        return lAltitude;
    }

    public void setlAltitude(long lAltitude) {
        this.lAltitude = lAltitude;
    }

    public String getByDirection() {
        return byDirection;
    }

    public void setByDirection(String byDirection) {
        this.byDirection = byDirection;
    }

    public String getByValid() {
        return byValid;
    }

    public void setByValid(String byValid) {
        this.byValid = byValid;
    }

    public String getByVersion() {
        return byVersion;
    }

    public void setByVersion(String byVersion) {
        this.byVersion = byVersion;
    }

    public String getbReserved() {
        return bReserved;
    }

    public void setbReserved(String bReserved) {
        this.bReserved = bReserved;
    }

    public VTime getsUTC() {
        return sUTC;
    }

    public void setsUTC(VTime sUTC) {
        this.sUTC = sUTC;
    }





    public class VTime{
        public String iYear;
        /**< Years since 1900 */
        public String iMon;       /**< Months since January - [0,11] */
        public String iDay;       /**< Day of the month - [1,31] */
        public String iHour;      /**< Hours since midnight - [0,23] */
        public String iMin;       /**< Minutes after the hour - [0,59] */
        public String iSec;       /**< Seconds after the minute - [0,59] */
//    unsigned char     iHsec;      /**< Hundredth part of second - [0,99] */
        public String getiYear() {
            return iYear;
        }

        public void setiYear(String iYear) {
            this.iYear = iYear;
        }

        public String getiMon() {
            return iMon;
        }

        public void setiMon(String iMon) {
            this.iMon = iMon;
        }

        public String getiDay() {
            return iDay;
        }

        public void setiDay(String iDay) {
            this.iDay = iDay;
        }

        public String getiHour() {
            return iHour;
        }

        public void setiHour(String iHour) {
            this.iHour = iHour;
        }

        public String getiMin() {
            return iMin;
        }

        public void setiMin(String iMin) {
            this.iMin = iMin;
        }

        public String getiSec() {
            return iSec;
        }

        public void setiSec(String iSec) {
            this.iSec = iSec;
        }


    }


}
