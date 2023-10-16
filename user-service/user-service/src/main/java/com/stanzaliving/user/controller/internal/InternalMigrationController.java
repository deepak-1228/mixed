package com.stanzaliving.user.controller.internal;

import com.stanzaliving.user.service.MigrationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/internal/migration")
public class InternalMigrationController {

    @Autowired
    private MigrationService migrationService;

    @GetMapping("/users")
    public void migrateUsers(){
        migrationService.migrateUsers();
    }

    @GetMapping("/roles")
    public void migrateRoles(){
        migrationService.migrateRoles();
    }

    @GetMapping("assign/roles")
    public void migrateUserRoleMapping(){
        migrationService.migrateUserRoleMapping();
    }

}
