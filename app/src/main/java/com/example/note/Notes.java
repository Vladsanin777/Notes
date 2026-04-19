package com.example.note;

import static com.example.note.TypeNote.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;


@RequiresApi(api = Build.VERSION_CODES.O)
public abstract class Notes extends AppCompatActivity {
    public class InterceptableLinearLayout extends LinearLayout {
        private GestureDetector gestureDetector;

        public InterceptableLinearLayout(Context context) {
            super(context);
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    performLongClick();
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            gestureDetector.onTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
        }
    }

    protected final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleNoteResult(result)
    );

    private LinearLayout m_notesLayout;
    private Context m_notesContext;
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
        launcher.launch(intent);
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

    public void onClickHistoryNote(View view) {
        LinearLayout layout = (LinearLayout) view;
        Note note = (Note) layout.getTag();
        Intent intent = new Intent(Notes.this, History.class);
        intent.putExtra("hash", note.getHash());
        launcher.launch(intent);
    }

    abstract protected void handleNoteResult(ActivityResult result);

    abstract protected void allUpdate();

    protected void clearNotes() {
        m_notesLayout.removeAllViews();
    }

    protected void addNote(Note note) {
        if (note != null) {

            LinearLayout mainLayout = createLinearLayoutNote(m_notesContext, LinearLayout.VERTICAL);

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

    private LinearLayout createLinearLayoutFooter(Context parent, int orientation) {
        LinearLayout layout = createLinearLayoutNote(parent, orientation);

        layout.setGravity(Gravity.END);

        return layout;
    }
    private LinearLayout createLinearLayoutNote(Context parent, int orientation) {
        LinearLayout layout = new InterceptableLinearLayout(parent);
        layout.setId(View.generateViewId());
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(orientation);

        layout.setAddStatesFromChildren(true);

        layout.setLongClickable(true);

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

        textView.setLongClickable(false);

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);

                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            }
        });

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

    protected void setOnLongClick(View.OnLongClickListener onLongClick) {
        m_onLongClick = onLongClick;
    }

    public LinearLayout getNotesLayout() {
        return m_notesLayout;
    }

    protected void setLabelActivity(String label) {
        TextView labelView = findViewById(R.id.label_notes);
        labelView.setText(label);
    }

    protected void hideButtonAdd() {
        Button addButton = findViewById(R.id.button_add);
        addButton.setVisibility(View.GONE);
    }

    protected void showButtonAdd() {
        Button addButton = findViewById(R.id.button_add);
        addButton.setVisibility(View.VISIBLE);
    }
}