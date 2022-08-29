package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NewOrderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);

    OptionallyClass optionallyClass = new OptionallyClass();
    private final Map<Integer, String> numTypeTableMap = new HashMap<>();
    private final List<Integer> typeNumTableList = new ArrayList<>();
    private List<Integer> finishNumTableList = new ArrayList<>();
    List<String> finishList = new ArrayList<>();
    List<ReserveTable> checkList = new ArrayList<>();
    List<String> clientsList = new ArrayList<>();
    List<String> tariffList = new ArrayList<>();
    SwitchCompat switchTypeTable;
    TextView tvPyramid, tvPool;
    AutoCompleteTextView actvFreeTable, actvClient, actvTariff;
    Button btnNewReserveTime, btnNewReserveDate, btnNewReserveDuration, btnCreateReserve, btnCreateClient;
    int numTable = -1;

    String[] durationTimeArr = {"30 мин", "45 мин", "50 мин", "1 ч", "1 ч 15 мин", "1 ч 30 мин", "1 ч 45 мин",
            "2 ч", "2 ч 15 мин", "2 ч 30 мин", "2 ч 45 мин", "3 ч", "3 ч 15 мин", "3 ч 30 мин", "3 ч 45 мин",
            "4 ч", "4 ч 15 мин", "4 ч 30 мин", "4 ч 45 мин", "5 ч", "5 ч 15 мин", "5 ч 30 мин", "5 ч 45 мин",
            "6 ч"};

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders, cursorClients;

    final Calendar currentDateCalendar = Calendar.getInstance();
    String currentHourSt, currentMinuteSt, currentYearSt, currentMonthSt, currentDaySt, hourReserveSt, minuteReserveSt;
    // задаем начальное значение для выбора времени (не важно какие)
    int hourReserve = 23;
    int minuteReserve = 59;
    // Get Current Date
    int yearCurrent = currentDateCalendar.get(Calendar.YEAR);
    int monthCurrent = currentDateCalendar.get(Calendar.MONTH);
    int dayCurrent = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
    int yearReserve = currentDateCalendar.get(Calendar.YEAR);
    int monthReserve = currentDateCalendar.get(Calendar.MONTH);
    int dayReserve = currentDateCalendar.get(Calendar.DAY_OF_MONTH);

    int hourCurrent = currentDateCalendar.get(Calendar.HOUR_OF_DAY);
    int minuteCurrent = currentDateCalendar.get(Calendar.MINUTE);
    String myMonthSt, myDaySt;
    String getAdminName = "";
    Date reserveDateTime;

    {
        if (monthReserve < 10) currentMonthSt = "0" + (monthReserve + 1);
        else currentMonthSt = "" + (monthReserve + 1);
        if (dayReserve < 10) currentDaySt = "0" + dayReserve;
        else currentDaySt = "" + dayReserve;
        currentYearSt = "" + yearCurrent;

        if (hourCurrent < 10) currentHourSt = "0" + hourCurrent;
        else currentHourSt = "" + hourCurrent;
        if (minuteCurrent < 10) currentMinuteSt = "0" + minuteCurrent;
        else currentMinuteSt = "" + minuteCurrent;
    }
    int durationNewReserve;

    private int getNumReserve, getNumTable, getDurationMinute;
    private String getType = "", getDate, getTime, getClient, getTariff, whoCall;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);


        // getIntent() загружаем данные
        Intent intent = getIntent();
        whoCall = intent.getStringExtra("whoCall");
        Log.i("Gas", "whoCall1 = " + whoCall);
        getNumReserve = intent.getIntExtra("numReserve", -1);
        getNumTable = intent.getIntExtra("numTable", -1);
        getType = intent.getStringExtra("type");
        if (getType == null) getType = "";
        Log.i("Gas5", "getType = " + getType);
        getDate = intent.getStringExtra("date");
        getTime = intent.getStringExtra("time");
        getClient = intent.getStringExtra("client");
        getTariff = intent.getStringExtra("tariff");
        getDurationMinute = intent.getIntExtra("duration", -1);
        getAdminName = intent.getStringExtra("adminName");
        Log.i("Gas4", "getAdminName in newOrder = " + getAdminName);


        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        clientsList = optionallyClass.initClient(this);
        tariffList = optionallyClass.initTariff(this, "tariffList");

        tvPyramid = findViewById(R.id.tvPyramid);
        tvPool = findViewById(R.id.tvPool);

        btnNewReserveTime = findViewById(R.id.btnNewReserveTime);
        btnNewReserveTime.setOnClickListener(this);
        btnNewReserveDate = findViewById(R.id.btnNewReserveDate);
        btnNewReserveDate.setOnClickListener(this);
        btnNewReserveDuration = findViewById(R.id.btnNewReserveDuration);
        btnNewReserveDuration.setOnClickListener(this);
        btnCreateReserve = findViewById(R.id.btnCreateReserve);
        btnCreateReserve.setTextColor(Color.BLACK);
        btnCreateClient = findViewById(R.id.btnCreateClient);
        btnCreateClient.setOnClickListener(this);

        switchTypeTable = findViewById(R.id.switchTypeTable);
        if (switchTypeTable != null) {     // мы еще не нажали на свитч
            if (getType.equals("Американский пул")) {
                tvPool.setTypeface(boldTypeface);
                tvPool.setText("Американский пул");
                tvPyramid.setTypeface(normalTypeface);
                tvPyramid.setText("");
                switchTypeTable.setChecked(false);
            } else if (getType.equals("Русская пирамида")) {
                tvPyramid.setTypeface(boldTypeface);
                tvPyramid.setText("Русская пирамида");
                tvPool.setTypeface(normalTypeface);
                tvPool.setText("");
                switchTypeTable.setChecked(true);
            }
            switchTypeTable.setOnCheckedChangeListener(this);
        }

        actvFreeTable = findViewById(R.id.actvFreeTable);
        actvFreeTable.setShowSoftInputOnFocus(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setOnTouchListener((v, event) -> {
            actvFreeTable.showDropDown();
            return false;
        });
        switchTypeTable = new SwitchCompat(this);
        actvFreeTable.setAdapter(adapter);
        actvFreeTable.setHint("Выберите стол");

        actvClient = findViewById(R.id.actvClient);
        ArrayAdapter<String> adapterClient = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, clientsList);
        actvClient.setThreshold(1);
        actvClient.setAdapter(adapterClient);

        actvTariff = findViewById(R.id.actvTariff);
        actvTariff.setShowSoftInputOnFocus(false);
        ArrayAdapter<String> adapterTariff = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tariffList);
        actvTariff.setOnTouchListener((v, event) -> {
            actvTariff.showDropDown();
            return false;
        });
        actvTariff.setAdapter(adapterTariff);

        // если наша активити была вызвана (изменить резерв)
        checkWhoCallThisActivity();

        initTablesList();  // сразу находим подходящие столы с данными по умолчанию
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNewReserveTime: {
                timePicker();
                break;
            }
            case R.id.btnNewReserveDate: {
                datePicker();
                break;
            }
            case R.id.btnNewReserveDuration: {
                dialogNumberPickerShow();
                break;
            }
            case R.id.btnCreateClient: {
                optionallyClass.openDialogCreateClient(this);

                List<String> clientsList = optionallyClass.initClient(this);
                ArrayAdapter<String> adapterClient = new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, clientsList);
                actvClient.setAdapter(adapterClient);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if (whoCall.equals("editDBActivity")) {
            intent = new Intent(NewOrderActivity.this, EditDBActivity.class);
            // передаем название заголовка
            intent.putExtra("headName", "Резервы");
            startActivity(intent);
            finish();
        } else if (whoCall.equals("commonActivity")) {
            intent = new Intent(NewOrderActivity.this, CommonActivity.class);
            startActivity(intent);
            finish();
        } else /*if (whoCall.equals("tableActivity_Start") || whoCall.equals("tableActivity_Add"))*/ {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
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
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void checkWhoCallThisActivity() {
        btnNewReserveTime.setText(hourReserve + ":" + minuteReserve);
        btnNewReserveDate.setText(currentDaySt + "." + currentMonthSt + "." + yearReserve);
        if (whoCall.equals("editDBActivity_Correct")) { // если вызвали с кнопки "Изменить резерв"
            btnCreateReserve.setText("Изменить  резерв");
            // задаем тип игры по умолчанию
            btnNewReserveDate.setText(getDate);
            btnNewReserveTime.setText(getTime);
            if (getDurationMinute < 60) btnNewReserveDuration.setText(getDurationMinute + " мин");
            else {
                int hour = getDurationMinute / 60;
                int minute = getDurationMinute % 60;
                if (minute == 0) btnNewReserveDuration.setText(hour + " ч ");
                else btnNewReserveDuration.setText(hour + " ч " + minute + " мин");
            }
            actvFreeTable.setText("Стол № " + getNumTable);
            actvClient.setText(getClient);
            actvTariff.setText(getTariff);
        } else if (whoCall.equals("editDBActivity_add")) {
            btnCreateReserve.setText("Создать  резерв");
            actvFreeTable.setText("");
        } else if (whoCall.equals("tableActivity_Start")) {
            actvFreeTable.setText("Стол № " + getNumTable);
            btnNewReserveDate.setText(getDate);
            btnNewReserveTime.setText(getTime);
        } else if (whoCall.equals("tableActivity_Add")) {
            actvFreeTable.setText("Стол № " + getNumTable);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Log.i("Gas", "isChecked first = " + isChecked);
        if (isChecked) {
            tvPyramid.setTypeface(boldTypeface);
            tvPyramid.setText("Русская пирамида");
            tvPool.setTypeface(normalTypeface);
            tvPool.setText("");
            getType = "Русская пирамида";
        } else {
            tvPool.setTypeface(boldTypeface);
            tvPool.setText("Американский пул");
            tvPyramid.setTypeface(normalTypeface);
            tvPyramid.setText("");
            getType = "Американский пул";
        }

        actvFreeTable.setHint("Доступные столы");
        if (whoCall.equals("tableActivity_Start") || whoCall.equals("tableActivity_Add")) {
            actvFreeTable.setText("");
        }
        initTablesList();
    }

    private void initTablesList() {
        // здесь фильтр столов по типу и вызов другого фильтра
        btnNewReserveDate.setTextColor(Color.BLACK);
        btnNewReserveTime.setTextColor(Color.BLACK);
        typeNumTableList.clear();

        Log.i("Gas5", "getType = " + getType);

        // получаем данные c табл "TABLES"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            do {
                if (getType.equals("")) {  // если тип стола не выбран
                    // то создаем список типов столов
//                    Log.i("Gas5", "тип стола не передавали");
//                    Log.i("Gas5", "создаем список типов столов");
                    typeNumTableList.add(cursorTables.getInt(numTableIndex));
                } else if (cursorTables.getString(typeTableIndex).equals(getType)) {
                    // фильтр по типу столов
//                    Log.i("Gas5", "фильтр столов по типу игры проходит");
                    typeNumTableList.add(cursorTables.getInt(numTableIndex));
                }
                numTypeTableMap.put(cursorTables.getInt(numTableIndex), cursorTables.getString(typeTableIndex));

            } while (cursorTables.moveToNext());
            Log.i("Gas4", "typeTableList = " + typeNumTableList);

            // здесь вызываем метод, кот. проведет выборку по времени
            changeFreeTableTime();
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    private void changeFreeTableTime() {
        // здесь фильтр по номеру стола и дате и создание списка сомнительных заказов
        checkList.clear();
        Log.i("Gas", "checkList.clear()");
        finishNumTableList.clear();
        Log.i("Gas", "finishNumTableList.clear()");
        finishList.clear();
        Log.i("Gas", "finishList.clear()");

        // у нас есть все столы одного типа
        // нужно проверить каждый стол на наличие свободного времени для резерва
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
            int orderDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
            int orderTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
            int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
            int tariffIndex = cursorOrders.getColumnIndex(DBHelper.KEY_TARIFF);
            int employeeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_EMPLOYEE);

            Log.i("Gas1", "typeNumTableList = " + typeNumTableList);
            do {
                Log.i("Gas1", "idIndex = " + cursorOrders.getInt(idIndex));
                for (int i = 0; i < typeNumTableList.size(); i++) {
                    // у нас есть изначально 19 столов (если не выбран тип игры) иначе меньше
                    // дальше нам нужно просто вычесть из них уже занятые

                    // если стол м.б. занят этим заказом (номера столов совпадают)
                    if (typeNumTableList.get(i) == cursorOrders.getInt(numTableIndex)) {
                        // если даты совпадают
                        Log.i("Gas1", "   Date btnNewReserveDate = " + btnNewReserveDate.getText().toString());
                        Log.i("Gas1", "   Date reserveDateIndex = " + cursorOrders.getString(reserveDateIndex));
                        if (cursorOrders.getString(reserveDateIndex).equals(btnNewReserveDate.getText().toString())) {

                            Log.i("Gas1", "checkList add");
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
                                    cursorOrders.getString(tariffIndex)));
                        }
                        // если на желаемую дату вообще нет резервов, то это супер
                        else break;
                    }
                }
            } while (cursorOrders.moveToNext());


            Log.i("Gas1", "List checkList: " + checkList);

            checkFreeTimeReserve();

        } else {
            // если в БД нет заказов то просто добавляем заказ
            checkFreeTimeReserve();
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
    }

    private void checkFreeTimeReserve() {
        Log.i("Gas1", " --- // --- ");
        Log.i("Gas1", "Start method: checkFreeTimeReserve");

        // здесь фильтр по времени
        // у нас есть жел. время резерва, например 16:30
        // у нас есть продолжительность жел.резерва
        // мы смотрим по каждому резерву
        // д. удовлетворять нескольким условиям
        // 1. время жел.резерва + продолжительнсть не д. протыкать след. резервы
        // 2. если продолжительность не указана, то просто чтобы вермя желаемого резерва не попадало в сущ. резервы
        // 3 НЕТ(СЛОЖНО). если время тоже не указано, но указана проложительность, то показать все столы доступные с такой прололжительностью


        String newReserveStartTime = btnNewReserveTime.getText().toString();
        Log.i("Gas5", "newReserveStartTime = " + newReserveStartTime);

        if (newReserveStartTime.equals("Выберите время")) {
            newReserveStartTime = hourReserve + ":" + "0" + minuteReserve;
        }
        String[] newReserveStartTimeArr = newReserveStartTime.split(":");

        int hourStartNewReserve = Integer.parseInt(newReserveStartTimeArr[0]);
        int minuteStartNewReserve = Integer.parseInt(newReserveStartTimeArr[1]);
        Log.i("Gas1", "желаем на часов = " + hourStartNewReserve);
        Log.i("Gas1", "на минут = " + minuteStartNewReserve);

        // проверяем выбрана ли продолжительность игры
        if (btnNewReserveDuration.getText().toString().equals("")) durationNewReserve = 0;
        else {
            // получаем и переводим значения в минуты
            String[] newReserveDurationArr = btnNewReserveDuration.getText().toString().split(" ");
            if (newReserveDurationArr.length > 2) { // если у нас "1 ч 30 мин", а не 1 ч
                durationNewReserve = Integer.parseInt(newReserveDurationArr[0]) * 60 + Integer.parseInt(newReserveDurationArr[2]);
            } else {
                if (newReserveDurationArr[1].equals("мин"))
                    durationNewReserve = Integer.parseInt(newReserveDurationArr[0]);
                else durationNewReserve = Integer.parseInt(newReserveDurationArr[0]) * 60;
            }
            Log.i("Gas2", "oldDurationNewReserve = " + durationNewReserve);
            int oldDurationNewReserve = durationNewReserve;

            durationNewReserve = optionallyClass.checkLeftEndWorkMinute(durationNewReserve);

            Log.i("Gas4", "durationNewReserve = " + durationNewReserve);
            if (oldDurationNewReserve != durationNewReserve) {
                btnNewReserveDuration.setText("До конца дня (" + durationNewReserve + " мин)");
                Log.i("Gas2", "btnNewReserveDuration = " + btnNewReserveDuration);
            }
        }


        int hourFinishNewReserve = hourStartNewReserve + (minuteStartNewReserve + durationNewReserve) / 60;
        int minuteFinishNewReserve = (minuteStartNewReserve + durationNewReserve) % 60;
        Log.i("Gas1", "закончим играть в часов = " + hourFinishNewReserve);
        Log.i("Gas1", "в минут = " + minuteFinishNewReserve);

        Log.i("Gas1", "   Size checkList = " + checkList.size() + " (Кол-во заказов на выбранную дату)");
        Log.i("Gas1", "   Start cycle (начинаем цикл по этим заказам)");
        Map<Integer, Integer> numTable_leftMinute_Map = new HashMap<>();
        for (int i = 0; i < checkList.size(); i++) {
            int numCheckTable = typeNumTableList.indexOf(checkList.get(i).getNumTable())/* + 1*/;
            Log.i("Gas1", " i = " + i);
            Log.i("Gas1", "NumTable = " + checkList.get(i).getNumTable());

            int hourStartOldReserve = checkList.get(i).getHourStartReserve();
            Log.i("Gas1", "Old hourStartOldReserve = " + hourStartOldReserve);
            int minuteStartOldReserve = checkList.get(i).getMinuteStartReserve();
            Log.i("Gas1", "Old minuteStartOldReserve = " + minuteStartOldReserve);
            int durationMinuteOldReserve = checkList.get(i).getDuration();
            Log.i("Gas1", "Old durationMinuteOldReserve = " + durationMinuteOldReserve);
            int hourFinishOldReserve = hourStartOldReserve + (minuteStartOldReserve + durationMinuteOldReserve) / 60;
            Log.i("Gas1", "Old hourFinishOldReserve " + hourFinishOldReserve);
            int minuteFinishOldReserve = (minuteStartOldReserve + durationMinuteOldReserve) % 60;
            Log.i("Gas1", "Old minuteFinishOldReserve = " + minuteFinishOldReserve);

            // проверяем удален ли стол сразу
            // если не содержит номера стола (вместо номера стола там -1)
            if (!typeNumTableList.contains(checkList.get(i).getNumTable())) {
                Log.i("Gas1", "Этот стол уже удален!");
                continue;
            }

            // фильтр по времени
            // если время желаемого резерва раньше след.резерва, но не протыкает
            if (((hourFinishNewReserve < hourStartOldReserve) ||
                    (hourFinishNewReserve == hourStartOldReserve) && (minuteFinishNewReserve <= minuteStartOldReserve)) &&
                    (hourStartNewReserve != hourStartOldReserve)) {
                // посчитаем сколько остается минут до след. резерва (на сколько м. зарезервировать)
                int leftMinute = ((hourStartOldReserve - hourFinishNewReserve) * 60) + minuteStartOldReserve - minuteFinishNewReserve;
                Log.i("Gas1", "Осталось минут до Old резерва leftMinute = " + leftMinute);
                Log.i("Gas1", "leftMinute = " + leftMinute);

                Log.i("Gas1", "мой резерв заканчивается раньше, чем начинается старый");
                if (leftMinute < 0) {
                    Log.i("Gas1", "но остается для игры меньше 30 минут, поэтому не подойдет");
                    Log.i("Gas1", "< 30 мин");
                    if (typeNumTableList.contains(checkList.get(i).getNumTable())) {
                        typeNumTableList.set(numCheckTable, -1);
                    }
                } else {
                    // для резерва 30 мин и более
                    Log.i("Gas1", "30 мин и более");
                    if (numTable_leftMinute_Map.containsKey(numCheckTable + 1)) {
                        // если мапа уже содержала в себе индекс этого стола с временем
                        Log.i("Gas1", "было " + numTable_leftMinute_Map.get(numCheckTable + 1));
                        Log.i("Gas1", "стало " + (durationNewReserve + leftMinute));
                        if (numTable_leftMinute_Map.get(numCheckTable + 1) > (durationNewReserve + leftMinute)) {
                            // если значение(время) в мапе было больше чем осталось
                            Log.i("Gas1", "записываем время в мапу");
                            Log.i("Gas1", "key = " + (numCheckTable + 1));
                            Log.i("Gas1", "value = " + (durationNewReserve + leftMinute));
                            numTable_leftMinute_Map.put(numCheckTable + 1, durationNewReserve + leftMinute);
                        }
                    } else {
                        // если мапа НЕ содержала в себе индекс этого стола с временем
                        Log.i("Gas1", "записываем оставшееся время");
                        numTable_leftMinute_Map.put(numCheckTable + 1, durationNewReserve + leftMinute);
                    }
                }
            } else if (((hourStartNewReserve > hourFinishOldReserve) ||
                    ((hourStartNewReserve == hourFinishOldReserve) && (minuteStartNewReserve >= minuteFinishOldReserve))) &&
                    (hourStartNewReserve != hourStartOldReserve)) {
                // если время начала желаемого.рез. после времени окончания проверяемого то тоже все ок
                // тут нам ничего не нужно делать, потому что м.б. проблема с другим резервом
                Log.i("Gas1", "Не мешает! Мой резерв начинается после окончания старого");
            } else {
                // если желаемый.рез проткнул старые резервы
                // или если часы старта желаемого. и старых резервов совпадают
                Log.i("Gas1", "Этот стол уже занят! (проткнул)");
                Log.i("Gas1", "убираем этот стол");
                typeNumTableList.set(numCheckTable, -1);
            }
        }


        Log.i("Gas1", "typeNumTableList.size() = " + typeNumTableList.size());
        Log.i("Gas1", "numTable_leftMinute_Map = " + numTable_leftMinute_Map);
        for (int i = 0; i < typeNumTableList.size(); i++) {
            // если значение typeNumTableList.get(i) = -1, то стол удален
            if (typeNumTableList.get(i) != -1) {
                Log.i("Gas1", "i = " + i);
                Log.i("Gas1", "numTable = " + (i + 1));
                Log.i("Gas1", "typeNumTableList.get(i) = " + typeNumTableList.get(i));

                if (numTable_leftMinute_Map.containsKey(typeNumTableList.get(i))) {
                    int duration = numTable_leftMinute_Map.get(typeNumTableList.get(i));
                    int hourDuration = duration / 60;
                    int minuteDuration = duration % 60;
                    Log.i("Gas1", "duration = " + duration);
                    Log.i("Gas1", "hourDuration = " + hourDuration);
                    Log.i("Gas1", "minuteDuration = " + minuteDuration);
                    // если передано меньше часа
                    if (hourDuration < 1) {
                        Log.i("Gas1", "(hourDuration < 1)  numTable = " + (i + 1));
                        finishList.add("Стол № " + typeNumTableList.get(i) + " (Не более " + minuteDuration + " мин)");
                    } else if (minuteDuration < 1) {
                        Log.i("Gas1", "(minuteDuration < 1)  numTable = " + (i + 1));
                        finishList.add("Стол № " + typeNumTableList.get(i) + " (Не более " + hourDuration + " ч) ");
                    } else {
                        Log.i("Gas1", "(hourDuration >= 1)  numTable = " + (i + 1));
                        finishList.add("Стол № " + typeNumTableList.get(i) + " (Не более " + hourDuration + " ч " + minuteDuration + " мин)");
                    }
                } else {
                    Log.i("Gas1", "(NOT numTable_leftMinute_Map.containsKey(typeNumTableList.get(i)))  numTable = " + (i + 1));
                    finishList.add("Стол № " + typeNumTableList.get(i));
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tariffList);
        actvTariff.setAdapter(adapter1);
    }

    private void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                yearReserve = year;
                monthReserve = monthOfYear;
                dayReserve = dayOfMonth;
                if (monthReserve < 10) myMonthSt = "0" + (monthReserve + 1);
                else myMonthSt = "" + (monthReserve + 1);
                if (dayReserve < 10) myDaySt = "0" + dayReserve;
                else myDaySt = "" + dayReserve;

                // проверяем, чтобы заказ не был оформлен на заднее число
                {
                    // переводим дату и время в формат Date
                    try {
                        // из строки в Date
                        reserveDateTime = dateTimeFormat.parse(myDaySt + "." + myMonthSt + "." + yearReserve + " " + "11:00");
//                        Log.i("Gas1", "reserveDateTime = " + reserveDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Gas1", "btnNewReserveDate.getText().toString() = " + btnNewReserveDate.getText().toString());
                Log.i("Gas1", "reserveDateTime = " + reserveDateTime);
                Log.i("Gas1", "reserveDateTime before currentTime? = " + reserveDateTime.before(new Date()));

                if ((reserveDateTime.before(new Date())) || (hourReserve < 11 && hourReserve > 4)) {
                    // то резерв не м.б. создан
                    Toast.makeText(NewOrderActivity.this, "Дата уже прошла", Toast.LENGTH_SHORT).show();
                    btnNewReserveDate.setTextColor(Color.RED);
                    btnNewReserveDate.setText("Выберите дату");

                    if (getNumTable == -1) {
                        actvFreeTable.setText("");
                    }
                } else {
                    btnNewReserveDate.setText(myDaySt + "." + myMonthSt + "." + yearReserve);
                    // обнуляем значения при изменении даты
                    actvFreeTable.setHint("Доступные столы");

                    // обнуляем значения при изменении даты
                    actvFreeTable.setHint("Доступные столы");
                    if (getNumTable == -1) {
                        actvFreeTable.setText("");
                    }
                    // если вызов был с tableActivity
                    if (whoCall.equals("tableActivity_Add") || whoCall.equals("tableActivity_Start")) {
                        // то нужно сделать так, чтобы номер стола не пропадал
                        // чтобы остальные поля пропадали, если резервы накладываются друг на друга
                        // или забить уйх
//                        ...
                    }

                    initTablesList();  // находим подходящие столы
                }
            }
        }, yearReserve, monthReserve, dayReserve);
        datePickerDialog.show();
    }

    private void timePicker() {
        // Get Current Time
//        hourReserve = currentHour;
//        minuteReserve = 0;

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                hourReserve = hourOfDay;
                minuteReserve = minute;
                if (hourReserve < 10) hourReserveSt = "0" + hourReserve;
                else hourReserveSt = "" + hourReserve;
                if (minuteReserve < 10) minuteReserveSt = "0" + minuteReserve;
                else minuteReserveSt = "" + minuteReserve;

                // проверяем, чтобы заказ не был оформлен на заднее число
                {
                    try {
                        // из строки в Date
                        reserveDateTime = dateTimeFormat.parse(btnNewReserveDate.getText().toString() + " " +
                                hourReserveSt + ":" + minuteReserveSt);
//                        Log.i("Gas1", "reserveDateTime = " + reserveDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Gas1", "btnNewReserveDate.getText().toString() = " + btnNewReserveDate.getText().toString());
                Log.i("Gas1", "btnNewReserveTime = " + hourReserveSt + ":" + minuteReserveSt);
                Log.i("Gas1", "reserveDateTime = " + reserveDateTime);
                Log.i("Gas1", "reserveDateTime before currentTime? = " + reserveDateTime.before(new Date()));

                if ((reserveDateTime.before(new Date())) || (hourReserve < 11 && hourReserve > 4)) {
                    // если резерв оформляется раньше текущего времени или не в рабочие часы, то резерв не м.б. создан
                    btnNewReserveTime.setTextColor(Color.RED);
                    btnNewReserveTime.setText("Выберите время");

                    // обнуляем значения при изменении времени
                    actvFreeTable.setHint("Доступные столы");
                    if (getNumTable == -1) {
                        actvFreeTable.setText("");
                    }
                } else {
                    btnNewReserveTime.setText(hourReserveSt + ":" + minuteReserveSt);

                    // обнуляем значения при изменении времени
                    actvFreeTable.setHint("Доступные столы");
                    if (getNumTable == -1) {
                        actvFreeTable.setText("");
                    }

                    initTablesList();  // находим подходящие столы
                }
            }
        }, hourReserve, minuteReserve, true);
        timePickerDialog.show();

        initTablesList();  // находим подходящие столы
    }

    public void dialogNumberPickerShow() {
        final Dialog dialog = new Dialog(NewOrderActivity.this);
        dialog.setTitle("NumberPicker");
        dialog.setContentView(R.layout.dialog_number_picker);
        Button btnSet = (Button) dialog.findViewById(R.id.btnSet);
        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker);

        numberPicker.setOnValueChangedListener(this);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(durationTimeArr.length - 1);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(durationTimeArr);

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Gas", "Тут будут дейтсвия при выборе продлжительности (нажатии кнопки выбрать)");
                btnNewReserveDuration.setText(durationTimeArr[numberPicker.getValue()]);
                dialog.dismiss();

                actvFreeTable.setHint("Доступные столы");
                if (getNumTable == -1) {
                    actvFreeTable.setText("");
                }
                initTablesList();  // находим подходящие столы
            }
        });
        dialog.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);
    }

    @Override // тут действия при предворительном выборе (прокрутке)
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        // тут действия при предворительном выборе (прокрутке)
        Log.i("Gas", "value is " + newVal);
    }

    public void showDialogAlertCreateReserve(View view) {
        if (actvFreeTable.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбран стол", Toast.LENGTH_SHORT).show();
        } else if (actvClient.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбран клиент", Toast.LENGTH_SHORT).show();
        } else if (actvTariff.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбран тариф", Toast.LENGTH_SHORT).show();
        } else if (btnNewReserveDuration.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбрана продолжительность игры", Toast.LENGTH_SHORT).show();
        } else {
            // вытаскиваем выбранный номер стола
            String[] numTableArr = actvFreeTable.getText().toString().split(" ");
            numTable = Integer.parseInt(numTableArr[2]);
            String typeTable;
            if (numTypeTableMap.get(numTable).equals("Американский пул"))
                typeTable = "Американский пул";
            else typeTable = "Русская пирамида";


            AlertDialog.Builder builderAlert = new AlertDialog.Builder(NewOrderActivity.this);
            builderAlert.setMessage("Стол № " + numTable +
                            "\nТип игры: " + typeTable +
                            "\nДата резерва: " + btnNewReserveDate.getText().toString() +
                            "\nВремя резерва: " + btnNewReserveTime.getText().toString() +
                            "\nПродолжительность: " + btnNewReserveDuration.getText().toString() +
                            "\nКлиент: " + actvClient.getText().toString() +
                            "\nТариф: " + actvTariff.getText().toString())
                    .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                    .setNegativeButton("Отмена", (dialogInterface, i) -> Toast.makeText(getApplicationContext(),
                            "Резерв отменен", Toast.LENGTH_SHORT).show())
                    .setOnCancelListener(dialogInterface -> Toast.makeText(getApplicationContext(),
                            "нажали назад", Toast.LENGTH_SHORT).show());

            if (getNumReserve != -1) { // если передали сюда стол
                actvClient.setText(getClient);
                actvTariff.setText(getTariff);
                builderAlert.setTitle("Изменить резерв?");
                builderAlert.setPositiveButton("Изменить", (dialogInterface, i) -> {
                    getClient = actvClient.getText().toString();
                    getTariff = actvTariff.getText().toString();
                    putReserveInDB();
                    Toast.makeText(getApplicationContext(), "Резерв изменен", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent("editDBActivity");
                    // передаем название заголовка
                    intent.putExtra("headName", "Резервы");
                    startActivity(intent);
                });

            } else {
                builderAlert.setTitle("Создать резерв?");
                builderAlert.setPositiveButton("Создать", (dialogInterface, i) -> {
                    getClient = actvClient.getText().toString();
                    getTariff = actvTariff.getText().toString();
                    putReserveInDB();
                    Toast.makeText(getApplicationContext(), "Резерв создан", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent("commonActivity");
                    startActivity(intent);
                });
            }
            // в зависимости от выбранного стола выбираем тип игры
            if (numTypeTableMap.get(numTable).equals("Американский пул"))
                builderAlert.setIcon(R.drawable.bol_pool1);
            else builderAlert.setIcon(R.drawable.bol_pyramide1);

            AlertDialog alertDialog = builderAlert.create();
            alertDialog.show();
        }
    }

    private void putReserveInDB() {
        // если хотим изменить запись, то просто передает тудатот же номер строки
        if (getNumReserve != -1) {
            database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + " = " + getNumReserve, null);
            contentValues.put(DBHelper.KEY_ID, getNumReserve);
        }
        contentValues.put(DBHelper.KEY_NUM_TABLE, numTable);
        contentValues.put(DBHelper.KEY_RESERVE_DATE, btnNewReserveDate.getText().toString());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, btnNewReserveTime.getText().toString());
        contentValues.put(DBHelper.KEY_DURATION, durationNewReserve);
        contentValues.put(DBHelper.KEY_CLIENT, getClient);
        contentValues.put(DBHelper.KEY_EMPLOYEE, getAdminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, currentDaySt + "." + currentMonthSt + "." + currentYearSt);
        contentValues.put(DBHelper.KEY_ORDER_TIME, currentHourSt + ":" + currentMinuteSt);
        contentValues.put(DBHelper.KEY_TARIFF, getTariff);
        contentValues.put(DBHelper.KEY_STATUS, "");

        database.insert(DBHelper.ORDERS, null, contentValues);
    }
}