package com.example.loginscreenwithcheckbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences myNoteSharedPref;
    private static String NOTE_TEXT = "note_text";
    public static final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 100;
    String INTERNAL_FILENAME = "loginpassint.txt";
    String EXTERNAL_FILENAME = "loginpassext.txt";
    CheckBox checkBox;
    EditText loginText;
    EditText passText;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myNoteSharedPref = getSharedPreferences("MyNote", MODE_PRIVATE);
        loginText = findViewById(R.id.editTextLogin);
        passText = findViewById(R.id.editTextPassword);
        checkBox = findViewById(R.id.checkBox);
        Button loginButton = findViewById(R.id.button_login);
        Button registButton = findViewById(R.id.button_registration);
        setChekBoxFromSharedPref(myNoteSharedPref, checkBox);
        location = myNoteSharedPref.getString(NOTE_TEXT, "");

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    location = "external";
                    //сразу просим разрешения
                    int permissionStatus = ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "Вы дали права на чтение и запись файлов", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_WRITE_STORAGE);
                    }
                } else {
                    location = "internal";

                }
                myNoteSharedPref = getSharedPreferences("MyNote", MODE_PRIVATE);
                SharedPreferences.Editor myEditor = myNoteSharedPref.edit();
                myEditor.putString(NOTE_TEXT, location);
                myEditor.apply();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    readFile(location, loginText.getText().toString(), passText.getText().toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        registButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeFile(location, loginText.getText().toString(), passText.getText().toString());
            }
        });
    }

    //method for setting checkBox based on data from sharedPref
    private void setChekBoxFromSharedPref(SharedPreferences myNoteSharedPref, CheckBox checkBox) {
        location = myNoteSharedPref.getString(NOTE_TEXT, "");
        if (location.equals(null)) {
            checkBox.setChecked(false);
        } else {
            if (location.equals("external")) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }
    }

    // method for login checking
    private void readFile(String location, String loginText, String passText) throws FileNotFoundException {
        Map<String, String> credentials = new HashMap<>();
        switch (location) {
            case "internal":
                try {
                    FileInputStream fileInputStream = openFileInput(INTERNAL_FILENAME);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    loginPassCheck(reader, credentials, loginText, passText);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "File not found", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "external":
                if (isExternalStorageReadable()) {
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                            EXTERNAL_FILENAME);
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    try {
                        loginPassCheck(reader, credentials, loginText, passText);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "file is unavailable", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

        }
    }

    //  method fot writing registration datd
    private void writeFile(String location, String loginText, String passText) {
        if (loginText.equals(null) || passText.equals(null)) {
            Toast.makeText(MainActivity.this, "Please enter login and password", Toast.LENGTH_SHORT).show();
        } else {
            String credentials = loginText + "&&&" + passText + System.getProperty("line.separator");

            switch (location) {
                case "internal":
                    try {
                        FileOutputStream fileOutputStream = openFileOutput(INTERNAL_FILENAME, MODE_PRIVATE);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                        bw.write(credentials);
                        bw.close();
                        Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "external":
                    int permissionStatus = ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        if (isExternalStorageWritable()) {
                            File file = new File(Environment
                                    .getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DOCUMENTS),
                                    EXTERNAL_FILENAME);
                            try {
                                FileWriter fw = new FileWriter(file);
                                fw.write(credentials);
                                fw.close();
                                Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_PERMISSION_WRITE_STORAGE);
                        }
                    }
                    break;

                default:
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        }
    }

    /* Checks if external storage is available to write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //check if password fits the login
    public void loginPassCheck(BufferedReader reader, Map<String, String> credentials, String loginText, String passText) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            String[] loginpass = line.split("&&&");
            credentials.put(loginpass[0], loginpass[1]);
            line = reader.readLine();
        }
        if (credentials.containsKey(loginText)) {
            if (credentials.get(loginText).equals(passText)) {
                Toast.makeText(MainActivity.this, "Login and password are correct", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Login is incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}