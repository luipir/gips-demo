<?php

// set the time zone to be used by all others
// maybe this is a bad idea, so I'm not doing this:
// date_default_timezone_set("Europe/London");

// Place general purpose methods here, so they can easily be used in serveral files.
// Sort alphabetically please.

// Check if a parameter only contains dash, 0-9 and dot
function checkNumericParameter($parameter) {
	if (!isset($parameter) || ereg('^[-0-9\.]+$',$parameter)) {
		return $parameter;
	} else {
   		die("Parameter '" . $parameter . "' contains illegal character(s)!");
	}
}

// Check if a parameter only contains: digits,-,+,:
// As the ISO 8601 format, for example: 2010-05-06T15:19:21+01:00 
function checkDateTimeParameter($parameter) {
	if (!isset($parameter) || ereg('^[-+:0-9tTZ.]+$',$parameter)) {
		return $parameter;
	} else {
		$error="Parameter '" . $parameter . "' contains illegal character(s)";
		header('HTTP/1.0 400 Bad Request ' . $error);
		echo($error);
	}
}

// Check if a parameter is an innocent text string (for logPos.php)
function checkPresence($parameter) {
	if (!isset($parameter) || $parameter == "") {
		die("Errore: i parametri \"campagna\", \"operatore\", \"latitudine\" e \"longitudine\" sono obbligatori.");
		mysql_close();
	} else {
		return $parameter;
	}	
}

// Check if a parameter is an innocent text string (for logManyPos.php)
function checkPresenceMany($parameter) {
	if (!isset($parameter) || $parameter == "") {
		$error='key, tag, ndata, time{1..ndata}, latitude{1..ndata} and longitude{1..ndata} are required parameters';
		header('HTTP/1.0 400 Bad Request ' . $error);
		echo($error);
		mysql_close();
	} else {
		return $parameter;
	}	
}

function connectToDatabase() {
	$db=parse_ini_file("settings.ini");
	$dbuser=$db["user"];
	$dbpassword=$db["password"];
	$dbdatabase=$db["database"];
	$dbhost=$db["host"];
	$conn=mysql_connect($dbhost,$dbuser,$dbpassword);
	mysql_select_db($dbdatabase,$conn);
}	

function explode_url($sUrl) {
            $aUrl = parse_url($sUrl);
            $aUrl['query_params'] = array();
            $aPairs = explode('&', $aUrl['query']);
            foreach($aPairs as $sPair) {
                if (trim($sPair) == '') { continue; }
                list($sKey, $sValue) = explode('=', $sPair);
                $aUrl['query_params'][$sKey] = urldecode($sValue);
            }
            return $aUrl;
}

function nullfill($str)
{
    if (is_null($str))
    {
        return "";
    }
    else
    {
        return $str;
    }
}
