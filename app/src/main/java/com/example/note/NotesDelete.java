package com.example.note;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotesDelete extends NotesBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideButtonAdd();

        setLabelActivity("Delete");

        allUpdate();
    }

    @Override
    protected void allUpdate() {
        int count = Note.getDeletedCount();

        setOnLongClick(this::onLongClickAddNoteDelete);

        for (int i = 0; i < count; i++) {
            addNote(Note.getDeletedNote(i));
        }
    }

    public boolean onLongClickAddNoteDelete(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenu().add(0, 1, 0, "Delete permanently");
        popup.getMenu().add(0, 2, 1, "Return as template");
        popup.getMenu().add(0, 3, 1, "Return as note");
        popup.getMenu().add(0, 4, 3, "History");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: {
                    onClickDeletePermanentlyNote(view);
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
                    onClickHistoryNote(view);
                default:
                    return false;
            }
        });

        popup.show();

        return true;
    }

    @Override
    protected Note createNote(String name, String content) {
        return new Note(name, content, TypeNote.DELETED);
    }

    @Override
    protected Note createNote(String name, String content, Note parent) {
        return new Note(name, content, TypeNote.DELETED);
    }
}
