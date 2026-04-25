package com.example.notes;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotesHeads extends NotesBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showButtonAdd();

        setLabelActivity(getString(R.string.notes));

        allUpdate();
    }

    @Override
    protected void allUpdate() {
        int count = Note.getHeadCount();

        setOnLongClick(this::onLongClickAddNoteHead);

        for (int i = 0; i < count; i++) {
            addNote(Note.getHeadNote(i));
        }
    }

    public boolean onLongClickAddNoteHead(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);

        PopupMenu popup = new PopupMenu(wrapper, view);

        popup.getMenu().add(0, 1, 0, getString(R.string.delete));
        popup.getMenu().add(0, 2, 1, getString(R.string.template));
        popup.getMenu().add(0, 3, 2, getString(R.string.create));
        popup.getMenu().add(0, 4, 3, getString(R.string.history));
        popup.getMenu().add(0, 5, 4, getString(R.string.edit));

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    onClickDeleteNote(view);
                    return true;
                case 2:
                    onClickTemplateNote(view);
                    return true;
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
        return new Note(name, content, TypeNote.HEAD);
    }

    @Override
    protected Note createNote(String name, String content, Note parent) {
        return new Note(name, content, TypeNote.HEAD, parent);
    }
}