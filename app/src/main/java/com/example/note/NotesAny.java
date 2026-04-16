package com.example.note;

import static com.example.note.TypeNote.values;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotesAny extends Notes {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String label = intent.getStringExtra("label");

        TextView labelView = findViewById(R.id.label_notes);

        labelView.setText(label);

        int type = intent.getIntExtra("type", -1);

        Log.d("type", String.valueOf(type));

        if (type != -1) {

            setType(values()[type]);

            int count = 0;

            switch (getType()) {
                case HEAD:
                    count = Note.getHeadCount();

                    setOnClickLong(this::onLongClickAddNoteHead);

                    for (int i = 0; i < count; i++) {
                        addNote(Note.getHeadNote(count - i - 1));
                    }

                    break;
                case TEMPLATE:
                    count = Note.getTemplateCount();

                    setOnClickLong(this::onLongClickAddNoteTemplate);

                    for (int i = 0; i < count; i++) {
                        addNote(Note.getTemplateNote(count - i - 1));
                    }

                    break;
                case DELETED:
                    count = Note.getDeletedCount();

                    setOnClickLong(this::onLongClickAddNoteDelete);

                    for (int i = 0; i < count; i++) {
                        addNote(Note.getDeletedNote(count - i - 1));
                    }

                    break;
            }
        }
    }
}
