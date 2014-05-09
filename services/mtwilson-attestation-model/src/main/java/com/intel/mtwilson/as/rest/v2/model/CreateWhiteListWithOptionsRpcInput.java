/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigData;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="create_whitelist_with_options_rpc_input")
public class CreateWhiteListWithOptionsRpcInput {
    private HostConfigData config;

    public HostConfigData getConfig() {
        return config;
    }

    public void setConfig(HostConfigData config) {
        this.config = config;
    }

    
}