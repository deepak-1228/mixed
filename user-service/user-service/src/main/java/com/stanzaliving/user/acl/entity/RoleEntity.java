/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

/**
 * @author naveen
 *
 * @date 19-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
@Entity(name = "roles")
public class RoleEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 7105880327634827863L;

	@Column(name = "role_name", columnDefinition = "varchar(255) NOT NULL", unique = true)
	private String roleName;

	@Enumerated(EnumType.STRING)
	@Column(name = "department", columnDefinition = "varchar(30)", nullable = false)
	private Department department;

	@Enumerated(EnumType.STRING)
	@Column(name = "access_level", columnDefinition = "varchar(30)", nullable = false)
	private AccessLevel accessLevel;

	@Column(name = "migrated",columnDefinition = "bit(1) default 1")
	private boolean migrated;

}