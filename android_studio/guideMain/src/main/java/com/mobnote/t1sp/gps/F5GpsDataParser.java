package com.mobnote.t1sp.gps;

import android.os.AsyncTask;

import com.mobnote.t1sp.util.CollectionUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by HuangJW on 2020-7-25 1:28.
 * Mail: 499655607@qq.com
 * Powered by Vcolco
 */
public class F5GpsDataParser extends AsyncTask<String, Void, List<GPSData>> {
    private List<GPSData> mGpsList;
    private GpsDataListener mListener;

    public F5GpsDataParser(GpsDataListener listener) {
        this.mListener = listener;
        this.mGpsList = new ArrayList();
    }

    protected List<GPSData> doInBackground(String... strings) {
        String path = strings[0];
        try {
            parseGps(path);
            sortGpsList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.mGpsList;
    }

    private void sortGpsList() {
        if (CollectionUtils.isEmpty(mGpsList))
            return;

        Collections.sort(mGpsList, new Comparator<GPSData>() {
            @Override
            public int compare(GPSData o1, GPSData o2) {
                return (int) (o1.time - o2.time);
            }
        });
    }

    protected void onPostExecute(List<GPSData> gpsData) {
        if (this.mListener != null) {
            this.mListener.getGpsData(gpsData);
        }

    }

    private void parseGps(String path) throws Exception {
        RandomAccessFile reader = new RandomAccessFile(path, "r");

        // 长度
        int length;
        // 类型
        byte[] bytesType = new byte[4];

        // ftyp
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // frea
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // mdat
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // moov
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);

        // moov下一级
        // mvhd
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // track 1
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // track 2
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // udta
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);
        reader.skipBytes(length - 8);

        // gps
        length = reader.readInt();
        reader.read(bytesType);
        System.out.println(new String(bytesType) + " : " + length);

        // gps版本和记录条数
        int gpsVersion = reader.readInt();
        int gpsCount = reader.readInt();
        System.out.println("Gps version: " + gpsVersion + ",  count:" + gpsCount);

        // 记录列表<位置,长度>
        Map<Integer, Integer> recordList = new HashMap<>(gpsCount);
        for (int i = 0; i < gpsCount; i++) {
            recordList.put(reader.readInt(), reader.readInt());
        }

        // 读取gps数据
        Iterator iterator = recordList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
            GPSData gpsData = readOneGpsRecord(reader, entry.getKey(), entry.getValue());
            mGpsList.add(gpsData);
        }

        reader.close();
    }

    private GPSData readOneGpsRecord(RandomAccessFile reader, Integer key, Integer value) {
        GPSData gpsData = new GPSData();
        try {
            // Seek到每条Gps记录的起始位置,读取指定长度的byte
            reader.seek(key);

//            byte[] bytes = new byte[80];
//            reader.read(bytes);
//            System.out.println(HexConvertTools.bytesToHex(bytes));

            // 跳过16个字节
            reader.skipBytes(4 * 4);

            byte[] bytes = new byte[4];

            reader.read(bytes);
            int hour = HexConvertTools.lBytesToInt(bytes);
            System.out.print(HexConvertTools.lBytesToInt(bytes) + ":");

            reader.read(bytes);
            int minutes = HexConvertTools.lBytesToInt(bytes);
            System.out.print(HexConvertTools.lBytesToInt(bytes) + " :");

            reader.read(bytes);
            int second = HexConvertTools.lBytesToInt(bytes);
            System.out.print(HexConvertTools.lBytesToInt(bytes) + "   ");

            reader.read(bytes);
            int year = HexConvertTools.lBytesToInt(bytes) + 2000;
            System.out.print(HexConvertTools.lBytesToInt(bytes) + "-");

            reader.read(bytes);
            int month = HexConvertTools.lBytesToInt(bytes);
            System.out.print(HexConvertTools.lBytesToInt(bytes) + "-");

            reader.read(bytes);
            int day = HexConvertTools.lBytesToInt(bytes);
            System.out.print(HexConvertTools.lBytesToInt(bytes) + "  status:");

            gpsData.time = parseTime(year, month, day, hour, minutes, second);

            byte[] statusbyte = new byte[1];
            reader.read(statusbyte);
            int status = (statusbyte[0] & 0x000000FF);
            System.out.print(status + " Lat/Lon:");
            gpsData.status = status;

            // 跳过3个无用的char字段
            reader.skipBytes(3);

            reader.read(bytes);
            System.out.print(HexConvertTools.lBytesToFloat(bytes) + "/");
            gpsData.latitude = HexConvertTools.lBytesToFloat(bytes);

            reader.read(bytes);
            System.out.print(HexConvertTools.lBytesToFloat(bytes) + " - speed:");
            gpsData.longitude = HexConvertTools.lBytesToFloat(bytes);

            reader.read(bytes);
            System.out.print(HexConvertTools.lBytesToFloat(bytes) + " - angle:");
            gpsData.speed = (int) HexConvertTools.lBytesToFloat(bytes);

            reader.read(bytes);
            System.out.print(HexConvertTools.lBytesToFloat(bytes) + "\n");
            gpsData.angle = (int) HexConvertTools.lBytesToFloat(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gpsData;
    }

    private long parseTime(int year, int month, int day, int hour, int minute, int second) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        return cal.getTime().getTime();
    }

    public interface GpsDataListener {
        void getGpsData(List<GPSData> gpsDataList);
    }

}


