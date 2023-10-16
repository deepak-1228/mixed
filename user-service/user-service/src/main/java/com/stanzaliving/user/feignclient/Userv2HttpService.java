package com.stanzaliving.user.feignclient;


import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.dto.userv2.UpdateUserDto;
import com.stanzaliving.user.dto.userv2.UserAttributesDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "Userv2HttpService", url = "${service.userManagement.url}")
public interface Userv2HttpService {

    @GetMapping(value = "/internal/redirect/mobile/{mobileNumber}")
    ResponseDto<UserDto> getActiveUserForMobileNumber(@PathVariable Long mobileNumber);

    @PostMapping(value = "/internal/redirect/user")
    public ResponseDto<UserDto> addUser(@RequestBody AddUserRequestDto addUserRequestDto);

    @GetMapping(value = "/internal/redirect/user/{userUuid}")
    public ResponseDto<UserDto> getUserByUuid(@PathVariable String userUuid);

    @PutMapping(value = "/internal/redirect/user")
    public void updateUser(@RequestBody UpdateUserDto updateUserDto);

    @PostMapping(value = "/internal/redirect/user/attributes")
    public ResponseDto<UserAttributesDto> getOrCreateUserAttributes(@RequestBody UserAttributesDto userAttributesDto);

    @PostMapping(value = "/internal/redirect/manager/{managerUuid}/users")
    ResponseDto<List<String>> getUserUuidsMappedWithManagerUuid(@PathVariable String managerUuid);

    @GetMapping(value = "/internal/redirect/manager/{userUuid}")
    ResponseDto<UserDto> getManagerForUser(@PathVariable String userUuid);

    @PostMapping(value = "/internal/redirect/users/list")
    ResponseDto<List<UserDto>> getUsersList(@RequestBody List<String> userIds);

    @PostMapping(value = "/internal/redirect/list")
    ResponseDto<Map<String,UserDto>> getUserManagers(@RequestBody List<String> userIds);

    @GetMapping(value = "/internal/redirect/manager/{managerId}/users")
    ResponseDto<List<UserDto>> getUsersReportingToManager(@PathVariable String managerId);

    @GetMapping(value = "/internal/redirect/{userUuid}/manager/delete")
    void deleteManagerUuidFromUserAttributes(@PathVariable String userUuid);

    @GetMapping(value = "/internal/redirect/userrole/roledept/list/{userUuid}")
    ResponseDto<List<UserDeptLevelRoleNameUrlExpandedDto>> getUserDeptRoleNameList(@PathVariable String userUuid);


    @GetMapping(value = "/internal/redirect/userrole/userAccessLevelMap/active")
    ResponseDto<Map<String,List<String>>> getActiveUserAndAccessLevelMapForRole(@RequestParam String roleName,
                                                                          @RequestParam Department department);

    @GetMapping(value = "/internal/redirect/userrole/userAccessLevelMap")
    ResponseDto<Map<String,List<String>>> getUserAndAccessLevelMapForRole(@RequestParam String roleName,
                                                                          @RequestParam Department department);
    @GetMapping(value = "/internal/redirect/userrole/users")
    ResponseDto<List<UserDto>> findUsersForRoleNameAndDepartment(@RequestParam String roleName,@RequestParam Department department);

    @PostMapping(value = "/internal/redirect/user/search")
    ResponseDto<PageResponse<UserDto>> searchOrFilterUsers(@RequestBody UserFilterDto userFilterDto);

    @GetMapping(value ="/internal/redirect/user/email")
    ResponseDto<List<UserDto>> getUserFromEmail(@RequestParam String email);

    @PostMapping(value = "/internal/redirect/role/")
    ResponseDto<List<RoleDto>> findFilteredRoles(@RequestBody RoleDto roleDto);

    @GetMapping(value = "/internal/redirect/{userUuid}/roles")
    ResponseDto<List<RoleDto>> getRolesFromUserUuid(@PathVariable String userUuid);

    @PostMapping(value = "/internal/redirect/update")
    ResponseDto<UserDto> updateWithUser(@RequestBody UpdateUserRequestDto updateUserRequestDto);

    @PostMapping(value = "internal/redirect/update/status")
    ResponseDto<String> updateUserStatus(@RequestParam String userId,@RequestParam boolean status);

    @PostMapping(value = "/internal/redirect/role/name")
    ResponseDto<List<RoleDto>> findByRoleNameInAndDepartment(@RequestBody List<String> roleNames,@RequestParam Department department);

    @GetMapping(value = "internal/redirect/role/{roleUuid}")
    ResponseDto<RoleDto> getRoleByUuid(@PathVariable String roleUuid);

    @GetMapping(value = "/internal/redirect/user/{userUuid}/active")
    ResponseDto<UserDto> getActiveUserByUuid(@PathVariable String userUuid);
}
