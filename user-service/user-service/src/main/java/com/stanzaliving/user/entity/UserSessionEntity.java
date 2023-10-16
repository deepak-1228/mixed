/**
 * 
 */
package com.stanzaliving.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.enums.UserType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_session")
@Entity(name = "user_session")
public class UserSessionEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = -947751731338229446L;

	@Column(name = "user_id", columnDefinition = "char(40)", nullable = false)
	private String userId;

	@Column(name = "token", columnDefinition = "varchar(100)", unique = true, nullable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type", columnDefinition = "varchar(50)", nullable = false)
	private UserType userType;

	@Column(name = "os", columnDefinition = "varchar(100)")
	private String os;

	@Column(name = "os_version", columnDefinition = "varchar(50)")
	private String osVerison;

	@Column(name = "browser", columnDefinition = "varchar(100)")
	private String browser;

	@Column(name = "browser_version", columnDefinition = "varchar(50)")
	private String browserVersion;

	@Column(name = "ip", columnDefinition = "varchar(50)")
	private String ip;

	@Column(name = "imei", columnDefinition = "varchar(50)")
	private String imei;

	@Column(name = "device", columnDefinition = "varchar(100)")
	private String device;

	@Column(name = "model", columnDefinition = "varchar(100)")
	private String model;

	@Column(name = "app_version", columnDefinition = "varchar(50)")
	private String appVersion;

}