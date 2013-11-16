<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testArrayMerge">

<!--- begin old test code --->
<cfif server.ColdFusion.ProductName EQ "railo">
<cfset var arr1=arrayNew(1)>
<cfset ArrayAppend( arr1, 1 )>
<cfset ArrayAppend( arr1, 2 )>
<cfset ArrayAppend( arr1, 3 )>

<cfset var arr2=arrayNew(1)>
<cfset ArrayAppend( arr2, 4 )>
<cfset ArrayAppend( arr2, 5 )>
<cfset ArrayAppend( arr2, 6 )>

<cfset var arr=arrayMerge(arr1,arr2)>
<cfset valueEquals(left="#arrayLen(arr)#", right="6")>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="2")>
<cfset valueEquals(left="#arr[3]#", right="3")>
<cfset valueEquals(left="#arr[4]#", right="4")>
<cfset valueEquals(left="#arr[5]#", right="5")>
<cfset valueEquals(left="#arr[6]#", right="6")>


<cfset arr=arrayMerge(arr1,arr2,true)>
<cfset valueEquals(left="#arrayLen(arr)#", right="3")>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="2")>
<cfset valueEquals(left="#arr[3]#", right="3")>

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