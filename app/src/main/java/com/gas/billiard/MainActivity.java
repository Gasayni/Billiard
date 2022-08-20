package com.gas.billiard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass optionallyClass = new OptionallyClass();
    AutoCompleteTextView actvAdmin;
    EditText etPas;
    Button btnEnter;
    Button btnSetting;

    List<String> adminsList = new ArrayList<>();
    List<String> passList = new ArrayList<>();

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        adminsList = optionallyClass.initAdmins(this, "adminsList");
        passList = optionallyClass.initAdmins(this, "passList");

        actvAdmin = findViewById(R.id.actvAdmin);
        actvAdmin.setShowSoftInputOnFocus(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, adminsList);
        actvAdmin.setOnTouchListener((v, event) -> {
            actvAdmin.showDropDown();
            return false;
        });
        actvAdmin.setAdapter(adapter);
//        actvAdmin.setText("Алена");   // проверка

        etPas = findViewById(R.id.etPas);
//        etPas.setText("1111");        // проверка

        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btnEnter: {
                if (checkMethod()) {
                    intent = new Intent("commonActivity");
                    // передаем имя админа
                    intent.putExtra("adminName", actvAdmin.getText().toString());
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Ошибка авторизации", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
            }
        }
    }

    // проверяем логин и пароль
    private boolean checkMethod() {
        boolean flag = false;
        for (int i = 0; i < adminsList.size(); i++) {
            // если введенный логин совпадает
            if (actvAdmin.getText().toString().equals(adminsList.get(i))) {
                // если пароль от этого логина совпадает
                if (etPas.getText().toString().equals(passList.get(i))) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}