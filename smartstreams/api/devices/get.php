<?php
/**
 *  SmartStreams API to get devices
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

require_once( "../db_entry.php" );
require_once( "../common.php" );

if( !check_api_key_for_reading() )
{
  goto exit_label;
}

$devices = array();
$sql = "SELECT `dev_id` as 'id', `dev_name` as 'name' FROM `st_devices` WHERE 1 ORDER BY `dev_name` ASC";

$result = $db->query( $sql );
while( $result && ($row = $result->fetch_assoc()) )
{
  array_push( $devices, $row );
}
$result->free();

print( json_encode($devices) );

exit_label:
require_once( "../db_exit.php" );
?>
