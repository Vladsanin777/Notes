package com.example.note;

import static com.example.note.TypeNote.values;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.O)
public class History extends Notes {
    String m_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLabelActivity("History");

        hideButtonAdd();

        Intent intent = getIntent();

        m_hash = intent.getStringExtra("hash");

        setOnLongClick(this::onLongClickHistoryNote);

        allUpdate();
    }

    @Override
    protected void handleNoteResult(ActivityResult result) {
        Log.d("test", "Return in Notes.java");
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            boolean isAllUpdate = data.getBooleanExtra("all_update", false);

            String hashNote = data.getStringExtra("hash_note");

            int typeId = data.getIntExtra("type_note", -1);


            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");


            if (typeId != -1) {
                TypeNote type = TypeNote.values()[typeId];

                Note note = null;
                Note noteOld = null;


                if (hashNote != null) {
                    if (name != null && content != null) {

                        noteOld = Note.getNote(hashNote);

                        note = new Note(name, content, type, noteOld);

                    } else {
                        note = new Note(name, content, type);
                    }

                    m_hash = note.getHash();

                    allUpdate();
                }
            }
        }
    }
    @Override
    public void allUpdate() {
        clearNotes();
        if (m_hash != null) {
            Note note = Note.getNote(m_hash);

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

    protected boolean onLongClickHistoryNote(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenu().add(0, 1, 0, "Delete");
        popup.getMenu().add(0, 2, 1, "Return as template");
        popup.getMenu().add(0, 3, 2, "Return as note");
        popup.getMenu().add(0, 4, 3, "New child");


        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: {
                    onClickDeleteNote(view);
                    return true;
                }
                case 2: {
                    onClickTemplateNote(view);
                    return true;
                }
                case 3:
                    onClickHeadNote(view);
                    return true;
                case 4:
                    onClickEditNote(view);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();

        return true;
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("all_update", true);
        setResult(Activity.RESULT_OK, returnIntent);
        super.finish();
    }
}