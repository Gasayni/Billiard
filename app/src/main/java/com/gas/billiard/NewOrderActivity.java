package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    OptionallyClass optionallyClass = new OptionallyClass();
    private final Map<Integer, String> numTypeTableMap = new HashMap<>();
    private final List<Integer> typeNumTableList = new ArrayList<>();
    private List<Integer> finishNumTableList = new ArrayList<>();
    private final List<String> finishList = new ArrayList<>();
    private final List<ReserveTable> checkList = new ArrayList<>();
    List<String> clientsList = new ArrayList<>();
    SwitchCompat switchTypeTable;
    TextView tvPyramid, tvPool;
    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
    AutoCompleteTextView actvFreeTable, actvClient;
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
    String currentMonthSt, currentDaySt, hourReserveSt, minuteReserveSt;
    // задаем начальное значение для выбора времени (не важно какие)
    int hourReserve = 11;
    int minuteReserve = 0;
    // Get Current Date
    int yearReserve = currentDateCalendar.get(Calendar.YEAR);
    int monthReserve = currentDateCalendar.get(Calendar.MONTH);
    int dayReserve = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
    String myMonthSt, myDaySt;
    String btnNewReserveDateSt, btnNewReserveTimeSt;
    String adminName = "";

    {
        if (monthReserve < 10) currentMonthSt = "0" + (monthReserve + 1);
        else currentMonthSt = "" + (monthReserve + 1);
        if (dayReserve < 10) currentDaySt = "0" + dayReserve;
        else currentDaySt = "" + dayReserve;
    }

    String choseTypeTable = "";
    int durationNewReserve;

    private int numReserveDB, numTableDB, durationMinuteDB;
    private String typeDB, dateDB, timeDB, clientDB, firstNameNewClient, secondNameNewClient, phoneNewClient;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        initСlient();

        tvPyramid = findViewById(R.id.tvPyramid);
        tvPool = findViewById(R.id.tvPool);

        btnNewReserveTime = findViewById(R.id.btnNewReserveTime);
        btnNewReserveTime.setOnClickListener(this);
        btnNewReserveDate = findViewById(R.id.btnNewReserveDate);
        btnNewReserveDate.setOnClickListener(this);
        btnNewReserveDuration = findViewById(R.id.btnNewReserveDuration);
        btnNewReserveDuration.setOnClickListener(this);
        btnCreateReserve = findViewById(R.id.btnCreateReserve);
        btnCreateClient = findViewById(R.id.btnCreateClient);
        btnCreateClient.setOnClickListener(this);

        switchTypeTable = findViewById(R.id.switchTypeTable);
        if (switchTypeTable != null) {
            Log.i("Gas", "мы еще не нажали на свитч");
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
        actvFreeTable.setAdapter(adapter);
        actvFreeTable.setHint("Выберите стол");

        actvClient = findViewById(R.id.actvClient);
        ArrayAdapter<String> adapterClient = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, clientsList);
        actvClient.setThreshold(1);
        actvClient.setAdapter(adapterClient);

        // загружаем данные с
        Intent intent = getIntent();
        numReserveDB = intent.getIntExtra("numReserve", -1);
        numTableDB = intent.getIntExtra("numTable", -1);
        typeDB = intent.getStringExtra("type");
        dateDB = intent.getStringExtra("date");
        timeDB = intent.getStringExtra("time");
        clientDB = intent.getStringExtra("client");
        durationMinuteDB = intent.getIntExtra("duration", -1);
        clientDB = intent.getStringExtra("client");

        adminName = intent.getStringExtra("adminName");

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
                dialogShow();
                break;
            }
            case R.id.btnCreateClient: {
                openDialogCreateClient();
                break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void checkWhoCallThisActivity() {
        if (numReserveDB != -1) { // если вызвали с кнопки "ИЗменить"
            btnCreateReserve.setText("Изменить  резерв");
            // задаем тип игры по умолчанию
            btnNewReserveDate.setText(dateDB);
            btnNewReserveTime.setText(timeDB);
            if (durationMinuteDB < 60) btnNewReserveDuration.setText(durationMinuteDB + " мин");
            else {
                int hour = durationMinuteDB / 60;
                int minute = durationMinuteDB % 60;
                if (minute == 0) btnNewReserveDuration.setText(hour + " ч ");
                else btnNewReserveDuration.setText(hour + " ч " + minute + " мин");
            }
            actvFreeTable.setText("Стол № " + numTableDB);
            choseTypeTable = typeDB;
            actvClient.setText(clientDB);
        } else {
            btnCreateReserve.setText("Создать  резерв");
            btnNewReserveDate.setText(currentDaySt + "." + currentMonthSt + "." + yearReserve);
            btnNewReserveTime.setText(hourReserve + ":" + "0" + minuteReserve);
            actvFreeTable.setText("");

        }
        Log.i("Gas5", "btnNewReserveDateSt: " + btnNewReserveDateSt);
        Log.i("Gas5", "btnNewReserveTimeSt: " + btnNewReserveTimeSt);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Log.i("Gas", "isChecked first = " + isChecked);
        if (isChecked) {
            tvPyramid.setTypeface(boldTypeface);
            tvPyramid.setText("Русская пирамида");
            tvPool.setTypeface(normalTypeface);
            tvPool.setText("");
            choseTypeTable = "pyramid";

        } else {
            tvPool.setTypeface(boldTypeface);
            tvPool.setText("Американский пул");
            tvPyramid.setTypeface(normalTypeface);
            tvPyramid.setText("");
            choseTypeTable = "pool";
        }


        actvFreeTable.setHint("Доступные столы");
        if (numTableDB == -1) {
            actvFreeTable.setText("");
        }

        initTablesList();
    }

    private void initTablesList() {
        typeNumTableList.clear();

        // получаем данные c табл "TABLES"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            do {
                if (choseTypeTable.equals("")) {  // если стол не выбран
                    typeNumTableList.add(cursorTables.getInt(numTableIndex));
                } else if (cursorTables.getString(typeTableIndex).equals(choseTypeTable)) {
                    // фильтр по типу столов
                    Log.i("Gas", "фильтр столов по типу игры проходит");
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

    private void initСlient() {
        // получаем данные c табл "EMPLOYEES"
        cursorClients = database.query(DBHelper.CLIENTS,
                null, null, null,
                null, null, null);
        if (cursorClients.moveToFirst()) {
            int firstNameIndex = cursorClients.getColumnIndex(DBHelper.KEY_FIRST_NAME);
            int secondNameIndex = cursorClients.getColumnIndex(DBHelper.KEY_SECOND_NAME);
            int phoneIndex = cursorClients.getColumnIndex(DBHelper.KEY_PHONE);
            int ordersCountIndex = cursorClients.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
            int spentIndex = cursorClients.getColumnIndex(DBHelper.KEY_SPENT);
            int ratingIndex = cursorClients.getColumnIndex(DBHelper.KEY_RATING);
            int descriptionIndex = cursorClients.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // находим всех клиентов из бд
                clientsList.add(cursorClients.getString(secondNameIndex) + " "
                        + cursorClients.getString(firstNameIndex));
            } while (cursorClients.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
    }

    private void changeFreeTableTime() {
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
            int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
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
                                    cursorOrders.getString(clientIndex),
                                    cursorOrders.getString(employeeIndex)));
                        }
                        // если на желаемую дату вообще нет резервов, то это супер
                        else break;
                    }
                }
            } while (cursorOrders.moveToNext());


            Log.i("Gas1", "List checkList: " + checkList);

            checkFreeTimeReserve();

        } else {
            // если в БД нет заказов
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
            Log.i("Gas1", "мы будем играть минут = " + durationNewReserve);
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

            int hourStartOldReserve = checkList.get(i).getHour();
            Log.i("Gas1", "Old hourStartOldReserve = " + hourStartOldReserve);
            int minuteStartOldReserve = checkList.get(i).getMinute();
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


                btnNewReserveDate.setText(myDaySt + "." + myMonthSt + "." + yearReserve);

                // обнуляем значения при изменении даты
                actvFreeTable.setHint("Доступные столы");
                if (numTableDB == -1) {
                    actvFreeTable.setText("");
                }

                initTablesList();  // находим подходящие столы
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

                btnNewReserveTime.setText(hourReserveSt + ":" + minuteReserveSt);

                // обнуляем значения при изменении времени
                actvFreeTable.setHint("Доступные столы");
                if (numTableDB == -1) {
                    actvFreeTable.setText("");
                }

                initTablesList();  // находим подходящие столы
            }
        }, hourReserve, minuteReserve, true);
        timePickerDialog.show();

        initTablesList();  // находим подходящие столы
    }

    public void dialogShow() {
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
                if (numTableDB == -1) {
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
        } else {
            // вытаскиваем выбранный номер стола
            String[] numTableArr = actvFreeTable.getText().toString().split(" ");
            numTable = Integer.parseInt(numTableArr[2]);
            String typeTable;
            if (numTypeTableMap.get(numTable).equals("pool")) typeTable = "Американский пул";
            else typeTable = "Русская пирамида";


            AlertDialog.Builder builderAlert = new AlertDialog.Builder(NewOrderActivity.this);
            builderAlert.setMessage("Стол № " + numTable +
                            "\nТип игры: " + typeTable +
                            "\nДата резерва: " + btnNewReserveDate.getText().toString() +
                            "\nВремя резерва: " + btnNewReserveTime.getText().toString() +
                            "\nПродолжительность: " + btnNewReserveDuration.getText().toString())
                    .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                    .setNegativeButton("Отмена", (dialogInterface, i) -> Toast.makeText(getApplicationContext(),
                            "Резерв отменен", Toast.LENGTH_SHORT).show())
                    .setOnCancelListener(dialogInterface -> Toast.makeText(getApplicationContext(),
                            "нажали назад", Toast.LENGTH_SHORT).show());

            if (numReserveDB != -1) {
                builderAlert.setTitle("Изменить резерв?");
                builderAlert.setPositiveButton("Изменить", (dialogInterface, i) -> {
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
                    putReserveInDB();
                    Toast.makeText(getApplicationContext(), "Резерв создан", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent("commonActivity");
                    startActivity(intent);
                });
            }
            // в зависимости от выбранного стола выбираем тип игры
            if (numTypeTableMap.get(numTable).equals("pool"))
                builderAlert.setIcon(R.drawable.bol_pool1);
            else builderAlert.setIcon(R.drawable.bol_pyramide1);

            AlertDialog alertDialog = builderAlert.create();
            alertDialog.show();
        }
    }

    private void openDialogCreateClient() {
        LayoutInflater inflater = LayoutInflater.from(NewOrderActivity.this);
        View subView = inflater.inflate(R.layout.dialog_create_client, null);
        final EditText etFirstName = (EditText) subView.findViewById(R.id.etFirstName);
        final EditText etSecondName = (EditText) subView.findViewById(R.id.etSecondName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавление клиента\n")
                .setMessage("Введите данные нового клиента")
                .setView(subView)
                .setPositiveButton("Добавть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firstNameNewClient = etFirstName.getText().toString();
                        secondNameNewClient = etSecondName.getText().toString();
                        phoneNewClient = etPhone.getText().toString();

                        if (firstNameNewClient.equals("")) {
                            Toast.makeText(NewOrderActivity.this, "Введите имя клиента", Toast.LENGTH_SHORT).show();
                            etFirstName.setHintTextColor(Color.RED);
                        } else {
                            putClientInDB();
                            Toast.makeText(NewOrderActivity.this, "Клиент добавлен", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(NewOrderActivity.this, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void putClientInDB() {
        contentValues.put(DBHelper.KEY_FIRST_NAME, firstNameNewClient);
        contentValues.put(DBHelper.KEY_SECOND_NAME, secondNameNewClient);
        contentValues.put(DBHelper.KEY_PHONE, phoneNewClient);

        database.insert(DBHelper.CLIENTS, null, contentValues);

        initСlient();
        ArrayAdapter<String> adapterClient = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, clientsList);
        actvClient.setAdapter(adapterClient);
    }

    private void putReserveInDB() {
        // если хотим изменить запись, то просто передает тудатот же номер строки
        if (numReserveDB != -1) {
            database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + numReserveDB, null);
            contentValues.put(DBHelper.KEY_ID, numReserveDB);
        }
        contentValues.put(DBHelper.KEY_NUM_TABLE, numTable);
        contentValues.put(DBHelper.KEY_RESERVE_DATE, btnNewReserveDate.getText().toString());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, btnNewReserveTime.getText().toString());
        contentValues.put(DBHelper.KEY_DURATION, durationNewReserve);
        contentValues.put(DBHelper.KEY_CLIENT, clientDB);
        contentValues.put(DBHelper.KEY_EMPLOYEE, adminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, btnNewReserveDate.getText().toString());
        contentValues.put(DBHelper.KEY_ORDER_TIME, btnNewReserveTime.getText().toString());
        contentValues.put(DBHelper.KEY_TARIFF, 5);
        contentValues.put(DBHelper.KEY_DESCRIPTION, "Тестовый_01");

        database.insert(DBHelper.ORDERS, null, contentValues);
    }
}