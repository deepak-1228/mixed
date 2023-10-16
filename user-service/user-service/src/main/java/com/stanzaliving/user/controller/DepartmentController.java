package com.stanzaliving.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.adapters.DepartmentAdapter;

import lombok.extern.log4j.Log4j2;

/**
 *
 * @author piyush srivastava "piyush.srivastava@stanzaliving.com"
 *
 * @date 15-Apr-2020
 *
 */

@Log4j2
@RestController
@RequestMapping("department")
public class DepartmentController {

	@GetMapping("list")
	public ResponseDto<List<EnumListing<Department>>> getUserDepartment() {

		log.info("Received Department listing request.");
		return ResponseDto.success("Found Department List", DepartmentAdapter.getDepartmentEnumAsEnumListing());
	}
}
