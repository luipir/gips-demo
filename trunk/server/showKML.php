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
//$colors=array("ltblu","blu","purple","pink","red","grn","ylw","wht");
$colors=array("ffff007f","ffffaf30","ffff8080","ffb458cf","ff6a6ae7","ff00ff00","ff00ffff","ffffffff");

$index=$args["index"];
if ($index == "" || $index < 0 || $index>count($colors))
{
  $index=4;
}
// BUG: quotes in tags aren't handled very well (for example: bla'"bla will result in a filename of bla.kml)
header('Content-Disposition: attachment; filename='.urlencode($tag).'.kml');

$sql='select latitude,longitude from PositionLog where keyid="'.$keyEscaped.'" and tag="'.$tagEscaped.'"';
$result = mysql_query($sql);
echo '<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">
<!-- Data generated at http://gipsin.it/
-->
<Document>
  <name>gipsin.it</name>
  <description>GiPS - Sistema di monitoraggio e localizzazione</description>
  <Style id="lineColor">
    <LineStyle>
      <color>'.$colors[$index].'</color>
      <width>4</width>
    </LineStyle>
  </Style>
  <Placemark>
    <name>'.str_ireplace("'","&apos;",$tag).'</name>
    <styleUrl>#lineColor</styleUrl>
    <LineString>
      <altitudeMode>relative</altitudeMode>
        <coordinates>
';
while($row = mysql_fetch_array($result))
  {
    $lat=$row[0];
    $long=$row[1];
    $time=$row[2];
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
        echo $long.','.$lat."\n"; 
    }
  }
echo '        </coordinates>
      </LineString>
    </Placemark>
  </Document>
</kml>
';
