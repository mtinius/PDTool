/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 */
package com.cisco.dvbu.cmdline.vcs.util;

import com.cisco.dvbu.cmdline.vcs.primitives.AbstractOptions;
import com.compositesw.cmdline.archive.PackageCommandConstants;
import com.compositesw.common.repository.Path;
import com.compositesw.common.vcs.primitives.ResourceNameCodec;

/**
 * @author panagiotis
 */
public class NameCodecCommand {
    
    public static void startCommand(String baseDir, String homeDir, String[] args) throws Exception {
        Options options = new Options(args);
        
        int exitCode = PackageCommandConstants.EXIT_WITH_NO_ERRORS;
        try {
            if (options.encode) {
                System.out.println("Encoded form: " + ResourceNameCodec.encode(options.namespacePathPath));
            }
            else {
                System.out.println("Decoded form: " + ResourceNameCodec.decode(options.namespacePathPath));
            }            
        }
        catch(RuntimeException e) {
            exitCode = PackageCommandConstants.PKGFILE_EXCEPTION;
            
            System.err.println("Encountered problem: " + e.getMessage());            
        }
        
        System.exit(exitCode);
    }
    
    private static class Options extends AbstractOptions {

        /**
         * Optional.
         */
        private boolean encode = true;

        private String namespacePath;
        
        private Path namespacePathPath;
        
        Options(String[] args) {
            if (args == null) usage(true);
            
            parse(args);
            
            validate();
        }
        
        @Override
        protected void usage(boolean error) {
            System.out.println("Usage: vcs_name_codec [-encode ] [-decode ] <namespacePath>");

            if (error) System.exit(ERROR_INVALID_ARGS_ON_CMD_LINE);
            else System.exit(EXIT_WITH_NO_ERRORS);            
        }        
        
        private void parse(String[] args) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-h")) {
                    usage(false);
                }
                else if (args[i].equalsIgnoreCase("-encode")) {
                    this.encode = true;
                } 
                else if (args[i].equalsIgnoreCase("-decode")) {
                    this.encode = false;
                } 
                else if (args[i].equals("")) {
                    /* ignore */
                } else if (args[i].startsWith("-")) {
                    unknownOptionError(args[i]);
                    usage(true);
                }
                // need to parse this at end since there is no command line
                // option for this
                else {
                    if (namespacePath == null) {
                        namespacePath = args[i];
                    }
                    else {
                        multiplePaths(namespacePath, args[i]);
                        usage(true);
                    }
                }
            }
        }
        
        private static void multiplePaths(String path1, String path2) {
            System.err.println("Found multiple paths: " + path1 + ", " + path2 + ". Expected exactly one.");
        }
        
        private void validate() {
            // check namespacePath
            namespacePathPath = new Path(namespacePath);
        }
    }
}
