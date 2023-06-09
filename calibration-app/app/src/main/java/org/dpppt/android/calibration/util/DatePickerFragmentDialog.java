package org.dpppt.android.calibration.util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragmentDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

	public static final String RESULT_EXTRA_DATE_MILLIS = "result_extra_date_millis";

	private static final String ARG_MIN_DATE = "arg_min_date";
	private static final String ARG_SELECTED_DATE = "arg_selected_date";

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		final Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();

		c.setTimeInMillis(getArguments().getLong(ARG_SELECTED_DATE, 0));

		int selectedDay = c.get(Calendar.DAY_OF_MONTH);
		int selectedMonth = c.get(Calendar.MONTH);
		int selectedYear = c.get(Calendar.YEAR);
		DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, selectedYear, selectedMonth, selectedDay);

		dialog.getDatePicker().setMinDate(getArguments().getLong(ARG_MIN_DATE, c.getTimeInMillis()));
		dialog.getDatePicker().setMaxDate(now);
		return dialog;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		Calendar selectedDate = Calendar.getInstance();
		selectedDate.set(year, month, dayOfMonth);
		Intent result = new Intent();
		result.putExtra(RESULT_EXTRA_DATE_MILLIS, selectedDate.getTimeInMillis());
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
	}

}
