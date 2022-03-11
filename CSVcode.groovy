import com.softwareag.jira.exalate.helper.Helper
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface
import java.sql.Connection
import com.softwareag.jira.exalate.helper.*
    
def log = Logger.getLogger("com.acme.CreateSubtask")
def productCodes = getProductCodeList()

log.debug("MOUNA2 "+productCodes)
log.setLevel(Level.DEBUG)
//String getProjectKey(String recipient, String productCode, String platform)
def file = new File("D:\\itrac-shared-home\\scripts\\com\\output.groovy")





def recipients   = ["Development", "CloudOps"]
 //productCodes = ["NAT", "DPO", "ADA"]
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
productCodes.each{productCode ->
    count=count+1
    line=productCode+","
    if(count<=1000){
    recipients.each{recipient ->
        platforms.each{platform ->
			//log.debug(recipient+"  "+productCode+" "+platform+" "+Helper.getProjectKey(recipient, productCode, platform))
                   //projectKeys.add(Helper.getProjectKey(recipient, productCode, platform))
                try{
                     line=line+Helper.getProjectKey(recipient, productCode, platform)+","   
                }
                catch(Exception e){
                    line=line+","
                }     

            }

        }
    		line= line.subSequence(0, line.length() - 1)

           line=line+"\n"
           all=all+line
    }
}
log.debug("===> "+productCodes)

file.write(all)
return all     
def getProductCodeList() {
    log.debug("CAMELIA")
    def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
    String helperName = delegator.getGroupHelperName("default")
	log.debug("CAMELIA")
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
    
