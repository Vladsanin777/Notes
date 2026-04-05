package com.example.note;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.example.note.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'note' library on application startup.
    static {
        System.loadLibrary("note");
    }

    private ArrayList<Note> m_notes;
    private final ActivityResultLauncher<Intent> addNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleNoteResult(result)
    );

    private ActivityMainBinding m_binding;
    private LinearLayout m_notesLayout;
    private Context m_notesContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());

        m_notesLayout = findViewById(R.id.notes);
        m_notesContext = m_notesLayout.getContext();

        Note.setContext(getApplicationContext());

        m_notes = new ArrayList<Note>();

        String[] files = fileList();

        for (String hashNote : files) {
            Note note = Note.deserialize(hashNote);
            addNote(note);
        }

    }

    public void onClickAddNote(View view) {
        Intent intent = new Intent(MainActivity.this, EditNote.class);
        addNoteLauncher.launch(intent);

    }

    private void handleNoteResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");

            Note note = new Note(name, content);
            addNote(note);
        }
    }


    public void addNote(Note note) {
        m_notes.add(note);

        LinearLayout mainLayout = createLinearLayoutNote(m_notesContext, LinearLayout.VERTICAL);

        m_notesLayout.addView(mainLayout);

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