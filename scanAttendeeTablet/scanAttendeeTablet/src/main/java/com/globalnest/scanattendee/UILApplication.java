package com.globalnest.scanattendee;

import android.os.StrictMode;

import com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication;



/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class UILApplication extends ScannerSettingsApplication {

	@Override
	public void onCreate() {
		super.onCreate();


		//DebugTools.setup(this);
		// This configuration tuning is custom. You can tune every option, you may tune some of them, 
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
	/*	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPoolSize(3)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.memoryCacheSize(15000000) // 1.5 Mb
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new Md5FileNameGenerator())
			.enableLogging() // Not necessary in common
			.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);*/
	}
}