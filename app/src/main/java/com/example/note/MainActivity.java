package com.example.note;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.note.databinding.ActivityMainBinding;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    // Used to load the 'note' library on application startup.
    static {
        System.loadLibrary("note");
    }

    private ActivityMainBinding m_binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());

        Note.INIT(getApplicationContext());

    }

    private void onClickNotes(String label, TypeNote type) {
        Intent intent = new Intent(MainActivity.this, Notes.class);
        intent.putExtra("label", label);
        intent.putExtra("type", type.ordinal());
        startActivity(intent);
    }

    public void onClickNotes(View view) {
        onClickNotes("Notes", TypeNote.HEAD);
    }

    public void onClickTemplate(View view) {
        onClickNotes("Templates", TypeNote.TEMPLATE);
    }

    public void onClickDeleted(View view) {
        onClickNotes("Deleted", TypeNote.DELETED);
    }
}