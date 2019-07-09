package com.example.android.sunshine;

public class Weather {

    private String mTemp;
    private String mDate;
    private String mTime;
    private String mDescription;
    private String mDay;

    public Weather(String Temp, String Day, String Date , String Time, String Description){
        mTemp=Temp;
        mDay=Day;
        mDate=Date;
        mTime=Time;
        mDescription=Description;

    }

    public String getTemp(){
        return mTemp;
    }

    public String getDay() {
        return mDay;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public String getDescription() {
        return mDescription;
    }
}
