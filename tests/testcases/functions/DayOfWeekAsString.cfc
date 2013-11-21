<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDayOfWeekAsString" localMode="modern">

<!--- begin old test code --->
<cfset org=getLocale()>
<cfset setLocale("German (Swiss)")>
<!--- <cfdump var="#getLocale()#"> --->
<cfset valueEquals(left="#DayOfWeekAsString(1)#", right="Sonntag")> 
<cfset valueEquals(left="#DayOfWeekAsString(2)#", right="Montag")> 
<cfset valueEquals(left="#DayOfWeekAsString(3)#", right="Dienstag")> 
<cfset valueEquals(left="#DayOfWeekAsString(4)#", right="Mittwoch")> 
<cfset valueEquals(left="#DayOfWeekAsString(5)#", right="Donnerstag")> 
<cfset valueEquals(left="#DayOfWeekAsString(6)#", right="Freitag")> 
<cfset valueEquals(left="#DayOfWeekAsString(7)#", right="Samstag")> 

<cftry> 
        <cfset valueEquals(left="#DayOfWeekAsString(0)#", right="Invalid DayOfWeek")> 
        <cfset fail("must throw:0.0 must be within range: ( 1 : 7 ) ")> 
        <cfcatch></cfcatch> 
</cftry> 
<cftry> 
        <cfset valueEquals(left="#DayOfWeekAsString(8)#", right="Invalid DayOfWeek")> 
        <cfset fail("must throw:0.0 must be within range: ( 1 : 7 ) ")> 
        <cfcatch></cfcatch> 
</cftry> 


<cfset setLocale("English (US)")>
<!--- <cfdump var="#getLocale()#"> --->
<cfset valueEquals(left="#DayOfWeekAsString(1)#", right="Sunday")> 
<cfset valueEquals(left="#DayOfWeekAsString(2)#", right="Monday")> 
<cfset valueEquals(left="#DayOfWeekAsString(3)#", right="Tuesday")> 
<cfset valueEquals(left="#DayOfWeekAsString(4)#", right="Wednesday")> 
<cfset valueEquals(left="#DayOfWeekAsString(5)#", right="Thursday")> 
<cfset valueEquals(left="#DayOfWeekAsString(6)#", right="Friday")> 
<cfset valueEquals(left="#DayOfWeekAsString(7)#", right="Saturday")> 

<cftry> 
        <cfset valueEquals(left="#DayOfWeekAsString(0)#", right="Invalid DayOfWeek")> 
        <cfset fail("must throw:0.0 must be within range: ( 1 : 7 ) ")> 
        <cfcatch></cfcatch> 
</cftry> 
<cftry> 
        <cfset valueEquals(left="#DayOfWeekAsString(8)#", right="Invalid DayOfWeek")> 
        <cfset fail("must throw:0.0 must be within range: ( 1 : 7 ) ")> 
        <cfcatch></cfcatch> 
</cftry> 

<cfset setLocale(org)>

<cfif left(server.railo.version,1) GTE 3>
	<cfset valueEquals(left="#evaluate('DayOfWeekAsString(7,"de")')#", right="Samstag")> 
	<cfset valueEquals(left="#evaluate('DayOfWeekAsString(7,"en")')#", right="Saturday")> 
</cfif>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>