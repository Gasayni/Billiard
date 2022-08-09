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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TableActivity extends AppCompatActivity {
    OptionallyClass option = new OptionallyClass();
    private final int tableCount = 19;
    TextView tvNameTable, tvTypeTable;
    ImageView ivTableType;
    Button btnTable;
    LinearLayout linTable;
    List<Button> btnTableTagsList = new ArrayList<>();
    List<String> typeTableList = new ArrayList<>();

    // БД
    DBHelper dbHelper;
    SQLiteDatabase database;
    ContentValues contentValues;
    Cursor cursorTables, cursorOrders;

    int getNumTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        // работа с БД
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();

        tvNameTable = findViewById(R.id.tvNameTable);
        tvNameTable.setTextColor(Color.BLACK);

        tvTypeTable = findViewById(R.id.tvTypeTable);
        tvTypeTable.setTextColor(Color.BLACK);

        ivTableType = findViewById(R.id.ivTableType);
        linTable = findViewById(R.id.linTable);

        // Получаем название заголовка
        Intent getIntent = getIntent();
        getNumTable = getIntent.getIntExtra("numTable", 0);

        addBtnTable();
        choseTable();


    }

    @SuppressLint("SetTextI18n")
    private void addBtnTable() {
        int marginLength = option.convertDpToPixels(this, 6);

        for (int i = 0; i < tableCount; i++) {
            btnTable = new Button(linTable.getContext());

            btnTable.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            btnTable.setTag("btnInfoTable" + (1+i));
            btnTableTagsList.add(btnTable);
            btnTable.setText("Стол " + (i+1));
            btnTable.setTextSize(12);
            btnTable.setWidth(option.convertDpToPixels(this, 50));
            btnTable.setHeight(option.convertDpToPixels(this, 25));
            btnTable.setBackgroundResource(R.drawable.btn_style_4);

            linTable.addView(btnTable);
        }
    }

    private void choseTable() {
        // получаем данные c табл "tables"
        cursorTables = database.query(DBHelper.TABLES,
                null, null, null,
                null, null, null);
        if (cursorTables.moveToFirst()) {
            int numberTableIndex = cursorTables.getColumnIndex(DBHelper.KEY_ID);
            int typeIndex = cursorTables.getColumnIndex(DBHelper.KEY_TYPE);
            int descriptionIndex = cursorTables.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            do {
                // инициализируем каждую кнопку шапки стола
                int numTable = cursorTables.getInt(numberTableIndex);
                btnTable = btnTableTagsList.get(numTable-1);
                typeTableList.add(cursorTables.getString(typeIndex));

                btnTable.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        // меняем стол
                        tvNameTable.setText("Стол № " + numTable);

                        // меняем картинку, в зависимости от типа стола
                        if (typeTableList.get(numTable-1).equals("pool")) {
                            ivTableType.setImageResource(R.drawable.table_pool);
                            tvTypeTable.setText("Американский пул");
                        } else if (typeTableList.get(numTable-1).equals("pyramid")) {
                            ivTableType.setImageResource(R.drawable.table_pyramide);
                            tvTypeTable.setText("Русская пирамида");
                        }
                    }
                });

            } while (cursorTables.moveToNext());
        } else {
            Log.d("Gas", "0 rows");
        }
        cursorTables.close();

        // вызваем изначально изменения, в зависимости от нажатой кнопки
        firstChangeTableActivity();
    }

    public void firstChangeTableActivity() {
        // меняем стол
        tvNameTable.setText("Стол № " + getNumTable);
        // меняем картинку, в зависимости от типа стола
        if (typeTableList.get(getNumTable-1).equals("pool")) {
            ivTableType.setImageResource(R.drawable.table_pool);
            tvTypeTable.setText("Американский пул");
        } else if (typeTableList.get(getNumTable-1).equals("pyramid")) {
            ivTableType.setImageResource(R.drawable.table_pyramide);
            tvTypeTable.setText("Русская пирамида");
        }
    }
}