/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.request.dto.UserAccessDto;
import com.stanzaliving.user.acl.service.AclService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@RestController
@RequestMapping("acl")
public class AclController {

	@Autowired
	private AclService aclService;

	@PostMapping("check")
	public ResponseDto<Boolean> isAccessible(@RequestBody @Valid UserAccessDto userAccessDto) {

		log.info("Checking User: " + userAccessDto.getUserId() + " access for url: " + userAccessDto.getUrl());

		boolean accessible = aclService.isAccessible(userAccessDto.getUserId(), userAccessDto.getUrl());

		log.info("URL: " + userAccessDto.getUrl() + " accessible by user: " + userAccessDto.getUserId() + " Status: " + accessible);

		return ResponseDto.success("URL Access Status: " + accessible, accessible);
	}

	@GetMapping("user/urlList/{userUuid}")
	public ResponseDto<Set<String>> getAccessibleUrlList(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid){
		log.info("Request received to get urlList for user : " + userUuid);
		return ResponseDto.success(aclService.getAccessibleUrlList(userUuid));
	}

	@GetMapping("user/fe/{userUuid}")
	public ResponseDto<List<UserDeptLevelRoleNameUrlExpandedDto>> getUserRolesFe(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

		log.info("Request received to getUserRolesFe for user : " + userUuid);
		return ResponseDto.success(aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userUuid));

	}

	@GetMapping("user/be/{userUuid}")
	public ResponseDto<List<UserDeptLevelRoleNameUrlExpandedDto>> getUserRolesBe(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

		log.info("Request received to getUserRolesBe for user : " + userUuid);
		return ResponseDto.success(aclService.getUserDeptLevelRoleNameUrlExpandedDtoBe(userUuid));

	}

}