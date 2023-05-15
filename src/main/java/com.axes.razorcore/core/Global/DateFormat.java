package com.axes.razorcore.core.Global;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DateFormat {
    /// Year-Month-Date 6 Character Date Representation
    public  String SixCharacter = "yyMMdd";
    /// YYYY-MM-DD Eight Character Date Representation
    public  String EightCharacter = "yyyyMMdd";
    /// Daily and hourly time format
    public  String TwelveCharacter = "yyyyMMdd HH:mm";
    /// JSON Format Date Representation
    public static String JsonFormat = "yyyy-MM-ddTHH:mm:ss";
    /// MySQL Format Date Representation
    public  String DB = "yyyy-MM-dd HH:mm:ss";
    /// QuantConnect UX Date Representation
    public  String UI = "yyyy-MM-dd HH:mm:ss";
    /// en-US Short Date and Time Pattern
    public  String USShort = "M/d/yy h:mm tt";
    /// en-US Short Date Pattern
    public  String USShortDateOnly = "M/d/yy";
    /// en-US format
    public  String US = "M/d/yyyy h:mm:ss tt";
    /// en-US Date format
    public  String USDateOnly = "M/d/yyyy";
    /// Date format of QC forex data
    public  String Forex = "yyyyMMdd HH:mm:ss.ffff";
    /// Date format of FIX Protocol UTC Timestamp without milliseconds
    public  String FIX = "yyyyMMdd-HH:mm:ss";
    /// Date format of FIX Protocol UTC Timestamp with milliseconds
    public  String FIXWithMillisecond = "yyyyMMdd-HH:mm:ss.fff";
    /// YYYYMM Year and Month Character Date Representation (used for futures)
    public  String YearMonth = "yyyyMM";
}
