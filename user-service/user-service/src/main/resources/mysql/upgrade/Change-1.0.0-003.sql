-- Added Profile fields
-- Added by Ankit Jindal

USE user_service;

alter table user_profile 
add column nationality varchar(100) default null after country, 
add column blood_group varchar(20) default null after nationality, 
add column arrival_date date default null after blood_group, 
add column next_destination varchar(20) default null after arrival_date;