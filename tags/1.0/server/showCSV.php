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

$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];
// mandatory parameters
$key=$args["key"];
$keyEscaped=checkPresence(mysql_real_escape_string($key));
$tag=$args["tag"];
$tagEscaped=checkPresence(mysql_real_escape_string($tag));

$sql='select time,latitude,longitude,altitude,speed,bearing,accuracy,provider,buttoncode from PositionLog where keyid="'.$keyEscaped.'" and tag="'.$tagEscaped.'"';
$result = mysql_query($sql);
while($row = mysql_fetch_array($result))
  {
    $time=nullfill($row[0]);
    $lat=nullfill($row[1]);
    $long=nullfill($row[2]);
    $altitude=nullfill($row[3]);
    $speed=nullfill($row[4]);
    $bearing=nullfill($row[5]);
    $accuracy=nullfill($row[6]);
    $provider=nullfill($row[7]);
    $provider=nullfill($row[8]);
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
        echo $key.','.$tag.','.str_ireplace(" ","T",$time).'+01:00,'.$lat.','.$long.','.$altitude.','.$speed.','.$bearing.','.$accuracy.','.$provider.','.$buttoncode."\n"; 
       
    }
  }

