<?php
/**
 *  SmartStreams API endpoint
 * 
 *  Copyright 2017 Chris Vincent
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  For more information, see https://github.com/project802/smartthings
 */
require_once( "db_entry.php" );
require_once( "common.php" );

$feed_args = $_REQUEST;

if( !check_api_key_for_writing() )
{
  goto exit_label;
}

$hub_id = $db->escape_string( $feed_args["hub_id"] );
$dev_id = $db->escape_string( $feed_args["dev_id"] );
$component = $db->escape_string( $feed_args["component"] );
$loc_id = $db->escape_string( $feed_args["loc_id"] );
$api_key = $db->escape_string( $feed_args["api_key"] );

unset( $feed_args["loc_id"] );
unset( $feed_args["hub_id"] );
unset( $feed_args["dev_id"] );
unset( $feed_args["component"] );
unset( $feed_args["api_key"] );

$streams = array_keys( $feed_args );
$values = array_values( $feed_args );

$stream = $db->escape_string( $streams[0] );
$value = $db->escape_string( $values[0] );

$sql = "INSERT INTO `st_events` VALUES (0, NOW(), '$hub_id', '$loc_id', '$dev_id', '$api_key', '$component', '$stream', '$value')";
$db->query( $sql );

$sql = "INSERT INTO `st_devices` (dev_id, dev_name) SELECT '$dev_id', '$component' FROM DUAL WHERE NOT EXISTS ( SELECT * FROM `st_devices` WHERE `dev_id` = '$dev_id' ) LIMIT 1";
$db->query( $sql );

exit_label:
require_once( "db_exit.php" );
?>
