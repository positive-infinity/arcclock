package de.pnpq.arcclock.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.pnpq.arcclock.R;
import de.pnpq.arcclock.helpers.ColorHelper;

public class DrawingOptions
{
    private Context mContext;
    private int mRadius;

    private int mBorder;
    private int mArcGap;
    private int mArcWidth;

    private int mFreeSpace;

    private int mInverseFactor;
    private boolean m24HourMode;
    private boolean mDrawOutline;

    private int mBgColor;
    private int mHourArcColor;
    private int mMinuteArcColor;
    private int mSecondArcColor;
    private int mHourArcOutlineColor;
    private int mMinuteArcOutlineColor;
    private int mSecondArcOutlineColor;
    
    private int mArcOutlineWidth;

    public DrawingOptions(Context context, int radius)
    {
        mContext = context;
        mRadius = radius;

        initFromPrefs();
    }

    public void initFromPrefs()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // determine widths (factor from prefs * max size)
        mBorder = (int) (prefs.getFloat(mContext.getString(R.string.border_width_key), 0.7f) * (mRadius / 4));
        mArcGap = (int) (prefs.getFloat(mContext.getString(R.string.arc_gap_key), 0.7f) * (mRadius / 8));
        mArcWidth = Math.max(2, (int)(prefs.getFloat(mContext.getString(R.string.arc_width_key), 0.7f) * (mRadius - 2 * mArcGap - mBorder) / 3.0f));
        mFreeSpace = mRadius - mBorder - 2 * mArcGap - 3 * mArcWidth;

        mInverseFactor = prefs.getBoolean(mContext.getString(R.string.inverse_mode_key), false) ? -1 : 1;
        m24HourMode = prefs.getBoolean(mContext.getString(R.string.twenty_four_hour_mode_key), false);
        mDrawOutline = prefs.getBoolean(mContext.getString(R.string.draw_outline_key), false);

        mBgColor = prefs.getInt(mContext.getString(R.string.background_color_key), 0);
        mHourArcColor = prefs.getInt(mContext.getString(R.string.hour_arc_color_key), 0);
        mMinuteArcColor = prefs.getInt(mContext.getString(R.string.minute_arc_color_key), 0);
        mSecondArcColor = prefs.getInt(mContext.getString(R.string.second_arc_color_key), 0);
        
        mHourArcOutlineColor = ColorHelper.lightenColor(mHourArcColor);
        mMinuteArcOutlineColor = ColorHelper.lightenColor(mMinuteArcColor);
        mSecondArcOutlineColor = ColorHelper.lightenColor(mSecondArcColor);
        
        mArcOutlineWidth = mRadius / 120;
    }

    public int getBorder()
    {
        return mBorder;
    }

    public int getArcGap()
    {
        return mArcGap;
    }

    public int getArcWidth()
    {
        return mArcWidth;
    }
    
    public int getArcOutlineWidth()
    {
        return mArcOutlineWidth;
    }
    
    public int getFreeSpace()
    {
        return mFreeSpace;
    }

    public int getInverseFactor()
    {
        return mInverseFactor;
    }

    public boolean get24HourMode()
    {
        return m24HourMode;
    }
    
    public boolean getDrawOutline()
    {
        return mDrawOutline;
    }

    public int getBgColor()
    {
        return mBgColor;
    }

    public int getHourArcColor()
    {
        return mHourArcColor;
    }

    public int getMinuteArcColor()
    {
        return mMinuteArcColor;
    }

    public int getSecondArcColor()
    {
        return mSecondArcColor;
    }
    
    public int getHourArcOutlineColor()
    {
        return mHourArcOutlineColor;
    }

    public int getMinuteArcOutlineColor()
    {
        return mMinuteArcOutlineColor;
    }

    public int getSecondArcOutlineColor()
    {
        return mSecondArcOutlineColor;
    }

}
