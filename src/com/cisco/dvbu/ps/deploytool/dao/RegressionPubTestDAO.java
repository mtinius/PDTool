/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 */
package com.cisco.dvbu.ps.deploytool.dao;

import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.common.util.wsapi.CompositeServer;
import com.cisco.dvbu.ps.deploytool.modules.RegressionTestType;

public interface RegressionPubTestDAO
{
	/**
	 * Executes all queries, procedures and web services in the input file,
	 * but only for published datasources (virtual databases). 
	 * Uses regressionConfig to control different aspects of execution,
	 * for example can skip some items from the input file if they
	 * are not from a predefined list of datasources.
	 * In that, this method is different from the original Pubtest utility
	 * which executes everything in the input file without restriction.
	 * 
	 * @param serverId
	 * @param dsList
	 * @param pathToRegressionXML
	 * @param pathToServersXML
	 * @throws CompositeException
	 */
	public void executeAll(CompositeServer cisServerConfig, RegressionTestType regressionConfig) throws CompositeException;
	

}
