package com.gas.billiard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    OptionallyClass optionalClass = new OptionallyClass();
    // определим, сколько заказов есть на этот день
    List<List<OrderClass>> allOrdersList = optionalClass.findAllOrders(this, "Необязательно", false);
    List<AdminClass> allAdminsList = optionalClass.findAllAdmins(this, false);
    List<ClientClass> allClientsList = optionalClass.findAllClients(this, false);
    List<TableClass> allTablesList = optionalClass.findAllTables(this, false);
    Button btnEmployee, btnBack, btnTables, btnShare;
    String getAdminName;
    int superPass = 1111, checkPass;

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
        btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(this);
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
            case R.id.btnShare: {
                openDialogShareDB();
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

    // метод вызывает диалоговое окно изменения резерва
    private void openDialogShareDB() {
        LayoutInflater inflater = LayoutInflater.from(SettingActivity.this);
        View subView = inflater.inflate(R.layout.dialog_chose_num, null);
        final EditText etNum = (EditText) subView.findViewById(R.id.etNum);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отправить клиентов\n")
                .setMessage("Введите пароль Супер Администратора")
                .setView(subView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPass = Integer.parseInt(etNum.getText().toString());
                        // сначала по номеру резерва узнаем все данные резерва
                        if (superPass == checkPass) {
                            shareDB();
                        } else
                            Toast.makeText(SettingActivity.this, "Пароль введен неверно", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SettingActivity.this, "Отменено", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void shareDB() {
        Log.i("SettingActivity", "\n ...//...    shareDB");
        String path = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/GasCsv.csv"); // Here path file name is MyCsvFile.path
        Log.i("SettingActivity", "AbsolutePath = " + Environment.getExternalStorageDirectory().getAbsolutePath());

        File file = new File(path);
        Uri uri = FileProvider.getUriForFile(
                SettingActivity.this,
                "com.example.homefolder.gas.provider", //(use your app signature + ".provider" )
                file);


        CSVWriter writer;
        try {
            writer = new CSVWriter(new FileWriter(path));

            List<String[]> data = fillingData();

            Log.i("SettingActivity", "data: " + Arrays.toString(data.get(data.size() - 1)));

            writer.writeAll(data); // data is adding to path

            writer.close();
            writeFile(file.getName(), data);

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("SettingActivity", "IOException_111");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareBody = "Your Body here";
        String shareTitle = "Your Title here";
        String to[] = {"gasajni@mail.ru"};
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        // передаем название заголовка
        intent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Pick an Email provider"));
    }


    private static void writeFile(String path, List<String[]> writeList) {
        Log.i("SettingActivity", "\n ...//...    writeFile");
        //        Записываем строку в файл
        String writeLine = "";
        for (int i = 0; i < writeList.size(); i++) {
            StringBuilder writeStr = new StringBuilder();
            for (int j = 0; j < writeList.get(i).length; j++) {
                writeStr.append(writeList.get(i)[j])/*
                        .append("\t")*/;
            }
            Log.i("SettingActivity", "\twriteLine = " + writeLine);
            writeLine = writeStr.toString();

            try (
                    FileWriter writer = new FileWriter(path, false)) {
                // запись всей строки
                new FileWriter(path, false).close();
                writer.write(writeLine);
                writer.flush();
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }


    }

    private List<String[]> fillingData() {
        List<String[]> data = new ArrayList<String[]>();
        for (int i = 0; i < allClientsList.size(); i++) {
            data.add(new String[]{allClientsList.get(i).getName(), allClientsList.get(i).getPhone()});
        }
        return data;
    }
}