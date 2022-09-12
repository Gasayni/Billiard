package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.Objects;

public class OptionallyClass {
    // БД
    SQLiteDatabase database;
    Cursor cursorEmployee, cursorOrders, cursorClients, cursorTables;

    static List<List<OrderClass>> allOrdersList = new ArrayList<>();
    static List<List<OrderClass>> allOrdersThisDayList = new ArrayList<>();
    static List<AdminClass> allAdminsList = new ArrayList<>();
    static List<ClientClass> allClientsList = new ArrayList<>();
    static List<TableClass> allTablesList = new ArrayList<>();
    List<OrderClass> oldReserveList = new ArrayList<>();

    //    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    Date reserveFinishDateTime;




    public List<List<OrderClass>> findAllOrders(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllOrders");
        if (allOrdersList.isEmpty() || reFind) { // если мы не инициализировали этот лист
            Log.i("OptionalClass", "True allReservesList.isEmpty() || reFind = ");
            // работа с БД
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase database = dbHelper.getWritableDatabase();

            int tableCount = 19;
            // создаем лист всех заказов по столам
            allOrdersList = new ArrayList<>();
            for (int i = 0; i < tableCount; i++) {
                List<OrderClass> tableOrdersList = new ArrayList<>();
                allOrdersList.add(tableOrdersList);
            }

            // String selection = "NOT " + DBHelper.KEY_STATUS + " = \'deleted\'";
            cursorOrders = database.query(DBHelper.ORDERS,
                    null, null, null,
                    null, null, null);
            if (cursorOrders.moveToFirst()) {
                int idIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ID);
                int numTableIndex = cursorOrders.getColumnIndex(DBHelper.KEY_NUM_TABLE);
                int reserveDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_DATE);
                int reserveTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_TIME);
                int dateOrderIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
                int timeOrderIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
                int durationIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DURATION);
                int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
                int employeeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_EMPLOYEE);
                int bronIndex = cursorOrders.getColumnIndex(DBHelper.KEY_BRON);
                int statusIndex = cursorOrders.getColumnIndex(DBHelper.KEY_STATUS);
                do {
                    Log.i("OptionalClass", "\ti: " + cursorOrders.getString(idIndex));

                        int numTable = cursorOrders.getInt(numTableIndex);

                        OrderClass orderClass = new OrderClass(
                                cursorOrders.getInt(idIndex),
                                cursorOrders.getInt(numTableIndex),
                                cursorOrders.getString(reserveDateIndex),
                                cursorOrders.getString(reserveTimeIndex),
                                cursorOrders.getInt(durationIndex),
                                cursorOrders.getString(dateOrderIndex),
                                cursorOrders.getString(timeOrderIndex),
                                cursorOrders.getString(clientIndex),
                                cursorOrders.getString(employeeIndex),
                                cursorOrders.getString(bronIndex),
                                cursorOrders.getString(statusIndex));

                        // каждый стол добавляем в список
                        allOrdersList.get(numTable - 1).add(orderClass);
                        Log.i("OptionalClass", "\t\t\tallTablesList.get(" + numTable + ") = " +
                                allOrdersList.get(numTable - 1).size());
                } while (cursorOrders.moveToNext());
            } else {
                Log.d("Gas_OptionalClass", "0 rows");
            }
            cursorOrders.close();
            dbHelper.close();
        } else Log.i("OptionalClass", "False allOrdersList.isEmpty() || reFind");
        return allOrdersList;
    }

    public List<List<OrderClass>> findAllOrdersThisDay(Context context, String date, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllOrders");
        if (allOrdersThisDayList.isEmpty() || reFind) { // если мы не инициализировали этот лист
            Log.i("OptionalClass", "True allReservesList.isEmpty() || reFind = ");
            // работа с БД
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase database = dbHelper.getWritableDatabase();

            String dateReserveTomorrow = tomorrowDateReserveMethod(date);
            Log.i("OptionalClass", "\tbtn Today dateReserve " + date);
            Log.i("OptionalClass", "\tTomorrow dateReserve = " + dateReserveTomorrow);

            int tableCount = 19;
            // создаем лист всех заказов по столам
            allOrdersThisDayList = new ArrayList<>();
            for (int i = 0; i < tableCount; i++) {
                List<OrderClass> tableOrdersList = new ArrayList<>();
                allOrdersThisDayList.add(tableOrdersList);
            }

            // String selection = "NOT " + DBHelper.KEY_STATUS + " = \'deleted\'";
            cursorOrders = database.query(DBHelper.ORDERS,
                    null, null, null,
                    null, null, null);
            if (cursorOrders.moveToFirst()) {
                int idIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ID);
                int numTableIndex = cursorOrders.getColumnIndex(DBHelper.KEY_NUM_TABLE);
                int reserveDateIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_DATE);
                int reserveTimeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_RESERVE_TIME);
                int dateOrderIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_DATE);
                int timeOrderIndex = cursorOrders.getColumnIndex(DBHelper.KEY_ORDER_TIME);
                int durationIndex = cursorOrders.getColumnIndex(DBHelper.KEY_DURATION);
                int clientIndex = cursorOrders.getColumnIndex(DBHelper.KEY_CLIENT);
                int employeeIndex = cursorOrders.getColumnIndex(DBHelper.KEY_EMPLOYEE);
                int bronIndex = cursorOrders.getColumnIndex(DBHelper.KEY_BRON);
                int statusIndex = cursorOrders.getColumnIndex(DBHelper.KEY_STATUS);
                do {
                    Log.i("OptionalClass", "\ti: " + cursorOrders.getString(idIndex));
                    // находим кнопку по времени резерва, воспользуемся спец. методом
                    // коорината по столбцу
                    String[] hourAr = cursorOrders.getString(reserveTimeIndex).split(":");
                    int hour;
                    try {
                        hour = Integer.parseInt(hourAr[0]);
                    } catch (NumberFormatException e) {
                        hour = 0;
                    }

                    Log.i("OptionalClass", "\t\tcursorOrders Today dateReserve " + cursorOrders.getString(reserveDateIndex));
                    // если дата указанная в btnDate и если время с 11 по 23
                    if ((((cursorOrders.getString(reserveDateIndex).equals(date) && hour >= 11)))
                            // или если дата следующая после указанной(завтрашняя) в btnDate и если время с 0 по 4
                            || ((cursorOrders.getString(reserveDateIndex).equals(dateReserveTomorrow)) && (hour >= 0 && hour < 5))) {
                        Log.i("OptionalClass", "\t\tTrue: Условие даты");
                        int numTable = cursorOrders.getInt(numTableIndex);
                        Log.i("OptionalClass", "\t\t\tnumTable = " + numTable);

                        OrderClass orderClass = new OrderClass(
                                cursorOrders.getInt(idIndex),
                                cursorOrders.getInt(numTableIndex),
                                cursorOrders.getString(reserveDateIndex),
                                cursorOrders.getString(reserveTimeIndex),
                                cursorOrders.getInt(durationIndex),
                                cursorOrders.getString(dateOrderIndex),
                                cursorOrders.getString(timeOrderIndex),
                                cursorOrders.getString(clientIndex),
                                cursorOrders.getString(employeeIndex),
                                cursorOrders.getString(bronIndex),
                                cursorOrders.getString(statusIndex));

                        // каждый стол добавляем в список
                        allOrdersThisDayList.get(numTable - 1).add(orderClass);
                        Log.i("OptionalClass", "\t\t\tallTablesList.get(" + numTable + ") = " +
                                allOrdersThisDayList.get(numTable - 1).size());
                    }
                } while (cursorOrders.moveToNext());
            } else {
                Log.d("Gas_OptionalClass", "0 rows");
            }
            cursorOrders.close();
            dbHelper.close();
        } else Log.i("OptionalClass", "False allOrdersList.isEmpty() || reFind");
        return allOrdersThisDayList;
    }

    public void putReserveInDB(Context context, OrderClass order) {
        // работа с БД
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // если хотим изменить запись, то просто передает тудатот же номер строки
        if (order.getIdOrder() != -1) {
            database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + " = " + order.getIdOrder(), null);
            contentValues.put(DBHelper.KEY_ID, order.getIdOrder());
        }
        contentValues.put(DBHelper.KEY_NUM_TABLE, order.getNumTable());
        contentValues.put(DBHelper.KEY_RESERVE_DATE, order.getDateStartReserve());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, order.getTimeStartReserve());
        contentValues.put(DBHelper.KEY_DURATION, order.getDuration());
        contentValues.put(DBHelper.KEY_CLIENT, order.getClient());
        contentValues.put(DBHelper.KEY_EMPLOYEE, order.getEmployee());
        contentValues.put(DBHelper.KEY_ORDER_DATE, order.getDateOrder());
        contentValues.put(DBHelper.KEY_ORDER_TIME, order.getTimeOrder());

        if (order.getBron().equals("Бронь"))
            contentValues.put(DBHelper.KEY_BRON, "Бронь");
        else contentValues.put(DBHelper.KEY_BRON, "Без брони");

        contentValues.put(DBHelper.KEY_STATUS, order.getStatus());

        database.insert(DBHelper.ORDERS, null, contentValues);
        dbHelper.close();
    }

    // нужно проверить БД на устаревание
    // если дата и время уже прошла, (если не был статус удалить) то меняем статус на old
    public void checkOldReserve(Context context) {
        Log.i("OptionalClass", "\n ...//...    checkOldReserve");
        for (int i = 0; i < allOrdersList.size(); i++) {
            for (int j = 0; j < allOrdersList.get(i).size(); j++) {
                OrderClass orderClass = allOrdersList.get(i).get(j);

                reserveFinishDateTime = orderClass.getEndDateTimeReserveCal().getTime();
                if ((reserveFinishDateTime.before(new Date())) && (orderClass.getStatus().equals(""))) {
                    // если время уже прошло, то копируем все данные и меняем статус
                    oldReserveList.add(orderClass);
                }
            }
        }
        changeOldReserveInDB(context);
    }

    private void changeOldReserveInDB(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < oldReserveList.size(); i++) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            // если хотим изменить запись, то просто передает тудатот же номер строки
            database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + oldReserveList.get(i).getIdOrder(), null);

            contentValues.put(DBHelper.KEY_ID, oldReserveList.get(i).getIdOrder());
            contentValues.put(DBHelper.KEY_NUM_TABLE, oldReserveList.get(i).getNumTable());
            contentValues.put(DBHelper.KEY_RESERVE_DATE, oldReserveList.get(i).getDateStartReserve());
            contentValues.put(DBHelper.KEY_RESERVE_TIME, oldReserveList.get(i).getTimeStartReserve());
            contentValues.put(DBHelper.KEY_DURATION, oldReserveList.get(i).getDuration());
            contentValues.put(DBHelper.KEY_CLIENT, oldReserveList.get(i).getClient());
            contentValues.put(DBHelper.KEY_EMPLOYEE, oldReserveList.get(i).getEmployee());
            contentValues.put(DBHelper.KEY_ORDER_DATE, oldReserveList.get(i).getDateOrder());
            contentValues.put(DBHelper.KEY_ORDER_TIME, oldReserveList.get(i).getTimeOrder());
            contentValues.put(DBHelper.KEY_BRON, oldReserveList.get(i).getBron());
            contentValues.put(DBHelper.KEY_STATUS, "Old");

            database.insert(DBHelper.ORDERS, null, contentValues);
            dbHelper.close();
        }
    }

    public void deleteOrder(Context context, int numOrder) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();

        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + numOrder, null);

        dbHelper.close();
    }

    @SuppressLint("DefaultLocale")
    public void stopOrder(Context context, OrderClass table, String getAdminName) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        int newDuration = currentDurationCalc(table);

        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + table.getIdOrder(), null);

        contentValues.put(DBHelper.KEY_ID, table.getIdOrder());
        contentValues.put(DBHelper.KEY_NUM_TABLE, table.getNumTable());
        contentValues.put(DBHelper.KEY_RESERVE_DATE, table.getDateStartReserve());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, table.getTimeStartReserve());
        contentValues.put(DBHelper.KEY_DURATION, newDuration);
        Log.i("Gas6", "newDuration = " + newDuration);
        contentValues.put(DBHelper.KEY_CLIENT, table.getClient());
        contentValues.put(DBHelper.KEY_EMPLOYEE, getAdminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, table.getDateOrder());
        contentValues.put(DBHelper.KEY_ORDER_TIME, table.getTimeOrder());
        contentValues.put(DBHelper.KEY_BRON, table.getBron());
        contentValues.put(DBHelper.KEY_STATUS, "");

        database.insert(DBHelper.ORDERS, null, contentValues);
        dbHelper.close();
    }

    @SuppressLint("DefaultLocale")
    public void resumeOrder(Context context, OrderClass table, String getAdminName) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Log.i("Gas5", "table.getNumTable = " + table.getNumTable());

        int newDuration = table.getDuration() + 15;
        // проверяем, можно ли добавить
        newDuration = checkLeftEndWorkMinute(newDuration);


        database.delete(DBHelper.ORDERS, DBHelper.KEY_ID + "=" + table.getIdOrder(), null);

        contentValues.put(DBHelper.KEY_ID, table.getIdOrder());
        contentValues.put(DBHelper.KEY_NUM_TABLE, table.getNumTable());
        contentValues.put(DBHelper.KEY_RESERVE_DATE, table.getDateStartReserve());
        contentValues.put(DBHelper.KEY_RESERVE_TIME, table.getTimeStartReserve());
        contentValues.put(DBHelper.KEY_DURATION, newDuration);
        contentValues.put(DBHelper.KEY_CLIENT, table.getClient());
        contentValues.put(DBHelper.KEY_EMPLOYEE, getAdminName);
        contentValues.put(DBHelper.KEY_ORDER_DATE, table.getDateOrder());
        contentValues.put(DBHelper.KEY_ORDER_TIME, table.getTimeOrder());
        contentValues.put(DBHelper.KEY_BRON, table.getBron());
        contentValues.put(DBHelper.KEY_STATUS, "");

        database.insert(DBHelper.ORDERS, null, contentValues);
        dbHelper.close();
    }





    public List<AdminClass> findAllAdmins(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllAdmins");
        if (allAdminsList.isEmpty() || reFind) { // если мы не инициализировали этот лист
            DBHelper dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();

            // получаем данные c табл "EMPLOYEES"
            cursorEmployee = database.query(DBHelper.EMPLOYEES,
                    null, null, null,
                    null, null, null);
            if (cursorEmployee.moveToFirst()) {
                int idIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_ID);
                int nameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_NAME);
                int phoneIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PHONE);
                int passIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PASS);
                do {
                    allAdminsList.add(new AdminClass(
                            cursorEmployee.getInt(idIndex),
                            cursorEmployee.getString(nameIndex),
                            cursorEmployee.getString(phoneIndex),
                            cursorEmployee.getString(passIndex)));
                } while (cursorEmployee.moveToNext());
            } else {
                // если не задан ни один сотрудник, то м. перейти в настройки его создания
                Log.d("OptionalClass", "0 rows");
            }
            cursorEmployee.close();
            dbHelper.close();
        }
        Log.i("OptionalClass", "False allAdminsList.isEmpty() || reFind");
        return allAdminsList;
    }

    public void putEmployeeInDB(Context context, String nameNewEmployee, String phoneNewEmployee) {
        // работа с БД
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME, nameNewEmployee);
        contentValues.put(DBHelper.KEY_PHONE, phoneNewEmployee);

        database.insert(DBHelper.EMPLOYEES, null, contentValues);
        dbHelper.close();
    }

    // метод заменяет данные администратора в БД (редактирование Администратора)
    public void putChangeEmployeeInDB(Context context, AdminClass admin) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        database.delete(DBHelper.EMPLOYEES, DBHelper.KEY_ID + "=" + admin.getId(), null);
        contentValues.put(DBHelper.KEY_ID, admin.getId());
        contentValues.put(DBHelper.KEY_NAME, admin.getName());
        contentValues.put(DBHelper.KEY_PHONE, admin.getPhone());

        database.insert(DBHelper.EMPLOYEES, null, contentValues);
        dbHelper.close();
    }






    public List<ClientClass> findAllClients(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllClients");
        if (allClientsList.isEmpty() || reFind) {
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            // получаем данные c табл "EMPLOYEES"
            cursorClients = database.query(DBHelper.CLIENTS,
                    null, null, null,
                    null, null, null);
            if (cursorClients.moveToFirst()) {
                int idIndex = cursorClients.getColumnIndex(DBHelper.KEY_ID);
                int nameIndex = cursorClients.getColumnIndex(DBHelper.KEY_NAME);
                int phoneIndex = cursorClients.getColumnIndex(DBHelper.KEY_PHONE);
                int ordersCountIndex = cursorClients.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
                int durationSumMinuteIndex = cursorClients.getColumnIndex(DBHelper.KEY_DURATION_SUM_MINUTE);
                do {
                    allClientsList.add(new ClientClass(
                            cursorClients.getInt(idIndex),
                            cursorClients.getString(nameIndex),
                            cursorClients.getString(phoneIndex),
                            cursorClients.getInt(ordersCountIndex),
                            cursorClients.getInt(durationSumMinuteIndex)));
                } while (cursorClients.moveToNext());
            } else {
                // если не задан ни один сотрудника, то м. перейти в настройки его создания
                Log.d("Gas", "0 rows");
            }
            cursorClients.close();
            dbHelper.close();
        } else Log.i("OptionalClass", "False allClientsList.isEmpty() || reFind");
        return allClientsList;
    }

    void putClientInDB(Context context, ClientClass client) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME, client.getName());
        contentValues.put(DBHelper.KEY_PHONE, client.getPhone());
        contentValues.put(DBHelper.KEY_ORDERS_COUNT, client.getOrdersCount());
        contentValues.put(DBHelper.KEY_DURATION_SUM_MINUTE, client.getDurationSumMinute());


        database.insert(DBHelper.CLIENTS, null, contentValues);
        dbHelper.close();
    }

    // метод заменяет данные Клиента в БД (редактирование клиента)
    public void putChangeClientInDB(Context context, ClientClass client) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        database.delete(DBHelper.CLIENTS, DBHelper.KEY_ID + "=" + client.getId(), null);

        contentValues.put(DBHelper.KEY_ID, client.getId());
        contentValues.put(DBHelper.KEY_NAME, client.getName());
        contentValues.put(DBHelper.KEY_PHONE, client.getPhone());
        contentValues.put(DBHelper.KEY_ORDERS_COUNT, client.getOrdersCount());
        contentValues.put(DBHelper.KEY_DURATION_SUM_MINUTE, client.getDurationSumMinute());

        database.insert(DBHelper.CLIENTS, null, contentValues);
        dbHelper.close();
    }






    public List<TableClass> findAllTables(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllTables");
        if (allTablesList.isEmpty() || reFind) {
            // работа с БД
            DBHelper dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            cursorTables = database.query(DBHelper.TABLES,
                    null, null, null,
                    null, null, null);
            if (cursorTables.moveToFirst()) {
                int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
                int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
                do {
                    allTablesList.add(new TableClass(
                            cursorTables.getInt(numberTableIndex),
                            cursorTables.getString(typeIndex)));
                } while (cursorTables.moveToNext());
            } else {
                Log.d("OptionalClass", "0 rows");
            }
            cursorTables.close();
            dbHelper.close();
        } else Log.i("OptionalClass", "False allTablesList.isEmpty() || reFind");
        return allTablesList;
    }

    // метод перезаписывает в БД "TABLES" изменения
    public void changeTableInDB(Context context, int numTableDB, String choseTypeTable) {
        // работа с БД
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // если хотим изменить запись, то просто передает туда тот же номер строки
        database.delete(DBHelper.TABLES, DBHelper.KEY_ID + "=" + numTableDB, null);
        contentValues.put(DBHelper.KEY_ID, numTableDB);
        contentValues.put(DBHelper.KEY_TYPE, choseTypeTable);

        database.insert(DBHelper.TABLES, null, contentValues);
        dbHelper.close();
    }





    @SuppressLint("DefaultLocale")
    private int currentDurationCalc(OrderClass finishTable) {
        Date reserveDateTime = new Date();
        int newDuration;
        {
            Calendar currentDateTimeCal = Calendar.getInstance();
            Calendar reserveDateTimeCal = Calendar.getInstance();
            try { // переводим дату и время из строки в Date
                reserveDateTime = dateTimeFormat.parse(finishTable.getDateStartReserve() + " " + finishTable.getTimeStartReserve());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert reserveDateTime != null;
            reserveDateTimeCal.setTime(reserveDateTime);
            newDuration = (Integer.parseInt(String.format("%.0f",
                    (currentDateTimeCal.getTimeInMillis() - reserveDateTimeCal.getTimeInMillis()) / (1000d * 60)))) - 1;
        }
        return newDuration;
    }

    public int convertDpToPixels(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
    public double convertDpToPixelsDouble(Context context, double dp) {
        return (dp * context.getResources().getDisplayMetrics().density);
    }

    public String dateDateToString(Calendar dateCal) {
        String myMonthSt, myDaySt;
        if (dateCal.get(Calendar.MONTH) < 10) myMonthSt = "0" + (dateCal.get(Calendar.MONTH) + 1);
        else myMonthSt = "" + (dateCal.get(Calendar.MONTH) + 1);
        if (dateCal.get(Calendar.DAY_OF_MONTH) < 10)
            myDaySt = "0" + dateCal.get(Calendar.DAY_OF_MONTH);
        else myDaySt = "" + dateCal.get(Calendar.DAY_OF_MONTH);

        return myDaySt + "." + myMonthSt + "." + dateCal.get(Calendar.YEAR);
    }

    public int checkLeftEndWorkMinute(int durationNewReserve) {
        // посчитаем сколько времени осталось до конца рабочего дня
        Calendar currentDateCalendar = Calendar.getInstance();
        Calendar endWorkTimeCalendar = Calendar.getInstance();
        if (endWorkTimeCalendar.get(Calendar.HOUR_OF_DAY) >= 5) {
            // если не полночь, а день, то окончание работы завтра в 5 утра
            endWorkTimeCalendar.add(Calendar.DATE, 1);  // number of days to add
            Log.i("gas4", "Прибавили день");
        }
        try {
            endWorkTimeCalendar.setTime(Objects.requireNonNull(dateTimeFormat.parse(
                    endWorkTimeCalendar.get(Calendar.DAY_OF_MONTH) + "." + (endWorkTimeCalendar.get(Calendar.MONTH) + 1) + "." +
                            endWorkTimeCalendar.get(Calendar.YEAR) + " 05:00")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        @SuppressLint("DefaultLocale")
        int leftEndWorkMinute = Integer.parseInt(String.format("%.0f", (
                endWorkTimeCalendar.getTimeInMillis() - currentDateCalendar.getTimeInMillis()) / (1000d * 60)));
        Log.i("gas4", "leftEndWorkMinute = " + leftEndWorkMinute);

        // если длительность превышает режим работы бильярдной
        if (durationNewReserve > leftEndWorkMinute) durationNewReserve = leftEndWorkMinute;

        return durationNewReserve;
    }

    public String tomorrowDateReserveMethod(String date) {
        // метод выводит следующий день после выбранного в btnDate
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(dateFormat.parse(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 1);  // number of days to add
        return dateFormat.format(c.getTime());  // dt is now the new date
    }

    public String yesterdayDateReserveMethod(String date) {
        // метод выводит следующий день после выбранного в btnDate
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(dateFormat.parse(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, -1);  // number of days to add
        return dateFormat.format(c.getTime());  // dt is now the new date
    }

    @SuppressLint("DefaultLocale")
    public int calcMinuteFromDateTime(String today, OrderClass orderClass) {
        Log.i("OptionalClass", "\n ...//...    calcMinuteFromDateTime");
        Date todayDateTime = new Date();
        Date reserveDateTime = new Date();
        int newDuration;
        {
            Calendar currentDateTimeCal = Calendar.getInstance();
            Calendar reserveDateTimeCal = Calendar.getInstance();
            try { // переводим дату и время из строки в Date
                todayDateTime = dateTimeFormat.parse(today + " 11:00");
                reserveDateTime = dateTimeFormat.parse(orderClass.getDateStartReserve() + " " + orderClass.getTimeStartReserve());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert todayDateTime != null;
            currentDateTimeCal.setTime(todayDateTime);
            assert reserveDateTime != null;
            reserveDateTimeCal.setTime(reserveDateTime);
            Log.i("OptionalClass", "currentDateTimeCal = " + currentDateTimeCal.getTime());
            Log.i("OptionalClass", "reserveDateTimeCal = " + reserveDateTimeCal.getTime());
            newDuration = (Integer.parseInt(String.format("%.0f",
                    (reserveDateTimeCal.getTimeInMillis() - currentDateTimeCal.getTimeInMillis()) / (1000d * 60)))) - 1;
            Log.i("OptionalClass", "newDuration = " + newDuration);
        }
        return newDuration;
    }

    public String getWorkDay() {
        // Проверяем дату смены
        String date;
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 11) {
            date = dateDateToString(Calendar.getInstance());
        } else // то записываем вчерашний день
            date = yesterdayDateReserveMethod(dateDateToString(Calendar.getInstance()));
        return date;
    }

    public String timeDateToString(Calendar dateCal) {
        String hourReserveSt, minuteReserveSt;

        if (dateCal.get(Calendar.HOUR_OF_DAY) < 10)
            hourReserveSt = "0" + dateCal.get(Calendar.HOUR_OF_DAY);
        else hourReserveSt = "" + dateCal.get(Calendar.HOUR_OF_DAY);
        if (dateCal.get(Calendar.MINUTE) < 10) minuteReserveSt = "0" + dateCal.get(Calendar.MINUTE);
        else minuteReserveSt = "" + dateCal.get(Calendar.MINUTE);

        return hourReserveSt + ":" + minuteReserveSt;
    }
}
