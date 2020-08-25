-- create table if not exists USER (
-- USE_ID int not null primary key auto_increment,
-- USE_NAME varchar(100),
-- USE_SEX varchar(1),
-- USE_AGE NUMBER(3),
-- USE_ID_NO VARCHAR(18),
-- USE_PHONE_NUM VARCHAR(11),
-- USE_EMAIL VARCHAR(100),
-- CREATE_TIME DATE,
-- MODIFY_TIME DATE,
-- USE_STATE VARCHAR(1));



create table if not exists user (
id bigint not null primary key auto_increment,
name varchar(100),
password varchar(200),
role varchar(200));



create table if not exists node (
id bigint not null primary key auto_increment,
nodeIP varchar(100),
nodePort varchar(100),
nodeType varchar(100),
nodeState varchar(100),
nodeVersion varchar(100),
userName varchar(100),
password varchar(100),
dbIP varchar(100),
dbPort varchar(100),
databaseName varchar(100),
dbUsername varchar(100),
dbPassword varchar(100),
leveldbPath varchar(100));

create table if not exists test (
id bigint not null primary key auto_increment);

-- create table if not exists node (
-- id bigint not null primary key auto_increment,
-- nodeIP varchar(100),
-- nodePort varchar(100),
-- nodeType int,
-- nodeState int,
-- nodeVersion varchar(100),
-- userName varchar(100),
-- password varchar(100),
-- dbIP varchar(100),
-- dbPort varchar(100),
-- databaseName varchar(100),
-- dbUsername varchar(100),
-- dbPassword varchar(100),
-- leveldbPath varchar(100);