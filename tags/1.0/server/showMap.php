<?php
/*
 * Author:
 *   Johan Ekblad <jka@ekblad.org>
 * Portions:
 *   Davide Rodomonti <davide.friends@gmail.com>
 * Portions:
 *   Alessandro Fanna <rospus@gmail.com>
 * License:
 *   GNU General Public License (GPL) version 2
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 */

$all=explode("/",$_SERVER['REQUEST_URI']);
$args=$all["query_params"];
$db=parse_ini_file("settings.ini");
$dbuser=$db["user"];
$dbpassword=$db["password"];
$dbdatabase=$db["database"];
$dbhost=$db["host"];
$conn=mysql_connect($dbhost,$dbuser,$dbpassword);
mysql_select_db($dbdatabase,$conn);
$id=$args["key"];
$key=$_REQUEST["key"];
?>
<!DOCTYPE>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta name="title" content="GiPS beta" />
    <meta name="description" content="GiPS - Sistema di monitoraggio puntuale" />
    <meta name="keywords" content="gps, direct marketing, mappe, android, html5" />
    <meta name="language" content="it" />
    <meta name="subject" content="Visualizzazione campagne sul territorio" />
    <meta name="robots" content="All" />
    <meta name="copyright" content="INTEGRA srl" />
    <meta name="abstract" content="Visualizzazione campagne sul territorio" />
    <meta name="MSSmartTagsPreventParsing" content="true" />
    <link id="theme" rel="stylesheet" type="text/css" href="style.css" title="theme" />
    <script type="text/javascript" language="javascript" src="js/addon.js"></script>
    <script type="text/javascript" language="javascript" src="js/custom.js"></script>
    <title>GiPS: cluster sperimentale</title>

    <style type="text/css">

      body {
        margin: 0;
      }

      #map-container {
        padding: 6px;
        border-width: 1px;
        border-style: solid;
        border-color: #ccc #ccc #999 #ccc;

        width: 100%;
      }

      #map {
        width: 100%;
        height: 600px;
      }

      #actions {
        list-style: none;
        padding: 0;
      }

      #inline-actions {
        padding-top: 10px;
      }

      .item {
        margin-left: 30px;
      }
    </style>

    <script src="http://www.google.com/jsapi"></script>
    <script src="data.php?key=<?php echo $key ?>" type="text/javascript"></script>
    <script type="text/javascript">
      var script = '<script type="text/javascript" src="../src/markerclusterer';
      if (document.location.search.indexOf('packed') !== -1) {
        script += '_packed';
      }
      if (document.location.search.indexOf('compiled') !== -1) {
        script += '_compiled';
      }
      script += '.js"><' + '/script>';
      document.write(script);
    </script>

    <script type="text/javascript">
      google.load('maps', '3', {
        other_params: 'sensor=false'
      });

      google.setOnLoadCallback(initialize);

      var styles = [[{
        url: '../images/people35.png',
        height: 35,
        width: 35,
        opt_anchor: [16, 0],
        opt_textColor: '#ff00ff',
        opt_textSize: 10
      }, {
        url: '../images/people45.png',
        height: 45,
        width: 45,
        opt_anchor: [24, 0],
        opt_textColor: '#ff0000',
        opt_textSize: 11
      }, {
        url: '../images/people55.png',
        height: 55,
        width: 55,
        opt_anchor: [32, 0],
        opt_textSize: 12
      }], [{
        url: '../images/conv30.png',
        height: 27,
        width: 30,
        anchor: [3, 0],
        textColor: '#ff00ff',
        opt_textSize: 10
      }, {
        url: '../images/conv40.png',
        height: 36,
        width: 40,
        opt_anchor: [6, 0],
        opt_textColor: '#ff0000',
        opt_textSize: 11
      }, {
        url: '../images/conv50.png',
        width: 50,
        height: 45,
        opt_anchor: [8, 0],
        opt_textSize: 12
      }], [{
        url: '../images/heart30.png',
        height: 26,
        width: 30,
        opt_anchor: [4, 0],
        opt_textColor: '#ff00ff',
        opt_textSize: 10
      }, {
        url: '../images/heart40.png',
        height: 35,
        width: 40,
        opt_anchor: [8, 0],
        opt_textColor: '#ff0000',
        opt_textSize: 11
      }, {
        url: '../images/heart50.png',
        width: 50,
        height: 44,
        opt_anchor: [12, 0],
        opt_textSize: 12
      }]];

      var markerClusterer = null;
      var map = null;
//      var imageUrl = 'http://chart.apis.google.com/chart?cht=mm&chs=24x32&' +
//          'chco=FFFFFF,008CFF,000000&ext=.png';
      var imageUrlG = 'images/greenV.png';
      var imageUrlB = 'images/blueV.png';
      var imageUrlO = 'images/orangeV.png';
      var imageUrlR = 'images/redV.png';


      function refreshMap() {
        if (markerClusterer) {
          markerClusterer.clearMarkers();
        }

        var markers = [];
        
        var markerImageG = new google.maps.MarkerImage(imageUrlG, null, null, new google.maps.Point(6, 28), new google.maps.Size(25, 32));
        var markerImageB = new google.maps.MarkerImage(imageUrlB, null, null, new google.maps.Point(6, 28), new google.maps.Size(25, 32));
        var markerImageO = new google.maps.MarkerImage(imageUrlO, null, null, new google.maps.Point(6, 28), new google.maps.Size(25, 32));
        var markerImageR = new google.maps.MarkerImage(imageUrlR, null, null, new google.maps.Point(6, 28), new google.maps.Size(25, 32));

//echo "$num_rows Rows\n";
  // echo "$sql\n";
	
	 for (var i = 0; i < data.count; ++i) {
          var latLng = new google.maps.LatLng(data.pt[i].lt,data.pt[i].lg)

// change marker for each buttoncode
          if (data.pt[i].st == 10) markerImage = markerImageG;
          else if (data.pt[i].st == 15) markerImage = markerImageB;
          else if (data.pt[i].st == 30) markerImage = markerImageO;
          else markerImage = markerImageR;

          var marker = new google.maps.Marker({
           position: latLng,
           draggable: false,
           icon: markerImage
          });
          markers.push(marker);
        }

        var zoom = parseInt(document.getElementById('zoom').value, 10);
        var size = parseInt(document.getElementById('size').value, 10);
        var style = parseInt(document.getElementById('style').value, 10);
        zoom = zoom == -1 ? null : zoom;
        size = size == -1 ? null : size;
        style = style == -1 ? null: style;

        markerClusterer = new MarkerClusterer(map, markers, {
          maxZoom: zoom,
          gridSize: size,
          styles: styles[style]
        });
      }

      function initialize() {
        map = new google.maps.Map(document.getElementById('map'), {
          zoom: 6,
          center: new google.maps.LatLng(42.6647, 14.0198),
          mapTypeId: google.maps.MapTypeId.ROADMAP
        });

        var refresh = document.getElementById('refresh');
        google.maps.event.addDomListener(refresh, 'click', refreshMap);

        var clear = document.getElementById('clear');
        google.maps.event.addDomListener(clear, 'click', clearClusters);

        refreshMap();
      }

      function clearClusters(e) {
        e.preventDefault();
        e.stopPropagation();
        markerClusterer.clearMarkers();
      }

    </script>
  </head>
  <body>
    <!-- top wrapper -->  
    <div id="topWrapper"> 
      <div id="topBanner"></div> 
    </div>  
    <!-- end top wrapper -->  
    <div id="wrapper"> 
      <div id="content">

<!-- inizio collage -->

<h1>GiPS - Cluster sperimentale</h1>
<!--    <p>
      <a href="?compiled">Compiled</a> |
      <a href="?packed">Packed</a> |
      <a href="?">Standard</a> version of the script.
    </p> -->
    <div id="map-container">
      <div id="map"></div>
    </div>
    <div id="inline-actions">
      <span>Max zoom level:
        <select id="zoom">
          <option value="-1">Default</option>
          <option value="7">7</option>
          <option value="8">8</option>
          <option value="9">9</option>
          <option value="10">10</option>
          <option value="11">11</option>
          <option value="12">12</option>
          <option value="13">13</option>
          <option value="14">14</option>
          <option value="15">15</option>
          <option value="16">16</option>
          <option value="17">17</option>
        </select>

      </span>
      <span class="item">Cluster size:
        <select id="size">
          <option value="-1">Default</option>
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
          <option value="30">30</option>
          <option value="50">50</option>
          <option value="70">70</option>
          <option value="80">80</option>
        </select>
      </span>
      <span class="item">Cluster style:
        <select id="style">
          <option value="-1">Default</option>
          <option value="0">People</option>
          <option value="1">Conversation</option>
          <option value="2">Heart</option>
       </select>
       <input id="refresh" type="button" value="Refresh Map" class="item"/>
       <a href="#" id="clear">Clear</a>
    </div>
<!-- fine collage -->

    <div class="clear"></div>
    <div class="clear"></div>
    <div class="clear"></div>

      </div>  
      <!-- end container --> 
    </div>  
    <div id="bottomWrapper"> 
     <div id="bottom-links">
	<br/>
        &copy;2010
          <a href="http://www.integraitalia.com">INTEGRA SRL</a>
        </div> 
      </div> 
    </div>    
  </body>
</html>
