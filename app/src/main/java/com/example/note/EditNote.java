package com.example.note;

import com.example.note.TypeNote.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditNote extends AppCompatActivity {
    EditText m_name;
    EditText m_content;
    String m_hashNote;

    TypeNote m_type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_note);

        m_name = findViewById(R.id.name_note);

        m_content = findViewById(R.id.content_note);

        Intent intent = getIntent();
        String label = intent.getStringExtra("label");

        TextView labelView = findViewById(R.id.text_new_note);

        labelView.setText(label);

        m_hashNote = intent.getStringExtra("hash_note");

        if (m_hashNote != null) {


            Note note = null;

            note = Note.getNote(m_hashNote);


            String name = note.getName();

            String content = note.getContent();

            m_name.setText(name);
            m_content.setText(content);
        }
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
        returnIntent.putExtra("hash_parent", m_hashNote);
        returnIntent.putExtra("name_note", name);
        returnIntent.putExtra("content_note", content);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void onClickBack(View view) {
        finish();
    }
}
