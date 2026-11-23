package com.example.mycalendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NoteEditActivity extends Activity {
    private EditText etNote;
    private TextView tvDate;
    private SharedPreferences prefs;
    private String key;
    private int y, m, d;
    private float startX, startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        prefs = getSharedPreferences("calendar_notes", Context.MODE_PRIVATE);
        y = getIntent().getIntExtra("year", 0);
        m = getIntent().getIntExtra("month", 0);
        d = getIntent().getIntExtra("day", 0);
        key = y + "-" + (m + 1) + "-" + d;

        etNote = (EditText) findViewById(R.id.etNote);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvDate.setText(y + "年" + (m + 1) + "月" + d + "日");
        etNote.setText(prefs.getString(key, ""));
        etNote.setSelection(etNote.getText().length());

        ((Button) findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String text = etNote.getText().toString().trim();
                if (text.length() == 0) prefs.edit().remove(key).apply();
                else prefs.edit().putString(key, text).apply();
                finish();
                overridePendingTransition(0, R.anim.slide_out_to_left);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startX = ev.getX(); startY = ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            float dx = ev.getX() - startX, dy = ev.getY() - startY;
            if (dx < -130 && Math.abs(dx) > Math.abs(dy) * 2) {
                finish();
                overridePendingTransition(0, R.anim.slide_out_to_left);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_out_to_left);
    }
}
