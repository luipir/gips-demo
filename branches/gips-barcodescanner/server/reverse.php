<?php
/*
 * Author:
 *   Alessandro Fanna <rospus@gmail.com>
 * License:
 *   GNU General Public License (GPL) version 2
 *   (see: http://www.gnu.org/licenses/gpl-2.0.txt)
 */

include "includes/common.inc";
// connect to database first, for mysql_real_escape_string
connectToDatabase();

$all=explode_url($_SERVER['REQUEST_URI']);
$args=$all["query_params"];

$key=$args["key"];
$tag=$args["tag"];
$lat=$args["lat"];
$lon=$args["long"];
$err=$args["err"];

echo "
<!DOCTYPE html>
<html>
<head>
<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\"/>
<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>
<title>GiPS geocodifica punto</title>
<link href=\"http://code.google.com/apis/maps/documentation/javascript/examples/standard.css\" rel=\"stylesheet\" type=\"text/css\" />
<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false\"></script>
<script type=\"text/javascript\">
  var geocoder;
  var map;
  var infowindow = new google.maps.InfoWindow();
  var marker;
// result div
  var info;

  function initialize() {
    geocoder = new google.maps.Geocoder();
    var latlng = new google.maps.LatLng($lat,$lon);
    var mapOptions = {
      zoom: 17,
      center: latlng,
      mapTypeId: 'roadmap',
      streetViewControl: true
    }
    map = new google.maps.Map(document.getElementById(\"map_canvas\"), mapOptions);
  streetViewControl: true
    geocoder.geocode({'latLng': latlng}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (results[1]) {
          map.setZoom(17);
          marker = new google.maps.Marker({
              position: latlng, 
              map: map,
              clickable: true,
              icon: 'images/singleblue.png'
          }); 
       // Create a draggable marker which will later on be binded to a
       // Circle overlay.
       // var marker = new google.maps.Marker({
       // map: map,
       // position: new google.maps.LatLng($lat,$lon),
       // draggable: true,
       // title: 'Drag me!'
       // });

        // Add a Circle overlay to the map.
        var circle = new google.maps.Circle({
          map: map,
          strokeWeight: 1,
          radius: $err //meters
        });

        // Since Circle and Marker both extend MVCObject, you can bind them
        // together using MVCObject's bindTo() method.  Here, we're binding
        // the Circle's center to the Marker's position.
        // http://code.google.com/apis/maps/documentation/v3/reference.html#MVCObject
        circle.bindTo('center', marker, 'position');

    var contentString = '<div id=\"content\">'+
//        '<div id=\"siteNotice\">'+
//        '</div>'+
//        '<h2 id=\"firstHeading\" class=\"firstHeading\">$key</h2>'+
        '<div id=\"bodyContent\">'+
        '<p><div>Campagna: $key </div>'+
        'Operatore: $tag </p><p>'+
        (results[0].formatted_address)
//        'Attribution: Uluru, <a href=\"http://en.wikipedia.org/w/index.php?title=Uluru&oldid=297882194\">'+
//        'http://en.wikipedia.org/w/index.php?title=Uluru</a> '+
//        '(last visited June 22, 2009).</p>'+
        '</div>';
        
    var infowindow = new google.maps.InfoWindow({
        content: contentString,
        maxWidth: 200
    });
          infowindow.open(map, marker);
        } else {
          alert(\"Nessun indirizzo rintracciato.\");
        }
      } else {
        alert(\"Geocoder failed due to: \" + status);
      }
    });
  }
</script>
</head>
<body onload=\"initialize()\">
  <div id=\"map_canvas\" style=\"height: 100%; border: none;\"></div>
  <div style=\"position:absolute;top:340px;left:10px;width:159px; height: 62px\"><a href=\"http://www.gipsin.it\" target=\"_blank\"><img src=\"/images/logo.png\" width=\"159\" height=\"62\" border=\"0\" alt=\"GiPS logo\" /></a></div>
</body>
</html>
";
