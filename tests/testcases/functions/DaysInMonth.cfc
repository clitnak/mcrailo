<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDaysInMonth" localMode="modern">

<!--- begin old test code --->
<cfset d1=CreateDateTime(2001, 12, 1, 4, 10, 1)> 
<cfset valueEquals(left="#daysInMonth(d1)#", right="31")>
	
<!--- all month of a year --->
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  1, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  2, 1, 0, 0, 0))#", right="28")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  3, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  4, 1, 0, 0, 0))#", right="30")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  5, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  6, 1, 0, 0, 0))#", right="30")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  7, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  8, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001,  9, 1, 0, 0, 0))#", right="30")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001, 10, 1, 0, 0, 0))#", right="31")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001, 11, 1, 0, 0, 0))#", right="30")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2001, 12, 1, 0, 0, 0))#", right="31")>

<!--- leap year --->
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2004, 2, 1, 0, 0, 0))#", right="29")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(2000, 2, 1, 0, 0, 0))#", right="29")>
<cfset valueEquals(left="#DaysInMonth(CreateDateTime(1900, 2, 1, 0, 0, 0))#", right="28")>

<!--- numeric date --->
<cfset valueEquals(left="#DaysInMonth(1)#", right="31")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>