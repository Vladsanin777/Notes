package com.example.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

    public void addNote(Note data) {
        LinearLayout note = new LinearLayout(m_notesContext);
        note.setId(View.generateViewId());
        note.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        note.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(m_notesContext);
        label.setId(View.generateViewId());
        label.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        label.setTextSize(32);
        label.setText("label");
        note.addView(label);

        Space space = new Space(m_notesContext);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(15)));
        note.addView(space);

        TextView content = new TextView(m_notesContext);
        content.setId(View.generateViewId());
        content.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        content.setTextSize(24);
        content.setText("content");
        note.addView(content);

        LinearLayout footerLayout = new LinearLayout(m_notesContext);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        footerLayout.setLayoutParams(footerParams);
        footerLayout.setGravity(Gravity.END);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView rename1 = createSmallTextView("rename");
        footerLayout.addView(rename1);

        footerLayout.addView(createHorizontalSpace(12));

        TextView edited1 = createSmallTextView("edited");
        footerLayout.addView(edited1);

        footerLayout.addView(createHorizontalSpace(12));

        TextView time1 = createSmallTextView("12:00 AM");
        footerLayout.addView(time1);

        note.addView(footerLayout);

        setContentView(note);
    }

    private TextView createSmallTextView(String text) {
        TextView tv = new TextView(m_notesContext);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        tv.setTextSize(12);
        tv.setText(text);
        return tv;
    }

    private Space createHorizontalSpace(int dp) {
        Space space = new Space(m_notesContext);
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