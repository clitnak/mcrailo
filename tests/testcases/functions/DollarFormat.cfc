<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testDollarFormat" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#DollarFormat("")#", right="$0.00")>
<cfset valueEquals(left="#DollarFormat("1")#", right="$1.00")>
<cfset valueEquals(left="#DollarFormat("1.3333333")#", right="$1.33")>
<cfset valueEquals(left="#DollarFormat("123.46")#", right="$123.46")>
<cfset valueEquals(left="#DollarFormat("1.999999")#", right="$2.00")>
<cfset valueEquals(left="#DollarFormat("1.774")#", right="$1.77")>
<cfset valueEquals(left="#DollarFormat("1.775")#", right="$1.78")>
<cftry>
	<cfset valueEquals(left="#DollarFormat("one Dollar")#", right="$1.00")>
	<cfset fail("must throw:invalid call of the function dollarFormat, first Argument (number) is invalid, Cant cast String [one Dollar] to a number")>
	<cfcatch></cfcatch>
</cftry>

<cfset org=GetLocale()>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('english (us)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('english (uk)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>
<cfset setLocale('german (swiss)')>
<cfset valueEquals(left="#DollarFormat(200000)#", right="$200,000.00")>

<cfset setLocale(org)>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>