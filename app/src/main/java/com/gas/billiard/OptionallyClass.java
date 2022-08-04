package com.gas.billiard;

import android.content.Context;

public class OptionallyClass {

    public int convertDpToPixels(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
