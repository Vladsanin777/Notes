package com.example.note;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotesTemplate extends NotesBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showButtonAdd();

        setLabelActivity("Heads");

        allUpdate();
    }

    @Override
    protected void allUpdate() {
        int count = Note.getTemplateCount();

        setOnLongClick(this::onLongClickAddNoteTemplate);

        for (int i = 0; i < count; i++) {
            addNote(Note.getTemplateNote(i));
        }
    }

    public boolean onLongClickAddNoteTemplate(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenu().add(0, 1, 0, "Delete");
        popup.getMenu().add(0, 2, 1, "Move to note");
        popup.getMenu().add(0, 3, 2, "Create");
        popup.getMenu().add(0, 4, 3, "History");
        popup.getMenu().add(0, 5, 4, "Edit");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: {
                    onClickDeleteNote(view);
                    return true;
                }
                case 2: {
                    onClickHeadNote(view);
                    return true;
                }
                case 3:
                    onClickAddNote(view);
                    return true;
                case 4:
                    onClickHistoryNote(view);
                    return true;
                case 5:
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
    protected Note createNote(String name, String content) {
        return new Note(name, content, TypeNote.TEMPLATE);
    }

    @Override
    protected Note createNote(String name, String content, Note parent) {
        return new Note(name, content, TypeNote.TEMPLATE);
    }
}