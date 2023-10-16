package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@Table(name = "user_department_level_roles")
@Entity(name = "user_department_level_roles")
@AllArgsConstructor
@NoArgsConstructor
public class UserDepartmentLevelRoleEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 7105880327634827863L;

	@Column(name = "user_department_level_uuid", columnDefinition = "char(40) NOT NULL")
	private String userDepartmentLevelUuid;

	@Column(name = "role_uuid", columnDefinition = "char(40) NOT NULL")
	private String roleUuid;

}