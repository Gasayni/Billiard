package com.gas.billiard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TableActivity extends AppCompatActivity implements View.OnClickListener {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    TextView tvTypeTable, tvCurrentClient, tvCurrentSum, tvCurrentTariff, tvCurrentPrice, tvCurrentGameDuration, tvTimeStartGame;
    AutoCompleteTextView actvNameTable;
    Button btnShowTableReserve, btnAdd, btnStart, btnResume;
    List<String> typeTableList = new ArrayList<>();
    List<String> nameTableList = new ArrayList<>();
    List<Integer> numTableList = new ArrayList<>();
    List<ReserveTable> checkList = new ArrayList<>();
    ReserveTable finishTable;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;
    int getNumTable;
    String type, adminName, currentDateStr, currentTimeStr;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        // загружаем данные с
        Intent getIntent = getIntent();
        getNumTable = getIntent.getIntExtra("numTable", -1);
        adminName = getIntent.getStringExtra("adminName");
        currentDateStr = dateFormat.format(new Date());
        currentTimeStr = timeFormat.format(new Date());

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        initNameTableList();

        tvTypeTable = findViewById(R.id.tvTypeTable);
        tvTimeStartGame = findViewById(R.id.tvTimeStartGame);
        tvCurrentClient = findViewById(R.id.tvCurrentClient);
        tvCurrentTariff = findViewById(R.id.tvCurrentTariff);
        tvCurrentPrice =  findViewById(R.id.tvCurrentPrice);
        tvCurrentGameDuration = findViewById(R.id.tvCurrentGameDuration);
        tvCurrentSum = findViewById(R.id.tvCurrentSum);

        btnShowTableReserve = findViewById(R.id.btnShowTableReserve);
        btnShowTableReserve.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnResume = findViewById(R.id.btnResume);
        btnResume.setOnClickListener(this);
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

        if (getNumTable != 0) {
            choseTable();
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
                startActivity(intent);
                break;
            }
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                // что мы м. передать туда
                intent.putExtra("whoCall", "tableActivity");
                intent.putExtra("adminName", adminName);
                intent.putExtra("numTable", getNumTable);
                startActivity(intent);
                // при добавлении нового резерва, также нужно обновить таблицу резерва
                break;
            }
        }
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

    private void initNameTableList() {
        // получаем данные c табл "EMPLOYEES"
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private void choseTable() {
        // мы получаем выбранное значение
        String nameTable = actvNameTable.getText().toString();
        Log.i("Gas", "nameTable = " + nameTable);
        for (int i = 0; i < nameTableList.size(); i++) {
            if (nameTableList.get(i).equals(nameTable)) {
                getNumTable = numTableList.get(i);
                // меняем картинку, в зависимости от типа стола
                if (typeTableList.get(i).equals("Американский пул")) {
                    tvCurrentSum.setBackgroundResource(R.drawable.table_pool);
                    tvTypeTable.setText("Американский пул");
                } else {
                    tvCurrentSum.setBackgroundResource(R.drawable.table_pyramide);
                    tvTypeTable.setText("Русская пирамида");
                    type = "Русская пирамида";
                }
                break;
            }
        }
        changeFreeTableTime();
    }

    private void changeFreeTableTime() {
        checkList.clear();

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
            int tariffIndex = cursorOrders.getColumnIndex(DBHelper.KEY_TARIFF);

            Log.i("Gas1", "getNumTable = " + getNumTable);
            do {
                Log.i("Gas1", "idIndex = " + cursorOrders.getInt(idIndex));
                Log.i("Gas1", "cursorOrders.getInt(numTableIndex) = " + cursorOrders.getInt(numTableIndex));
                // если стол м.б. занят этим заказом (номера столов совпадают)
                if (getNumTable == cursorOrders.getInt(numTableIndex)) {
                    Log.i("Gas1", "   Date currentDateStr = " + currentDateStr);
                    Log.i("Gas1", "   Date reserveDateIndex = " + cursorOrders.getString(reserveDateIndex));
                    // если даты совпадают
                    if (currentDateStr.equals(cursorOrders.getString(reserveDateIndex))) {
                        Log.i("Gas1", "checkList add");
                        checkList.add(new ReserveTable(
                                cursorOrders.getInt(idIndex),
                                cursorOrders.getInt(numTableIndex),
                                cursorOrders.getString(reserveDateIndex),
                                cursorOrders.getString(reserveTimeIndex),
                                cursorOrders.getInt(durationIndex),
                                cursorOrders.getString(clientIndex),
                                cursorOrders.getString(employeeIndex),
                                cursorOrders.getString(tariffIndex)));
                    }
                }
            } while (cursorOrders.moveToNext());
            checkFreeTimeReserve();
            Log.i("Gas1", "List checkList: " + checkList);

        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
    }

    @SuppressLint("SetTextI18n")
    private void checkFreeTimeReserve() {
        Log.i("Gas1", " --- // --- ");
        Log.i("Gas1", "Start method: checkFreeTimeReserve from TableActivity");

        // нам просто нужно определить занят ли стол в данный момент, ничего лишнего
        String[] newReserveStartTimeArr = currentTimeStr.split(":");
        Log.i("Gas1", "currentTimeStr = " + currentTimeStr);
        int currentHour = Integer.parseInt(newReserveStartTimeArr[0]);
        int currentMinute = Integer.parseInt(newReserveStartTimeArr[1]);
        Log.i("Gas1", "сейчас часов = " + currentHour);
        Log.i("Gas1", "сейчас минут = " + currentMinute);

        Log.i("Gas1", "   Size checkList = " + checkList.size() + " (Кол-во проверяемых заказов на выбранную дату)");
        Log.i("Gas1", "   Start cycle (начинаем цикл по этим заказам)");
        Map<Integer, Integer> numTable_leftMinute_Map = new HashMap<>();
        Log.i("Gas1", "NumTable = " + getNumTable);

        boolean busyFlag = false;
        boolean leftLess30MinFlag = false;
        int finishLeftMinute = 1080;
        for (int i = 0; i < checkList.size(); i++) {
            Log.i("Gas1", " i = " + i);
            finishTable = checkList.get(i);

            int hourStartReserve = checkList.get(i).getHour();
            Log.i("Gas1", "hourStartReserve = " + hourStartReserve);
            int minuteStartReserve = checkList.get(i).getMinute();
            Log.i("Gas1", "minuteStartReserve = " + minuteStartReserve);
            int durationMinuteReserve = checkList.get(i).getDuration();
            Log.i("Gas1", "Old durationMinuteReserve = " + durationMinuteReserve);
            int hourFinishReserve = hourStartReserve + (minuteStartReserve + durationMinuteReserve) / 60;
            Log.i("Gas1", "Old hourFinishReserve " + hourFinishReserve);
            int minuteFinishReserve = (minuteStartReserve + durationMinuteReserve) % 60;
            Log.i("Gas1", "Old minuteFinishReserve = " + minuteFinishReserve);

            // проверяем если время протыкает, то сразу заканчиваем проверку и пользуемся данными этого резерва

            // фильтр по времени
            // посчитаем сколько остается минут до след. резерва (на сколько м. зарезервировать)
            int leftMinute = ((hourStartReserve - currentHour) * 60) + (minuteStartReserve - currentMinute);
            Log.i("Gas1", "Осталось минут до след резерва leftMinute = " + leftMinute);
            // если текущее время раньше след.резерва, (не протыкает) остается больше 30 мин
            if (leftMinute >= 30) {
                Log.i("Gas1", "Стол свободен! И до следующего резерва больше 30 мин");
                // но нужно проверить дальше, если, вдруг дальше выяснится, что стол занят
                if (leftMinute < finishLeftMinute) {
                    finishLeftMinute = leftMinute;
                }
            } else if (leftMinute > 0) { // если не протыкает, но остается меньше 30 мин
                Log.i("Gas1", "Стол свободен, НО! Остается меньше 30 мин");
                if (leftMinute < finishLeftMinute) {
                    finishLeftMinute = leftMinute;
                }
                leftLess30MinFlag = true;
            } else {
                // если текущее время протыкает старые резервы
                Log.i("Gas1", "Этот стол сейчас занят! (проткнул)");
                busyFlag = true;
                break;
            }
        }

        if (finishTable.getDuration() < 60)
            tvCurrentGameDuration.setText("Резерв на : " + finishTable.getDuration() + " мин");
        else {
            int hour = finishTable.getDuration() / 60;
            int minute = finishTable.getDuration() % 60;
            if (minute == 0) tvCurrentGameDuration.setText("Резерв на : " + hour + " ч ");
            else tvCurrentGameDuration.setText("Резерв на : " + hour + " ч " + minute + " мин");
        }

        btnResume.setClickable(false);
        if (!checkList.isEmpty()) {
            if (busyFlag) { // если стол занят
                tvCurrentClient.setTextColor(Color.BLACK);
                btnResume.setClickable(true);
                btnStart.setText("Закрыть");
                // здесь - обработка нажатия кнопки "закрыть"
                tvCurrentClient.setText("Клиент: " + finishTable.getClient());
                tvCurrentTariff.setText(finishTable.getTariff());
                tvTimeStartGame.setText("Начало игры: " + finishTable.getTime());

                if (finishTable.getDuration() < 60)
                    tvCurrentGameDuration.setText("Резерв на : " + finishTable.getDuration() + " мин");
                else {
                    int hour = finishTable.getDuration() / 60;
                    int minute = finishTable.getDuration() % 60;
                    if (minute == 0) tvCurrentGameDuration.setText("Резерв на : " + hour + " ч ");
                    else tvCurrentGameDuration.setText("Резерв на : " + hour + " ч " + minute + " мин");
                }

            } else if (leftLess30MinFlag) { // если стол свободен, но <30 минут
                btnStart.setText("Открыть");
                tvCurrentClient.setTextColor(Color.RED);
                tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTime() + " (осталось " + finishLeftMinute + " мин.)");
//            tvCurrentClient.setText("До начала следующего резерва осталось " + finishLeftMinute);
            } else { // если стол свободен и >30 мин
                btnStart.setText("Открыть");
                tvCurrentClient.setTextColor(Color.BLACK);
                tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTime() + " (осталось " + finishLeftMinute + " мин.)");
//            tvCurrentClient.setText("До начала следующего резерва осталось " + finishLeftMinute);
            }
        } else {  // если заказов для этого стола на сегодня нет
            tvCurrentClient.setText("Стол свободен");
            tvTimeStartGame.setText("Начало игры: __:__");
            tvCurrentGameDuration.setText("Продолжительность игры: _ ч _ мин");
            tvCurrentTariff.setText("...");
            tvCurrentPrice.setText("___ р / ч");
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
                currentTimeStr = timeFormat.format(new Date());
                checkFreeTimeReserve();
                // также обращаемся каждую минуту к калькулятору оставшегося времени
                actualTime();  // мисис рекурсия
            });
        }).start();
    }
}