package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TableActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass optionallyClass = new OptionallyClass();
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    TextView tvTypeTable, tvCurrentClient, tvCurrentGameDuration, tvTimeStartGame, tvTimeEndGame, tvImage;
    AutoCompleteTextView actvNameTable;
    String[] durationTimeArr = {"30 мин", "45 мин", "50 мин", "1 ч", "1 ч 15 мин", "1 ч 30 мин", "1 ч 45 мин",
            "2 ч", "2 ч 15 мин", "2 ч 30 мин", "2 ч 45 мин", "3 ч", "3 ч 15 мин", "3 ч 30 мин", "3 ч 45 мин",
            "4 ч", "4 ч 15 мин", "4 ч 30 мин", "4 ч 45 мин", "5 ч", "5 ч 15 мин", "5 ч 30 мин", "5 ч 45 мин",
            "6 ч"};
    Button btnShowTableReserve, btnAdd, btnStart, btnResume, btnDeleteReserve;
    List<String> typeTableList = new ArrayList<>();
    List<String> nameTableList = new ArrayList<>();
    List<Integer> numTableList = new ArrayList<>();
    List<ReserveTable> checkList = new ArrayList<>();
    ReserveTable finishTable, getTable;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;
    int getId, getNumTable, getDurationMinute, numberRowDB;
    boolean findReserveFlag;
    String type, getAdminName, currentDateStr, currentTimeStr, whoCall, getReserveFinishTimeStr,
            getReserveDateStr, getReserveStartTimeStr, getClient, getBron, getDate, getOrderTime, getOrderDate;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        // загружаем данные с
        Intent getIntent = getIntent();
        whoCall = getIntent.getStringExtra("whoCall");
        if (whoCall == null) whoCall = "";
        Log.i("Gas", "whoCall = " + whoCall);
        getNumTable = getIntent.getIntExtra("numTable", -1);
        getId = getIntent.getIntExtra("id", -1);
        getClient = getIntent.getStringExtra("client");
        getReserveDateStr = getIntent.getStringExtra("reserveDateStr");
        getReserveStartTimeStr = getIntent.getStringExtra("reserveStartTimeStr");
        getReserveFinishTimeStr = getIntent.getStringExtra("reserveFinishTimeStr");
        getBron = getIntent.getStringExtra("bron");
        getOrderDate = getIntent.getStringExtra("dateOrder");
        getOrderTime = getIntent.getStringExtra("timeOrder");
        getDurationMinute = getIntent.getIntExtra("duration", -1);
        getAdminName = getIntent.getStringExtra("adminName");
        Log.i("Gas4", "getAdminName in Table = " + getAdminName);

        currentDateStr = dateFormat.format(new Date());
        currentTimeStr = timeFormat.format(new Date());

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        initNameTableList();

        tvTypeTable = findViewById(R.id.tvTypeTable);
        tvTimeStartGame = findViewById(R.id.tvTimeStartGame);
        tvTimeEndGame = findViewById(R.id.tvTimeEndGame);
        tvCurrentClient = findViewById(R.id.tvCurrentClient);
        tvCurrentGameDuration = findViewById(R.id.tvCurrentGameDuration);
        tvImage = findViewById(R.id.tvImage);

        btnShowTableReserve = findViewById(R.id.btnShowTableReserve);
        btnShowTableReserve.setOnClickListener(this);
        btnDeleteReserve = findViewById(R.id.btnDeleteReserve);
        btnDeleteReserve.setOnClickListener(this);
        btnDeleteReserve.setVisibility(View.INVISIBLE);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);
        btnResume = findViewById(R.id.btnResume);
        btnResume.setOnClickListener(this);

        actvNameTable = findViewById(R.id.actvNameTable);
        actvNameTable.setTextColor(Color.BLACK);
        actvNameTable.setText("Стол № " + getNumTable);
        actvNameTable.setShowSoftInputOnFocus(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, nameTableList);
        actvNameTable.setOnTouchListener((v, event) -> {
            actvNameTable.showDropDown();
            return false;
        });
        actvNameTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                choseTable();
            }
        });
        actvNameTable.setAdapter(adapter);

        optionallyClass.checkOldReserve(this);

        choseTable();

        if (whoCall.equals("btnCommon")) {
            Log.i("Gas5", "getNumTable = " + getNumTable);
            Log.i("Gas5", "getReserveDateStr = " + getReserveDateStr);
            Log.i("Gas5", "getReserveStartTimeStr = " + getReserveStartTimeStr);
            Log.i("Gas5", "getDurationMinute = " + getDurationMinute);
            Log.i("Gas5", "getClient = " + getClient);
            Log.i("Gas5", "getAdminName = " + getAdminName);
            Log.i("Gas5", "getBron = " + getBron);
            getTable = new ReserveTable(
                    getId,
                    getNumTable,
                    getReserveDateStr,
                    getReserveStartTimeStr,
                    getDurationMinute,
                    getOrderDate,
                    getOrderTime,
                    getClient,
                    getAdminName,
                    getBron,
                    "");
            checkList.add(getTable);
        } else {
            changeFreeTableTime();
        }
        actualTime();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btnShowTableReserve: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Резервы");
                intent.putExtra("getFilterNumTable", getNumTable);
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                // что мы м. передать туда
                intent.putExtra("whoCall", "tableActivity_Add");
                intent.putExtra("adminName", getAdminName);
                intent.putExtra("numTable", getNumTable);
                intent.putExtra("type", type);
                startActivity(intent);
                // при добавлении нового резерва, также нужно обновить таблицу резерва
                break;
            }
            case R.id.btnStart: {
                if (btnStart.getText().equals("Открыть")) {
                    intent = new Intent("newOrderActivity");
                    // что мы м. передать туда
                    intent.putExtra("whoCall", "tableActivity_Start");
                    intent.putExtra("whatIsBtn", "btnStart");
                    intent.putExtra("adminName", getAdminName);
                    intent.putExtra("numTable", getNumTable);
                    intent.putExtra("date", currentDateStr);
                    intent.putExtra("time", currentTimeStr);
                    Log.i("Gas5", "type = " + type);
                    intent.putExtra("type", type);
                    startActivity(intent);
                } else {
                    // если кнопка закрыть
                    openQuitDialog();
                }
                break;
            }
            case R.id.btnResume: {
                resumeBtnChangeDurationInDB();
            }
        }
    }

    public void openQuitDialog() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(TableActivity.this);
        builderAlert.setTitle("Закрыть стол: Вы уверены?")
                .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                .setPositiveButton("Да", ((dialogInterface, i) -> {
                    // действия закрытия стола
                    // нам нужно изменить продолжительность игры в БД на окончание сейчас
                    stopBtnChangeDurationInDB();
                }));
        builderAlert.setIcon(R.drawable.bol_pyramide1);

        AlertDialog alertDialog = builderAlert.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(TableActivity.this, CommonActivity.class);
        intent.putExtra("adminName", getAdminName);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private void stopBtnChangeDurationInDB() {
        Log.i("Gas6", " --- /// ---");
        Log.i("Gas6", "Start Method:  stopBtnChangeDurationInDB");

        Log.i("Gas6", "finishTable.getNumTable = " + finishTable.getNumTable());

        Date reserveDateTime = new Date();
        int newDuration = 0;
        {
            Calendar currentDateTimeCal = Calendar.getInstance();
            Calendar reserveDateTimeCal = Calendar.getInstance();
            try { // переводим дату и время из строки в Date
                reserveDateTime = dateTimeFormat.parse(finishTable.getDateStartReserve() + " " + finishTable.getTimeStartReserve());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            reserveDateTimeCal.setTime(reserveDateTime);
            newDuration = (Integer.parseInt(String.format("%.0f",
                    (currentDateTimeCal.getTimeInMillis() - reserveDateTimeCal.getTimeInMillis()) / (1000d * 60)))) - 1;
            Log.i("Gas5", "currentDateTimeCal = " + currentDateTimeCal.getTime());
            Log.i("Gas5", "reserveDateTimeCal = " + reserveDateTimeCal.getTime());
            Log.i("Gas5", "newDuration = " + newDuration);
        }

        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + finishTable.getIdOrder(), null);

        contentValues.put(DBHelper.KEY_ID, finishTable.getIdOrder());
        contentValues.put(DBHelper.KEY_NUM_TABLE, finishTable.getNumTable());
        contentValues.put(DBHelper.KEY_RESERVE_DATE, finishTable.getDateStartReserve());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, finishTable.getTimeStartReserve());
        contentValues.put(DBHelper.KEY_DURATION, newDuration);
        Log.i("Gas6", "newDuration = " + newDuration);
        contentValues.put(DBHelper.KEY_CLIENT, finishTable.getClient());
        contentValues.put(DBHelper.KEY_EMPLOYEE, getAdminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, finishTable.getDateOrder());
        contentValues.put(DBHelper.KEY_ORDER_TIME, finishTable.getTimeOrder());
        contentValues.put(DBHelper.KEY_BRON, finishTable.getBron());
        contentValues.put(DBHelper.KEY_STATUS, "");

        database.insert(DBHelper.ORDERS, null, contentValues);

        // нужно обновить стол
        Intent intent = new Intent("tableActivity");
        // передаем название заголовка
        intent.putExtra("numTable", getNumTable);
        intent.putExtra("adminName", getAdminName);
        startActivity(intent);

        Log.i("Gas5", "finishTable.duration = " + finishTable.getDuration());
        Log.i("Gas5", "finishTable.getDateEndReserve() = " + finishTable.getDateEndReserve());
        Log.i("Gas5", "finishTable.getTimeEndReserve() = " + finishTable.getTimeEndReserve());
    }

    @SuppressLint("DefaultLocale")
    private void resumeBtnChangeDurationInDB() {
        Log.i("Gas5", "finishTable.getNumTable = " + finishTable.getNumTable());

        Date reserveDateTime = new Date();
        int newDuration = 0;
        {
            Calendar currentDateTimeCal = Calendar.getInstance();
            Calendar reserveDateTimeCal = Calendar.getInstance();
            try { // переводим дату и время из строки в Date
                reserveDateTime = dateTimeFormat.parse(finishTable.getDateStartReserve() + " " + finishTable.getTimeStartReserve());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            reserveDateTimeCal.setTime(reserveDateTime);
            newDuration = (Integer.parseInt(String.format("%.0f",
                    (currentDateTimeCal.getTimeInMillis() - reserveDateTimeCal.getTimeInMillis()) / (1000d * 60)))) - 1;
            Log.i("Gas5", "currentDateTimeCal = " + currentDateTimeCal.getTime());
            Log.i("Gas5", "reserveDateTimeCal = " + reserveDateTimeCal.getTime());
            Log.i("Gas5", "newDuration = " + newDuration);
        }

        newDuration = finishTable.getDuration() + 15;
        newDuration = optionallyClass.checkLeftEndWorkMinute(newDuration);


        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + finishTable.getIdOrder(), null);

        contentValues.put(DBHelper.KEY_ID, finishTable.getIdOrder());
        contentValues.put(DBHelper.KEY_NUM_TABLE, finishTable.getNumTable());
        contentValues.put(DBHelper.KEY_RESERVE_DATE, finishTable.getDateStartReserve());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, finishTable.getTimeStartReserve());
        contentValues.put(DBHelper.KEY_DURATION, newDuration);
        contentValues.put(DBHelper.KEY_CLIENT, finishTable.getClient());
        contentValues.put(DBHelper.KEY_EMPLOYEE, getAdminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, finishTable.getDateOrder());
        contentValues.put(DBHelper.KEY_ORDER_TIME, finishTable.getTimeOrder());
        contentValues.put(DBHelper.KEY_BRON, finishTable.getBron());
        contentValues.put(DBHelper.KEY_STATUS, "");

        database.insert(DBHelper.ORDERS, null, contentValues);

        Toast.makeText(TableActivity.this, "Добавлено 15 минут. \n", Toast.LENGTH_SHORT).show();

        // нужно обновить стол
        Intent intent = new Intent("tableActivity");
        // передаем название заголовка
        intent.putExtra("numTable", getNumTable);
        intent.putExtra("adminName", getAdminName);
        startActivity(intent);

        Log.i("Gas5", "finishTable.duration = " + finishTable.getDuration());
        Log.i("Gas5", "finishTable.getDateEndReserve() = " + finishTable.getDateEndReserve());
        Log.i("Gas5", "finishTable.getTimeEndReserve() = " + finishTable.getTimeEndReserve());
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

    // метод формирует список номеров и список типы столов
    private void initNameTableList() {
        // получаем данные c табл "TABLES"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
        int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
        if (cursorTables.moveToFirst()) {
            cursorTables.getColumnIndex(DBHelper.KEY_ID);
            do {
                // инициализируем каждую кнопку шапки стола
                nameTableList.add("Стол № " + cursorTables.getInt(numberTableIndex));
                numTableList.add(cursorTables.getInt(numberTableIndex));
                typeTableList.add(cursorTables.getString(typeIndex));
            } while (cursorTables.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    // метод меняет вьюшки активити в зависимости от типа стола
    @SuppressLint("ClickableViewAccessibility")
    private void choseTable() {
        // мы получаем выбранное значение
        String nameTable = actvNameTable.getText().toString();
        Log.i("Gas5", "nameTable = " + nameTable);
        for (int i = 0; i < nameTableList.size(); i++) {
            if (nameTableList.get(i).equals(nameTable)) {
                getNumTable = numTableList.get(i);
                // меняем картинку, в зависимости от типа стола
                if (typeTableList.get(i).equals("Американский пул")) {
                    tvImage.setBackgroundResource(R.drawable.table_pool);
                    type = "Американский пул";
                } else {
                    tvImage.setBackgroundResource(R.drawable.table_pyramide);
                    type = "Русская пирамида";
                }
                tvTypeTable.setText(type);
                break;
            }
        }
//        changeFreeTableTime();
    }

    // метод формирует список резервов на сегодня, которые нужно проверить тщательнее
    // и вызывает следующей метод
    private void changeFreeTableTime() {
        checkList.clear();

        // получаем данные c табл "ORDERS"
        String selection = DBHelper.KEY_STATUS + " = \'\'";
        cursorOrders = database.query(DBHelper.ORDERS,
                null, selection, null,
                null, null, null);
        if (cursorOrders.moveToFirst()) {
            int idIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ID);
            int numTableIndex = cursorOrders.getColumnIndex(DBHelper.KEY_NUM_TABLE);
            int reserveDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_DATE);
            int reserveTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_TIME);
            int durationIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DURATION);
            int orderDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
            int orderTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
            int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
            int employeeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_EMPLOYEE);
            int bronIndex = cursorOrders.getColumnIndex(DBHelper.KEY_BRON);
            int statusIndex = cursorOrders.getColumnIndex(DBHelper.KEY_STATUS);

            Log.i("Gas5", "getNumTable = " + getNumTable);
            do {
                Log.i("Gas5", "idIndex = " + cursorOrders.getInt(idIndex));
                Log.i("Gas5", "cursorOrders.getInt(numTableIndex) = " + cursorOrders.getInt(numTableIndex));
                // если стол м.б. занят этим заказом (номера столов совпадают)
                if (getNumTable == cursorOrders.getInt(numTableIndex)) {
                    Log.i("Gas5", "   Date currentDateStr = " + currentDateStr);
                    Log.i("Gas5", "   Date reserveDateIndex = " + cursorOrders.getString(reserveDateIndex));
                    // если даты совпадают
                    if (currentDateStr.equals(cursorOrders.getString(reserveDateIndex))) {
                        Log.i("Gas5", "checkList add");
                        checkList.add(new ReserveTable(
                                cursorOrders.getInt(idIndex),
                                cursorOrders.getInt(numTableIndex),
                                cursorOrders.getString(reserveDateIndex),
                                cursorOrders.getString(reserveTimeIndex),
                                cursorOrders.getInt(durationIndex),
                                cursorOrders.getString(orderDateIndex),
                                cursorOrders.getString(orderTimeIndex),
                                cursorOrders.getString(clientIndex),
                                cursorOrders.getString(employeeIndex),
                                cursorOrders.getString(bronIndex),
                                cursorOrders.getString(statusIndex)));
                    }
                }
            } while (cursorOrders.moveToNext());
            Log.i("Gas5", "List checkList: " + checkList);
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();

        checkFreeTimeReserve();
    }

    // метод проверяет занят ли стол в данный момент (формирует список столов на сегодня, которые нужно проверить тщательнее)
    @SuppressLint("SetTextI18n")
    private void checkFreeTimeReserve() {
        Log.i("Gas5", " --- // --- ");
        Log.i("Gas5", "Start method: checkFreeTimeReserve from TableActivity");

        // нам просто нужно определить занят ли стол в данный момент, ничего лишнего
        String[] newReserveStartTimeArr = currentTimeStr.split(":");
        Log.i("Gas5", "currentTimeStr = " + currentTimeStr);
        int currentHour = Integer.parseInt(newReserveStartTimeArr[0]);
        int currentMinute = Integer.parseInt(newReserveStartTimeArr[1]);

        Log.i("Gas5", "сейчас часов = " + currentHour);
        Log.i("Gas5", "сейчас минут = " + currentMinute);

        Log.i("Gas5", "   Size checkList = " + checkList.size() + " (Кол-во проверяемых заказов на выбранную дату)");
        Log.i("Gas5", "   Start cycle (начинаем цикл по этим заказам)");
        Log.i("Gas5", "NumTable = " + getNumTable);

        boolean busyFlag = false;
        boolean leftLess30MinFlag = false;
        int finishLeftMinute = 1080;
        for (int i = 0; i < checkList.size(); i++) {
            Log.i("Gas5", " i = " + i);
            int hourStartReserve = checkList.get(i).getHourStartReserve();
            Log.i("Gas5", "hourStartReserve = " + hourStartReserve);
            int minuteStartReserve = checkList.get(i).getMinuteStartReserve();
            Log.i("Gas5", "minuteStartReserve = " + minuteStartReserve);
            int durationMinuteReserve = checkList.get(i).getDuration();
            Log.i("Gas5", "Old durationMinuteReserve = " + durationMinuteReserve);
            int hourFinishReserve = hourStartReserve + (minuteStartReserve + durationMinuteReserve) / 60;
            Log.i("Gas5", "Old hourFinishReserve " + hourFinishReserve);
            int minuteFinishReserve = (minuteStartReserve + durationMinuteReserve) % 60;
            Log.i("Gas5", "Old minuteFinishReserve = " + minuteFinishReserve);

            // проверяем если время протыкает, то сразу заканчиваем проверку и пользуемся данными этого резерва

            // фильтр по времени
            // посчитаем сколько остается минут до след. резерва (на сколько м. зарезервировать)
            if (hourStartReserve < 5) hourStartReserve += 24;
            int leftMinute = ((hourStartReserve - currentHour) * 60) + (minuteStartReserve - currentMinute);
            Log.i("Gas5", "hourStartReserve = " + hourStartReserve);
            Log.i("Gas5", "currentHour = " + currentHour);
            Log.i("Gas5", "minuteStartReserve = " + minuteStartReserve);
            Log.i("Gas5", "currentMinute = " + currentMinute);
            Log.i("Gas5", "Осталось минут до след резерва leftMinute = " + leftMinute);

            // если СТОЛ СВОБОДЕН и остается больше 30 мин
            if (leftMinute >= 30 || (checkList.get(i).getEndDateTimeReserveCal().before(Calendar.getInstance()))) {
                Log.i("Gas5", "Стол свободен! И до следующего резерва больше 30 мин");
                // но нужно проверить дальше, если, вдруг дальше выяснится, что стол занят
                if (leftMinute < finishLeftMinute) {
                    finishLeftMinute = leftMinute;
                    finishTable = checkList.get(i);
                }
            } else if (leftMinute > 0) { // если СТОЛ СВОБОДЕН, но остается меньше 30 мин
                Log.i("Gas5", "Стол свободен, НО! Остается меньше 30 мин");
                if (leftMinute < finishLeftMinute) {
                    finishLeftMinute = leftMinute;
                    finishTable = checkList.get(i);
                }
                leftLess30MinFlag = true;
            } else {
                /*int finishMinute = ((hourFinishReserve - currentHour) * 60) + (minuteFinishReserve - currentMinute);
                Log.i("Gas5", "finishMinute = " + finishMinute);
                if (finishMinute > 0) {*/
                // если СТОЛ ОТКРЫТ(ЗАНЯТ) (текущее время Протыкает время резерва)
                Log.i("Gas5", "Этот стол сейчас занят! (проткнул)");
                finishTable = checkList.get(i);
                busyFlag = true;
                break;
            }
        }

        btnStart.setBackgroundResource(R.drawable.btn_style_7);
        btnStart.setText("Открыть");
//        btnResume.setClickable(false);
        btnStart.setClickable(true);

        String phoneClient = "8 800 2000 600";
        // если стол вообще есть
        if (finishTable != null) {
            // нужно найти номер клиента
            phoneClient = optionallyClass.findPhoneClient(this, finishTable.getClient());
            tvTimeStartGame.setText("Начало игры: " + finishTable.getTimeStartReserve());
            tvTimeEndGame.setText("Конец игры: " + finishTable.getTimeEndReserve());

            if (finishTable.getDuration() < 60)
                tvCurrentGameDuration.setText("Резерв на : " + finishTable.getDuration() + " мин");
            else {
                int hour = finishTable.getDuration() / 60;
                int minute = finishTable.getDuration() % 60;
                if (minute == 0) tvCurrentGameDuration.setText("Резерв на : " + hour + " ч ");
                else
                    tvCurrentGameDuration.setText("Резерв на : " + hour + " ч " + minute + " мин");
            }

            if (busyFlag) { // если СТОЛ ЗАНЯТ
                Log.i("Gas5", "finishTable.getDuration() = " + finishTable.getDuration());
                tvCurrentClient.setTextColor(Color.BLACK);
                btnStart.setText("Закрыть");
                btnStart.setBackgroundResource(R.drawable.btn_style_6);
//                btnResume.setClickable(true);
                tvCurrentClient.setText("Клиент: " + finishTable.getClient() + "\nТел: " + phoneClient);
            } else if (leftLess30MinFlag) { // если СТОЛ СВОБОДЕН, но <30 минут
                btnStart.setClickable(false);
                btnStart.setBackgroundResource(R.drawable.btn_style_6_1);
                tvCurrentClient.setTextColor(Color.RED);
                tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n" +
                        "(осталось " + finishLeftMinute + " мин.)\n" +
                        "Клиент: " + finishTable.getClient() + "\tТел: " + phoneClient);

            } else { // если стол свободен и >30 мин
                tvCurrentClient.setTextColor(Color.BLACK);
                if (finishLeftMinute > 60) {
                    int hour = finishLeftMinute / 60;
                    int minute = finishLeftMinute % 60;
                    if (minute == 0)
                        tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n(осталось " + hour + " час. ");
                    else
                        tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n(осталось " + hour + " час. " + minute + " мин.)");
                }
            }
        } else {
            // если заказов для этого стола на сегодня нет
            Log.i("Gas5", "Заказов для этого стола нет");
            tvCurrentClient.setText("Стол свободен");
            tvTimeStartGame.setText("Начало игры: __:__");
            tvTimeEndGame.setText("Конец игры: __:__");
            tvCurrentGameDuration.setText("Продолжительность игры: _ ч _ мин");
        }

        if (getTable != null) { // если мы открыли резерв через таблицу
            tvCurrentClient.setText("Резерв на " + finishTable.getTimeStartReserve() + " \n" +
                    "Клиент: " + finishTable.getClient() + "\tТел: " + phoneClient);

//            btnDeleteReserve.setVisibility(View.VISIBLE);
        } else {
//            btnDeleteReserve.setVisibility(View.INVISIBLE);
        }

    }

    // метод выполняется в параллельном потоке ежесекундно смотрит время и вызывает метод проверки занят ли стол в данный момент
    private void actualTime() {
        // каждую секунду обновляет время
        if (whoCall.equals("btnCommon")) {
            checkFreeTimeReserve();
        } else {
            changeFreeTableTime();
        }
        final Handler handler = new Handler();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                currentTimeStr = timeFormat.format(new Date());

                if (whoCall.equals("btnCommon")) {
                    checkFreeTimeReserve();
                } else {
                    changeFreeTableTime();
                }
                actualTime();  // мисис рекурсия
            });
        }).start();
    }
}