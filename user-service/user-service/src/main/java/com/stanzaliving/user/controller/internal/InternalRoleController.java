package com.stanzaliving.user.controller.internal;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.acl.service.RoleService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Log4j2
@RestController
@RequestMapping("internal")
public class InternalRoleController {

    @Autowired
    RoleService roleService;

    @GetMapping("/acl/role/{roleUuid}")
    public ResponseDto<RoleDto> getRole(@PathVariable @NotBlank(message = "Role Id must not be blank") String roleUuid) {

        log.info("Fetching role with id: " + roleUuid);

        return ResponseDto.success("Found Role with Id: " + roleUuid, roleService.getRoleByUuid(roleUuid));
    }
    @GetMapping("/acl/roles/{roleName}")
    public ResponseDto<RoleDto> getRoleByName(@PathVariable @NotBlank(message = "Role Name must not be blank") String roleName) {

        log.info("Fetching role with name: " + roleName);

        return ResponseDto.success("Found Role with Name: " + roleName, roleService.findByRoleName(roleName));
    }

    @GetMapping("/acl/roles/{roleName}/{department}")
    public ResponseDto<RoleDto> getRoleByNameAndDepartment(@PathVariable @NotBlank(message = "Role Name must not be blank") String roleName
            ,@PathVariable @NotBlank(message = "Department Name must not be blank") Department department) {

        log.info("Fetching role with name and department name: " + roleName + department);

        return ResponseDto.success("Found Role with Name: " + roleName, roleService.findByRoleNameAndDepartment(roleName,department));
    }

}
