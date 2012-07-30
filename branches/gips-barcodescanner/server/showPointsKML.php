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

header('Content-type: application/vnd.google-earth.kml+xml');
$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];
$key=$args["key"];
$keyEscaped=checkPresence(mysql_real_escape_string($key));
$tag=$args["tag"];
$tagEscaped=checkPresence(mysql_real_escape_string($tag));

// BUG: quotes in tags aren't handled very well (for example: bla'"bla will result in a filename of bla.kml)
header('Content-Disposition: attachment; filename="'.urlencode($tag).'.kml"');

$sql='select latitude,longitude,time from PositionLog where keyid="'.$keyEscaped.'" and tag="'.$tagEscaped.'"';
$result = mysql_query($sql);
echo '<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2"> 
<!-- Data generated at http://gipsin.it/
-->
<Document>
  <name>www.gipsin.it</name>
  <description>GiPS - Sistema di monitoraggio e localizzazione</description>
';
while($row = mysql_fetch_array($result))
  {
    $lat=$row[0];
    $long=$row[1];
    $time=$row[2];
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
        echo '
  <Placemark>
    <TimeStamp>
      <when>'.str_ireplace(" ","T",$time).'+01:00</when>
    </TimeStamp>
    <Point>
      <coordinates>
          '.$long.','.$lat.'
      </coordinates>
    </Point>
  </Placemark>';
    }
  }
echo'  </Document>
</kml>
';
