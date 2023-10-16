package com.stanzaliving.user.controller.internal;

import com.stanzaliving.user.entity.UserSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.dto.SessionRequestDto;
import com.stanzaliving.user.service.SessionService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/session/")
public class InternalSessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping("create")
    public ResponseDto<Void> create(@RequestBody SessionRequestDto sessionRequestDto) {

        log.info("Request received to create session : " + sessionRequestDto);
        sessionService.createSession(sessionRequestDto);
        return ResponseDto.success("Session Created Successfully");

    }

    @PostMapping("remove")
    public ResponseDto<Void> remove(@RequestBody SessionRequestDto sessionRequestDto) {

        log.info("Request received to remove session : " + sessionRequestDto);
        sessionService.removeUserSession(sessionRequestDto.getToken());
        return ResponseDto.success("Session Removed Successfully");

    }

    @PostMapping("update")
    public void updateUserSession(@RequestBody UserSessionEntity userSessionEntity){
        sessionService.updateUserSession(userSessionEntity);
    }

    @PostMapping("get")
    public UserSessionEntity getUserSessionEntity(@RequestParam String token){
        return sessionService.getUserSessionByToken(token);
    }

}
