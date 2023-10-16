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
 * @author naveen
 *
 * @date 13-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
		name = "user_property_mapping",
		uniqueConstraints = { @UniqueConstraint(name = "UK_mapping_userid_propertyid", columnNames = { "user_id", "property_id" }) })
@Entity(name = "user_property_mapping")
public class UserPropertyMappingEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = -947751731338229446L;

	@Column(name = "user_id", columnDefinition = "char(40)", nullable = false)
	private String userId;

	@Column(name = "property_id", columnDefinition = "char(40)", nullable = false)
	private String propertyId;

}