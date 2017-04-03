<?php
/**
 *  SmartStreams API DB entry point
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

if( !isset($db) )
{
  $db = new mysqli( "hostname", "username", "password", "database_name" );

  if( $db->connect_errno > 0 )
  {
    die( "Unable to connect to database (" . $db->connect_errno . ")" );
  }
}

?>
