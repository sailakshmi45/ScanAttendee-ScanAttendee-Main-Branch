package com.globalnest.appsessions;


public interface OnRemoveSessionListener {
	public void removeSession(DeviceSessionResponse mResponse,DeviceSessionId currentSessionId);
}
