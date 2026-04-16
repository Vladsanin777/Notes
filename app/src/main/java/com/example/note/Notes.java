package com.example.note;

import static com.example.note.TypeNote.*;

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
import android.util.Log;
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
    private TypeNote m_type;
    private View.OnLongClickListener m_onLongClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notes);


        m_notesLayout = findViewById(R.id.notes);
        m_notesContext = m_notesLayout.getContext();
    }

    public void onClickEditNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();
        Intent intent = new Intent(Notes.this, EditNote.class);
        intent.putExtra("label", "Edit note");
        intent.putExtra("hash_note", note.getHash());
        intent.putExtra("type_note", note.getType().ordinal());
        addNoteLauncher.launch(intent);
    }

    public void onClickHeadNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();

        note.head();

        m_notesLayout.removeView(noteLayout);
    }

    public void onClickTemplateNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();

        note.template();

        m_notesLayout.removeView(noteLayout);
    }

    public void onClickDeleteNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();

        note.delete();

        m_notesLayout.removeView(noteLayout);
    }

    public void onClickDeletePermanentlyNote(View view) {
        LinearLayout noteLayout = (LinearLayout) view;
        Note note = (Note) noteLayout.getTag();

        note.deleteForce();

        m_notesLayout.removeView(noteLayout);
    }

    public void onClickAddNote(View view) {
        Intent intent = new Intent(Notes.this, EditNote.class);
        intent.putExtra("label", "New note");
        intent.putExtra("type_note", m_type.ordinal());
        addNoteLauncher.launch(intent);
    }

    public void onClickHistoryNote(View view) {
        LinearLayout layout = (LinearLayout) view;
        Note note = (Note) layout.getTag();
        Intent intent = new Intent(Notes.this, History.class);
        intent.putExtra("label", "History");
        intent.putExtra("type", m_type.ordinal());
        intent.putExtra("hash", note.getHash());
        addNoteLauncher.launch(intent);
    }

    private void handleNoteResult(ActivityResult result) {
        Log.d("test", "Return in Notes.java");
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();

            String hashNote = data.getStringExtra("hash_note");

            int typeId = data.getIntExtra("type_note", -1);


            String name = data.getStringExtra("name_note");
            String content = data.getStringExtra("content_note");


            if (typeId != -1) {
                TypeNote type = TypeNote.values()[typeId];

                Note note = null;
                Note noteOld = null;

                if (hashNote != null) {

                    noteOld = Note.getNote(hashNote);

                    note = new Note(name, content, type, noteOld);

                    View view = m_notesLayout.findViewWithTag(noteOld);

                    m_notesLayout.removeView(view);
                } else {
                    note = new Note(name, content, type);
                }
                Log.d("type", note.getContent());
                Log.d("test", "Type: " + type + " vs M_Type: " + m_type);
                if (type == m_type) {
                    addNote(note);
                }
            }
        }
    }

    protected void addNote(Note note) {
        if (note != null) {

            LinearLayout mainLayout = createLinearLayoutNote(m_notesContext, LinearLayout.VERTICAL);

            mainLayout.setClickable(true);
            mainLayout.setFocusable(true);

            mainLayout.setTag(note);

            mainLayout.setOnLongClickListener(m_onLongClick);

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
    }

    public boolean onLongClickAddNoteHead(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

        popup.getMenu().add(0, 1, 0, "Delete");
        popup.getMenu().add(0, 2, 1, "Template");
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
                    onClickTemplateNote(view);
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

    public boolean onLongClickHistoryNote(View view) {
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

    protected void setType(TypeNote type) {
        m_type = type;
    }

    public TypeNote getType() {
        return m_type;
    }

    protected void setOnClickLong(View.OnLongClickListener onLongClick) {
        m_onLongClick = onLongClick;
    }
}