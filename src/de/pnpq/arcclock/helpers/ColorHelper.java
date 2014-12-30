package de.pnpq.arcclock.helpers;

import android.graphics.Color;

public class ColorHelper
{
    public static int lightenColor(int color)
    {
        return Color.rgb(Math.min(Color.red(color) + 50, 255), Math.min(Color.green(color) + 50, 255), Math.min(Color.blue(color) + 50, 255));
    }

}
