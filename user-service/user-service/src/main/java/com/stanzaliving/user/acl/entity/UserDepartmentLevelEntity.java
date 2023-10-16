package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
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
@Table(name = "user_department_level", uniqueConstraints = { @UniqueConstraint(name = "UK_user_dept_level_status", columnNames = { "user_uuid", "department", "access_level", "status" }) })
@Entity(name = "user_department_level")
@AllArgsConstructor
@NoArgsConstructor
public class UserDepartmentLevelEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 7105880327634827863L;

	@Column(name = "user_uuid", columnDefinition = "char(40) NOT NULL")
	private String userUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "department", columnDefinition = "varchar(30) NOT NULL", nullable = false)
	private Department department;

	@Enumerated(EnumType.STRING)
	@Column(name = "access_level", columnDefinition = "varchar(30) NOT NULL", nullable = false)
	private AccessLevel accessLevel;

	@Column(name = "access_level_entity_uuids", columnDefinition = "TEXT", nullable = false)
	private String csvAccessLevelEntityUuid;

}
