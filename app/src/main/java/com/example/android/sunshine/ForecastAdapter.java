package com.example.android.sunshine;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherContract;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    //    private ArrayList<Weather> mWeatherData;
    private final Context mContext;
    private Cursor mCursor;
    private static final int VIEW_TYPE_TODAY=0;
    private static final int VIEW_TYPE_FUTURE_DAY=1;
    private static String date_to_display;
    private static String currentDate;


    private boolean mUseTodayLayout;

    // COMPLETED (3) Create a final private ForecastAdapterOnClickHandler called mClickHandler
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView*/
    private ForecastAdapterOnClickListener mOnClickListener;

    // COMPLETED (1) Add an interface called ForecastAdapterOnClickHandler
    // COMPLETED (2) Within that interface, define a void method that access a String as a parameter

    /**
     * The interface that receives onClick messages.
     */

    public interface ForecastAdapterOnClickListener {
        void onListItemClick(Long date);
    }


    // COMPLETED (4) Add a ForecastAdapterOnClickHandler as a parameter to the constructor and store it in mClickHandler

    /**
     * Creates a ForecastAdapter.
     **/

    public ForecastAdapter(Context context, ForecastAdapterOnClickListener onClickListener) {
        mContext=context;
        mOnClickListener = onClickListener;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }


    // COMPLETED (5) Implement OnClickListener in the ForecastAdapterViewHolder class

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mDayTextView;
        private TextView mDescriptionTextView;
        private TextView mTemperatureTextView;
        private ImageView mImageTextView;
        private TextView mTimeView;

        ForecastViewHolder(View view) {
            super(view);
            mDayTextView = (TextView) view.findViewById(R.id.day_today);
            mDescriptionTextView = (TextView) view.findViewById(R.id.description);
            mTemperatureTextView = (TextView) view.findViewById(R.id.temperature);
            mImageTextView = (ImageView) view.findViewById(R.id.image_view);
            mTimeView=(TextView) view.findViewById(R.id.time);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mCursor.moveToPosition(clickedPosition);
            long date=mCursor.getLong(mCursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
            mOnClickListener.onListItemClick(date);
        }
    }


    // COMPLETED (24) Override onCreateViewHolder
    // COMPLETED (25) Within onCreateViewHolder, inflate the list item xml into a view
    // COMPLETED (26) Within onCreateViewHolder, return a new ForecastAdapterViewHolder with the above view passed in as a parameter

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent   The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (which ours doesn't) you
     *                 can use this viewType integer to provide a different layout. See
     *                 {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                 for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        int layoutId;
        if(viewType==VIEW_TYPE_TODAY){
            layoutId=R.layout.list_item_forecast_today;
        }else {
            layoutId=R.layout.list_item;
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(layoutId,parent,false);
        return new ForecastViewHolder(view);

    }


    // COMPLETED (27) Override onBindViewHolder
    // COMPLETED (28) Set the text of the TextView to the weather for this list item's position

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the
     *                 contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {



        mCursor.moveToPosition(position);

        int dateColoumnIndex=mCursor.getColumnIndex(WeatherEntry.COLUMN_DATE);
        int tempColoumnIndex=mCursor.getColumnIndex(WeatherEntry.COLOUMN_TEMP);
        int descriptionColoumnIndex=mCursor.getColumnIndex(WeatherEntry.COLOUMN_DESCRIPTION);

        long date=mCursor.getLong(dateColoumnIndex);
        String date_to_display=getDate(date);


        String temperature=mCursor.getString(tempColoumnIndex);
        String description=mCursor.getString(descriptionColoumnIndex);
        String time=getTime(date);

        holder.mDayTextView.setText(date_to_display);
        holder.mTemperatureTextView.setText(temperature);
        holder.mDescriptionTextView.setText(description);

        holder.mTimeView.setText(time);
        setWeatherImage(description,holder);


    }

    private static void setWeatherImage(String description , ForecastViewHolder holder){
        if(description.equals("light rain"))
            holder.mImageTextView.setImageResource(R.drawable.ic_light_rain);
        else if(description.equals("few clouds")){
            holder.mImageTextView.setImageResource(R.drawable.ic_light_clouds);
        }else if(description.equals("scattered clouds")){
            holder.mImageTextView.setImageResource(R.drawable.ic_cloudy);
        }else if(description.equals("broken clouds")){
            holder.mImageTextView.setImageResource(R.drawable.ic_cloudy);
        }else if(description.equals("overcast clouds")){
            holder.mImageTextView.setImageResource(R.drawable.art_storm);
        }else if(description.equals("moderate rain")){
            holder.mImageTextView.setImageResource(R.drawable.ic_light_rain);
        }else if(description.equals("clear sky")){
            holder.mImageTextView.setImageResource(R.drawable.art_clear);
        }else if(description.equals("heavy intensity rain")){
            holder.mImageTextView.setImageResource(R.drawable.ic_rain);
        }
        else{
            holder.mImageTextView.setImageResource(R.drawable.map_marker);
        }
    }



    private static String getDay(long time) {
        Date date = new Date(time * 1000);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        return dayFormat.format(date);
    }

    public static String getTime(long time) {
        Date date = new Date(time * 1000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a");
        return timeFormat.format(date);
    }

// COMPLETED (29) Override getItemCount
    // COMPLETED (30) Return 0 if mWeatherData is null, or the size of mWeatherData if it is not null

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    // COMPLETED (31) Create a setWeatherData method that saves the weatherData to mWeatherData
    // COMPLETED (32) After you save mWeatherData, call notifyDataSetChanged

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        // After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }


    public static String getDate(long time){
        Date date = new Date(time * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        date_to_display=dateFormat.format(date);
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        String todayAsString = dateFormat.format(today);
        String tomorrowAsString = dateFormat.format(tomorrow);

        Log.v("ForecastAdapter", "Tomorrows date is " + tomorrowAsString);

        currentDate=date_to_display;
        if (todayAsString.equals(date_to_display)) {
            date_to_display = "Today";
        }
        else if (tomorrowAsString.equals(date_to_display)) {
            date_to_display = "Tomorrow";
        }else{
            date_to_display=getDay(time);
        }

        return date_to_display;
    }


    @Override
    public int getItemViewType(int position) {

        Log.v("ForecastAdapter","Porttrait:"+mUseTodayLayout+" position "+String.valueOf(position));

        if(mUseTodayLayout==true && position==0){
            Log.v("ForecastAdapter","value of viewType is "+String.valueOf(VIEW_TYPE_TODAY));
            return VIEW_TYPE_TODAY;
        }else{
            return VIEW_TYPE_FUTURE_DAY;
        }
    }
}


