-- 
-- Table structure for table `api_profiles`
-- 

CREATE TABLE "api_profiles" (
  "id" mediumint(8) unsigned NOT NULL auto_increment,
  "creation_date" datetime NOT NULL,
  "last_updated" timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  "email" varchar(256) NOT NULL,
  "api_key" char(36) NOT NULL,
  "can_read" tinyint(1) NOT NULL,
  "can_write" tinyint(1) NOT NULL,
  PRIMARY KEY  ("id")
);

-- --------------------------------------------------------

-- 
-- Table structure for table `st_devices`
-- 

CREATE TABLE "st_devices" (
  "id" mediumint(8) unsigned NOT NULL auto_increment,
  "dev_id" char(36) NOT NULL,
  "dev_name" char(36) NOT NULL,
  PRIMARY KEY  ("id")
);

-- --------------------------------------------------------

-- 
-- Table structure for table `st_events`
-- 

CREATE TABLE "st_events" (
  "id" mediumint(8) unsigned NOT NULL auto_increment,
  "creation_date" datetime NOT NULL,
  "hub_id" char(36) NOT NULL,
  "loc_id" char(36) NOT NULL,
  "dev_id" char(36) NOT NULL,
  "api_key" char(36) NOT NULL,
  "component" varchar(256) NOT NULL,
  "stream" varchar(256) NOT NULL,
  "value" varchar(256) NOT NULL,
  PRIMARY KEY  ("id")
);
