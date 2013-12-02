<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDuplicate" localMode="modern">
 		<cfapplication action="update" clientmanagement="true">
<!--- begin old test code --->
<!--- String --->
	<cfset str="String">
	<cfset str2=duplicate(str)>
	<cfset str="String 2">
	<cfset valueEquals(left="#str2#", right="String")>
<!--- Number --->
	<cfset str=1+1>
	<cfset str2=duplicate(str)>
	<cfset str=str+1>
	<cfset valueEquals(left="#str2#", right="2")>
<!--- boolean --->
	<cfset str=true>
	<cfset str2=duplicate(str)>
	<cfset str=false>
	<cfset valueEquals(left="#str2#", right="true")>
<!--- struct --->
	<cfset str=structNew()>
	<cfset str.data="aaaaa">
	<cfset str2=duplicate(str)>
	<cfset str.data="bbbbb">
	<cfset valueEquals(left="#str2.data#", right="aaaaa")>
<!--- array --->
	<cfset str=arrayNew(1)>
	<cfset str[1]="aaaaa">
	<cfset str2=duplicate(str)>
	<cfset str[1]="bbbbb">
	<cfset valueEquals(left="#str2[1]#", right="aaaaa")>
<!--- query --->
	<cfset qry=queryNew("col")>
	<cfset QueryAddRow(qry)>
	<cfset QuerySetCell(qry,"col","aaaaa")>
	<cfset qry2=duplicate(qry)>
	<cfset QuerySetCell(qry,"col","bbbbb")>
	<cfset valueEquals(left="#qry2.col#", right="aaaaa")>

<cfif server.ColdFusion.ProductName eq "RAILO">
<cfobject type="component" name="c" component="duplicate.comps.some.Hello">
<cfset valueEquals(left="#c.get()#", right="0")>

<cfset d=duplicate(c)>
<cfset c.set(1)>


<cfset valueEquals(left="#c.get()#", right="1")>
<cfset valueEquals(left="#d.get()#", right="0")>
</cfif>


<cfset duplicate(client)>
<cfset duplicate(session)>
<cfset duplicate(application)>
<cfset duplicate(request)>
<cfset duplicate(cgi)>
<cfset duplicate(variables)>
<cfset duplicate(server)>


<cfsavecontent variable="xrds">
	<?xml version="1.0" encoding="UTF-8"?>
	<xrd>
		<Service priority="10">
			<Type>http://openid.net/signon/1.0</Type>
			<URI priority="15">http://resolve2.example.com</URI>
			<URI priority="10">http://resolve.example.com</URI>
			<URI>https://resolve.example.com</URI>
		</Service>
	</xrd>
</cfsavecontent>
<cfset xrds = xmlParse(trim(xrds)).xmlRoot />
<cfset xrdsService = xrds.xmlChildren[1] />
<cfset xrdsService.xmlChildren[2] = duplicate(xrdsService.URI[2]) />
<cfset xrdsService.URI[1] = duplicate(xrdsService.URI[2]) />

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>