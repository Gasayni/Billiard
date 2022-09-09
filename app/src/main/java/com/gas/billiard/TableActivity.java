package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TableActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass optionalClass = new OptionallyClass();
    // определим, сколько заказов есть на этот день
    List<List<OrderClass>> allOrdersList = optionalClass.findAllOrders(this, false);
    List<AdminClass> allAdminsList = optionalClass.findAllAdmins(this, false);
    List<ClientClass> allClientsList = optionalClass.findAllClients(this, false);
    List<TableClass> allTablesList = optionalClass.findAllTables(this, false);
    List<String> phoneClientsList = new ArrayList<>();

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    TextView tvTypeTable, tvCurrentClient, tvCurrentGameDuration, tvTimeStartGame, tvTimeEndGame, tvImage;
    AutoCompleteTextView actvNameTable;
    Button btnShowTableReserve, btnAdd, btnStart, btnResume, btnDeleteReserve, btnEditReserve;
    List<String> typeTableList = new ArrayList<>();
    List<String> nameTableList = new ArrayList<>();
    List<Integer> numTableList = new ArrayList<>();
    List<OrderClass> checkOrdersList = new ArrayList<>();
    OrderClass finishTable, getTable;

    int getId, getNumTable, getDurationMinute, numberRowDB;
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
        btnEditReserve = findViewById(R.id.btnEditReserve);
        btnEditReserve.setOnClickListener(this);
        btnEditReserve.setVisibility(View.INVISIBLE);
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

        optionalClass.checkOldReserve(TableActivity.this);

        choseTable();

        if (whoCall.equals("btnCommon")) {
            Log.i("Gas5", "getNumTable = " + getNumTable);
            Log.i("Gas5", "getReserveDateStr = " + getReserveDateStr);
            Log.i("Gas5", "getReserveStartTimeStr = " + getReserveStartTimeStr);
            Log.i("Gas5", "getDurationMinute = " + getDurationMinute);
            Log.i("Gas5", "getClient = " + getClient);
            Log.i("Gas5", "getAdminName = " + getAdminName);
            Log.i("Gas5", "getBron = " + getBron);
            getTable = new OrderClass(
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
            checkOrdersList.add(getTable);
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
                    openStartResumeStopDialog("Закрыть");
                }
                break;
            }
            case R.id.btnResume: {
                optionalClass.resumeOrder(this, finishTable, getAdminName);

                Toast.makeText(TableActivity.this, "Добавлено 15 минут. \n", Toast.LENGTH_SHORT).show();
                allOrdersList = optionalClass.findAllOrders(TableActivity.this, true);
                optionalClass.findAllOrdersThisDay(TableActivity.this, optionalClass.getWorkDay(), true);

                // нужно обновить стол
                intent = new Intent("tableActivity");
                // передаем название заголовка
                intent.putExtra("numTable", getNumTable);
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.btnDeleteReserve: {
                openStartResumeStopDialog("Удалить");
                break;
            }
            case R.id.btnEditReserve: {
                openStartEditDialog();
                break;
            }
        }
    }

    public void openStartEditDialog() {
        Log.i("TableActivity", "\n --- /// ---   Method openStartEditDialog");
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(TableActivity.this);
        builderAlert.setTitle("Изменить резерв: Вы уверены?");
        builderAlert.setPositiveButton("Да", ((dialogInterface, i) -> {
            // действия измения стола

            Intent intent = new Intent("newOrderActivity");
            intent.putExtra("whoCall", "editFromTableActivity");
            intent.putExtra("numReserve", numberRowDB);
            intent.putExtra("numTable", finishTable.getNumTable());
            intent.putExtra("type", findTypeTable(finishTable.getNumTable()));
            intent.putExtra("date", finishTable.getDateStartReserve());
            intent.putExtra("time", finishTable.getTimeStartReserve());
            intent.putExtra("dateOrder", finishTable.getDateOrder());
            intent.putExtra("timeOrder", finishTable.getTimeOrder());
            intent.putExtra("duration", finishTable.getDuration());
            intent.putExtra("client", finishTable.getClient());
            intent.putExtra("adminName", getAdminName);
            intent.putExtra("status", finishTable.getStatus());
            intent.putExtra("bron", finishTable.getBron());
            intent.putExtra("timeEndReserve", finishTable.getTimeEndReserve());
            startActivity(intent);
        }));

        builderAlert.setCancelable(true);  // разрешает/запрещает нажатие кнопки назад
        builderAlert.setIcon(R.drawable.bol_pyramide1);
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.show();
    }



    public void openStartResumeStopDialog(String whoCall) {
        Log.i("TableActivity", "\n --- /// ---   Method openStartResumeStopDialog");
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(TableActivity.this);
        if (whoCall.equals("Закрыть")) {
            builderAlert.setTitle("Закрыть стол: Вы уверены?");
            builderAlert.setPositiveButton("Да", ((dialogInterface, i) -> {
                // действия закрытия стола
                // нам нужно изменить продолжительность игры в БД на окончание сейчас
                optionalClass.stopOrder(TableActivity.this, finishTable, getAdminName);

                allOrdersList = optionalClass.findAllOrders(TableActivity.this, true);
                optionalClass.findAllOrdersThisDay(TableActivity.this, optionalClass.getWorkDay(), true);

                Intent intent = new Intent("commonActivity");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);

                Log.i("Gas5", "finishTable.duration = " + finishTable.getDuration());
                Log.i("Gas5", "finishTable.getDateEndReserve() = " + finishTable.getDateEndReserve());
                Log.i("Gas5", "finishTable.getTimeEndReserve() = " + finishTable.getTimeEndReserve());
            }));
        } else if (whoCall.equals("Удалить")) {
            builderAlert.setTitle("Удалить резерв: Вы уверены?");
            builderAlert.setPositiveButton("Да", ((dialogInterface, i) -> {
                // действия удаления стола

                optionalClass.deleteOrder(TableActivity.this, numberRowDB);
                Toast.makeText(TableActivity.this, "Резерв " + numberRowDB + " удален", Toast.LENGTH_LONG).show();

                allOrdersList = optionalClass.findAllOrders(TableActivity.this, true);
                optionalClass.findAllOrdersThisDay(TableActivity.this, optionalClass.getWorkDay(), true);

                Intent intent = new Intent("commonActivity");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
            }));
        }

        builderAlert.setCancelable(true);  // разрешает/запрещает нажатие кнопки назад
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
        Log.i("TableActivity", "\n --- /// ---   Method initNameTableList");
        for (int i = 0; i < allTablesList.size(); i++) {
            TableClass table = allTablesList.get(i);

            // инициализируем каждую кнопку шапки стола
            nameTableList.add("Стол № " + table.getNumber());
            numTableList.add(table.getNumber());
            typeTableList.add(table.getType());
        }

        Log.i("TableActivity", "nameTableList: " + nameTableList);
        Log.i("TableActivity", "numTableList: " + numTableList);
        Log.i("TableActivity", "typeTableList: " + typeTableList);
    }

    private String findTypeTable(int numTable) {
        Log.i("TableActivity", "\n --- /// ---   Method initNameTableList");
        for (int i = 0; i < allTablesList.size(); i++) {
            TableClass table = allTablesList.get(i);

            if (numTable == table.getNumber()) return table.getType();
        }
        return "";
    }

    // метод меняет вьюшки активити в зависимости от типа стола
    @SuppressLint("ClickableViewAccessibility")
    private void choseTable() {
        Log.i("TableActivity", "\n --- /// ---   Method choseTable");
        // мы получаем выбранное значение
        String nameTable = actvNameTable.getText().toString();
        Log.i("TableActivity", "nameTable = " + nameTable);
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
        Log.i("TableActivity", "getNumTable = " + getNumTable);
        getTable = null; // если мы переключили стол, то нужно удалить переданный стол, чтобы не мешал
        btnDeleteReserve.setVisibility(View.INVISIBLE);
        btnEditReserve.setVisibility(View.INVISIBLE);
        changeFreeTableTime();
    }

    // метод формирует список резервов на сегодня, которые нужно проверить тщательнее
    // и вызывает следующей метод
    private void changeFreeTableTime() {
        Log.i("TableActivity", "\n --- /// ---   Method changeFreeTableTime");
        checkOrdersList.clear();

        Log.i("TableActivity", "   Date currentDateStr = " + currentDateStr);
        // ?? старые заказы
        for (int i = 0; i < allOrdersList.size(); i++) {
            for (int j = 0; j < allOrdersList.get(i).size(); j++) {
                OrderClass order = allOrdersList.get(i).get(j);

                // если номера столов совпадают
                Log.i("TableActivity", "   Date reserveDateIndex = " + order.getDateStartReserve());
                if (getNumTable == order.getNumTable()) {
                    // если даты совпадают
                    if (currentDateStr.equals(order.getDateStartReserve())) {
                        Log.i("TableActivity", "checkList added");
                        checkOrdersList.add(order);
                    }
                }
            }
        }

        Log.i("TableActivity", "checkList.size(): " + checkOrdersList.size());
        checkFreeTimeReserve();
    }

    // метод проверяет занят ли стол в данный момент (формирует список столов на сегодня, которые нужно проверить тщательнее)
    @SuppressLint("SetTextI18n")
    private void checkFreeTimeReserve() {
        Log.i("TableActivity", "\n --- /// ---   Method TableActivity");

        // нам просто нужно определить занят ли стол в данный момент, ничего лишнего
        String[] currentTimeArr = currentTimeStr.split(":");
        Log.i("TableActivity", "currentTimeStr = " + currentTimeStr);
        int currentHour = Integer.parseInt(currentTimeArr[0]);
        int currentMinute = Integer.parseInt(currentTimeArr[1]);
        Log.i("TableActivity", "сейчас часов = " + currentHour);
        Log.i("TableActivity", "сейчас минут = " + currentMinute);


        boolean busyFlag = false;
        boolean leftLess30MinFlag = false;
        int finishLeftMinute = 1080;
        Log.i("TableActivity", "\tStart cycle (начинаем цикл по заказам checkOrdersList)");
        for (int i = 0; i < checkOrdersList.size(); i++) {
            Log.i("TableActivity", " i = " + i);
            int hourStartReserve = checkOrdersList.get(i).getHourStartReserve();
            Log.i("TableActivity", "hourStartReserve = " + hourStartReserve);
            int minuteStartReserve = checkOrdersList.get(i).getMinuteStartReserve();
            Log.i("TableActivity", "minuteStartReserve = " + minuteStartReserve);
            int durationMinuteReserve = checkOrdersList.get(i).getDuration();
            Log.i("TableActivity", "Old durationMinuteReserve = " + durationMinuteReserve);
            int hourFinishReserve = hourStartReserve + (minuteStartReserve + durationMinuteReserve) / 60;
            Log.i("TableActivity", "Old hourFinishReserve " + hourFinishReserve);
            int minuteFinishReserve = (minuteStartReserve + durationMinuteReserve) % 60;
            Log.i("TableActivity", "Old minuteFinishReserve = " + minuteFinishReserve);

            // фильтр по времени
            // посчитаем сколько остается минут до след. резерва (на сколько м. зарезервировать)
            if (hourStartReserve < 5) hourStartReserve += 24;
            int leftMinute = ((hourStartReserve - currentHour) * 60) + (minuteStartReserve - currentMinute);
            Log.i("TableActivity", "hourStartReserve = " + hourStartReserve);
            Log.i("TableActivity", "currentHour = " + currentHour);
            Log.i("TableActivity", "minuteStartReserve = " + minuteStartReserve);
            Log.i("TableActivity", "currentMinute = " + currentMinute);
            Log.i("TableActivity", "leftMinute = " + leftMinute);

            if (getTable != null) {
                finishTable = getTable;
            } else {
                // если СТОЛ СВОБОДЕН и остается больше 30 мин
                if (leftMinute >= 30 || (checkOrdersList.get(i).getEndDateTimeReserveCal().before(Calendar.getInstance()))) {
                    Log.i("TableActivity", "Стол свободен! И до следующего резерва больше 30 мин");
                    // но нужно проверить дальше, если, вдруг дальше выяснится, что стол занят
                    if (leftMinute < finishLeftMinute) {
                        finishLeftMinute = leftMinute;
                        finishTable = checkOrdersList.get(i);
                    }

                }
                // если СТОЛ СВОБОДЕН, но остается меньше 30 мин
                else if (leftMinute > 0) {
                    Log.i("TableActivity", "Стол свободен, НО! Остается меньше 30 мин");
                    if (leftMinute < finishLeftMinute) {
                        finishLeftMinute = leftMinute;
                        finishTable = checkOrdersList.get(i);
                    }
                    leftLess30MinFlag = true;
                }
                // если СТОЛ ЗАНЯТ (текущее время Протыкает время резерва)
                else {
                    Log.i("TableActivity", "Этот стол сейчас занят! (проткнул)");
                    finishTable = checkOrdersList.get(i);
                    busyFlag = true;
                    break;
                }
            }
        }

        btnStart.setBackgroundResource(R.drawable.btn_style_7);
        btnStart.setText("Открыть");
        btnStart.setClickable(true);

        // если стол вообще есть
        if (finishTable != null) {
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
                btnResume.setBackgroundResource(R.drawable.btn_style_6);
                btnResume.setClickable(true);
                tvCurrentClient.setText("Клиент: " + finishTable.getClient() + "\nТел: " + findPhoneClient(finishTable.getClient()));
            } else if (leftLess30MinFlag) { // если СТОЛ СВОБОДЕН, но <30 минут
                btnStart.setClickable(false);
                btnStart.setBackgroundResource(R.drawable.btn_style_6_1);
                tvCurrentClient.setTextColor(Color.RED);
                tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n" +
                        "(осталось " + finishLeftMinute + " мин.)\n" +
                        "Клиент: " + finishTable.getClient() + "\tТел: " + findPhoneClient(finishTable.getClient()));
                btnResume.setBackgroundResource(R.drawable.btn_style_6_1);
                btnResume.setClickable(false);

            } else { // если стол свободен и >30 мин
                tvCurrentClient.setTextColor(Color.BLACK);
                if (finishLeftMinute > 60) {
                    int hour = finishLeftMinute / 60;
                    int minute = finishLeftMinute % 60;
                    if (minute == 0)
                        tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n" +
                                "(осталось " + hour + " час.)\n" +
                                "Клиент: " + finishTable.getClient() + "\tТел: " + findPhoneClient(finishTable.getClient()));
                    else
                        tvCurrentClient.setText("Следующий резерв начнется в " + finishTable.getTimeStartReserve() + " \n" +
                                "(осталось " + hour + " час. " + minute + " мин.)\n" +
                                "Клиент: " + finishTable.getClient() + "\tТел: " + findPhoneClient(finishTable.getClient()));
                }
                btnResume.setBackgroundResource(R.drawable.btn_style_6_1);
                btnResume.setClickable(false);
            }
        } else {
            // если заказов для этого стола на сегодня нет
            Log.i("Gas5", "Заказов для этого стола нет");
            tvCurrentClient.setText("Стол свободен");
            tvTimeStartGame.setText("Начало игры: __:__");
            tvTimeEndGame.setText("Конец игры: __:__");
            tvCurrentGameDuration.setText("Продолжительность игры: _ ч _ мин");
            btnResume.setBackgroundResource(R.drawable.btn_style_6_1);
            btnResume.setClickable(false);
        }

        // если мы открыли резерв через таблицу (Если мы получили getTable)
        if (getTable != null) {
            tvCurrentClient.setText("Резерв на " + finishTable.getTimeStartReserve() + " \n" +
                    "Клиент: " + finishTable.getClient() + "\tТел: " + findPhoneClient(finishTable.getClient()));

            numberRowDB = finishTable.getIdOrder();
            btnDeleteReserve.setVisibility(View.VISIBLE);
            btnEditReserve.setVisibility(View.VISIBLE);
            btnResume.setBackgroundResource(R.drawable.btn_style_6);
            btnResume.setClickable(true);
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

    private String findPhoneClient(String nameClient) {
        Log.i("TableActivity", "\n --- /// ---   Method findPhoneClient");
        String phone = "Номер не найден";
        for (int i = 0; i < allClientsList.size(); i++) {
            if (allClientsList.get(i).getName().equals(nameClient)) {
                phone = allClientsList.get(i).getPhone();
                break;
            }
        }
        Log.i("TableActivity", "phone = " + phone);
        return phone;
    }
}