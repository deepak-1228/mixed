/**
 * 
 */
package com.stanzaliving.user.entity;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.enums.OtpType;
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
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp")
@Entity(name = "otp")
public class OtpEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 2284651697599647979L;

	@Enumerated(EnumType.STRING)
	@Column(name = "otp_type", columnDefinition = "varchar(50)", nullable = false)
	private OtpType otpType;

	@Column(name = "otp", columnDefinition = "int(8)", nullable = false)
	private Integer otp;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type", columnDefinition = "varchar(50)", nullable = false)
	private UserType userType;

	@Column(name = "user_id", columnDefinition = "char(40)")
	private String userId;

	@Column(name = "iso_code", columnDefinition = "varchar(4)")
	private String isoCode;

	@Column(name = "mobile", columnDefinition = "varchar(15)")
	private String mobile;

	@Column(name = "email", columnDefinition = "varchar(100)")
	private String email;

	@Column(name = "resend_count", columnDefinition = "int(2)", nullable = false)
	private int resendCount;
	
	@Column(name = "validate_count", columnDefinition = "int(2)", nullable = false)
	private int validateCount;

}