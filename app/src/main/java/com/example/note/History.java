package com.example.note;

import static com.example.note.TypeNote.values;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.O)
public class History extends Notes {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        String label = intent.getStringExtra("label");

        TextView labelView = findViewById(R.id.label_notes);

        labelView.setText(label);

        int type = intent.getIntExtra("type", -1);

        Log.d("type", String.valueOf(type));

        String hash = intent.getStringExtra("hash");

        if (type != -1 && hash != null) {

            setType(values()[type]);

            Note note = Note.getNote(hash);

            setOnClickLong(this::onLongClickHistoryNote);

            ArrayList<Note> list = new ArrayList<Note>();

            while (note != null) {
                list.add(note);
                note = note.getParent();
            }

            int count = list.size();

            for (int indexList = 0; indexList < count; indexList++) {
                addNote(list.get(count - indexList - 1));
            }
        }
    }
}