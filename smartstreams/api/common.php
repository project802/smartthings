<?php
/**
 *  SmartStreams API common functions
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

function check_api_key_for_writing()
{
  global $db;

  $retVal = true;

  if( !isset($_REQUEST["api_key"]) )
  {
    header( "HTTP/1.1 403 Forbidden" );
    return false;
  }

  $api_key = $db->escape_string( $_REQUEST["api_key"] );

  $sql = "SELECT * FROM `api_profiles` WHERE `api_key` = '$api_key' AND `can_write` = '1' LIMIT 1";
  $result = $db->query( $sql );

  if( !$result || ($result->num_rows != 1) )
  {
    header( "HTTP/1.1 403 Forbidden" );
    $retVal = false;
  }

  if( $result )
  {
    $result->free();
  }

  return $retVal;
}

function check_api_key_for_reading()
{
  global $db;

  $retVal = true;

  if( !isset($_REQUEST["api_key"]) )
  {
    header( "HTTP/1.1 403 Forbidden" );
    return false;
  }

  $api_key = $db->escape_string( $_REQUEST["api_key"] );

  $sql = "SELECT * FROM `api_profiles` WHERE `api_key` = '$api_key' AND `can_read` = '1' LIMIT 1";
  $result = $db->query( $sql );

  if( !$result || ($result->num_rows != 1) )
  {
    header( "HTTP/1.1 403 Forbidden" );
    $retVal = false;
  }

  if( $result )
  {
    $result->free();
  }

  return $retVal;
}

?>
