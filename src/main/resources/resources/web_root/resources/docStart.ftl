<!DOCTYPE html>
<#setting number_format="0">
<html class="no-js">
    <head>
        <title>Inquisitor</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" media="all" href="../css/reset.css" />
        <link rel="stylesheet" type="text/css" media="all" href="../css/text.css" />
        <link rel="stylesheet" type="text/css" media="all" href="../css/960.css" />
        <link rel="stylesheet" type="text/css" media="all" href="../css/jquery/jquery.css" />
        <link rel="stylesheet" type="text/css" media="all" href="../css/style.css" />
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
        <script src="../js/modernizr.js"></script>
        <script src="../js/Three.js"></script>
        <script src="../js/PlayerSkin.js"></script>
    </head>
    <body>
        <div class="background">
        <div class="container_16">

        <div id="topNavigation" class="grid_10">
            <ul>
                <li><a href="../players/">Players</a></li>
            </ul>
        </div>

        <div id="playerSearch" class="grid_6">
            <form method="GET" action="../playerSearch/">
                Player Search: <input id="playerSearchInput" type="text" name="playerName" value="${playerName!}" placeholder="Player Name" autocomplete="off"/>
            </form>
            <div id="playerSearchSuggestions"></div>
        </div>
        <div class="clear"></div>
