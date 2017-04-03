<?php
/**
 *  SmartStreams API to get events
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

$events = array();

if( !isset($_REQUEST["dev_id"]) )
{
  print( json_encode($events) );
  goto exit_label;
}

$dev_id = $db->escape_string( $_REQUEST["dev_id"] );

$sql = "SELECT UNIX_TIMESTAMP(`creation_date`) as 'creation_date',`stream`,`value`,`dev_name`,st_events.dev_id FROM st_events JOIN st_devices ON (st_devices.dev_id = st_events.dev_id) WHERE st_events.dev_id = '$dev_id'";

if( isset($_REQUEST["stream"]) )
{
  $stream_query = $db->escape_string( $_REQUEST["stream"] );
  $sql = $sql . " AND `stream` = '$stream_query'";
}

if( isset($_REQUEST["start"]) && isset($_REQUEST["end"]) )
{
  $start = $db->escape_string( $_REQUEST["start"] );
  $end = $db->escape_string( $_REQUEST["end"] );

  // if we get time passed in ms, convert to seconds
  if( $start > 1000000000000 ) $start = $start / 1000;
  if( $end > 1000000000000 ) $end = $end / 1000;

  $sql = $sql . " HAVING (`creation_date` >= '$start' AND `creation_date` <= '$end')";
}

$sql = $sql . " ORDER BY `stream`,`creation_date` ASC";

$stream = "";
$dev_name = "";

$result = $db->query( $sql );
while( $result && ($row = $result->fetch_assoc()) )
{
  if( sizeof($events) == 0 )
  {
    $events["dev_id"] = $row["dev_id"];
    $events["dev_name"] = $row["dev_name"];
    $events["streams"] = array();
  }

  $stream = $row["stream"];

  if( !isset($events["streams"][$stream]) )
  {
    $events["streams"][$stream] = array();
  }

  $events["streams"][$stream][$row["creation_date"]] = $row["value"];
}
$result->free();

print( json_encode($events) );

exit_label:
require_once( "../db_exit.php" );
?>
