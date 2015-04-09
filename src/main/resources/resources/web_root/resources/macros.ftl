<#macro "distance" distance>
    <#if (distance < 1000)>
        ${distance?round} m
    <#else>
        ${(((distance / 100)?round)/10)?string} km
    </#if>
</#macro>

<#macro "elapsedTime" time>
    <#local time=time?floor>
    <#if (time < 60)>
        ${time} sec
        <#return>
    </#if>
    <#local mins=(time / 60)?floor>
    <#local secs=time - (60 * mins)>
    <#if (mins < 60)>
        ${mins} min, ${secs} sec
        <#return>
    </#if>
    <#local hours=(mins / 60)?floor>
    <#local mins=mins - (hours * 60)>
    <#if (hours < 24)>
        ${hours} hrs, ${mins} min
        <#return>
    </#if>
    <#local days=(hours / 24)?floor>
    <#local hours=hours - (days * 24)>
    <#if (days < 7)>
        ${days} days, ${hours} hrs, ${mins} min
        <#return>
    </#if>
    <#local weeks=(days / 7)?floor>
    <#local days=days - (weeks * 7)>
    <#if (weeks < 52)>
        ${weeks} wks, ${days} days, ${hours} hrs
        <#return>
    </#if>
    <#local years=(weeks / 52)?floor>
    <#local weeks=weeks - (years * 52)>
    ${years} yrs, ${weeks} wks, ${days} days
</#macro>

<#macro "coords" coords>
    <#if coords??>
        <#local res = coords?matches("^(-?\\d+).*,(-?\\d+).*,(-?\\d+)")>
        ${res[0]?groups[1]}, ${res[0]?groups[2]}, ${res[0]?groups[3]}
    <#else>
        None
    </#if>
</#macro>

<#macro "iconMeter" baseSrc level>
    <#local full=(level / 2)?floor>
    <#local empty=((20 - level) / 2)?floor>
    <div class="iconMeter">
        <#if (full > 0)>
            <#list 1..full as i>
                <img src="../img/${baseSrc}-full.png"/>
            </#list>
        </#if>
        <#if ((full + empty) < 10)>
            <img src="../img/${baseSrc}-half.png"/>
        </#if>
        <#if (empty > 0)>
            <#list 1..empty as i>
                <img src="../img/${baseSrc}-empty.png"/>
            </#list>
        </#if>
    </div>
</#macro>
