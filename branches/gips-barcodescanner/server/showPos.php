<?php
/*
 * Author:
 *   Johan Ekblad <jka@ekblad.org>
 * License:
 *   GNU General Public License (GPL) version 2
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 */

include "includes/common.inc";

$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];
$id=$args["id"];
if ($id)
{
    connectToDatabase();

    $id=mysql_real_escape_string($args["id"]);
    if (!(ereg('^[a-zA-Z0-9]+$',$id)))
    {
        die("L'id contiene caratteri illeciti.");
    }

    $sql='select keyid,tag from PositionLogInfo where access="'.$id.'"';
    $result = mysql_query($sql);
    $row = mysql_fetch_array($result);
    $keys = array();
    $keys[0] = $row[0];
    $tags = array();
    $tags[0] = $row[1];
}
else
{

    $key=checkPresence($args["key"]);
    $keys=explode(",",$key);
    $tag=checkPresence($args["tag"]);
    $tags=explode(",",$tag);
}
if (count($keys) != count($tags))
{
    die('Number of keys and tags must be equal (the ","-char separates the keys and tags)');
}
echo '
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>GiPS beta</title>
    <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=true&amp;key=ABQIAAAAJa0Ocy-n4vIvcYjy9UqglBSJWT7SPzFpXFVrgWrFLBP9m1idyRRHmbRXbDg_CrZ5E-UWBQzNPEUMFg" type="text/javascript"></script>
    <script type="text/javascript">

var map;
var geoXml=new Array(); 
var geoXmlPoint=new Array();
var ntracks='.count($keys).';
var toggleState = 1;
var firstTime = 1;

var geoCallback = function()
{
  if (firstTime == 1)
  {
    for (i=0;i<ntracks;i++)
    {
      geoXml[i].gotoDefaultViewport(map);
    }
    map.setUIToDefault();
    firstTime=0;
  }
  if (toggleState == 1)
  {
    for (i=0; i<ntracks; i++)
    {
      map.addOverlay(geoXml[i]);
    }
  }
  for (i=0; i<ntracks; i++)
  {
    map.addOverlay(geoXmlPoint[i]);
  }
  map.setCenter(geoXmlPoint[ntracks-1].getDefaultCenter());

}

function initialize() {
  if (GBrowserIsCompatible()) {
    var rnd=randomString();
';
for ($i=0; $i<count($keys); $i++)
{
    echo '    geoXmlPoint['.$i.'] = new GGeoXml("http://gipsin.it/showPosKML.php?key='.urlencode($keys[$i]).'&tag='.urlencode($tags[$i]).'&index='.$i.'&rnd="+rnd);'."\n";
    echo '    geoXml['.$i.'] = new GGeoXml("http://gipsin.it/showKML.php?key='.urlencode($keys[$i]).'&tag='.urlencode($tags[$i]).'&index='.$i.'&rnd="+rnd,geoCallback);'."\n";
}
    echo '
    map = new GMap2(document.getElementById("map_canvas")); 
  }
} 

function randomString() {
	var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
	var string_length = 8;
	var randomstring = "";
	for (var i=0; i<string_length; i++) {
		var rnum = Math.floor(Math.random() * chars.length);
		randomstring += chars.substring(rnum,rnum+1);
	}
	return randomstring;
}


function toggleMyKml() {
  if (toggleState == 1) {
    for (i=0; i<ntracks; i++)
    {
      map.removeOverlay(geoXml[i]);
    }
    toggleState = 0;
  } else {
    for (i=0; i<ntracks; i++)
    {
      map.addOverlay(geoXml[i]);
    }
    toggleState = 1;
  }
}

function updateNow()
{
    var rnd=randomString();
    for (i=0; i<ntracks; i++)
    { 
      map.removeOverlay(geoXml[i]);
      map.removeOverlay(geoXmlPoint[i]);
    }
';
for ($i=0; $i<count($keys); $i++)
{
    echo '    geoXmlPoint['.$i.'] = new GGeoXml("http://gipsin.it/showPosKML.php?key='.urlencode($keys[$i]).'&tag='.urlencode($tags[$i]).'&index='.$i.'&rnd="+rnd);'."\n";
    echo '    geoXml['.$i.'] = new GGeoXml("http://gipsin.it/showKML.php?key='.urlencode($keys[$i]).'&tag='.urlencode($tags[$i]).'&index='.$i.'&rnd="+rnd,geoCallback);'."\n";
}
echo '

}

    </script>

  </head>
  <body onload="initialize()" onunload="GUnload()" style="font-family: Arial;border: 0 none;">
    <div id="map_canvas" style="width: '.(getEnv("isMobileDevice")?"455":"1024").'px; height: '.(getenv("isMobileDevice")?"225":"800").'px; float:left; border: 1px solid black;"></div>
  </div>
  <br clear="all"/>
  <input type="button" value="Traccia on/off" onClick="toggleMyKml();"/>
  <input type="button" value="Aggiorna" onClick="updateNow();"/>
  </body>
</html>';
