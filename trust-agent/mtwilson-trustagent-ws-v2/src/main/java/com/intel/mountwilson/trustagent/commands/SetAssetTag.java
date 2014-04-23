/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mountwilson.common.TAConfig;
import java.io.PrintWriter;
import org.apache.commons.lang.StringUtils;



/**
 *
 * @author stdalex
 */
public class SetAssetTag implements ICommand{
   Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;
    private final static String index ="0x40000010";
    
    public SetAssetTag(TADataContext context) {
        this.context = context;
    }
        
    @Override
    public void execute() throws TAException{
        try {
            
            //String password = "ffffffffffffffffffffffffffffffffffffffff";  //No longer needed, read it from props file in createIndex()
            String tpmNvramPass = generateRandomPass();
            
            //create the index if needed
            boolean iExists = indexExists();
            if(iExists){  // if it exists we need to get the password from the service for the nvram
                log.debug("Index exists. Releasing index...");
                releaseIndex();
                log.debug("Creating new index...");
                createIndex(tpmNvramPass);
            }else{ // generate random password 
                // Just use the same password right now for testing
                // password =  generateRandomPass();
                log.debug("Index does not exist. creating it...");
                createIndex(tpmNvramPass);
            }
            //log.debug("using password " + password + " for index");
            //now index is created, write value to it
            writeHashToFile();  // store the hash as a binary file
            
            if(!writeHashToNvram(tpmNvramPass)) {
                // need some type of exception here
            }
            
            //last thing is, if we generated a new password, we need to register it
            if(!iExists) {
                registerPassword();
            }
            
            context.setResponseXML("<set_asset_tag><response>true</response></set_asset_tag>");
            
        } catch (Exception ex) {
            log.error(ex.getMessage());
            context.setResponseXML("<set_asset_tag><response>false</response><error>"+ex.getMessage() + "</error></set_asset_tag>");
        }
    }
    
    private void registerPassword() {
        // get uuid from context.getHostUUID() and call the asset tag service
        // and associate password with context.getHostUUID()
    }
    
    private boolean writeHashToNvram(String NvramPassword) throws TAException, IOException {
        List<String> result;
        try {
            //String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            //String tpmNvramPass = TAConfig.getConfiguration().getString("TpmNvramAuth");
            log.debug("running command tpm_nvwrite -x -i " + index + " -pXXXX -f /tmp/hash");
            String[] variables = { "NvramPassword=" + NvramPassword };
            result = CommandUtil.runCommand("tpm_nvwrite -x -t -i " + index + " -pNvramPassword -f /tmp/hash", variables);
            String response = StringUtils.join(result,"\n");
            log.debug("writeHashToNvram output: " + response);
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private void writeHashToFile() throws TAException, IOException {
        try {
            List<String> result = CommandUtil.runCommand("/usr/local/bin/hex2bin " + context.getAssetTagHash() + " /tmp/hash"); //| /usr/local/bin/hex2bin > /tmp/hash");
            String response = StringUtils.join(result,"\n");
            log.debug("writeHashToFile output: " + response);
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }        
    }
    
    private boolean createIndex(String NvramPassword) throws TAException, IOException {
        List<String> result;
        try {
            String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            //String tpmNvramPass = TAConfig.getConfiguration().getString("TpmNvramAuth");
            log.debug("running command tpm_nvdefine -i " + index + " -s 0x14 -x -aXXXX -oXXXX --permissions=AUTHWRITE");
            String[] variables = { "tpmOwnerPass=" + tpmOwnerPass, "NvramPassword=" + NvramPassword };
            result = CommandUtil.runCommand("tpm_nvdefine -i " + index + " -s 0x14 -x -t -aNvramPassword -otpmOwnerPass --permissions=AUTHWRITE", variables);
            String response = StringUtils.join(result,"\n");
            log.debug("createIndex output: " + response);
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private boolean releaseIndex() throws TAException, IOException {
        List<String> result;
        try {
            String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            log.debug("running command tpm_nvrelease -x -t -i " + index + " -oXXXX");
            String[] variables = { "tpmOwnerPass=" + tpmOwnerPass };
            result = CommandUtil.runCommand("tpm_nvrelease -x -t -i " + index + " -otpmOwnerPass", variables);
            String response = StringUtils.join(result,"\n");
            log.debug("releaseIndex output: " + response);
        }catch(TAException ex) {
                log.error("error releasing nvram index, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private String generateRandomPass() {
       return RandomUtil.randomHexString(20);
    }
    
    private boolean indexExists() throws TAException, IOException {     
        List<String> result;
        try {
            result = CommandUtil.runCommand("tpm_nvinfo -i " + index);  
            String response = StringUtils.join(result,"\n");
            log.debug("indexExists output: " + response);
            if(response.contains("NVRAM index")) 
                return true;
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return false;
    }
}