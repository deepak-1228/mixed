package com.stanzaliving.user.acl.entity;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.AccessModule;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@Table(name = "role_access_module_mapping", uniqueConstraints = { @UniqueConstraint(name = "UK_role_module_status", columnNames = { "role_uuid", "access_module", "status" }) })
@Entity(name = "role_access_module_mapping")
@AllArgsConstructor
@NoArgsConstructor
public class RoleAccessModuleMappingEntity extends AbstractJpaEntity {

    private static final long serialVersionUID = 7105880327634827863L;

    @Column(name = "role_uuid", columnDefinition = "char(40) NOT NULL", nullable = false)
    private String roleUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_module", columnDefinition = "varchar(40) NOT NULL", nullable = false)
    private AccessModule accessModule;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", columnDefinition = "varchar(30) NOT NULL", nullable = false)
    private AccessLevel accessLevel;
}
