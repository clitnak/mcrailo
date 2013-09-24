<cfscript>
component extends="org.railo.cfml.test.RailoTestCase"	{

	public function beforeTests(){
		variables.dest=getDirectoryFromPath(getCurrenttemplatepath())&"Jira2614/downloads";
		if(fileExists(dest)) fileDelete(dest);
		if(directoryexists(dest)) directorydelete(dest,true);
		
		http method="post" result="result" url="#createURL("Jira2614/index.cfm")#" addtoken="false"  multipart="true"{
			httpparam type="file" name="file" file="#getCurrentTemplatePath()#";
		}
	}

	public function afterTests(){
		if(fileExists(dest)) fileDelete(dest);
		if(directoryexists(dest)) directorydelete(dest,true);
		
	}

	public void function testFileContent(){
		assertEquals("",trim(result.filecontent));
	}

	public void function testDestinationFile(){
		assertEquals(false,fileExists(dest));
		assertEquals(true,directoryexists(dest));
		assertEquals(true,fileExists(dest&"/"&listLast(getCurrenttemplatepath(),'\/')));
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>