/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 */
package com.cisco.dvbu.ps.deploytool.services;

import com.cisco.dvbu.ps.common.exception.CompositeException;
/**
 * Interface contains methods necessary for running regression tests against a 
 * CIS instance. 
 * 
 * @author sst
 *
 */
public interface RegressionManager
{
	/**
	 * Generates input file for regression tests for one published datasource 
	 * on a given CIS server for a given user. The server connection information
	 * comes from servers.xml by serverId parameter, the other parameter 
	 * specifies the name of the published data source on that server. 
	 * 
	 * @param serverId     		server Id from servers.xml
	 * @param regressionIds       	comma-separated list of the published regression identifiers to run test against
	 * @param pathToRegressionXML  path to the config file of this module 
	 * @param pathToServersXML     path to servers.xml
	 * 
	 * @return String     String representation of the input file
	 * @throws CompositeException
	 */
	 public void generateInputFile(String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;
	 
	 /**
	  * Runs a regression test for a given CIS server. Before the test is run,
	  * the regression input file is generated for that server, containing all
	  * published views, procedures and web services for a given published datasource 
	  * on that server.
	  * This method is for tests where no manual changes of the input file are necessary,
	  * and the generated file is ready for execution right away, 
	  * so input file generation and test run are part of one step. This degree of automation 
	  * insures that the input file never gets out of date.  
	  * 
	  * @param serverId     		server Id from servers.xml
	  * @param regressionIds       	comma-separated list of the published regression identifiers to run test against
	  * @param pathToRegressionXML  path to the config file of this module 
	  * @param pathToServersXML     path to servers.xml
	  * 
	  * @throws CompositeException
	  */
	 public void executeRegressionTest(String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;
	 
     /**
      * Compares the contents of two files for a given CIS server. The objective of the
      * comparison is to compare the before and after results.  This method should be
      * invoked in the context of a higher-level process where PD Tool is invoked to
      * executeRegressionTest() and generate a file.  PD Tool is invoked again at a
      * different point in time or against a different CIS server and produces another
      * file.  Then PD Tool is executed to compare the results of the two files
      * and determine if they are a match or not.  The results of this are output
      * to a result file.  The result of each regression comparison is either SUCCESS when 
      * the files match or FAILURE when the files do not match.
      * 
      * @param serverId   -  server Id from servers.xml
      * @param regressionIds -  comma-separated list of the regression identifiers to execute the file comparison
      * @param pathToRegressionXML - path to the config file of this module
      * @param pathToServersXML  -   path to servers.xml
      * @throws CompositeException
      */
    public void compareRegressionFiles(String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;

	/** 
	 * Compare the Query Execution log files for two separate execution runs.
	 * Determine if queries executed in for two similar but separate tests are within the acceptable delta level.
	 * Compare each similar result duration and apply a +- delta level to see if it falls within the acceptable range.
	 * 
     * @param serverId   -  server Id from servers.xml
     * @param regressionIds -  comma-separated list of the regression identifiers to execute the log comparison
     * @param pathToRegressionXML - path to the config file of this module
     * @param pathToServersXML  -   path to servers.xml
     * @throws CompositeException
	 */
	public void compareRegressionLogs(String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;

	/**
	 * executePerformanceTest - execute a performance test
	 * 
     * @param serverId   -  server Id from servers.xml
     * @param regressionIds -  comma-separated list of the regression identifiers to execute the log comparison
     * @param pathToRegressionXML - path to the config file of this module
     * @param pathToServersXML  -   path to servers.xml
	 * @throws CompositeException
	 */
	public void executePerformanceTest( String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;

	/**
	 * executeSecurityTest - execute a security test
	 * 
     * @param serverId   -  server Id from servers.xml
     * @param regressionIds -  comma-separated list of the regression identifiers to execute the log comparison
     * @param pathToRegressionXML - path to the config file of this module
     * @param pathToServersXML  -   path to servers.xml
	 * @throws CompositeException
	 */
	public void executeSecurityTest( String serverId, String regressionIds, String pathToRegressionXML, String pathToServersXML) throws CompositeException;

	/**
	 * Generates the Regression Security XML section of the RegressionModule.xml.
	 *   Generates Regression Security Users from the given filter applying the xml schema userMode=[NOEXEC|OVERWRITE|APPEND].
	 *   Generates Regression Security Queries from the given filter applying the xml schema queryMode=[NOEXEC|OVERWRITE|APPEND].
	 *   Generates a Cartesian product for the Regression Security Plans applying the xml schema planMode=[NOEXEC|OVERWRITE|APPEND] 
	 *     and planModeType=[SINGLEPLAN|MULTIPLAN].
	 *   Generates to a different RegressionModule.xml file than the source file so that formatting can be maintained in the XML in the source.
	 *     This is based on the xml schema pathToTargetRegressionXML.
	 *   It is recommended that the users copy the results out of the target, generated file and paste into the source file as needed.
	 *   A Cartesian product is where each user contains an execution for all of the queries. 
	 *   A security plan is as follows:
	 *     A security plan consists of executing all queries for a single user.
	 *     A Cartesian product involves creating a plan for each user with all queries.
	 * 
	 * @param serverId     				server Id from servers.xml
	 * @param regressionIds       		comma-separated list of the published regression identifiers to run test against
	 * @param pathToSourceRegressionXML path to the source configuration file for the regression module.  Provides a way of maintaining existing files without overwriting.
	 * @param pathToServersXML     		path to servers.xml
	 * @throws CompositeException
	 */
	public void generateRegressionSecurityXML(String serverId, String regressionIds, String pathToSourceRegressionXML, String pathToServersXML) throws CompositeException; 


}
