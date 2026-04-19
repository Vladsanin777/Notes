package com.example.note;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.note.databinding.ActivityMainBinding;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding m_binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());

        Note.INIT(getApplicationContext());

    }

    public void onClickNotes(View view) {
        Intent intent = new Intent(MainActivity.this, NotesHeads.class);
        startActivity(intent);
    }

    public void onClickTemplate(View view) {
        Intent intent = new Intent(MainActivity.this, NotesTemplate.class);
        startActivity(intent);
    }

    public void onClickDeleted(View view) {
        Intent intent = new Intent(MainActivity.this, NotesDelete.class);
        startActivity(intent);
    }
}