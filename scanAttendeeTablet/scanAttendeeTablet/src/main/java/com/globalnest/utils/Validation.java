package com.globalnest.utils;

import android.widget.EditText;
import android.widget.TextView;

import java.sql.Date;
import java.util.regex.Pattern;

public class Validation {

	public static String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String PHONE_REGEX = "\\d{3}-\\d{7}";

	// call this method when you need to check email validation
	public static boolean isEmailAddress(EditText editText, boolean required,
			String errMsg) {
		//Log.i("-----Eit Text Id----", ":" + editText.getId());
		return isValid(editText, EMAIL_REGEX, errMsg, required);
	}

	public static boolean isPassword(EditText editText, boolean required,
			String errMsg) {
		//Log.i("-----Eit Text Id----", ":" + editText.getId());
		return isPasswordValid(editText, EMAIL_REGEX, errMsg, required);
	}

	// call this method when you need to check phone number validation
	public static boolean isPhoneNumber(EditText editText, boolean required,
			String errMsg) {

		return isValid(editText, PHONE_REGEX, errMsg, required);
	}

	public static boolean isPasswordValid(EditText editText, String regex,
			String errMsg, boolean required) {

		String text = editText.getText().toString().trim();
		// clearing the error, if it was previously set by some other values
		editText.setError(null);

		// text required and editText is blank, so return false
		if (required && !hasTextAll(editText, errMsg))
			return false;

		// pattern doesn't match so returning false
		if (required && text.length() < 6) {
			editText.setError(errMsg);
			editText.requestFocus();
			return false;
		}
		;

		return true;
	}

	// return true if the input field is valid, based on the parameter passed
	public static boolean isValid(EditText editText, String regex,
			String errMsg, boolean required) {

		String text = editText.getText().toString().trim();
		// clearing the error, if it was previously set by some other values
		editText.setError(null);

		// text required and editText is blank, so return false
		if (required && !hasTextAll(editText, errMsg))
			return false;

		// pattern doesn't match so returning false
		if (required && !Pattern.matches(regex, text)) {
			editText.setError(errMsg);
			editText.requestFocus();
			return false;
		}
		;

		return true;
	}

	public static boolean hasValidPhone(EditText editText, String msg) {

		String text = editText.getText().toString().trim();
		editText.setError(null);

		// length 0 means there is no text
		if (text.length() == 0 || text.length() < 10) {
			editText.setError(msg);
			editText.requestFocus();
			return false;
		}

		return true;
	}

	public static boolean hasTextAll(EditText editText, String msg) {

		String text = editText.getText().toString().trim();
		editText.setError(null);

		// length 0 means there is no text
		if (text.length() == 0) {
			editText.setError(msg);
			editText.requestFocus();
			return false;
		}

		return true;
	}

	public static boolean hasValidDate(TextView editText, String msg,
			Date startDate, Date endDate) {

		// String text = editText.getText().toString().trim();
		editText.setError(null);

		if (endDate.compareTo(startDate) < 0
				|| endDate.compareTo(startDate) == 0) {
			editText.requestFocus();
			editText.setError(msg);

			return false;
		}

		return true;
	}

	public static String NullChecker(String var) {

		if (var == null) {
			return "";
		} else if (var.equals("null")) {
			return "";
		} else {
			return var;
		}

	}
}
