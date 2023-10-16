package com.stanzaliving.user.service;

import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;

public interface SignUpService {

	String signUpUser(AddUserRequestDto addUserRequestDto);

	UserProfileDto validateSignUpOtp(String uuid, String otp);

}
