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

$colors=array("purple","ltblu","blu","pink","red","grn","ylw","wht");

$index=$args["index"];
if ($index == "" || $index < 0 || $index>count($colors))
{
  $index=7;
}

// BUG: quotes in tags aren't handled very well (for example: bla'"bla will result in a filename of bla.kml)
header('Content-Disposition: attachment; filename="'.urlencode($tag).'.kml"');

$sql='select latitude,longitude,time from PositionLog where id=(select max(id) from PositionLog where keyid="'.$keyEscaped.'" and tag="'.$tagEscaped.'")';
$result = mysql_query($sql);
echo '<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">
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
    $lastTime=$row[2];
  }
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
       
        $tz="GMT+1";
        $diff=0;
        $d = file_get_contents('http://ws.geonames.org/timezone?lat='.$lat.'&lng='.$long);
        $answer = new SimpleXMLElement($d);
        $tz=$answer->timezone->timezoneId;
        $diff=$answer->timezone->rawOffset - 1;
        if (diff >= 0)
        {
            $diff="+".$diff;
        }
      
        if ($tz == "" )
        {
            $tz="GMT+1"; 
            $diff=0;
        }
        $localTime=date('Y-m-d H:i:s',strtotime($lastTime.$diff.' hour'));

        echo $long.','.$lat."\n"; 
echo '<Placemark>
      <Style>
        <IconStyle>
          <Icon>
            <href>http://maps.google.com/mapfiles/kml/paddle/'.$colors[$index].'-blank.png</href>
          </Icon>
        </IconStyle>
      </Style>
      <description>Current Position<br/>'.$localTime.'<br/>'.$tz.'</description>
      <name>'.str_ireplace("'","&apos;",$tag).'</name>
      <LookAt>
        <longitude>'.$long.'</longitude>
        <latitude>'.$lat.'</latitude>
        <range>500.00</range>
        <heading>0</heading>
      </LookAt>
      <Point>
        <coordinates>'.$long.','.$lat.'</coordinates>
      </Point>
    </Placemark>
  </Document>
</kml>
';
}
