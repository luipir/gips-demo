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
$limit=200;

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

// getting the totals for each code
$querytot=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND tag="'.$tagEscaped.'"');
$numbertot=mysql_num_rows($querytot); 
$queryok=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND tag="'.$tagEscaped.'" AND buttoncode=10');
$numberok=mysql_num_rows($queryok); 
$querycp=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND tag="'.$tagEscaped.'" AND buttoncode=30');
$numbercp=mysql_num_rows($querycp);
$queryko=mysql_query('SELECT * FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND tag="'.$tagEscaped.'" AND buttoncode=20');
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

$sql='SELECT time,buttoncode,accuracy,tag,ip,provider,latitude,longitude FROM PositionLog WHERE keyid="'.$keyEscaped.'" AND tag="'.$tagEscaped.'" ORDER BY time DESC LIMIT '.$start_from.','.$limit.'';
$result = mysql_query($sql);

echo '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
     <html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
     <head>
     <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
     <title>Punti operatore</title>
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
        <li><a href=\"../all.php?key=$key&tag=$tag\">Punti campagna</a></li>
        <li> > </li>
        <li>Punti operatore</li>
       </ul><br/>
      <div id=\"content\">
	<h1>Punti operatore $tag campagna $key<br/></h1></b>
	<div>Totale punti</a> $numbertot</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"alloperok.php?key=$key&tag=$tag\">Punti distribuiti</a> $numberok</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"allopercp.php?key=$key&tag=$tag\">Cassette pubblicitarie</a> $numbercp</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"alloperko.php?key=$key&tag=$tag\">Cause ostative</a> $numberko</div>
	<div style=\"color: PaleGoldenrod;\"><a href=\"showMap2.php?key=$key&tag=$tag\">Visualizza mappa</a></div>
	<div class=\"clear\"></div>
	<hr noshade class=\"thin\">
	<div id=\"dwrapseven\">
	<div style=\"color: #ffffff; border: none;\">Operatore</div>
	<div style=\"color: #ffffff; border: none\">Ultimo punto</div>
	<div style=\"color: #ffffff;text-align: center; border: none\">Status</div>
	<div style=\"color: #ffffff;text-align: center; border: none\">Precisione</div>
	<div style=\"color: #ffffff; border: none\">Provider</div>
	<div style=\"color: #ffffff; border: none\">IP</div>
	<div style=\"color: #ffffff; border: none\">Geocodifica</div>
	";


while($row = mysql_fetch_array($result))
  {
    $time=nullfill($row[0]);
    $buttoncode=nullfill($row[1]);
    $accuracy=nullfill($row[2]);
    $tag=nullfill($row[3]);
    $ip=nullfill($row[4]);
    $provider=nullfill($row[5]);
    $lat=nullfill($row[6]);
    $long=nullfill($row[7]);
    $localTime=date('d-m-Y H:i:s',strtotime($time));
/*    $provider=nullfill($row[8]); 
    if (!($lat == "" || $lat == " " || $long == "" || $long == " "))
    {
        echo $key.','.$tag.','.str_ireplace(" ","T",$time).'+01:00,'.$lat.','.$long.','.$altitude.','.$speed.','.$bearing.','.$accuracy.','.$provider.','.$buttoncode."\n"; 
       
    } */

    echo '<div><a href="alloper.php?key='.$key.'&tag='.$tag.'">'.$tag.'</a></div>
	  <div>'.$localTime.'</div>';

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
  elseif ($buttoncode == 20)
  {
      $status='Causa ostativa';
      $textcolor='color:Tomato';
  }
  elseif ($buttoncode == 30)
  {
    $status='Cassetta pubblicitaria';
    $textcolor='color:LightSkyBlue';
  }

    echo '<div style="text-align:center;'.$textcolor.'">'.$status.'</div>
	  <div style="text-align:center;">'.$accuracy.'</div>
	  <div>'.$provider.'</div>
	  <div>'.$ip.'</div>
	  <div><a href="reverse.php?key='.$key.'&tag='.$tag.'&lat='.$lat.'&long='.$long.'&err='.$accuracy.'" target="_blank">Indirizzo e mappa</a></div>';
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
