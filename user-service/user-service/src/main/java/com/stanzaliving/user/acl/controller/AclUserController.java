package com.stanzaliving.user.acl.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.AddUserAndRoleDto;
import com.stanzaliving.core.user.acl.dto.CityMicromarketDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownRequestDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.UpdateAccessModuleAccessLevelRequestDto;
import com.stanzaliving.core.user.acl.dto.UserAccessModuleDto;
import com.stanzaliving.core.user.acl.dto.UserDepartmentLevelAccessModulesDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesRequestDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesResponseDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.transformations.pojo.CityMetadataDto;
import com.stanzaliving.user.acl.service.AclUserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("acl/user")
public class AclUserController {

    @Autowired
    AclUserService aclUserService;

    @PostMapping("add/role")
    public ResponseDto<Void> addRole(@RequestBody @Valid AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleDto);
        aclUserService.addRole(addUserDeptLevelRoleDto);
        return ResponseDto.success("Role Assignment successful");
    }

    //this api will give only top level roles
    @GetMapping("{userUuid}")
    public ResponseDto<List<UserDeptLevelRoleDto>> getUserRoles(@PathVariable @NotBlank(message = "User uuid must not be blank") String userUuid) {

        log.info("Fetching user role with id: " + userUuid);
        return ResponseDto.success("User roles fetched for user " + userUuid, aclUserService.getActiveUserDeptLevelRole(userUuid));

    }

    @PostMapping("revoke/department/roles/all")
    public ResponseDto<Void> revokeAllRolesForDepartment(@RequestParam String userUuid,
                                                         @RequestParam Department department) {
        log.info("Received request to revoke all roles for user {} of department {}", userUuid, department);
        aclUserService.revokeAllRolesOfDepartment(userUuid, department);
        return ResponseDto.success("Role Revocation successful");
    }

    @PostMapping("revoke/department/level/roles/all")
    public ResponseDto<Void> revokeAllRolesForDepartmentOfLevel(@RequestParam @NotEmpty String userUuid,
                                                                @RequestParam @NotNull Department department,
                                                                @RequestParam @NotNull AccessLevel accessLevel) {
        log.info("Received request to revoke all roles for user {} of department {} of level {}", userUuid, department, accessLevel);
        aclUserService.revokeAllRolesOfDepartmentOfLevel(userUuid, department, accessLevel);
        return ResponseDto.success("Role Revocation successful");
    }

    @PostMapping("revoke/department/level/levelEntityList")
    public ResponseDto<Void> revokeAccessLevelEntityForDepartmentOfLevel(@RequestBody @Valid AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        log.info("Received request to revoke Access Level Entity List for user " + addUserDeptLevelRequestDto);
        aclUserService.revokeAccessLevelEntityForDepartmentOfLevel(addUserDeptLevelRequestDto);
        return ResponseDto.success("Access Level Entity Revocation successful");
    }

    @PostMapping("revoke/department/level/roleList")
    public ResponseDto<Void> revokeRolesForDepartmentOfLevel(@RequestBody @Valid UserDeptLevelRoleListDto userDeptLevelRoleListDto) {
        log.info("Received request to revoke role list for user " + userDeptLevelRoleListDto);
        aclUserService.revokeRolesForDepartmentOfLevel(userDeptLevelRoleListDto);
        return ResponseDto.success("Roles Revocation successful");
    }

    @PostMapping("/bulk/add/role")
    public ResponseDto<Void> bulkAddRole(@RequestBody @Valid AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleByEmailRequestDto);
        aclUserService.bulkAddRole(addUserDeptLevelRoleByEmailRequestDto);
        return ResponseDto.success("Bulk Role Assignment successful");
    }

    @GetMapping("/accessModule")
    public ResponseDto<List<UserAccessModuleDto>> getUserAccessModulesByUserUuid(@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userUuid) {

        log.info("Get Access Modules for user : {}", userUuid);
        List<UserAccessModuleDto> userAccessModuleDtoList = aclUserService.getUserAccessModulesByUserUuid(userUuid);
        if (CollectionUtils.isNotEmpty(userAccessModuleDtoList)) {
            return ResponseDto.success("List of Modules that the user has access to", userAccessModuleDtoList);
        } else {
            return ResponseDto.failure("Access to modules denied");
        }
    }

    @GetMapping("/cities/{department}")
    public ResponseDto<List<CityMetadataDto>> getCitiesByUserAcessAndDepartment(@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userUuid,
                                                                                @PathVariable @NotBlank(message = "Department must not be blank") Department department) {
        log.info("Get Cities for User : {}, and Department : {}", userUuid, department);
        List<CityMetadataDto> cityMetadataDtoList = aclUserService.getCitiesByUserAcessAndDepartment(userUuid, department);
        if (CollectionUtils.isNotEmpty(cityMetadataDtoList)) {
            return ResponseDto.success("List of cities", cityMetadataDtoList);
        } else {
            return ResponseDto.failure("No access to cities found");
        }
    }

    @PostMapping("accessModules/cities")
    public ResponseDto<List<UsersByAccessModulesAndCitiesResponseDto>> getUsersByAccessModulesAndCitites(
        @RequestBody UsersByAccessModulesAndCitiesRequestDto requestDto,
        @RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userUuid) {
        log.info("Get Users by access modules and cities : {}", requestDto);
        List<UsersByAccessModulesAndCitiesResponseDto> responseDtos = aclUserService.getUsersByAccessModulesAndCitites(requestDto, userUuid);
        if (CollectionUtils.isNotEmpty(responseDtos)) {
            return ResponseDto.success("Users by Access Modules, Cities and Access Level", responseDtos);
        } else {
            return ResponseDto.failure("Can't find Users by Access Modules, Cities and Access Level");
        }
    }

    @GetMapping("/accessModule/accessLevel/{userUuid}/{department}")
    public ResponseDto<List<UserDepartmentLevelAccessModulesDto>> getUserDepartmentLevelAccessModules(@PathVariable @NotBlank(message = "User uuid must not be blank")String userUuid,
                                                                                         @PathVariable @NotBlank(message = "Department must not be blank") Department department) {
        log.info("Get User Department Level Access Modules for user : {} and department : {}", userUuid, department);
        List<UserDepartmentLevelAccessModulesDto> userDepartmentLevelAccessModulesDtos = aclUserService
            .getUserDepartmentLevelAccessModules(userUuid, department);
        if (CollectionUtils.isNotEmpty(userDepartmentLevelAccessModulesDtos)) {
            return ResponseDto.success("Access modules and access levels of the user", userDepartmentLevelAccessModulesDtos);
        } else {
            return ResponseDto.failure("Can't find access modules for the user");
        }
    }

    @PostMapping("/update/accessModule/accessLevel")
    public ResponseDto<Void> updateUserAccessModuleAccessLevel(@RequestBody UpdateAccessModuleAccessLevelRequestDto requestDto) {
        log.info("Update user access module access level {}", requestDto);
        aclUserService.updateUserAccessModuleAccessLevel(requestDto);
        return ResponseDto.success("Access Module Access Level Updated Successfuly");
    }

    @PostMapping("/addUserAndRole")
    public ResponseDto<AddUserAndRoleDto> addUserAndRole (@RequestBody AddUserAndRoleDto addUserAndRoleDto) {

        log.info("Add User and assign Role : {}", addUserAndRoleDto);
        AddUserAndRoleDto userAndRoleDto = aclUserService.addUserAndRole(addUserAndRoleDto);
        return ResponseDto.success("Added User and assigned roles", userAndRoleDto);
    }

    @PostMapping("/micromarket/residence/dropdown")
    public ResponseDto<List<MicromarketAndResidencesDropdownResponseDto>> getMicromarketAndResidenceDropdown
        (@RequestBody MicromarketAndResidencesDropdownRequestDto requestDto) {
        log.info("Get micromarket and residence dropdown {}", requestDto);
        List<MicromarketAndResidencesDropdownResponseDto> responseDtos = aclUserService.getMicromarketAndResidenceDropdown(requestDto);
        if (CollectionUtils.isNotEmpty(responseDtos)) {
            return ResponseDto.success("Micromarket Residence Dropdown", responseDtos);
        } else {
            return ResponseDto.failure("Couldn't fetch Micromarket Residence Dropdown");
        }
    }

    @PostMapping("/city/micromarket/dropdown")
    public ResponseDto<List<CityMicromarketDropdownResponseDto>> getCityMicromarketDropdown(@RequestBody MicromarketAndResidencesDropdownRequestDto requestDto) {
        log.info("Get city and micromarket dropdown {}", requestDto);
        List<CityMicromarketDropdownResponseDto> responseDtos = aclUserService.getCityMicromarketDropdown(requestDto);
        if (CollectionUtils.isNotEmpty(responseDtos)) {
            return ResponseDto.success("City Micromarket Dropdown", responseDtos);
        } else {
            return ResponseDto.failure("Couldn't fetch City Micromarket Dropdown");
        }
    }

    @PostMapping("add/role/v2")
    public ResponseDto<Void> addRoleV2(@RequestBody @Valid AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {
        log.info("Received request to add role " + addUserDeptLevelRoleDto);
        aclUserService.addRoleV2(addUserDeptLevelRoleDto);
        return ResponseDto.success("Role Assignment successful");
    }
}
