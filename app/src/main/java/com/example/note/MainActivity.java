package com.example.note;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.note.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'note' library on application startup.
    static {
        System.loadLibrary("note");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    /**
     * A native method that is implemented by the 'note' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}