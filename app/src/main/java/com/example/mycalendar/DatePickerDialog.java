package com.example.mycalendar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class DatePickerDialog extends Dialog {
    private MainActivity host;
    private NumberPicker npYear, npMonth;

    public DatePickerDialog(Context c, MainActivity host, int year, int month) {
        super(c, android.R.style.Theme_Material_Dialog_NoActionBar);
        this.host = host;
        this.mYear = year; this.mMonth = month;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_picker);
        setTitle("跳转到年月");

        npYear = (NumberPicker) findViewById(R.id.npYear);
        npMonth = (NumberPicker) findViewById(R.id.npMonth);
        npYear.setMinValue(1970); npYear.setMaxValue(2099);
        npMonth.setMinValue(1); npMonth.setMaxValue(12);
        npYear.setValue(getIntentYear()); npMonth.setValue(getIntentMonth());
        npYear.setWrapSelectorWheel(false); npMonth.setWrapSelectorWheel(true);

        ((Button) findViewById(R.id.btnOk)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                host.jumpTo(npYear.getValue(), npMonth.getValue() - 1);
                dismiss();
            }
        });
    }

    private int mYear, mMonth;
    private int getIntentYear() { return mYear; }
    private int getIntentMonth() { return mMonth; }
    private void setInitial(int y, int m) { mYear = y; mMonth = m; }
}
