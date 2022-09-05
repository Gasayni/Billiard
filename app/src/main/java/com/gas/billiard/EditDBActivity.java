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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditDBActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    OptionallyClass optionalClass = new OptionallyClass();
    // определим, сколько заказов есть на этот день
    List<List<OrderClass>> allOrdersList = optionalClass.findAllOrders(this, "Необязательно", false);
    List<AdminClass> allAdminsList = optionalClass.findAllAdmins(this, false);
    List<ClientClass> allClientsList = optionalClass.findAllClients(this, false);
    List<TableClass> allTablesList = optionalClass.findAllTables(this, false);


    LinearLayout linColumns;
    Button btnColumns, btnAdd, btnDelete, btnChange;
    TextView tvHead, tvData, tvPyramid, tvPool;
    Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
    Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
    String choseTypeTable = "Русская пирамида";

    private List<String> nameBtnColumns = new ArrayList<>();
    private List<Button> btnColumnsTagsList = new ArrayList<>();
    private int marginLength, numberRowDB, numTableDB, durationMinuteDB, ordersCountDB, durationSumMinuteDB;
    private String typeDB, dateDB, timeDB, clientDB, nameDB, phoneDB, bronDB, statusDB;
    String choseStr;
    AutoCompleteTextView actvChose;
    List<Button> getBtnColumnsTagsList = new ArrayList<>();
    boolean findClientFlag, findEmployeeFlag, findReserveFlag;

    List<String> adminsList = new ArrayList<>();
    List<String> clientsList = new ArrayList<>();

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTABLE_DB, cursorOrders, cursorTables, cursorClient, cursorEmployee;
    private String TABLE_DB, getAdminName;

    String sortTableName;
    int getFilterNumTable;
    String[] columnsArr = {DBHelper.KEY_ID, DBHelper.KEY_NUM_TABLE, DBHelper.KEY_RESERVE_DATE,
            DBHelper.KEY_RESERVE_TIME, DBHelper.KEY_DURATION, DBHelper.KEY_CLIENT, DBHelper.KEY_EMPLOYEE,
            DBHelper.KEY_ORDER_DATE, DBHelper.KEY_ORDER_TIME, DBHelper.KEY_BRON, DBHelper.KEY_STATUS};
    String selection = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_db);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        tvData = findViewById(R.id.tvData);
        tvHead = findViewById(R.id.tvHead);

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnChange = findViewById(R.id.btnChange);
        btnChange.setOnClickListener(this);

        // Получаем название заголовка
        Intent getIntent = getIntent();
        tvHead.setText(getIntent.getStringExtra("headName"));
        getFilterNumTable = getIntent.getIntExtra("getFilterNumTable", -1);
        getAdminName = getIntent.getStringExtra("adminName");
        Log.i("Gas4", "getAdminName in Edit = " + getAdminName);

        addBtnColumns();

        if (tvHead.getText().toString().equals("Резервы")) {
            Log.i("Gas6", "getFilterNumTable = " + getFilterNumTable);
            // здесь будет фильтр по столам, если в getFilterNumTable передали номер стола
            if (getFilterNumTable == -1) {
                selection = DBHelper.KEY_STATUS + " = \'\'";
                editBtnColumns(columnsArr, selection, null, null, null, DBHelper.KEY_ID);
            } else {
                selection = DBHelper.KEY_NUM_TABLE + " = " + getFilterNumTable + " AND " + DBHelper.KEY_STATUS + " = \'\'";
                editBtnColumns(columnsArr, selection, null, null, null, null);
            }
        } else if (tvHead.getText().toString().equals("Столы")) {
            Log.i("Gas5", "Столы столы");
            // прячем ненужные кнопки
            btnAdd.setVisibility(View.GONE); /*INVISIBLE*/
            btnDelete.setVisibility(View.GONE);
            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
        } else if (tvHead.getText().toString().equals("Клиенты")) {
            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
        } else if (tvHead.getText().toString().equals("Сотрудники")) {
            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
        } else if (tvHead.getText().toString().equals("Тарифы")) {
            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnAdd: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    intent = new Intent("newOrderActivity");
                    intent.putExtra("whoCall", "editDBActivity_add");
                    intent.putExtra("adminName", getAdminName);
                    startActivity(intent);
                }
                if (tvHead.getText().toString().equals("Клиенты")) {
                    optionalClass.openDialogCreateClient(this, getAdminName);
                }
                if (tvHead.getText().toString().equals("Сотрудники")) {
                    optionalClass.openDialogCreateEmployee(this, getAdminName);
                }
                break;
            }
            case R.id.btnDelete: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    openDialogDelete("Резервы");
                } else if (tvHead.getText().toString().equals("Клиенты")) {
                    openDialogDelete("Клиенты");
                } else if (tvHead.getText().toString().equals("Сотрудники")) {
                    openDialogDelete("Сотрудники");
                } else if (tvHead.getText().toString().equals("Тарифы")) {
                    openDialogDelete("Тарифы");
                }
                break;
            }
            case R.id.btnChange: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    openDialogChangeReserve();
                } else if (tvHead.getText().toString().equals("Столы")) {
                    openDialogChangeTable();
                } else if (tvHead.getText().toString().equals("Клиенты")) {
                    openDialogTakeDataChangeClient();
                } else if (tvHead.getText().toString().equals("Сотрудники")) {
                    openDialogTakeDataChangeEmployee();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(EditDBActivity.this, CommonActivity.class);
        intent.putExtra("adminName", getAdminName);
        startActivity(intent);
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
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }




    // метод отрисовывает кнопки сортировки и фильтрации и прочих в шапке
    public void addBtnColumns() {
        // сначала отрисуем кнопки шапки
        marginLength = optionalClass.convertDpToPixels(this, 2);

        linColumns = findViewById(R.id.linHead);
        nameBtnColumns.add("Сортировка");
        nameBtnColumns.add("Фильтр");
        // если нужны дополнительные кнопки, то добавляем к листу еще с названиями
        switch (tvHead.getText().toString()) {
            case "Столы": {
                TABLE_DB = DBHelper.TABLES;
                break;
            }
            case "Сотрудники": {
                TABLE_DB = DBHelper.EMPLOYEES;
                break;
            }
            case "Клиенты": {
                TABLE_DB = DBHelper.CLIENTS;
                break;
            }
            case "Резервы": {
                TABLE_DB = DBHelper.ORDERS;
                nameBtnColumns.add("Все резервы");
                break;
            }
        }

        for (int i = 0; i < nameBtnColumns.size(); i++) {
            linColumns = findViewById(R.id.linColumns);

            btnColumns = new Button(linColumns.getContext());
            btnColumns.setTag("btnColumns" + (i + 1));
            btnColumns.setLayoutParams(new LinearLayout.LayoutParams(
                    optionalClass.convertDpToPixels(this, 60),
                    optionalClass.convertDpToPixels(this, 60)));
            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnColumns.getLayoutParams();

            // здесь особые кнопки сортировки и фильтации и другие (else)
            if (btnColumns.getTag().equals("btnColumns1")) {
                btnColumns.setBackgroundResource(R.drawable.sort_icon);
                marginBtnTable.setMargins((marginLength * 8), 0, (marginLength * 8), 0);
                if (tvHead.getText().toString().equals("Столы") || tvHead.getText().toString().equals("Сотрудники")) {
                    btnColumns.setVisibility(View.GONE); /*INVISIBLE*/
                }
            } else if (btnColumns.getTag().equals("btnColumns2")) {
                btnColumns.setBackgroundResource(R.drawable.filter_icon);
                marginBtnTable.setMargins((marginLength * 8), 0, (marginLength * 8), 0);
                if (tvHead.getText().toString().equals("Столы") || tvHead.getText().toString().equals("Сотрудники")) {
                    btnColumns.setVisibility(View.GONE); /*INVISIBLE*/
                }
            } else {
                btnColumns.setLayoutParams(new LinearLayout.LayoutParams(
                        optionalClass.convertDpToPixels(this, 180),
                        optionalClass.convertDpToPixels(this, 60)));

                marginBtnTable.setMargins((marginLength * 8), 0, (marginLength * 8), 0);
                btnColumns.setBackgroundResource(R.drawable.btn_style_2);
                btnColumns.setText(nameBtnColumns.get(i));
                btnColumns.setTextSize(20);
                btnColumns.setTextColor(Color.BLACK);
            }


            getBtnColumnsTagsList.add(btnColumns);
            Log.i("Gas", "btnColumns.getTag()" + btnColumns.getTag());
            btnColumnsTagsList.add(btnColumns);


            linColumns.addView(btnColumns);
            onClickSortMethod(btnColumns);
        }

    }

    // метод формирует список данных БД (общий формат)
    public void editBtnColumns(String[] column, String selection, String[] selectionArgs,
                               String groupBy, String having, String orderBy) {
        // очищаем сначала
        tvData.setText("");


        int idIndex = 0, durationSumMinuteIndex = 0, ordersIndex = 0, phoneIndex = 0,
                nameIndex = 0, numTableIndex = 0, reserveDateIndex = 0, reserveTimeIndex = 0,
                durationIndex = 0, clientIndex = 0, employeeIndex = 0, dateOrderIndex = 0,
                timeOrderIndex = 0, typeIndex = 0, bronIndex = 0, statusIndex = 0;

        // получаем данные c табл выбранной таблицы
        cursorTABLE_DB = database.query(TABLE_DB, column, selection, selectionArgs, groupBy, having, orderBy);
        if (cursorTABLE_DB.moveToFirst()) {
            idIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ID);
            // здесь мы инициализируем
            switch (tvHead.getText().toString()) {
                // здесь мы инициализируем
                case "Столы": {
                    typeIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_TYPE);
                    break;
                }
                case "Сотрудники": {
                    nameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_NAME);
                    phoneIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_PHONE);
                    break;
                }
                case "Клиенты": {
                    nameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_NAME);
                    phoneIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_PHONE);
                    ordersIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
                    durationSumMinuteIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_DURATION_SUM_MINUTE);
                    break;
                }
                case "Резервы": {
                    numTableIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_NUM_TABLE);
                    reserveDateIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_RESERVE_DATE);
                    reserveTimeIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_RESERVE_TIME);
                    durationIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_DURATION);
                    clientIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_CLIENT);
                    employeeIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_EMPLOYEE);
                    dateOrderIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ORDER_DATE);
                    timeOrderIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ORDER_TIME);
                    bronIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_BRON);
                    statusIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_STATUS);
                    break;
                }
            }
            do {
                // записали все, что было до
                StringBuilder s = new StringBuilder(tvData.getText().toString());
                // здесь мы выводим все
                switch (tvHead.getText().toString()) {
                    case "Столы": {
                        s.append("\n" + String.format("%-7s", cursorTABLE_DB.getInt(idIndex) + "."))
                                .append(cursorTABLE_DB.getString(typeIndex));
                        break;
                    }
                    case "Сотрудники": {
                        s.append("\n" + String.format("%-7s", cursorTABLE_DB.getInt(idIndex) + "."))
                                .append(String.format("%-26s", cursorTABLE_DB.getString(nameIndex)))
                                .append("Тел: " + String.format("%-16s", cursorTABLE_DB.getString(phoneIndex)));
                        break;
                    }
                    case "Клиенты": {
                        s.append("\n" + String.format("%-4s", (cursorTABLE_DB.getInt(idIndex) + ".")))
                                .append(String.format("%-32s", cursorTABLE_DB.getString(nameIndex)))
                                .append("Тел: " + String.format("%-14s", cursorTABLE_DB.getString(phoneIndex)))
                                .append("Заказал: " + String.format("%-10s", (cursorTABLE_DB.getInt(ordersIndex) + " раз")))
                                .append("Пребывание(мин): " + String.format("%-12s", (cursorTABLE_DB.getInt(durationSumMinuteIndex) + " руб.")));
                        Log.i("Gas5", s.toString());
                        break;
                    }
                    case "Резервы": {
                        s.append("\n" + String.format("%-7s", cursorTABLE_DB.getInt(idIndex) + "."))
                                .append("Стол № " + String.format("%-7s", cursorTABLE_DB.getInt(numTableIndex)))
                                .append("Резерв на: " + String.format("%-15s", cursorTABLE_DB.getString(reserveDateIndex)))
                                .append(String.format("%-10s", cursorTABLE_DB.getString(reserveTimeIndex)))
                                .append("Продолжительность(мин): " + String.format("%-8s", cursorTABLE_DB.getInt(durationIndex)))
                                .append("Клиент: " + String.format("%-36s", cursorTABLE_DB.getString(clientIndex)))
                                .append("Оформил: " + String.format("%-36s", cursorTABLE_DB.getString(employeeIndex)))
                                .append(String.format("%-13s", cursorTABLE_DB.getString(dateOrderIndex)))
                                .append(String.format("%-10s", cursorTABLE_DB.getString(timeOrderIndex)))
                                .append("\t" + cursorTABLE_DB.getString(bronIndex));
                        break;
                    }
                }
                tvData.setText(s.toString());
                tvData.setTextColor(Color.BLACK);

            } while (cursorTABLE_DB.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
            tvData.setHint("Актуальных данных нет");
        }
        cursorTABLE_DB.close();
    }

    // метод определяет выбор переключателя типа стола (изменение стола)
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Log.i("Gas", "isChecked first = " + isChecked);
        if (isChecked) {
            tvPyramid.setTypeface(boldTypeface);
            tvPyramid.setText("Русская пирамида");
            tvPool.setTypeface(normalTypeface);
            tvPool.setText("");
            choseTypeTable = "Русская пирамида";

        } else {
            tvPool.setTypeface(boldTypeface);
            tvPool.setText("Американский пул");
            tvPyramid.setTypeface(normalTypeface);
            tvPyramid.setText("");
            choseTypeTable = "Американский пул";
        }
    }




    // метод забирает все данные из изменяемого резерва (изменение резерва)
    private void takeDataChangeOrder() {
        findReserveFlag = false;
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
            int bronIndex = cursorOrders.getColumnIndex(DBHelper.KEY_BRON);
            int statusIndex = cursorOrders.getColumnIndex(DBHelper.KEY_STATUS);
            do {
                if (numberRowDB == cursorOrders.getInt(idIndex)) {
                    findReserveFlag = true;
                    numTableDB = cursorOrders.getInt(numTableIndex);
                    dateDB = cursorOrders.getString(reserveDateIndex);
                    timeDB = cursorOrders.getString(reserveTimeIndex);
                    durationMinuteDB = cursorOrders.getInt(durationIndex);
                    clientDB = cursorOrders.getString(clientIndex);
                    bronDB = cursorOrders.getString(bronIndex);
                    statusDB = cursorOrders.getString(statusIndex);
                }
            } while (cursorOrders.moveToNext());

            Log.i("Gas5", "numReserveDB = " + numberRowDB);
            Log.i("Gas5", "numTableDB = " + numTableDB);
            Log.i("Gas5", "dateDB = " + dateDB);
            Log.i("Gas5", "timeDB = " + timeDB);
            Log.i("Gas5", "durationMinuteDB = " + durationMinuteDB);
            Log.i("Gas5", "clientDB = " + clientDB);

            if (findReserveFlag) takeTypeTableMethod();
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
    }

    // метод забирает все данные из изменяемого клиента (изменение клиента)
    private void takeDataChangeClient() {
        findClientFlag = false;
        cursorClient = database.query(DBHelper.CLIENTS,
                null, null, null,
                null, null, null);
        if (cursorClient.moveToFirst()) {
            int idIndex = cursorClient.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursorClient.getColumnIndex(DBHelper.KEY_NAME);
            int phoneIndex = cursorClient.getColumnIndex(DBHelper.KEY_PHONE);
            int ordersCountIndex = cursorClient.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
            int durationSumMinuteIndex = cursorClient.getColumnIndex(DBHelper.KEY_DURATION_SUM_MINUTE);
            do {
                if (numberRowDB == cursorClient.getInt(idIndex)) {
                    findClientFlag = true;
                    nameDB = cursorClient.getString(nameIndex);
                    phoneDB = cursorClient.getString(phoneIndex);
                    ordersCountDB = cursorClient.getInt(ordersCountIndex);
                    durationSumMinuteDB = cursorClient.getInt(durationSumMinuteIndex);
                }
            } while (cursorClient.moveToNext());

            Log.i("Gas5", "nameDB = " + nameDB);
            Log.i("Gas5", "phoneDB = " + phoneDB);
            Log.i("Gas5", "ordersCountDB = " + ordersCountDB);
            Log.i("Gas5", "durationSumMinuteDB = " + durationSumMinuteDB);
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorClient.close();
    }

    // метод забирает все данные из изменяемого админа (изменение админа)
    private void takeDataChangeEmployee() {
        findEmployeeFlag = false;
        cursorEmployee = database.query(DBHelper.EMPLOYEES,
                null, null, null,
                null, null, null);
        if (cursorEmployee.moveToFirst()) {
            int idIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_NAME);
            int phoneIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PHONE);
            do {
                if (numberRowDB == cursorEmployee.getInt(idIndex)) {
                    findEmployeeFlag = true;
                    nameDB = cursorEmployee.getString(nameIndex);
                    phoneDB = cursorEmployee.getString(phoneIndex);
                }
            } while (cursorEmployee.moveToNext());

            Log.i("Gas5", "nameDB = " + nameDB);
            Log.i("Gas5", "phoneDB = " + phoneDB);
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorEmployee.close();
    }

    // метод забирает тип стола изменяемого резерва (изменение стола)
    private void takeTypeTableMethod() {
        // метод получает тип выбранного стола

        // получаем данные c табл "TABLES"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            do {
                if (cursorTables.getInt(numTableIndex) == numTableDB) {
                    typeDB = cursorTables.getString(typeTableIndex);
                }
            } while (cursorTables.moveToNext());

            Log.i("Gas5", "typeDB = " + typeDB);
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }





    // метод отрабатывает нажатия кнопок сорт и фильтра и прочих в шапке
    private void onClickSortMethod(Button btnColumns) {
        btnColumns.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // здесь будет сортировка в зависимости от нажатой кнопки
                switch (tvHead.getText().toString()) {
                    case "Столы": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            // Нажата Кнопка "Сортировка"
                            sortTableName = DBHelper.KEY_ID;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            // Нажата Кнопка "Фильтр"
                            sortTableName = DBHelper.KEY_TYPE;
                        }
                        break;
                    }
                    case "Сотрудники": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            // Нажата Кнопка "Сортировка"
                            sortTableName = DBHelper.KEY_NAME;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            // Нажата Кнопка "Фильтр"
                            sortTableName = DBHelper.KEY_DURATION_SUM_MINUTE;
                        }
                        break;
                    }
                    case "Клиенты": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            // Нажата Кнопка "Сортировка"
                            showSortClientPopupMenu(btnColumns);
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            // Нажата Кнопка "Фильтр"
                            showFilterClientPopupMenu(btnColumns);
                        }
                        break;
                    }
                    case "Резервы": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            // Нажата Кнопка "Сортировка"
                            showSortReserveTablePopupMenu(btnColumns);
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            // Нажата Кнопка "Фильтр"
                            showFilterReserveTablePopupMenu(btnColumns);
                        } else if (btnColumns.getTag().equals("btnColumns3")) {
                            // Нажата Кнопка "Все резервы"
                            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
                            // здесь покажем все резервы, включая архивные и прошедшие
                        }
                        break;
                    }
                }
            }
        });
    }

    // метод обрабатывает нажатия кнопок из всплывающего меню (Сортировка резервов)
    private void showSortReserveTablePopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.sort_reserve_table_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.numReserveMenu:
                        // здесь будет сортировка по резервам
                        sortTableName = DBHelper.KEY_ID;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.numTableMenu:
                        sortTableName = DBHelper.KEY_NUM_TABLE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.dateReserveMenu:
                        sortTableName = DBHelper.KEY_RESERVE_DATE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.durationMenu:
                        sortTableName = DBHelper.KEY_DURATION;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.clientMenu:
                        sortTableName = DBHelper.KEY_CLIENT;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.employeeMenu:
                        sortTableName = DBHelper.KEY_EMPLOYEE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.dateOrderMenu:
                        sortTableName = DBHelper.KEY_ORDER_DATE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    default: {
                        return false;
                    }
                }
            }
        });
        popupMenu.show();
    }

    // метод обрабатывает нажатия кнопок из всплывающего меню (Фильтр резервов)
    private void showFilterReserveTablePopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.filter_reserve_table_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.numTableMenu:
                        // здесь будет фильтр по столам
                        // Нужно сначала реализовать "выбор стола" а затем по выбранному номеру фильтровать
                        openDialogChoseTable();
                        return true;
                    case R.id.dateReserveMenu:
                        sortTableName = DBHelper.KEY_RESERVE_DATE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    case R.id.clientMenu:
                        initClients();
                        Log.i("Gas", "clientsList = " + clientsList);
                        openDialogActvChose(clientsList, "Клиент");
                        return true;
                    case R.id.employeeMenu:
                        initAdmins();
                        Log.i("Gas", "adminsList = " + adminsList);
                        openDialogActvChose(adminsList, "Администратор");
                        return true;

                    case R.id.dateOrderMenu:

                        sortTableName = DBHelper.KEY_ORDER_DATE;
                        editBtnColumns(columnsArr, null, null, null, null, sortTableName);
                        return true;
                    default: {
                        return false;
                    }
                }
            }
        });
        popupMenu.show();
    }

    // метод обрабатывает нажатия кнопок из всплывающего меню (Сортировка клиентов)
    private void showSortClientPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.sort_client_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nameMenu:
                        // здесь будет сортировка по клиентам
                        sortTableName = DBHelper.KEY_NAME;
                        editBtnColumns(null, null, null, null, null, sortTableName);
                        return true;
                    case R.id.ordersCountMenu:
                        sortTableName = DBHelper.KEY_ORDERS_COUNT + " DESC";
                        editBtnColumns(null, null, null, null, null, sortTableName);
                        return true;
                    case R.id.durationSumMinuteMenu:
                        sortTableName = DBHelper.KEY_DURATION_SUM_MINUTE + " DESC";
                        editBtnColumns(null, null, null, null, null, sortTableName);
                        return true;
                    default: {
                        return false;
                    }
                }
            }
        });
        popupMenu.show();
    }

    // метод обрабатывает нажатия кнопок из всплывающего меню (Фильтр резервов)
    private void showFilterClientPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.filter_client_popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nameMenu:
                        initClients();
                        openDialogActvChose(clientsList, "Table Client Name");
                        return true;
                    default: {
                        return false;
                    }
                }
            }
        });
        popupMenu.show();
    }




    // метод вызывает диалоговое окно выбора стола (для фильтации)
    public void openDialogChoseTable() {
        final Dialog dialog = new Dialog(EditDBActivity.this);
        dialog.setContentView(R.layout.dialog_number_picker);
        Button btnSet = (Button) dialog.findViewById(R.id.btnSet);
        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker);

//        numberPicker.setOnValueChangedListener(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(19);
        numberPicker.setWrapSelectorWheel(false);

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Тут будут дейтсвия при выборе продлжительности (нажатии кнопки выбрать);
                selection = DBHelper.KEY_NUM_TABLE + " = " + numberPicker.getValue();
                editBtnColumns(columnsArr, selection, null, null, null, null);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // метод вызывает диалоговое окно изменения резерва
    private void openDialogChangeReserve() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_num, null);
        final EditText etNum = (EditText) subView.findViewById(R.id.etNum);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение резерва\n")
                .setMessage("Введите порядковый номер резерва для изменения")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberRowDB = Integer.parseInt(etNum.getText().toString());
                        // сначала по номеру резерва узнаем все данные резерва
                        takeDataChangeOrder();
                        if (findReserveFlag) {
                            Intent intent = new Intent("newOrderActivity");
                            intent.putExtra("whoCall", "editDBActivity_Correct");
                            intent.putExtra("numReserve", numberRowDB);
                            intent.putExtra("numTable", numTableDB);
                            intent.putExtra("type", typeDB);
                            intent.putExtra("date", dateDB);
                            intent.putExtra("time", timeDB);
                            intent.putExtra("duration", durationMinuteDB);
                            intent.putExtra("client", clientDB);
                            intent.putExtra("adminName", getAdminName);
                            intent.putExtra("status", statusDB);
                            intent.putExtra("bron", bronDB);
                            startActivity(intent);

                            editBtnColumns(null, null, null,
                                    null, null, DBHelper.KEY_ID);
                        } else
                            Toast.makeText(EditDBActivity.this, "Этого резерва нет БД", Toast.LENGTH_SHORT).show();


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно изменения стола
    private void openDialogChangeTable() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_change_table, null);
        final EditText etNumTable = (EditText) subView.findViewById(R.id.etNum);
        SwitchCompat switchTypeTable = subView.findViewById(R.id.switchTypeTable);
        if (switchTypeTable != null) {
            Log.i("Gas", "мы еще не нажали на свитч");
            switchTypeTable.setOnCheckedChangeListener(EditDBActivity.this);
        }
        tvPyramid = subView.findViewById(R.id.tvPyramid);
        tvPool = subView.findViewById(R.id.tvPool);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение стола\n")
                .setMessage("Введите номер стола для изменения")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numTableDB = Integer.parseInt(etNumTable.getText().toString());
                        // у нас только 19 столов
                        if (numTableDB >= 1 && numTableDB < 20) {
                            // то вызываем изменение таблицы "столы"
                            Log.i("Gas", "numTableDB = " + numTableDB);
                            Log.i("Gas", "choseTypeTable = " + choseTypeTable);

                            optionalClass.changeTableInDB(EditDBActivity.this, numTableDB, choseTypeTable);

                            editBtnColumns(null, null, null,
                                    null, null, DBHelper.KEY_ID);
                        } else
                            Toast.makeText(EditDBActivity.this, "Этого стола нет в БД", Toast.LENGTH_SHORT).show();


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для выбора порядкового номера клиента (изменение клиента)
    private void openDialogTakeDataChangeClient() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_num, null);
        final EditText etNum = (EditText) subView.findViewById(R.id.etNum);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение клиента\n")
                .setMessage("Введите порядковый номер клиента для изменения")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberRowDB = Integer.parseInt(etNum.getText().toString());
                        // сначала по номеру резерва узнаем все данные резерва
                        takeDataChangeClient();
                        if (findClientFlag) {
                            openDialogChangeClient();
                        } else
                            Toast.makeText(EditDBActivity.this, "Этого клиента нет БД", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для выбора порядкового номера администратора (изменение администратора)
    private void openDialogTakeDataChangeEmployee() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_num, null);
        final EditText etNum = (EditText) subView.findViewById(R.id.etNum);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение администратора\n")
                .setMessage("Введите порядковый номер администратора для изменения")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberRowDB = Integer.parseInt(etNum.getText().toString());
                        // сначала по номеру резерва узнаем все данные резерва
                        takeDataChangeEmployee();
                        if (findEmployeeFlag) {
                            openDialogChangeEmployee();
                        } else
                            Toast.makeText(EditDBActivity.this, "Этого администратора нет БД", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для удаления (удаление)
    private void openDialogDelete(String whoCallMe) {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_num, null);
        final EditText etNum = (EditText) subView.findViewById(R.id.etNum);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(subView);
        if (whoCallMe.equals("Резервы")) {
            builder.setTitle("Удаление резерва\n")
                    .setMessage("Введите порядковый номер резерва для удаления");
        } else if (whoCallMe.equals("Клиенты")) {
            builder.setTitle("Удаление клиента\n")
                    .setMessage("Введите порядковый номер клиента для удаления");
        } else if (whoCallMe.equals("Сотрудники")) {
            builder.setTitle("Удаление администратора\n")
                    .setMessage("Введите порядковый номер администратора для удаления");
        } else if (whoCallMe.equals("Тарифы")) {
            builder.setTitle("Удаление Тарифа\n")
                    .setMessage("Введите порядковый номер тарифа для удаления");
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberRowDB = Integer.parseInt(etNum.getText().toString());
                        if (whoCallMe.equals("Резервы")) {
                            takeDataChangeOrder();
                            if (findReserveFlag) {
                                database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + numberRowDB, null);

                                Toast.makeText(EditDBActivity.this, "Резерв " + numberRowDB + " удален", Toast.LENGTH_LONG).show();
                                editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
                            } else
                                Toast.makeText(EditDBActivity.this, "В БД не найден!", Toast.LENGTH_SHORT).show();
                        } else if (whoCallMe.equals("Клиенты")) {
                            takeDataChangeClient();
                            if (findClientFlag) {
                                database.delete(DBHelper.CLIENTS, DBHelper.KEY_ID + "=" + numberRowDB, null);
                                Toast.makeText(EditDBActivity.this, "Клиент " + numberRowDB + " удален", Toast.LENGTH_LONG).show();
                                editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
                            } else
                                Toast.makeText(EditDBActivity.this, "Клиент В БД не найден!", Toast.LENGTH_SHORT).show();
                        } else if (whoCallMe.equals("Сотрудники")) {
                            takeDataChangeEmployee();
                            if (findEmployeeFlag) {
                                database.delete(DBHelper.EMPLOYEES, DBHelper.KEY_ID + "=" + numberRowDB, null);
                                Toast.makeText(EditDBActivity.this, "Администратор " + numberRowDB + " удален", Toast.LENGTH_LONG).show();
                                editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
                            } else
                                Toast.makeText(EditDBActivity.this, "Администратор В БД не найден!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для выбора из ACTV (фильтрация)
    private void openDialogActvChose(List<String> list, String whoCallMe) {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_actv, null);
        actvChose = (AutoCompleteTextView) subView.findViewById(R.id.actvChose);
        Log.i("Gas", "list = " + list);
        ArrayAdapter<String> adapterChose = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, list);
        actvChose.setThreshold(1);
        actvChose.setAdapter(adapterChose);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (whoCallMe.equals("Тариф")) {
            builder.setTitle("Выбор тарифа\n");
            builder.setMessage("Выберите тариф для фильтрации");
        } else if (whoCallMe.equals("Клиент") || whoCallMe.equals("Table Client Name")) {
            builder.setTitle("Выбор клиента\n");
            builder.setMessage("Выберите клиента для фильтрации");
        } else if (whoCallMe.equals("Администратор") || whoCallMe.equals("Table Employee Name")) {
            builder.setTitle("Выбор Администратора\n");
            builder.setMessage("Выберите Администратора для фильтрации");
        }
        builder.setView(subView)
                .setPositiveButton("Выбрать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (actvChose.getText().toString().equals("")) {
                            Toast.makeText(EditDBActivity.this, "Ничего не выбрано", Toast.LENGTH_SHORT).show();
                        } else {
                            choseStr = actvChose.getText().toString();
                            Log.i("Gas", "choseStr = " + choseStr);
                            Log.i("Gas", "TABLE_DB = " + TABLE_DB);

                            if (whoCallMe.equals("Клиент")) {
                                selection = DBHelper.KEY_CLIENT + " = \'" + choseStr + "\'";
                            } else if (whoCallMe.equals("Администратор")) {
                                selection = DBHelper.KEY_EMPLOYEE + " = \'" + choseStr + "\'";
                            } else if (whoCallMe.equals("Table Client Name") || whoCallMe.equals("Table Employee Name")) {
                                // если вызвали с таблицы "Клиенты" или "Администраторы"
                                selection = DBHelper.KEY_NAME + " = \'" + choseStr + "\'";
                            }
                            Log.i("Gas", "selection: " + selection);
                            editBtnColumns(null, selection, null, null, null, null);
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для редактирования клиента (редактирование клиента)
    private void openDialogChangeClient() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_create_client_or_employee, null);
        final EditText etName = (EditText) subView.findViewById(R.id.etName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);
        etName.setText(nameDB);
        etPhone.setText(phoneDB);

        AlertDialog.Builder builder = new AlertDialog.Builder(EditDBActivity.this);
        builder.setTitle("Изменение клиента\n")
                .setMessage("Измените данные клиента")
                .setView(subView)
                .setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nameDB = etName.getText().toString();
                        phoneDB = etPhone.getText().toString();

                        if (nameDB.equals("")) {
                            Toast.makeText(EditDBActivity.this, "Введите имя клиента", Toast.LENGTH_SHORT).show();
                            etName.setHintTextColor(Color.RED);
                        } else {
                            putChangeClientInDB();
                            Toast.makeText(EditDBActivity.this, "Клиент добавлен", Toast.LENGTH_SHORT).show();

                            // чтобы БД клиентов сразу обновилась
                            Intent intent = new Intent(EditDBActivity.this, EditDBActivity.class);
                            // передаем название заголовка
                            intent.putExtra("headName", "Клиенты");
                            intent.putExtra("adminName", getAdminName);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // метод вызывает диалоговое окно для редактирования администратора (редактирование администратора)
    private void openDialogChangeEmployee() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_create_client_or_employee, null);
        final EditText etName = (EditText) subView.findViewById(R.id.etName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);
        etName.setText(nameDB);
        etPhone.setText(phoneDB);

        AlertDialog.Builder builder = new AlertDialog.Builder(EditDBActivity.this);
        builder.setTitle("Изменение администратора\n")
                .setMessage("Измените данные администратора")
                .setView(subView)
                .setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nameDB = etName.getText().toString();
                        phoneDB = etPhone.getText().toString();

                        if (nameDB.equals("")) {
                            Toast.makeText(EditDBActivity.this, "Введите имя администратора", Toast.LENGTH_SHORT).show();
                            etName.setHintTextColor(Color.RED);
                        } else {
                            putChangeEmployeeInDB();
                            Toast.makeText(EditDBActivity.this, "Администратор добавлен", Toast.LENGTH_SHORT).show();

                            // чтобы БД клиентов сразу обновилась
                            Intent intent = new Intent(EditDBActivity.this, EditDBActivity.class);
                            // передаем название заголовка
                            intent.putExtra("headName", "Сотрудники");
                            intent.putExtra("adminName", getAdminName);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EditDBActivity.this, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



    // метод заменяет данные Клиента в БД (редактирование клиента)
    private void putChangeClientInDB() {
        database.delete(DBHelper.CLIENTS, DBHelper.KEY_ID + "=" + numberRowDB, null);
        contentValues.put(DBHelper.KEY_ID, numberRowDB);
        contentValues.put(DBHelper.KEY_NAME, nameDB);
        contentValues.put(DBHelper.KEY_PHONE, phoneDB);
        contentValues.put(DBHelper.KEY_ORDERS_COUNT, ordersCountDB);
        contentValues.put(DBHelper.KEY_DURATION_SUM_MINUTE, durationSumMinuteDB);

        database.insert(DBHelper.CLIENTS, null, contentValues);
    }

    // метод заменяет данные администратора в БД (редактирование Администратора)
    private void putChangeEmployeeInDB() {
        database.delete(DBHelper.EMPLOYEES, DBHelper.KEY_ID + "=" + numberRowDB, null);
        contentValues.put(DBHelper.KEY_ID, numberRowDB);
        contentValues.put(DBHelper.KEY_NAME, nameDB);
        contentValues.put(DBHelper.KEY_PHONE, phoneDB);

        database.insert(DBHelper.EMPLOYEES, null, contentValues);
    }

    private void initClients() {
        Log.i("EditDBActivity", "\n --- /// ---   Method initClients");
        for (int i = 0; i < allClientsList.size(); i++) {
            clientsList.add(allClientsList.get(i).getName());
        }
        Log.i("EditDBActivity", "clientsList: " + clientsList);
    }

    private void initAdmins() {
        Log.i("EditDBActivity", "\n --- /// ---   Method initAdmins");
        for (int i = 0; i < allAdminsList.size(); i++) {
            adminsList.add(allAdminsList.get(i).getName());
        }
        Log.i("EditDBActivity", "adminsList: " + adminsList);
    }
}