/**
 * 
 * Author: JHE
 * Reviewed by: 
 * Short Description: Provides iTrac common functions 
 * 
 * 
 */

package com.softwareag.jira.exalate.helper

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.option.Option
import org.apache.log4j.Logger

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import groovy.json.JsonSlurper

import org.apache.log4j.Logger;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;

public class Configuration {

	public final static String supportField = "customfield_13483";
	public final static String developmentField = "customfield_13484";
	
	public final static String totalTestPatchesField = "customfield_13481";
	public final static Long totalTestPatchesOption = 16273L;

	public final static String totalBadTestPatchesField = "customfield_13482";
	public final static Long totalBadTestPatchesOption = 16274L;
	public final static Long totalSuccessfulTestPatchesOption = 16271L;
	public final static Long totalBadTestPatchesWithSideEffectOption = 17070L;
	
	public final static String totalBadTestPatchesWithSideEffectField = "customfield_13680";

	public final static String testPatchField = "customfield_13480";
	public final static String testPatchValue = "waiting for verification";
	
	public final static String supportGroup = "TraFixGlobalSupport";
	public final static String developmentGroup = "jira-developers";

	public static final Map<Long, String> optionIncrementCounterMappings;
	static
	{
		optionIncrementCounterMappings = new HashMap<Long, String>();
		optionIncrementCounterMappings.put(totalTestPatchesOption, totalTestPatchesField);
		optionIncrementCounterMappings.put(totalBadTestPatchesOption, totalBadTestPatchesField);
		optionIncrementCounterMappings.put(totalBadTestPatchesWithSideEffectOption, totalBadTestPatchesWithSideEffectField);
	}

	public static final Map<Long, String> optionResetFieldMappings;
	static
	{
		optionResetFieldMappings = new HashMap<Long, String>();
		optionResetFieldMappings.put(totalSuccessfulTestPatchesOption, testPatchField);
		optionResetFieldMappings.put(totalBadTestPatchesOption, testPatchField);
		optionResetFieldMappings.put(totalBadTestPatchesWithSideEffectOption, testPatchField);
	}

	public static final List<String> updateDevGsIssuePanelFields;
	static
	{
		updateDevGsIssuePanelFields = new ArrayList<String>();
		updateDevGsIssuePanelFields.add(testPatchField);
		updateDevGsIssuePanelFields.add(totalTestPatchesField);
		updateDevGsIssuePanelFields.add(totalBadTestPatchesField);
		updateDevGsIssuePanelFields.add(totalBadTestPatchesWithSideEffectField);
	}

}

public class Helper {
    
    static log = Logger.getLogger("com.softwareag.jira.exalate.helper.Helper")
    
    private static String getUpdateDevelopmentOption(String updateDevelopment) {
       
        log.warn("Update Development: ${updateDevelopment}")
		def updateDevelopmentOptions = [
            "Communication only"                  : "16270",
            "Test Patch failed"                   : "16274",
            "Test Patch failed with side effects" : "17070",
            "Test Patch verified"                 : "16271"
        ]
        def updateDevelopmentOption = updateDevelopmentOptions[updateDevelopment]
        log.warn("Update Development Option: ${updateDevelopmentOption}")
        return updateDevelopmentOption ? updateDevelopmentOption : "16270" // default -> communication only
    }    
        
    private static String getNextActionOwnerOption(String nextActionOwner) {
      
        log.warn("Next Action Owner: ${nextActionOwner}")
        def nextActionOwnerOptions = [
            "Support"   : "11289",
            "Other SAG" : "11288"
        ]
        def nextActionOwnerOption = nextActionOwnerOptions[nextActionOwner]
        log.warn("Next Action Owner Option: ${nextActionOwnerOption}")
        return nextActionOwnerOption ? nextActionOwnerOption : "11288" // default -> rnd 
    }
    
    private static String getNextActionOption(String nextAction) {
       
        log.warn("Next Action: ${nextAction}")
        def nextActionOptions = [
            "Status Update Requested"                  : "16375",
            "Provide Additional Data"                  : "11272",
            "Confirm to Close"                         : "11273",
            "Inform Customer"                          : "11274",
            "Consulting Requested"                     : "11275",
            "Confirm Provided Information"             : "11276",
            "Provide Workaround or Solution"           : "11277",
            "Analyze Root Cause"                       : "11279",
            "Close Issue"                              : "11280",
            "Resolve Issues with Diagnostic Collector" : "16376",
            "Resolve Issues with Test Patch"           : "16377",
            "Provide Diagnostic Collector Results"     : "16378",
            "Confirm if Test Patch resolves problem"   : "16379",
            "Release Official Fix"                     : "11287",
            "Waiting for Customer Response"            : "19472"
        ]
        def nextActionOption = nextActionOptions[nextAction]
        log.warn("Next Action Option: ${nextActionOption}")
        return nextActionOption ? nextActionOption : "16375" // default -> status update requested
    }
    
    public static boolean updateDevGsAsSupporter(MutableIssue issue, String updateDevelopment, String nextActionOwner, String nextAction, String comment) {
        
        def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
        user = ComponentAccessor.getUserUtil().getUser("supportuser") as ApplicationUser
        log.warn("User: ${user}")

        def updateDevelopmentOption = getUpdateDevelopmentOption(updateDevelopment)
        def nextActionOwnerOption = getNextActionOwnerOption(nextActionOwner)
        def nextActionOption = getNextActionOption(nextAction)
		def debugInfo = "\r\n" + "Update Development Option: ${updateDevelopmentOption}" + 
                        "\r\n" + "Next Action Owner Option : ${nextActionOwnerOption}" +
                        "\r\n" + "Next Action Option       : ${nextActionOption}"

        def updateDevGs = [1401,1251,1141,1131,1121,1101,1111]

        def cfNextActionOwner = ComponentAccessor.customFieldManager.getCustomFieldObjectByName("Next Action Owner ")
        def cfNextAction = ComponentAccessor.customFieldManager.getCustomFieldObjectByName("Next Action ")
        def cfUpdateDevelopment = ComponentAccessor.customFieldManager.getCustomFieldObjectByName("Update Development")

		def issueService = ComponentAccessor.getIssueService()

        def issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.with {
            setSkipScreenCheck(true)
            setComment(comment + debugInfo)
            addCustomFieldValue(cfUpdateDevelopment.id, updateDevelopmentOption)
            addCustomFieldValue(cfNextActionOwner.id, nextActionOwnerOption)
            addCustomFieldValue(cfNextAction.id, nextActionOption)
        }

        // validate and transition subtask
        def validationResult = issueService.validateTransition(user, issue.id, updateDevGs[0], issueInputParameters)
        log.warn ("Tranistion ${updateDevGs[0]} - validation result is ${validationResult.isValid()}")
        if (!validationResult.isValid()) {
            validationResult = issueService.validateTransition(user, issue.id, updateDevGs[1], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[1]} - validation result is ${validationResult.isValid()}")
        }
        if (!validationResult.isValid()) {
            validationResult = issueService.validateTransition(user, issue.id, updateDevGs[2], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[2]} - validation result is ${validationResult.isValid()}")
        }
        if (!validationResult.isValid()) {
            validationResult = issueService.validateTransition(user, issue.id, updateDevGs[3], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[3]} - validation result is ${validationResult.isValid()}")
        }
        if (!validationResult.isValid()) {
            validationResult = issueService.validateTransition(user, issue.id, updateDevGs[4], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[4]} - validation result is ${validationResult.isValid()}")
    	}
    	if (!validationResult.isValid()) {
        	validationResult = issueService.validateTransition(user, issue.id, updateDevGs[5], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[5]} - validation result is ${validationResult.isValid()}")
        }
    	if (!validationResult.isValid()) {
        	validationResult = issueService.validateTransition(user, issue.id, updateDevGs[6], issueInputParameters)
	        log.warn ("Tranistion ${updateDevGs[6]} - validation result is ${validationResult.isValid()}")
        }
        
        if (validationResult.isValid()) {
            def issueResult = issueService.transition(user, validationResult)
            if (!issueResult.isValid()) {
                log.warn("Failed to transition issue ${issue.key}, errors: ${issueResult.errorCollection}")
                return false
            }
        } else {
            log.warn("Could not transition issue ${issue.key}, errors: ${validationResult.errorCollection}")
            return false
        }

        return true;
    }
    
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
        log.warn("baseUrl: ${baseUrl}")
        def getTargetProject = "/rest/scriptrunner/latest/custom/getProjectKey?productcode=${productCode}&platform=${platform}"
        if (recipient != "Development") {
            getTargetProject = "/rest/scriptrunner/latest/custom/getCSOProjectKey?productcode=${productCode}&platform=${platform}"
        }
        getTargetProject = baseUrl + getTargetProject
        log.warn("getTargetProject: ${getTargetProject}")
        // GET
        def get = new URL(getTargetProject).openConnection();
        def getRC = get.getResponseCode();
        log.warn("getRC: ${getRC}");
        if(getRC.equals(200)) {
            def response = get.getInputStream().getText() as String
            log.warn("Response: ${response}");
            def parsedJson = new JsonSlurper().parseText(response) as Map
            targetProjectKey = parsedJson.projectkey as String
            log.warn("Project Key: ${targetProjectKey}")
        }
		return targetProjectKey
    }

    private static boolean incrementCounterIfRequired(MutableIssue issue, Long option) {
		boolean counterHasBeenIncremented = false;
		
		String logPrefix = "Issue [" + issue?.key + "] - ";

		if (option != null) {
			String counterFieldId = Configuration.optionIncrementCounterMappings.get(option);
			if (counterFieldId != null) {

				if ((Configuration.totalBadTestPatchesOption.compareTo(option) == 0)
						|| (Configuration.totalBadTestPatchesWithSideEffectOption.compareTo(option) == 0)) {
					CustomField testPatch = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Configuration.testPatchField);
					Object o = issue.getCustomFieldValue(testPatch);
					if (o != null) {
						addValueToCounterField(issue, counterFieldId, 1.0 as Double);
					}
				}
				else {
					addValueToCounterField(issue, counterFieldId, 1.0 as Double);
				}
				if (Configuration.totalTestPatchesOption.compareTo(option) == 0 as Double) {
					log.warn(logPrefix + "Provide a Test Patch");
					addValueToCounterField(issue, Configuration.testPatchField, 0.0 as Double);
				}
				counterHasBeenIncremented = true;
			}
		}
		return counterHasBeenIncremented;
	}

	private static void addValueToCounterField(MutableIssue issue, String counterFieldId, Double increment) {
		CustomField counterField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(counterFieldId);
	
		String logPrefix = "Issue [" + issue.key + "] - ";
		
		Object o = issue.getCustomFieldValue(counterField);
		log.warn(logPrefix + "Field " + counterField.getName() + " had value " + o);
		if (o == null) {
			counterField.createValue(issue, new Double("1.0"));
			log.warn(logPrefix + "Field " + counterField.getName() + " is set to " + "1.0");
		}
		else {	
			Double value = (Double) o;
			value = value + increment;
			IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
			counterField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(counterField), value), changeHolder);
			issue.setCustomFieldValue(counterField, value);
			log.warn(logPrefix + "Field " + counterField.getName() + " is set to " + value);
			if (Configuration.testPatchField.equalsIgnoreCase(counterFieldId)) {
				log.warn(logPrefix + "Total Bad Test Patches Field will be incremented implicitly") ;
				addValueToCounterField(issue, Configuration.totalBadTestPatchesField, 1.0 as Double);
			}
		}
	}

	private static boolean resetFieldIfRequired(MutableIssue issue, Long option) {
		boolean fieldHasBeenReset = false;
       
		if (option != null) {
			String fieldId = Configuration.optionResetFieldMappings.get(option);
			if (fieldId != null) {

				String logPrefix = "Issue [" + issue.getKey() + "] - ";

				// reset field
				CustomField field = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldId);
				IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
				field.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(field), null), changeHolder);
				issue.setCustomFieldValue(field, null);
				log.warn(logPrefix + "Field " + field.getName() + " is reset.");
				fieldHasBeenReset = true;
			}
		}
		return fieldHasBeenReset;
	}

   	public static void updateCounters(MutableIssue issue, Long option) {
        String logPrefix = "Issue [" + issue.getKey() + "] - ";
       
        boolean counterHasBeenIncremented = Helper.incrementCounterIfRequired(issue, option);
        log.warn(logPrefix + "Corresponding Counter has " + (counterHasBeenIncremented ? "" : "NOT ") + "been incremented.");
        boolean fieldHasBeenReset = Helper.resetFieldIfRequired(issue, option);
        log.warn(logPrefix + "Corresponding Field has " + (fieldHasBeenReset ? "" : "NOT ") + "been reset.");
    }

}
