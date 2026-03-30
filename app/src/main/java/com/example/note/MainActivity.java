package com.example.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.example.note.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'note' library on application startup.
    static {
        System.loadLibrary("note");
    }

    private ActivityMainBinding m_binding;
    private LinearLayout m_notes;
    private Context m_notesContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());

        m_notes = findViewById(R.id.notes);
        m_notesContext = m_notes.getContext();
    }

    public void addNote(View view) {
        LinearLayout note = createLinearLayoutNote(m_notesContext, LinearLayout.VERTICAL);

        m_notes.addView(note);

        Context noteContext = note.getContext();

        note.addView(createTextView(noteContext, "label", 32));

        note.addView(createVertacalSpace(note.getContext(), 15));

        note.addView(createTextView(noteContext, "context", 24));

        LinearLayout footerLayout = createLinearLayoutFooter(
                note.getContext(), LinearLayout.HORIZONTAL);

        note.addView(footerLayout);

        Context footerLayoutContext = footerLayout.getContext();

        footerLayout.addView(createTextView(footerLayoutContext, "rename", 12));

        footerLayout.addView(createHorizontalSpace(footerLayoutContext, 12));

        footerLayout.addView(createTextView(footerLayoutContext, "edited", 12));

        footerLayout.addView(createHorizontalSpace(footerLayoutContext, 12));

        footerLayout.addView(createTextView(footerLayoutContext, "12:00 AM", 12));
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
        TextView tv = new TextView(parent);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        tv.setTextSize(textSize);
        tv.setText(text);
        return tv;
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