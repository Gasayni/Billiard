package com.gas.billiard;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditDBActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass option = new OptionallyClass();
    LinearLayout linColumns;
    Button btnColumns, btnAdd, btnDelete, btnChange;
    TextView tvHead, tvData;
    private List<String> nameBtnColumns = new ArrayList<>();
    private List<Button> btnColumnsTagsList = new ArrayList<>();
    private int marginLength, numReserveDB, numTableDB, durationMinuteDB;
    private String typeDB, dateDB, timeDB, clientDB;
    List<Button> getBtnColumnsTagsList = new ArrayList<>();
    boolean findReserveFlag;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTABLE_DB, cursorOrders, cursorTables;
    private String TABLE_DB;

    String sortTableName;
    int getFilterNumTable;
    String[] columnsArr  = {DBHelper.KEY_ID, DBHelper.KEY_NUM_TABLE, DBHelper.KEY_RESERVE_DATE,
            DBHelper.KEY_RESERVE_TIME, DBHelper.KEY_DURATION, DBHelper.KEY_TARIFF,
            DBHelper.KEY_CLIENT, DBHelper.KEY_EMPLOYEE, DBHelper.KEY_ORDER_DATE, DBHelper.KEY_ORDER_TIME};

    /*final Calendar currentDateCalendar = Calendar.getInstance();
    String currentMonthSt, currentDaySt, hourReserveSt, minuteReserveSt;
    // задаем начальное значение для выбора времени (не важно какие)
    int currentHour = currentDateCalendar.get(Calendar.HOUR_OF_DAY);
    int currentMinute = currentDateCalendar.get(Calendar.MINUTE);
    int currentYear = currentDateCalendar.get(Calendar.YEAR);*/


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

        addBtnColumns();

        Log.i("Gas6", "getFilterNumTable = " + getFilterNumTable);
        // здесь будет фильтр по столам, если фильтр вызвали
        if (getFilterNumTable == -1) {
            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
        } else {
            String selection = DBHelper.KEY_NUM_TABLE + " = " + getFilterNumTable;
            editBtnColumns(columnsArr, selection, null,
                    null, null, null);
        }


        if (tvHead.getText().toString().equals("Столы")) {
            Log.i("Gas5", "Столы столы");
            btnAdd.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
        }
    }

    public void addBtnColumns() {
        // сначала отрисуем кнопки шапки часов
        marginLength = option.convertDpToPixels(this, 2);

        linColumns = findViewById(R.id.linHead);

        switch (tvHead.getText().toString()) {
            case "Тарифы": {
                TABLE_DB = DBHelper.RATES;
                nameBtnColumns.add("Цена / ч");
                nameBtnColumns.add("Тип скидки");
                break;
            }
            case "Столы": {
                TABLE_DB = DBHelper.TABLES;
                nameBtnColumns.add("Стол");
                nameBtnColumns.add("Тип игры");
                break;
            }
            case "Сотрудники": {
                TABLE_DB = DBHelper.EMPLOYEES;
                nameBtnColumns.add("ФИО");
                nameBtnColumns.add("Рейтинг");
                break;
            }
            case "Клиенты": {
                TABLE_DB = DBHelper.CLIENTS;
                nameBtnColumns.add("ФИО");
                nameBtnColumns.add("Заказы");
                nameBtnColumns.add("Сумма");
                nameBtnColumns.add("Рейтинг");
                break;
            }
            case "Резервы": {
                TABLE_DB = DBHelper.ORDERS;
                nameBtnColumns.add("Резерв");
                nameBtnColumns.add("Стол");
                nameBtnColumns.add("Дата резерва");
                nameBtnColumns.add("Продолжительность");
                nameBtnColumns.add("Клиент");
                nameBtnColumns.add("Администратор");
                nameBtnColumns.add("Дата заказа");
                nameBtnColumns.add("Тариф");
                break;
            }
        }

        for (int i = 0; i < nameBtnColumns.size(); i++) {
            linColumns = findViewById(R.id.linColumns);

            btnColumns = new Button(linColumns.getContext());
            btnColumns.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            btnColumns.setTag("btnColumns" + (i + 1));
            getBtnColumnsTagsList.add(btnColumns);
            Log.i("Gas", "btnColumns.getTag()" + btnColumns.getTag());
            btnColumns.setText(nameBtnColumns.get(i));
            btnColumnsTagsList.add(btnColumns);
            btnColumns.setTextSize(12);
            btnColumns.setTextColor(Color.BLACK);
            btnColumns.setBackgroundResource(R.drawable.btn_style_2);

            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnColumns.getLayoutParams();
            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linColumns.addView(btnColumns);
            onClickSortMethod(btnColumns);
        }

    }

    private void onClickSortMethod(Button btnColumns) {
        btnColumns.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String selection = null;
                // здесь будет сортировка в зависимости от нажатой кнопки
                switch (tvHead.getText().toString()) {
                    case "Тарифы": {
                        Log.i("Gas", "tvHead.getText().toString()" + tvHead.getText().toString());
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            Log.i("Gas", "btnColumns.getTag()" + btnColumns.getTag());
                            sortTableName = DBHelper.KEY_PRICE;
                            Log.i("Gas", "sortTableName = " + sortTableName);
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            sortTableName = DBHelper.KEY_TARIFF;
                        }
                        break;
                    }
                    case "Столы": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            sortTableName = DBHelper.KEY_ID;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            sortTableName = DBHelper.KEY_TYPE;
                        }
                        break;
                    }
                    case "Сотрудники": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            sortTableName = DBHelper.KEY_SECOND_NAME;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            sortTableName = DBHelper.KEY_RATING;
                        }
                        break;
                    }
                    case "Клиенты": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            sortTableName = DBHelper.KEY_SECOND_NAME;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            sortTableName = DBHelper.KEY_ORDERS_COUNT;
                        } else if (btnColumns.getTag().equals("btnColumns3")) {
                            sortTableName = DBHelper.KEY_SPENT;
                        } else if (btnColumns.getTag().equals("btnColumns4")) {
                            sortTableName = DBHelper.KEY_RATING;
                        }
                        break;
                    }
                    case "Резервы": {
                        if (btnColumns.getTag().equals("btnColumns1")) {
                            // Кнопка "Резерв"
                            sortTableName = DBHelper.KEY_ID;
                        } else if (btnColumns.getTag().equals("btnColumns2")) {
                            // Кнопка "Стол"
                            selection = DBHelper.KEY_NUM_TABLE + " = " + getFilterNumTable;
                            sortTableName = DBHelper.KEY_NUM_TABLE;
                                    Здесь нужно сначала реализовать выбор стола а затем по выбранному номеру фильтровать
                        } else if (btnColumns.getTag().equals("btnColumns3")) {
                            // Кнопка "Дата резерва"
                            sortTableName = DBHelper.KEY_RESERVE_DATE;
                        } else if (btnColumns.getTag().equals("btnColumns4")) {
                            // Кнопка "Продолжительность"
                            sortTableName = DBHelper.KEY_DURATION;
                        } else if (btnColumns.getTag().equals("btnColumns5")) {
                            // Кнопка "Клиент"
                            sortTableName = DBHelper.KEY_CLIENT;
                        } else if (btnColumns.getTag().equals("btnColumns6")) {
                            // Кнопка "Администратор"
                            sortTableName = DBHelper.KEY_EMPLOYEE;
                        } else if (btnColumns.getTag().equals("btnColumns7")) {
                            // Кнопка "Дата заказа"
                            sortTableName = DBHelper.KEY_ORDER_DATE;
                        } else if (btnColumns.getTag().equals("btnColumns8")) {
                            // Кнопка "Тариф"
                            sortTableName = DBHelper.KEY_TARIFF;
                        }
                        break;
                    }
                }
                Log.i("Gas", "sortTableName = " + sortTableName);
                editBtnColumns(columnsArr, selection, null,
                        null, null, sortTableName);
            }
        });
    }

    public void editBtnColumns(String[] column, String selection, String[] selectionArgs,
                               String groupBy, String having, String orderBy) {
        // очищаем сначала
        tvData.setText("");

        int idIndex = 0, priceIndex = 0, ratingIndex = 0, spentIndex = 0, ordersIndex = 0, phoneIndex = 0,
                secondNameIndex = 0, firstNameIndex = 0, rateIndex = 0, numTableIndex = 0, reserveDateIndex = 0,
                reserveTimeIndex = 0, durationIndex = 0, clientIndex = 0, employeeIndex = 0, dateOrderIndex = 0,
                timeOrderIndex = 0, typeIndex = 0, descriptionIndex = 0;

        // получаем данные c табл выбранной таблицы
        cursorTABLE_DB = database.query(TABLE_DB, column, selection, selectionArgs, groupBy, having, orderBy);
        if (cursorTABLE_DB.moveToFirst()) {
            idIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ID);
            switch (tvHead.getText().toString()) {
                // здесь мы инициализируем
                case "Тарифы": {
                    priceIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_PRICE);
                    rateIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_TARIFF);
                    break;
                }
                case "Столы": {
                    typeIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_TYPE);
                    break;
                }
                case "Сотрудники": {
                    firstNameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_FIRST_NAME);
                    secondNameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_SECOND_NAME);
                    phoneIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_PHONE);
                    ratingIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_RATING);
                    descriptionIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_DESCRIPTION);
                    break;
                }
                case "Клиенты": {
                    firstNameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_FIRST_NAME);
                    secondNameIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_SECOND_NAME);
                    phoneIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_PHONE);
                    ordersIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
                    spentIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_SPENT);
                    ratingIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_RATING);
                    descriptionIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_DESCRIPTION);
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
                    rateIndex = cursorTABLE_DB.getColumnIndex(DBHelper.KEY_TARIFF);
                    break;
                }
            }
            do {
                // записали все, что было до
                StringBuilder s = new StringBuilder(tvData.getText().toString());
                // здесь мы выводим все
                switch (tvHead.getText().toString()) {
                    case "Тарифы": {
                        s.append("\n" + cursorTABLE_DB.getInt(idIndex))
                                .append(".\t\t" + cursorTABLE_DB.getInt(priceIndex))
                                .append("\t\t" + cursorTABLE_DB.getString(rateIndex));
                        break;
                    }
                    case "Столы": {
                        s.append("\n" + cursorTABLE_DB.getInt(idIndex))
                                .append(".\t\t" + cursorTABLE_DB.getString(typeIndex));
                        break;
                    }
                    case "Сотрудники": {
                        s.append("\n" + cursorTABLE_DB.getInt(idIndex))
                                .append(".\t\t\t\t" + cursorTABLE_DB.getString(secondNameIndex))
                                .append("\t" + cursorTABLE_DB.getString(firstNameIndex))
                                .append("\t\t\t\t" + cursorTABLE_DB.getString(phoneIndex))
                                .append("\t\t\t\tРейтинг: " + cursorTABLE_DB.getInt(ratingIndex));
//                                .append("\t\tРейтинг " + cursorTableDb.getInt(ratingIndex))
//                                .append("\t\t(" + cursorTableDb.getString(descriptionIndex) + ")")
                        break;
                    }
                    case "Клиенты": {
                        s.append("\n" + cursorTABLE_DB.getInt(idIndex))
                                .append(".\t\t\t\t" + cursorTABLE_DB.getString(secondNameIndex))
                                .append("\t" + cursorTABLE_DB.getString(firstNameIndex))
                                .append("\t\t\t\t" + cursorTABLE_DB.getString(phoneIndex))
                                .append("\t\t\t\tЗаказов: " + cursorTABLE_DB.getInt(ordersIndex))
                                .append("\t\t\t\tПотратил: " + cursorTABLE_DB.getInt(spentIndex))
                                .append("\t\t\t\tРейтинг: " + cursorTABLE_DB.getInt(ratingIndex));
//                                .append("\t\tРейтинг: " + cursorTableDb.getInt(ratingIndex))
//                                .append("\t\t(" + cursorTableDb.getString(descriptionIndex) + ")")
                        break;
                    }
                    case "Резервы": {
                        s.append("\n" + cursorTABLE_DB.getInt(idIndex))
                                .append(".\t\t\tСтол № " + cursorTABLE_DB.getInt(numTableIndex))
                                .append("\t\t\t\t\tРезерв на: " + cursorTABLE_DB.getString(reserveDateIndex))
                                .append("\t" + cursorTABLE_DB.getString(reserveTimeIndex))
                                .append("\t\t\t\t\t" + cursorTABLE_DB.getInt(durationIndex))
                                .append("\t\t\t\t\tТариф: " + cursorTABLE_DB.getString(rateIndex))
                                .append("\t\t\t\t\tКлиент: " + cursorTABLE_DB.getString(clientIndex))
                                .append("\t\t\t\t\tОформил: " + cursorTABLE_DB.getString(employeeIndex))
                                .append("\t\t" + cursorTABLE_DB.getString(dateOrderIndex))
                                .append("\t" + cursorTABLE_DB.getString(timeOrderIndex));
                        break;
                    }
                }
                tvData.setText(s);
                tvData.setTextColor(Color.BLACK);

            } while (cursorTABLE_DB.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTABLE_DB.close();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnAdd: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    intent = new Intent("newOrderActivity");
                    startActivity(intent);
                }
                break;
            }
            case R.id.btnDelete: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    openDialogDeleteReserve();
                }
                break;
            }
            case R.id.btnChange: {
                if (tvHead.getText().toString().equals("Резервы")) {
                    openDialogChangeReserve(view);
                }
                break;
            }
        }
    }

    private void openDialogChangeReserve(View view) {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_reserve, null);
        final EditText etNumReserve = (EditText) subView.findViewById(R.id.etNumReserve);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение резерва\n")
                .setMessage("Введите порядковый номер резерва для изменения")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numReserveDB = Integer.parseInt(etNumReserve.getText().toString());
                        // сначала по номеру резерва узнаем все данные резерва
                        takeDataChangeOrder();
                        if (findReserveFlag) {
                            Intent intent = new Intent("newOrderActivity");
                            intent.putExtra("numReserve", numReserveDB);
                            intent.putExtra("numTable", numTableDB);
                            intent.putExtra("type", typeDB);
                            intent.putExtra("date", dateDB);
                            intent.putExtra("time", timeDB);
                            intent.putExtra("duration", durationMinuteDB);
                            intent.putExtra("client", clientDB);
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
            do {
                if (numReserveDB == cursorOrders.getInt(idIndex)) {
                    findReserveFlag = true;
                    numTableDB = cursorOrders.getInt(numTableIndex);
                    dateDB = cursorOrders.getString(reserveDateIndex);
                    timeDB = cursorOrders.getString(reserveTimeIndex);
                    durationMinuteDB = cursorOrders.getInt(durationIndex);
                    clientDB = cursorOrders.getString(clientIndex);
                }
            } while (cursorOrders.moveToNext());

            Log.i("Gas5", "numReserveDB = " + numReserveDB);
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
    }

    private void takeTypeTableMethod() {
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
    }

    private void openDialogDeleteReserve() {
        LayoutInflater inflater = LayoutInflater.from(EditDBActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_reserve, null);
        final EditText etNumReserve = (EditText) subView.findViewById(R.id.etNumReserve);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление резерва\n")
                .setMessage("Введите порядковый номер резерва для удаления")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numReserveDB = Integer.parseInt(etNumReserve.getText().toString());
                        takeDataChangeOrder();
                        if (findReserveFlag) {
                            database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + numReserveDB, null);
                            Toast.makeText(EditDBActivity.this, "Резерв " + numReserveDB + " удален.", Toast.LENGTH_LONG).show();
                            editBtnColumns(null, null, null, null, null, DBHelper.KEY_ID);
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
}