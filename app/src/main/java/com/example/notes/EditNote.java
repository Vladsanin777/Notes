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
import androidx.appcompat.widget.Toolbar;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EditNote extends NewNote {
    private Note m_noteParent;
    private String m_hashParent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView labelView = findViewById(R.id.text_new_note);

        labelView.setText(R.string.edit_note);

        Intent intent = getIntent();

        m_hashParent = intent.getStringExtra("hash_parent");

        if (m_hashParent != null) {
            m_noteParent = Note.getNote(m_hashParent);

            if (m_noteParent != null) {
                setName(m_noteParent.getName());

                setContent(m_noteParent.getContent());
            }
        }
    }

    public boolean isEqualParnet() {
        return getName().equals(m_noteParent.getName()) &&
                getContent().equals(m_noteParent.getContent());
    }

    public boolean isEqualParentMessage() {
        if (isEqualParnet()) {
            Toast.makeText(this, getString(R.string.not_edited), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onClickApply(View view) {
        if (!isEmptyMessage() && !isEqualParentMessage()) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("hash_parent", m_hashParent);
            returnIntent.putExtra("name_note", getName());
            returnIntent.putExtra("content_note", getContent());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }
}
