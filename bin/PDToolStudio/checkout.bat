@echo off
REM ######################################################################
REM # (c) 2014 Cisco and/or its affiliates. All rights reserved.
REM ######################################################################
REM #=======================================================================================
REM # Example Execution Statement:
REM # checkout.bat [CIS-resource-path] [CIS-resource-type] [rollback-revision] [vcs-workspace-project-folder] [vcs-temp-folder]
REM # 
REM # Parameters
REM # ----------
REM # %1 ->  Resource path 			(e.g. /shared/MyFolder/My__View), using file system (encoded) names
REM # %2 ->  Resource type 			(e.g. FOLDER, table, procedure etc.)
REM # %3 ->  Rollback revision  	(e.g. HEAD)
REM # %4 ->  VCS Workspace Folder 	(e.g. C:\Temp\workspaces\workspace_CIS)
REM # %5 ->  VCS Temp Folder 		(e.g. C:\Temp\workspaces\temp_CIS)
REM #=======================================================================================
REM #------------------------------------------
REM # Set constants
REM #------------------------------------------
set SCRIPT1=checkout.bat
set PREFIX1=%SCRIPT1%::execute::
set PREFIX1_ERR=ERROR::%SCRIPT1%::

REM # Print out the Banner
echo. 
echo.---------------------------------------------------------
echo.---                                                   ---
echo.--- Composite PS Promotion and Deployment Tool Studio ---
echo.---                                                   ---
echo.---------------------------------------------------------
echo. 

REM #---------------------------------------------
REM # Set environment variables
REM #---------------------------------------------
call setVars.bat
REM #=======================================
REM # Substitute PROJECT_HOME path
REM #=======================================
REM #   The PD Tool PROJECT HOME is a substituted
REM #   path in order to shorten the path and
REM #   prevent "too long a file name" error
REM #=======================================
if defined SUBSTITUTE_DRIVE ( 
	if EXIST %SUBSTITUTE_DRIVE% subst /D %SUBSTITUTE_DRIVE%
	echo Substitute drive %SUBSTITUTE_DRIVE% for path "%PROJECT_DIR%"
	subst %SUBSTITUTE_DRIVE% "%PROJECT_DIR%"
)

REM #=======================================
REM # Validate Paths exist
REM #=======================================
if NOT EXIST "%PROJECT_HOME%" (
   echo.Execution Failed::PROJECT_HOME does not exist: %PROJECT_HOME%
   ENDLOCAL
   exit /B 1
)
if NOT EXIST "%JAVA_HOME%" (
   echo.Execution Failed::JAVA_HOME does not exist: %JAVA_HOME%
   ENDLOCAL
   exit /B 1
)
echo.PROJECT_HOME=%PROJECT_HOME%
echo.JAVA_HOME=%JAVA_HOME%
REM #=======================================
REM # Display Licenses
REM #=======================================
echo. 
echo.------------------------------------------------------------------
echo.------------------------ PD Tool Licenses ------------------------
echo.------------------------------------------------------------------
type "%PROJECT_HOME%\licenses\Composite_License.txt"
echo. 
type "%PROJECT_HOME%\licenses\Project_Specific_License.txt"
echo. 

REM #=======================================
REM # Set DeployManager Environment Variables
REM #=======================================
set DEPLOY_CLASSPATH="%PROJECT_HOME%\dist\*;%PROJECT_HOME%\lib\*"
set ENDORSED_DIR=%PROJECT_HOME%\lib\endorsed
set DEPLOY_MANAGER=com.cisco.dvbu.ps.deploytool.DeployManagerUtil
set CONFIG_LOG4J=-Dlog4j.configuration="file:%PROJECT_HOME%\resources\config\log4j.properties" 
set CONFIG_ROOT=-Dcom.cisco.dvbu.ps.configroot="%PROJECT_HOME%\resources\config"

REM #------------------------------------------
REM # Set incoming arguments
REM #------------------------------------------
SET ResPath=%1
SET ResType=%2
SET Revision=%3
SET Workspace=%4
SET VcsTemp=%5

REM # Convert Revision to upper case
if "%Revision%" == "head" set Revision=HEAD
if "%Revision%" == "Head" set Revision=HEAD

REM #---------------------------------------------
REM # Display Arguments
REM #---------------------------------------------
echo.......................................
echo.%SCRIPT1% Arguments:
echo.......................................
echo.Script Path=%0
echo.Resource Path=%ResPath%
echo.Resource Type=%ResType%
echo.Revision=%Revision%
echo.Workspace=%Workspace%
echo.Temp Folder=%VcsTemp%

REM #------------------------------------------
REM # Perform argument validations
REM #------------------------------------------
echo.......................................
echo.Perform argument validations:
echo.......................................

if not defined ResPath (
   echo.%PREFIX1_ERR%ResPath has no value. Error=111.
   exit /B 111
)
if not defined ResType (
   echo.%PREFIX1_ERR%ResType has no value. Error=112.
   exit /B 112
)
if not defined Workspace (
   echo.%PREFIX1_ERR%Workspace has no value.. Error=113.
   exit /B 113
)
if not defined VcsTemp (
   echo.%PREFIX1_ERR%VcsTemp has no value.. Error=115.
   exit /B 115
)
if not defined Revision (
   echo.%PREFIX1_ERR%Revision has no value.  Set to HEAD or a revision # [e.g. 1]. Error=124.
   exit /B 124
)
echo.Arguments are valid.

REM #---------------------------------------------
REM # Begin Processing
REM #---------------------------------------------

REM #***********************************************
REM # Invoke: DeployManagerUtil vcsStudioCheckout
REM #***********************************************
set JAVA_ACTION=vcsStudioCheckout
set COMMAND="%JAVA_HOME%\bin\java" %JAVA_OPT% -cp  %DEPLOY_CLASSPATH% %CONFIG_ROOT% %CONFIG_LOG4J% -Djava.endorsed.dirs="%ENDORSED_DIR%" -DPROJECT_HOME="%PROJECT_HOME%" -DCONFIG_PROPERTY_FILE=%CONFIG_PROPERTY_FILE% %DEPLOY_MANAGER% %JAVA_ACTION% %ResPath% %ResType% %Revision% %Workspace% %VcsTemp%
set PRCOMMAND=%COMMDAND%

REM #=======================================
REM # Execute the script
REM #=======================================
REM Escape (") in the COMMAND with ("") before printing
echo.%PREFIX1% 
echo.%PREFIX1%-- COMMAND: %JAVA_ACTION% ----------------------
echo.%PREFIX1% 
echo.%COMMAND%
echo.%PREFIX1% 
echo.%PREFIX1%-- BEGIN OUTPUT ------------------------------------
echo.%PREFIX1% 

REM #------------------------------------------------------------------------
REM # Execute the command line CIS script
REM #------------------------------------------------------------------------
call %COMMAND%
set ERROR=%ERRORLEVEL%
if %ERROR% NEQ 0 (
   echo.%PREFIX1_ERR% Script %SCRIPT% Failed. Abnormal Script Termination. Exit code is: %ERROR%.		
   ENDLOCAL
   exit /B %ERROR%
)

REM #=======================================
REM # Successful script completion
REM #=======================================
echo.%PREFIX1% 
echo.%PREFIX1%-------------- SUCCESSFUL SCRIPT COMPLETION [%SCRIPT% %CMD%] --------------
echo.%PREFIX1% End of script." 
ENDLOCAL
exit /B 0