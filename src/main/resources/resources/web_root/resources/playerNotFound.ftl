<#escape x as x?html>
<#include "/resources/docStart.ftl">

<div class="grid_16">

    <h1>Player not found</h1>
    <#if playerName??>
        <p>
            I'm sorry, but no players matching "${playerName}" could be found.
        </p>
    <#else>
        <p>
            No player name was specified.
        </p>
    </#if>

</div>

<#include "/resources/docEnd.ftl">
</#escape>
