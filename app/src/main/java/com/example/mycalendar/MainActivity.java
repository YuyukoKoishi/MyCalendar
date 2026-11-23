package com.example.mycalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends Activity {
    private static final int COLOR_OTHER_MONTH = Color.parseColor("#555558");
    private static final int COLOR_TODAY = Color.parseColor("#FF498205");
    private static final int COLOR_NORMAL = Color.parseColor("#ECECEC");
    private static final int COLOR_WHITE = Color.parseColor("#FFFFFF");

    private TextView tvTitle, tvNotePreview;
    private GridView grid;
    private ScrollView noteScroll;
    private SharedPreferences prefs;
    private DayAdapter adapter;

    private int curYear, curMonth;
    private int selYear, selMonth, selDay;
    private int cellCount;
    private int todayY, todayM, todayD;

    private final String[] days = new String[42];
    private final int[] dayColors = new int[42];
    private final boolean[] hasNote = new boolean[42];
    private final int[] cellYear = new int[42];
    private final int[] cellMonth = new int[42];
    private final int[] cellDay = new int[42];
    private final LunarCalendarUtils.LunarDate lunarDate = new LunarCalendarUtils.LunarDate();
    private android.graphics.drawable.Drawable bgSel, bgToday, bgTodaySel;
    private java.util.Set<String> noteKeys = java.util.Collections.emptySet();

    private final Calendar cal = Calendar.getInstance();
    private final Calendar curCal = Calendar.getInstance();
    private final Calendar workCal = Calendar.getInstance();
    private final StringBuilder sb = new StringBuilder(24);

    private float touchStartX, touchStartY;
    private final Handler doubleClickHandler = new Handler();
    private boolean titleClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("calendar_notes", Context.MODE_PRIVATE);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvNotePreview = (TextView) findViewById(R.id.tvNotePreview);
        noteScroll = (ScrollView) findViewById(R.id.noteScroll);
        grid = (GridView) findViewById(R.id.grid);
        bgSel = getResources().getDrawable(R.drawable.cell_selected);
        bgToday = getResources().getDrawable(R.drawable.cell_today);
        bgTodaySel = getResources().getDrawable(R.drawable.cell_today_selected);
        adapter = new DayAdapter();
        grid.setAdapter(adapter);

        curCal.setTimeInMillis(System.currentTimeMillis());
        curYear = curCal.get(Calendar.YEAR);
        curMonth = curCal.get(Calendar.MONTH);
        selYear = curYear; selMonth = curMonth; selDay = curCal.get(Calendar.DAY_OF_MONTH);

        grid.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override public void onItemClick(android.widget.AdapterView<?> parent, View v, int position, long id) {
                selYear = cellYear[position]; selMonth = cellMonth[position]; selDay = cellDay[position];
                adapter.notifyDataSetChanged();
                refreshNotePreview();
            }
        });
        findViewById(R.id.btnPrev).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { shiftMonth(-1); }
        });
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { shiftMonth(1); }
        });
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (titleClicked) {
                    titleClicked = false;
                    doubleClickHandler.removeCallbacksAndMessages(null);
                    gotoToday();
                } else {
                    titleClicked = true;
                    doubleClickHandler.postDelayed(pickerRunnable, 300);
                }
            }
        });
        render();
    }

    private final Runnable pickerRunnable = new Runnable() {
        @Override public void run() {
            if (titleClicked) {
                titleClicked = false;
                new DatePickerDialog(MainActivity.this, MainActivity.this, curYear, curMonth + 1).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    @Override
    protected void onDestroy() {
        doubleClickHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            touchStartX = ev.getX(); touchStartY = ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            float dx = ev.getX() - touchStartX, dy = ev.getY() - touchStartY;
            if (Math.abs(dx) > 100 && Math.abs(dx) > Math.abs(dy) * 2 && dx > 0 && touchStartY > 300) {
                openNoteEdit(); overridePendingTransition(R.anim.slide_in_from_left, R.anim.hold); return true;
            }
            if (Math.abs(dy) > 130 && Math.abs(dy) > Math.abs(dx) * 2 && dy < 0 && touchStartY > 300) {
                startActivity(new Intent(this, NoteListActivity.class));
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.hold); return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void jumpTo(int year, int month) {
        curYear = year; curMonth = month;
        selYear = year; selMonth = month; selDay = 1;
        render();
    }

    private void gotoToday() {
        curCal.setTimeInMillis(System.currentTimeMillis());
        curYear = curCal.get(Calendar.YEAR); curMonth = curCal.get(Calendar.MONTH);
        selYear = curYear; selMonth = curMonth; selDay = curCal.get(Calendar.DAY_OF_MONTH);
        render();
    }

    private void refreshNotePreview() {
        final String text = prefs.getString(keyOf(selYear, selMonth, selDay), "");
        tvNotePreview.setText(text.length() == 0 ? "" : text.replace("\n", "\n\n"));
        noteScroll.post(resizeRunnable);
    }

    private final Runnable resizeRunnable = new Runnable() {
        @Override public void run() {
            int measured = tvNotePreview.getMeasuredHeight();
            int cap = getResources().getDisplayMetrics().heightPixels / 4;
            noteScroll.getLayoutParams().height = Math.max(1, Math.min(measured, cap));
            noteScroll.requestLayout();
            noteScroll.scrollTo(0, 0);
        }
    };

    private void openNoteEdit() {
        Intent i = new Intent(this, NoteEditActivity.class);
        i.putExtra("year", selYear); i.putExtra("month", selMonth); i.putExtra("day", selDay);
        startActivity(i);
    }

    private String keyOf(int y, int m, int d) {
        sb.setLength(0);
        sb.append(y).append('-').append(m + 1).append('-').append(d);
        return sb.toString();
    }

    private void shiftMonth(int delta) {
        curMonth += delta;
        if (curMonth < 0) { curMonth = 11; curYear--; }
        if (curMonth > 11) { curMonth = 0; curYear++; }
        render();
    }

    private void render() {
        sb.setLength(0);
        tvTitle.setText(sb.append(curYear).append('年').append(curMonth + 1).append('月').toString());

        cal.set(curYear, curMonth, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cellCount = 42;

        curCal.setTimeInMillis(System.currentTimeMillis());
        todayY = curCal.get(Calendar.YEAR); todayM = curCal.get(Calendar.MONTH); todayD = curCal.get(Calendar.DAY_OF_MONTH);

        noteKeys = prefs.getAll().keySet();
        for (int i = 0; i < cellCount; i++) {
            workCal.set(curYear, curMonth, 1 - firstDow + i);
            int y = workCal.get(Calendar.YEAR), m = workCal.get(Calendar.MONTH), d = workCal.get(Calendar.DAY_OF_MONTH);
            cellYear[i] = y; cellMonth[i] = m; cellDay[i] = d;
            hasNote[i] = noteKeys.contains(keyOf(y, m, d));
            LunarCalendarUtils.solarToLunar(y, m + 1, d, lunarDate);
            String lunarText = lunarDate.day == 1
                ? LunarCalendarUtils.monthName(lunarDate.month, lunarDate.leap)
                : LunarCalendarUtils.dayName(lunarDate.day);
            String fest = LunarCalendarUtils.festival(y, m + 1, d, lunarDate);
            days[i] = fest != null ? fest : lunarText;
            if (m != curMonth) dayColors[i] = COLOR_OTHER_MONTH;
            else if (y == todayY && m == todayM && d == todayD) dayColors[i] = COLOR_TODAY;
            else dayColors[i] = COLOR_NORMAL;
        }
        adapter.notifyDataSetChanged();
        refreshNotePreview();
    }

    private class DayAdapter extends BaseAdapter {
        public int getCount() { return cellCount; }
        public Object getItem(int p) { return null; }
        public long getItemId(int p) { return p; }
        public View getView(int p, View cv, ViewGroup parent) {
            TextView tv = (TextView) cv;
            if (tv == null) {
                tv = new TextView(MainActivity.this);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setTextSize(15f);
                tv.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, 215));
            }
            boolean isSel = (cellYear[p] == selYear && cellMonth[p] == selMonth && cellDay[p] == selDay);
            boolean isToday = (cellYear[p] == todayY && cellMonth[p] == todayM && cellDay[p] == todayD);
            android.graphics.drawable.Drawable want = isSel && isToday ? bgTodaySel : isToday ? bgToday : isSel ? bgSel : null;
            if (tv.getBackground() != want) tv.setBackgroundDrawable(want);
            String text = String.valueOf(cellDay[p]) + "\n" + days[p] + (hasNote[p] ? "\n  •  " : "\n ");
            if (!text.equals(tv.getText().toString())) tv.setText(text);
            tv.setTextColor(isToday ? COLOR_WHITE : dayColors[p]);
            return tv;
        }
    }
}
