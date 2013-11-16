<cfscript>
component extends="org.railo.cfml.test.RailoTestCase"	{
	

	public void function testArrayAppend(){
		var arr=arrayNew(1);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		assertEquals(3,arrayLen(arr));

		arr=arrayNew(1);
		arr[1]=1;
		arr[7]=7;
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		
		assertEquals(10,arrayLen(arr));

		arr=arrayNew(1);
		ArrayResize(arr, 20);
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		assertEquals(23,arrayLen(arr));
		

		arr=arrayNew(2);
		ArrayAppend(arr, arrayNew(1));
		try{
			ArrayAppend(arr, 1);
			fail("must throw:Array dimension error");
		}
		catch(local.exp){}
		
		var sct=structNew();
		sct.aaa=1;
		
		try{
			ArrayAppend( sct,"value" );
			fail("must throw:cant cast struct to a array, key [aaa] is not a number");
		}
		catch(local.exp){}
		
		arr=createObject('java','java.util.LinkedList').init();
		ArrayAppend( arr, 1 );
		ArrayAppend( arr, 2 );
		ArrayAppend( arr, 3 );
		assertEquals(3,arrayLen(arr));
		
		arr=arr.toArray();
		arr=[1];
		ArrayAppend( arr, [1,2,3] );
		assertEquals(2,arrayLen(arr));
		assertEquals(true,isArray(arr[2]));
		assertEquals(3,arrayLen(arr[2]));
		
		arr=[1];
		ArrayAppend( arr, [4,5,6] ,true);
		assertEquals(4,arrayLen(arr));
		assertEquals(false,isArray(arr[2]));
		assertEquals("1,4,5,6",arrayToList(arr));
		
    	arr=[1];
    	ArrayAppend( arr, 2 ,true);
		assertEquals(2,arrayLen(arr));
		assertEquals(false,isArray(arr[2]));
		assertEquals("1,2",arrayToList(arr));
    	
    	arr=[1];
    	el=[2];
    	el[5]=5;
    	ArrayAppend( arr, el ,true);
		assertEquals(6,arrayLen(arr));
		assertEquals(false,isArray(arr[2]));
		assertEquals("1,2,,,,5",arrayToList(arr));
		
		
		
		/*assertEquals("","");
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
} 
</cfscript>