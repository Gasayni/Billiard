package com.gas.billiard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnTariff, btnEmployee, btnClient, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnTariff = findViewById(R.id.btnTariff);
        btnTariff.setOnClickListener(this);
        btnEmployee = findViewById(R.id.btnEmployee);
        btnEmployee.setOnClickListener(this);
        btnClient = findViewById(R.id.btnClient);
        btnClient.setOnClickListener(this);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // переключаемся на редактор резерва
            case R.id.btnTariff: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Тарифы");
                startActivity(intent);
                break;
            }
            case R.id.btnEmployee: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Сотрудники");
                startActivity(intent);
                break;
            }
            case R.id.btnClient: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Клиенты");
                startActivity(intent);
                break;
            }
            case R.id.btnBack: {
                intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}