package com.gas.billiard;

import static java.time.temporal.ChronoUnit.MINUTES;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonActivity extends AppCompatActivity implements View.OnClickListener {
    private Date currentTime;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private Date currentDate = new Date();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    String today = dateFormat.format(currentDate);
    List<String> todayReserveList = new ArrayList<>();

    private TextView tvTime, tvAdminName;
    LocalTime startGameLocalTime, endGameLocalTime;

    Button btnAdd;
    public Button
            btnTable1, btnTable2, btnTable3, btnTable4, btnTable5, btnTable6, btnTable7, btnTable8,
            btnTable9, btnTable10, btnTable11, btnTable12, btnTable13, btnTable14, btnTable15,
            btnTable16, btnTable17, btnTable18, btnTable19;
    public Button btnStatus1, btnStatus2, btnStatus3, btnStatus4, btnStatus5, btnStatus6,
            btnStatus7, btnStatus8, btnStatus9, btnStatus10, btnStatus11, btnStatus12, btnStatus13,
            btnStatus14, btnStatus15, btnStatus16, btnStatus17, btnStatus18, btnStatus19;
    public Button btnStartGameTime1, btnStartGameTime2, btnStartGameTime3, btnStartGameTime4, btnStartGameTime5,
            btnStartGameTime6, btnStartGameTime7, btnStartGameTime8, btnStartGameTime9, btnStartGameTime10,
            btnStartGameTime11, btnStartGameTime12, btnStartGameTime13, btnStartGameTime14, btnStartGameTime15,
            btnStartGameTime16, btnStartGameTime17, btnStartGameTime18, btnStartGameTime19;
    public String endGameTime1, endGameTime2, endGameTime3, endGameTime4, endGameTime5,
            endGameTime6, endGameTime7, endGameTime8, endGameTime9, endGameTime10,
            endGameTime11, endGameTime12, endGameTime13, endGameTime14, endGameTime15,
            endGameTime16, endGameTime17, endGameTime18, endGameTime19;
    public Button btnduration1, btnduration2, btnduration3, btnduration4,
            btnduration5, btnduration6, btnduration7, btnduration8, btnduration9,
            btnduration10, btnduration11, btnduration12, btnduration13, btnduration14,
            btnduration15, btnduration16, btnduration17, btnduration18, btnduration19;
    // возможность резерва
    public Button btnReserve1, btnReserve2, btnReserve3, btnReserve4, btnReserve5, btnReserve6,
            btnReserve7, btnReserve8, btnReserve9, btnReserve10, btnReserve11, btnReserve12,
            btnReserve13, btnReserve14, btnReserve15, btnReserve16, btnReserve17, btnReserve18,
            btnReserve19;
    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        initBtn();

        // Получаем имя админа
        Intent intent = getIntent();
        String adminName = intent.getStringExtra("adminName");
        tvAdminName.setText(String.format("Администратор:   %s", adminName));
        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        choseTable();
        // текущее время
        actualTime();
        // времени осталось до конца
        btnduration1.setOnClickListener(this);
        btnReserve1.setOnClickListener(this);
        // добавить новый резерв
        btnAdd.setOnClickListener(this);

        // нам нужно загрузить с Таблиц каждого стола данные о резервах на сегодня
        reserveToday();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnDuration1: {
                intent = new Intent("TimeEditActivity");
                intent.putExtra("tableNumber", "1");
                intent.putExtra("startTime", "19:00");
                intent.putExtra("endTime", "21:00");
                startActivity(intent);
                break;
            }
            case R.id.btnReserve1: {
                intent = new Intent("ReserveEdit");
                intent.putExtra("tableNumber", "1");
                startActivity(intent);
                break;
            }
            case R.id.btnAdd: {
                intent = new Intent("newOrderActivity");
                startActivity(intent);
                break;
            }
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
                currentTime = new Date();
                tvTime.setText(timeFormat.format(currentTime));
                // также обращаемся каждую минуту к калькулятору оставшегося времени
                calculateduration(timeFormat.format(currentTime));
                actualTime();  // мис рекурсия
            });
        }).start();
    }

    private void choseTable() {
        // получаем данные c табл "tables"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            int startTimeIndex = cursorTables.getColumnIndex(DBHelper.KEY_START_TIME);
            int endTimeIndex = cursorTables.getColumnIndex(DBHelper.KEY_END_TIME);
            int startDateIndex = cursorTables.getColumnIndex(DBHelper.KEY_START_DATE);
            int endDateIndex = cursorTables.getColumnIndex(DBHelper.KEY_END_DATE);
            int statusIndex = cursorTables.getColumnIndex(DBHelper.KEY_STATUS);
            int endRateIndex = cursorTables.getColumnIndex(DBHelper.KEY_RATE);
            int descriptionIndex = cursorTables.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // для каждого стола
                switch (cursorTables.getInt(numberTableIndex)) {
                    case 1: {
                        btnStartGameTime1.setText(cursorTables.getString(startTimeIndex));
                        endGameTime1 = cursorTables.getString(endTimeIndex);
                        Log.i("Gas", "endGameTime1 = " + endGameTime1);
                        if (cursorTables.getString(statusIndex).equals("pool")) {
                            btnTable1.setBackgroundResource(R.drawable.am_flag);
                            btnTable1.setPadding(28, 0, 0, 0);
                        } else if (cursorTables.getString(statusIndex).equals("pyramid")) {
                            btnTable1.setBackgroundResource(R.drawable.rus_flag);
                            btnTable1.setPadding(28, 0, 0, 0);
                        }
                        break;
                    }
                    case 2: {
                        btnStartGameTime2.setText(cursorTables.getString(startTimeIndex));
                        endGameTime2 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 3: {
                        btnStartGameTime3.setText(cursorTables.getString(startTimeIndex));
                        endGameTime3 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 4: {
                        btnStartGameTime4.setText(cursorTables.getString(startTimeIndex));
                        endGameTime4 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 5: {
                        btnStartGameTime5.setText(cursorTables.getString(startTimeIndex));
                        endGameTime5 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 6: {
                        btnStartGameTime6.setText(cursorTables.getString(startTimeIndex));
                        endGameTime6 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 7: {
                        btnStartGameTime7.setText(cursorTables.getString(startTimeIndex));
                        endGameTime7 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 8: {
                        btnStartGameTime8.setText(cursorTables.getString(startTimeIndex));
                        endGameTime8 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 9: {
                        btnStartGameTime9.setText(cursorTables.getString(startTimeIndex));
                        endGameTime9 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 10: {
                        btnStartGameTime10.setText(cursorTables.getString(startTimeIndex));
                        endGameTime10 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 11: {
                        btnStartGameTime11.setText(cursorTables.getString(startTimeIndex));
                        endGameTime11 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 12: {
                        btnStartGameTime12.setText(cursorTables.getString(startTimeIndex));
                        endGameTime12 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 13: {
                        btnStartGameTime13.setText(cursorTables.getString(startTimeIndex));
                        endGameTime13 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 14: {
                        btnStartGameTime14.setText(cursorTables.getString(startTimeIndex));
                        endGameTime14 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 15: {
                        btnStartGameTime15.setText(cursorTables.getString(startTimeIndex));
                        endGameTime15 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 16: {
                        btnStartGameTime16.setText(cursorTables.getString(startTimeIndex));
                        endGameTime16 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 17: {
                        btnStartGameTime17.setText(cursorTables.getString(startTimeIndex));
                        endGameTime17 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 18: {
                        btnStartGameTime18.setText(cursorTables.getString(startTimeIndex));
                        endGameTime18 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                    case 19: {
                        btnStartGameTime19.setText(cursorTables.getString(startTimeIndex));
                        endGameTime19 = cursorTables.getString(endTimeIndex);
                        break;
                    }
                }
            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();
    }

    private void initBtn() {
        tvAdminName = findViewById(R.id.tvAdminName);
        tvTime = findViewById(R.id.tvTime);
        btnAdd = findViewById(R.id.btnAdd);
        // статусы столов
        btnStatus1 = findViewById(R.id.btnStatus1);
        btnStatus2 = findViewById(R.id.btnStatus2);
        btnStatus3 = findViewById(R.id.btnStatus3);
        btnStatus4 = findViewById(R.id.btnStatus4);
        btnStatus5 = findViewById(R.id.btnStatus5);
        btnStatus6 = findViewById(R.id.btnStatus6);
        btnStatus7 = findViewById(R.id.btnStatus7);
        btnStatus8 = findViewById(R.id.btnStatus8);
        btnStatus9 = findViewById(R.id.btnStatus9);
        btnStatus10 = findViewById(R.id.btnStatus10);
        btnStatus11 = findViewById(R.id.btnStatus11);
        btnStatus12 = findViewById(R.id.btnStatus12);
        btnStatus13 = findViewById(R.id.btnStatus13);
        btnStatus14 = findViewById(R.id.btnStatus14);
        btnStatus15 = findViewById(R.id.btnStatus15);
        btnStatus16 = findViewById(R.id.btnStatus16);
        btnStatus17 = findViewById(R.id.btnStatus17);
        btnStatus18 = findViewById(R.id.btnStatus18);
        btnStatus19 = findViewById(R.id.btnStatus19);
        // время старта
        btnStartGameTime1 = findViewById(R.id.btnStartTime1);
        btnStartGameTime2 = findViewById(R.id.btnStartTime2);
        btnStartGameTime3 = findViewById(R.id.btnStartTime3);
        btnStartGameTime4 = findViewById(R.id.btnStartTime4);
        btnStartGameTime5 = findViewById(R.id.btnStartTime5);
        btnStartGameTime6 = findViewById(R.id.btnStartTime6);
        btnStartGameTime7 = findViewById(R.id.btnStartTime7);
        btnStartGameTime8 = findViewById(R.id.btnStartTime8);
        btnStartGameTime9 = findViewById(R.id.btnStartTime9);
        btnStartGameTime10 = findViewById(R.id.btnStartTime10);
        btnStartGameTime11 = findViewById(R.id.btnStartTime11);
        btnStartGameTime12 = findViewById(R.id.btnStartTime12);
        btnStartGameTime13 = findViewById(R.id.btnStartTime13);
        btnStartGameTime14 = findViewById(R.id.btnStartTime14);
        btnStartGameTime15 = findViewById(R.id.btnStartTime15);
        btnStartGameTime16 = findViewById(R.id.btnStartTime16);
        btnStartGameTime17 = findViewById(R.id.btnStartTime17);
        btnStartGameTime18 = findViewById(R.id.btnStartTime18);
        btnStartGameTime19 = findViewById(R.id.btnStartTime19);
        // осталось времени до конца
        btnduration1 = findViewById(R.id.btnDuration1);
        btnduration2 = findViewById(R.id.btnDuration2);
        btnduration3 = findViewById(R.id.btnDuration3);
        btnduration4 = findViewById(R.id.btnDuration4);
        btnduration5 = findViewById(R.id.btnDuration5);
        btnduration6 = findViewById(R.id.btnDuration6);
        btnduration7 = findViewById(R.id.btnDuration7);
        btnduration8 = findViewById(R.id.btnDuration8);
        btnduration9 = findViewById(R.id.btnDuration9);
        btnduration10 = findViewById(R.id.btnDuration10);
        btnduration11 = findViewById(R.id.btnDuration11);
        btnduration12 = findViewById(R.id.btnDuration12);
        btnduration13 = findViewById(R.id.btnDuration13);
        btnduration14 = findViewById(R.id.btnDuration14);
        btnduration15 = findViewById(R.id.btnDuration15);
        btnduration16 = findViewById(R.id.btnDuration16);
        btnduration17 = findViewById(R.id.btnDuration17);
        btnduration18 = findViewById(R.id.btnDuration18);
        btnduration19 = findViewById(R.id.btnDuration19);
        // возможность резерва
        btnReserve1 = findViewById(R.id.btnReserve1);
        btnReserve2 = findViewById(R.id.btnReserve2);
        btnReserve3 = findViewById(R.id.btnReserve3);
        btnReserve4 = findViewById(R.id.btnReserve4);
        btnReserve5 = findViewById(R.id.btnReserve5);
        btnReserve6 = findViewById(R.id.btnReserve6);
        btnReserve7 = findViewById(R.id.btnReserve7);
        btnReserve8 = findViewById(R.id.btnReserve8);
        btnReserve9 = findViewById(R.id.btnReserve9);
        btnReserve10 = findViewById(R.id.btnReserve10);
        btnReserve11 = findViewById(R.id.btnReserve11);
        btnReserve12 = findViewById(R.id.btnReserve12);
        btnReserve13 = findViewById(R.id.btnReserve13);
        btnReserve14 = findViewById(R.id.btnReserve14);
        btnReserve15 = findViewById(R.id.btnReserve15);
        btnReserve16 = findViewById(R.id.btnReserve16);
        btnReserve17 = findViewById(R.id.btnReserve17);
        btnReserve18 = findViewById(R.id.btnReserve18);
        btnReserve19 = findViewById(R.id.btnReserve19);
    }

    private void calculateduration(String curTime) {
        LocalTime currentTime = LocalTime.parse(curTime);  // текущее время
        // узнаем продолжительность игры 1 стола
        // у нас есть время начала и текущее
        if ((!btnStartGameTime1.getText().toString().equals(""))/* &&
                (!endGameTime1.equals(""))*/) {  // если мы забираем не пустое значение
            // забираем время начала и конца игры
            startGameLocalTime = LocalTime.parse(btnStartGameTime1.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime1);

            // текущее время д.б больше времени старта игры и меньше времени окончания игры
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus1.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus1.setText("Занят");
                btnduration1.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus1.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus1.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus2.setText("Свободен");
        }

        // аналогично продолжительность 2 стола
        if (!btnStartGameTime2.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime2.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime2);

            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus2.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus2.setText("Занят");
                btnduration2.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus2.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus2.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus2.setText("Свободен");
        }

        // аналогично продолжительность 3 стола
        if (!btnStartGameTime3.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime3.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime3);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus3.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus3.setText("Занят");
                btnduration3.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus3.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus3.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus3.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus3.setText("Свободен");
        }

        // аналогично продолжительность 4 стола
        if (!btnStartGameTime4.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime4.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime4);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus4.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus4.setText("Занят");
                btnduration4.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus4.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus4.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus4.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus4.setText("Свободен");
        }

        // аналогично продолжительность 5 стола
        if (!btnStartGameTime5.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime5.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime5);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus5.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus5.setText("Занят");
                btnduration5.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus5.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus5.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus5.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus5.setText("Свободен");
        }

        // аналогично продолжительность 6 стола
        if (!btnStartGameTime6.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime6.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime6);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus6.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus6.setText("Занят");
                btnduration6.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus6.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus6.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus6.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus6.setText("Свободен");
        }

        // аналогично продолжительность 7 стола
        if (!btnStartGameTime7.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime7.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime7);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus7.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus7.setText("Занят");
                btnduration7.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus7.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus7.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus7.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus7.setText("Свободен");
        }

        // аналогично продолжительность 8 стола
        if (!btnStartGameTime8.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime8.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime8);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus8.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus8.setText("Занят");
                btnduration8.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus8.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus8.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus8.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus8.setText("Свободен");
        }

        // аналогично продолжительность 9 стола
        if (!btnStartGameTime9.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime9.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime9);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus9.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus9.setText("Занят");
                btnduration9.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus9.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus9.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus9.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus9.setText("Свободен");
        }

        // аналогично продолжительность 10 стола
        if (!btnStartGameTime10.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime10.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime10);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus10.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus10.setText("Занят");
                btnduration10.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus10.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus10.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus10.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus10.setText("Свободен");
        }

        // аналогично продолжительность 11 стола
        if (!btnStartGameTime11.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime11.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime11);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus11.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus11.setText("Занят");
                btnduration11.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus11.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus11.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus11.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus11.setText("Свободен");
        }

        // аналогично продолжительность 12 стола
        if (!btnStartGameTime12.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime12.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime12);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus12.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus12.setText("Занят");
                btnduration12.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus12.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus12.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus12.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus12.setText("Свободен");
        }

        // аналогично продолжительность 13 стола
        if (!btnStartGameTime13.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime13.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime13);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus13.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus13.setText("Занят");
                btnduration13.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus13.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus13.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus13.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus13.setText("Свободен");
        }

        // аналогично продолжительность 14 стола
        if (!btnStartGameTime14.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime14.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime14);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus14.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus14.setText("Занят");
                btnduration14.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus14.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus14.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus14.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus14.setText("Свободен");
        }

        // аналогично продолжительность 15 стола
        if (!btnStartGameTime15.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime15.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime15);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus15.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus15.setText("Занят");
                btnduration15.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus15.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus15.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus15.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus15.setText("Свободен");
        }

        // аналогично продолжительность 16 стола
        if (!btnStartGameTime16.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime16.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime16);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus16.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus16.setText("Занят");
                btnduration16.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus16.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus16.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus16.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus16.setText("Свободен");
        }

        // аналогично продолжительность 17 стола
        if (!btnStartGameTime17.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime17.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime17);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus17.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus17.setText("Занят");
                btnduration17.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus17.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus17.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus17.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus17.setText("Свободен");
        }

        // аналогично продолжительность 18 стола
        if (!btnStartGameTime18.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime18.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime18);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus18.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus18.setText("Занят");
                btnduration18.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus18.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus18.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus18.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus18.setText("Свободен");
        }

        // аналогично продолжительность 19 стола
        if (!btnStartGameTime19.getText().toString().equals("")) {
            startGameLocalTime = LocalTime.parse(btnStartGameTime19.getText().toString());
            endGameLocalTime = LocalTime.parse(endGameTime19);
            if ((currentTime.isAfter(startGameLocalTime)) && (endGameLocalTime.isAfter(currentTime))) {
                // кнопка принимает красный фон
                btnStatus19.setBackgroundResource(R.drawable.btn_style_busy);
                btnStatus19.setText("Занят");
                btnduration19.setText(convertMinuteToHour(startGameLocalTime.until(currentTime, MINUTES)));
            } else {
                // кнопка принимает зеленый фон
                btnStatus19.setBackgroundResource(R.drawable.btn_style_free);
                btnStatus19.setText("*Свободен");
            }
        } else {
            // кнопка принимает зеленый фон
            btnStatus19.setBackgroundResource(R.drawable.btn_style_free);
            btnStatus19.setText("Свободен");
        }
    }

    private String convertMinuteToHour(long allMinutes) {
        long hourFinish = allMinutes / 60;
        long minFinish = allMinutes % 60;
        if (hourFinish == 0) {
            return "" + minFinish + " мин";
        } else return hourFinish + " ч\n" + minFinish + " мин";
    }

    private void reserveToday() {
        // нам нужно загрузить с Таблиц каждого стола данные о резервах на сегодня

        // получаем данные c табл "table 1"
        cursorTable = database.query(DBHelper.TABLE_1,
                null, null, null,
                null, null, null);
        if (cursorTable.moveToFirst()) {
            int numberTableIndex = cursorTable.getColumnIndex(DBHelper.KEY_ID);
            int startDateIndex = cursorTable.getColumnIndex(DBHelper.KEY_START_DATE);
            int startTimeIndex = cursorTable.getColumnIndex(DBHelper.KEY_START_TIME);
            int endDateIndex = cursorTable.getColumnIndex(DBHelper.KEY_END_DATE);
            int endTimeIndex = cursorTable.getColumnIndex(DBHelper.KEY_END_TIME);
            int clientIndex = cursorTable.getColumnIndex(DBHelper.KEY_CLIENT);
            int employeeIndex = cursorTable.getColumnIndex(DBHelper.KEY_EMPLOYEE);
            int rateIndex = cursorTable.getColumnIndex(DBHelper.KEY_RATE);
            int dateOrderIndex = cursorTable.getColumnIndex(DBHelper.KEY_DATE_ORDER);
            int timeOrderIndex = cursorTable.getColumnIndex(DBHelper.KEY_TIME_ORDER);
            int descriptionIndex = cursorTable.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // находим все сегодняшние резервы
                // если дата сегодняшняя, то заносим времена в лист сегодняшних резервов
                if (cursorTable.getString(startDateIndex).equals(today)) {
                    todayReserveList.add(cursorTable.getString(startTimeIndex));
                    Log.i("Gas", "заносим в лист сегодняшних резервов: = "
                            + cursorTable.getString(startTimeIndex));

                }
                Log.i("Gas", "endGameTime1 = " + endGameTime1);
                break;

            } while (cursorTable.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTable.close();
    }
}