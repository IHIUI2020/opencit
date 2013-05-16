/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.as.controller.*;
import com.intel.mtwilson.audit.controller.*;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.ms.controller.*;
import java.io.IOException;

/**
 * Convenience class to instantiate JPA controllers for the purpose of writing JUnit tests...
 * Using this class allows you to eliminate a lot of boilerplate from your tests.
 * 
 * Instead of writing this:
 * 
 * TblHostsJpaController hosts = new TblHostsJpaController(ASPersistenceManager.createEntityManagerFactory("ASDataPU", ASConfig.getJpaProperties()));
 * (and that only works when it executes on a server with /etc/intel/cloudsecurity/attestation-service.properties)
 * 
 * You write this:
 * 
 * TblHostsJpaController hosts = My.jpa().mwHosts();
 * 
 * The naming convention is that given a table name like mw_api_client_x509, the method name is
 * chosen by removing underscores, and capitalizing the first letter of 
 * every word after the first one. So the method to get the corresponding JPA controller
 * in this example would be mwApiClientX509().
 * 
 * 
 * @author jbuhacoff
 */
public class MyJpa {
    private final MyPersistenceManager pm;
    TblApiClientJpaController mwApiClientHmac;
    MwApiClientHttpBasicJpaController mwApiClientHttpBasic;
    ApiClientX509JpaController mwApiClientX509;
    ApiRoleX509JpaController mwApiRoleX509;
    AuditLogEntryJpaController mwAuditLogEntry;
    MwCertificateX509JpaController mwCertificateX509;
    // XXX TODO we don't have a controller for the schema changelog   mw_changelog !!!
    MwConfigurationJpaController mwConfiguration;
    TblEventTypeJpaController mwEventType;
    TblHostSpecificManifestJpaController mwHostSpecificManifest;
    TblHostsJpaController mwHosts;
    MwKeystoreJpaController mwKeystore;
    TblLocationPcrJpaController mwLocationPcr;
    TblMleJpaController mwMle;
    MwMleSourceJpaController mwMleSource;
    TblModuleManifestJpaController mwModuleManifest;
    TblModuleManifestLogJpaController mwModuleManifestLog;
    TblOemJpaController mwOem;
    TblOsJpaController mwOs;
    TblPackageNamespaceJpaController mwPackageNamespace;
    TblPcrManifestJpaController mwPcrManifest;
    MwPortalUserJpaController mwPortalUser;
    MwRequestLogJpaController mwRequestLog;
    TblRequestQueueJpaController mwRequestQueue;
    TblSamlAssertionJpaController mwSamlAssertion;
    TblTaLogJpaController mwTaLog;

    public MyJpa(MyPersistenceManager pm) { this.pm = pm; }
    
    public TblApiClientJpaController mwApiClientHmac() throws IOException {
		if( mwApiClientHmac == null ) { mwApiClientHmac = new TblApiClientJpaController(pm.getMSData()); }
        return mwApiClientHmac;
	}
    public MwApiClientHttpBasicJpaController mwApiClientHttpBasic() throws IOException {
		if( mwApiClientHttpBasic == null ) { mwApiClientHttpBasic = new MwApiClientHttpBasicJpaController(pm.getASData()); }
		return mwApiClientHttpBasic;
	}
    public ApiClientX509JpaController mwApiClientX509() throws IOException {
		if( mwApiClientX509 == null ) { mwApiClientX509 = new ApiClientX509JpaController(pm.getMSData()); }
		return mwApiClientX509;
	}
    public ApiRoleX509JpaController mwApiRoleX509() throws IOException {
		if( mwApiRoleX509 == null ) { mwApiRoleX509 = new ApiRoleX509JpaController(pm.getMSData()); }
		return mwApiRoleX509;
	}
    public AuditLogEntryJpaController mwAuditLogEntry() throws IOException {
		if( mwAuditLogEntry == null ) { mwAuditLogEntry = new AuditLogEntryJpaController(pm.getAuditData()); }
		return mwAuditLogEntry;
	}
    public MwCertificateX509JpaController mwCertificateX509() throws IOException {
		if( mwCertificateX509 == null ) { mwCertificateX509 = new MwCertificateX509JpaController(pm.getASData()); }
		return mwCertificateX509;
	}
    // XXX TODO we don't have a controller for the schema changelog   mw_changelog !!!
    public MwConfigurationJpaController mwConfiguration() throws IOException {
		if( mwConfiguration == null ) { mwConfiguration = new MwConfigurationJpaController(pm.getMSData()); }
		return mwConfiguration;
	}
    public TblEventTypeJpaController mwEventType() throws IOException {
		if( mwEventType == null ) { mwEventType = new TblEventTypeJpaController(pm.getASData()); }
		return mwEventType;
	}
    public TblHostSpecificManifestJpaController mwHostSpecificManifest() throws IOException {
		if( mwHostSpecificManifest == null ) { mwHostSpecificManifest = new TblHostSpecificManifestJpaController(pm.getASData()); }
		return mwHostSpecificManifest;
	}
    public TblHostsJpaController mwHosts() throws IOException, CryptographyException {
		if( mwHosts == null ) { mwHosts = new TblHostsJpaController(pm.getASData()); }
		return mwHosts;
	}
    public MwKeystoreJpaController mwKeystore() throws IOException {
		if( mwKeystore == null ) { mwKeystore = new MwKeystoreJpaController(pm.getASData()); }
		return mwKeystore;
	}
    public TblLocationPcrJpaController mwLocationPcr() throws IOException {
		if( mwLocationPcr == null ) { mwLocationPcr = new TblLocationPcrJpaController(pm.getASData()); }
		return mwLocationPcr;
	}
    public TblMleJpaController mwMle() throws IOException {
		if( mwMle == null ) { mwMle = new TblMleJpaController(pm.getASData()); }
		return mwMle;
	}
    public MwMleSourceJpaController mwMleSource() throws IOException {
		if( mwMleSource == null ) { mwMleSource = new MwMleSourceJpaController(pm.getASData()); }
		return mwMleSource;
	}
    public TblModuleManifestJpaController mwModuleManifest() throws IOException {
		if( mwModuleManifest == null ) { mwModuleManifest = new TblModuleManifestJpaController(pm.getASData()); }
		return mwModuleManifest;
	}
    public TblModuleManifestLogJpaController mwModuleManifestLog() throws IOException {
		if( mwModuleManifestLog == null ) { mwModuleManifestLog = new TblModuleManifestLogJpaController(pm.getASData()); }
		return mwModuleManifestLog;
	}
    public TblOemJpaController mwOem() throws IOException {
		if( mwOem == null ) { mwOem = new TblOemJpaController(pm.getASData()); }
		return mwOem;
	}
    public TblOsJpaController mwOs() throws IOException {
		if( mwOs == null ) { mwOs = new TblOsJpaController(pm.getASData()); }
		return mwOs;
	}
    public TblPackageNamespaceJpaController mwPackageNamespace() throws IOException {
		if( mwPackageNamespace == null ) { mwPackageNamespace = new TblPackageNamespaceJpaController(pm.getASData()); }
		return mwPackageNamespace;
	}
    public TblPcrManifestJpaController mwPcrManifest() throws IOException {
		if( mwPcrManifest == null ) { mwPcrManifest = new TblPcrManifestJpaController(pm.getASData()); }
		return mwPcrManifest;
	}
    public MwPortalUserJpaController mwPortalUser() throws IOException {
		if( mwPortalUser == null ) { mwPortalUser = new MwPortalUserJpaController(pm.getMSData()); }
		return mwPortalUser;
	}
    public MwRequestLogJpaController mwRequestLog() throws IOException {
		if( mwRequestLog == null ) { mwRequestLog = new MwRequestLogJpaController(pm.getASData()); }
		return mwRequestLog;
	}
    public TblRequestQueueJpaController mwRequestQueue() throws IOException {
		if( mwRequestQueue == null ) { mwRequestQueue = new TblRequestQueueJpaController(pm.getASData()); }
		return mwRequestQueue;
	}
    public TblSamlAssertionJpaController mwSamlAssertion() throws IOException {
		if( mwSamlAssertion == null ) { mwSamlAssertion = new TblSamlAssertionJpaController(pm.getASData()); }
		return mwSamlAssertion;
	}
    public TblTaLogJpaController mwTaLog() throws IOException {
		if( mwTaLog == null ) { mwTaLog = new TblTaLogJpaController(pm.getASData()); }
		return mwTaLog;
	}

}