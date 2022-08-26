package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CommonActivity extends AppCompatActivity implements View.OnClickListener {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    private TextView tvTime;

    OptionallyClass option = new OptionallyClass();
    LinearLayout linTable, linHour, linTableTime, linTableTimeHead, linTableHead;
    private final int hourCount = 18;
    private final int tableCount = 19;
    Button btnAdd, btnDate, btnDate1, btnTableHead, btnTime, btnTable;
    private int marginLength;
    // листы для тегов (нужны, чтобы запоминать теги)
    List<Button> btnTableHeadTagsList = new ArrayList<>();
    List<Button> btnTimeTagsList = new ArrayList<>();
    Button[][] btnTableTagArray = new Button[tableCount][hourCount];
    String adminName = "";

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;

    // задаем начальное значение для выбора даты
    int myYear;
    int myMonth;
    int myDay;
    String dateReserveTomorrow;

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
        adminName = getIntent.getStringExtra("adminName");

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // сначала отрисуем кнопки шапки часов
        marginLength = option.convertDpToPixels(this, 2);
        addBtnHour();
        // также отрисуем кнопку выбора даты и шапки столов
        addBtnTableHead();

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        // покажем текущее время
        tvTime = findViewById(R.id.tvTime);
        actualTime();

        // отрисовываем таблицу заказов на изначальную дату
        addBtnCommon();
        addBtnHour();
        choseTypeTable();

        try {
            choseBtnCommon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        openQuitDialog();
    }

    public void openQuitDialog() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(CommonActivity.this);
        builderAlert.setTitle("Выход: Вы уверены?")
                .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                .setPositiveButton("Да", ((dialogInterface, i) -> {
                    Intent intent = new Intent(CommonActivity.this, MainActivity.class);
                    // передаем название заголовка
                    intent.putExtra("headName", "Резервы");
                    startActivity(intent);
                }));
        builderAlert.setIcon(R.drawable.bol_pyramide1);

        AlertDialog alertDialog = builderAlert.create();
        alertDialog.show();
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
                startActivity(intent);
                break;
            }
            case R.id.reserves: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Резервы");
                startActivity(intent);
                break;
            }
            case R.id.setting: {
                intent = new Intent("settingActivity");
                // передаем имя админа (взависимости от переданного имени, разные права доступа)
                intent.putExtra("adminName", adminName);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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
                intent.putExtra("adminName", adminName);
                startActivity(intent);
                // при добавлении нового резерва, также нужно обновить таблицу резерва
                break;
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
                tvTime.setText(timeFormat.format(new Date()));
                // также обращаемся каждую минуту к калькулятору оставшегося времени
//                calculateduration(timeFormat.format(currentTime));
                actualTime();  // мисис рекурсия
            });
        }).start();
    }

    private void choseTypeTable() {
        // заодно слушаем кнопку с выбором даты
        btnDate.findViewWithTag("btnChoseDate");
        btnDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                datePicker();
            }
        });
        btnDate1.findViewWithTag("btnChoseDate1");
        btnDate1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                datePicker();
            }
        });

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
                int numTable = cursorTables.getInt(numberTableIndex);
                btnTableHead = btnTableHeadTagsList.get(numTable - 1).findViewWithTag("btnTableHead" + numTable);

                // меняем фон кнопки каждого стола, в зависимости от типа стола
                if (cursorTables.getString(typeIndex).equals("Американский пул")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pool1);
                } else if (cursorTables.getString(typeIndex).equals("Русская пирамида")) {
                    btnTableHead.setBackgroundResource(R.drawable.bol_pyramide1);
                    btnTableHead.setTextColor(Color.WHITE);
                }

                btnTableHead.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent("tableActivity");
                        // передаем название заголовка
                        intent.putExtra("numTable", numTable);
                        intent.putExtra("adminName", adminName);
                        startActivity(intent);
                    }
                });

            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    private void choseBtnCommon() throws IOException {
        Log.i("Gas", "\n ...//... ");

        dateReserveTomorrow = btnDate.getText().toString();  // Start date
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(dateFormat.parse(dateReserveTomorrow)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 1);  // number of days to add
        dateReserveTomorrow = dateFormat.format(c.getTime());  // dt is now the new date

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
            do {
                if ((cursorOrders.getString(reserveDateIndex).equals(btnDate.getText().toString()))
                        || (cursorOrders.getString(reserveDateIndex).equals(dateReserveTomorrow))) {
                    // меняем нужные кнопки
                    // смотрим какой это стол
                    // кооордината по строке
                    int indexNumTable = cursorOrders.getInt(numTableIndex);
                    // находим кнопку по времени резерва, воспользуемся спец. методом
                    // коорината по столбцу
                    int indexHourTable = indexTimeMethod(cursorOrders.getString(reserveTimeIndex),
                            cursorOrders.getString(reserveDateIndex));
                    Log.i("Gas", "indexHourTable = " + indexHourTable);
                    // т.к. массив начинается с 0, а номера столов с 1 ...
                    // [i-1][j-1], где i-номер стола, j-время
                    btnTable = btnTableTagArray[indexNumTable - 1][indexHourTable];

                    Log.i("Gas", "cursorOrders.getString(reserveDateIndex) = " + cursorOrders.getString(reserveDateIndex));
                    Log.i("Gas", "dateReserveTomorrow = " + dateReserveTomorrow);

                    //  Идем по заказам
                    // находим все заказы на указанный день

                    // если дата резерва совпадает с ячейкой БД
                    if (cursorOrders.getString(reserveDateIndex).equals(btnDate.getText().toString())) {
                        if (indexHourTable < 13) { // если время с 11 по 23
                            // записываем время с БД в отдельный параметр
                            String reserveTime = cursorOrders.getString(reserveTimeIndex);
                            // записываем продолжительность с БД в отдельный параметр
                            int durationMinute = cursorOrders.getInt(durationIndex);
                            // записываем клиента с БД в отдельный параметр
                            String client = cursorOrders.getString(clientIndex);

                            // выбираем кнопку для покраски (красим кнопки)
                            // передаем туда индексы кнопки в матрице
                            changeBtnCommon(indexNumTable, indexHourTable, reserveTime, durationMinute, client);
                        }
                    } else if (cursorOrders.getString(reserveDateIndex).equals(dateReserveTomorrow)) {
                        // если следующий день
                        if (indexHourTable > 12 && indexHourTable < 18) { // если время с 0 по 4
                            // если врея от 0 до 5 утра (индекс времени от 13 по 17)

                            // записываем время с БД в отдельный параметр
                            String reserveTime = cursorOrders.getString(reserveTimeIndex);
                            Log.i("Gas", "reserveTime = " + reserveTime);
                            // записываем продолжительность с БД в отдельный параметр
                            int durationMinute = cursorOrders.getInt(durationIndex);
                            // записываем клиента с БД в отдельный параметр
                            String client = cursorOrders.getString(clientIndex);

                            // выбираем кнопку для покраски (красим кнопки)
                            // передаем туда индексы кнопки в матрице
                            changeBtnCommon(indexNumTable, indexHourTable, reserveTime, durationMinute, client);
                        }
                    }
                }

            } while (cursorOrders.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
    }

    private void changeBtnCommon(int indexNumTable, int indexHourTable,
                                 String reserveTime, int durationMinute, String client) throws IOException {
//        где-то здесь д.б. условия для правильной покраски кнопки на время после 0 и до 5

        int reserveMinute;
        // инициализируем значения
        String[] reserveTimeArr = reserveTime.split(":");
        reserveMinute = Integer.parseInt(reserveTimeArr[1]);

        // получаем входной поток
        InputStream inputStream;
        Drawable imageBtnCommon;


        // можем посчитать кол-во кнопок, кот. нужно закрасить
        // если продолжительность резерва больше 1 часа, то уже больше 1 кнопки
        int btnCountDraw = (durationMinute + reserveMinute) / 60;
        // если ровно час, то добавлять кнопку не нужно (делится на 60мин без остатка)
        if (durationMinute % 60 != 0) {
            btnCountDraw++;
        }
        // если продолжительность резерва больше 1, то все предыдущие нужно красить полностью (кроме последней)
        if (btnCountDraw > 1) {
            // для первой кнопки нужны осбобые условия покраски (м.б. что не сначала, но точно до конца)
            Log.i("Gas", "для первого часа reserveTime = " + reserveTime);
            Log.i("Gas", "durationMinute = " + durationMinute);
            drawCustomBtn(indexNumTable, indexHourTable, reserveMinute, durationMinute, client, false);
//            ...
            //начинаем со всторой кнопки
            for (int i = indexHourTable + 1; i < indexHourTable + btnCountDraw - 1; i++) {
                inputStream = getAssets().open("reserve_60min.png");
                // загружаем как Drawable
                imageBtnCommon = Drawable.createFromStream(inputStream, null);
                btnTableTagArray[indexNumTable - 1][i].setBackgroundDrawable(imageBtnCommon);
                btnTableTagArray[indexNumTable - 1][i].setText(client);
            }
            // для последней кнопки нужны особые условия
            // далее разделить durationMinute на btnCountDraw-1 и найти остаток
            //  и конечно же вычесть reserveMinute - и будет кол-во наших минут
            Log.i("Gas", "для последнего часа reserveTime = " + reserveTime);
            durationMinute = durationMinute - ((btnCountDraw - 1) * 60) + reserveMinute;
            Log.i("Gas", "durationMinute After = " + durationMinute);
            // ну и рисуем
            drawCustomBtn(indexNumTable, (indexHourTable + btnCountDraw - 1), reserveMinute, durationMinute, client, true);
        }
        // если у нас 1 кнопка то особые условия для этой кнопки
        else if (btnCountDraw == 1) {
            drawCustomBtn(indexNumTable, indexHourTable, reserveMinute, durationMinute, client, false);
        }


    }

    private void drawCustomBtn(int indexNumTable, int indexHourTable, int reserveMinute,
                               int durationMinute, String client, boolean endBtnFlag) throws IOException {

        // получаем входной поток
        InputStream inputStream = getAssets().open("reserve_0min.png");
        Drawable imageBtnCommon;
        // красим кнопки
        // Если минуты Резерва начинаются c начала часа или если эта последняя кнопка, то красим всегда с начала
        if ((reserveMinute == 0) || (endBtnFlag)) {
            Log.i("Gas", "минуты Резерва начинаются c начала часа");
            if ((durationMinute > 2) && (durationMinute < 8)) {
                Log.i("Gas", "до 5 мин");
                inputStream = getAssets().open("reserve_5min_atStart.png");
            } else if ((durationMinute > 7) && (durationMinute < 13)) {
                Log.i("Gas", "до 10 мин");
                inputStream = getAssets().open("reserve_10min_atStart.png");
            } else if ((durationMinute > 12) && (durationMinute < 18)) {
                Log.i("Gas", "до 15 мин");
                inputStream = getAssets().open("reserve_15min_atStart.png");
            } else if ((durationMinute > 7) && (durationMinute < 23)) {
                Log.i("Gas", "до 20 мин");
                inputStream = getAssets().open("reserve_20min_atStart.png");
            } else if ((durationMinute > 22) && (durationMinute < 28)) {
                Log.i("Gas", "до 25 мин");
                inputStream = getAssets().open("reserve_25min_atStart.png");
            } else if ((durationMinute > 27) && (durationMinute < 33)) {
                Log.i("Gas", "до 30 мин");
                inputStream = getAssets().open("reserve_30min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((durationMinute > 32) && (durationMinute < 38)) {
                Log.i("Gas", "до 35 мин");
                inputStream = getAssets().open("reserve_35min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((durationMinute > 37) && (durationMinute < 43)) {
                Log.i("Gas", "до 40 мин");
                inputStream = getAssets().open("reserve_40min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((durationMinute > 42) && (durationMinute < 48)) {
                Log.i("Gas", "до 45 мин");
                inputStream = getAssets().open("reserve_45min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((durationMinute > 47) && (durationMinute < 53)) {
                Log.i("Gas", "до 50 мин");
                inputStream = getAssets().open("reserve_50min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((durationMinute > 52) && (durationMinute < 58)) {
                Log.i("Gas", "до 55 мин");
                inputStream = getAssets().open("reserve_55min_atStart.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if (durationMinute > 57) {
                Log.i("Gas", "до конца часа");
                inputStream = getAssets().open("reserve_60min.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            }
        }

        // если минуты резерва начинаются не с начала часа
        // Пример: время резерва: 18:15 продолжительностью 45мин
        // или время резерва: 16:30 продолжительностью 60мин
        else if ((reserveMinute > 0) && (reserveMinute < 57)) {
            Log.i("Gas", "время резерва: 16:30 продолжительностью 60мин");
            // пока рисунки только до конца часа, поэтому меньше условий
//            durationMinute = durationMinute + reserveMinute;
            if ((reserveMinute > 2) && (reserveMinute < 8)) {
                Log.i("Gas", "с 5 мин");
                inputStream = getAssets().open("reserve_5min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 7) && (reserveMinute < 13)) {
                Log.i("Gas", "с 10 мин");
                inputStream = getAssets().open("reserve_10min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 12) && (reserveMinute < 18)) {
                Log.i("Gas", "с 15 мин");
                inputStream = getAssets().open("reserve_15min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 17) && (reserveMinute < 23)) {
                Log.i("Gas", "с 20 мин");
                inputStream = getAssets().open("reserve_20min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 22) && (reserveMinute < 28)) {
                Log.i("Gas", "с 25 мин");
                inputStream = getAssets().open("reserve_25min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 27) && (reserveMinute < 33)) {
                Log.i("Gas", "с 30 мин");
                inputStream = getAssets().open("reserve_30min_end.png");
                btnTableTagArray[indexNumTable - 1][indexHourTable].setText(client);
            } else if ((reserveMinute > 32) && (reserveMinute < 38)) {
                Log.i("Gas", "с 35 мин");
                inputStream = getAssets().open("reserve_35min_end.png");
            } else if ((reserveMinute > 37) && (reserveMinute < 43)) {
                Log.i("Gas", "с 40 мин");
                inputStream = getAssets().open("reserve_40min_end.png");
            } else if ((reserveMinute > 42) && (reserveMinute < 48)) {
                Log.i("Gas", "с 45 мин");
                inputStream = getAssets().open("reserve_45min_end.png");
            } else if ((reserveMinute > 47) && (reserveMinute < 53)) {
                Log.i("Gas", "с 50 мин");
                inputStream = getAssets().open("reserve_50min_end.png");
            } else if ((reserveMinute > 52) && (reserveMinute < 58)) { //логично, если достигнет
                Log.i("Gas", "с 55 мин");
                inputStream = getAssets().open("reserve_55min_end.png");
            } else { // (if (reserveMinute > 57) )
                Log.i("Gas", "с 5 мин");
//                inputStream = getAssets().open("reserve_0min.png");
            }
        }

        // загружаем как Drawable
        imageBtnCommon = Drawable.createFromStream(inputStream, null);
        btnTableTagArray[indexNumTable - 1][indexHourTable].setBackgroundDrawable(imageBtnCommon);
    }

    private Integer indexTimeMethod(String time, String date) {
        // метод принимает дату и время и возвращает номер j позиции(=часу) в btnTableTagArray

        // получили переданный час
        // у нас есть время в виде строки в формате: hh:mm
        String[] hourAr = time.split(":");
        int hour = Integer.parseInt(hourAr[0]);
        // теперь сравниваем, сложная тараканиха по индексу
        int result;
        if (hour > 10 && hour < 24) result = hour - 11;
        else result = hour + 13;
        return result;
    }

    public void addBtnTableHead() {
        linTableHead = findViewById(R.id.linTableHead);
        LinearLayout.LayoutParams marginBtnTable;


        btnDate = new Button(linTableHead.getContext());
        btnDate.setLayoutParams(new LinearLayout.LayoutParams(
                option.convertDpToPixels(this, 80),
                option.convertDpToPixels(this, 50)));

        btnDate.setTag("btnChoseDate");
        // задаем сегодняшнюю дату
        btnDate.setText(dateFormat.format(new Date()));
        btnDate.setTextSize(14);
        btnDate.setBackgroundResource(R.drawable.btn_style_2);

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

//            marginBtnTable = (LinearLayout.LayoutParams) btnTableHead.getLayoutParams();
//            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linTableHead.addView(btnTableHead);
        }


        btnDate1 = new Button(linTableHead.getContext());
        btnDate1.setLayoutParams(new LinearLayout.LayoutParams(
                option.convertDpToPixels(this, 80),
                option.convertDpToPixels(this, 50)));

        btnDate1.setTag("btnChoseDate1");
        // задаем сегодняшнюю дату
        btnDate1.setText(dateFormat.format(new Date()));
        btnDate1.setTextSize(14);
        btnDate1.setBackgroundResource(R.drawable.btn_style_2);

        marginBtnTable = (LinearLayout.LayoutParams) btnDate1.getLayoutParams();
        marginBtnTable.setMargins(0, 0, marginLength, 0);

        linTableHead.addView(btnDate1);
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
                    option.convertDpToPixels(this, 120),
                    option.convertDpToPixels(this, 50)));

            btnTime.setTag("btnTime" + hourRight);
            btnTimeTagsList.add(btnTableHead);
            btnTime.setText(hourRight + ":00");
            btnTime.setTextSize(14);
            btnTime.setClickable(false);

//            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTime.getLayoutParams();
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
                        option.convertDpToPixels(this, 120),
                        option.convertDpToPixels(this, 40)));

//                btnTable.setTag("btnTable." + (i+1) + "." + hourRight);
                btnTable.setTag(hourRight);
                btnTableTagArray[i][j] = btnTable;
                btnTable.setBackgroundResource(R.drawable.btn_style_4);
                btnTable.setClickable(false);
                btnTable.setTextSize(10);

//                LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnTable.getLayoutParams();
//                marginBtnTable.setMargins(0, 0, marginLength, 0);

                linTable.removeView(btnTable);
                linTable.addView(btnTable);
            }
            linTableTime.removeView(linTable);
            linTableTime.addView(linTable);
        }
    }

    public void clearBtnCommon() throws IOException {
        for (int i = 0; i < tableCount; i++) {
            for (int j = 0; j < hourCount; j++) {
                Button btnTable = btnTableTagArray[i][j];
//                btnTable.setBackgroundResource(R.drawable.btn_style_4);
                // получаем входной поток
                InputStream inputStream = getAssets().open("reserve_0min.png");
                Drawable imageBtnCommon = Drawable.createFromStream(inputStream, null);
                btnTable.setBackgroundDrawable(imageBtnCommon);

                btnTable.setText("");
            }
        }
    }

    private void datePicker() {
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


                        dateReserveTomorrow = btnDate.getText().toString();  // Start date
                        Calendar c = Calendar.getInstance();
                        try {
                            c.setTime(Objects.requireNonNull(dateFormat.parse(dateReserveTomorrow)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        c.add(Calendar.DATE, 1);  // number of days to add
                        dateReserveTomorrow = dateFormat.format(c.getTime());  // dt is now the new date

                        try {
                            choseBtnCommon();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }, myYear, myMonth, myDay);
        datePickerDialog.show();
    }
}