/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.common;

import com.intel.mountwilson.common.Config;
import com.intel.mountwilson.trustagent.datatype.IPAddress;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author dsmagadX
 */
public class CommandUtil {
    
    private static final Logger log = LoggerFactory.getLogger(CommandUtil.class.getName());

    public static List<String> runCommand(String commandLine, String[] envp) throws TAException, IOException {
        if(StringUtils.isBlank(commandLine))
            throw new TAException(ErrorCode.ERROR,"Command cannot be empty.");
        
        String[] command = commandLine.split(" ");
        
        if(new File(Config.getBinPath() + File.separator + command[0]).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;

        if(new File(Config.getBinPath() + File.separator + commandLine).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;
        
        log.debug("Command to be executed is :" + commandLine);

        Process p = Runtime.getRuntime().exec(commandLine, envp);

        List<String> result = new ArrayList<String>();

        readResults(p, result);
        
        //do a loop to wait for an exit value
        boolean isRunning;
        int timeout = 500000;
        int countToTimeout = 0;
        do {
            countToTimeout++;
            isRunning = false;
            try {
                p.exitValue();

            } catch (IllegalThreadStateException e1) {
                isRunning = true;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    isRunning = false;
                }
            }
        } while (isRunning
                && (countToTimeout < timeout));

        if (countToTimeout
                == timeout) {
            log.info("Command is not responding.");
            p.destroy();

        }

        checkError(p.exitValue(), commandLine);


        return result;
    }
    
    public static List<String> runCommand(String commandLine) throws TAException, IOException {
        
        if(StringUtils.isBlank(commandLine))
            throw new TAException(ErrorCode.ERROR,"Command cannot be empty.");
        
        String[] command = commandLine.split(" ");
        
        if(new File(Config.getBinPath() + File.separator + command[0]).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;

        if(new File(Config.getBinPath() + File.separator + commandLine).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;
        
        log.debug("Command to be executed is :" + commandLine);

        Process p = Runtime.getRuntime().exec(commandLine);

        List<String> result = new ArrayList<String>();

        readResults(p, result);

//        if (Config.isDebug()) {
//            log.log(Level.INFO, "Result Output \n{}", (result == null ) ? "null":result.toString());
//        }

        //do a loop to wait for an exit value
        boolean isRunning;
        int timeout = 500000;
        int countToTimeout = 0;
        do {
            countToTimeout++;
            isRunning = false;
            try {
                p.exitValue();

            } catch (IllegalThreadStateException e1) {
                isRunning = true;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {
                    isRunning = false;
                }
            }
        } while (isRunning
                && (countToTimeout < timeout));

        if (countToTimeout
                == timeout) {
            log.info("Command is not responding.");
            p.destroy();

        }

        checkError(p.exitValue(), commandLine);


        return result;
    }
        
    private static void checkError(int exitValue, String commandLine) throws TAException {
        log.debug( "Return code {}", exitValue);

        if (exitValue != 0) {
            throw new TAException(ErrorCode.FATAL_ERROR, exitValue + ": Error while running command: " + commandLine);
        }


    }

    private static void readResults(Process p, List<String> result) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));


        try {
            String newLine;
            while ((newLine = input.readLine()) != null) {
                result.add(newLine);
            }
        } finally {
            input.close();
        }


    }

    public static byte[] readfile(String fileName) throws TAException {


        InputStream fStream = null;
        try {
            int fileLength = (int) new File(fileName).length();
            fStream = new FileInputStream(fileName);
            byte[] fileContents = new byte[fileLength];
            int read = fStream.read(fileContents);
            if (read != fileLength) {
                log.warn("Length of file read is not same as file length");
            }
            return fileContents;
        } catch (Exception ex) {
            throw new TAException(ErrorCode.ERROR, "Error while reading cert", ex);
        } finally {
           
                try {
                    if(fStream != null){
                        fStream.close();
                    }
                } catch (IOException e) {
                    log.warn("Error while closing stream", e);
                }
        }


    }

    public static String readCertificate(String fileName) throws TAException {
        try {
            FileInputStream in = new FileInputStream(new File(fileName));
            String pem = IOUtils.toString(in);
            in.close();
//            X509Certificate certificate = X509Util.decodePemCertificate(pem);
            return pem;
        } catch (Exception e) {
            throw new TAException(ErrorCode.ERROR, "Error while reading AIK Cert", e);
        }
    }

    public static String getHostIpAddress() {
        String localIpAddress = "127.0.0.1";
        Enumeration<NetworkInterface> networkInterface;
        try {

            networkInterface = NetworkInterface.getNetworkInterfaces();
            for (; networkInterface.hasMoreElements();) {
                NetworkInterface e = networkInterface.nextElement();
                log.debug( "Interface: {}", new Object[]{e.getName()});
                Enumeration<InetAddress> ad = e.getInetAddresses();
                for (; ad.hasMoreElements();) {
                    InetAddress addr = ad.nextElement();
                    String returnIpAddress = addr.getHostAddress();
                    if (!returnIpAddress.equals(localIpAddress) && IPAddress.isValid(returnIpAddress)) {
                        return returnIpAddress;
                    } else {
                        log.debug("{} == {} or ip validation failed.", new Object[]{returnIpAddress, localIpAddress});
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Error while getting the network interfaces returning 127.0.0.1", ex);
        }
        return localIpAddress;
    }

    public static String generateErrorResponse(ErrorCode errorCode) {
        return generateErrorResponse(errorCode, null);
    }
    
    public static String generateErrorResponse(ErrorCode errorCode, String optionalDescription) {
        String extra = "";
        if( optionalDescription != null ) {
            extra = ": "+optionalDescription;
        }
        String responseXML =
                "<client_request> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<clientIp>" + StringEscapeUtils.escapeXml(CommandUtil.getHostIpAddress()) + "</clientIp>"
                + "<error_code>" + errorCode.getErrorCode() + "</error_code>"
                + "<error_message>" + StringEscapeUtils.escapeXml(errorCode.getMessage() + extra) + "</error_message>"
                + "</client_request>";
        return responseXML;
    }
        
    
}