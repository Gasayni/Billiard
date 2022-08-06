package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import java.time.LocalDate;
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
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd\nMM\nyyyy", Locale.ENGLISH);
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
    Button[][] btnTableTagArray = new Button[tableCount][hourCount];

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;

    // задаем начальное значение для выбора даты
    int DIALOG_DATE = 1;
    int myYear = 2022;
    int myMonth = 8;
    int myDay;

    {
        // задаем начальное значение для выбора даты
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate currentDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDate = LocalDate.parse(dateFormat.format(new Date()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myDay = currentDate.getDayOfMonth();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myMonth = currentDate.getMonthValue() - 1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myYear = currentDate.getYear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // сначала отрисуем кнопки шапки часов
        marginLength = option.convertDpToPixels(this, 2);
        addBtnHour();
        // также отрисуем кнопку выбора даты и шапки столов
        addBtnTableHead();

        // выбираем дату
        btnDate = btnDate.findViewWithTag("btnChoseDate");
        btnDate.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        // покажем текущее время
        tvTime = findViewById(R.id.tvTime);
        actualTime();

        // отрисовываем таблицу заказов на изначальную дату
        addBtnCommon();
        choseTypeTable();
        choseTable();


        // нам нужно загрузить с Таблиц каждого стола данные о резервах на сегодня
//        reserveToday();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                startActivity(intent);
                // при добавлении нового резерва, также нужно обновить таблицу резерва
                break;
            }
        }

        // кнопки по тегам
        switch (view.getTag().toString()) {
            case "btnChoseDate": {
                showDialog(DIALOG_DATE);
                // при изменении даты, нужно, чтобы обновлялась и таблица резерва
                break;
            }
        }


       /* int i = cursorOrders.getInt(numTableIndex);
        int j = indexTimeMethod(cursorOrders.getString(reserveTimeIndex));
        // т.к. массив начинается с 0, а номера столов с 1 ...
        // [i-1][j-1], где i-номер стола, j-время
        btnTable = btnTableTagArray[i-1][j];*/
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
            do {
                // инициализируем каждую кнопку шапки стола
                int i = cursorTables.getInt(numberTableIndex);
                btnTableHead = btnTableHeadTagsList.get(i-1).findViewWithTag("btnTableHead" + i);

                // меняем фон кнопки каждого стола, в зависимости от типа стола
                if (cursorTables.getString(typeIndex).equals("pool")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pool1);
                } else if (cursorTables.getString(typeIndex).equals("pyramid")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pyramide1);
                    btnTableHead.setTextColor(Color.WHITE);
                }

                btnTableHead.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent("settingActivity");
                        startActivity(intent);
                        // Perform action on click
                        btnTableHead.setText("555");
//                        Log.i("Gas", "onClick");
                    }
                });

            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    private void choseTable() {
        Log.i("Gas", "");
        Log.i("Gas", " ...//... ");

        // очищаем таблицу
        clearBtnCommon();
        // нужно, чтобы при загрузке и при изменении даты показывались данные (подкрашивались кнопки) по времени
        // получаем данные c табл "ORDERS"
        cursorOrders = database.query(DBHelper.ORDERS,
                null, null, null,
                null, null, null);
        if (cursorOrders.moveToFirst()) {
            int idIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ID);
            int numTableIndex = cursorOrders.getColumnIndex(DBHelper.KEY_NUM_TABLE);
            int reserveDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_DATE);
            int reserveTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_TIME);
            int durationIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DURATION);
            int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
            int employeeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_EMPLOYEE);
            int orderDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
            int orderTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
            int rateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RATE);
            int descriptionIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // меняем нужные кнопки
                // смотрим какой это стол
                int i = cursorOrders.getInt(numTableIndex);
                // находим кнопку по времени резерва, воспользуемся спец. методом
                int j = indexTimeMethod(cursorOrders.getString(reserveTimeIndex));
                // т.к. массив начинается с 0, а номера столов с 1 ...
                // [i-1][j-1], где i-номер стола, j-время
                btnTable = btnTableTagArray[i-1][j];


                Log.i("Gas", "btnDate.getText().toString() = "
                        + btnDate.getText().toString());
                Log.i("Gas", "cursorOrders.getString(reserveDateIndex) = "
                        + cursorOrders.getString(reserveDateIndex));

                // находим все заказы на указанный день
                if (cursorOrders.getString(reserveDateIndex).equals(btnDate.getText().toString())) {
                    Log.i("Gas", "даты идентичны");
                    btnTable.setBackgroundResource(R.drawable.btn_style_6);
                    btnTable.setText(cursorOrders.getString(clientIndex));
                }

            } while (cursorOrders.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();

        // нужно, чтобы обновлялось
//        addBtnCommon();
    }

    private Integer indexTimeMethod(String time) {
        // метод принимает время и возвращает номер 2-й позиции в btnTableTagArray
        LocalTime timeLoc = null;  // переданное время
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeLoc = LocalTime.parse(time);
        }
        // получили переданный час
        int hour = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hour = timeLoc.getHour();
        }
        // теперь сравниваем
        int result;
        // сложная тараканиха по индексу
        if (hour > 10 && hour < 24) result = hour - 11;
        else result = hour + 13;
        return result;
    }

    private Integer hourMethod(String time) {
        // этот метод будет принимать время и возвращать часы
        // все просто
        LocalTime timeLoc = null;  // переданное время
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timeLoc = LocalTime.parse(time);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return timeLoc.getHour();
        } else return -1;
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
            myMonth = monthOfYear + 1;
            myDay = dayOfMonth;
            String myMonthSt, myDaySt;
            if (myMonth < 10) myMonthSt = "0" + myMonth;
            else myMonthSt = "" + myMonth;
            if (myDay < 10) myDaySt = "0" + myDay;
            else myDaySt = "" + myDay;

            btnDate.setText(myDaySt + "." + myMonthSt + "." + myYear);

            choseTable();

        }
    };


    public void addBtnTableHead() {
        linTableHead = findViewById(R.id.linTableHead);
        LinearLayout.LayoutParams marginBtnTable;


        btnDate = new Button(linTableHead.getContext());
        btnDate.setLayoutParams(new LinearLayout.LayoutParams(
                option.convertDpToPixels(this, 40),
                option.convertDpToPixels(this, 40)));

        btnDate.setTag("btnChoseDate");
        // задаем сегодняшнюю дату
        btnDate.setText(dateFormat.format(new Date()));
        btnDate.setTextSize(8);
        btnDate.setBackgroundResource(R.drawable.btn_style_4);

        marginBtnTable = (LinearLayout.LayoutParams) btnDate.getLayoutParams();
        marginBtnTable.setMargins(0, 0, marginLength, 0);

        linTableHead.addView(btnDate);
        for (int i = 0; i < tableCount; i++) {
            btnTableHead = new Button(linTableHead.getContext());
            btnTableHead.setLayoutParams(new LinearLayout.LayoutParams(
                    option.convertDpToPixels(this, 40),
                    option.convertDpToPixels(this, 40)));

            btnTableHead.setTag("btnTableHead" + (1 + i));
            btnTableHeadTagsList.add(btnTableHead);
            btnTableHead.setText((i + 1) + "");
            btnTableHead.setTextSize(10);
            /*btnTableHead.setOnClickListener(this);
            btnTableHead.setClickable(true);*/

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
        for (int i = 0; i < hourCount; i++, hourRight++) {
            if (hourRight == 24) {
                hourRight = 0;
            }

            btnTime = new Button(linHour.getContext());
            btnTime.setLayoutParams(new LinearLayout.LayoutParams(
                    option.convertDpToPixels(this, 50),
                    option.convertDpToPixels(this, 40)));

            btnTime.setTag("btnTime" + hourRight);
            btnTimeTagsList.add(btnTableHead);
            btnTime.setText(hourRight + ":00");
            btnTime.setTextSize(10);
            btnTime.setClickable(false);

            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTime.getLayoutParams();
//            marginBtnTable.setMargins(0, 0, marginLength, 0);

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
            for (int j = 0; j < hourCount; j++, hourRight++) {
                if (hourRight == 24) {
                    hourRight = 0;
                }

                btnTable = new Button(linTable.getContext());
                btnTable.setLayoutParams(new LinearLayout.LayoutParams(
                        option.convertDpToPixels(this, 50),
                        option.convertDpToPixels(this, 40)));

                btnTable.setTag("btnTable." + (i+1) + "." + hourRight);
                btnTableTagArray[i][j] = btnTable;
                btnTable.setBackgroundResource(R.drawable.btn_style_4);
                btnTable.setClickable(false);
                btnTable.setTextSize(10);

                LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTable.getLayoutParams();
//                marginBtnTable.setMargins(0, 0, marginLength, 0);

                linTable.removeView(btnTable);
                linTable.addView(btnTable);
            }
            linTableTime.removeView(linTable);
            linTableTime.addView(linTable);
        }
    }

    public void clearBtnCommon() {
        for (int i = 0; i < tableCount; i++) {
            for (int j = 0; j < hourCount; j++) {
                Button btnTable = btnTableTagArray[i][j];
                btnTable.setBackgroundResource(R.drawable.btn_style_4);
                btnTable.setText("");
            }
        }
    }
}