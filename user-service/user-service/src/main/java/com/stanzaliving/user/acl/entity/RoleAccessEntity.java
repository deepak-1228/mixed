/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
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
@Table(name = "role_access", uniqueConstraints = { @UniqueConstraint(name = "UK_roleUuid_type_accessUuid ", columnNames = { "role_uuid", "role_access_type", "access_uuid" }) })
@Entity(name = "role_access")
public class RoleAccessEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 5062512483145481979L;

	@Column(name = "role_uuid", columnDefinition = "char(40) NOT NULL")
	private String roleUuid;

	@Column(name = "access_uuid", columnDefinition = "char(40) NOT NULL")
	private String accessUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_access_type", columnDefinition = "char(10) NOT NULL")
	private RoleAccessType roleAccessType;
}