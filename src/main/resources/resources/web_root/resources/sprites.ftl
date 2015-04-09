<#include "spriteData.ftl">

<#macro "sprite" id class="">
    <#local w=spritesMeta.width>
    <#local h=spritesMeta.height>
    <#if ! sprites[id]?has_content>
        <div class="sprite empty ${class}" style="width:${spritesMeta.width}px;height:${spritesMeta.height}px;"></div>
    <#else>
        <#local info=sprites[id]>
        <#local x=-(spritesMeta.width * info[0])>
        <#local y=-(spritesMeta.height * info[1])>
        <div class="sprite ${class}" style="width:${spritesMeta.width}px;height:${spritesMeta.height}px;background-image:url(../img/sprites.png);background-position:${x}px ${y}px;"></div>
    </#if>
</#macro>
