-- Added Schema for User ACL
-- Added by Naveen on 21 October 2019

USE user_service;

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
