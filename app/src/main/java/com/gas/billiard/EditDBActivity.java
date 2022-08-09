package com.gas.billiard;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EditDBActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass option = new OptionallyClass();
    LinearLayout linColumns;
    Button btnColumns;
    TextView tvHead;
    TextView tvData;
    private List<String> nameBtnColumns = new ArrayList<>();
    private List<Button> btnColumnsTagsList = new ArrayList<>();
    private int marginLength;

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTableDb;
    private String TABLE_DB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dbactivity);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        tvData = findViewById(R.id.tvData);

        tvHead = findViewById(R.id.tvHead);

        // Получаем название заголовка
        Intent getIntent = getIntent();
        tvHead.setText(getIntent.getStringExtra("headName"));

        addBtnColumns();
        editBtnColumns();
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
            case "Сотрудники": {
                TABLE_DB = DBHelper.EMPLOYEES;
                nameBtnColumns.add("ФИО");
                nameBtnColumns.add("Телефон");
                nameBtnColumns.add("Рейтинг");
                nameBtnColumns.add("Дополнительно");
                break;
            }
            case "Клиенты": {
                TABLE_DB = DBHelper.CLIENTS;
                nameBtnColumns.add("ФИО");
                nameBtnColumns.add("Телефон");
                nameBtnColumns.add("Заказов");
                nameBtnColumns.add("Общая сумма");
                nameBtnColumns.add("Рейтинг");
                nameBtnColumns.add("Дополнительно");
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
            btnColumns.setText(nameBtnColumns.get(i));
            btnColumnsTagsList.add(btnColumns);
            btnColumns.setTextSize(16);

            LinearLayout.LayoutParams marginBtnTable = (LinearLayout.LayoutParams) btnColumns.getLayoutParams();
            marginBtnTable.setMargins(0, 0, marginLength, 0);

            linColumns.addView(btnColumns);

            btnColumns.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
//                    Intent intent = new Intent("tableActivity");
//                    startActivity(intent);
//                    ''
                }
            });
        }
    }

    public void editBtnColumns() {
        int priceIndex = 0, descriptionIndex = 0, ratingIndex = 0, spentIndex = 0,
                ordersIndex = 0, phoneIndex = 0, secondNameIndex = 0, firstNameIndex = 0, rateIndex = 0;

        // получаем данные c табл выбранной таблицы
        cursorTableDb = database.query(TABLE_DB,
                null, null, null,
                null, null, null);
        if (cursorTableDb.moveToFirst()) {
            int idIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_ID);
            // если была выбрана таблица с тарифами
            switch (tvHead.getText().toString()) {
                case "Тарифы": {
                    priceIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_PRICE);
                    rateIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_RATE);
                    break;
                }
                case "Сотрудники": {
                    firstNameIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_FIRST_NAME);
                    secondNameIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_SECOND_NAME);
                    phoneIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_PHONE);
                    ratingIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_RATING);
                    descriptionIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_DESCRIPTION);
                    break;
                }
                case "Клиенты": {
                    firstNameIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_FIRST_NAME);
                    secondNameIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_SECOND_NAME);
                    phoneIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_PHONE);
                    ordersIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_ORDERS_COUNT);
                    spentIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_SPENT);
                    ratingIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_RATING);
                    descriptionIndex = cursorTableDb.getColumnIndex(DBHelper.KEY_DESCRIPTION);
                    break;
                }
            }
            do {
                // записали все, что было до
                StringBuilder s = new StringBuilder(tvData.getText().toString());

                switch (tvHead.getText().toString()) {
                    case "Тарифы": {
                        s.append("\n" + cursorTableDb.getInt(idIndex))
                                .append(".\t\t" + cursorTableDb.getInt(priceIndex))
                                .append("\t\t" + cursorTableDb.getString(rateIndex));
                        break;
                    }
                    case "Сотрудники": {
                        s.append("\n" + cursorTableDb.getInt(idIndex))
                                .append(".\t\t" + cursorTableDb.getString(secondNameIndex))
                                .append(" " + cursorTableDb.getString(firstNameIndex))
                                .append("\t\t" + cursorTableDb.getString(phoneIndex));
//                                .append("\t\tРейтинг " + cursorTableDb.getInt(ratingIndex))
//                                .append("\t\t(" + cursorTableDb.getString(descriptionIndex) + ")")
                        break;
                    }
                    case "Клиенты": {
                        s.append("\n" + cursorTableDb.getInt(idIndex))
                                .append(".\t\t" + cursorTableDb.getString(secondNameIndex))
                                .append(" " + cursorTableDb.getString(firstNameIndex))
                                .append("\t\t" + cursorTableDb.getString(phoneIndex))
                                .append("\t\tЗаказов: " + cursorTableDb.getInt(ordersIndex))
                                .append("\t\tПотратил: " + cursorTableDb.getInt(spentIndex));
//                                .append("\t\tРейтинг: " + cursorTableDb.getInt(ratingIndex))
//                                .append("\t\t(" + cursorTableDb.getString(descriptionIndex) + ")")
                        break;
                    }
                }
                tvData.setText(s);

            } while (cursorTableDb.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTableDb.close();
    }

    @Override
    public void onClick(View view) {

    }
}