CREATE TABLE `signup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` char(40) NOT NULL,
  `otp` int NOT NULL,
  `mobile` varchar(15) NOT NULL,
  `signup_Object` json DEFAULT NULL,
  `is_validated` bit(1) DEFAULT b'0',
  `status` bit(1) DEFAULT b'1',
  `created_at` datetime DEFAULT NULL,
  `created_by` char(40) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` char(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
)