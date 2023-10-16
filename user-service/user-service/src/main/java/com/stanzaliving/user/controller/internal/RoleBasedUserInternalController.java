package com.stanzaliving.user.controller.internal;

import java.util.Arrays;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.service.UserService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("internal/add")
public class RoleBasedUserInternalController {

	@Autowired
	private UserService userService;

	@GetMapping("roleBaseUser/{userType}")
	@ApiOperation("Create Role Base User.")
	public ResponseDto<Boolean> createRoleBaseUser(
			@PathVariable(name = "userType") @NotBlank(message = "User user Type must not be blank") UserType userType) {

		log.info("Request received to createRoleBaseUser : " + userType);

		boolean response =userService.createRoleBaseUser(userType);

		return ResponseDto.success("Role Base User Created Successfully.", response);

	}
	
	@GetMapping("roleBaseUser")
	@ApiOperation("Create Role Base User.")
	public ResponseDto<Boolean> createRoleBaseUserForMobile(
			@RequestParam(name = "mobile") String mobiles) {

		boolean response =userService.createRoleBaseUser(Arrays.asList(mobiles.split(",")));

		return ResponseDto.success("Role Base User Created Successfully.", response);

	}

}
