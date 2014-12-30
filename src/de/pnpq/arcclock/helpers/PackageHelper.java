package de.pnpq.arcclock.helpers;

import java.util.Locale;

import android.content.Context;

public class PackageHelper
{
    public static boolean isProVersion(Context context)
    {
        return getUniquePackageName(context).contains("full");
    }

    public static String getUniquePackageName(Context context)
    {
        return context.getPackageName().toLowerCase(Locale.US);
    }
}
