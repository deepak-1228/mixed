/**
 * 
 */
package com.stanzaliving.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author raj.kumar
 *
 */

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
		name = "user_manager_mapping",
		uniqueConstraints = { @UniqueConstraint(name = "UK_mapping_userid_managerid", columnNames = { "user_id", "manager_id" }) })
@Entity(name = "user_manager_mapping")
public class UserManagerMappingEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 786112770873632799L;

	@Column(name = "user_id", columnDefinition = "char(40)", nullable = false)
	private String userId;

	@Column(name = "manager_id", columnDefinition = "char(40)", nullable = false)
	private String managerId;

}