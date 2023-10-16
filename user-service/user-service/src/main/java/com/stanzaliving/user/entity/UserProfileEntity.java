/**
 * 
 */
package com.stanzaliving.user.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.stanzaliving.core.sqljpa.conveter.LocalDateAttributeConverter;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.sqljpa.entity.AddressEntity;
import com.stanzaliving.core.user.enums.BloodGroup;
import com.stanzaliving.core.user.enums.Gender;
import com.stanzaliving.core.user.enums.MaritalStatus;
import com.stanzaliving.core.user.enums.Nationality;

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
@ToString(exclude = {"user", "address"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile")
@Entity(name = "user_profile")
public class UserProfileEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 2429210304319495082L;

	@Column(name = "first_name", columnDefinition = "varchar(100)", nullable = false)
	private String firstName;

	@Column(name = "middle_name", columnDefinition = "varchar(100)")
	private String middleName;

	@Column(name = "last_name", columnDefinition = "varchar(100)")
	private String lastName;

	@Column(name = "secondary_email", columnDefinition = "varchar(100)")
	private String secondaryEmail;

	@Column(name = "secondary_email_verified")
	private boolean secondaryEmailVerified;

	@Column(name = "secondary_iso_code", columnDefinition = "varchar(4)")
	private String secondaryIsoCode;

	@Column(name = "secondary_mobile", columnDefinition = "varchar(15)")
	private String secondaryMobile;

	@Column(name = "secondary_mobile_verified")
	private boolean secondaryMobileVerified;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", columnDefinition = "varchar(20)")
	private Gender gender;

	@Column(name = "profile_picture", columnDefinition = "varchar(255)")
	private String profilePicture;

	@Column(name = "birthday", columnDefinition = "DATE")
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate birthday;

	@Enumerated(EnumType.STRING)
	@Column(name = "marital_status", columnDefinition = "varchar(20)")
	private MaritalStatus maritalStatus;

	@Column(name = "anniversary_date", columnDefinition = "DATE")
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate anniversaryDate;

	@Enumerated(EnumType.STRING)
	@Column(name= "nationality",columnDefinition = "varchar(100)")
	private Nationality nationality;

	@Enumerated(EnumType.STRING)
	@Column(name = "blood_group",columnDefinition = "varchar(20)")
	private BloodGroup bloodGroup;

	@Column(name = "arrival_date", columnDefinition = "DATE")
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate arrivalDate;

	@Column(name= "next_destination",columnDefinition = "varchar(100)")
	private String nextDestination;

	@Embedded
	private AddressEntity address;

	@OneToOne
	private UserEntity user;
}