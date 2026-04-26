package com.example.notes;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotesDeleted extends NotesBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideButtonAdd();

        setLabelActivity(getString(R.string.deleted));

        allUpdate();
    }

    @Override
    protected void allUpdate() {
        super.allUpdate();

        int count = Note.getDeletedCount();

        setOnLongClick(this::onLongClickAddNoteDelete);

        for (int i = 0; i < count; i++) {
            addNote(Note.getDeletedNote(i));
        }
    }

    public boolean onLongClickAddNoteDelete(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);

        PopupMenu popup = new PopupMenu(wrapper, view);

        popup.getMenu().add(0, 1, 0, getString(R.string.delete_permanently));
        popup.getMenu().add(0, 2, 1, getString(R.string.return_as_template));
        popup.getMenu().add(0, 3, 1, getString(R.string.return_as_note));
        popup.getMenu().add(0, 4, 3, getString(R.string.history));

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
        return null;
    }

    @Override
    protected Note createNote(String name, String content, Note parent) {
        return null;
    }
}
