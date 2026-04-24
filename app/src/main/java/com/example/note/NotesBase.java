package com.example.note;

import static com.example.note.TypeNote.values;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
abstract public class NotesBase extends Notes {
    public void onClickAddNote(View view) {
        Intent intent = new Intent(this, EditNote.class);
        intent.putExtra("label", getString(R.string.new_note));
        launcher.launch(intent);
    }

    @Override
    protected void handleNoteResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            boolean isAllUpdate = data.getBooleanExtra("all_update", false);

            String hashParent = data.getStringExtra("hash_parent");

            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");


            Note note = null;

            Note noteOld = Note.getNote(hashParent);


            if (name != null || content != null) {
                if (noteOld != null) {
                    note = createNote(name, content, noteOld);

                    View view = getNotesLayout().findViewWithTag(noteOld);

                    getNotesLayout().removeView(view);
                } else {
                    note = createNote(name, content);
                }

                addNote(note);
            }

            if (isAllUpdate) {
                allUpdate();
            }
        }
    }

    abstract protected Note createNote(String name, String content);

    abstract protected Note createNote(String name, String content, Note parent);
}