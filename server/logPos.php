<?php
/*
 * Author:
 *   Johan Ekblad <jka@ekblad.org>
 * License:
 *   GNU General Public License (GPL) version 2  
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 */

include "includes/common.inc";

// connect to database first, for mysql_real_escape_string
connectToDatabase();

$logIP=1;  // Set to 0 if you don't want to log IP numbers
$logTime=date("Y-m-d H:i:s");
$ip="";
if ($logIP)
{
    $ip=$_SERVER['REMOTE_ADDR']; // Proxy? check: HTTP_X_FORWARDED_FOR?
}


// PARSE ARGUMENTS
// ***************
$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];

// mandatory parameters
$key=checkPresence(mysql_real_escape_string($args["key"]));
$tag=checkPresence(mysql_real_escape_string($args["tag"]));
$latitude=checkPresence(checkNumericParameter($args["latitude"]));
$longitude=checkPresence(checkNumericParameter($args["longitude"]));
// optional parameters:
$altitude=checkNumericParameter($args["altitude"]);
$speed=checkNumericParameter($args["speed"]);
$bearing=checkNumericParameter($args["bearing"]);
$accuracy=checkNumericParameter($args["accuracy"]);
$provider=mysql_real_escape_string($args["provider"]);


$sql='select lastlog from PositionLogInfo where keyid="'.$key.'" and tag="'.$tag.'"';
$result = mysql_query($sql);
$row = mysql_fetch_array($result);
if ($row[0]) { // lastlog=true, which means the last logentry should be overwritten each call (no trail is saved in the db)
        $sql = 'select max(id) from PositionLog where keyid="'.$key.'" and tag="'.$tag.'"';
        $result = mysql_query($sql);
        $row = mysql_fetch_array($result);
        $lastid=$row[0];
        $sql = 'update PositionLog set time=now(),latitude="'.$latitude.'",longitude="'.$longitude.'",ip="'.$ip.'"';
        if (isset($altitude))
	{
	  $sql .= ',altitude="'.$altitude.'"';
        }
        if (isset($speed))
	{
	  $sql .= ',speed="'.$speed.'"';
        }
        if (isset($bearing))
	{
	  $sql .= ',bearing="'.$bearing.'"';
        }
        if (isset($accuracy))
	{
	  $sql .= ',accuracy="'.$accuracy.'"';
        }
        if (isset($provider))
	{
	  $sql .= ',provider="'.$provider.'"';
        }
        $sql .= ' where id='.$lastid;
        
        mysql_query($sql);
}
else
{   
    $extraFields='';
    $extraValues='';
    if (isset($altitude))
    {
        $extraFields.=',altitude';
	$extraValues.=',"'.$altitude.'"';
    }
    if (isset($speed))
    {
        $extraFields.=',speed';
	$extraValues.=',"'.$speed.'"';
    }
    if (isset($bearing))
    {
        $extraFields.=',bearing';
	$extraValues.=',"'.$bearing.'"';
    }
    if (isset($accuracy))
    {
        $extraFields.=',accuracy';
	$extraValues.=',"'.$accuracy.'"';
    }
    if (isset($provider))
    {
        $extraFields.=',provider';
	$extraValues.=',"'.$provider.'"';
    }

    $sql='insert into PositionLog (time,keyid,tag,latitude,longitude,ip'.$extraFields.') values (now(),"'.$key.'","'.$tag.'","'.$latitude.'","'.$longitude.'","'.$ip.'"'.$extraValues.')';
    mysql_query($sql);
}

// check for errors
if (mysql_errno() == 0) 
{
    echo "OK";
} 
else 
{
  echo "Database error ! (".mysql_error().")";
	//echo "Mysql returned code " . mysql_errno() . ": " . mysql_error() . "\n";
}

mysql_close();

