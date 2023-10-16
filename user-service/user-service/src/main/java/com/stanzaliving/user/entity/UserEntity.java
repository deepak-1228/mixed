/**
 * 
 */
package com.stanzaliving.user.entity;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.enums.UserType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true, exclude = {"userProfile"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", uniqueConstraints = { @UniqueConstraint(columnNames = { "mobile", "user_type", "iso_code" }) })
@Entity(name = "user")
public class UserEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 2429210304319495082L;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type", columnDefinition = "varchar(50)", nullable = false)
	private UserType userType;

	@Column(name = "iso_code", columnDefinition = "varchar(4)", nullable = false)
	private String isoCode;

	@Column(name = "mobile", columnDefinition = "varchar(15)", nullable = false)
	private String mobile;

	@Column(name = "mobile_verified", columnDefinition = "bit(1) default 0")
	private boolean mobileVerified;

	@Column(name = "email", columnDefinition = "varchar(100)")
	private String email;

	@Column(name = "email_verified", columnDefinition = "bit(1) default 0")
	private boolean emailVerified;

	@Enumerated(EnumType.STRING)
	@Column(name = "department", columnDefinition = "varchar(50)", nullable = false)
	private Department department;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private UserProfileEntity userProfile;

	@Column(name = "migrated", columnDefinition = "bit(1) default 0")
	private boolean migrated;
}