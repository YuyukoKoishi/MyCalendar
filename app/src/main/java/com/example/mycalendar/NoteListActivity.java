package com.example.mycalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NoteListActivity extends Activity {
    private SharedPreferences prefs;
    private List<String[]> notes = new ArrayList<String[]>();
    private float startX, startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        prefs = getSharedPreferences("calendar_notes", Context.MODE_PRIVATE);
        loadNotes();
        final ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(new NoteAdapter());
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override public void onItemClick(android.widget.AdapterView<?> parent, View v, int position, long id) {
                String[] n = notes.get(position);
                Intent i = new Intent(NoteListActivity.this, NoteEditActivity.class);
                i.putExtra("year", Integer.parseInt(n[0]));
                i.putExtra("month", Integer.parseInt(n[1]) - 1);
                i.putExtra("day", Integer.parseInt(n[2]));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        ((BaseAdapter) ((ListView) findViewById(R.id.list)).getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startX = ev.getX(); startY = ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            float dx = ev.getX() - startX, dy = ev.getY() - startY;
            if (dy > 150 && Math.abs(dy) > Math.abs(dx) * 2) {
                finish();
                overridePendingTransition(0, R.anim.slide_out_to_bottom);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_out_to_bottom);
    }

    private String keyOf(String[] n) { return n[0] + "-" + n[1] + "-" + n[2]; }

    private void loadNotes() {
        notes.clear();
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> e : all.entrySet()) {
            String[] parts = e.getKey().split("-");
            if (parts.length == 3) notes.add(new String[]{parts[0], parts[1], parts[2], String.valueOf(e.getValue())});
        }
        Collections.sort(notes, new java.util.Comparator<String[]>() {
            @Override public int compare(String[] a, String[] b) {
                int ya = Integer.parseInt(a[0]), yb = Integer.parseInt(b[0]);
                if (ya != yb) return yb - ya;
                int ma = Integer.parseInt(a[1]), mb = Integer.parseInt(b[1]);
                if (ma != mb) return mb - ma;
                return Integer.parseInt(b[2]) - Integer.parseInt(a[2]);
            }
        });
    }

    private class NoteAdapter extends BaseAdapter {
        public int getCount() { return notes.size(); }
        public Object getItem(int p) { return null; }
        public long getItemId(int p) { return p; }
        public View getView(final int p, View cv, ViewGroup parent) {
            if (cv == null) cv = View.inflate(NoteListActivity.this, R.layout.item_note, null);
            final String[] n = notes.get(p);
            ((TextView) cv.findViewById(R.id.tvDate)).setText(n[0] + "." + n[1] + "." + n[2]);
            ((TextView) cv.findViewById(R.id.tvText)).setText(n[3]);
            View del = cv.findViewById(R.id.btnDelete);
            del.setBackgroundColor(getResources().getColor(R.color.theme_green));
            ((ImageView) cv.findViewById(R.id.ivDelete)).setImageResource(R.drawable.ic_delete_white);
            del.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    prefs.edit().remove(keyOf(n)).apply();
                    loadNotes();
                    ((BaseAdapter) ((ListView) findViewById(R.id.list)).getAdapter()).notifyDataSetChanged();
                }
            });
            return cv;
        }
    }
}
