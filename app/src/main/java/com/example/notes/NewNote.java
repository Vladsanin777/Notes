package com.example.notes;

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
public class NewNote extends AppCompatActivity {
    EditText m_name;
    EditText m_content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_note);

        m_name = findViewById(R.id.name_note);

        m_content = findViewById(R.id.content_note);
    }

    public boolean isEmpty() {
        if (m_name == null || m_content == null) {
            return true;
        }

        if (getName().isEmpty() && getContent().isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean isEmptyMessage() {
        if (isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void onClickApply(View view) {
        if (!isEmptyMessage()) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("name_note", getName());
            returnIntent.putExtra("content_note", getContent());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    public String getName() {
        return m_name.getText().toString();
    }

    public String getContent() {
        return m_content.getText().toString();
    }

    public void setName(String newName) {
        m_name.setText(newName);
    }

    public void setContent(String newContent) {
        m_content.setText(newContent);
    }

    public void onClickBack(View view) {
        finish();
    }
}
