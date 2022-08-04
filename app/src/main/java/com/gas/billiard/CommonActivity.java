package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonActivity extends AppCompatActivity implements View.OnClickListener {
    private Date currentTime;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private Date currentDate = new Date();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    String today = dateFormat.format(currentDate);
    List<String> todayReserveList = new ArrayList<>();
    LocalTime startGameLocalTime, endGameLocalTime;
    private TextView tvTime, tvAdminName;

    OptionallyClass option = new OptionallyClass();
    LinearLayout linTable, linHour, linTableTime, linTableTimeHead, linTableHead;
    private final int hourCount = 18;
    private final int tableCount = 19;
    Button btnAdd, btnDate, btnTableHead, btnTime, btnTable;
    private int marginLength;
    // листы для тегов (нужны, чтобы запоминать теги)
    List<Button> btnTableHeadTagsList = new ArrayList<>();
    List<Button> btnTimeTagsList = new ArrayList<>();
    Button[][] btnTableTagArray = new Button[19][24];

    // для выбора даты
    int DIALOG_DATE = 1;
    int myYear = 2022;
    int myMonth = 7;
    int myDay = 1;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        // сначала создадим все кнопки
        marginLength = option.convertDpToPixels(this, 2);
        addBtnTableHead();
        addBtnHour();
        addBtnCommon();

        // покажем текущее время
        tvTime = findViewById(R.id.tvTime);
        actualTime();

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        choseTypeTable();

        // выбираем дату
        btnDate = btnDate.findViewWithTag("btnChoseDate");
        btnDate.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        // нам нужно загрузить с Таблиц каждого стола данные о резервах на сегодня
//        reserveToday();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        /*Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                startActivity(intent);
                break;
            }
        }*/

        // кнопки по тегам
        switch (view.getTag().toString()) {
            case "btnChoseDate": {
                showDialog(DIALOG_DATE);
            }
        }
    }

    private void actualTime() {
        // каждую секунду обновляет время
        final Handler handler = new Handler();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                currentTime = new Date();
                tvTime.setText(timeFormat.format(currentTime));
                // также обращаемся каждую минуту к калькулятору оставшегося времени
//                calculateduration(timeFormat.format(currentTime));
                actualTime();  // мисис рекурсия
            });
        }).start();
    }

    private void choseTypeTable() {
        // получаем данные c табл "tables"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            int descriptionIndex = cursorTables.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            int i = 0;
            do {
                // находим кнопки столов
                switch (cursorTables.getInt(numberTableIndex)) {
                    case 1: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead1");
                        break;
                    }
                    case 2: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead2");
                        break;
                    }
                    case 3: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead3");
                        break;
                    }
                    case 4: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead4");
                        break;
                    }
                    case 5: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead5");
                        break;
                    }
                    case 6: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead6");
                        break;
                    }
                    case 7: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead7");
                        break;
                    }
                    case 8: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead8");
                        break;
                    }
                    case 9: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead9");
                        break;
                    }
                    case 10: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead10");
                        break;
                    }
                    case 11: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead11");
                        break;
                    }
                    case 12: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead12");
                        break;
                    }
                    case 13: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead13");
                        break;
                    }
                    case 14: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead14");
                        break;
                    }
                    case 15: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead15");
                        break;
                    }
                    case 16: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead16");
                        break;
                    }
                    case 17: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead17");
                        break;
                    }
                    case 18: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead18");
                        break;
                    }
                    case 19: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead19");
                        break;
                    }
                }

                // меняем фон кнопки каждого стола, в зависимости от типа стола
                if (cursorTables.getString(typeIndex).equals("pool")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pool1);
                } else if (cursorTables.getString(typeIndex).equals("pyramid")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pyramide1);
                    btnTableHead.setTextColor(Color.WHITE);
                }
                i++;

            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    private void choseTable() {
        // получаем данные c каждой табл "table"

        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            int descriptionIndex = cursorTables.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            int i = 0;
            do {
                // находим кнопки столов
                switch (cursorTables.getInt(numberTableIndex)) {
                    case 1: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead1");
                        break;
                    }
                    case 2: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead2");
                        break;
                    }
                    case 3: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead3");
                        break;
                    }
                    case 4: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead4");
                        break;
                    }
                    case 5: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead5");
                        break;
                    }
                    case 6: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead6");
                        break;
                    }
                    case 7: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead7");
                        break;
                    }
                    case 8: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead8");
                        break;
                    }
                    case 9: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead9");
                        break;
                    }
                    case 10: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead10");
                        break;
                    }
                    case 11: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead11");
                        break;
                    }
                    case 12: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead12");
                        break;
                    }
                    case 13: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead13");
                        break;
                    }
                    case 14: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead14");
                        break;
                    }
                    case 15: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead15");
                        break;
                    }
                    case 16: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead16");
                        break;
                    }
                    case 17: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead17");
                        break;
                    }
                    case 18: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead18");
                        break;
                    }
                    case 19: {
                        btnTableHead = btnTableHeadTagsList.get(i).findViewWithTag("btnTableHead19");
                        break;
                    }
                }

                // меняем фон кнопки каждого стола, в зависимости от типа стола
                if (cursorTables.getString(typeIndex).equals("pool")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pool1);
                } else if (cursorTables.getString(typeIndex).equals("pyramid")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pyramide1);
                    btnTableHead.setTextColor(Color.WHITE);
                }
                i++;

            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }


    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_DATE) {
            DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
            return tpd;
        }
        return super.onCreateDialog(id);
    }

    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            btnDate.setText(myDay + "." + myMonth + "." + myYear);
        }
    };




    /* private void calculateduration(String curTime) {
        LocalTime currentTime = LocalTime.parse(curTime);  // текущее время
        // узнаем продолжительность игры 1 стола
        // у нас есть время начала и текущее
        if ((!btnStartGameTime1.getText().toString().equals(""))*/
    /*) {  // если мы забираем не пустое значение
            // забираем время начала и конца игры
            startGameLocalTime = LocalTime.parse(btnStartGameTime1.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime1);

            // текущее время д.б больше времени старта игры и меньше времени окончания игры
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus1.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus1.setText("Занят");
                btnduration1.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus1.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus1.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus2.setText("Свободен");
        }

        // аналогично продолжительность 2 стола
        if (!btnStartGameTime2.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime2.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime2);

            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus2.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus2.setText("Занят");
                btnduration2.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus2.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus2.setText("Свободен");
        }

        // аналогично продолжительность 3 стола
        if (!btnStartGameTime3.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime3.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime3);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus3.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus3.setText("Занят");
                btnduration3.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus3.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus3.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus3.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus3.setText("Свободен");
        }

        // аналогично продолжительность 4 стола
        if (!btnStartGameTime4.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime4.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime4);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus4.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus4.setText("Занят");
                btnduration4.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus4.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus4.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus4.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus4.setText("Свободен");
        }

        // аналогично продолжительность 5 стола
        if (!btnStartGameTime5.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime5.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime5);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus5.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus5.setText("Занят");
                btnduration5.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus5.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus5.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus5.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus5.setText("Свободен");
        }

        // аналогично продолжительность 6 стола
        if (!btnStartGameTime6.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime6.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime6);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus6.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus6.setText("Занят");
                btnduration6.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus6.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus6.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus6.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus6.setText("Свободен");
        }

        // аналогично продолжительность 7 стола
        if (!btnStartGameTime7.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime7.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime7);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus7.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus7.setText("Занят");
                btnduration7.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus7.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus7.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus7.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus7.setText("Свободен");
        }

        // аналогично продолжительность 8 стола
        if (!btnStartGameTime8.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime8.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime8);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus8.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus8.setText("Занят");
                btnduration8.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus8.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus8.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus8.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus8.setText("Свободен");
        }

        // аналогично продолжительность 9 стола
        if (!btnStartGameTime9.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime9.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime9);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus9.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus9.setText("Занят");
                btnduration9.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus9.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus9.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus9.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus9.setText("Свободен");
        }

        // аналогично продолжительность 10 стола
        if (!btnStartGameTime10.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime10.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime10);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus10.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus10.setText("Занят");
                btnduration10.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus10.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus10.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus10.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus10.setText("Свободен");
        }

        // аналогично продолжительность 11 стола
        if (!btnStartGameTime11.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime11.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime11);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus11.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus11.setText("Занят");
                btnduration11.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus11.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus11.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus11.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus11.setText("Свободен");
        }

        // аналогично продолжительность 12 стола
        if (!btnStartGameTime12.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime12.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime12);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus12.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus12.setText("Занят");
                btnduration12.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus12.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus12.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus12.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus12.setText("Свободен");
        }

        // аналогично продолжительность 13 стола
        if (!btnStartGameTime13.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime13.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime13);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus13.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus13.setText("Занят");
                btnduration13.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus13.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus13.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus13.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus13.setText("Свободен");
        }

        // аналогично продолжительность 14 стола
        if (!btnStartGameTime14.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime14.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime14);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus14.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus14.setText("Занят");
                btnduration14.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus14.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus14.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus14.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus14.setText("Свободен");
        }

        // аналогично продолжительность 15 стола
        if (!btnStartGameTime15.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime15.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime15);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus15.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus15.setText("Занят");
                btnduration15.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus15.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus15.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus15.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus15.setText("Свободен");
        }

        // аналогично продолжительность 16 стола
        if (!btnStartGameTime16.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime16.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime16);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus16.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus16.setText("Занят");
                btnduration16.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus16.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus16.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus16.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus16.setText("Свободен");
        }

        // аналогично продолжительность 17 стола
        if (!btnStartGameTime17.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime17.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime17);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus17.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus17.setText("Занят");
                btnduration17.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus17.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus17.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus17.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus17.setText("Свободен");
        }

        // аналогично продолжительность 18 стола
        if (!btnStartGameTime18.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime18.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime18);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus18.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus18.setText("Занят");
                btnduration18.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus18.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus18.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus18.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus18.setText("Свободен");
        }

        // аналогично продолжительность 19 стола
        if (!btnStartGameTime19.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime19.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime19);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus19.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus19.setText("Занят");
                btnduration19.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus19.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus19.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus19.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus19.setText("Свободен");
        }
    }*/

    /*private String convertMinuteToHour(long allMinutes) {
        long hourFinish = allMinutes / 60;
        long minFinish = allMinutes % 60;
        if (hourFinish == 0) {
            return "" + minFinish + " мин";
        } else return hourFinish + " ч\n" + minFinish + " мин";
    }*/

    /*private void reserveToday() {
        // нам нужно загрузить с Таблиц каждого стола данные о резервах на сегодня

        // получаем данные c табл "table 1"
        cursorTable = database.query(DBHelper.TABLE_1,
                null, null, null,
                null, null, null);
        if (cursorTable.moveToFirst()) {
            int numberTableIndex = cursorTable.getColumnIndex(DBHelper.KEY_ID);
            int startDateIndex = cursorTable.getColumnIndex(DBHelper.KEY_START_DATE);
            int startTimeIndex = cursorTable.getColumnIndex(DBHelper.KEY_START_TIME);
            int endDateIndex = cursorTable.getColumnIndex(DBHelper.KEY_END_DATE);
            int endTimeIndex = cursorTable.getColumnIndex(DBHelper.KEY_END_TIME);
            int clientIndex = cursorTable.getColumnIndex(DBHelper.KEY_CLIENT);
            int employeeIndex = cursorTable.getColumnIndex(DBHelper.KEY_EMPLOYEE);
            int rateIndex = cursorTable.getColumnIndex(DBHelper.KEY_RATE);
            int dateOrderIndex = cursorTable.getColumnIndex(DBHelper.KEY_DATE_ORDER);
            int timeOrderIndex = cursorTable.getColumnIndex(DBHelper.KEY_TIME_ORDER);
            int descriptionIndex = cursorTable.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // находим все сегодняшние резервы
                // если дата сегодняшняя, то заносим времена в лист сегодняшних резервов
                if (cursorTable.getString(startDateIndex).equals(today)) {
                    todayReserveList.add(cursorTable.getString(startTimeIndex));
                    Log.i("Gas", "заносим в лист сегодняшних резервов: = "
                            + cursorTable.getString(startTimeIndex));

                }
                Log.i("Gas", "endGameTime1 = " + endGameTime1);
                break;

            } while (cursorTable.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTable.close();
    }*/






    public void addBtnTableHead() {
        linTableHead = findViewById(R.id.linTableHead);
        LinearLayout.LayoutParams marginBtnTable;


        btnDate = new Button(linTableHead.getContext());
        btnDate.setLayoutParams(new LinearLayout.LayoutParams(
                option.convertDpToPixels(this, 75),
                option.convertDpToPixels(this, 75)));

        btnDate.setTag("btnChoseDate");
        // задаем сегодняшнюю дату
        btnDate.setText(dateFormat.format(new Date()));
        btnDate.setTextSize(15);

        marginBtnTable = (LinearLayout.LayoutParams) btnDate.getLayoutParams();
        marginBtnTable.setMargins(0, 0, marginLength, 0);

        linTableHead.addView(btnDate);
        for (int i = 0; i < tableCount; i++) {
            btnTableHead = new Button(linTableHead.getContext());
            btnTableHead.setLayoutParams(new LinearLayout.LayoutParams(
                    option.convertDpToPixels(this, 75),
                    option.convertDpToPixels(this, 75)));

            btnTableHead.setTag("btnTableHead" + (1 + i));
            btnTableHeadTagsList.add(btnTableHead);
            btnTableHead.setText((i + 1) + "");
            btnTableHead.setTextSize(16);

            marginBtnTable = (LinearLayout.LayoutParams) btnTableHead.getLayoutParams();
            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linTableHead.addView(btnTableHead);
        }
    }

    public void addBtnHour() {
        linTableTimeHead = findViewById(R.id.linTableTimeHead);


        linHour = new LinearLayout(linTableTimeHead.getContext());
        linHour.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linHour.setOrientation(LinearLayout.HORIZONTAL);


        int hourRight = 11;
        for (int i = 0; i < hourCount; i++, hourRight ++) {
            if (hourRight == 24) {
                hourRight = 0;
            }

            btnTime = new Button(linHour.getContext());
            btnTime.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            btnTime.setTag("btnTime" + hourRight);
            btnTimeTagsList.add(btnTableHead);
            btnTime.setText(hourRight + ":00");
            btnTime.setTextSize(20);
            btnTime.setWidth(option.convertDpToPixels(this, 75));
            btnTime.setHeight(option.convertDpToPixels(this, 75));

            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTime.getLayoutParams();
            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linHour.addView(btnTime);
        }
        linTableTimeHead.addView(linHour);
    }

    public void addBtnCommon() {
        linTableTime = findViewById(R.id.linTableTimeHead);

        for (int i = 0; i < tableCount; i++) {
            linTable = new LinearLayout(linTableTime.getContext());
            linTable.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            linTable.setOrientation(LinearLayout.HORIZONTAL);

            int hourRight = 11;
            for (int j = 0; j < hourCount; j++, hourRight ++) {
                if (hourRight == 24) {
                    hourRight = 0;
                }

                btnTable = new Button(linTable.getContext());
                btnTable.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                btnTable.setTag("btnTable" + hourRight);
                btnTableTagArray[i][j] = btnTable;
                btnTable.setWidth(option.convertDpToPixels(this, 75));
                btnTable.setHeight(option.convertDpToPixels(this, 75));

                LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTable.getLayoutParams();
                marginBtnTable.setMargins(0, 0, marginLength, 0);

                linTable.addView(btnTable);
            }
            linTableTime.addView(linTable);
        }
    }
}