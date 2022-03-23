import com.softwareag.jira.exalate.helper.Helper
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface
import java.sql.Connection
//import com.softwareag.jira.exalate.Helper.*
import groovy.xml.MarkupBuilder
import org.apache.log4j.Logger;
import groovy.json.JsonSlurper


def writer = new StringWriter()
def xml = new MarkupBuilder(writer)


def log = Logger.getLogger("com.acme.CreateSubtask")
def productCodes = getProductCodeList()

log.debug("MOUNA2 "+productCodes)
log.setLevel(Level.DEBUG)
//String getProjectKey(String recipient, String productCode, String platform)
def file = new File("D:\\itrac-shared-home\\scripts\\com\\output.groovy")


xml.style(type:"text/css",
    '''
         #scriptField, #scriptField *{
                border: 1px solid black;
            }

            #scriptField{
                border-collapse: collapse;
            }
        ''')


def recipients   = ["Development", "CloudOps"]
def platforms    = ["ZS","PC","MR","MF","UX"]
def projectKeys  = []

def all=""
def line ="ProductCode,"


    
   
  
recipients.each{recipient ->
    platforms.each{platform ->

       line=line+recipient+"-"+platform+","
    }
}
line= line.subSequence(0, line.length() - 1)
all=all+line


def count=0

xml.table(id:"scriptField"){
    tr{
        th("ProductCode")
        th("Development-ZS")
        th("Development-PC")
        th("Development-MR")
        th("Development-MF")
        th("Development-UX")
        th("CloudOps-ZS")
        th("CloudOps-PC")
        th("CloudOps-MR")
        th("CloudOps-MF")
        th("CloudOps-UX")
    }
    

   
productCodes.each{productCode ->
    count=count+1
    line=productCode+","
   
    if(count<=100){
        
        tr{
             td(productCode)
    recipients.each{recipient ->
     
       
        platforms.each{platform ->
		        def entry=""
            //def entry2=Helper.getProjectKey("Development", "AAA", "ZS") 
            
                try{
                   // log.debug("PLATFORM "+platform+" recipient "+ recipient +" productCode "+ productCode+" key: "+Helper.getProjectKey(recipient, productCode, platform) )

                     entry=getProjectKey(recipient, productCode, platform)

                     line=line+entry+","   
                    
					 
            		
                }
           
                catch(Exception e){
                    line=line+","
                }   
            	 td(entry)

            }

        }
    		line= line.subSequence(0, line.length() - 1)
          
           line=line+"\n"
           all=all+line
    }
        }

}
}

    


file.write(all)
  


return (writer.toString())  



def getProductCodeList() {
    
    def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
    String helperName = delegator.getGroupHelperName("default")
	
    def sqlStmt = """
        select CODE, NAME, FIX_ALIAS, PRODUCT_GROUP
        from iTrac_ppd_products
    """
	
    Connection conn = ConnectionFactory.getConnection(helperName)
    Sql sql = new Sql(conn)
    def productCodeList = []
    try {
        StringBuffer sb = new StringBuffer()
        sql.eachRow(sqlStmt) { row ->
            productCodeList.add(row.getAt("CODE"))
        }
    }
    finally {
        sql.close()
    }
    return productCodeList
}
import org.apache.log4j.Logger

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import groovy.json.JsonSlurper
public static String getProjectKey(String recipient, String productCode, String platform) {
        // ***************************************************
        // HTTP request handling - do not validate certificate
        // ***************************************************

        // Create a trust manager that does not validate certificate chains
       
        TrustManager[] trustAllCerts = [ new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        ];

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        // ***************************************************
        // determine target project
        // ***************************************************
        def targetProjectKey = "RDJ" // default
        def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
       // log.warn("baseUrl: ${baseUrl}")
        def getTargetProject = "/rest/scriptrunner/latest/custom/getProjectKey?productcode=${productCode}&platform=${platform}"
        if (recipient != "Development") {
            getTargetProject = "/rest/scriptrunner/latest/custom/getCSOProjectKey?productcode=${productCode}&platform=${platform}"
        }
        getTargetProject = baseUrl + getTargetProject
       // log.warn("getTargetProject: ${getTargetProject}")
        // GET
        def get = new URL(getTargetProject).openConnection();
        def getRC = get.getResponseCode();
      //  log.warn("getRC: ${getRC}");
        if(getRC.equals(200)) {
            def response = get.getInputStream().getText() as String
         //   log.warn("Response: ${response}");
            def parsedJson = new JsonSlurper().parseText(response) as Map
            targetProjectKey = parsedJson.projectkey as String
        //    log.warn("Project Key: ${targetProjectKey}")
        }
		return targetProjectKey
    }
