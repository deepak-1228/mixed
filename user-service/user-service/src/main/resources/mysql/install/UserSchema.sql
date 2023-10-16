
CREATE DATABASE IF NOT EXISTS `user_service` ;

USE `user_service`;

CREATE TABLE IF NOT EXISTS `otp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `otp_type` varchar(50) NOT NULL,
  `otp` int(8) NOT NULL,
  `user_type` varchar(50) NOT NULL,
  `user_id` char(40) DEFAULT NULL,
  `iso_code` varchar(4) DEFAULT NULL,
  `mobile` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `resend_count` int(2) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_otp_uuid` (`uuid`),
  KEY `Key_otp_mobile` (`mobile`),
  KEY `Key_otp_userid` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `user_type` varchar(50) NOT NULL,
  `iso_code` varchar(4) NOT NULL,
  `mobile` varchar(15) NOT NULL,
  `mobile_verified` bit(1) DEFAULT b'0',
  `email` varchar(100) DEFAULT NULL,
  `email_verified` bit(1) DEFAULT b'0',
  `department` varchar(50) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_mobile_type_iso` (`mobile`,`user_type`,`iso_code`),
  UNIQUE KEY `UK_user_uuid` (`uuid`),
  KEY `Key_user_type` (`user_type`),
  KEY `Key_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `secondary_iso_code` varchar(4) DEFAULT NULL,
  `secondary_mobile` varchar(15) DEFAULT NULL,
  `secondary_mobile_verified` bit(1) DEFAULT b'0',
  `secondary_email` varchar(100) DEFAULT NULL,
  `secondary_email_verified` bit(1) DEFAULT b'0',
  `gender` varchar(20) DEFAULT NULL,
  `birthday` date,
  `marital_status` varchar(20) DEFAULT NULL,
  `anniversary_date` date,
  `profile_picture` varchar(255) DEFAULT NULL,
  `address_line_1` TEXT DEFAULT NULL,
  `address_line_2` TEXT DEFAULT NULL,
  `landmark` TEXT DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_profile_uuid` (`uuid`),
  KEY `FK_user_profile_userid` (`user_id`),
  CONSTRAINT `FK_user_profile_userid` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `user_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `user_id` char(40) NOT NULL,
  `token` varchar(100) NOT NULL,
  `user_type` varchar(50) NOT NULL,
  `os` varchar(100) DEFAULT NULL,
  `os_version` varchar(50) DEFAULT NULL,
  `browser` varchar(100) DEFAULT NULL,
  `browser_version` varchar(50) DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `imei` varchar(50) DEFAULT NULL,
  `device` varchar(100) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `app_version` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_session_token` (`token`),
  UNIQUE KEY `UK_session_uuid` (`uuid`),
  KEY `Key_session_userid` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_property_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `user_id` char(40) NOT NULL,
  `property_id` char(40) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mapping_uuid` (`uuid`),
  UNIQUE KEY `UK_mapping_userid_propertyid` (`user_id`, `property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `api` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `api_name` varchar(255) NOT NULL,
  `action_url` varchar(255) NOT NULL,
  `category` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mapping_uuid` (`uuid`),
  UNIQUE KEY `UK_api_action_url` (`action_url`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `role_name` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_roles_uuid` (`uuid`),
  UNIQUE KEY `UK_roles_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `role_api` (
  `api_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`api_id`,`role_id`),
  KEY `FK643fmg45xe56qfex2b0slqdn2` (`role_id`),
  CONSTRAINT `FK643fmg45xe56qfex2b0slqdn2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKrg1a556vmdwjbd6ooopdf08pe` FOREIGN KEY (`api_id`) REFERENCES `api` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `role_id` char(40) NOT NULL,
  `user_id` char(40) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_roles_uuid` (`uuid`),
  UNIQUE KEY `UK_user_roles_userid_roleid` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_manager_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `user_id` char(40) NOT NULL,
  `manager_id` char(40) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mapping_uuid` (`uuid`),
  UNIQUE KEY `UK_mapping_userid_managerid` (`user_id`, `manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `department_name` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `status` bit(1) DEFAULT b'1',
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_departments_uuid` (`uuid`),
  UNIQUE KEY `UK_departments_name` (`department_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
