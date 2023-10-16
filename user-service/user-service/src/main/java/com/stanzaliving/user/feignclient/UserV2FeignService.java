package com.stanzaliving.user.feignclient;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.dto.userv2.UpdateUserDto;
import com.stanzaliving.user.dto.userv2.UserAttributesDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
public class UserV2FeignService {
    @Autowired
    private Userv2HttpService userv2HttpService;

    public UserDto getActiveUser(Long mobileNumber){
        try {
            ResponseDto<UserDto> userDtoResponseDto = userv2HttpService.getActiveUserForMobileNumber(mobileNumber);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }
//
//    public ResponseDto<UserDto> addUser(@RequestBody AddUserRequestDto addUserRequestDto);
//
    public UserDto getUserByUuid(String userUuid){
        try {
            ResponseDto<UserDto> userDtoResponseDto = userv2HttpService.getUserByUuid(userUuid);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }

    public void updateUser(@RequestBody UpdateUserDto updateUserDto){
        try {
            userv2HttpService.updateUser(updateUserDto);
        }
        catch (Exception e){}
    }
//
//    public ResponseDto<UserAttributesDto> getOrCreateUserAttributes(@RequestBody UserAttributesDto userAttributesDto);
//
//    ResponseDto<List<String>> getUserUuidsMappedWithManagerUuid(@PathVariable String managerUuid);
//
    public UserDto getManagerForUser(String userUuid){
        try {
            ResponseDto<UserDto> userDtoResponseDto = userv2HttpService.getManagerForUser(userUuid);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }
//
    public List<UserDto> getUsersList(List<String> userIds){
        try {
            ResponseDto<List<UserDto>> listResponseDto = userv2HttpService.getUsersList(userIds);
            if (Objects.nonNull(listResponseDto) && Objects.nonNull(listResponseDto.getData())) {
                return listResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }

    public Map<String,UserDto> getUserManagers(List<String> userIds){
        try {
            ResponseDto<Map<String, UserDto>> mapResponseDto = userv2HttpService.getUserManagers(userIds);
            if (Objects.nonNull(mapResponseDto) && Objects.nonNull(mapResponseDto.getData())) {
                mapResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new HashMap<>();
    }

    public List<UserDto> getUsersReportingToManager(@PathVariable String managerId){
        try {
            ResponseDto<List<UserDto>> listResponseDto = userv2HttpService.getUsersReportingToManager(managerId);
            if (Objects.nonNull(listResponseDto) && Objects.nonNull(listResponseDto.getData())) {
                return listResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }
//
//    void deleteManagerUuidFromUserAttributes(@PathVariable String userUuid);
//
    public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptRoleNameList(String userUuid){
        try {
            ResponseDto<List<UserDeptLevelRoleNameUrlExpandedDto>> listResponseDto = userv2HttpService.getUserDeptRoleNameList(userUuid);
            if (Objects.nonNull(listResponseDto) && Objects.nonNull(listResponseDto.getData())) {
                return listResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }
//
    public Map<String,List<String>> getActiveUserAndAccessLevelMapForRole(String roleName, Department department){
        try {
            ResponseDto<Map<String, List<String>>> mapResponseDto = userv2HttpService.getActiveUserAndAccessLevelMapForRole(roleName, department);
            if (Objects.nonNull(mapResponseDto) && Objects.nonNull(mapResponseDto.getData())) {
                return mapResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new HashMap<>();
    }

    public Map<String,List<String>> getUserAndAccessLevelMapForRole(String roleName, Department department){
        try {
            ResponseDto<Map<String, List<String>>> mapResponseDto = userv2HttpService.getUserAndAccessLevelMapForRole(roleName, department);
            if (Objects.nonNull(mapResponseDto) && Objects.nonNull(mapResponseDto.getData())) {
                return mapResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new HashMap<>();
    }

    public List<UserDto> findUsersForRoleNameAndDepartment(String roleName,Department department){
        try {
            ResponseDto<List<UserDto>> listResponseDto = userv2HttpService.findUsersForRoleNameAndDepartment(roleName, department);
            if (Objects.nonNull(listResponseDto) && Objects.nonNull(listResponseDto.getData())) {
                return listResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }

    public PageResponse<UserDto> searchOrFilterUsers(UserFilterDto userFilterDto){
        try {
            ResponseDto<PageResponse<UserDto>> pageResponseResponseDto = userv2HttpService.searchOrFilterUsers(userFilterDto);
            if (Objects.nonNull(pageResponseResponseDto) && Objects.nonNull(pageResponseResponseDto.getData())) {
                return pageResponseResponseDto.getData();
            }
        }
        catch (Exception e){}
        return PageResponse.of(userFilterDto.getPageRequest().getPageNo(),userFilterDto.getPageRequest().getLimit(),new ArrayList<>());
    }

    public List<UserDto> getUserFromEmail(String email){
        try {
            ResponseDto<List<UserDto>> listResponseDto = userv2HttpService.getUserFromEmail(email);
            if (Objects.nonNull(listResponseDto) && Objects.nonNull(listResponseDto.getData())) {
                return listResponseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }

    public List<RoleDto> findFilteredRoles(RoleDto roleDto){
        try {
            ResponseDto<List<RoleDto>> responseDto = userv2HttpService.findFilteredRoles(roleDto);
            if (Objects.nonNull(responseDto) && Objects.nonNull(responseDto.getData())) {
                return responseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }

    public List<RoleDto> getRolesFromUserUuid(String userUuid){
        try {
            ResponseDto<List<RoleDto>> responseDto = userv2HttpService.getRolesFromUserUuid(userUuid);
            if (Objects.nonNull(responseDto) && Objects.nonNull(responseDto.getData())) {
                return responseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();
    }

    public UserDto updateWithUser(UpdateUserRequestDto updateUserRequestDto){
        try {
            ResponseDto<UserDto> userDtoResponseDto = userv2HttpService.updateWithUser(updateUserRequestDto);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }

    public String updateUserStatus(String userId,boolean status){
        try {
            ResponseDto<String> responseDto = userv2HttpService.updateUserStatus(userId, status);
            if (Objects.nonNull(responseDto) && Objects.nonNull(responseDto.getData())) {
                return responseDto.getData();
            }
        }
        catch (Exception e){}
        return "Not Updated";
    }

    public UserDto getActiveUserByUuid(String userId) {
        try {
            ResponseDto<UserDto> userDtoResponseDto = userv2HttpService.getActiveUserByUuid(userId);
            if (Objects.nonNull(userDtoResponseDto) && Objects.nonNull(userDtoResponseDto.getData())) {
                return userDtoResponseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }

    public List<RoleDto> findByRoleNameInAndDepartment(List<String> roleNames,Department department){
        try {
            ResponseDto<List<RoleDto>> responseDto = userv2HttpService.findByRoleNameInAndDepartment(roleNames, department);
            if (Objects.nonNull(responseDto) && Objects.nonNull(responseDto.getData())) {
                return responseDto.getData();
            }
        }
        catch (Exception e){}
        return new ArrayList<>();

    }

    public RoleDto getRoleByUuid(String roleUuid){
        try {
            ResponseDto<RoleDto> responseDto = userv2HttpService.getRoleByUuid(roleUuid);
            if (Objects.nonNull(responseDto) && Objects.nonNull(responseDto.getData())) {
                return responseDto.getData();
            }
        }
        catch (Exception e){}
        return null;
    }

}
