package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);

    OptionallyClass optionalClass = new OptionallyClass();
    // определим, сколько заказов есть на этот день
    List<List<OrderClass>> allOrdersList = optionalClass.findAllOrders(this, false);
    // теперь мы можем работать с каждым заказом без обращения к БД
    List<ClientClass> allClientsList = optionalClass.findAllClients(this, false);
    List<TableClass> allTablesList = optionalClass.findAllTables(this, false);

    private final Map<Integer, String> numTypeTableMap = new HashMap<>();
    private final List<Integer> typeNumTableList = new ArrayList<>();
    private List<Integer> finishNumTableList = new ArrayList<>();
    List<String> finishList = new ArrayList<>();
    List<OrderClass> checkList = new ArrayList<>();
    List<String> clientsList = new ArrayList<>();
    SwitchCompat switchTypeTable;
    TextView tvPyramid, tvPool;
    AutoCompleteTextView actvFreeTable, actvClient;
    Button btnNewReserveTime, btnNewReserveDate, btnNewReserveDuration, btnCreateReserve, btnCreateClient, btnBron;
    int numTable = -1;

    String[] durationTimeArr = {"1 ч", "1 ч 30 мин", "2 ч", "2 ч 30 мин", "3 ч", "3 ч 30 мин",
            "4 ч", "4 ч 30 мин", "5 ч", "5 ч 30 мин", "6 ч"};

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders, cursorClients;

    final Calendar currentDateCalendar = Calendar.getInstance();
    String currentHourSt, currentMinuteSt, currentYearSt, currentMonthSt, currentDaySt, hourReserveSt, minuteReserveSt;
    // задаем начальное значение для выбора времени (не важно какие)
    int hourReserve = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int minuteReserve = Calendar.getInstance().get(Calendar.MINUTE);
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
    Date reserveDateTime;
    String getAdminName;

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

    private int getNumOrder, getNumTable, getDurationMinute;
    private String getType = "", getDate, getTime, getDateOrder, getTimeOrder, getTimeEndReserve, getClient, getBron, getStatus, whoCall, whatIsBtn;
    boolean choseClientFlag = false;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("NewOrderActivity", "\n ...//...    onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        // getIntent() загружаем данные
        Intent intent = getIntent();
        whoCall = intent.getStringExtra("whoCall");
        whatIsBtn = intent.getStringExtra("whatIsBtn");
        if (whatIsBtn == null) whatIsBtn = "";
        Log.i("Gas", "whoCall1 = " + whoCall);
        getNumOrder = intent.getIntExtra("numReserve", -1);
        getNumTable = intent.getIntExtra("numTable", -1);
        getType = intent.getStringExtra("type");
        if (getType == null) getType = "";
        Log.i("Gas5", "getType = " + getType);
        getDate = intent.getStringExtra("date");
        getTime = intent.getStringExtra("time");
        getDateOrder = intent.getStringExtra("dateOrder");
        getTimeOrder = intent.getStringExtra("timeOrder");
        getTimeEndReserve = intent.getStringExtra("timeEndReserve");
        getClient = intent.getStringExtra("client");
        getBron = intent.getStringExtra("bron");
        getStatus = intent.getStringExtra("status");
        if (getBron == null) getBron = "";
        getDurationMinute = intent.getIntExtra("duration", -1);
        getAdminName = intent.getStringExtra("adminName");
        Log.i("Gas4", "getAdminName in newOrder = " + getAdminName);

        tvPyramid = findViewById(R.id.tvPyramid);
        tvPool = findViewById(R.id.tvPool);

        btnNewReserveTime = findViewById(R.id.btnNewReserveTime);
        btnNewReserveTime.setOnClickListener(this);
        btnNewReserveDate = findViewById(R.id.btnNewReserveDate);
        btnNewReserveDate.setOnClickListener(this);
        btnNewReserveDuration = findViewById(R.id.btnNewReserveDuration);
        btnNewReserveDuration.setOnClickListener(this);
        btnNewReserveDuration.setText("4 ч");
        btnCreateReserve = findViewById(R.id.btnCreateReserve);
        btnCreateReserve.setOnClickListener(this);
        btnCreateReserve.setTextColor(Color.BLACK);
        btnCreateClient = findViewById(R.id.btnCreateClient);
        btnCreateClient.setOnClickListener(this);
        btnBron = findViewById(R.id.btnBron);
        btnBron.setOnClickListener(this);

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
        actvClient.setThreshold(1);
        initClients();
        actvClient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
                choseClientFlag = true;
                Log.i("NewOrderActivity", "choseClientFlag = " + choseClientFlag);
            }
        });
        actvClient.setClickable(true);
        actvClient.setOnClickListener(this);



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
                openDialogCreateClient();
                break;
            }
            case R.id.btnBron: {
                if (btnBron.getText().toString().equals("Бронь")) {
                    btnBron.setText("Без брони");
                    btnBron.setBackgroundResource(R.drawable.btn_style_6);
                } else {
                    btnBron.setText("Бронь");
                    btnBron.setBackgroundResource(R.drawable.btn_style_1);
                }
                break;
            }
            case R.id.btnCreateReserve: {
                showDialogAlertCreateReserve();
                break;
            }
            case R.id.actvClient: {
                choseClientFlag = false;
                Log.i("NewOrderActivity", "choseClientFlag = " + choseClientFlag);
                break;
            }
        }
    }

    public void openDialogCreateClient() {
        LayoutInflater inflater = LayoutInflater.from(NewOrderActivity.this);
        View subView = inflater.inflate(R.layout.dialog_create_client_or_employee, null);
        final EditText etName = (EditText) subView.findViewById(R.id.etName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);
        final TextView tvOrdersCount = (TextView) subView.findViewById(R.id.tvOrdersCount);
        final TextView tvDurationSumMinute = (TextView) subView.findViewById(R.id.tvDurationSumMinute);
        final EditText etOrdersCount = (EditText) subView.findViewById(R.id.etOrdersCount);
        final EditText etDurationSumMinute = (EditText) subView.findViewById(R.id.etDurationSumMinute);
        tvOrdersCount.setVisibility(View.GONE);
        tvDurationSumMinute.setVisibility(View.GONE);
        etOrdersCount.setVisibility(View.GONE);
        etDurationSumMinute.setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(NewOrderActivity.this);
        builder.setTitle("Добавление клиента\n")
                .setMessage("Введите данные нового клиента")
                .setView(subView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    final String nameNewClient = etName.getText().toString();
                    final String phoneNewClient = etPhone.getText().toString();

                    if (nameNewClient.equals("")) {
                        Toast.makeText(NewOrderActivity.this, "Введите имя клиента", Toast.LENGTH_SHORT).show();
                        etName.setHintTextColor(Color.RED);
                    } else {
                        ClientClass newClient = new ClientClass(0, nameNewClient, phoneNewClient, 0, 0);
                        optionalClass.putClientInDB(NewOrderActivity.this, newClient);
                        Toast.makeText(NewOrderActivity.this, "Клиент добавлен", Toast.LENGTH_SHORT).show();

                        allClientsList = optionalClass.findAllClients(NewOrderActivity.this, true);
                        initClients();
                        actvClient.setText(nameNewClient);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> Toast.makeText(NewOrderActivity.this, "Отмена", Toast.LENGTH_SHORT).show());
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if (whoCall.equals("editFromEditDBActivity")) {
            intent = new Intent(NewOrderActivity.this, EditDBActivity.class);
            // передаем название заголовка
            intent.putExtra("headName", "Резервы");
            intent.putExtra("adminName", getAdminName);
            startActivity(intent);
            finish();
        } else if (whoCall.equals("editFromTableActivity")) {
            intent = new Intent("tableActivity");
            intent.putExtra("whoCall", "btnCommon");
            intent.putExtra("numTable", getNumTable);
            intent.putExtra("id", getNumOrder);
            intent.putExtra("adminName", getAdminName);
            intent.putExtra("client", getClient);
            intent.putExtra("duration", getDurationMinute);
            intent.putExtra("bron", getBron);
            intent.putExtra("reserveDateStr", getDate);
            intent.putExtra("reserveStartTimeStr", getTime);
            intent.putExtra("dateOrder", getDateOrder);
            intent.putExtra("timeOrder", getTimeOrder);
            intent.putExtra("reserveFinishTimeStr",  getTimeEndReserve);
            startActivity(intent);
        } else if (whoCall.equals("commonActivity")) {
            intent = new Intent(NewOrderActivity.this, CommonActivity.class);
            intent.putExtra("adminName", getAdminName);
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

    @SuppressLint("SetTextI18n")
    private void checkWhoCallThisActivity() {
        btnNewReserveTime.setText(hourReserve + ":" + minuteReserve);
        btnNewReserveDate.setText(currentDaySt + "." + currentMonthSt + "." + yearReserve);
        // если вызвали с кнопки "Изменить резерв"
        if (whoCall.equals("editFromEditDBActivity") || whoCall.equals("editFromTableActivity")) {
            btnCreateReserve.setText("Изменить  резерв");
            choseClientFlag = true;
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
            btnBron.setText(getBron);
            if (getBron.equals("C бронью")) btnBron.setBackgroundResource(R.drawable.btn_style_1);
            else btnBron.setBackgroundResource(R.drawable.btn_style_6);
            numTypeTableMap.put(getNumTable, getType);
        } else if (whoCall.equals("editDBActivity_add")) {
            btnCreateReserve.setText("Создать  резерв");
            actvFreeTable.setText("");
            btnBron.setText("Бронь");
            btnBron.setBackgroundResource(R.drawable.btn_style_1);
        } else if (whoCall.equals("tableActivity_Start")) {
//            hourReserve = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//            minuteReserve = Calendar.getInstance().get(Calendar.MINUTE);
            actvFreeTable.setText("Стол № " + getNumTable);
            btnNewReserveDate.setText(getDate);
            btnNewReserveTime.setText(getTime);
            btnBron.setText("Без брони");
            btnBron.setBackgroundResource(R.drawable.btn_style_6);
            numTypeTableMap.put(getNumTable, getType);
        } else if (whoCall.equals("tableActivity_Add")) {
            actvFreeTable.setText("Стол № " + getNumTable);
            btnBron.setText("Бронь");
            btnBron.setBackgroundResource(R.drawable.btn_style_1);
            numTypeTableMap.put(getNumTable, getType);
        } else {
            btnCreateReserve.setText("Создать  резерв");
//            actvFreeTable.setText("");
            btnBron.setText("Бронь");
            btnBron.setBackgroundResource(R.drawable.btn_style_1);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Log.i("NewOrderActivity", "\n ...//...    onCheckedChanged");
        Log.i("NewOrderActivity", "поменяли тип стола");
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
            whoCall = "";
        }

        initTablesList();
    }

    private void initTablesList() {
        Log.i("NewOrderActivity", "\n ...//...    initTablesList");

        Log.i("NewOrderActivity", "getType = " + getType);

        // здесь фильтр столов по типу и вызов другого фильтра
        btnNewReserveDate.setTextColor(Color.BLACK);
        btnNewReserveTime.setTextColor(Color.BLACK);
        typeNumTableList.clear();

        // получаем данные c табл "TABLES"
        // если тип стола не выбран
        if (getType.equals("")) {
            for (int i = 0; i < allTablesList.size(); i++) {
                numTypeTableMap.put(allTablesList.get(i).getNumber(), allTablesList.get(i).getType());
                typeNumTableList.add(allTablesList.get(i).getNumber());
            }
        } else { // если выбран тип стола, то заносятся только номера подходящих столов
            for (int i = 0; i < allTablesList.size(); i++) {
                if (allTablesList.get(i).getType().equals(getType)) {
                    numTypeTableMap.put(allTablesList.get(i).getNumber(), allTablesList.get(i).getType());
                    typeNumTableList.add(allTablesList.get(i).getNumber());
                }
            }
        }
        Log.i("NewOrderActivity", "typeNumTableList: " + typeNumTableList);
        // здесь вызываем метод, кот. проведет выборку по времени
        changeFreeTableTime();
    }

    private void changeFreeTableTime() {
        Log.i("NewOrderActivity", "\n ...//...    changeFreeTableTime");
        // здесь фильтр по номеру стола и дате и создание списка сомнительных заказов
        checkList.clear();
        Log.i("NewOrderActivity", "checkList.clear()");
        finishNumTableList.clear();
        Log.i("NewOrderActivity", "finishNumTableList.clear()");
        finishList.clear();
        Log.i("NewOrderActivity", "finishList.clear()");

        for (int i = 0; i < allOrdersList.size(); i++) {
            for (int j = 0; j < allOrdersList.get(i).size(); j++) {
                OrderClass order = allOrdersList.get(i).get(j);

                // у нас есть изначально 19 столов (если не выбран тип игры) иначе меньше
                // дальше нам нужно просто вычесть из них уже занятые
                for (int k = 0; k < typeNumTableList.size(); k++) {
                    // если стол теоретически м.б. занят этим заказом (номера столов совпадают)
                    if (typeNumTableList.get(k) == order.getNumTable()) {
                        // если даты совпадают
                        Log.i("NewOrderActivity", "   Date btnNewReserveDate = " + btnNewReserveDate.getText().toString());
                        Log.i("NewOrderActivity", "   Date reserveDateIndex = " + order.getDateStartReserve());
                        if (order.getDateStartReserve().equals(btnNewReserveDate.getText().toString())) {
                            Log.i("NewOrderActivity", "checkList add");
                            checkList.add(order);
                        } else break; // если на желаемую дату вообще нет резервов, то это супер
                    }
                }
            }
        }
        Log.i("NewOrderActivity", "List checkList: " + checkList);

        checkFreeTimeReserve();
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

            durationNewReserve = optionalClass.checkLeftEndWorkMinute(durationNewReserve);

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
//                        reserveDateTime = dateTimeFormat.parse(myDaySt + "." + myMonthSt + "." + yearReserve + " " + "11:00");
                        reserveDateTime = dateTimeFormat.parse(myDaySt + "." + myMonthSt + "." + yearReserve + " " +
                                btnNewReserveTime.getText());
                        Log.i("Gas3", "reserveDateTime = " + reserveDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Gas1", "btnNewReserveDate.getText().toString() = " + btnNewReserveDate.getText().toString());
                Log.i("Gas1", "reserveDateTime = " + reserveDateTime);
                Log.i("Gas1", "reserveDateTime before currentTime? = " + reserveDateTime.before(new Date()));

                if ((reserveDateTime.before(new Date())) || (hourReserve < 11 && hourReserve > 4)) {
                    Log.i("Gas3", "new Date() = " + new Date());
                    Log.i("Gas3", "hourReserve = " + hourReserve);
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

    public void showDialogAlertCreateReserve() {
        Log.i("NewOrderActivity", "\n ...//...    showDialogAlertCreateReserve");
        Log.i("NewOrderActivity", "whoCall = " + whoCall);
        // если наша активити была вызвана (изменить резерв)
//        checkWhoCallThisActivity();
        if (whoCall.equals("tableActivity_Start") || whoCall.equals("tableActivity_Add") || whoCall.equals("editFromTableActivity") ) {
            numTypeTableMap.put(getNumTable, getType);

            Log.i("NewOrderActivity", "True: " +
                    "whoCall.equals(\"tableActivity_Start\") || whoCall.equals(\"tableActivity_Add\")");
        }
        Log.i("NewOrderActivity", "numTypeTableMap.isEmpty() = " + numTypeTableMap.isEmpty());
        Log.i("NewOrderActivity", "actvClient.getListSelection() = " + actvClient.getListSelection());

        if (actvFreeTable.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбран стол", Toast.LENGTH_SHORT).show();
        } else if (actvClient.getText().toString().equals("") || !choseClientFlag) {
            Toast.makeText(NewOrderActivity.this, "Не выбран клиент", Toast.LENGTH_SHORT).show();
        } else if (btnNewReserveDuration.getText().toString().equals("")) {
            Toast.makeText(NewOrderActivity.this, "Не выбрана продолжительность игры", Toast.LENGTH_SHORT).show();
        } else if (!(btnBron.getText().toString().equals("Бронь") || btnBron.getText().toString().equals("Без брони"))) {
            Toast.makeText(NewOrderActivity.this, "Не выбрана бронь", Toast.LENGTH_SHORT).show();
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
                            "\n" + btnBron.getText().toString())
                    .setCancelable(true)  // разрешает/запрещает нажатие кнопки назад
                    .setNegativeButton("Отмена", (dialogInterface, i) -> Toast.makeText(getApplicationContext(),
                            "Резерв отменен", Toast.LENGTH_SHORT).show())
                    .setOnCancelListener(dialogInterface -> Toast.makeText(getApplicationContext(),
                            "нажали назад", Toast.LENGTH_SHORT).show());

            if (getNumOrder != -1) { // если передали сюда стол
                actvClient.setText(getClient);
                builderAlert.setTitle("Изменить резерв?");
                builderAlert.setPositiveButton("Изменить", (dialogInterface, i) -> {
                    getClient = actvClient.getText().toString();
                    OrderClass order = new OrderClass(
                            getNumOrder,
                            getNumTable,
                            btnNewReserveDate.getText().toString(),
                            btnNewReserveTime.getText().toString(),
                            durationNewReserve,
                            currentDaySt + "." + currentMonthSt + "." + currentYearSt,
                            currentHourSt + ":" + currentMinuteSt,
                            getClient,
                            getAdminName,
                            btnBron.getText().toString(),
                            "");
                    optionalClass.putReserveInDB(NewOrderActivity.this, order);
                    Toast.makeText(getApplicationContext(), "Резерв изменен", Toast.LENGTH_SHORT).show();

                    // также нам нужно изменить минуты этому клиенту
                    // для этого нам нужно знать старую продолжительность и новую
                    int oldDuration = getDurationMinute;
                    int newDuration = durationNewReserve;
                    // далее нам нужно вызвать изменение клиента
                    // для этого нам нужно знать все данные этого клиента(создадим специальный метод findIdClient())
                    ClientClass oldClient = findIdClient(getClient);
                    ClientClass newClient = new ClientClass(
                            oldClient.getId(),
                            getClient,
                            oldClient.getPhone(),
                            oldClient.getOrdersCount(),
                            oldClient.getDurationSumMinute() - oldDuration + newDuration);
                    optionalClass.putChangeClientInDB(NewOrderActivity.this, newClient);
                    Log.i("NewOrderActivity", "oldClient.getDurationSumMinute() = " + oldClient.getDurationSumMinute());
                    Log.i("NewOrderActivity", "oldDuration = " + oldDuration);
                    Log.i("NewOrderActivity", "newDuration = " + newDuration);
                    Log.i("NewOrderActivity", "oldClient.getDurationSumMinute() - oldDuration + newDuration = "
                            + (oldClient.getDurationSumMinute() - oldDuration + newDuration));

                    // обновим лист всех заказов
                    allOrdersList = optionalClass.findAllOrders(NewOrderActivity.this, true);
                    optionalClass.findAllOrdersThisDay(NewOrderActivity.this, optionalClass.getWorkDay(), true);

                    if (whoCall.equals("editFromEditDBActivity")) {
                        Intent intent = new Intent("editDBActivity");
                        intent.putExtra("headName", "Резервы");
                        intent.putExtra("adminName", getAdminName);
                        startActivity(intent);
                    } else if (whoCall.equals("editFromTableActivity")) {
                        Intent intent = new Intent("tableActivity");
                        intent.putExtra("whoCall", "btnCommon");
                        intent.putExtra("numTable", getNumTable);
                        intent.putExtra("id", getNumOrder);
                        intent.putExtra("adminName", getAdminName);
                        intent.putExtra("client", getClient);
                        intent.putExtra("duration", getDurationMinute);
                        intent.putExtra("bron", getBron);
                        intent.putExtra("reserveDateStr", getDate);
                        intent.putExtra("reserveStartTimeStr", getTime);
                        intent.putExtra("dateOrder", getDateOrder);
                        intent.putExtra("timeOrder", getTimeOrder);
                        intent.putExtra("reserveFinishTimeStr",  getTimeEndReserve);
                        startActivity(intent);
                    }


                });

            } else {
                builderAlert.setTitle("Создать резерв?");
                builderAlert.setPositiveButton("Создать", (dialogInterface, i) -> {
                    getClient = actvClient.getText().toString();
                    getNumTable = numTable;
                    OrderClass order = new OrderClass(
                            getNumOrder,
                            getNumTable,
                            btnNewReserveDate.getText().toString(),
                            btnNewReserveTime.getText().toString(),
                            durationNewReserve,
                            currentDaySt + "." + currentMonthSt + "." + currentYearSt,
                            currentHourSt + ":" + currentMinuteSt,
                            getClient,
                            getAdminName,
                            btnBron.getText().toString(),
                            "");
                    optionalClass.putReserveInDB(NewOrderActivity.this, order);

                    // также нам нужно инкрементировать посещения и добавить минуты этому клиенту
                    // для этого нам нужно знать старую продолжительность и новую
                    int newDuration = durationNewReserve;
                    // далее нам нужно вызвать изменение клиента
                    // для этого нам нужно знать все данные этого клиента(создадим специальный метод findIdClient())
                    ClientClass oldClient = findIdClient(getClient);
                    ClientClass newClient = new ClientClass(
                            oldClient.getId(),
                            getClient,
                            oldClient.getPhone(),
                            oldClient.getOrdersCount() + 1,
                            oldClient.getDurationSumMinute() + newDuration);

                    optionalClass.putChangeClientInDB(NewOrderActivity.this, newClient);


                    Toast.makeText(getApplicationContext(), "Резерв создан", Toast.LENGTH_SHORT).show();
                    // обновим лист всех заказов
                    allOrdersList = optionalClass.findAllOrders(NewOrderActivity.this, true);
                    optionalClass.findAllOrdersThisDay(NewOrderActivity.this, optionalClass.getWorkDay(), true);
                    Intent intent = new Intent("commonActivity");
                    intent.putExtra("headName", "Резервы");
                    intent.putExtra("adminName", getAdminName);
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

    private void initClients() {
        Log.i("NewOrderActivity", "\n --- /// ---   Method initClients");
        for (int i = 0; i < allClientsList.size(); i++) {
            ClientClass client = allClientsList.get(i);

            clientsList.add(client.getName());
        }
        ArrayAdapter<String> adapterClient = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, clientsList);
        actvClient.setAdapter(adapterClient);

        Log.i("NewOrderActivity", "clientsList = " + clientsList);
    }

    public ClientClass findIdClient(String clientName) {
        for (int i = 0; i < allClientsList.size(); i++) {
            ClientClass client = allClientsList.get(i);

            if (client.getName().equals(clientName)) return client;

        }
        return null;
    }


}