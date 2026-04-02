package com.example.note;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EditNote extends AppCompatActivity {
    private Note m_note = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_note);

        Bundle arguments = getIntent().getExtras();

        m_note = null;
        if(arguments!=null){
            m_note = (Note) arguments.getSerializable(Note.class.getSimpleName());
        }
    }
}
