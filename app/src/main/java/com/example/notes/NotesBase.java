package com.example.notes;

import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
abstract public class NotesBase extends Notes {
    public void onClickAddNote(View view) {
        Intent intent = new Intent(this, NewNote.class);
        launcher.launch(intent);
    }

    @Override
    protected void handleNoteResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            boolean isAllUpdate = data.getBooleanExtra("all_update", false);

            String hashParent = data.getStringExtra("hash_parent");

            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");


            Note note = null;

            if (name != null || content != null) {
                if (hashParent != null) {
                    Note noteOld = Note.getNote(hashParent);
                    if (noteOld != null) {
                        note = createNote(name, content, noteOld);

                        View view = getNotesLayout().findViewWithTag(noteOld);

                        getNotesLayout().removeView(view);
                    } else {
                        note = createNote(name, content);
                    }
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