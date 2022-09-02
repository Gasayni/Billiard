package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
    Calendar cal = Calendar.getInstance();
    Cursor cursorEmployee, cursorOrders, cursorClients;

    List<String> adminsList = new ArrayList<>();
    List<String> passList = new ArrayList<>();
    List<ReserveTable> oldReserveList = new ArrayList<>();

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
        cursorEmployee.close();
        if (thatReturn.equals("adminsList")) {
            return adminsList;
        }
        else return passList;
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
            int durationSumMinuteIndex = cursorClients.getColumnIndex(DBHelper.KEY_DURATION_SUM_MINUTE);
            do {
                // находим всех клиентов из бд
                clientsList.add(cursorClients.getString(nameIndex));
            } while (cursorClients.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        cursorClients.close();
        return clientsList;
    }

    public String findPhoneClient(Context context, String client) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        String phone = "";

        // получаем данные c табл "EMPLOYEES"
        cursorClients = database.query(DBHelper.CLIENTS,
                null, null, null,
                null, null, null);
        if (cursorClients.moveToFirst()) {
            int nameIndex = cursorClients.getColumnIndex(DBHelper.KEY_NAME);
            int phoneIndex = cursorClients.getColumnIndex(DBHelper.KEY_PHONE);
            int ordersCountIndex = cursorClients.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
            int durationSumMinuteIndex = cursorClients.getColumnIndex(DBHelper.KEY_DURATION_SUM_MINUTE);
            do {
                if (client.equals(cursorClients.getString(nameIndex))) {
                    phone = cursorClients.getString(phoneIndex);
                }
            } while (cursorClients.moveToNext());
        } else {
            // если не задан ни один сотрудника, то м. перейти в настройки его создания
            Log.d("Gas", "0 rows");
        }
        cursorClients.close();
        database.close();
        return phone;
    }

    // нужно проверить БД на устаревание
    // если дата и время уже прошла, (если не был статус удалить) то меняем статус на old
    public void checkOldReserve(Context context) {
        // работа с БД
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        oldReserveList.clear();

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
            int bronIndex = cursorOrders.getColumnIndex(DBHelper.KEY_BRON);
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
                    cal.setTime(reserveStartDateTime);
                    cal.add(Calendar.MINUTE, cursorOrders.getInt(durationIndex));
                    String reserveFinishTimeStr = dateTimeFormat.format(cal.getTime());
                    try {
                        reserveFinishDateTime = dateTimeFormat.parse(reserveFinishTimeStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } // здесь находим время конца резерва

                Log.i("Gas1", "index = " + cursorOrders.getInt(idIndex));
                Log.i("Gas1", "reserveStartDateTime = " + dateTimeFormat.format(reserveStartDateTime));
                Log.i("Gas1", "durationIndex = " + cursorOrders.getInt(durationIndex));
                Log.i("Gas1", "reserveFinishTime = " + dateTimeFormat.format(reserveFinishDateTime));
                Log.i("Gas1", "new Date() = " + dateTimeFormat.format(new Date()));
                Log.i("Gas1", "before? = " + reserveFinishDateTime.before(new Date()));

                if ((reserveFinishDateTime.before(new Date())) && (cursorOrders.getString(statusIndex).equals(""))) {
                    // если время уже прошло, то копируем все данные и меняем статус
                    Log.i("Gas1", "make old");
                    oldReserveList.add(new ReserveTable(
                            cursorOrders.getInt(idIndex),
                            cursorOrders.getInt(numTableIndex),
                            cursorOrders.getString(reserveDateIndex),
                            cursorOrders.getString(reserveTimeIndex),
                            cursorOrders.getInt(durationIndex),
                            cursorOrders.getString(orderDateIndex),
                            cursorOrders.getString(orderTimeIndex),
                            cursorOrders.getString(clientIndex),
                            cursorOrders.getString(employeeIndex),
                            cursorOrders.getString(bronIndex),
                            cursorOrders.getString(statusIndex)));
                }
            } while (cursorOrders.moveToNext());
        } else {
            // если в БД нет заказов
            Log.d("Gas", "0 rows");
        }
        cursorOrders.close();
        database.close();
        changeReserveInDB();
    }

    private void changeReserveInDB() {
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
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
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
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Отмена", Toast.LENGTH_SHORT).show();
                    }
                });
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

    public String dateFormatMethod(Calendar dateCal) {
        String myMonthSt, myDaySt;
        if (dateCal.get(Calendar.MONTH) < 10) myMonthSt = "0" + (dateCal.get(Calendar.MONTH) + 1);
        else myMonthSt = "" + (dateCal.get(Calendar.MONTH) + 1);
        if (dateCal.get(Calendar.DAY_OF_MONTH) < 10) myDaySt = "0" + dateCal.get(Calendar.DAY_OF_MONTH);
        else myDaySt = "" + dateCal.get(Calendar.DAY_OF_MONTH);

        return myDaySt + "." + myMonthSt + "." + dateCal.get(Calendar.YEAR);
    }

    public String timeFormatMethod(Calendar dateCal) {
        String hourReserveSt, minuteReserveSt;

        if (dateCal.get(Calendar.HOUR_OF_DAY) < 10) hourReserveSt = "0" + dateCal.get(Calendar.HOUR_OF_DAY);
        else hourReserveSt = "" + dateCal.get(Calendar.HOUR_OF_DAY);
        if (dateCal.get(Calendar.MINUTE) < 10) minuteReserveSt = "0" + dateCal.get(Calendar.MINUTE);
        else minuteReserveSt = "" + dateCal.get(Calendar.MINUTE);

        return hourReserveSt + ":" + minuteReserveSt;
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
        int leftEndWorkMinute = Integer.parseInt(String.format("%.0f",(
                endWorkTimeCalendar.getTimeInMillis() - currentDateCalendar.getTimeInMillis()) / (1000d * 60)));
        Log.i("gas4", "leftEndWorkMinute = " + leftEndWorkMinute);

        // если длительность превышает режим работы бильярдной
        if (durationNewReserve > leftEndWorkMinute) durationNewReserve = leftEndWorkMinute;

        return durationNewReserve;
    }


}
