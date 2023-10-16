package com.stanzaliving.user.service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.dto.userv2.*;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserManagerMappingEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.feignclient.MigrationHttpService;
import com.stanzaliving.user.repository.UserManagerMappingRepository;
import lombok.extern.log4j.Log4j2;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MigrationService {

    @Autowired
    private MigrationHttpService migrationHttpService;

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private UserManagerMappingRepository managerMappingRepository;

    @Autowired
    private RoleDbService roleDbService;

    @Autowired
    private UserDepartmentLevelDbService userDepartmentLevelDbService;

    @Autowired
    private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

    public void migrateUsers() {
        List<UserEntity> userEntityList=userDbService.findByUserTypeInAndStatus(UserType.getMigratedUserTypes(),true);

        List<UserDto> userDtos=new ArrayList<>();
        List<UserEntity> userEntities=new ArrayList<>();
        for(UserEntity userEntity: userEntityList){
            UserProfileEntity userProfile=userEntity.getUserProfile();
            UserManagerMappingEntity userManagerMappingEntity=managerMappingRepository.findFirstByUserId(userEntity.getUuid());
            try {
                UserDto userDto=UserDto.builder()
                        .userUuid(userEntity.getUuid())
                        .birthday(Objects.nonNull(userProfile)?userProfile.getBirthday():null)
                        .bloodGroup(Objects.nonNull(userProfile)?userProfile.getBloodGroup():null)
                        .gender(Objects.nonNull(userProfile)?userProfile.getGender():null)
                        .firstName(Objects.nonNull(userProfile)?userProfile.getFirstName():"first")
                        .lastName(Objects.nonNull(userProfile)?userProfile.getLastName():"last")
                        .middleName(Objects.nonNull(userProfile)?userProfile.getMiddleName():"middle")
                        .emailId(userEntity.getEmail())
                        .mobileNumber(userEntity.getMobile())
                        .isoCode(userEntity.getIsoCode())
                        .maritalStatus(Objects.nonNull(userProfile)?userProfile.getMaritalStatus():null)
                        .userUuid(userEntity.getUuid())
                        .status(userEntity.isStatus())
                        .nationality(Objects.nonNull(userProfile)?userProfile.getNationality():null)
                        .userProfileDto(UserProfileDto.builder()
                                .department(userEntity.getDepartment())
                                .userType(userEntity.getUserType())
                                .addressLine1("")
                                .addressLine2("")
                                .build())
                        .userAttributesDto(UserAttributesDto.
                                builder()
                                .managerUuid(Objects.nonNull(userManagerMappingEntity)?userManagerMappingEntity.getManagerId():null)
                                .userUuid(userEntity.getUuid())
                                .build())
                        .build();
                userDtos.add(userDto);
                userEntity.setUuid(userEntity.getUuid());
                if(Objects.nonNull(userProfile)) {
                    userProfile.setStatus(false);
                    userEntity.setUserProfile(userProfile);
                }
                userEntity.setStatus(false);
                userEntity.setMigrated(true);
                userEntities.add(userEntity);
            }
            catch (Exception e){
                log.info("failed for user entity {}",userEntity);
                throw new StanzaException("failed for useruuid "+userEntity.getUuid());
            }
        }
        migrationHttpService.migrateUsers(userDtos);
        userDbService.save(userEntities);
    }

    public void migrateRoles(){
        List<UserEntity> userEntityList=userDbService.findByUserTypeIn(UserType.getMigratedUserTypes());
        if(Objects.nonNull(userEntityList)) {

            for(UserEntity userEntity:userEntityList){
                List<UserDepartmentLevelEntity> userDepartmentLevelEntities=userDepartmentLevelDbService.findByUserUuidAndStatus(userEntity.getUuid(),true);
                if(Objects.nonNull(userDepartmentLevelEntities)) {
                    for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntities) {
                        RoleDto roleDto = RoleDto.builder()
                                .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                .department(userDepartmentLevelEntity.getDepartment())
                                .roleName(userEntity.getUserType().toString())
                                .build();
                        migrationHttpService.migrateRoles(roleDto);
                    }
                }
            }
        }
    }

    public void migrateUserRoleMapping(){
        List<UserEntity> userEntityList=userDbService.findByUserTypeIn(UserType.getMigratedUserTypes());
        if(Objects.nonNull(userEntityList)) {

            for(UserEntity userEntity: userEntityList){
                List<UserDepartmentLevelEntity> userDepartmentLevelEntities=userDepartmentLevelDbService.findByUserUuidAndStatus(userEntity.getUuid(),true);
                if (Objects.nonNull(userDepartmentLevelEntities)) {
                    for(UserDepartmentLevelEntity userDepartmentLevelEntity: userDepartmentLevelEntities){
                        List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities=userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuid(userDepartmentLevelEntity.getUuid());
                        if(Objects.nonNull(userDepartmentLevelRoleEntities)){
                            for(UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity: userDepartmentLevelRoleEntities){
                                RoleEntity roleEntity=roleDbService.findByUuid(userDepartmentLevelRoleEntity.getRoleUuid());
                                if(Objects.nonNull(roleEntity)) {

                                    String[] temp=userEntity.getUserType().toString().split("_");
                                    String role_name= userDepartmentLevelEntity.getDepartment().getDepartmentName();
                                    for(int i=0;i<temp.length;i++){
                                        role_name+=" "+temp[i];
                                    }
                                    RoleDto roleDto = RoleDto.builder()
                                            .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                            .department(userDepartmentLevelEntity.getDepartment())
                                            .roleName(role_name)
                                            .build();

                                    UserRoleMappingMigrationDto userRoleMappingMigrationDto=UserRoleMappingMigrationDto.builder()
                                            .accesslevelUuids(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid())
                                            .roleUuid(roleEntity.getUuid())
                                            .accessLevel(userDepartmentLevelEntity.getAccessLevel())
                                            .userUuid(userEntity.getUuid())
                                            .department(userDepartmentLevelEntity.getDepartment())
                                            .permission(roleEntity.getRoleName())
                                            .build();

                                    UserMigrationRoleAndAssignmentDto userMigrationRoleAndAssignmentDto=UserMigrationRoleAndAssignmentDto.builder()
                                            .roleDto(roleDto)
                                            .userRoleMappingMigrationDto(userRoleMappingMigrationDto)
                                            .build();
                                    try {
                                        migrationHttpService.migrateUserRoleMapping(userMigrationRoleAndAssignmentDto);
                                        roleEntity.setMigrated(true);
                                        roleDbService.save(roleEntity);
                                    }catch (Exception e){

                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
