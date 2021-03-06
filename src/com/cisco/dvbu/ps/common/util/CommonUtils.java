/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 */
package com.cisco.dvbu.ps.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.zip.Checksum;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;

import com.cisco.dvbu.ps.common.CommonConstants;
import com.cisco.dvbu.ps.common.exception.ApplicationException;
import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.common.exception.ValidationException;
import com.cisco.dvbu.ps.deploytool.dao.ServerAttributeDAO;
import com.cisco.dvbu.ps.deploytool.dao.wsapi.ServerAttributeWSDAOImpl;
import com.compositesw.common.security.CompositeSecurityException;
import com.compositesw.common.security.EncryptionManager;
import com.compositesw.ps.utils.repository.CisPathQuoter;
import com.compositesw.ps.utils.repository.CisPathQuoterException;
import com.compositesw.services.system.util.common.Attribute;
import com.compositesw.services.system.util.common.AttributeList;

// -- CommonUtils to avoid confusion with Ant Utils 
public class CommonUtils {
	

	public static HashMap<String, String> assembleArgs(String[] args) throws CompositeException {
		
		HashMap<String, String> retval = new HashMap<String, String>();
		
		if (args.length % 2 != 0) {
			throw new CompositeException("Invalid number of arguments supplied.");
		}
		
		String argName = null;
		String argValue = null;
		for (int i=0; i < args.length; i++) {

			if ((i+1) % 2 == 0) {
				argValue = args[i];
				if (argValue.startsWith("-")) {
					throw new CompositeException("Invalid arguments - values may not be preceeded with a \"-\".");
				} else {
					retval.put(argName, argValue);
				}
			} else {
				argName = args[i];
				if (!argName.startsWith("-")) {
					throw new CompositeException("Invalid arguments - argument names must start with \"-\".");
				} else {
					argName = argName.substring(1);
					if (argName.startsWith("-")) {
						throw new CompositeException("Invalid arguments - argument names must start with a single \"-\".");
					}
				}	
			}
		}
		
		return retval;

	}
	
	public static boolean isTrue(String bool) {
		return bool.equalsIgnoreCase("TRUE");
	}
	
	@SuppressWarnings("rawtypes")
	public static void setPropertiesFromArgs(HashMap<String, String> args, Object object) throws CompositeException {
		
		// -- properties for the wsapi call must be set from the incoming properties,
		//    whether from main or from Ant
		Method[] methods = object.getClass().getMethods();
		for (Method method: methods) {
			
			String methodName = method.getName();
			Class[] parameterTypes = method.getParameterTypes();

			if (methodName.startsWith("set") && parameterTypes.length == 1) {
				if (parameterTypes[0].equals(String.class)) {
					
					// -- strip "set"
					String hashKey = methodName.substring(3);
					// -- initial lower case
					hashKey = hashKey.substring(0, 1).toLowerCase() +
						hashKey.substring(1);
					
					if (args.containsKey(hashKey)) {
						if (args.get(hashKey) != null) {
							String value = args.get(hashKey);
							
							// -- now set
							try {
								
								// -- might want this output
								//System.out.println("Setting property '" + 
								//		hashKey + "' to '" + value + "'");
								
								method.invoke(object, value);
							} catch (IllegalArgumentException e) {
								throw new CompositeException(e);
							} catch (IllegalAccessException e) {
								throw new CompositeException(e);
							} catch (InvocationTargetException e) {
								throw new CompositeException(e);
							}
							
						} else {
							
							System.out.println("Property '" + 
									hashKey + "' is null");
						}
					}
					
				}
			}
		
			// -- for (Method method: methods) {
		}
		
	}
	
	public static HashMap<String, String> assembleArgs(Object object) throws CompositeException {
		
		HashMap<String, String> retval = new HashMap<String, String>();
		
		Method[] methods = object.getClass().getMethods();
		for (Method method: methods) {
			String methodName = method.getName();
			Type returnType = method.getReturnType();
			if (methodName.startsWith("get") && returnType.equals(String.class)) {
				try {
					// -- strip "get"
					String argName = methodName.substring(3);
					// -- initial lower case
					argName = argName.substring(0, 1).toLowerCase() +
						argName.substring(1);
					retval.put(argName, (String)method.invoke(object));
				} catch (IllegalArgumentException e) {
					throw new CompositeException(e);
				} catch (IllegalAccessException e) {
					throw new CompositeException(e);
				} catch (InvocationTargetException e) {
					throw new CompositeException(e);
				}
			}
		}
			
		return retval;
		
	}
	
	public static boolean stringIsValidBoolean(String bool) {
		if (!(bool.equalsIgnoreCase("TRUE") || bool.equalsIgnoreCase("FALSE"))) {
			return false;
		}
		return true;
	}
	
	public static XMLGregorianCalendar getXMLGregorianCalendarFromTimestamp (String timestamp) throws CompositeException {
		
		// -- e.g. "2011-02-10T14:18:42.000Z"
		
		timestamp = timestamp.replace("T", " ");
		timestamp = timestamp.replace("Z", "");
		
		Date date = Timestamp.valueOf(timestamp);
		
		DatatypeFactory df;
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new CompositeException(e);
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		XMLGregorianCalendar retval = df.newXMLGregorianCalendar(cal); 
		
		return retval;
		
	}
  
	public static String getFileAsString(String file) throws CompositeException {
		StringBuilder stringBuilder = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
		} catch (FileNotFoundException e) {
			throw new CompositeException(e.getMessage(),e);
		} catch (IOException e) {
			throw new CompositeException(e.getMessage(),e);
		}
		return stringBuilder.toString();
	}
   
    public static String getStackTraceAsString(Throwable throwable) {
        Writer stackTraceWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTraceWriter);
        throwable.printStackTrace(printWriter);
        return stackTraceWriter.toString();
      }	
	
	public static String encrypt(String unencrypted) {
		
		String retval = null; 

		try {
			if(unencrypted != null && !unencrypted.startsWith("Encrypted:")){
				retval = "Encrypted:"+EncryptionManager.encrypt(unencrypted);
			}else{
				retval = unencrypted;
			}
		} catch (CompositeSecurityException e) {
			CompositeLogger.logException(e, e.getMessage());
			throw new CompositeException(e);
		}
		
		return retval;
		
	}	
	
	public static String decrypt(String encrypted) {
		
		String retval = null;
		try {
			if(encrypted != null && encrypted.trim().length() > 25 && encrypted.startsWith("Encrypted:")){
				retval = EncryptionManager.decrypt(encrypted.substring(10));
			}else{
				retval = encrypted;
			}
		} catch (CompositeSecurityException e) {
			CompositeLogger.logException(e, e.getMessage());
			throw new CompositeException(e);
		}
		
		return retval;
	}

	// Check to see if a property exists in the encrypt property list
	// Encrypt property list="VCS_PASSWORD encryptedPassword PASSWORD_STRING SVN_VCS_PASSWORD, P4_VCS_PASSWORD CVS_VCS_PASSWORD TFS_VCS_PASSWORD GIT_VCS_PASSWORD"
	public static boolean existsEncryptPropertyList(String property) 
	{
		boolean exists = false;
		StringTokenizer st = new StringTokenizer(CommonConstants.encryptPropertiesList, " ");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (property.equalsIgnoreCase(token)) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	// Test for file exists
	public static boolean fileExists(String filePath) {
		File file=new File(filePath);
		boolean exists = file.exists();
		return exists;
	}
	// Make directories
	public static boolean mkdirs(String filePath) {
		File file=new File(filePath);
		boolean result = false;
		if (!file.exists()) {
			result = file.mkdirs();
		}
		return result;
	}
	
	 /**
	  Remove a file.
	*/
	public static boolean removeFile(String filePath) {
		if (filePath != null) {
			File file=new File(filePath);
			if (file.exists())
				file.delete();
			return true;
		}
		return false;
	}

	 /**
	  Get directory from full file path
	*/
	public static String getDirectory(String filePath) {
		String dir = null;
		if (filePath != null) {
			if (filePath.contains("\\")) {
				dir = filePath.substring(0, filePath.lastIndexOf("\\"));
			} else if (filePath.contains("/")) {
				dir = filePath.substring(0, filePath.lastIndexOf("/"));				
			} else {
				dir = filePath;
			}
		}
		return dir;
	}


	
	 /**
	  Remove a directory and all of its contents.

	  The results of executing File.delete() on a File object
	  that represents a directory seems to be platform
	  dependent. This method removes the directory
	  and all of its contents.

	  @return true if the complete directory was removed, false if it could not be.
	  If false is returned then some of the files in the directory may have been removed.

	*/
	public static boolean removeDirectory(File directory) {

	  // System.out.println("removeDirectory " + directory);

	  if (directory == null)
	    return false;
	  if (!directory.exists())
	    return true;
	  if (!directory.isDirectory())
	    return false;

	  String[] list = directory.list();

	  // Some JVMs return null for File.list() when the
	  // directory is empty.
	  if (list != null) {
	    for (int i = 0; i < list.length; i++) {
	      File entry = new File(directory, list[i]);

	      //        System.out.println("\tremoving entry " + entry);

	      if (entry.isDirectory())
	      {
	        if (!removeDirectory(entry))
	          return false;
	      }
	      else
	      {
	        if (!entry.delete())
	          return false;
	      }
	    }
	  }

	  return directory.delete();
	}

	// Get the current date timestamp as a string
	public static String getCurrentDateAsString(String timestampFormat) {
		Format formatter;
		Date date = new Date();
		formatter = new SimpleDateFormat(timestampFormat);
		return formatter.format(date).toString();
	}

	// Generate unique file name based on a date
	public static String getUniqueFilename(String filename, String extension) {
		return filename+"_"+getCurrentDateAsString("yyyy_MM_dd_HH_mm_ss_SSS")+"."+extension;
	}

	// Remove slash "/" or "\" at end of filename
	public static String setCanonicalPath(String filename) {
		
		if (filename != null && filename.length() > 0) {
			// Replace double forward slashes // with a single /
			filename = filename.replaceAll("//","/");
	
			// Replace double back slashes \\ with a single /
			filename = filename.replaceAll("\\\\","/");
	
			String lastChar = filename.substring(filename.length()-1, filename.length());
			if (lastChar.equalsIgnoreCase("/"))  {
				filename = filename.substring(0, filename.length()-1);
			}
		}
		return filename;
	}

	/**
	 * Pad a number to the left for totalPadAmount using padChar and return as a string
	 * @param num - the number to pad as a string
	 * @param totalPadAmount - the amount to pad
	 * @param padChar - the character to pad with e.g. " "
	 * @return String
	 */
	public static String padCount(int num, int totalPadAmount, String padChar) {
		// Pad a string with spaces starting on the left
		String padString = String.valueOf(num);
		if (padString.length() < totalPadAmount) {
			String pad = "";
			for (int i=padString.length(); i < totalPadAmount; i++) {
				pad = pad + padChar;
			}
			padString = pad + padString;
		}
		return padString;
	}
	
	/**
	 * Pad to the left of a string for totalPadAmount using padChar
	 * @param str - string to pad
	 * @param totalPadAmount - the amount to pad
	 * @param padChar - the character to pad with e.g. " "
	 * @return String
	 */
	public static String lpad(String str, int totalPadAmount, String padChar) {
		// Pad a string with spaces starting on the left
		String padStr = str;
		if (padStr.length() < totalPadAmount) {
			String pad = "";
			for (int i=padStr.length(); i < totalPadAmount; i++) {
				pad = pad + padChar;
			}
			padStr = pad + padStr;
		}
		return padStr;
	}
	
	/**
	 * Pad to the right of a string for totalPadAmount using padChar
	 * @param str - string to pad
	 * @param totalPadAmount - the amount to pad
	 * @param padChar - the character to pad with e.g. " "
	 * @return String
	 */
	public static String rpad(String str, int totalPadAmount, String padChar) {
		// Pad a string with spaces starting on the left
		String padStr = str;
		if (padStr.length() < totalPadAmount) {
			String pad = "";
			for (int i=padStr.length(); i < totalPadAmount; i++) {
				pad = pad + padChar;
			}
			padStr = padStr + pad;
		}
		return padStr;
	}
	
	/**
	 * Create a file with passed in file path
	 * Overwrite the file if it already exists.
	 * Create the sub-directories if they do not exist.

	 * @param file - file name with path
	 * @param content - the content to write to the file
	 */
	public static void createFileWithContent(String file, String content) {
		
		if (file == null || file.length() <= 0) {
			throw new CompositeException("File path is not defined.");
		}
		// Create the sub-directories if they do not already exist
		String fileDir = getDirectory(file);
		if (fileDir != null && !fileExists(fileDir)) {
			mkdirs(fileDir);
		}
		// Write to the file
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(content);
			out.flush();
			out.close();
		} catch (IOException e) {
			CompositeLogger.logException(e, "Could not create file " + file);
			throw new ValidationException(e.getMessage(), e);
		}
	}
	
	/**
	 * Append passed in content to the file.
	 * Create file if it does not exist.  
	 * Create the sub-directories if they do not exist.
	 * 
	 * @param file - file name with path
	 * @param content - the content to append to the file
	 */
	public static void appendContentToFile(String file, String content) {

		if (file == null || file.length() <= 0) {
			throw new CompositeException("File path is not defined.");
		}

		// Create the sub-directories if they do not already exist
		String fileDir = getDirectory(file);
		if (fileDir != null && !fileExists(fileDir)) {
			mkdirs(fileDir);
		}
		// Write to the file
		BufferedWriter bw = null;
		try {
			if (file != null && file.trim().length() > 0) {
				bw = new BufferedWriter(new FileWriter(file, true));
				if (content != null && content.trim().length() > 0) {

					bw.write(content);
					bw.newLine();
					bw.flush();
				}
			}
		} catch (IOException e) {
			CompositeLogger.logException(e, "Could not append to file " + file);
			throw new ValidationException(e.getMessage(), e);
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					// just ignore it
				}
		} // end try/catch/finally
	}
	
	/**
	 * compareFiles - compare two files based on checksum.
	 * @modified Jan. 15 2014 by SST. 
	 * @comment	 before the change on Jan.15 this method returned false when both files being compared are empty;
	 * 			 so it was decided to change that initial approach, because having both files 0 length is a valid 
	 * 			 scenario.
	 * 			 another change is to use row-based checksums instead of checksum of the whole file; so now instead of 
	 * 			 fileChecksum, method fileChecksumByRow is used.  
	 * 
	 * @param filePath1 - file path for file1
	 * @param filePath2 - file path for file2
	 * @return result - comparison: true=files are the same, false=files are different
	 */
	public static boolean compareFiles(String filePath1, String filePath2) throws CompositeException {
		boolean result = false;
		
		BigInteger sum1 = new BigInteger("0");
		BigInteger sum2 = new BigInteger("0");
		
		try {
			if (fileExists(filePath1)) 
				sum1 = fileChecksumByRow(filePath1);
			else 
				throw new ApplicationException("File not found at path: "+ filePath1);
			if (fileExists(filePath1)) 
				sum2 = fileChecksumByRow(filePath2);
			else 
				throw new ApplicationException("File not found at path: "+ filePath2);

			if (
					sum1 != null  &&   // object sum1 is not null 
					sum2 != null  &&   // object sum2 is not null 
					(sum1.compareTo(sum2) == 0)  									 // sum1 and sum2 contain the same value. 
				)
			{
				result = true;
			}
		} catch (Exception e) {
			throw new CompositeException(e);
		}
		return result;
	}
	
	/**
	 * Returns a CRC32 checksum of a file as a whole (as opposed to sum of checksums of all lines/rows).
	 * 
	 * @param filePath		file name with full path
	 * @return				checksum value for the input file
	 * @throws IOException
	 */
    public static long fileChecksum(String filePath) throws IOException {
      
    	long checkSumValue = 0L;
    	
    	FileInputStream file = new FileInputStream(filePath);
  
		CheckedInputStream check = new CheckedInputStream(file, new CRC32());
	  
		BufferedInputStream in = new BufferedInputStream(check);
	  
		while (in.read() != -1) {
	      // Read file in completely
		}
		checkSumValue = check.getChecksum().getValue();
//		System.out.println("fileChecksum(): checkSumValue = " + checkSumValue);
		return checkSumValue;
	}
    
    /**
	 * Returns a sum of CRC32 checksums of all lines/rows in a file.
	 * This method is used to compare files with the same lines/rows, which may be in different order, in which case we
	 * still want to consider them equal (from the point of view of containing the same data)
	 * In such case this method will return the same result.
	 * 
	 * This is useful when the file contains results of a database query and we need to compare
	 * results of two queries that may return the same data but in different order.
	 * 	  
	 * @author 				SST
	 * @param filePath		file name with full path
	 * @return				sum of checksums of each line(row) from the input file
	 * 						The type of this value could be long for files up to probably several GB in size.
	 * 						BigInteger was chosen in case even bigger files are used.
	 * @throws IOException
	 */
    public static BigInteger fileChecksumByRow(String filePath) throws IOException {
      
    	BigInteger sumOfcheckSumValues = new BigInteger("0");
    	long currentLineCheckSumValue = 0L;
    	Checksum checksum = new CRC32();
    	
    	BufferedReader br = new BufferedReader(new FileReader(filePath));
    	String line;
    	
//    	System.out.println("currentLineCheckSumValue: ");
    	while ((line = br.readLine()) != null) {
    		// Read one line at a time

    		byte bytes[] = line.getBytes();
    		checksum.reset();
    		checksum.update(bytes, 0, bytes.length);
    
    		currentLineCheckSumValue = checksum.getValue();	
//    		System.out.println(currentLineCheckSumValue);

    		sumOfcheckSumValues = sumOfcheckSumValues.add(BigInteger.valueOf(currentLineCheckSumValue));
    	}
    	br.close();
//    	System.out.println("fileChecksumByRow(): sumOfcheckSumValues = " + sumOfcheckSumValues);		
		return sumOfcheckSumValues;
	}

	/* Extract and resolve variables [$VAR or %VAR%] from an argument string
	 * 
	 * @param - prefix (method name) is prepended to any info or error message.
	 * @param - arg - the value to evaluate and resolve variables for 
	 * @param - propertyFile - the name of the property file located in the /resources/config directory
	 *    if left null, CisDeployTool will look for an environment variable PROPERTY_FILE to get the value.
	 *    if null and no PROPERTY_FILE set, it will default to CommonConstants.propertyFile
	 * @param - fixPathSeparator - true or false.  
	 *            true=Perform the fix on path-type variables by replacing double "//" with single "/" or double "\\" with single "\".
	 *                 It is only recommended to set this to true when applying to path-oriented variables.
	 *            false=Do not perform the fix for path separator.    
	 *                 It is recommended to set to false when fields values are not path-oriented such as URLs which 
	 *                 will contain "//" and are valid. 
	 *    
	 * Use cases supported:
	 * Single $ separator followed by forward slash: 		$MODULE_HOME/servers.xml
	 * Percent % separator on either side of the variable: 	%MODULE_HOME%/servers.xml 
	 * Single % separator: 									%MODULE_HOME/servers.xml 
	 * Single $ separator followed by a % separator: 		$MODULE_HOME%/servers.xml
	 * Single % separator followed by a $ separator: 		%MODULE_HOME$/servers.xml
	 * Variable at the end of the line with no separator following the variable:  $VAR or %VAR
	 */
	public static String extractVariable(String prefix, String arg, String propertyFile, Boolean fixPathSeparator) {
		
		// Determine if the property file is set in the environment otherwise use the default property file
		String defaultPropertyFile = CommonConstants.propertyFile;
		if (propertyFile == null || propertyFile.length() == 0) {
			propertyFile = getFileOrSystemPropertyValue(defaultPropertyFile, "CONFIG_PROPERTY_FILE");
			if (propertyFile == null || propertyFile.length() == 0) {
				propertyFile = defaultPropertyFile;
			}
		}
		if (arg == null) {
			arg = "";
		}
		String resolvedArg = arg.trim();
		String propertyVal = null;
		String replaceProperty = null;
		boolean resolvedVariables = false;
		
		/* The separator list is a list of single characters that may be encountered within a string being parsed
		 *   mtinius: 2012-01-23: added ":" and "@" as separators
		 *   mtinius: 2013-02-13: added "'" as a separator for use case 11. below
		 *   that will serve as a natural separator in the text. Examples are listed below
		 *      1. / - $VCS_WORKSPACE_HOME/
		 *      2. $ - $VCS_TYPE$_cisVcsTemp
		 *      3. \ - $VCS_WORKSPACE_HOME\
		 *      4. . - $VAR.log
		 *      5. " - "$VAR"
		 *      6. $ - $VCS_TYPE$_cisVcsTemp
		 *      7. % - %VCS_TYPE%_cisVcsTemp
		 *      8.   - $VAR1 $VAR2
		 *      9. : - :pserver:$VCS_USERNAME:$VCS_PASSWORD@kauai:2401/home/cvs
		 *     10. @ - :pserver:$VCS_USERNAME:$VCS_PASSWORD@kauai:2401/home/cvs
		 *     11. WHERE status='$STATUS' (for query string from RegressionModule)
		 */
		String separatorList = "\\/.\"$% :@'";

		/*
		 *  Handle the use case to escape an argument containing $$ or %%
		 *    Convert $$ into "!D!O!L!L!A!R!-#D#O#L#L#A#R#"
		 *    Convert %% into "!P!E!R!C!E!N!T!-#P#E#R#C#E#N#T#"
		 *    
		 *    This is not quite orthodox but it was quick and easy.  
		 *    Just as long as this pattern never shows up in the actual text it will all work. Playing the odds.
		 */
		resolvedArg = argumentEscape("escape", resolvedArg);
		
		// Loop over the argument text until all variables are resolved - this gets repeated because variables may contain other variables.
		boolean variableSepFound = true;
		while (variableSepFound) {
			
			// Determine if this argument string contains a valid separator of $ or %
			if (resolvedArg.contains("$") || resolvedArg.contains("%")) {
				
				// Initialize variables each time through the loop
				String property = null;
				boolean endSepFound = false;
				int begSep = -1;
				int endSep = -1;

				// Parse variables beginning with $ or %
				if (resolvedArg.contains("$") || resolvedArg.contains("%")) {
					// Determine if the $ or % is the first separator found
					int begSep1 = resolvedArg.indexOf("$");
					int begSep2 = resolvedArg.indexOf("%");
					if (begSep1 > -1 && begSep2 > -1) {
						if (begSep1 < begSep2) {
							begSep = begSep1;
						} else {
							begSep = begSep2;
						}
					} else if (begSep1 > -1) {
						begSep = begSep1;
					} else {
						begSep = begSep2;
					}

					for (int i = begSep+1; i < resolvedArg.length() && !endSepFound; i++)  {
						// Extract the character from the argument text
						String ch = resolvedArg.substring(i, i+1);
						// Determine if the separator list contains the character from the argument string (if yes then this is the end of this variable)
						if (separatorList.contains(ch)) {
							endSep = i;	
							property = resolvedArg.substring(begSep+1, endSep);
							// Extract replacement variable differently depending on whether the end separator is a $ and % or other
							// SeparatorList=$ %
							if (ch.equals("$") || ch.equals("%")) {
								// Include the end separator when extracting
								replaceProperty = resolvedArg.substring(begSep, endSep+1);
							} else { //SeparatorList=/ \ . "
								// Do not include the end separator when extracting
								replaceProperty = resolvedArg.substring(begSep, endSep);								
							}
							endSepFound = true;
						}
					}
					if (!endSepFound) {
						property = resolvedArg.substring(begSep+1, resolvedArg.length());
						replaceProperty	= resolvedArg.substring(begSep, resolvedArg.length());
						endSepFound = true;
					}

					if (endSepFound) {
						// First look in the property file and then look in the System Environment for the property
						propertyVal = getFileOrSystemPropertyValue(propertyFile, property);
						// Handle the use case to escape an argument containing $$ or %%
						propertyVal = argumentEscape("escape", propertyVal);

						// Property was found
						if (propertyVal != null) 
						{	
							// Replace Variables with the retrieved property value (note: variables may contain other variables)
							resolvedArg = resolvedArg.replaceAll(Matcher.quoteReplacement(replaceProperty), Matcher.quoteReplacement(propertyVal));
							resolvedVariables = true;
							
						} else {
							// Property was not found in either the property file or System environment
							throw new ApplicationException("Error parsing property file="+propertyFile+"  Property=["+property+" was not found.  Invoked from method="+prefix);						
						}
					}
				}
				
			} else {
				// No more variables found so exit the loop
				variableSepFound = false;
			}
		}
		if (resolvedVariables && fixPathSeparator) {
			
			String resolvedArgPrefix = null, resolvedArgSuffix = null;
			
			// We have to preserve the beginning // and \\ of the path for the network paths to work
			if (resolvedArg.startsWith("//") || resolvedArg.startsWith("\\\\")) {
				resolvedArgPrefix = resolvedArg.substring(0,2);
				if (resolvedArgPrefix.equals("\\\\")) {
					resolvedArgPrefix = "//";
				}
				resolvedArgSuffix = resolvedArg.substring(2);
				
				//LogFactory.getLog(CommonUtils.class).info("ResolvedArg1: " + resolvedArg);
			
				// Replace double forward slashes // with a single /
				resolvedArgSuffix =  resolvedArgSuffix.replaceAll("//","/");

				// Replace double back slashes \\ with a single /
				resolvedArg = resolvedArgPrefix + resolvedArgSuffix.replaceAll("\\\\","/");
				//LogFactory.getLog(CommonUtils.class).info("ResolvedArg2: " + resolvedArg);
			} else {
				//LogFactory.getLog(CommonUtils.class).info("ResolvedArg3: " + resolvedArg);
				// Replace double forward slashes // with a single /
				resolvedArg =  resolvedArg.replaceAll("//","/");

				// Replace double back slashes \\ with a single /
				resolvedArg = resolvedArg.replaceAll("\\\\","/");
				//LogFactory.getLog(CommonUtils.class).info("ResolvedArg4: " + resolvedArg);
			}
		}

		// Handle the use case to unescape an argument containing $$ or %%
		resolvedArg = argumentEscape("unescape", resolvedArg);

		return resolvedArg;
	}	
	
	/**
	 * Handle the escaping and un-escaping of an argument
	 * Perform this for $$ and %% found in arguments.
	 * 
	 * @param action - "escape" or "unescape"
	 * @param argument - the argument to act upon 
	 */
	private static String argumentEscape(String action, String argument) {
		
		/*
		 * Handle the use case where there is an escaped $$ where the $ is part of the argument and not a variable
		 *   the intended outcome
		 *     VCS_REPOSITORY_URL=http://hostname:8080/$/Project/
		 *   escape the single $ with $$
		 *     VCS_REPOSITORY_URL=http://hostname:8080/$$/Project/
		 *   what gets returned is this
		 *     VCS_REPOSITORY_URL=http://hostname:8080/$/Project/
		 *     
		 *    This is not quite orthodox but it was quick and easy.  
		 *    Just as long as this pattern never shows up in the actual text it will all work. Playing the odds.
		 */
		String replaceDollarProperty = "!D!O!L!L!A!R!-#D#O#L#L#A#R#";
		/*
		 * Handle the use case where there is an escaped %% where the % is part of the argument and not a variable
		 *   the intended outcome
		 *     <query>SELECT * FROM CAT1.SCH2.ViewSales WHERE ProductName like 'Mega%'</query>
		 *   escape the single % with %%
		 *     <query>SELECT * FROM CAT1.SCH2.ViewSales WHERE ProductName like 'Mega%%'</query>
		 *   what gets returned is this
		 *     <query>SELECT * FROM CAT1.SCH2.ViewSales WHERE ProductName like 'Mega%'</query>
		 *     
		 *    This is not quite orthodox but it was quick and easy.  
		 *    Just as long as this pattern never shows up in the actual text it will all work. Playing the odds.
		 */
		String replacePercentProperty = "!P!E!R!C!E!N!T!-#P#E#R#C#E#N#T#";	

		// Perform the action to escape $$ or %%
		if (action.equalsIgnoreCase("escape")) {
			
			 //ESCAPE $$: Handle the use case where there is an escaped $$ where the $ is part of the argument and not a variable
			if (argument.contains("$$")) {
				// Replace Variables with the retrieved property value (note: variables may contain other variables)
				argument = argument.replaceAll(Matcher.quoteReplacement("$$"), Matcher.quoteReplacement(replaceDollarProperty));
			}

			// ESCAPE %%: Handle the use case where there is an escaped %% where the % is part of the argument and not a variable
			if (argument.contains("%%")) {
				// Replace Variables with the retrieved property value (note: variables may contain other variables)
				argument = argument.replaceAll(Matcher.quoteReplacement("%%"), Matcher.quoteReplacement(replacePercentProperty));
			}
			
		// Perform the action to unescape $$ to a single $ or %% to a single %
		} else {
			
			 //UNESCAPE $$: Handle the use case where there is an escaped $$ where the $ is part of the argument and not a variable
			if (argument.contains(replaceDollarProperty)) {
				// replace the unorthodox text with a single $ sign
				// Replace Variables with the retrieved property value (note: variables may contain other variables)
				argument = argument.replaceAll(Matcher.quoteReplacement(replaceDollarProperty), Matcher.quoteReplacement("$"));
			}

			// UNESCAPE %%: Handle the use case where there is an escaped %% where the % is part of the argument and not a variable
			if (argument.contains(replacePercentProperty)) {
				// replace the unorthodox text with a single % sign
				// Replace Variables with the retrieved property value (note: variables may contain other variables)
				argument = argument.replaceAll(Matcher.quoteReplacement(replacePercentProperty), Matcher.quoteReplacement("%"));
			}
		}
		return argument;
	}
	
	// Get a property from the property file specified or the System environment
	public static String getFileOrSystemPropertyValue(String propertyFile, String property) {
		String propertyVal = null;
		try {
			// mtinius: 2012-01-20:  
			//		Modified to get the JVM environment before the property file.
			//		This change was put in place so that VCS Module could set the VCS Environment variables that it
			//		retrieved from the VCS Module XML and still have VCS work within the context of the existing code base.
			//		Therefore, if no JVM property was found then default to looking in the property file.
			
			// Try getting the property from the JVM environment either set by -D when PDTool was invoked or by PDTool using System.setProperty()
			if (propertyVal == null) {
				propertyVal = System.getProperty(property);
			}

			// Try getting the property from the property file if the propertyFile is not null
			if (propertyFile != null && propertyVal == null) {
				// Get the property
				propertyVal = PropertyManager.getInstance().getProperty(propertyFile, property);	
			}

			// Try getting the property from a System Environment variable
			if (propertyVal == null) {
				propertyVal = System.getenv(property);
			}
			
			// No property was found so initialize to non-null value
			if (propertyVal == null || propertyVal.length() == 0) {
				propertyVal = "";
			}
			
			return propertyVal;
			
		} catch (Exception e) {
			throw new ApplicationException("Error parsing property file=["+propertyFile+"]  Property=["+property+"] was not found.");						
		}
	}
	

	// Return an argument list extracted from a passed in argument string and a separator.
	public static List<String> getArgumentsList(List<String> argList, boolean initArgList, String arguments, String separator){

		if (initArgList) {
			argList = new ArrayList<String>();
		}
		if(arguments != null && arguments.trim().length() > 0){
			StringTokenizer st = new StringTokenizer(arguments, separator);
			// Loop through the space separated tokens and add them to the argument list
			while(st.hasMoreTokens()){
				argList.add(st.nextToken().trim());
			}
			return argList;
		}
		return null;
	}
	
	// Mask the property value following "maskProp" with the value of "maskValue".  Case sensitivity is ignored
	// This method is used when displaying output to the log file or system.out
	public static String maskCommand(String arguments) {

		if (arguments != null && arguments.length() > 0) {
			String maskValue = "********";
			
			// Mask out passwords for Perforce
			// Command Format: 			[:method:][[user][:password]@]hostname[:[port]]/repository_path		
			// Example (Mask): 			:pserver:user1:password@remotehost:2401/home/cvs
			// Example (Mask): 			:pserver:user1:password@remotehost:/home/cvs
			// Example (Do not mask): 	:pserver:user1@remotehost:/home/cvs 
			if (arguments.contains(":pserver:")) {	
				int colonCount = 0;
				int symbol_ampersand = arguments.indexOf("@");
				// Count the number of : prior to 
				boolean exitLoop = false;
				for (int i=0; i < symbol_ampersand && !exitLoop; i++) {
					String ch = arguments.substring(i, i+1);
					if (ch.equalsIgnoreCase(":")) {
						colonCount++;
					}
					if (colonCount == 3) {
						arguments = arguments.substring(0, i+1) + maskValue + arguments.substring(symbol_ampersand, arguments.length());
						exitLoop = true;
					}
				}
			}

			// Mask out passwords for TFS
			// Command Format: 			[/login:user,[password]]	
			// Example (Mask): 			/login:user1,password
			// Example (Do not mask): 	/login:user1
			if (arguments.contains("/login:")) {
				int pswdBeg = -1;
				int pswdEnd = -1;
				int login_beg = arguments.indexOf("/login:");
				// Count the number of : prior to 
				boolean exitLoop = false;
				for (int i=login_beg; i < arguments.length() && !exitLoop; i++) {
					String ch = arguments.substring(i, i+1);
					if (ch.equalsIgnoreCase(",")) {
						pswdBeg = i+1;
					}
					if (ch.equalsIgnoreCase(" ")) {
						if (pswdBeg > 0) {
							pswdEnd = i;
						}
						exitLoop = true;
					}
				}
				if (pswdBeg > 0) {
					if (pswdEnd == -1) {
						arguments = arguments.substring(0, pswdBeg) + maskValue;						
					} else {
						arguments = arguments.substring(0, pswdBeg) + maskValue + arguments.substring(pswdEnd, arguments.length());
					}
					exitLoop = true;
				}
			}

			// Mask the arguments
			String[] maskTokens = {"password","passwd"};
			for (int i=0; i < maskTokens.length; i++ ) {
				arguments = CommonUtils.maskArgument(arguments,"=",maskTokens[i],maskValue);
				arguments = CommonUtils.maskArgument(arguments," ",maskTokens[i],maskValue);			
			}
		}
		return arguments==null?"":arguments.trim();
	}
	
	// Mask the property value following "maskProp" with the value of "maskValue".  Case sensitivity is ignored
	// This method is used when displaying output to the log file or system.out
	public static String maskArgument(String arguments, String separator, String maskProp, String maskValue) {
		StringTokenizer st = new StringTokenizer(arguments,separator);
		String newArguments = " ";
		boolean maskPropFound = false;
		int tokenCount = 1;
		while(st.hasMoreTokens()){
			String token = st.nextToken();
			tokenCount++;
			// Since the property was found on the previous loop, this token is assumed to be the value.
			// Use the maskValue instead of the real value.
			if (maskPropFound) {
				newArguments = newArguments + maskValue + separator;
				maskPropFound = false;
			} else {
				// Only test the first token in a name=value pair combination. 
				// The first token starts at 2 so it is always even numbers
				// It is assumed that the first token contains the property and the next token contains the value
				// Case sensativity does not matter
				// Example 1: P4PASSWD=password (separator is an = sign)
				// Example 2: --password password (separator is a space)
				// -- didn't work too well: if ((tokenCount % 2 == 0) && (token.toLowerCase().contains(maskProp.toLowerCase()))) {
				if (token.toLowerCase().contains(maskProp.toLowerCase())) {
					maskPropFound = true;
				}
				newArguments = newArguments + token + separator;				
			}
		}
		// remove the last character if it matches the separator
		if (newArguments.lastIndexOf(separator) == newArguments.length()-1) {
			newArguments = newArguments.substring(0, newArguments.length()-1);
		}
		return newArguments.trim();
	}
	
	/**
	 * Get the list of arguments for display and mask out any passwords
	 */
	public static String getArgumentListMasked(List<String> argsList) {
		String maskedArgList = "";
		String token = null;
		String maskTokenList = "-password,password,password2";
		boolean maskToken = false;
		boolean valueMasked = false;
		
		for (int i=0; i < argsList.size(); i++) {
			if (maskedArgList.length() > 0) {
				maskedArgList = maskedArgList + ", ";
			}
			token = argsList.get(i);
			if (maskToken) {
				maskedArgList = maskedArgList + "*******";
				valueMasked = true;
			} else {
				maskedArgList = maskedArgList + token;
				valueMasked = false;
			}
			// OK to set if it was not previously set and the token is in the mask token list
			if (!valueMasked && maskTokenList.contains(token)) {
				maskToken = true;
			} else {
				maskToken = false;
			}
			
		}
		return maskedArgList;
	}

	/* Parse the argument string and return an array list of arguments with variables resolved.
	 * 
	 * @param argument="localhost "ds1,ds2,ds3" $MODULE_HOME/DataSourceModule.xml $MODULE_HOME/servers.xml"
	 * @param preserveQuotes - boolean: preserve the quotes around the argument string
	 * 
	 * @return - array list of arguments:
	 * 		arg[0]=localhost
	 * 		arg[1]=ds1,ds2,ds3
	 * 		arg[2]=D:/CisDeployTool/resources/modules/DataSourceModule.xml
	 * 		arg[3]=D:/CisDeployTool/resources/modules/servers.xml
	 * 
	 * Variable Definitions Supported:
	 * ------------------------------
	 * Single $ separator followed by forward slash: 		$MODULE_HOME/servers.xml
	 * Percent % separator on either side of the variable: 	%MODULE_HOME%/servers.xml 
	 * Single % separator: 									%MODULE_HOME/servers.xml 
	 * Single $ separator followed by a % separator: 		$MODULE_HOME%/servers.xml
	 * Single % separator followed by a $ separator: 		%MODULE_HOME$/servers.xml
	 * Variable at the end of the line with no separator following the variable:  $VAR or %VAR
	 */
	public static List<String> parseArguments(List<String> argList, boolean initArgList, String arguments, boolean preserveQuotes, String propertyFile) {
		if (initArgList) {
			argList = new ArrayList<String>();
		}
		String prefix = "CisDeployTool.parseArguments";
		String arg = "";
		if (arguments != null) {
			// Trim white space
			arguments = arguments.trim();
			boolean argFound = false;
			boolean quoteFound = false;
			boolean sepFound = false;
			int begSep = 0;
			int endSep = 0;
			int begQuote = -1;
			int endQuote = -1;
			int nextVal = 0;
			int i = 0;
			int len = arguments.length();
			while (i < len) {
				// Extract the character from the argument text
				String ch = arguments.substring(i, i+1);
				// The separator is a space
				if (ch.equals(" ")) {
					// Set the space separator found "sepFound" as long as it is not within double quotes
					if (!quoteFound) {
						sepFound = true;
						argFound = true;
						endSep = i;
						nextVal = i;
					}
				}
				// The separator is a quote
				if (ch.equals("\"")) {
					// Check for a double quote and mark either the beginning or ending of the double quote
					quoteFound = true;
					if (begQuote < 0) {
						begQuote = i;
						
						// If the quote isn't the first character, it isn't a separator, and we need to keep the stuff before the quote
						if(i>0)
							begSep = 0;
						else
							begSep = i+1;
					} else if (endQuote < 0) {
						endQuote = i;
						endSep = i;
						nextVal = i+1;
						argFound = true;
					}
				}
				// A space separator was found but no quote separator was found
				if (sepFound && !quoteFound) {
					argFound = true;
				}
				// There were no separators found at the end of line so take the last value as an argument 
				if ( !argFound && (i+1 == len) ) {
					argFound = true;
					endSep = i+1;
					nextVal = i+1;
				}
				// An argument was found either by a space separator, enclosed in double quotes or the last value on the line.
				if (argFound) {
					// Extract the argument from the arguments string
					arg = arguments.substring(begSep, endSep);
					//Resolve Property variables
					// arg = resolveProperties(prefix, arg);
					arg = CommonUtils.extractVariable(prefix, arg, propertyFile, true);
					// Add to the args array
					if (quoteFound && preserveQuotes && begSep != 0) {
						// If the quote was a separator, then surround it with quotes
						argList.add("\""+arg+"\"");
					} else if (quoteFound && preserveQuotes && begSep == 0) {
						// If the quote was not a separator, then add a quote at the end only
						argList.add(arg+"\"");
					} else {
						argList.add(arg);						
					}
					// Extract the rest of the line and trim spaces.
					arguments = arguments.substring(nextVal).trim();
					i = 0;
					len = arguments.length();
					// Re-initialize
					begSep = 0;
					endSep = 0;
					begQuote = -1;
					endQuote = -1;
					argFound = false;
					sepFound = false;
					quoteFound = false;
				} else {
					i++;
				}
			}
		}
		return argList;
	}
	
	
	// Write an output message to a log based on application level settings
	public static void writeOutput(String message, String prefix, String options, Log logger, boolean debug1, boolean debug2, boolean debug3) {

		// Determine if there is a prefix to prepend
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = prefix+"::";
		}
		//Write out the log if not suppressed
		if (!options.contains("-suppress")) {
			
			//Write to log when -error
			if (options.contains("-error")) {
				if (logger.isErrorEnabled()) {
					logger.error(prefix+message);
				}
			}

			//Write to log when -info
			if (options.contains("-info")) {
				if (logger.isInfoEnabled()) {
					logger.info(prefix+message);
				}
			}

			//Write to log when -debug1
			if (options.contains("-debug1") && debug1) {
				// logger.isInfoEnabled() is checked on purpose.  Don't change it.
				if (logger.isInfoEnabled()) {
					logger.info("DEBUG1::"+prefix+message);
				}
			}		

			//Write to log when -debug2
			if (options.contains("-debug2") && debug2) {
				// logger.isInfoEnabled() is checked on purpose.  Don't change it.
				if (logger.isInfoEnabled()) {
					logger.info("DEBUG2::"+prefix+message);
				}
			}	
			
			//Write to log when -debug3
			if (options.contains("-debug3") && debug3) {
				// logger.isInfoEnabled() is checked on purpose.  Don't change it.
				if (logger.isInfoEnabled()) {
					logger.info("DEBUG3::"+prefix+message);
				}
			}	
		}
	}
	
	/**
	 * Calculate elapsed time
	 * @param startDate
	 * @return String with a format like "000 00:00:00.0000"
	 */
	public static String getElapsedTime(Date startDate) {
		long duration = System.currentTimeMillis() - startDate.getTime();
		String elapsedString = getElapsedDuration(duration);
		return elapsedString;
	}

	/**
	 * Calculate elapsed time
	 * @param duration - long number representing the difference between two dates
	 * @return String with a format like "000 00:00:00.0000"
	 */
	public static String getElapsedDuration(long duration) {
		String elapsedString = "";
		String plus_minus=" ";
		if (duration < 0) {
			plus_minus="-";
			// make it positive
			duration = duration * -1;
		}
		long days=0, hours=0, minutes=0, seconds=0, milliseconds=0;
		long DAY=24*60*60*1000;
		long HOUR=60*60*1000;
		long MINUTE=60*1000;
		long SECOND=1000;
		// days
		if (duration > DAY) {
			days = duration / DAY;
		}
		duration = duration % DAY;
		// hours
		if (duration > HOUR) {
			hours = duration / HOUR;
		}
		duration = duration % HOUR;
		// minutes
		if (duration > MINUTE) {
			minutes = duration / MINUTE;
		}
		duration = duration % MINUTE;
		if (duration > SECOND) {
			seconds = duration / SECOND;
		}
		milliseconds = duration % SECOND;

		// Return a standard format: "000 00:00:00.0000"
		//	  000=days
		//	  00:=hours
		//	  00:=minutes
		//    00:=seconds
		//	.0000=milliseconds
		elapsedString = plus_minus+
			lpad(Long.toString(days), 3, "0")+" "+
			lpad(Long.toString(hours), 2, "0")+":"+
			lpad(Long.toString(minutes), 2, "0")+":"+
			lpad(Long.toString(seconds), 2, "0")+"."+
			lpad(Long.toString(milliseconds), 4, "0");
		
		/* mtinius: this is commented out but left for documentation purposes
		 *  I wanted to have a record of the actual word label format.
		 *  
		// Construct the elapsedTime string
		if (days > 0) {
			elapsedString += days + " days "; // days
		}
		if (days != 0 || hours > 0) {
			elapsedString += hours + " hours "; // hours
		}
		if (days != 0 || hours != 0 || minutes > 0) {
			elapsedString += minutes + " min "; // min
		}
		if (days != 0 || hours != 0 || minutes != 0 || (seconds > 0 && milliseconds > 0)) {
			elapsedString += seconds+"."+milliseconds + " sec "; // sec
		} else {
			elapsedString += milliseconds + " milliseconds"; // milliseconds
		}
		*/
		return elapsedString;
	}
	
	/**
	 * Get the actual long value from the formatted duration
	 * 
	 * @param String with a format like "000 00:00:00.0000"
	 *                                   01234567890123456
	 * @return duration - long number representing the difference between two dates
	 */
	public static long getLongDuration(String duration) throws CompositeException {
		long longDuration = 0L;
		String validFormat = "000 00:00:00.0000";
		int validFormatLen = validFormat.length();
		
		if (duration == null) {
			throw new ApplicationException("The duration parameter cannot be null.");			
		}
		if (duration.length() != validFormatLen) { // length=17
			throw new ApplicationException("The format of the duration parameter ("+duration+") does not meet the required length of "+validFormatLen+" characters.");
		}
		if (duration.indexOf(" ") != 3) {
			throw new ApplicationException("The format of the duration parameter ("+duration+") is incorrect: space in wrong position.  It must be in the format of ["+validFormat+"].");
		}
		if (duration.indexOf(":") != 6) {
			throw new ApplicationException("The format of the duration parameter ("+duration+") is incorrect: first colon in wrong position.  It must be in the format of ["+validFormat+"].");
		}
		if (duration.lastIndexOf(":") != 9) {
			throw new ApplicationException("The format of the duration parameter ("+duration+") is incorrect: second colon in wrong position.  It must be in the format of ["+validFormat+"].");
		}
		if (duration.lastIndexOf(".") != 12) {
			throw new ApplicationException("The format of the duration parameter ("+duration+") is incorrect: period in wrong position.  It must be in the format of ["+validFormat+"].");
		}
		long days=0, hours=0, minutes=0, seconds=0, milliseconds=0;
		long DAY=24*60*60*1000;
		long HOUR=60*60*1000;
		long MINUTE=60*1000;
		long SECOND=1000;
		// days
		String d= duration.substring(0,3);
		days = Long.valueOf(d) * DAY;	
		// hours
		String h = duration.substring(4,6);
		hours = Long.valueOf(h) * HOUR;
		// minutes
		String m = duration.substring(7,9);
		minutes = Long.valueOf(m) * MINUTE;
		// seconds
		String s = duration.substring(10,12);
		seconds = Long.valueOf(s) * SECOND;
		// milliseconds
		String ms = duration.substring(13,17);
		milliseconds = Long.valueOf(ms);
		
		// Sum it up
		longDuration = days + hours + minutes + seconds + milliseconds;
		
		return longDuration;
	}
	
	/**
	 * escapeXML - replace special characters with the XML escape equivalent.  Remove single quotes around the outside of variables.
	 * 
	 * Examples:  
	 *   'dummystr' --> dummystr
	 *   we'll be there --> we&apos;ll be there
	 *   you & me --> you &amp; me
	 *   5 < 3 --> 5 &lt; 3
	 * 
	 * @param s - incoming string
	 * @return r - outgoing escaped string
	 */
	public static String escapeXML(String s) {
		String r = s;
		
		if (r != null) {
			int len = r.length();
			// Remove the single quote at the end of the string
			if (r.lastIndexOf("'") == len-1) {
				r = r.substring(0, len-1);
			}
			// Remove the single quote at the beginning of the string
			if (r.indexOf("'") == 0) {
				r = r.substring(1);
			}
			// First make sure that anything already escaped is turned back to its original unesapced value so as to not cause an issue with conversion.
			r = r.replaceAll(Matcher.quoteReplacement("&amp;"), Matcher.quoteReplacement("&"));
			r = r.replaceAll(Matcher.quoteReplacement("&lt;"), Matcher.quoteReplacement("<"));
			r = r.replaceAll(Matcher.quoteReplacement("&gt;"), Matcher.quoteReplacement(">"));
			r = r.replaceAll(Matcher.quoteReplacement("&apos;"), Matcher.quoteReplacement("'"));
			r = r.replaceAll(Matcher.quoteReplacement("&quot;"), Matcher.quoteReplacement("\""));

			// Perform the XML escape conversions
			String from = "&";
			if (r.indexOf(from) >= 0) {
				r = r.replaceAll(Matcher.quoteReplacement(from), Matcher.quoteReplacement("&amp;"));
			}
			from = "<";
			if (r.indexOf(from) >= 0) {
				r = r.replaceAll(Matcher.quoteReplacement(from), Matcher.quoteReplacement("&lt;"));
			}
			from = ">";
			if (r.indexOf(from) >= 0) {
				r = r.replaceAll(Matcher.quoteReplacement(from), Matcher.quoteReplacement("&gt;"));
			}
			from = "'";
			if (r.indexOf(from) >= 0) {
				r = r.replaceAll(Matcher.quoteReplacement(from), Matcher.quoteReplacement("&apos;"));
			}
			from = "\"";
			if (r.indexOf(from) >= 0) {
				r = r.replaceAll(Matcher.quoteReplacement(from), Matcher.quoteReplacement("&quot;"));
			}
		}
		return r;
	}

	public static int getMaskParamNum(String executeAction, int numInputArgs, String methodList) {
		int maskParamNum = -1;
		int totalParamNum = -1;
		
		if (methodList.contains(executeAction) ) {
			StringTokenizer st = new StringTokenizer(methodList, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();

				if (token.contains(executeAction)) {
				
					// Extract the number within the brackets [2|2], parse to an int
					int count = 1;
					boolean continueLoop = true;
					String paramNums = token.substring(token.indexOf("[")+1, token.indexOf("]"));
					StringTokenizer pst = new StringTokenizer(paramNums, "|");
					while (pst.hasMoreTokens() && continueLoop) {
						String ptoken = pst.nextToken();
						if (count == 1) {
							totalParamNum = Integer.parseInt(ptoken);
						} else	if (count == 2) {
							maskParamNum = Integer.parseInt(ptoken);
						} else {
							continueLoop = false;
						}
						count ++;
					}
					if (numInputArgs == totalParamNum)
						return maskParamNum;
				}
			}
		}
		return maskParamNum;
	}

	
	public static String getCisVersion(String serverId, String pathToServersXML) {
		String startPath = "/server/config/info";
		String versionPath = startPath + "/version";
		String version = null;
		
		ServerAttributeDAO serverAttributeDAO = new ServerAttributeWSDAOImpl();;

		// Retrieve the list of Server Attributes by invoking the CIS Web Service API
		AttributeList attributeList = serverAttributeDAO.getServerAttributesFromPath(serverId, startPath, pathToServersXML);

		// Continue if there is a list
		if(attributeList != null && attributeList.getAttribute() != null && !attributeList.getAttribute().isEmpty()){
			// Assign the list of Server Attributes to a local Attribute type variable
			List<Attribute> attributes = attributeList.getAttribute();
			
			// Iterate over the retrieved Server Attribute List until the version path is found
			for (Attribute attribute : attributes) {
				if (attribute.getName().equalsIgnoreCase(versionPath)) {
					version = attribute.getValue();
					return version;
				}
			}
		}				
							
		return version;
	}


	/**
	 * applyReservedListToPath - Use CIS Utilities RepoUtils to apply reserved list to path
	 * 
	 * Examples:  
	 *  CIS Path
	 *   /services/databases/MYDB/_cat/1schema/customer order --> /services/databases/MYDB/"_cat"/"1schema"/"customer order"
	 *   
	 *  CIS select count(1) FROM ...
	 *   _cat.1schema.customer order --> "_cat"."1schema"."customer order"
	 * 
	 * @param path - incoming path with unquoted word parts
	 * @param delimiter - / or . may be used for a delimiter
	 * @return returnPath - outgoing path with reserved words double quoted
	 */
	public static String applyReservedListToPath(String path, String delimiter) throws CompositeException{
		String returnPath = path;
		boolean preserveDelimiter = false;
		
		if (returnPath != null) {
			try {
				returnPath = "";
				// Check to see if the path came in with a delimiter on the end so that it can be preserved.
		        int len = path.length()-1;
		        int lastidx = path.lastIndexOf(delimiter);
		        if (len > 0 && lastidx == len) {
		        	preserveDelimiter = true;
		        }
		        
				// Split the path into parts based on the delimiter
				String[] pathparts = path.split("\\"+delimiter);
	
				// Loop through the parts
		        for (int i=0; i<pathparts.length; i++)
		        {
		        	returnPath = returnPath + CisPathQuoter.quoteWord(pathparts[i]) + delimiter;
		        }
			} catch (CisPathQuoterException e) {
		        throw new CompositeException("Error retrieving quoted path for path="+path+"  Error="+e.getMessage());
			}
	        // Remove the last delimiter
			if (!preserveDelimiter) {
		        int len = returnPath.length()-1;
		        int lastidx = returnPath.lastIndexOf(delimiter);
		        if (len > 0 && lastidx == len) {
		        	returnPath = returnPath.substring(0, len);
		        }
			}
		}
        return returnPath;
	}
	
	/**
	 * applyReservedListToWord - Use CIS Utilities RepoUtils to apply reserved list to a word
	 * 
	 * Examples:  
	 *   _test --> "_test"
	 *   1test --> "1test"
	 *   select --> "select"
	 * 
	 * @param word - incoming unquoted word
	 * @return word - outgoing word double quoted if reserved
	 */
	public static String applyReservedListToWord(String word) {
		
		try {
			if (word != null)
				word = CisPathQuoter.quoteWord(word);
			return word;
		} catch (CisPathQuoterException e) {
	        throw new CompositeException("Error retrieving quoted word for word="+word+"  Error="+e.getMessage());
		}
	}
	
	/**
	 * For testing.
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{
		
		CommonUtils utilsObject = new CommonUtils();

		// test file comparisons.
		
		String file1 = "C:/temp/TestFileComparisons/prdFileEmpty.txt";  // compFileXLarge.txt";   //prdFile.txt";  // prdFileSmall2.txt";
		String file2 = "C:/temp/TestFileComparisons/compfileEmpty.txt";  // compFileXLarge.txt";  // compFileSmall.txt";
		
		boolean areFilesTheSame = utilsObject.compareFiles(file1, file2);
		System.out.println("areFilesTheSame = " + areFilesTheSame);

	}

}
