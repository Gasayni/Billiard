package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonActivity extends AppCompatActivity implements View.OnClickListener {
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    private TextView tvTime;

    OptionallyClass optionalClass = new OptionallyClass();
    // определим, сколько заказов есть на этот день
    List<List<OrderClass>> allOrdersList = optionalClass.findAllOrders(this, "Необязательно", false);
    List<TableClass> allTablesList = optionalClass.findAllTables(this, false);

    LinearLayout linTable, linHour, linTableTime, linTableTimeHead, linTableHead;
    RelativeLayout relativeTable;
    Button btnAdd, btnDate, btnDate1, btnDate2, btnTableHead, btnTime, btnTable;
    private int marginLength, marginLengthMinute;
    String getAdminName;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;

    // задаем начальное значение для выбора даты
    int myYear;
    int myMonth;
    int myDay;

    final Calendar currentDateCalendar = Calendar.getInstance();
    String currentMonthSt, currentDaySt;

    {
        // Get Current Date
        myYear = currentDateCalendar.get(Calendar.YEAR);
        myMonth = currentDateCalendar.get(Calendar.MONTH);
        myDay = currentDateCalendar.get(Calendar.DAY_OF_MONTH);

        if (myMonth < 10) currentMonthSt = "0" + (myMonth + 1);
        else currentMonthSt = "" + (myMonth + 1);
        if (myDay < 10) currentDaySt = "0" + myDay;
        else currentDaySt = "" + myDay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher_foreground);


        Intent getIntent = getIntent();
        getAdminName = getIntent.getStringExtra("adminName");
        Log.i("Gas4", "getAdminName in Common = " + getAdminName);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // сначала отрисуем кнопки шапки часов
        marginLength = optionalClass.convertDpToPixels(this, 2);
        marginLengthMinute = optionalClass.convertDpToPixels(this, 4);
        addBtnHour();
        // также отрисуем кнопку выбора даты и шапки столов
        addBtnTableHead();

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        // покажем текущее время
        tvTime = findViewById(R.id.tvTime);

        addBtnCommon();
        actualTime();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                intent.putExtra("whoCall", "commonActivity");
                intent.putExtra("adminName", getAdminName);
                intent.putExtra("type", "");
                startActivity(intent);
                // при добавлении нового резерва, также нужно обновить таблицу резерва
                break;
            }
        }
    }


    @SuppressLint("SetTextI18n")
    public void addBtnTableHead() {
        linTableHead = findViewById(R.id.linTableHead);
        LinearLayout.LayoutParams marginBtnTable;


        btnDate = new Button(linTableHead.getContext());
        btnDate.setLayoutParams(new LinearLayout.LayoutParams(
                optionalClass.convertDpToPixels(this, 100),
                optionalClass.convertDpToPixels(this, 50)));

        btnDate.setTag("btnChoseDate");
        // задаем сегодняшнюю дату
        // если новая дата, но время с 0 до 5 утра, то показывается вчерашняя дата (дата смены)
        Calendar dayCalendar = Calendar.getInstance();
        if (dayCalendar.get(Calendar.HOUR_OF_DAY) < 5) {
            dayCalendar.add(Calendar.DATE, -1);  // number of days to add
        }
        btnDate.setText(optionalClass.dateDateToString(dayCalendar));
        btnDate.setTextSize(14);
        btnDate.setBackgroundResource(R.drawable.btn_style_2);
        // заодно слушаем кнопку с выбором даты
        btnDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                datePicker();
            }
        });

        marginBtnTable = (LinearLayout.LayoutParams) btnDate.getLayoutParams();
        marginBtnTable.setMargins(0, 0, marginLength, 0);

        linTableHead.removeView(btnDate);
        linTableHead.addView(btnDate);


        int tableCount = 19;
        for (int i = 0; i < tableCount; i++) {
            btnTableHead = new Button(linTableHead.getContext());
            btnTableHead.setLayoutParams(new LinearLayout.LayoutParams(
                    optionalClass.convertDpToPixels(this, 60),
                    optionalClass.convertDpToPixels(this, 60)));

            btnTableHead.setText("" + (i + 1));
            btnTableHead.setTextSize(10);
            btnTableHead.setClickable(false);
            choseTypeTable();

            linTableHead.removeView(btnTableHead);
            linTableHead.addView(btnTableHead);

            // ищи btnTableHead.setOnClickListener
            int numTable = i + 1;
            btnTableHead.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent("tableActivity");
                    // передаем название заголовка
                    intent.putExtra("whoCall", "btnTable");
                    intent.putExtra("numTable", numTable);
                    intent.putExtra("adminName", getAdminName);
                    startActivity(intent);
                }
            });
        }


        btnDate1 = new Button(linTableHead.getContext());
        btnDate1.setLayoutParams(new LinearLayout.LayoutParams(
                optionalClass.convertDpToPixels(this, 100),
                optionalClass.convertDpToPixels(this, 50)));

        btnDate1.setTag("btnChoseDate1");
        // задаем сегодняшнюю дату
        btnDate1.setText(btnDate.getText());
        btnDate1.setTextSize(14);
        btnDate1.setBackgroundResource(R.drawable.btn_style_2);
        // заодно слушаем кнопку с выбором даты
        btnDate1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                datePicker();
            }
        });

        marginBtnTable = (LinearLayout.LayoutParams) btnDate1.getLayoutParams();
        marginBtnTable.setMargins(0, 0, marginLength, 0);
    }

    private void choseTypeTable() {
        Log.i("CommonActivityClass", "\n --- /// ---   Method choseTypeTable");
        int num = Integer.parseInt(btnTableHead.getText().toString()) - 1;
        Log.i("CommonActivityClass", "\tallTablesList.get(i).getType() = " + allTablesList.get(num).getType());
        if (allTablesList.get(num).getType().equals("Американский пул")) {
            btnTableHead.setBackgroundResource(R.drawable.bol_pool1);
            btnTableHead.setTextColor(Color.BLACK);
        } else {
            btnTableHead.setBackgroundResource(R.drawable.bol_pyramide1);
            btnTableHead.setTextColor(Color.WHITE);
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
                tvTime.setText(timeFormat.format(new Date()));
                // также обращаемся каждую минуту к калькулятору оставшегося времени
                actualTime();  // мисис рекурсия
            });
        }).start();
    }

    public void addBtnHour() {
        linTableTimeHead = findViewById(R.id.linTableTimeHead);
        linHour = new LinearLayout(linTableTimeHead.getContext());
        linHour.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linHour.setOrientation(LinearLayout.HORIZONTAL);

        int hourRight = 11;
        int hourCount = 18;
        for (int i = 0; i < hourCount; i++, hourRight++) {
            if (hourRight == 24) {
                hourRight = 0;
            }

            btnTime = new Button(linHour.getContext());
            btnTime.setLayoutParams(new LinearLayout.LayoutParams(
                    optionalClass.convertDpToPixels(this, 180),
                    optionalClass.convertDpToPixels(this, 50)));

            btnTime.setTag("btnTime" + hourRight);
            btnTime.setText(hourRight + ":00");
            btnTime.setTextSize(16);
            btnTime.setClickable(false);
//            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTime.getLayoutParams();
//            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linHour.removeView(btnTable);
            linHour.addView(btnTime);
        }
        linTableTimeHead.removeView(linHour);
        linTableTimeHead.addView(linHour);
    }

    public void addBtnCommon() {
        Log.i("CommonActivityClass", "\n --- /// ---   Method addBtnCommon");
        OrderClass order;
        linTableTime = findViewById(R.id.linTableTimeHead);
        Log.i("CommonActivityClass", "linTableTime.toString() = " + linTableTime.toString());

        Log.i("CommonActivityClass", "allTablesList.size() = " + allOrdersList.size());
        for (int i = 0; i < allOrdersList.size(); i++) {

            Log.i("CommonActivityClass", "i: " + i);
            linTable = new LinearLayout(linTableTime.getContext());
            linTable.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    optionalClass.convertDpToPixels(this, 60)));
            linTable.setOrientation(LinearLayout.HORIZONTAL);
            linTable.setTag(i);
            linTable.setBaselineAligned(false); // чтобы не сползало вниз

            relativeTable = new RelativeLayout(linTable.getContext());
            relativeTable.setLayoutParams(new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            Log.i("CommonActivityClass", "allTablesList.get(i).size() = " + allOrdersList.get(i).size());
            for (int j = 0; j < allOrdersList.get(i).size(); j++) {
                order = allOrdersList.get(i).get(j);
                Log.i("CommonActivityClass", "orderClass.getNumTable() = " + order.getNumTable());

                if (i == (order.getNumTable() - 1)) {
                    Log.i("CommonActivityClass", "\tTrue: if(getNumTable() - 1) == i");
                    btnTable = new Button(relativeTable.getContext());
                    btnTable.setLayoutParams(new LinearLayout.LayoutParams(
                            optionalClass.convertDpToPixels(this, order.getDuration() * 3),
                            optionalClass.convertDpToPixels(this, 60)));

                    btnTable.setTag(order.getIdOrder());
                    Log.i("CommonActivityClass", "\t\tTag = " + order.getIdOrder());
                    btnTable.setBackgroundResource(R.drawable.btn_style_4);
                    btnTable.setTextSize(12);
                    OrderClass thisOrder = order;
                    btnTable.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            Intent intent = new Intent("tableActivity");
                            intent.putExtra("whoCall", "btnCommon");
                            intent.putExtra("numTable", thisOrder.getNumTable());
                            intent.putExtra("id", thisOrder.getIdOrder());
                            intent.putExtra("adminName", getAdminName);
                            intent.putExtra("client", thisOrder.getClient());
                            intent.putExtra("duration", thisOrder.getDuration());
                            intent.putExtra("bron", thisOrder.getBron());
                            intent.putExtra("reserveDateStr", /*btnDate.getText().toString()*/thisOrder.getDateStartReserve());
                            intent.putExtra("reserveStartTimeStr", thisOrder.getTimeStartReserve());
                            intent.putExtra("dateOrder", thisOrder.getDateOrder());
                            intent.putExtra("timeOrder", thisOrder.getTimeOrder());
                            intent.putExtra("reserveFinishTimeStr", thisOrder.getTimeEndReserve());
                            startActivity(intent);
                        }
                    });

                    LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTable.getLayoutParams();

                    int shiftMinute = optionalClass.calcMinuteFromDateTime(btnDate.getText().toString(), order);
                    int shift = optionalClass.convertDpToPixels(this, shiftMinute);
                    Log.i("CommonActivityClass", "\t\tshift = " + shift);
                    marginBtnTable.setMargins(marginLength * shift + marginLengthMinute, 0, 0, 0);


                    relativeTable.removeView(btnTable);
                    relativeTable.addView(btnTable);

                    drawBtnCommon(order);
                }
            }

            linTableTime.addView(linTable);
            linTable.addView(relativeTable);
        }
    }

    @SuppressLint("SetTextI18n")
    private void drawBtnCommon(OrderClass orderClass) {
        Log.i("CommonActivityClass", "\n --- /// ---   Method changeBtnCommon");
        if (orderClass != null) {
            btnTable = btnTable.findViewWithTag(orderClass.getIdOrder());

            // пишем в кнопке
            if (orderClass.getBron().equals("Без брони"))
                btnTable.setText(orderClass.getClient() + "\n" +
                        orderClass.getTimeStartReserve() + "  -  " + orderClass.getTimeEndReserve());
            else
                btnTable.setText(orderClass.getClient() + "\n" +
                        orderClass.getTimeStartReserve() + "  -  " + orderClass.getTimeEndReserve() + "\t🅱");

            // красим кнопку
            if (orderClass.getEndDateTimeReserveCal().before(Calendar.getInstance()))
                btnTable.setBackgroundResource(R.drawable.btn_style_3);
            else btnTable.setBackgroundResource(R.drawable.btn_style_6);
        } else
            Log.i("CommonActivityClass", "orderClass is null");
    }

    private void datePicker() {
        Log.i("CommonActivityClass", "\n --- /// ---   Method datePicker");
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myYear = year;
                        myMonth = monthOfYear;
                        myDay = dayOfMonth;

                        String myMonthSt, myDaySt;
                        if (myMonth < 10) myMonthSt = "0" + (myMonth + 1);
                        else myMonthSt = "" + (myMonth + 1);
                        if (myDay < 10) myDaySt = "0" + myDay;
                        else myDaySt = "" + myDay;

                        btnDate.setText(myDaySt + "." + myMonthSt + "." + myYear);
                        btnDate1.setText(myDaySt + "." + myMonthSt + "." + myYear);

                        allOrdersList = optionalClass.findAllOrders(CommonActivity.this,
                                btnDate.getText().toString(), true);

                        linTableTimeHead.removeAllViews();
                        addBtnHour();
                        addBtnCommon();
                    }
                }, myYear, myMonth, myDay);
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // переключаемся на редактор резерва
            case R.id.clients: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Клиенты");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.reserves: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Резервы");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.setting: {
                intent = new Intent("settingActivity");
                // передаем имя админа (взависимости от переданного имени, разные права доступа)
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(CommonActivity.this);
        builderAlert.setTitle("Выход: Вы уверены?")
                .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                .setPositiveButton("Да", ((dialogInterface, i) -> {
                    Intent intent = new Intent(CommonActivity.this, MainActivity.class);
                    // передаем название заголовка
                    intent.putExtra("headName", "Резервы");
                    intent.putExtra("adminName", getAdminName);
                    startActivity(intent);
                }));
        builderAlert.setIcon(R.drawable.bol_pyramide1);

        AlertDialog alertDialog = builderAlert.create();
        alertDialog.show();
    }
}