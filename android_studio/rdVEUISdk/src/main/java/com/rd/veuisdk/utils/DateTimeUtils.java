package com.rd.veuisdk.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 时间日期及格式化工具
 *
 * @author abreal
 */
public class DateTimeUtils {

    private static final StringBuilder m_sbFormator = new StringBuilder();
    private static final Formatter m_formatter = new Formatter(m_sbFormator,
            Locale.getDefault());

    /**
     * 毫秒数转换为时间格式化字符串
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false);
    }

    /**
     * 毫秒数转换为时间格式化字符串 支持是否显示小时
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs, boolean existsHours) {
        boolean bNegative = timeMs < 0;// 是否为负数
        if (bNegative) {
            timeMs = -timeMs;
        }
        int totalSeconds = (int) (timeMs / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        m_sbFormator.setLength(0);
        try {
            if (hours > 0 || existsHours) {
                return m_formatter.format("%s%02d:%02d:%02d",
                        bNegative ? "-" : "", hours, minutes, seconds)
                        .toString();
            } else {
                return m_formatter.format("%s%02d:%02d", bNegative ? "- " : "",
                        minutes, seconds).toString();
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 获取相差天数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int daysBetween(final Calendar startDate,
                                  final Calendar endDate) {
        final boolean forward = startDate.before(endDate);
        final int multiplier = forward ? 1 : -1;
        final Calendar date = (Calendar) startDate.clone();
        int daysBetween = 0;
        int fieldAccuracy = 4;
        int field;
        int dayBefore, dayAfter;

        while (forward && date.before(endDate) || !forward
                && endDate.before(date)) {
            switch (fieldAccuracy) {
                case 4:
                    field = Calendar.MILLISECOND;
                    break;
                case 3:
                    field = Calendar.SECOND;
                    break;
                case 2:
                    field = Calendar.MINUTE;
                    break;
                case 1:
                    field = Calendar.HOUR_OF_DAY;
                    break;
                default:
                case 0:
                    field = Calendar.DAY_OF_MONTH;
                    break;
            }
            dayBefore = date.get(Calendar.DAY_OF_MONTH);
            date.add(field, multiplier);
            dayAfter = date.get(Calendar.DAY_OF_MONTH);

            if (dayBefore == dayAfter && date.get(field) == endDate.get(field))
                fieldAccuracy--;
            if (dayBefore != dayAfter) {
                daysBetween += multiplier;
            }
        }
        return daysBetween;
    }

    private final static long HOURS_PER_DAY = 60 * 60 * 1000;

    /**
     * 获取相差小时或分钟
     *
     * @param startDate
     * @param endDate
     * @param hours
     * @return
     */
    public static int hoursOrMinutesBetween(final Calendar startDate,
                                            final Calendar endDate, boolean hours) {
        long beginMS = startDate.getTimeInMillis();
        long endMS = endDate.getTimeInMillis();
        long diff = (endMS - beginMS)
                / (hours ? HOURS_PER_DAY : (HOURS_PER_DAY / 60));
        return (int) diff;
    }


    /**
     * 获取时间
     *
     * @param time
     * @return
     */
    public static String getDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        try {
            return sdf.format(time);
        } catch (Exception e) {
            return sdf.format(System.currentTimeMillis());
        }

    }

    /**
     * 提交微博登陆中的生日
     *
     * @param birthday
     * @return
     */
    public static String getBirthday(String birthday) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.format(new Date(Long.parseLong(birthday)));
        } catch (Exception e) {
            return "1990-01-01";
        }

    }

    /**
     * 毫秒数转换为时间格式化字符串支持毫秒
     *
     * @param timeMs
     * @return
     */
    public static String stringForMillisecondTime(long timeMs,
                                                  boolean isShowMillisecond, boolean alignment) {
        return stringForTime(timeMs, false, true, isShowMillisecond, alignment);
    }

    /**
     * 只返回S:ms
     *
     * @param timeMs 单位毫秒
     * @return
     */
    public static String stringForMillisecondTime(long timeMs) {
        return stringForTime(timeMs, false, false, true, true);
    }

    /**
     * 只返回S:ms
     *
     * @param timeMs 单位毫秒
     * @return
     */
    public static String stringForMillisecondTime(float timeMs) {
        return stringForMillisecondTime((int) timeMs);
    }

    /**
     * 毫秒数转换为时间格式化字符串 支持是否显示小时或毫秒
     *
     * @param timeMs
     * @param existsHours
     * @param existsMillisecond
     * @param alignment         是否需要统计显示宽度，如为true时，5:4.5就为05:04.5
     * @return
     */
    public static String stringForTime(long timeMs, boolean existsHours,
                                       boolean exitsMin, boolean existsMillisecond, boolean alignment) {
        boolean bNegative = timeMs < 0;// 是否为负数
        if (bNegative) {
            timeMs = -timeMs;
        }
        int totalSeconds = (int) (timeMs / 1000);// 总计时间
        int millisecond = (int) (timeMs % 1000) / 100;// 毫秒
        int seconds = totalSeconds % 60;// 秒
        int minutes = (totalSeconds / 60) % 60;// 分
        int hours = totalSeconds / 3600;// 时

        m_sbFormator.setLength(0);
        try {
            // 判断是否支持小时
            if (hours > 0 || existsHours) {
                return m_formatter.format(
                        alignment ? "%s%02d:%02d:%02d" : "%s%d:%d:%d",
                        bNegative ? "-" : "", hours, minutes, seconds)
                        .toString();

            } else if (existsMillisecond) {

                if (exitsMin) {

                    if (minutes > 0 || alignment) {
                        return m_formatter.format(
                                alignment ? "%s%02d:%02d.%d" : "%s%d:%d.%d",
                                bNegative ? "- " : "", minutes, seconds,
                                millisecond).toString();
                    } else {
                        return m_formatter.format(
                                alignment ? "%s%02d.%d" : "%s%d.%d",
                                bNegative ? "- " : "", seconds, millisecond)
                                .toString();
                    }
                } else {
                    int sec = hours * 60 * 60 + minutes * 60 + seconds;
                    return m_formatter.format("%d.%d", sec, millisecond)
                            .toString();
                }
            } else {
                return m_formatter.format(
                        alignment ? "%s%02d:%02d" : "%s%d:%d",
                        bNegative ? "- " : "", minutes, seconds).toString();
            }
        } catch (Exception ex) {
            return "";
        }
    }

}
