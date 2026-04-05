package com.example.note;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditNote extends AppCompatActivity {
    EditText m_name;
    EditText m_content;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_note);

        m_name = findViewById(R.id.name_note);

        m_content = findViewById(R.id.content_note);
    }

    public void onClickApply(View view) {
        if (m_name == null || m_content == null) return;

        String name = m_name.getText().toString();
        String content = m_content.getText().toString();

        if (name.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("name_note", name != null ? name : "");
        returnIntent.putExtra("content_note", content != null ? content : "");
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void onClickBack(View view) {
        finish();
    }
}
