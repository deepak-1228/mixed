/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(name = "api", uniqueConstraints = {@UniqueConstraint(name = "UK_url_service", columnNames = {"action_url", "service"})})
@Entity(name = "api")
public class ApiEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 5062512483145481979L;

	@Column(name = "api_name", columnDefinition = "varchar(255) NOT NULL", unique = true)
	private String apiName;

	@Column(name = "action_url", columnDefinition = "varchar(255) NOT NULL")
	private String actionUrl;

	@Column(name = "service", columnDefinition = "varchar(100) NOT NULL")
	private String service;

}