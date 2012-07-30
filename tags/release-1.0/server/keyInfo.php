<?php
/*
 * Author:
 *   Johan Ekblad <jka@ekblad.org>
 * Portions:
 *   Alessandro Fanna <rospus@gmail.com>
 * License:
 *   GNU General Public License (GPL) version 2  
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 *
 * Get altitude (elevation) first that returns non-negative numbers:
 * 1) http://ws.geonames.org/astergdem?lat=63.827457&lng=20.261718
 * 2) http://ws.geonames.org/srtm3?lat=63.827457&lng=20.261718
 * 3) http://ws.geonames.org/gtopo30?lat=63.827457&lng=20.261718
 * 
 * Documentation: http://www.geonames.org/export/web-services.html
 */

include "includes/common.inc";

// connect to database first, for mysql_real_escape_string
connectToDatabase();

$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];
$key=$args["key"];
$keyEscaped=checkPresence(mysql_real_escape_string($key));

// getting totals for each code
$querytot=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'"');
$numbertot=mysql_num_rows($querytot); 
$queryok=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND buttoncode=10');
$numberok=mysql_num_rows($queryok);
$queryokmult=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND buttoncode=15');
$numberokmult=mysql_num_rows($queryokmult);
$querycp=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND buttoncode=30');
$numbercp=mysql_num_rows($querycp);
$queryko=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND buttoncode=20');
$numberko=mysql_num_rows($queryko);

$sql='SELECT tag,COUNT(*),MAX(time) AS time FROM PositionLog WHERE keyid="'.$keyEscaped.'" GROUP BY tag ORDER BY time DESC';
$result = mysql_query($sql);
echo '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
     <html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
     <head>
     <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
     <title>Campagna '.$key.'</title>
     <link id="theme" rel="stylesheet" type="text/css" href="style.css" title="theme" />
     <script type="text/javascript" language="javascript" src="js/addon.js"></script>
     <script type="text/javascript" language="javascript" src="js/custom.js"></script>
     </head><body>
     <!-- top wrapper -->  
     <div id="topWrapper"> 
       <div id="topBanner"></div>
     </div>';
    /*<table border="0">';*/
//$tz="";
$diff=0;

$nrows=0;
while($row = mysql_fetch_array($result))
  {
    $nrows++;
    $tag=$row[0];
    $escapedTag=mysql_real_escape_string($tag);
    $count=$row[1];
    $lastTime=$row[2];

    $sql2 = 'SELECT PositionLog.time,PositionLog.id,PositionLog.latitude,PositionLog.longitude,PositionLog.place,PositionLog.keyid,PositionLog.tag,PositionLog.place FROM rospuni_gips.PositionLog PositionLog WHERE (PositionLog.keyid = "'.$keyEscaped.'") and (PositionLog.tag="'.$escapedTag.'") ORDER BY PositionLog.time DESC LIMIT 1';
        $result2 = mysql_query($sql2) or die(mysql_error());
        $row2 = mysql_fetch_array($result2);

    if (is_null($row2[4]) || ($row2[4] == ""))
    {
	$ch = curl_init();
	$timeout = 1; // set to zero for no timeout
	curl_setopt ($ch, CURLOPT_URL, 'http://ws.geonames.org/findNearbyPlaceName?lat='.$row2[2].'&lng='.$row2[3]);
	curl_setopt ($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt ($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
	$d = curl_exec($ch);
	curl_close($ch);
        if (!is_null($d) && !($d == ""))
        {
           $answer = new SimpleXMLElement($d);
           $loc=$answer->geoname->toponymName;
           mysql_query('UPDATE PositionLog SET place="'.$loc.'" WHERE id="'.$id.'"');
           if ($loc == "" )
           {
              $loc="Non disponibile"; 
           }
        }
    }
    else
    {
       $loc = $row2[4]; 
    } 

    $sql3='select lastlog,access from PositionLogInfo where keyid="'.$keyEscaped.'" and tag="'.$escapedTag.'"';
    $result3 = mysql_query($sql3);
    $row3 = mysql_fetch_array($result3);
    $onlyShowLast=0;
    $accessString="";
    if ($row3)
    {
        $onlyShowLast=$row3[0];
        $accessString=$row3[1];
    }

/*    $sql4='select latitude,longitude from PositionLog where keyid="'.$keyEscaped.'" and tag="'.$escapedTag.'"';
    $result4 = mysql_query($sql4);
    $row4 = mysql_fetch_array($result4);
    $distkm=calcDist($result4);
    $distmi=$distkm*0.621371192;
    $dist=sprintf("%01.3f",$distkm).'/'.sprintf("%01.3f",$distmi); */

    if ($nrows == 1)
    {
echo "<hr noshade class=\"thin\">
      <ul id=\"breadcrumbs\">
        <li><a href=\"../\">Home</a></li>
        <li> > </li>
        <li>Campagna $key</li>
       </ul><br/>
      <div id=\"content\">
	<h1>Campagna $key<br/></h1></b>
	<div style=\"color: PaleGoldenrod; border-bottom: 1px solid #303030; width: 160px;\"><a href=\"all.php?key=$key&tag=$tag\">Totale punti</a> $numbertot</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allok.php?key=$key&tag=$tag\">Punti distribuiti</a> $numberok</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allokmult.php?key=$key&tag=$tag\">Casellari</a> $numberokmult</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allcp.php?key=$key&tag=$tag\">Cassette pubblicitarie</a> $numbercp</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allko.php?key=$key&tag=$tag\">Cause ostative</a> $numberko</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"showMap.php?key=$key&tag=$tag\" target=\"_blank\">Visualizza mappa</a></div>	
	<div class=\"clear\"></div>
	<hr noshade class=\"thin\">
	<div id=\"dwrap\">
	<div style=\"color: #ffffff;border: none;\">Operatore</div>
	<div style=\"color: #ffffff;border: none;\">CSV</div>
	<div style=\"color: #ffffff;border: none;\">KML</div>
	<div style=\"color: #ffffff; text-align: center;border: none;\">Punti</div>
	<div style=\"color: #ffffff; text-align: center;border: none;\">Ultimo punto</div>
	<div style=\"color: #ffffff; text-align: center;border: none;\">Località</div>
	<div style=\"color: #ffffff;border: none;\">Mappa</div>
	<div style=\"color: #ffffff; text-align: center; border: none;\">Aggregazioni <a href=\"faq.html\"><b>?</b></a></div>";
    }
    
    $localTime=date('d-m-Y H:i:s',strtotime($lastTime.$diff.' hour'));
    $extStr='External access: <a href="showPos.php?id='.$accessString.'">this url</a>';
    
    echo '<div><a href="alloper.php?key='.$key.'&tag='.$tag.'">'.$tag.'</a><br/>';
    $tag_clean=$tag;
    $tag=urlencode($tag);
    $key_clean=$key;
    $key=urlencode($key);
    
    if (! $accessString)
    {
       $extStr='<a href="genAccess.php?key='.$key.'&tag='.$tag.'">Generate external access URL</a>'; 
    }
    $saveLast='<a href="toggleSavePoint.php?key='.$key.'&tag='.$tag.'">Only save last point</a>';
    if ($onlyShowLast)
    {
        $saveLast='<a href="toggleSavePoint.php?key='.$key.'&tag='.$tag.'">Save all points</a>';
    } 
    echo '</div>
	<div><a href="showCSV.php?key='.$key.'&tag='.$tag.'">CSV data</a></div>
	<div><a href="showPointsKML.php?key='.$key.'&tag='.$tag.'">KML punti</a><br/><a href="showKML.php?key='.$key.'&tag='.$tag.'">KML tracciato</a></div>
	<div style=\'text-align: center;\'>'.$count.'</div>
	<div style=\'text-align: center;\'>'.$localTime.'</div>
	<div style=\'text-align: center;overflow: auto;\'>'.$loc.'</div>
	<div><a href="showPos.php?key='.$key.'&tag='.$tag.'">Map link</a></div>
	<div style=\'text-align: left;font-size:11px;\'><form method="GET" action="showPos2.php">campagna&nbsp;<br/><input type="text" name="key" value="'.str_ireplace('"',"&quot;",$key_clean).','.'"><br/>operatore&nbsp;<br/><input type="text" name="tag" value="'.str_ireplace('"',"&quot;",$tag_clean).','.'"><br/><input type="submit" name="Show" value="Visualizza mappa" style="background: #A9A9A9;\"</form></div>';
}

if ($nrows == 0)
{
  echo "Non ci sono dati disponibili per il codice $key.";
}

echo '	</div><div class="clear"></div><hr noshade class="thin"></div></div><div class="clear"></div><div id="bottomWrapper">
        <div id="bottom-links">
	<br/>
        &copy;2010
          <a href="http://www.integraitalia.com">INTEGRA SRL</a> - <a href="/credits.html">CREDITS</a>
        </div></body></html>';


function calcDist($resultset)
{
   $dist=doubleval(0.0);
   $lastlat=10000.0;
   $lastlng=10000.0;
   $pi = 3.141592654;
   $rad = doubleval($pi/180.0);
   $i=0; 
   while($latlng = mysql_fetch_array($resultset))
   {
       $i++;
       $lat=$latlng[0];
       $lng=$latlng[1];
       if ($lastlat < 1000)
       {
           $lat1 = doubleval($lastlat)*$rad;
           $lng1 = doubleval($lastlng)*$rad;
           $lat2 = doubleval($lat)*$rad;
           $lng2 = doubleval($lng)*$rad;
           $x = sin($lat1) * sin($lat2) + cos($lat1) * cos($lat2) * cos($lng1 - $lng2);
           if ($x < 1.0)
           {
               $pdist = 6371.2 * acos($x);
           }
           else
           {
               $pdist=0.0;
           }
           $dist += $pdist; 
       }
       $lastlat=$lat;
       $lastlng=$lng;
   }
   return $dist;
}
