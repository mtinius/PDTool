/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 */
package com.cisco.dvbu.ps.deploytool.services;

/**
 * Initial Version:	Mike Tinius :: 6/8/2011		
 * Modifications:	initials :: Date :: reason
 * mtinius :: 1/27/2014 :: Added updateResourceCacheEnabled
 * 
 */

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;

import com.cisco.dvbu.ps.common.CommonConstants;
import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.common.exception.ValidationException;
import com.cisco.dvbu.ps.common.util.CommonUtils;
import com.cisco.dvbu.ps.common.util.XMLUtils;
import com.cisco.dvbu.ps.deploytool.DeployManagerUtil;
import com.cisco.dvbu.ps.deploytool.dao.ResourceCacheDAO;
import com.cisco.dvbu.ps.deploytool.dao.wsapi.ResourceCacheWSDAOImpl;
import com.cisco.dvbu.ps.deploytool.util.DeployUtil;
import com.cisco.dvbu.ps.deploytool.modules.ObjectFactory;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheCalendarPeriodType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheConfigType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheModule;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheRefreshScheduleType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheRefreshType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheStorageTargetsType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheStorageType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceCacheType;
import com.cisco.dvbu.ps.deploytool.modules.ResourceTypeSimpleType;
import com.compositesw.services.system.admin.resource.CacheConfig;
import com.compositesw.services.system.admin.resource.CacheConfig.Refresh;
import com.compositesw.services.system.admin.resource.CacheConfig.Storage;
import com.compositesw.services.system.admin.resource.CalendarPeriod;
import com.compositesw.services.system.admin.resource.ClearRule;
import com.compositesw.services.system.admin.resource.RefreshMode;
import com.compositesw.services.system.admin.resource.Resource;
import com.compositesw.services.system.admin.resource.ResourceList;
import com.compositesw.services.system.admin.resource.ResourceType;
import com.compositesw.services.system.admin.resource.Schedule;
import com.compositesw.services.system.admin.resource.ScheduleMode;
import com.compositesw.services.system.admin.resource.StorageMode;
import com.compositesw.services.system.admin.resource.TargetPathTypePair;
import com.compositesw.services.system.admin.resource.TargetPathTypePairList;
import com.compositesw.services.system.util.common.DetailLevel;


public class ResourceCacheManagerImpl implements ResourceCacheManager{

	private ResourceCacheDAO resourceCacheDAO = null;
	private ArrayList<String> tokenType = new ArrayList<String>();
	private ArrayList<Integer> tokenNum = new ArrayList<Integer>();
	
	private static Log logger = LogFactory.getLog(ResourceCacheManagerImpl.class);

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceCacheManager#updateResourceCache(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateResourceCache(String serverId, String resourceIds, String pathToResourceCacheXML, String pathToServersXML) throws CompositeException {
		if(logger.isDebugEnabled()){
			logger.debug(" Entering ResourceCacheManagerImpl.updateResourceCache() with following params "+" serverId: "+serverId+", resourceIds: "+resourceIds+", pathToResourceCacheXML: "+pathToResourceCacheXML+", pathToServersXML: "+pathToServersXML);
		}
		try {
			resourceCacheAction(ResourceCacheDAO.action.UPDATE.name(), serverId, resourceIds, pathToResourceCacheXML, pathToServersXML);
		} catch (CompositeException e) {
			logger.error("Error while updating resource cache: " , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}				
	}

	
	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceCacheManager#clearResourceCache(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void clearResourceCache(String serverId, String resourceIds, String pathToResourceCacheXML, String pathToServersXML) throws CompositeException {
		if(logger.isDebugEnabled()){
			logger.debug(" Entering ResourceCacheManagerImpl.clearResourceCache() with following params "+" serverId: "+serverId+", resourceIds: "+resourceIds+", pathToResourceCacheXML: "+pathToResourceCacheXML+", pathToServersXML: "+pathToServersXML);
		}
		try {
			resourceCacheAction(ResourceCacheDAO.action.CLEAR.name(), serverId, resourceIds, pathToResourceCacheXML, pathToServersXML);
		} catch (CompositeException e) {
			logger.error("Error while clearing resource cache: " , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}		
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceCacheManager#refreshResourceCache(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void refreshResourceCache(String serverId, String resourceIds, String pathToResourceCacheXML, String pathToServersXML) throws CompositeException {
		if(logger.isDebugEnabled()){
			logger.debug(" Entering ResourceCacheManagerImpl.refreshResourceCache() with following params "+" serverId: "+serverId+", resourceIds: "+resourceIds+", pathToResourceCacheXML: "+pathToResourceCacheXML+", pathToServersXML: "+pathToServersXML);
		}
		try {
			resourceCacheAction(ResourceCacheDAO.action.REFRESH.name(), serverId, resourceIds, pathToResourceCacheXML, pathToServersXML);
		} catch (CompositeException e) {
			logger.error("Error while refreshing resource cache: " , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}		
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceCacheManager#updateResourceCacheEnabled(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateResourceCacheEnabled(String serverId, String resourceIds, String pathToResourceCacheXML, String pathToServersXML) throws CompositeException {
		if(logger.isDebugEnabled()){
			logger.debug(" Entering ResourceCacheManagerImpl.updateResourceCacheEnabled() with following params "+" serverId: "+serverId+", resourceIds: "+resourceIds+", pathToResourceCacheXML: "+pathToResourceCacheXML+", pathToServersXML: "+pathToServersXML);
		}
		try {
			resourceCacheAction(ResourceCacheDAO.action.ENABLE_DISABLE.name(), serverId, resourceIds, pathToResourceCacheXML, pathToServersXML);
		} catch (CompositeException e) {
			logger.error("Error while enabling/disabling resource cache: " , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}				
	}

	private void resourceCacheAction(String actionName, String serverId, String resourceIds, String pathToResourceCacheXML, String pathToServersXML) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceCacheXML)) {
			throw new CompositeException("File ["+pathToResourceCacheXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		String prefix = "resourceCacheAction";
		try {
			List<ResourceCacheType> resourceCacheModuleList = getResourceCache(serverId, resourceIds, pathToResourceCacheXML, pathToServersXML);
			
			// Get the configuration property file set in the environment with a default of deploy.properties
			String propertyFile = CommonUtils.getFileOrSystemPropertyValue(CommonConstants.propertyFile, "CONFIG_PROPERTY_FILE");

			// Extract variables for the resourceIds
			resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

			if (resourceCacheModuleList != null && resourceCacheModuleList.size() > 0) {
	
				// Loop over the list of resource cache entries and apply their configurations to the target CIS instance.
				for (ResourceCacheType resourceCacheModule : resourceCacheModuleList) {

					// Get the identifier and convert any $VARIABLES
					String identifier = CommonUtils.extractVariable(prefix, resourceCacheModule.getId(), propertyFile, true);
					
					/**
					 * Possible values for resource cache 
					 * 1. csv string like rc1,rc2 (we process only resource names which are passed in)
					 * 2. '*' or whatever is configured to indicate all resources (we process all resources in this case)
					 * 3. csv string with '-' or whatever is configured to indicate exclude resources as prefix 
					 * 	  like -rc1,rc2 (we ignore passed in resources and process rest of the in the input xml
					 */
					 if(DeployUtil.canProcessResource(resourceIds, identifier)) 
					 {
						CacheConfig cacheConfig = new CacheConfig();			 
						String resourceCachePath = resourceCacheModule.getResourcePath();
						String resourceCacheType = resourceCacheModule.getResourceType().toString();
						
						if(logger.isInfoEnabled()){
							logger.info("processing action "+actionName+" on resource cache "+resourceCachePath);
						}
						if (actionName.equals(ResourceCacheDAO.action.ENABLE_DISABLE.name().toString())) {
							
							if (resourceCacheModule.getCacheConfig() != null) {
								// Set enabled flag
								Boolean enabled = null;
								if (resourceCacheModule.getCacheConfig().isEnabled() != null) {
									enabled = resourceCacheModule.getCacheConfig().isEnabled();
								}
								updateResourceCacheEnabledAll(serverId, resourceCachePath, resourceCacheType, pathToServersXML, enabled);
							}
						} else {
							if (resourceCacheModule.getCacheConfig() != null) {

								// Set configured if it exists
								if (resourceCacheModule.getCacheConfig().isConfigured() != null) {
									cacheConfig.setConfigured(resourceCacheModule.getCacheConfig().isConfigured());
								}
								// Set enabled if it exists
								if (resourceCacheModule.getCacheConfig().isEnabled() != null) {
									cacheConfig.setEnabled(resourceCacheModule.getCacheConfig().isEnabled());
								}
								// Set the storage if it exists
								if (resourceCacheModule.getCacheConfig().getStorage() != null) {
									Storage storage = new Storage();
									if (resourceCacheModule.getCacheConfig().getStorage().getMode() != null) {
										storage.setMode(StorageMode.valueOf(resourceCacheModule.getCacheConfig().getStorage().getMode()));
									}
									if (resourceCacheModule.getCacheConfig().getStorage().getStorageDataSourcePath() != null) {
										storage.setStorageDataSourcePath(resourceCacheModule.getCacheConfig().getStorage().getStorageDataSourcePath());
									}
									if (resourceCacheModule.getCacheConfig().getStorage().getStorageTargets() != null) {
										// Define the Target Storage List
										TargetPathTypePairList entry = new TargetPathTypePairList();
		
										for (ResourceCacheStorageTargetsType storageTarget : resourceCacheModule.getCacheConfig().getStorage().getStorageTargets()) {
											// Define the Target Storage Entry
											TargetPathTypePair targetPair = new TargetPathTypePair();
											// Set the target pair entry
											targetPair.setPath(storageTarget.getPath());
											targetPair.setTargetName(storageTarget.getTargetName());
											targetPair.setType(ResourceType.valueOf(storageTarget.getType().toUpperCase()));
											// Add the target pair entry to the list
											entry.getEntry().add(targetPair);						
										}
										storage.setStorageTargets(entry);
									}
									cacheConfig.setStorage(storage);
								} //end::if (resourceCacheModule.getCacheConfig().getStorage() != null) {
								
								// Set the refresh if it exists
								if (resourceCacheModule.getCacheConfig().getRefresh() != null) {
									Refresh refresh = new Refresh();
									String refreshMode = resourceCacheModule.getCacheConfig().getRefresh().getMode().toUpperCase();
									refresh.setMode(RefreshMode.valueOf(refreshMode));
									
									if (resourceCacheModule.getCacheConfig().getRefresh().getSchedule() != null) {
										if (refreshMode.equalsIgnoreCase("SCHEDULED")) {
											
											Schedule schedule = new Schedule();
		
											if (resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getStartTime() != null) {							
												schedule.setStartTime(resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getStartTime());
											}
		
											if (resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getRefreshPeriod().getPeriod() != null) {
												String period = resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getRefreshPeriod().getPeriod().toUpperCase();
												
												// Set the mode to INTERVAL
												if (period.equalsIgnoreCase("SECOND") || period.equalsIgnoreCase("MINUTE")) {
													schedule.setMode(ScheduleMode.valueOf("INTERVAL"));
													Long interval = convertPeriodCount(period, resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getRefreshPeriod().getCount(), "seconds");
													schedule.setInterval(interval.intValue());
												} else {
													schedule.setMode(ScheduleMode.valueOf("CALENDAR"));
													schedule.setPeriod(CalendarPeriod.valueOf(resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getRefreshPeriod().getPeriod().toUpperCase()));
													Integer count = (int) resourceCacheModule.getCacheConfig().getRefresh().getSchedule().getRefreshPeriod().getCount();
													schedule.setCount(count);
												}
											}
											refresh.setSchedule(schedule);
										} 
									}
									cacheConfig.setRefresh(refresh);
								} //end::if (resourceCacheModule.getCacheConfig().getRefresh() != null) {
	
								// Set the Expiration if it exists
								if (resourceCacheModule.getCacheConfig().getExpirationPeriod() != null) {
									Long milliCount = convertPeriodCount(resourceCacheModule.getCacheConfig().getExpirationPeriod().getPeriod(), resourceCacheModule.getCacheConfig().getExpirationPeriod().getCount(), "milliseconds");
									cacheConfig.setExpirationPeriod(milliCount);
								}
								// Set the clear rule if it exists
								if (resourceCacheModule.getCacheConfig().getClearRule() != null) {
									cacheConfig.setClearRule(ClearRule.valueOf(resourceCacheModule.getCacheConfig().getClearRule().toUpperCase()));
								}
							} //end::if (resourceCacheModule.getCacheConfig() != null) {
							
							// Validate that the resource exists before acting on it.
							Boolean validateResourceExists = true;
							
							// Execute takeResourceCacheAction()
							getResourceCacheDAO().takeResourceCacheAction(actionName, resourceCachePath, resourceCacheType, cacheConfig, serverId, pathToServersXML, validateResourceExists);
							
						} // end:: if (actionName.equals(ResourceCacheDAO.action.ENABLE_DISABLE.name().toString())) {
					}  // end:: if(DeployUtil.canProcessResource(resourceIds, identifier)) {
				} // end:: for (ResourceCacheType resourceCache : resourceCacheList) {
			}
		} catch (CompositeException e) {
			logger.error("Error on resource cache action ("+actionName+"): " , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}
	}

	private List<ResourceCacheType> getResourceCache(String serverId, String resourceIds,	String pathToResourceCacheXML, String pathToServersXML) {
		// validate incoming arguments
		if(serverId == null || serverId.trim().length() ==0 || resourceIds == null || resourceIds.trim().length() ==0 || pathToServersXML == null || pathToServersXML.trim().length() ==0 || pathToResourceCacheXML == null || pathToResourceCacheXML.trim().length() ==0){
			throw new ValidationException("Invalid Arguments");
		}

		try {
			//using jaxb convert xml to corresponding java objects
			ResourceCacheModule resourceCacheModuleType = (ResourceCacheModule)XMLUtils.getModuleTypeFromXML(pathToResourceCacheXML);
			if(resourceCacheModuleType != null && resourceCacheModuleType.getResourceCache() != null && !resourceCacheModuleType.getResourceCache().isEmpty()){
				return resourceCacheModuleType.getResourceCache();
			}
		} catch (CompositeException e) {
			logger.error("Error while parsing resource cache xml" , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}
		return null;
	}
	
	
	private void updateResourceCacheEnabledAll(String serverId, String resourcePath, String resourceType, String pathToServersXML, Boolean enabled) {
	
		// Set the cache enabled flag for all cache configured resources found within this starting resource path
		if (resourceType.equalsIgnoreCase(ResourceType.CONTAINER.name())) {
			// Don't set any filters
			String filter = null;
			
			// Don't validate the paths exists since the list of paths has already been acquired from the server.
			Boolean validateResourceExists = false;

			// Retrieve the list of Resources by invoking the CIS Web Service API
			ResourceList resourceList = new ResourceList();
			
			// Get all resources in the given resource path
			resourceList.getResource().addAll(DeployManagerUtil.getDeployManager().getResourcesFromPath(serverId, resourcePath, resourceType, filter, DetailLevel.FULL.name(), pathToServersXML).getResource());
	
			// Continue if there is a list
			if(resourceList != null && resourceList.getResource() != null && !resourceList.getResource().isEmpty()){
	
				// Assign the list of resources to a local Attribute type variable
				List<Resource> resources = resourceList.getResource();			
					
				// Iterate over the retrieved Server Attribute List
				for (Resource resource : resources) {
	
					// Set the resource type
					String resourceCachePath = resource.getPath();
					// Set the resource path
					String resourceCacheType = resource.getType().name();
					// Define the original cache enabled flag
					Boolean enabledOrig = null;
	
					// Get the resource cache configuration from the CIS server for a given path and type
					CacheConfig cacheConfig = getResourceCacheDAO().getResourceCacheConfig(resource.getPath(), resource.getType().toString(), serverId, pathToServersXML, validateResourceExists);
	
					if (cacheConfig != null) {
						
						// Only set cacheConfig objects if it is configured
						if (cacheConfig.isConfigured()) {
	
							// Set the original enabled value
							if (cacheConfig.isEnabled() != null) {
								enabledOrig = cacheConfig.isEnabled();
							}
							CacheConfig cacheConfigNew = new CacheConfig();
							
							// Set the cache config enabled to the enabled value passed in.
							cacheConfigNew.setEnabled(enabled);
							
							// Execute takeResourceCacheAction()
							getResourceCacheDAO().takeResourceCacheAction(ResourceCacheDAO.action.UPDATE.name(), resourceCachePath, resourceCacheType, cacheConfigNew, serverId, pathToServersXML, validateResourceExists);
	
							// Output an info line with the results
							if(logger.isInfoEnabled()){
								logger.info("Cache operation (enable)="+enabled+"  prevStatus="+enabledOrig+"  currStatus="+enabled+"  resourceType="+resourceCacheType+"  resourcePath="+resourceCachePath);
							}
						}
					}
				}
			}	
		// Set the cache enabled flag for this cache configured resource found at this resource path
		} else {
			// Define the original cache enabled flag
			Boolean enabledOrig = null;

			// Validate the path exists prior to continuing.
			Boolean validateResourceExists = true;

			// Get the resource cache configuration from the CIS server for a given path and type
			CacheConfig cacheConfig = getResourceCacheDAO().getResourceCacheConfig(resourcePath, resourceType, serverId, pathToServersXML, validateResourceExists);

			if (cacheConfig != null) {
				
				// Only set cacheConfig objects if it is configured
				if (cacheConfig.isConfigured()) {

					// Set the original enabled value
					if (cacheConfig.isEnabled() != null) {
						enabledOrig = cacheConfig.isEnabled();
					}
					
					CacheConfig cacheConfigNew = new CacheConfig();
					
					// Set the cache config enabled to the enabled value passed in.
					cacheConfigNew.setEnabled(enabled);
					
					// The resource has already been validated once.
					validateResourceExists = false;
					
					// Execute takeResourceCacheAction()
					getResourceCacheDAO().takeResourceCacheAction(ResourceCacheDAO.action.UPDATE.name(), resourcePath, resourceType, cacheConfigNew, serverId, pathToServersXML, validateResourceExists);

					// Output an info line with the results
					if(logger.isInfoEnabled()){
						logger.info("Cache operation (enable)="+enabled+"  prevStatus="+enabledOrig+"  currStatus="+enabled+"  resourceType="+resourceType+"  resourcePath="+resourcePath);
					}
				}
			}			
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceCacheManager#generateResourceCacheXML(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void generateResourceCacheXML(String serverId, String startPath, String pathToResourceCacheXML, String pathToServersXML, String options) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		// Prepare a local ResourceCacheModule XML variable for creating a list of "ResourceCache" nodes
		// This XML variable will be written out to the specified file. 
		ResourceCacheModule resourceCacheModule = new ObjectFactory().createResourceCacheModule();

		// Use the options list to set boolean variables for getting resources based on TABLE,PROCEDURE,CONFIGURED and NONCONFIGURED
		Boolean getResourceType_TABLE = false;
		Boolean getResourceType_PROCDURE = false;
		Boolean getConfigured = false;
		Boolean getNonConfigured = false;
		String filter = null;
		// If the options field is null or empty the default is to get both resource types and both configured and non-configured resource cache types
		if (options == null || options.isEmpty() || options.equalsIgnoreCase("")) {
			// TABLE and PROCEDURE are the valid resource types that can have caching configured
			getResourceType_TABLE = true;
			getResourceType_PROCDURE = true;
			getConfigured = true;
			getNonConfigured = true;
		} else {
			// Insure there are no spaces on the end and replace any space in between with commas to insure a comma separated list
			options = options.trim().replaceAll(Matcher.quoteReplacement(" "), ",");
			StringTokenizer st = new StringTokenizer(options, ",");
		    while (st.hasMoreTokens()) {
		    	String token = st.nextToken();
				if (token.equalsIgnoreCase("TABLE")) {
					getResourceType_TABLE = true;
					filter = "TABLE";
				}
				if (token.equalsIgnoreCase("PROCEDURE")) {
					getResourceType_PROCDURE = true;
					if (filter == null) {
						filter = "PROCEDURE";
					} else {
						filter = filter+",PROCEDURE";
					}
				}
				if (token.equalsIgnoreCase("CONFIGURED")) {
					getConfigured = true;
				}
				if (token.equalsIgnoreCase("NONCONFIGURED")) {
					getNonConfigured = true;
				}
		    }
			// If neither TABLE or PROCEDURE is found the default is to get both resource types
			if (!getResourceType_TABLE && !getResourceType_PROCDURE) {
				getResourceType_TABLE = true;
				getResourceType_PROCDURE = true;
				filter = "TABLE,PROCEDURE";
			}
			// If neither CONFIGURED or NONCONFIGURED is found the default is to get both configured and non-configured cache resources
			if (!getConfigured && !getNonConfigured) {
				getConfigured = true;
				getNonConfigured = true;
			}
		}
		
		// Retrieve the list of Resources by invoking the CIS Web Service API
		ResourceList resourceList = new ResourceList();

		if (getResourceType_TABLE) {
			resourceList.getResource().addAll(DeployManagerUtil.getDeployManager().getResourcesFromPath(serverId, startPath, ResourceType.CONTAINER.name(), filter, DetailLevel.FULL.name(), pathToServersXML).getResource());
		}

		// Continue if there is a list
		if(resourceList != null && resourceList.getResource() != null && !resourceList.getResource().isEmpty()){
			
			// Don't validate the resource exists since the paths have already been acquired from the server.
			Boolean validateResourceExists = false;

			// Assign the list of resources to a local Attribute type variable
			List<Resource> resources = resourceList.getResource();			
				
			// Iterate over the retrieved Server Attribute List
			for (Resource resource : resources) {
				// Define the resource cache type
				ResourceCacheType resourceCacheType = new ResourceCacheType();
				
				// Set the resource path
				resourceCacheType.setResourcePath(resource.getPath());
				// Set the resource type
				resourceCacheType.setResourceType(ResourceTypeSimpleType.valueOf(resource.getType().toString()));

				// Get the resource cache configuration from the CIS server for a given path and type
				CacheConfig cacheConfig = getResourceCacheDAO().getResourceCacheConfig(resource.getPath(), resource.getType().toString(), serverId, pathToServersXML, validateResourceExists);

				if (cacheConfig != null) {
					// Define the resource cache config type
					ResourceCacheConfigType resourceCacheConfigType = new ResourceCacheConfigType();
					
					// Set the cache configured element
					resourceCacheConfigType.setConfigured(cacheConfig.isConfigured());
					
					// Only set cacheConfig objects if it is configured
					if (cacheConfig.isConfigured()) {

						if (cacheConfig.isEnabled() != null) {
							resourceCacheConfigType.setEnabled(cacheConfig.isEnabled());
						}
						if (cacheConfig.getExpirationPeriod() != null) {
							// Define the calendar period type
							ResourceCacheCalendarPeriodType calendarPeriod = new ResourceCacheCalendarPeriodType();
							
							Period period = new Period();
							period = period.getCalandarPeriod(cacheConfig.getExpirationPeriod(), "milliseconds");
							calendarPeriod.setPeriod(period.getPeriod());
							calendarPeriod.setCount(period.getCount());
							// Set the expiration period
							resourceCacheConfigType.setExpirationPeriod(calendarPeriod);
						}
						if (cacheConfig.getClearRule() != null) {
							resourceCacheConfigType.setClearRule(cacheConfig.getClearRule().toString());
						}
						
						if (cacheConfig.getRefresh() != null) {
							// Define the resource refresh type
							ResourceCacheRefreshType resourceCacheRefreshType = new ResourceCacheRefreshType();
							
							// Modes: MANUAL or SCHEDULED
							if (cacheConfig.getRefresh().getMode() != null) {
								resourceCacheRefreshType.setMode(cacheConfig.getRefresh().getMode().toString());
							}
							
							// Continue if not null and the mode is SCHEDULED -- no need to print out a schedule if MANUAL
							if (cacheConfig.getRefresh().getSchedule() != null) {
								if (cacheConfig.getRefresh().getMode().toString().equalsIgnoreCase("SCHEDULED")) {
									// Define the resource refresh schedule
									ResourceCacheRefreshScheduleType resourceCacheRefreshScheduleType = new ResourceCacheRefreshScheduleType();
									
									if (cacheConfig.getRefresh().getSchedule().getStartTime() != null) {
										resourceCacheRefreshScheduleType.setStartTime(cacheConfig.getRefresh().getSchedule().getStartTime());
									}

									if (cacheConfig.getRefresh().getSchedule().getMode().toString().equalsIgnoreCase("INTERVAL")) {
										// Define the calendar period type
										ResourceCacheCalendarPeriodType calendarPeriod = new ResourceCacheCalendarPeriodType();

										
										if (cacheConfig.getRefresh().getSchedule().getInterval() != null) {
											Period period = new Period();
											period = period.getCalandarPeriod(cacheConfig.getExpirationPeriod(), "seconds");
											calendarPeriod.setPeriod(period.getPeriod());
											calendarPeriod.setCount(period.getCount());
											// Set the refresh period
											resourceCacheRefreshScheduleType.setRefreshPeriod(calendarPeriod);
										}
									}

									if (cacheConfig.getRefresh().getSchedule().getMode().toString().equalsIgnoreCase("CALENDAR")) {
										// Define the calendar period type
										ResourceCacheCalendarPeriodType calendarPeriod = new ResourceCacheCalendarPeriodType();

										if (cacheConfig.getRefresh().getSchedule().getPeriod() != null) {
											calendarPeriod.setPeriod(cacheConfig.getRefresh().getSchedule().getPeriod().toString());
											calendarPeriod.setCount(cacheConfig.getRefresh().getSchedule().getCount());
											// Set the refresh period
											resourceCacheRefreshScheduleType.setRefreshPeriod(calendarPeriod);
										}
									}
									// set the resource cache refresh schedule
									resourceCacheRefreshType.setSchedule(resourceCacheRefreshScheduleType);										
								}
							}
							// set the resource refresh
							resourceCacheConfigType.setRefresh(resourceCacheRefreshType);
						}
						
						if (cacheConfig.getStorage() != null) {
							// Define the resource storage type
							ResourceCacheStorageType resourceCacheStorageType = new ResourceCacheStorageType();
							
							if (cacheConfig.getStorage().getMode() != null) {
								resourceCacheStorageType.setMode(cacheConfig.getStorage().getMode().toString());
							}
							if (cacheConfig.getStorage().getStorageDataSourcePath() != null) {
								resourceCacheStorageType.setStorageDataSourcePath(cacheConfig.getStorage().getStorageDataSourcePath());
							}
							
							if (cacheConfig.getStorage().getStorageTargets() != null) {
								// Define the storage target type
								ResourceCacheStorageTargetsType entry = new ResourceCacheStorageTargetsType();

								for (TargetPathTypePair storageTarget : cacheConfig.getStorage().getStorageTargets().getEntry()) {
									// Set the storage target entry
									entry.setPath(storageTarget.getPath());
									entry.setTargetName(storageTarget.getTargetName());
									entry.setType(storageTarget.getType().toString());
									resourceCacheStorageType.getStorageTargets().add(entry);
								}
							}
							// Set the resource storage type
							resourceCacheConfigType.setStorage(resourceCacheStorageType );
						}
					}
					// Set the resource cache configuration
					resourceCacheType.setCacheConfig(resourceCacheConfigType );
					
					// Add an XML node if the resource is configured for cache and the user is requesting CONFIGURED
					if (cacheConfig.isConfigured() && getConfigured) {
						// Set basic values for Id, Path and Type					
						resourceCacheType.setId(getTokenId("cache"));

						// Add a new row to the XML for Resource Cache
						resourceCacheModule.getResourceCache().add(resourceCacheType);						
					}
					// Add an XML node if the resource is not configured for cache and the user is requesting NONCONFIGURED
					if (!cacheConfig.isConfigured() && getNonConfigured) {
						// Set basic values for Id, Path and Type
						resourceCacheType.setId(getTokenId("cache"));

						// Add a new row to the XML for Resource Cache
						resourceCacheModule.getResourceCache().add(resourceCacheType);						
					}
				}
			}
		}

		// Generate the XML file
		XMLUtils.createXMLFromModuleType(resourceCacheModule, pathToResourceCacheXML);
	}

	/**
	 * @return the token and numerical Id 
	 * Using an ArrayList, track the various tokens that are passed in and increment a number.
	 * Return the token and the number concatenated together
	 * services
	 * shared
	 * users
	 */
	private String getTokenId(String name) {
		for (int i=0; i < tokenType.size(); i++) {
			if (tokenType.get(i).equalsIgnoreCase(name)) {
				int n = tokenNum.get(i).intValue();
				tokenNum.set(i, Integer.valueOf(++n));
				return name+n;
			}
		}
		tokenType.add(name);
		tokenNum.add(Integer.valueOf(1));
	    return name+1;
	}

	/**
	 * @return the conversion in seconds or milliseconds for a period and count
	 * period [SECOND,MINUTE,HOUR,DAY,WEEK,MONTH,YEAR]
	 * periodCount is measured in either milliseconds or seconds
	 * qualifier is either "milliseconds" or "seconds"
	 * Based on the period count that is passed, calculate what the period is
	 */
	private Long convertPeriodCount(String period, Long periodCount, String qualifier) {

		// Assume "SECOND" as the default
		Long count = periodCount;

		if (period.equalsIgnoreCase("MINUTE")) {
			count = periodCount * 60;
		}
		if (period.equalsIgnoreCase("HOUR")) {
			count = periodCount * 3600;
		}
		if (period.equalsIgnoreCase("DAY")) {
			count = periodCount * 86400;
		}
		if (period.equalsIgnoreCase("WEEK")) {
			count = periodCount * 604800;
		}
		if (period.equalsIgnoreCase("MONTH")) {
			count = periodCount * 2592000;
		}
		if (period.equalsIgnoreCase("YEAR")) {
			count = periodCount * 31536000;
		}
		// if negative then set to 0
		if (count < 0) {
			count = (long) 0;
		}
		// if the qualifier is "milliseconds" multiply by 1000
		if (qualifier.equalsIgnoreCase("milliseconds")) {
			count = count * 1000;
		}		
		return count;
	}

	// period object is used to hold a period and count
	public class Period {
	    public String period = null;
	    public Long count = null;
	    //constructor
	    public Period() {
	    	period = null;
	    	count = null;
	    }
	    public void setPeriod(String u) {
	    	period = u;
	    }
	    public void setCount(Long c) {
	    	count = c;
	    }
	    public String getPeriod() {
	    	return this.period;
	    }
	    public Long getCount() {
	    	return this.count;
	    }
		/**
		 * @return the Calendar period count for the given period [SECOND,MINUTE,HOUR,DAY,WEEK,MONTH,YEAR]
		 * periodCount is measured in either milliseconds or seconds
		 * qualifier is either "milliseconds" or "seconds"
		 * Based on the period count that is passed, calculate what the period is
		 */
		public Period getCalandarPeriod(Long periodCount, String qualifier) {
			// Initialize a new return variable
			Period period = new Period();
			Long seconds = null;
			// When calculating expiration use milliseconds
			if (qualifier.equalsIgnoreCase("milliseconds")) { // periodCount is "milliseconds"
				seconds = periodCount / 1000;
			} else {   // periodCount is "seconds"
				seconds = periodCount;
			}
			//If the the periodCount is not divisible by 60 seconds then assume the period is seconds
			Long remainder = seconds % 60;
			if (remainder > 0) {
				period.setPeriod("SECOND");
				period.setCount(seconds);
				return period;
			}
			if (seconds < 60) { // SECOND
				period.setPeriod("SECOND");
				if (seconds < 0) {
					period.setCount((long) 0);
				} else {
					period.setCount(seconds);
				}
				return period;
			}
			if (seconds >= 60 && seconds < 3600) { // MINUTE
				period.setCount(seconds / 60);
				period.setPeriod("MINUTE");
				return period;
			}
			if (seconds >= 3600 && seconds < 86400) { // HOUR
				period.setCount(seconds / 3600);
				period.setPeriod("HOUR");
				return period;
			}
			if (seconds >= 86400 && seconds < 604800) { // DAY
				period.setCount(seconds / 86400);
				period.setPeriod("DAY");
				return period;
			}
			if (seconds >= 604800 && seconds < 2592000) { // WEEK
				period.setCount(seconds / 604800);
				period.setPeriod("WEEK");
				return period;
			}
			if (seconds >= 2592000 && seconds < 31536000) { // MONTH
				period.setCount(seconds / 2592000);
				period.setPeriod("MONTH");
				return period;
			}
			if (seconds >= 31536000) { // YEAR
				period.setCount(seconds / 31536000);
				period.setPeriod("YEAR");
				return period;
			}		
			return period;
		}
	}
	/**
	 * @return the resourceCacheDAO
	 */
	public ResourceCacheDAO getResourceCacheDAO() {
		if(this.resourceCacheDAO == null){
			this.resourceCacheDAO = new ResourceCacheWSDAOImpl();
		}
		return resourceCacheDAO;
	}

	/**
	 * @param resourceCacheDAO the resourceCacheDAO to set
	 */
	public void setResourceCacheDAO(ResourceCacheDAO resourceCacheDAO) {
		this.resourceCacheDAO = resourceCacheDAO;
	}
}