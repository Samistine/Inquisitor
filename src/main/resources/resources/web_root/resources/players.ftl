<#escape x as x?html>
<#include "/resources/docStart.ftl">

<#import "/resources/macros.ftl" as macros>

<#assign columnDefs = {
    'position': {
        'header': 'Position',
        'cellClass': 'number',
        'format': r'${player_index + firstPlayerOffset + 1}.',
        'sortable': false
        },
    'name': {
        'header': 'Name',
        'cellClass': 'name',
        'link': r'"../player/${player.name?url}"',
        'linkTitle': 'View Player',
        'format': r'<div class="face" style="background-image:url(../skin/${player.name})"></div><div class="name">${player.name}</div>'
        },
    'displayName' : {'header': 'Display Name', 'cellClass': 'number'},
    'address' : {'header': 'Address', 'cellClass': 'number'},
    'level' : {'header': 'Level', 'cellClass': 'number'},
    'deaths' : {'header': 'Deaths', 'cellClass': 'number'},
    'heldItemSlot' : {'header': 'Held Item Slot', 'cellClass': 'number'},
    'health' : {
        'header': 'Health',
        'cellClass': 'nowrap',
        'format': r'<@macros.iconMeter baseSrc="heart" level=player.health/>'
    },
    'remainingAir' : {'header': 'Air', 'cellClass': 'number'},
    'fireTicks' : {'header': 'Fire Ticks', 'cellClass': 'number'},
    'foodLevel' : {
        'header': 'Food',
        'cellClass': 'nowrap',
        'format': r'<@macros.iconMeter baseSrc="food" level=player.foodLevel/>'
    },
    'exhaustion' : {'header': 'Exhaustion', 'cellClass': 'number'},
    'saturation' : {'header': 'Saturation', 'cellClass': 'number'},
    'gameMode' : {'header': 'Game Mode', 'cellClass': 'number'},
    'exp' : {'header': 'XP', 'cellClass': 'number'},
    'totalExperience' : {'header': 'Total XP', 'cellClass': 'number'},

    'server' : {'header': 'Server', 'cellClass': 'number'},
    'world' : {'header': 'World', 'cellClass': 'number'},
    'coords' : {'header': 'Coords', 'cellClass': 'number', 'format': r'<@macros.coords coords=player.coords/>'},
    'bedServer' : {'header': 'Bed Server', 'cellClass': 'number'},
    'bedWorld' : {'header': 'Bed World', 'cellClass': 'number'},
    'bedCoords' : {'header': 'Bed Coords', 'cellClass': 'number', 'format': r'<@macros.coords coords=player.bedCoords/>'},

    'joins' : {'header': 'Joins', 'cellClass': 'number'},
    'firstJoin' : {'header': 'First Joined', 'cellClass': 'number', 'format': r'${(player.firstJoin?datetime?string.short)!"Never"}'},
    'lastJoin' : {'header': 'Last Joined', 'cellClass': 'number', 'format': r'${(player.lastJoin?datetime?string.short)!"Never"}'},
    'quits' : {'header': 'Quits', 'cellClass': 'number'},
    'lastQuit' : {'header': 'Last Quit', 'cellClass': 'number', 'format': r'${(player.lastQuit?datetime?string.short)!"Never"}'},
    'kicks' : {'header': 'Kicks', 'cellClass': 'number'},
    'lastKick' : {'header': 'Last Kick', 'cellClass': 'number', 'format': r'${(player.lastKick?datetime?string.short)!"Never"}'},
    'lastKickMessage' : {'header': 'Last Kick Message', 'cellClass': 'number'},
    'lastDeath' : {'header': 'Last Death', 'cellClass': 'number', 'format': r'${(player.lastDeath?datetime?string.short)!"Never"}'},
    'lastDeathMessage' : {'header': 'Last Death Message', 'cellClass': 'number'},

    'totalPlayersKilled' : {'header': 'Players Killed', 'cellClass': 'number'},
    'lastPlayerKill' : {'header': 'Last Player Kill', 'cellClass': 'number', 'format': r'${(player.lastPlayerKill?datetime?string.short)!"Never"}'},
    'lastPlayerKilled' : {'header': 'Last Player Killed', 'cellClass': 'number'},
    'totalMobsKilled' : {'header': 'Mobs Killed', 'cellClass': 'number'},
    'lastMobKill' : {'header': 'Last Mob Kill', 'cellClass': 'number', 'format': r'${(player.lastMobKill?datetime?string.short)!"Never"}'},
    'lastMobKilled' : {'header': 'Last Mob Killed', 'cellClass': 'number'},
    'totalBlocksBroken' : {'header': 'Blocks Broken', 'cellClass': 'number'},
    'totalBlocksPlaced' : {'header': 'Blocks Placed', 'cellClass': 'number'},
    'totalItemsDropped' : {'header': 'Items Dropped', 'cellClass': 'number'},
    'totalItemsPickedUp' : {'header': 'Items Picked Up', 'cellClass': 'number'},
    'totalItemsCrafted' : {'header': 'Items Crafted', 'cellClass': 'number'},
    'totalDistanceTraveled' : {'header': 'Distance Traveled', 'cellClass': 'number nowrap', 'format': r'<@macros.distance distance=player.totalDistanceTraveled/>'},
    'totalTime' : {'header': 'Total Time Played', 'cellClass': 'number nowrap', 'format': r'<@macros.elapsedTime time=player.totalTime/>'},
    'sessionTime' : {'header': 'Time Played', 'cellClass': 'number nowrap', 'format': r'<@macros.elapsedTime time=player.sessionTime/>'},
    'online' : {'header': 'Online', 'format': r'${player.online?string("Yes", "No")}'},

    'timesSlept' : {'header': 'Times Slept', 'cellClass': 'number'},
    'arrowsShot' : {'header': 'Arrows Shot', 'cellClass': 'number'},
    'firesStarted' : {'header': 'Fires Started', 'cellClass': 'number'},
    'fishCaught' : {'header': 'Fish Caught', 'cellClass': 'number'},
    'sheepSheared' : {'header': 'Sheep Sheared', 'cellClass': 'number'},
    'chatMessages' : {'header': 'Chat Messages', 'cellClass': 'number'},
    'portalsCrossed' : {'header': 'Portals Crossed', 'cellClass': 'number'},
    'waterBucketsFilled' : {'header': 'Water Buckets Filled', 'cellClass': 'number'},
    'waterBucketsEmptied' : {'header': 'Water Bucket Emptied', 'cellClass': 'number'},
    'lavaBucketsFilled' : {'header': 'Lava Buckets Filled', 'cellClass': 'number'},
    'lavaBucketsEmptied' : {'header': 'Lava Buckets Emptied', 'cellClass': 'number'},
    'cowsMilked' : {'header': 'Cows Milked', 'cellClass': 'number'},
    'mooshroomsMilked' : {'header': 'Mooshrooms Milked', 'cellClass': 'number'},
    'mooshroomsSheared' : {'header': 'Mooshrooms Sheared', 'cellClass': 'number'},
    'sheepDyed' : {'header': 'Sheep Dyed', 'cellClass': 'number'},
    'lifetimeExperience' : {'header': 'Lifetime XP', 'cellClass': 'number'},
    'itemsEnchanted' : {'header': 'Items Enchanted', 'cellClass': 'number'},
    'itemEnchantmentLevels' : {'header': 'Enchantment Levels', 'cellClass': 'number'},
    'money' : {'header': 'Money', 'cellClass': 'number', 'format': r'${player.money?string("0.00")!"0"}'}

}>

<#macro columnList>
<#list showColumns as column>${column}<#if column_has_next>,</#if></#list>
</#macro>

<#macro sortURL column dir>
?columns=<@columnList/>&sortBy=${column}&sortDir=${dir}&pageSize=${pageSize}&page=${page}<#if playerName??>&playerName=${playerName}</#if>
</#macro>

<#macro pageURL page>
?columns=<@columnList/>&sortBy=${sortBy}&sortDir=${sortDir}&pageSize=${pageSize}&page=${page}<#if playerName??>&playerName=${playerName}</#if>
</#macro>

<#macro paginator>
    <div class="paginator">
        Page:
        <div class="previous <#if (page == 1)>disabled</#if>" title="Previous page">
            <#if (page > 1)>
                <a href="<@pageURL page=(page - 1)/>">&nbsp;</a>
            </#if>
        </div>
        <div class="next <#if (page == totalPages)>disabled</#if>" title="Next page">
            <#if (page < totalPages)>
                <a href="<@pageURL page=(page + 1)/>">&nbsp;</a>
            </#if>
        </div>
        <div class="prePages">[</div>
        <div class="pages">
            <#list 1..totalPages as paginatorPage>
                <div class="page">
                    <#if page != paginatorPage>
                        <a href="<@pageURL page=paginatorPage/>" title="Go to page ${paginatorPage}">
                    </#if>
                    ${paginatorPage}
                    <#if page != paginatorPage>
                        </a>
                    </#if>
                </div>
            </#list>
        </div>
        <div class="postPages">]</div>
    </div>
</#macro>

<div id="players" class="grid_16">

<h1>Players</h1>

<#if (totalPlayers == 0)>
    <div id="noPlayers">
        <#if playerName??>
            There are no players that match "${playerName}".
        <#else>
            There are no players.
        </#if>
    </div>
<#else>

    <p>
        <#if playerName??>
            Total players matching "${playerName}": ${totalPlayers}
        <#else>
            Total players: ${totalPlayers}
        </#if>
    </p>

    <#if (totalPages > 1)>
        <@paginator/>
    </#if>

    <button id="columnChooserButton">Columns</button>
    <div class="clear"></div>

    <table class="players">
        <thead>
            <tr>
                <#list ['position', 'name'] + showColumns as column>
                    <#if columnDefs[column]?has_content>
                        <#assign header=columnDefs[column]>
                    <#else>
                        <#assign header={'header': column}>
                    </#if>
                    <#assign class="">
                    <#assign sortable=false>
                    <#if (! header.sortable?has_content) || header.sortable>
                        <#assign sortable=true>
                        <#assign dir="DESC">
                        <#if column == sortBy>
                            <#assign class="sorted${sortDir}">
                            <#if sortDir == "DESC">
                                <#assign dir="ASC">
                            </#if>
                        </#if>
                    </#if>
                    <th class="${class}">
                        <#if sortable>
                            <a href="<@sortURL column=column dir=dir/>" title="Sort by ${header.header}">
                        </#if>
                        ${header.header}
                        <#if sortable>
                            </a>
                        </#if>
                    </th>
                </#list>
            </tr>
        </thead>
        <tbody>
            <#list players as player>
                <tr>
                    <#list ['position', 'name'] + showColumns as column>
                        <#if columnDefs[column]??>
                            <#assign def=columnDefs[column]>
                            <td class="${def.cellClass!""} <#if column == sortBy>sorted</#if>">
                                <#if def.link??>
                                    <a href="${def.link?eval}" title="${def.linkTitle!""}">
                                </#if>
                                <#if def.format??>
                                    <#assign format=[def.format, "format"]?interpret>
                                    <@format/>
                                <#else>
                                    ${player[column]!""}
                                </#if>
                                <#if def.link??>
                                    </a>
                                </#if>
                            </td>
                        <#else>
                            <td>
                                ${player[column]!""}
                            </td>
                        </#if>
                    </#list>
                </tr>
            </#list>
        </tbody>
    </table>

    <#if (totalPages > 1)>
        <@paginator/>
    </#if>

</#if>

</div> <!-- players -->
<div class="clear spacer"></div>

<div id="columnChooser" title="Choose Columns">
    <p>
        Select which columns to display. Drag and drop items to change their order.
    </p>
    <ul>
    <#list showColumns + hideColumns?sort as column>
        <#if columnDefs[column]??>
            <#assign columnDef=columnDefs[column]>
        <#else>
            <#assign columnDef={'header': column}>
        </#if>
        <li>
            <input id="col-${column}" type="checkbox" name="${column}" <#if showColumns?seq_contains(column)>checked</#if>/>
            <label for="col-${column}">${columnDef.header}</label>
        </li>
    </#list>
</div>

<script type="text/javascript">
    $(document).ready(function(){
        $('#columnChooserButton').button();
        $('#columnChooserButton').click(function(event) {
            $('#columnChooser').dialog('open');
        });
        $('#columnChooser').dialog({
            autoOpen: false,
            modal: true,
            resizable: true,
            width: 800,
            buttons: [{
                text: 'OK',
                click: function() {
                    var cols = [];
                    $('#columnChooser input:checkbox:checked').each(function() {
                        cols.push($(this).attr('name'));
                    });
                    var url = '?columns=' + cols.join(',') +
                        '&sortBy=${sortBy}' +
                        '&sortDir=${sortDir}' +
                        '&pageSize=${pageSize}' +
                        '&page=${page}';
                    <#if playerName??>url += '&playerName=${playerName}'</#if>;
                    window.location = url;
                }
            }, {
                text: 'Cancel',
                click: function() { $(this).dialog("close"); }
            }]
        });
        $('#columnChooser ul').sortable();
    });
</script>

<#include "/resources/docEnd.ftl">
</#escape>

