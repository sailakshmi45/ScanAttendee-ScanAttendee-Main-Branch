package com.globalnest.cropimage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class CropUtil {

	public static boolean ENABLE_PATTERN = false;

	public static void copyStream(InputStream input, OutputStream output) throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static void logMsg(LogLevel level, String tag, String msg) {
		if (level.toString().equals(LogLevel.DEBUG.toString())) {
			// Log.d(tag, msg);
		} else if (level.toString().equals(LogLevel.INFO.toString())) {
			// //Log.i(tag, msg);
		} else if (level.toString().equals(LogLevel.WARNING.toString())) {
			// Log.w(tag, msg);
		} else if (level.toString().equals(LogLevel.ERROR.toString())) {
			// Log.e(tag, msg);
		} else if (level.toString().equals(LogLevel.VERBOSE.toString())) {
			// Log.v(tag, msg);
		}
	}
}
