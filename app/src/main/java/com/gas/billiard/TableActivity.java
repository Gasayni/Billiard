package com.gas.billiard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TableActivity extends AppCompatActivity implements View.OnClickListener{
    TextView tvTypeTable, tvSum;
    AutoCompleteTextView actvNameTable;
    Button btnShowTableReserve;
    List<String> typeTableList = new ArrayList<>();
    List<String> nameTableList = new ArrayList<>();

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables;

    int getNumTable;
    String getClient;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        // Получаем название заголовка
        Intent getIntent = getIntent();
        getNumTable = getIntent.getIntExtra("numTable", 0);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        initNameTableList();

        tvTypeTable = findViewById(R.id.tvTypeTable);
        tvTypeTable.setTextColor(Color.BLACK);
        tvSum = findViewById(R.id.tvSum);
        btnShowTableReserve = findViewById(R.id.btnShowTableReserve);
        btnShowTableReserve.setOnClickListener(this);

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
                // меняем картинку, в зависимости от типа стола
                if (typeTableList.get(i).equals("pool")) {
                    tvSum.setBackgroundResource(R.drawable.table_pool);
                    tvTypeTable.setText("Американский пул");
                } else {
                    tvSum.setBackgroundResource(R.drawable.table_pyramide);
                    tvTypeTable.setText("Русская пирамида");
                }
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent, intent1;
        switch (view.getId()) {
            case R.id.btnShowTableReserve: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Резервы");
                intent.putExtra("getFilterNumTable", getNumTable);
                startActivity(intent);
                break;
            }
        }
    }
}