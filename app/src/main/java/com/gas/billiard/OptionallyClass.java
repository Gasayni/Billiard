package com.gas.billiard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OptionallyClass {
    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorEmployee, cursorTables, cursorOrders, cursorClients, cursorRates;
    int idDB, numTableDB, durationMinuteDB;
    String reserveDateDB, reserveTimeDB, clientDB, employeeDB, orderDateDB, orderTimeDB, tariffDB, descriptionDB, statusDB;

    List<String> adminsList = new ArrayList<>();
    List<String> passList = new ArrayList<>();

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    String currentDateStr = dateFormat.format(new Date()), currentTimeStr = timeFormat.format(new Date());
    Date reserveStartDateTime, reserveFinishDateTime;

    public int convertDpToPixels(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public List<String> initAdmins(Context context, String thatReturn) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // получаем данные c табл "EMPLOYEES"
        cursorEmployee = database.query(DBHelper.EMPLOYEES,
                null, null, null,
                null, null, null);
        if (cursorEmployee.moveToFirst()) {
            int nameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_NAME);
            int passIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PASS);
            do {
                // находим всех сотрудников из бд
                if (thatReturn.equals("adminsList")) {
                    adminsList.add(cursorEmployee.getString(nameIndex));
                } else passList.add(cursorEmployee.getString(passIndex));
            } while (cursorEmployee.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        if (thatReturn.equals("adminsList")) {
            return adminsList;
        } else return passList;
    }

    public List<String> initClient(Context context) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        List<String> clientsList = new ArrayList<>();

        // получаем данные c табл "EMPLOYEES"
        cursorClients = database.query(DBHelper.CLIENTS,
                null, null, null,
                null, null, null);
        if (cursorClients.moveToFirst()) {
            int nameIndex = cursorClients.getColumnIndex(DBHelper.KEY_NAME);
            int phoneIndex = cursorClients.getColumnIndex(DBHelper.KEY_PHONE);
            int ordersCountIndex = cursorClients.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
            int spentIndex = cursorClients.getColumnIndex(DBHelper.KEY_SPENT);
            int ratingIndex = cursorClients.getColumnIndex(DBHelper.KEY_RATING);
            int descriptionIndex = cursorClients.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // находим всех клиентов из бд
                clientsList.add(cursorClients.getString(nameIndex));
            } while (cursorClients.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        return clientsList;
    }

    public List<String> initTariff(Context context, String thatReturn) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        List<String> tariffList = new ArrayList<>();

        // получаем данные c табл "TARIFF"
        cursorRates = database.query(DBHelper.TARIFF,
                null, null, null,
                null, null, null);
        if (cursorRates.moveToFirst()) {
            int priceIndex = cursorRates.getColumnIndex(DBHelper.KEY_PRICE);
            int nameIndex = cursorRates.getColumnIndex(DBHelper.KEY_NAME);
            do {
                if (thatReturn.equals("tariffList")) {
                    tariffList.add(cursorRates.getString(nameIndex));
                }

            } while (cursorRates.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        return tariffList;
    }

    /*private void initTablesList() {
        btnNewReserveDate.setTextColor(Color.BLACK);
        btnNewReserveTime.setTextColor(Color.BLACK);
        typeNumTableList.clear();

        // получаем данные c табл "TABLES"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            do {
                if (choseTypeTable.equals("")) {  // если тип стола не выбран
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
    }*/ // initTablesList из newOrderActivity надеюсь получиться перенести


    // нужно проверить БД на устаревание
    // если дата и время уже прошла, (если не был статус удалить) то меняем статус на old
    public void checkOldReserve(Context context) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        Log.i("Gas1", "currentDateStr = " + currentDateStr);

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
            int tariffIndex = cursorOrders.getColumnIndex(DBHelper.KEY_TARIFF);
            int descriptionIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            int statusIndex = cursorOrders.getColumnIndex(DBHelper.KEY_STATUS);
            do {

                {
                    String reserveDate = cursorOrders.getString(reserveDateIndex);
                    String reserveStartTime = cursorOrders.getString(reserveTimeIndex);
                    try {
                        // из строки в Date
                        reserveStartDateTime = dateTimeFormat.parse(reserveDate + " " + reserveStartTime);
//                        Log.i("Gas1", "reserveStartDateTime = " + reserveStartDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(reserveStartDateTime);
                    cal.add(Calendar.MINUTE, cursorOrders.getInt(durationIndex));
                    String reserveFinishTimeStr = dateTimeFormat.format(cal.getTime());
                    try {
                        // из строки в Date
                        reserveFinishDateTime = dateTimeFormat.parse(reserveFinishTimeStr);
//                        Log.i("Gas1", "reserveStartDateTime = " + reserveStartDateTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                Log.i("Gas1", "index = " + cursorOrders.getInt(idIndex));
                Log.i("Gas1", "reserveStartDateTime = " + dateTimeFormat.format(reserveStartDateTime));
                Log.i("Gas1", "durationIndex = " + cursorOrders.getInt(durationIndex));
                Log.i("Gas1", "reserveFinishTime = " + dateTimeFormat.format(reserveFinishDateTime));
                Log.i("Gas1", "new Date() = " + dateTimeFormat.format(new Date()));
                Log.i("Gas1", "before? = " + reserveFinishDateTime.before(new Date()));

                if ((reserveFinishDateTime.before(new Date())) && (cursorOrders.getString(statusIndex).equals(""))) {
                    // если время уже прошло, то копируем все данные и меняем статус
                    Log.i("Gas1", "make old");
                    idDB = cursorOrders.getInt(idIndex);
                    numTableDB = cursorOrders.getInt(numTableIndex);
                    reserveDateDB = cursorOrders.getString(reserveDateIndex);
                    reserveTimeDB = cursorOrders.getString(reserveTimeIndex);
                    durationMinuteDB = cursorOrders.getInt(durationIndex);
                    clientDB = cursorOrders.getString(clientIndex);
                    employeeDB = cursorOrders.getString(employeeIndex);
                    orderDateDB = cursorOrders.getString(orderDateIndex);
                    orderTimeDB = cursorOrders.getString(orderTimeIndex);
                    tariffDB = cursorOrders.getString(tariffIndex);
                    descriptionDB = cursorOrders.getString(descriptionIndex);
                    statusDB = cursorOrders.getString(statusIndex);

                    changeReserveInDB();
                }
            } while (cursorOrders.moveToNext());
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
    }

    private void changeReserveInDB() {
        // если хотим изменить запись, то просто передает тудатот же номер строки
        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + idDB, null);
        contentValues.put(DBHelper.KEY_ID, idDB);
        contentValues.put(DBHelper.KEY_NUM_TABLE, numTableDB);
        contentValues.put(DBHelper.KEY_RESERVE_DATE, reserveDateDB);
        contentValues.put(DBHelper.KEY_RESERVE_TIME, reserveTimeDB);
        contentValues.put(DBHelper.KEY_DURATION, durationMinuteDB);
        contentValues.put(DBHelper.KEY_CLIENT, clientDB);
        contentValues.put(DBHelper.KEY_EMPLOYEE, employeeDB);
        contentValues.put(DBHelper.KEY_ORDER_DATE, orderDateDB);
        contentValues.put(DBHelper.KEY_ORDER_TIME, orderTimeDB);
        contentValues.put(DBHelper.KEY_TARIFF, tariffDB);
        contentValues.put(DBHelper.KEY_DESCRIPTION, descriptionDB);
        contentValues.put(DBHelper.KEY_STATUS, "Old");

        database.insert(DBHelper.ORDERS, null, contentValues);
    }
}
