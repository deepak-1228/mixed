/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateApiRequestDto;
import com.stanzaliving.user.acl.service.ApiService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * 
 * @author naveen.kumar
 * 
 * @date 22-Oct-2019
 *
 */
@Log4j2
@RestController
@RequestMapping("acl/api")
public class ApiController {

	@Autowired
	private ApiService apiService;
	//TODO - add bulk upload csv
	@PostMapping("add")
	public ResponseDto<ApiDto> addApi(@RequestBody @Valid AddApiRequestDto addApiRequestDto) {

		log.info("Received Request to add API [Name: " + addApiRequestDto.getApiName() + ", URL: " + addApiRequestDto.getActionUrl() + "]");

		return ResponseDto.success("Added API with URL " + addApiRequestDto.getActionUrl(), apiService.addApi(addApiRequestDto));
	}

	@PostMapping("update")
	public ResponseDto<ApiDto> updateApi(@RequestBody @Valid UpdateApiRequestDto updateApiRequestDto) {

		log.info("Received Request to update API [ID: " + updateApiRequestDto.getApiUuid() + ", Name: " + updateApiRequestDto.getApiName() + ", URL: " + updateApiRequestDto.getActionUrl() + "]");

		return ResponseDto.success("Updated API with apiUuid " + updateApiRequestDto.getApiUuid(), apiService.updateApi(updateApiRequestDto));
	}

	@DeleteMapping("delete/{apiUuid}")
	public ResponseDto<Void> deleteApi(@PathVariable(name = "apiUuid") String apiUuid) {

		log.info("Received request to delete api: " + apiUuid);
		
		apiService.deleteApi(apiUuid);

		return ResponseDto.success("Deleted Api " + apiUuid);
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<ApiDto>> searchApi(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "apiName", required = false) String apiName,
			@RequestParam(name = "apiUrl", required = false) String apiUrl,
			@RequestParam(name = "service", required = false) String service,
			@RequestParam(name = "status", required = false) Boolean status) {

		log.info("Received Api Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", ApiName: " + apiName + ", ApiUrl: " + apiUrl + ", Category: " + service + ", Status: "
				+ status + "]");

		PageResponse<ApiDto> apiDtos = apiService.searchApi(apiName, apiUrl, service, status, pageNo, limit);

		return ResponseDto.success("Found " + apiDtos.getRecords() + " Apis for Search Criteria", apiDtos);

	}

}