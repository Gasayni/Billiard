package com.gas.billiard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OptionallyClass {
    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorEmployee, cursorTables, cursorOrders;

    List<String> adminsList = new ArrayList<>();
    List<String> passList = new ArrayList<>();

    public int convertDpToPixels(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public List<String> initAdmins(Context context, String thatReturn) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // получаем данные c табл "EMPLOYEES"
        cursorEmployee = database.query(DBHelper.EMPLOYEES,
                null, null, null,
                null, null, null);
        if (cursorEmployee.moveToFirst()) {
            int firstNameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_FIRST_NAME);
            int secondNameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_SECOND_NAME);
            int passIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PASS);
            do {
                // находим всех сотрудников из бд
                if (thatReturn.equals("adminsList")) {
                    adminsList.add(cursorEmployee.getString(secondNameIndex) + " "
                            + cursorEmployee.getString(firstNameIndex));
                } else  passList.add(cursorEmployee.getString(passIndex));
            } while (cursorEmployee.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        if (thatReturn.equals("adminsList")) {
            return adminsList;
        } else return passList;
    }
}
