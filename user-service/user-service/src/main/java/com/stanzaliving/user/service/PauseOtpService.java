package com.stanzaliving.user.service;

public interface PauseOtpService {

	boolean checkIfNeedToStop(String mobile);
	
	boolean pauseOtp(String mobile);
	
	boolean resumeOtp(String mobile);
}
