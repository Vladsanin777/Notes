package com.example.note;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Space;
import android.widget.TextView;


@RequiresApi(api = Build.VERSION_CODES.O)
public class Notes extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> addNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleNoteResult(result)
    );

    private LinearLayout m_notesLayout;
    private Context m_notesContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_notesLayout = findViewById(R.id.notes);
        m_notesContext = m_notesLayout.getContext();

        int count = Note.getCount();
        for (int i = 0; i < count; i++) {
            addNote(Note.getNote(i));
        }
    }

    public void onClickEditNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();
        Intent intent = new Intent(Notes.this, EditNote.class);
        intent.putExtra("label", "Edit note");
        intent.putExtra("id_note", note.getId());
        addNoteLauncher.launch(intent);
    }

    public void onClickDeleteNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();

        note.delete();

        m_notesLayout.removeView(noteLayout);
    }

    public void onClickAddNote(View view) {
        Intent intent = new Intent(Notes.this, EditNote.class);
        intent.putExtra("label", "New note");
        addNoteLauncher.launch(intent);
    }

    private void handleNoteResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            int idNote = data.getIntExtra("id_note", -1);

            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");

            Note note;
            if (idNote != -1) {
                Note noteOld = Note.getNote(idNote);

                note = new Note(name, content, noteOld);

                View view = m_notesLayout.findViewWithTag(noteOld);

                m_notesLayout.removeView(view);
            } else {
                note = new Note(name, content);
            }

            addNote(note);
        }
    }

    public void addNote(Note note) {
        if (note.isDeleted())
            return;

        LinearLayout mainLayout = createLinearLayoutNote(m_notesContext, LinearLayout.VERTICAL);

        mainLayout.setClickable(true);
        mainLayout.setFocusable(true);

        mainLayout.setTag(note);

        mainLayout.setOnLongClickListener(this::onLongClickAddNote);

        m_notesLayout.addView(mainLayout, 0);

        Context noteContext = mainLayout.getContext();

        mainLayout.addView(createTextView(noteContext, note.getName(), 32, 2));

        mainLayout.addView(createVertacalSpace(mainLayout.getContext(), 15));

        mainLayout.addView(createTextView(noteContext, note.getContent(), 24, 6));

        LinearLayout footerLayout = createLinearLayoutFooter(
                mainLayout.getContext(), LinearLayout.HORIZONTAL);

        mainLayout.addView(footerLayout);

        Context footerLayoutContext = footerLayout.getContext();

        footerLayout.addView(createTextView(footerLayoutContext, "renamed", 12, note.isRenamed()));

        footerLayout.addView(createHorizontalSpace(footerLayoutContext, 12));

        footerLayout.addView(createTextView(footerLayoutContext, "edited", 12, note.isEdited()));

        footerLayout.addView(createHorizontalSpace(footerLayoutContext, 12));

        footerLayout.addView(createTextView(footerLayoutContext, note.getTime().toString(), 12));
    }

    public boolean onLongClickAddNote(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenu().add(0, 1, 0, "Edit");
        popup.getMenu().add(0, 2, 1, "Delete");
        popup.getMenu().add(0, 3, 2, "Create");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: {
                    onClickEditNote(view);
                    return true;
                }
                case 2: {
                    onClickDeleteNote(view);
                    return true;
                }
                case 3:
                    onClickAddNote(view);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();

        return true;
    }

    private LinearLayout createLinearLayoutFooter(Context parent, int orientation) {
        LinearLayout layout = createLinearLayoutNote(parent, orientation);

        layout.setGravity(Gravity.END);

        return layout;
    }
    private LinearLayout createLinearLayoutNote(Context parent, int orientation) {
        LinearLayout layout = new LinearLayout(parent);
        layout.setId(View.generateViewId());
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(orientation);
        return layout;
    }

    private TextView createTextView(Context parent, String text, int textSize) {
        TextView textView = new TextView(parent);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        textView.setTextSize(textSize);
        textView.setText(text);
        return textView;
    }

    private TextView createTextView(Context parent, String text, int textSize, boolean isVisibility) {
        TextView textView = createTextView(parent, text, textSize);
        textView.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        return textView;
    }

    private TextView createTextView(Context parent, String text, int textSize, int countLine) {
        TextView textView = createTextView(parent, text, textSize);

        textView.setGravity(Gravity.TOP | Gravity.START);

        textView.setMaxLines(countLine);

        textView.setMovementMethod(new ScrollingMovementMethod());

        textView.setVerticalScrollBarEnabled(true);
        return textView;
    }

    private Space createVertacalSpace(Context parent, int dp) {
        Space space = new Space(parent);
        int px = dpToPx(dp);
        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px));
        return space;
    }

    private Space createHorizontalSpace(Context parent, int dp) {
        Space space = new Space(parent);
        int px = dpToPx(dp);
        space.setLayoutParams(new LinearLayout.LayoutParams(px, LinearLayout.LayoutParams.MATCH_PARENT));
        return space;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * A native method that is implemented by the 'note' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}