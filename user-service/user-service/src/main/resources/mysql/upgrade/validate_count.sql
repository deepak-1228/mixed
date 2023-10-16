ALTER TABLE `user_service`.`otp` 
ADD COLUMN `validate_count` INT NOT NULL AFTER `resend_count`;