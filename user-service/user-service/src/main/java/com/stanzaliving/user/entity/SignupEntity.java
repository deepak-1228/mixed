
package com.stanzaliving.user.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.adapters.JpaConverterJson;

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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "signup")
@Entity(name = "signup")
public class SignupEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = -947751731338229446L;

	@Column(name = "mobile", columnDefinition = "varchar(15)")
	private String mobile;

	@Column(name = "otp", columnDefinition = "int(8)", nullable = false)
	private Integer otp;

	@Convert(converter = JpaConverterJson.class)
	@Column(name = "signup_Object", columnDefinition = "json")
	private AddUserRequestDto signupObject;

	@Column(name = "is_validated", columnDefinition = "bit(1) default 0")
	private boolean validated;

}
