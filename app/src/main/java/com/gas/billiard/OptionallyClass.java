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
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorEmployee, cursorOrders, cursorClients, cursorTables;

    static List<List<OrderClass>> allOrdersList = new ArrayList<>();
    static List<AdminClass> allAdminsList = new ArrayList<>();
    static List<ClientClass> allClientsList = new ArrayList<>();
    static List<TableClass> allTablesList = new ArrayList<>();
    List<OrderClass> oldReserveList = new ArrayList<>();

    //    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    Date reserveFinishDateTime;

    public int convertDpToPixels(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public List<List<OrderClass>> findAllOrders(Context context, String date, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllOrders");
        if (allOrdersList.isEmpty() || reFind) { // если мы не инициализировали этот лист
            Log.i("OptionalClass", "True allReservesList.isEmpty() || reFind = ");

            // работа с БД
            dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            contentValues = new ContentValues();

            String dateReserveTomorrow = tomorrowDateReserveMethod(date);
            Log.i("OptionalClass", "\tbtn Today dateReserve " + date);
            Log.i("OptionalClass", "\tTomorrow dateReserve = " + dateReserveTomorrow);

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
                    // находим кнопку по времени резерва, воспользуемся спец. методом
                    // коорината по столбцу
                    String[] hourAr = cursorOrders.getString(reserveTimeIndex).split(":");
                    int hour = Integer.parseInt(hourAr[0]);
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
                        allOrdersList.get(numTable - 1).add(orderClass);
                        Log.i("OptionalClass", "\t\t\tallTablesList.get(" + numTable + ") = " +
                                allOrdersList.get(numTable - 1).size());
                    }
                } while (cursorOrders.moveToNext());
            } else {
                Log.d("Gas_OptionalClass", "0 rows");
            }
            cursorOrders.close();
        } else Log.i("OptionalClass", "False allOrdersList.isEmpty() || reFind");
        return allOrdersList;
    }

    public List<AdminClass> findAllAdmins(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllAdmins");
        if (allAdminsList.isEmpty() || reFind) { // если мы не инициализировали этот лист
            dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            contentValues = new ContentValues();

            // получаем данные c табл "EMPLOYEES"
            cursorEmployee = database.query(DBHelper.EMPLOYEES,
                    null, null, null,
                    null, null, null);
            if (cursorEmployee.moveToFirst()) {
                int idIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_ID);
                int nameIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_NAME);
                int phoneIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_NAME);
                int passIndex = cursorEmployee.getColumnIndex(DBHelper.KEY_PASS);
                do {
                    allAdminsList.add(new AdminClass(
                            cursorEmployee.getInt(idIndex),
                            cursorEmployee.getString(nameIndex),
                            cursorEmployee.getString(phoneIndex),
                            cursorEmployee.getString(passIndex)));
                } while (cursorEmployee.moveToNext());
            } else {
                // если не задан ни один сотрудника, то м. перейти в настройки его создания
                Log.d("OptionalClass", "0 rows");
            }
            cursorEmployee.close();
        }
        Log.i("OptionalClass", "False allAdminsList.isEmpty() || reFind");
        return allAdminsList;
    }

    public List<TableClass> findAllTables(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllTables");
        if (allTablesList.isEmpty() || reFind) {
            // работа с БД
            dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            contentValues = new ContentValues();

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
        } else Log.i("OptionalClass", "False allTablesList.isEmpty() || reFind");
        return allTablesList;
    }

    public List<ClientClass> findAllClients(Context context, boolean reFind) {
        Log.i("OptionalClass", "\n ...//...    findAllClients");
        if (allClientsList.isEmpty() || reFind) {
            dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            contentValues = new ContentValues();

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
        } else Log.i("OptionalClass", "False allClientsList.isEmpty() || reFind");
        return allClientsList;
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
        changeReserveInDB(context);
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

    public String timeDateToString(Calendar dateCal) {
        String hourReserveSt, minuteReserveSt;

        if (dateCal.get(Calendar.HOUR_OF_DAY) < 10)
            hourReserveSt = "0" + dateCal.get(Calendar.HOUR_OF_DAY);
        else hourReserveSt = "" + dateCal.get(Calendar.HOUR_OF_DAY);
        if (dateCal.get(Calendar.MINUTE) < 10) minuteReserveSt = "0" + dateCal.get(Calendar.MINUTE);
        else minuteReserveSt = "" + dateCal.get(Calendar.MINUTE);

        return hourReserveSt + ":" + minuteReserveSt;
    }


    private void changeReserveInDB(Context context) {
        dbHelper = new DBHelper(context);
        contentValues = new ContentValues();
        database = dbHelper.getWritableDatabase();

        for (int i = 0; i < oldReserveList.size(); i++) {
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
        }
    }

    // метод перезаписывает в БД "TABLES" изменения
    public void changeTableInDB(Context context, int numTableDB, String choseTypeTable) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        // если хотим изменить запись, то просто передает туда тот же номер строки
        database.delete(DBHelper.TABLES, DBHelper.KEY_ID + "=" + numTableDB, null);
        contentValues.put(DBHelper.KEY_ID, numTableDB);
        contentValues.put(DBHelper.KEY_TYPE, choseTypeTable);

        database.insert(DBHelper.TABLES, null, contentValues);
    }

    public void openDialogCreateClient(Context context, String getAdminName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.dialog_create_client_or_employee, null);
        final EditText etName = (EditText) subView.findViewById(R.id.etName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавление клиента\n")
                .setMessage("Введите данные нового клиента")
                .setView(subView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    final String nameNewClient = etName.getText().toString();
                    final String phoneNewClient = etPhone.getText().toString();

                    if (nameNewClient.equals("")) {
                        Toast.makeText(context, "Введите имя клиента", Toast.LENGTH_SHORT).show();
                        etName.setHintTextColor(Color.RED);
                    } else {
                        putClientInDB(context, nameNewClient, phoneNewClient);
                        Toast.makeText(context, "Клиент добавлен", Toast.LENGTH_SHORT).show();

                        // чтобы БД клиентов сразу обновилась
                        Intent intent = new Intent(context, EditDBActivity.class);
                        // передаем название заголовка
                        intent.putExtra("headName", "Клиенты");
                        intent.putExtra("adminName", getAdminName);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> Toast.makeText(context, "Отмена", Toast.LENGTH_SHORT).show());
        AlertDialog alert = builder.create();
        alert.show();
    }

    void putClientInDB(Context context, String nameNewClient, String phoneNewClient) {
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME, nameNewClient);
        contentValues.put(DBHelper.KEY_PHONE, phoneNewClient);
        contentValues.put(DBHelper.KEY_ORDERS_COUNT, 0);
        contentValues.put(DBHelper.KEY_DURATION_SUM_MINUTE, 0);


        database.insert(DBHelper.CLIENTS, null, contentValues);
    }

    public void openDialogCreateEmployee(Context context, String getAdminName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View subView = inflater.inflate(R.layout.dialog_create_client_or_employee, null);
        final EditText etName = (EditText) subView.findViewById(R.id.etName);
        final EditText etPhone = (EditText) subView.findViewById(R.id.etPhone);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Добавление Администратора\n")
                .setMessage("Введите данные нового администратора")
                .setView(subView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    final String nameNewEmployee = etName.getText().toString();
                    final String phoneNewEmployee = etPhone.getText().toString();

                    if (nameNewEmployee.equals("")) {
                        Toast.makeText(context, "Введите имя администратора", Toast.LENGTH_SHORT).show();
                        etName.setHintTextColor(Color.RED);
                    } else {
                        putEmployeeInDB(context, nameNewEmployee, phoneNewEmployee);
                        Toast.makeText(context, "Администратор добавлен", Toast.LENGTH_SHORT).show();

                        // чтобы БД клиентов сразу обновилась
                        Intent intent = new Intent(context, EditDBActivity.class);
                        // передаем название заголовка
                        intent.putExtra("headName", "Сотрудники");
                        intent.putExtra("adminName", getAdminName);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> Toast.makeText(context, "Отмена", Toast.LENGTH_SHORT).show());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void putEmployeeInDB(Context context, String nameNewEmployee, String phoneNewEmployee) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME, nameNewEmployee);
        contentValues.put(DBHelper.KEY_PHONE, phoneNewEmployee);

        database.insert(DBHelper.EMPLOYEES, null, contentValues);
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
}
