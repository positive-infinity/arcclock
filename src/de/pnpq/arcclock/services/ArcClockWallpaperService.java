package de.pnpq.arcclock.services;

import java.util.Calendar;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import de.pnpq.arcclock.R;
import de.pnpq.arcclock.misc.DrawingOptions;

public class ArcClockWallpaperService extends WallpaperService
{
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate()
    {
        super.onCreate();
        // android.os.Debug.waitForDebugger();

        // set preference default values if this is the first start ever
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }

    @Override
    public Engine onCreateEngine()
    {
        return new ArcClockEngine();
    }

    class ArcClockEngine extends Engine implements OnSharedPreferenceChangeListener
    {
        private DrawingOptions mDrawingOptions;

        private final Paint mPaint;
        private boolean mVisible;
        private int mWidth;
        private int mHeight;
        private int mRadius;

        private int mLastDrawSecond;

        private final Runnable mRunnable = new Runnable()
        {
            public void run()
            {
                drawFrame();
            }
        };

        public ArcClockEngine()
        {
            mPaint = new Paint();
            mPaint.setStyle(Style.STROKE);
            mPaint.setAntiAlias(true);

            mVisible = false;

            mWidth = 0;
            mHeight = 0;
            mRadius = 0;

            mLastDrawSecond = -1;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);
            PreferenceManager.getDefaultSharedPreferences(ArcClockWallpaperService.this).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            mHandler.removeCallbacks(mRunnable);
            PreferenceManager.getDefaultSharedPreferences(ArcClockWallpaperService.this).unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            mVisible = visible;
            if (visible)
            {
                drawFrame();
            }
            else
            {
                mHandler.removeCallbacks(mRunnable);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            super.onSurfaceChanged(holder, format, width, height);

            mWidth = width;
            mHeight = height;
            mRadius = Math.min(width, height) / 2;

            mDrawingOptions = new DrawingOptions(ArcClockWallpaperService.this, mRadius);

            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder)
        {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mRunnable);
        }

        private void drawFrame()
        {                        
            int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
            if (currentSecond != mLastDrawSecond)
            {
                final SurfaceHolder holder = getSurfaceHolder();
                Canvas c = null;
                try
                {
                    c = holder.lockCanvas();
                    if (c != null)
                    {
                        // draw
                        drawFrame(c);
                    }
                }
                finally
                {
                    if (c != null)
                        holder.unlockCanvasAndPost(c);
                }
            }

            // reschedule the next redraw
            mHandler.removeCallbacks(mRunnable);
            if (mVisible)
            {
                mHandler.postDelayed(mRunnable, 50);
            }                        
        }

        private void drawFrame(Canvas c)
        {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);
            int second = Calendar.getInstance().get(Calendar.SECOND);

            int centerX = mWidth / 2;
            int centerY = mHeight / 2;
            
            // draw background
            c.drawColor(mDrawingOptions.getBgColor());
           
            // -----
            // hours
            // -----
            float hourSweepAngle = mDrawingOptions.getInverseFactor() * 360 * (mDrawingOptions.get24HourMode() ? (hour / 24.0f) : ((hour % 12) / 12.0f));

            // draw hour path
            Path hourPath = new Path();
            int hourPathOffset = mDrawingOptions.getFreeSpace() + (int) (0.5 * mDrawingOptions.getArcWidth());
            RectF hourBoundRect = new RectF(centerX - hourPathOffset, centerY - hourPathOffset, centerX + hourPathOffset, centerY + hourPathOffset);
            hourPath.arcTo(hourBoundRect, 270, hourSweepAngle);
            
            mPaint.setStrokeWidth(mDrawingOptions.getArcWidth());
            mPaint.setColor(mDrawingOptions.getHourArcColor());
            
            c.drawPath(hourPath, mPaint);

            if (mDrawingOptions.getDrawOutline())
            {
                // draw hour outline paths
                Path hourPathInnerOutline = new Path();
                int hourPathOutline1Offset = mDrawingOptions.getFreeSpace();
                RectF hourOutline1BoundRect = new RectF(centerX - hourPathOutline1Offset, centerY - hourPathOutline1Offset, centerX + hourPathOutline1Offset, centerY + hourPathOutline1Offset);
                hourPathInnerOutline.arcTo(hourOutline1BoundRect, 270, hourSweepAngle);
                Path hourPathOuterOutline = new Path();
                int hourPathOutline2Offset = mDrawingOptions.getFreeSpace() + (int) (1.0 * mDrawingOptions.getArcWidth());
                RectF hourOutline2BoundRect = new RectF(centerX - hourPathOutline2Offset, centerY - hourPathOutline2Offset, centerX + hourPathOutline2Offset, centerY + hourPathOutline2Offset);
                hourPathOuterOutline.arcTo(hourOutline2BoundRect, 270, hourSweepAngle);
                
                mPaint.setStrokeWidth(mDrawingOptions.getArcOutlineWidth());
                mPaint.setColor(mDrawingOptions.getHourArcOutlineColor());
                
                c.drawPath(hourPathInnerOutline, mPaint);
                c.drawPath(hourPathOuterOutline, mPaint);
                
                if (hour > 0)
                {
                    float hourPathInnerOutlineStart[] = {0f, 0f};
                    float hourPathInnerOutlineEnd[] = {0f, 0f};
                    PathMeasure hourPathInnerOutlineMeasure = new PathMeasure(hourPathInnerOutline, false);
                    hourPathInnerOutlineMeasure.getPosTan(0, hourPathInnerOutlineStart, null);
                    hourPathInnerOutlineMeasure.getPosTan(hourPathInnerOutlineMeasure.getLength(), hourPathInnerOutlineEnd, null);
                    
                    float hourPathOuterOutlineStart[] = {0f, 0f};
                    float hourPathOuterOutlineEnd[] = {0f, 0f};
                    PathMeasure hourPathOuterOutlineMeasure = new PathMeasure(hourPathOuterOutline, false);
                    hourPathOuterOutlineMeasure.getPosTan(0, hourPathOuterOutlineStart, null);
                    hourPathOuterOutlineMeasure.getPosTan(hourPathOuterOutlineMeasure.getLength(), hourPathOuterOutlineEnd, null);

                    c.drawLine(hourPathInnerOutlineStart[0], hourPathInnerOutlineStart[1], hourPathOuterOutlineStart[0], hourPathOuterOutlineStart[1], mPaint);
                    c.drawLine(hourPathInnerOutlineEnd[0], hourPathInnerOutlineEnd[1], hourPathOuterOutlineEnd[0], hourPathOuterOutlineEnd[1], mPaint); 
                }
            }

            // -------
            // minutes
            // -------
            float minuteSweepAngle = mDrawingOptions.getInverseFactor() * 360 * (minute / 60.0f);

            // draw minute path
            Path minutePath = new Path();
            int minutePathOffset = mDrawingOptions.getFreeSpace() + mDrawingOptions.getArcGap() + (int) (1.5 * mDrawingOptions.getArcWidth());
            RectF minuteBoundRect = new RectF(centerX - minutePathOffset, centerY - minutePathOffset, centerX + minutePathOffset, centerY + minutePathOffset);
            minutePath.arcTo(minuteBoundRect, 270, minuteSweepAngle);
            
            mPaint.setStrokeWidth(mDrawingOptions.getArcWidth());
            mPaint.setColor(mDrawingOptions.getMinuteArcColor());
            
            c.drawPath(minutePath, mPaint);

            if (mDrawingOptions.getDrawOutline())
            {
                // draw minute outline paths
                Path minutePathInnerOutline = new Path();
                int minutePathOutline1Offset = mDrawingOptions.getFreeSpace() + mDrawingOptions.getArcGap() + (int) (1.0 * mDrawingOptions.getArcWidth());
                RectF minuteOutline1BoundRect = new RectF(centerX - minutePathOutline1Offset, centerY - minutePathOutline1Offset, centerX + minutePathOutline1Offset, centerY + minutePathOutline1Offset);
                minutePathInnerOutline.arcTo(minuteOutline1BoundRect, 270, minuteSweepAngle);
                Path minutePathOuterOutline = new Path();
                int minutePathOutline2Offset = mDrawingOptions.getFreeSpace() + mDrawingOptions.getArcGap() + (int) (2.0 * mDrawingOptions.getArcWidth());
                RectF minuteOutline2BoundRect = new RectF(centerX - minutePathOutline2Offset, centerY - minutePathOutline2Offset, centerX + minutePathOutline2Offset, centerY + minutePathOutline2Offset);
                minutePathOuterOutline.arcTo(minuteOutline2BoundRect, 270, minuteSweepAngle);
                
                mPaint.setStrokeWidth(mDrawingOptions.getArcOutlineWidth());
                mPaint.setColor(mDrawingOptions.getMinuteArcOutlineColor());
                
                c.drawPath(minutePathInnerOutline, mPaint);
                c.drawPath(minutePathOuterOutline, mPaint);
                
                if (minute > 0)
                {
                    float minutePathInnerOutlineStart[] = {0f, 0f};
                    float minutePathInnerOutlineEnd[] = {0f, 0f};
                    PathMeasure minutePathInnerOutlineMeasure = new PathMeasure(minutePathInnerOutline, false);
                    minutePathInnerOutlineMeasure.getPosTan(0, minutePathInnerOutlineStart, null);
                    minutePathInnerOutlineMeasure.getPosTan(minutePathInnerOutlineMeasure.getLength(), minutePathInnerOutlineEnd, null);
                    
                    float minutePathOuterOutlineStart[] = {0f, 0f};
                    float minutePathOuterOutlineEnd[] = {0f, 0f};
                    PathMeasure minutePathOuterOutlineMeasure = new PathMeasure(minutePathOuterOutline, false);
                    minutePathOuterOutlineMeasure.getPosTan(0, minutePathOuterOutlineStart, null);
                    minutePathOuterOutlineMeasure.getPosTan(minutePathOuterOutlineMeasure.getLength(), minutePathOuterOutlineEnd, null);

                    c.drawLine(minutePathInnerOutlineStart[0], minutePathInnerOutlineStart[1], minutePathOuterOutlineStart[0], minutePathOuterOutlineStart[1], mPaint);
                    c.drawLine(minutePathInnerOutlineEnd[0], minutePathInnerOutlineEnd[1], minutePathOuterOutlineEnd[0], minutePathOuterOutlineEnd[1], mPaint); 
                }
            }

            // -------
            // seconds
            // -------
            float secondSweepAngle = mDrawingOptions.getInverseFactor() * 360 * (second / 60.0f);

            // draw second path
            Path secondPath = new Path();
            int secondPathOffset = mDrawingOptions.getFreeSpace() + 2 * mDrawingOptions.getArcGap() + (int) (2.5 * mDrawingOptions.getArcWidth());
            RectF secondBoundRect = new RectF(centerX - secondPathOffset, centerY - secondPathOffset, centerX + secondPathOffset, centerY + secondPathOffset);
            secondPath.arcTo(secondBoundRect, 270, secondSweepAngle);
            
            mPaint.setStrokeWidth(mDrawingOptions.getArcWidth());
            mPaint.setColor(mDrawingOptions.getSecondArcColor());
            
            c.drawPath(secondPath, mPaint);

            if (mDrawingOptions.getDrawOutline())
            {
                // draw second outline paths
                Path secondPathInnerOutline = new Path();
                int secondPathOutline1Offset = mDrawingOptions.getFreeSpace() + 2 * mDrawingOptions.getArcGap() + (int) (2.0 * mDrawingOptions.getArcWidth());
                RectF secondOutline1BoundRect = new RectF(centerX - secondPathOutline1Offset, centerY - secondPathOutline1Offset, centerX + secondPathOutline1Offset, centerY + secondPathOutline1Offset);
                secondPathInnerOutline.arcTo(secondOutline1BoundRect, 270, secondSweepAngle);
                Path secondPathOuterOutline = new Path();
                int secondPathOutline2Offset = mDrawingOptions.getFreeSpace() + 2 * mDrawingOptions.getArcGap() + (int) (3.0 * mDrawingOptions.getArcWidth());
                RectF secondOutline2BoundRect = new RectF(centerX - secondPathOutline2Offset, centerY - secondPathOutline2Offset, centerX + secondPathOutline2Offset, centerY + secondPathOutline2Offset);
                secondPathOuterOutline.arcTo(secondOutline2BoundRect, 270, secondSweepAngle);
                
                mPaint.setStrokeWidth(mDrawingOptions.getArcOutlineWidth());
                mPaint.setColor(mDrawingOptions.getSecondArcOutlineColor());
                
                c.drawPath(secondPathInnerOutline, mPaint);
                c.drawPath(secondPathOuterOutline, mPaint);
                
                if (second > 0)
                {
                    float secondPathInnerOutlineStart[] = {0f, 0f};
                    float secondPathInnerOutlineEnd[] = {0f, 0f};
                    PathMeasure secondPathInnerOutlineMeasure = new PathMeasure(secondPathInnerOutline, false);
                    secondPathInnerOutlineMeasure.getPosTan(0, secondPathInnerOutlineStart, null);
                    secondPathInnerOutlineMeasure.getPosTan(secondPathInnerOutlineMeasure.getLength(), secondPathInnerOutlineEnd, null);
                    
                    float secondPathOuterOutlineStart[] = {0f, 0f};
                    float secondPathOuterOutlineEnd[] = {0f, 0f};
                    PathMeasure secondPathOuterOutlineMeasure = new PathMeasure(secondPathOuterOutline, false);
                    secondPathOuterOutlineMeasure.getPosTan(0, secondPathOuterOutlineStart, null);
                    secondPathOuterOutlineMeasure.getPosTan(secondPathOuterOutlineMeasure.getLength(), secondPathOuterOutlineEnd, null);

                    c.drawLine(secondPathInnerOutlineStart[0], secondPathInnerOutlineStart[1], secondPathOuterOutlineStart[0], secondPathOuterOutlineStart[1], mPaint);
                    c.drawLine(secondPathInnerOutlineEnd[0], secondPathInnerOutlineEnd[1], secondPathOuterOutlineEnd[0], secondPathOuterOutlineEnd[1], mPaint);                    
                }
                
            }

            // update mLastDrawSecond
            mLastDrawSecond = second;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            mDrawingOptions.initFromPrefs();
        }
    }
}
