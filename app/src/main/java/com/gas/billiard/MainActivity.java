package com.gas.billiard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass optionalClass = new OptionallyClass();
    AutoCompleteTextView actvAdmin;
    EditText etPas;
    Button btnEnter;

    List<AdminClass> allAdminsList = new ArrayList<>();
    List<String> adminsList = new ArrayList<>();
    List<String> passList = new ArrayList<>();


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        allAdminsList = optionalClass.findAllAdmins(this, false);
        for (int i = 0; i < allAdminsList.size(); i++) {
            adminsList.add(allAdminsList.get(i).getName());
            passList.add(allAdminsList.get(i).getPass());
        }

        actvAdmin = findViewById(R.id.actvAdmin);
        actvAdmin.setShowSoftInputOnFocus(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, adminsList);
        actvAdmin.setOnTouchListener((v, event) -> {
            actvAdmin.showDropDown();
            return false;
        });
        actvAdmin.setAdapter(adapter);
        actvAdmin.setText("Алена Водонаева");   // проверка
        etPas = findViewById(R.id.etPas);
        etPas.setText("1111");        // проверка

        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this);

        // создаем список всех резервов (для ускорения)
        optionalClass.findAllOrders(MainActivity.this, optionalClass.getWorkDay(), true);
        // создаем список всех клиентов (для ускорения)
        optionalClass.findAllClients(MainActivity.this, true);
        // создаем список всех столов (для ускорения)
        optionalClass.findAllTables(MainActivity.this, true);

        // проверяем старые резервы
        optionalClass.checkOldReserve(MainActivity.this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.btnEnter) {
            if (checkMethod()) {
                intent = new Intent("commonActivity");
                // передаем имя админа
                intent.putExtra("headName", "Авторизация");
                intent.putExtra("adminName", actvAdmin.getText().toString());
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Ошибка авторизации", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
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