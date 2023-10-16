package com.stanzaliving.user.service;
import com.stanzaliving.booking.dto.BookingResponseDto;
import com.stanzaliving.core.base.common.dto.ResponseDto;

import org.springframework.stereotype.Service;

public interface OnboardGuestService {

	public ResponseDto<BookingResponseDto> createGuestBooking(String phoneNumber);


}