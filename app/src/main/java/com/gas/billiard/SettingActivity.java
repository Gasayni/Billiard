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
    Button btnEmployee, btnBack, btnTables;
    String getAdminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // Получаем название заголовка
        Intent getIntent = getIntent();
        getAdminName = getIntent.getStringExtra("adminName");


        btnEmployee = findViewById(R.id.btnEmployee);
        btnEmployee.setOnClickListener(this);
        btnTables = findViewById(R.id.btnTables);
        btnTables.setOnClickListener(this);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {

            case R.id.btnTables: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Столы");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.btnEmployee: {
                intent = new Intent("editDBActivity");
                // передаем название заголовка
                intent.putExtra("headName", "Сотрудники");
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
            case R.id.btnBack: {
                intent = new Intent(SettingActivity.this, CommonActivity.class);
                intent.putExtra("adminName", getAdminName);
                startActivity(intent);
                break;
            }
        }
    }
}