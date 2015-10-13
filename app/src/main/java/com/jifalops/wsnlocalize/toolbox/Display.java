package com.jifalops.wsnlocalize.toolbox;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 *
 */
public class Display {
    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }
}
