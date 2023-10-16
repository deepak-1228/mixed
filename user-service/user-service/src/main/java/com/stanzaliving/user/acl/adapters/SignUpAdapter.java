package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.entity.SignupEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SignUpAdapter {

	public static SignupEntity getSignupEntity(AddUserRequestDto addUserRequestDto, int otp) {

		SignupEntity signup = SignupEntity.builder().otp(otp).mobile(addUserRequestDto.getMobile())
				.validated(Boolean.FALSE).signupObject(addUserRequestDto).build();

		return signup;
	}

}
