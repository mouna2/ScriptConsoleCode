import com.softwareag.jira.exalate.helper.Helper
import org.apache.log4j.Logger
import org.apache.log4j.Level

def log = Logger.getLogger("com.acme.CreateSubtask")
log.setLevel(Level.DEBUG)
//String getProjectKey(String recipient, String productCode, String platform)
def file = new File("D:\\itrac-shared-home\\scripts\\com\\output.groovy")

def recipients   = ["Development", "CloudOps"]
def productCodes = ["NAT", "DPO", "ADA"]
def platforms    = ["ZS","PC","MR","MF","UX"]
def projectKeys  = []

def all=""
def line ="ProductCode,"

recipients.each{recipient ->
    platforms.each{platform ->

       line=line+recipient+"-"+platform+","
    }
}

line=line+"\n"
all=all+line

productCodes.each{productCode ->
    line=productCode+","
    recipients.each{recipient ->
        platforms.each{platform ->
			log.debug(recipient+"  "+productCode+" "+platform+" "+Helper.getProjectKey(recipient, productCode, platform))
                   //projectKeys.add(Helper.getProjectKey(recipient, productCode, platform))
                      line=line+Helper.getProjectKey(recipient, productCode, platform)+","   

            }

        }
           line=line+"\n"
           all=all+line
    }

file.write(all)
return all 