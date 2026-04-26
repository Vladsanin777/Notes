package com.example.notes;

import static com.example.notes.TypeNote.values;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.O)
public class History extends Notes {
    String m_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLabelActivity(getString(R.string.history));

        hideButtonAdd();

        Intent intent = getIntent();

        m_hash = intent.getStringExtra("hash");

        setOnLongClick(this::onLongClickHistoryNote);

        allUpdate();
    }

    @Override
    protected void handleNoteResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            String hashParent = data.getStringExtra("hash_parent");

            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");


            Note note = null;
            Note noteOld = null;


            if (hashParent != null) {
                noteOld = Note.getNote(hashParent);

                note = new Note(name, content, TypeNote.HEAD, noteOld);
            } else {
                note = new Note(name, content, TypeNote.HEAD);
            }

            m_hash = note.getHash();

            allUpdate();
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
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);

        PopupMenu popup = new PopupMenu(wrapper, view);

        popup.getMenu().add(0, 1, 0, getString(R.string.delete));
        popup.getMenu().add(0, 2, 1, getString(R.string.return_as_template));
        popup.getMenu().add(0, 3, 2, getString(R.string.return_as_note));
        popup.getMenu().add(0, 4, 3, getString(R.string.edit));


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
        setResult(RESULT_OK, returnIntent);
        super.finish();
    }
}