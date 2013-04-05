
package com.hiiragi.med.dev;

import android.os.Environment;

public class ExternalDevice {

	// check availability of ext device
	public static boolean isExternalDeviceAvailable() {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

}
