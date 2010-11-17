<?php
/*
 * Author:
 *   Johan Ekblad <jka@ekblad.org>
 * Portions:
 *    Alessandro Fanna <rospus@gmail.com>
 * License:
 *   GNU General Public License (GPL) version 2  
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 */

include "includes/common.inc";

// connect to database first, for mysql_real_escape_string
connectToDatabase();

//** Limit of result per page
$limit=100;

/* Checks that the start variable isn’t negative. If it finds that it is, it redirects to the default page where the start is 0.
if($_GET['page'] < 0){
header("Location: index.php?page=0&limit=".$limit);
} */

if (isset($_GET['page'])) { $page = $_GET['page']; } else { $page=1; }; 
$start_from = ($page-1) * $limit; 
$prev_page = $page - 1; //paging
$next_page = $page + 1; //paging


$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];
// mandatory parameters
$key=$args["key"];
$keyEscaped=checkPresence(mysql_real_escape_string($key));
$tag=$args["tag"];
$tagEscaped=checkPresence(mysql_real_escape_string($tag));

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

if ($numbertot <= $limit) {
$num_pages = 1;
} else if (($numbertot % $limit) == 0) {
$num_pages = ($numbertot / $limit);
} else {
$num_pages = ($numbertot / $limit) + 1;
}
$num_pages = (int) $num_pages;

if (($page > $num_pages) || ($page < 0)) {
error("You have specified an invalid page number");
}
	// use this line instead
	// $max_pages=ceil($img_total/$pgsize);

$sql='SELECT time,buttoncode,accuracy,tag,ip,provider,latitude,longitude,place,zip,province,id FROM PositionLog WHERE keyid="'.$keyEscaped.'" ORDER BY time DESC LIMIT '.$start_from.','.$limit.'';
$result = mysql_query($sql);

echo '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
     <html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
     <head>
     <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
     <title>Punti campagna '.$key.'</title>
     <link id="theme" rel="stylesheet" type="text/css" href="style.css" title="theme" />
     <script type="text/javascript" language="javascript" src="js/addon.js"></script>
     <script type="text/javascript" language="javascript" src="js/custom.js"></script>

     </head><body>
     <!-- top wrapper -->  
     <div id="topWrapper"> 
       <div id="topBanner"></div>
     </div>';

echo "<hr noshade class=\"thin\">
      <ul id=\"breadcrumbs\">
        <li><a href=\"../\">Home</a></li>
        <li> > </li>
        <li><a href=\"../keyInfo.php?key=$key&tag=$tag\">Campagna $key</a></li>
        <li> > </li>
        <li>Punti campagna</li>
       </ul><br/>
      <div id=\"content\">
	<h1 >Punti campagna $key<br/></h1></b>
	<div style=\"border-bottom: 1px solid #303030; width: 13%;\">Totale punti</a> $numbertot</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allok.php?key=$key&tag=$tag\">Punti distribuiti</a> $numberok</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allokmult.php?key=$key&tag=$tag\">Casellari</a> $numberokmult</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allcp.php?key=$key&tag=$tag\">Cassette pubblicitarie</a> $numbercp</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allko.php?key=$key&tag=$tag\">Cause ostative</a> $numberko</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"showMap.php?key=$key&tag=$tag\" target=\"_blank\">Visualizza mappa</a></div>
	<div class=\"clear\"></div>
	<hr noshade class=\"thin\">
<div id=\"container7\" style=\"color: #ffffff; border: none;\">
<div id=\"container4\" style=\"color: #ffffff; border: none;\">
<div id=\"container5\" style=\"color: #ffffff; border: none;\">
	<div id=\"container4\" style=\"color: #ffffff; border: none;\">
		<div id=\"container3\" style=\"color: #ffffff; border: none;\">
			<div id=\"container2\" style=\"color: #ffffff; border: none;\">
				<div id=\"container1\" style=\"color: #ffffff; border: none;\">
					<div id=\"col1\">Operatore</div>
					<div id=\"col2\">Ultimo punto</div>
					<div id=\"col3\">Status</div>
					<div id=\"col4\">Precisione</div>
					<div id=\"col5\">Provider</div>
					<div id=\"col6\">Località</div>
					<div id=\"col7\">Geocodifica</div>
				</div>
			</div>
		</div>
	</div>
</div>
</div>
</div>";

$loc="";

while($row = mysql_fetch_array($result))
  {
    $time=nullfill($row[0]);
    $buttoncode=nullfill($row[1]);
    $accuracy=round(nullfill($row[2])); //$numero_eccesso = ceil($numero);
    $tag=nullfill($row[3]);
    $ip=nullfill($row[4]);
    $provider=nullfill($row[5]);
    $lat=nullfill($row[6]);
    $lng=nullfill($row[7]);
    $place=nullfill($row[8]);
    $zip=nullfill($row[9]);
    $province=nullfill($row[10]);
    $id=nullfill($row[11]);
    $localTime=date('d-m-Y H:i:s',strtotime($time));
/*    $provider=nullfill($row[8]); 
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
        echo $key.','.$tag.','.str_ireplace(" ","T",$time).'+01:00,'.$lat.','.$long.','.$altitude.','.$speed.','.$bearing.','.$accuracy.','.$provider.','.$buttoncode."\n"; 
       
    } */

    echo '
<div id="container7">
<div id="container4">
<div id="container5">
	<div id="container4">
		<div id="container3">
			<div id="container2">
				<div id="container1">
					<div id="col1"><a href="alloper.php?key='.$key.'&tag='.$tag.'">'.$tag.'</a></div>
					<div id="col2">'.$localTime.'</div>';

// Replacement text to display for different codes and colors
    if ($buttoncode == 0)
    {
      $status='Non codificato';
      $textcolor='color:#a9a9a9';
    }
    elseif ($buttoncode == 10)
    {
      $status='Distribuito';
      $textcolor='color:PaleGreen';
    }
    elseif ($buttoncode == 15)
    {
      $status='Casellario';
      $textcolor='color:SpringGreen';
    }
    elseif ($buttoncode == 20)
    {
      $status='Causa ostativa';
      $textcolor='color:OrangeRed';
    }
    elseif ($buttoncode == 25)
    {
      $status='Inaccessibile';
      $textcolor='color:Tomato';
    }
    elseif ($buttoncode == 30)
    {
      $status='Cassetta pubblicitaria';
      $textcolor='color:LightSkyBlue';
    }

    echo '<div id="col3" style="'.$textcolor.'">'.$status.'</div>
	  <div id="col4" >'.$accuracy.'</div>
	  <div id="col5">'.$provider.'</div>';

// caching geonames.org answer if available

    if (is_null($place) || ($place == "") || is_null($zip) || ($zip == "") || is_null($province) || ($province == ""))
    {
       $ch = curl_init();
       $timeout = 1; // set to zero for no timeout
//       curl_setopt ($ch, CURLOPT_URL, 'http://ws.geonames.org/findNearbyPlaceName?lat='.$lat.'&lng='.$lng);
       curl_setopt ($ch, CURLOPT_URL, 'http://ws.geonames.org/findNearbyPostalCodes?lat='.$lat.'&lng='.$lng.'&radius=20');
       curl_setopt ($ch, CURLOPT_RETURNTRANSFER, 1);
       curl_setopt ($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
       $d = curl_exec($ch);
       curl_close($ch);
       if (!is_null($d) && !($d == ""))
       {
          $answer = new SimpleXMLElement($d);
//          $loc = $answer->geoname->name;
          $zip = $answer->code->postalcode;
          $loc = $answer->code->name;
          $prov = $answer->code->adminCode2;
          mysql_query('UPDATE PositionLog SET zip="'.$zip.'", place="'.$loc.'", province="'.$prov.'" WHERE id="'.$id.'"');
          if ($loc == "" )
          {
             $loc="Non disponibile"; 
          }
       }
    }
    else
    {
       $cap = $zip;
       $loc = $place;
       $prov = $province;
    } 

echo '<div id="col6">'.$loc.' ('.$prov.') - '.$cap.'</div>
      <div id="col7"><a href="reverse.php?key='.$key.'&tag='.$tag.'&lat='.$lat.'&long='.$lng.'&err='.$accuracy.'" target="_blank">Indirizzo e mappa</a></div>
				</div>
			</div>
		</div>
	</div>
</div>
</div>
</div>';
  }
echo '</div>';

/* if ($nrows == 0)
{
  echo "Non ci sono dati disponibili per il codice $key.";
} */

// START paging
echo '<div class="clear"></div>
      <hr noshade class="thin">
      <div id="content" style="text-align: center;">';
// Previous page
if ($prev_page) {
echo "<a href=\"$PHP_SELF?key=$key&tag=$tag&page=$prev_page\">Prec.</a> < ";
}
// Page # direct links
// If you don't want direct links to each page, you should be able to
// safely remove this chunk.
for ($i = 1; $i <= $num_pages; $i++) {
if ($i != $page) {
echo " <a href=\"$PHP_SELF?key=$key&tag=$tag&page=$i\">$i</a>";
} else {
echo " $i ";
}
}
// Next page
if ($page != $num_pages && $num_pages > 1) {
echo " > <a href=\"$PHP_SELF?key=$key&tag=$tag&page=$next_page\"> Succ.</a>";
}
// END paging

echo '</div></div><div class="clear"></div><div id="bottomWrapper">
        <div id="bottom-links">
	<br/>
        &copy;2010
          <a href="http://www.integraitalia.com">INTEGRA SRL</a>
        </div></body></html>';
