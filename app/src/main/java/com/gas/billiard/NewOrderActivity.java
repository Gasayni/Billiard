package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private Map<Integer, String> numTypeTableMap = new HashMap<>();
    private List<Integer> typeNumTableList = new ArrayList<>();
    private List<Integer> finishNumTableList = new ArrayList<>();
    private List<String> finishList = new ArrayList<>();
    //    private List<Integer> falseNumTableList = new ArrayList<>();
    private List<ReserveTable> checkList = new ArrayList<>();
    SwitchCompat switchTypeTable;
    TextView tvPyramid, tvPool;
    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
    AutoCompleteTextView actvFreeTable;
    Button btnNewReserveTime, btnNewReserveDate, btnNewReserveDuration, btnCreateReserve;

    String[] durationTimeArr = {"30 мин", "1 ч", "1 ч 30 мин", "2 ч", "2 ч 30 мин",
            "3 ч", "3 ч 30 мин", "4 ч", "4 ч 30 мин", "5 ч", "5 ч 30 мин", "6 ч"};

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;

    // задаем начальное значение для выбора времени (не важно какие)
    int hourReserve = 11;
    int minuteReserve = 0;
    // задаем начальное значение для выбора даты
    int yearReserve = 2022;
    int monthReserve = 8;
    int dayReserve;

    final Calendar currentDateCalendar = Calendar.getInstance();
    String currentMonthSt, currentDaySt;

    {
        // Get Current Date
        yearReserve = currentDateCalendar.get(Calendar.YEAR);
        monthReserve = currentDateCalendar.get(Calendar.MONTH);
        dayReserve = currentDateCalendar.get(Calendar.DAY_OF_MONTH);

        if (monthReserve < 10) currentMonthSt = "0" + (monthReserve + 1);
        else currentMonthSt = "" + (monthReserve + 1);
        if (dayReserve < 10) currentDaySt = "0" + dayReserve;
        else currentDaySt = "" + dayReserve;
    }

    String choseTypeTable = "";
    int durationNewReserve;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        tvPyramid = findViewById(R.id.tvPyramid);
        tvPool = findViewById(R.id.tvPool);

        btnNewReserveTime = findViewById(R.id.btnNewReserveTime);
        btnNewReserveTime.setText(hourReserve + ":0" + minuteReserve);
        btnNewReserveTime.setOnClickListener(this);
        btnNewReserveDate = findViewById(R.id.btnNewReserveDate);
        btnNewReserveDate.setText(currentDaySt + "." + currentMonthSt + "." + yearReserve);
        btnNewReserveDate.setOnClickListener(this);
        btnNewReserveDuration = findViewById(R.id.btnNewReserveDuration);
        btnNewReserveDuration.setOnClickListener(this);

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
        actvFreeTable.setHint("Выберите тип стола");

        initTablesList();  // сразу находим подходящие столы с данными по умолчанию

//        btnCreateReserve = findViewById(R.id.btnCreateReserve);
//        btnCreateReserve.setOnClickListener(this);
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
            }
//            case R.id.btnCreateReserve: {
//                showDialogAlert();
//                break;
//            }
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
            choseTypeTable = "pyramid";

        } else {
            tvPool.setTypeface(boldTypeface);
            tvPool.setText("Американский пул");
            tvPyramid.setTypeface(normalTypeface);
            tvPyramid.setText("");
            choseTypeTable = "pool";
        }


        actvFreeTable.setHint("Доступные столы");
        actvFreeTable.setText("");

        initTablesList();  // находим подходящие столы
        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishFreeReserveNumTableList);
        actvFreeTable.setAdapter(adapter);*/
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
            int orderDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
            int orderTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
            int rateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RATE);
            int descriptionIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DESCRIPTION);

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
        int hourStartTimeNewReserve = Integer.parseInt(newReserveStartTimeArr[0]);
        int minuteStartTimeNewReserve = Integer.parseInt(newReserveStartTimeArr[1]);
        Log.i("Gas1", "желаем на часов = " + hourStartTimeNewReserve);
        Log.i("Gas1", "на минут = " + minuteStartTimeNewReserve);

        // проверяем выбрана ли продолжительность игры
//        int durationNewReserve;
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


        int hourFinishTimeNewReserve = hourStartTimeNewReserve + (minuteStartTimeNewReserve + durationNewReserve) / 60;
        int minuteFinishNewReserve = (minuteStartTimeNewReserve + durationNewReserve) % 60;
        Log.i("Gas1", "закончим играть в часов = " + hourFinishTimeNewReserve);
        Log.i("Gas1", "в минут = " + minuteFinishNewReserve);

        Log.i("Gas1", "   Size checkList = " + checkList.size() + " (Кол-во заказов на выбранную дату)");
        Log.i("Gas1", "   Start cycle (начинаем цикл по этим заказам)");
        Map<Integer, Integer> numTable_leftMinute_Map = new HashMap<>();
        for (int i = 0; i < checkList.size(); i++) {
            int numCheckTable = typeNumTableList.indexOf(checkList.get(i).getNumTable());
            Log.i("Gas1", " i = " + i);
            Log.i("Gas1", "NumTable = " + checkList.get(i).getNumTable());

            int hourStartTimeOldReserve = checkList.get(i).getHour();
            Log.i("Gas1", "Old hourStartTimeOldReserve = " + hourStartTimeOldReserve);
            int minuteStartTimeOldReserve = checkList.get(i).getMinute();
            Log.i("Gas1", "Old minuteStartTimeOldReserve = " + minuteStartTimeOldReserve);
            int durationMinuteOldReserve = checkList.get(i).getDuration();
            Log.i("Gas1", "Old durationMinuteOldReserve = " + durationMinuteOldReserve);
            int hourFinishOldReserve = hourStartTimeOldReserve + (minuteStartTimeOldReserve + durationMinuteOldReserve) / 60;
            Log.i("Gas1", "Old hourFinishOldReserve " + hourFinishOldReserve);
            int minuteFinishOldReserve = (minuteStartTimeOldReserve + durationMinuteOldReserve) % 60;
            Log.i("Gas1", "Old minuteFinishOldReserve = " + minuteFinishOldReserve);


            // посчитаем сколько остается минут до след. резерва (на сколько м. зарезервировать)
            int leftMinute = ((hourStartTimeOldReserve - hourFinishTimeNewReserve) * 60) + minuteStartTimeOldReserve - minuteFinishNewReserve;
            Log.i("Gas1", "Осталось минут до Old резерва leftMinute = " + leftMinute);

            Log.i("Gas1", "leftMinute = " + leftMinute);

            // проверяем удален ли стол сразу
            // если не содержит номера стола (вместо номера стола там -1)
            if (!typeNumTableList.contains(checkList.get(i).getNumTable())) {
                Log.i("Gas1", "Этот стол уже удален!");
                continue;
            }

            // фильтр по времени
            // если время желаемого резерва раньше след.резерва, но не протыкает
            if ((hourStartTimeNewReserve < hourStartTimeOldReserve) && (hourFinishTimeNewReserve <= hourStartTimeOldReserve)) {
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
                    if (numTable_leftMinute_Map.containsKey(numCheckTable)) {
                        // если мапа уже содержала в себе индекс этого стола с временем
                        Log.i("Gas1", "было " + numTable_leftMinute_Map.get(numCheckTable));
                        Log.i("Gas1", "стало " + (durationNewReserve + leftMinute));
                        if (numTable_leftMinute_Map.get(numCheckTable) > (durationNewReserve + leftMinute)) {
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
            } else if (hourFinishTimeNewReserve < hourStartTimeOldReserve) {
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
                    finishList.add("Стол № " + typeNumTableList.get(i) + " (До конца дня)");
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);
    }

    private void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        yearReserve = year;
                        monthReserve = monthOfYear;
                        dayReserve = dayOfMonth;
                        String myMonthSt, myDaySt;
                        if (monthReserve < 10) myMonthSt = "0" + (monthReserve + 1);
                        else myMonthSt = "" + (monthReserve + 1);
                        if (dayReserve < 10) myDaySt = "0" + dayReserve;
                        else myDaySt = "" + dayReserve;

                        btnNewReserveDate.setText(myDaySt + "." + myMonthSt + "." + yearReserve);

                        // обнуляем значения при изменении даты
                        actvFreeTable.setHint("Доступные столы");
                        actvFreeTable.setText("");

                        initTablesList();  // находим подходящие столы
                    }
                }, yearReserve, monthReserve, dayReserve);
        datePickerDialog.show();

//        initTablesList();  // находим подходящие столы

        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);*/
    }

    private void timePicker() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        hourReserve = c.get(Calendar.HOUR_OF_DAY);
//        minuteReserve = c.get(Calendar.MINUTE);
        minuteReserve = 0;

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        hourReserve = hourOfDay;
                        minuteReserve = minute;
                        String hourReserveSt = "" + hourOfDay;
                        String minuteReserveSt = "" + minute;
                        if (hourOfDay < 10) hourReserveSt = "0" + hourOfDay;
                        if (minute < 10) minuteReserveSt = "0" + minute;

                        btnNewReserveTime.setText(hourReserveSt + ":" + minuteReserveSt);

                        // обнуляем значения при изменении времени
                        actvFreeTable.setHint("Доступные столы");
                        actvFreeTable.setText("");

                        initTablesList();  // находим подходящие столы
                    }
                }, hourReserve, minuteReserve, true);
        timePickerDialog.show();

        initTablesList();  // находим подходящие столы

        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);*/
    }

    public void dialogShow() {
        final Dialog dialog = new Dialog(NewOrderActivity.this);
        dialog.setTitle("NumberPicker");
        dialog.setContentView(R.layout.activity_dialog);
        Button btnSet = (Button) dialog.findViewById(R.id.btnSet);
        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker1);

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
                actvFreeTable.setText("");

                initTablesList();  // находим подходящие столы
            }
        });
        dialog.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, finishList);
        actvFreeTable.setAdapter(adapter);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        // тут действия при предворительном выборе (прокрутке)
        Log.i("Gas", "value is " + newVal);
    }

    public void showDialogAlert(View view) {
        // вытаскиваем выбранный номер стола
        String[] numTableArr = actvFreeTable.getText().toString().split(" ");
        int numTable = Integer.parseInt(numTableArr[2]);

        AlertDialog.Builder builder = new AlertDialog.Builder(NewOrderActivity.this);
        builder.setTitle("Создать резерв ?")
                .setMessage("Номер стола, Тип игры, дата, время, продолжительность")
                .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Резерв отменен", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Резерв создан", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(getApplicationContext(), "нажали назад", Toast.LENGTH_SHORT).show();
                    }
                });
        // в зависимости от выбранного стола выбираем тип игры

        if (numTypeTableMap.get(numTable).equals("pool")) {
            builder.setIcon(R.drawable.bol_pool1);
        } else {
            builder.setIcon(R.drawable.bol_pyramide1);
        }
        AlertDialog alert = builder.create();
        alert.show();
    }
}