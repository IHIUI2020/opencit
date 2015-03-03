/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryCreateException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import static com.intel.mtwilson.as.rest.v2.rpc.CertifyHostBindingKeyRunnable.isAikCertifiedByPrivacyCA;
import static com.intel.mtwilson.as.rest.v2.rpc.CertifyHostBindingKeyRunnable.validatePublicKeyDigest;
import com.intel.mtwilson.util.tpm12.CertifyKey;
import gov.niarl.his.privacyca.TpmCertifyKey;

/**
 *
 * @author ssbangal
 */
@RPC("certify-host-signing-key")
@JacksonXmlRootElement(localName = "certify_host_signing_key")
public class CertifyHostSigningKeyRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyHostSigningKeyRunnable.class);
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;
    private String signingKeyPemCertificate;
    private byte[] tpmCertifyKeySignature;
    private String aikPemCertificate;

    public byte[] getPublicKeyModulus() {
        return publicKeyModulus;
    }

    public void setPublicKeyModulus(byte[] publicKeyModulus) {
        this.publicKeyModulus = publicKeyModulus;
    }

    public byte[] getTpmCertifyKey() {
        return tpmCertifyKey;
    }

    public void setTpmCertifyKey(byte[] tpmCertifyKey) {
        this.tpmCertifyKey = tpmCertifyKey;
    }

    public String getSigningKeyPemCertificate() {
        return signingKeyPemCertificate;
    }

    public void setSigningKeyPemCertificate(String signingKeyPemCertificate) {
        this.signingKeyPemCertificate = signingKeyPemCertificate;
    }

    public byte[] getTpmCertifyKeySignature() {
        return tpmCertifyKeySignature;
    }

    public void setTpmCertifyKeySignature(byte[] tpmCertifyKeySignature) {
        this.tpmCertifyKeySignature = tpmCertifyKeySignature;
    }

    public String getAikPemCertificate() {
        return aikPemCertificate;
    }

    public void setAikPemCertificate(String aikPemCertificate) {
        this.aikPemCertificate = aikPemCertificate;
    }

    
    @Override
    @RequiresPermissions({"host_signing_key_certificates:create"})
    public void run() {
        try {
            if (publicKeyModulus != null && tpmCertifyKey != null && tpmCertifyKeySignature != null && aikPemCertificate != null) {

                log.debug("Starting to verify the Signing key TCG certificate and generate the MTW certified certificate.");

                log.debug("Public key modulus {}, TpmCertifyKey data {} & TpmCertifyKeySignature data {} are specified.",
                        TpmUtils.byteArrayToHexString(publicKeyModulus), TpmUtils.byteArrayToHexString(tpmCertifyKey), TpmUtils.byteArrayToHexString(tpmCertifyKeySignature));

                // Verify the encryption scheme, key flags etc
//                validateCertifyKeyData(tpmCertifyKey, false);
                if( !CertifyKey.isSigningKey(new TpmCertifyKey(tpmCertifyKey))) {
                    throw new Exception("Not a valid signing key");
                }
                
                X509Certificate decodedAikPemCertificate = X509Util.decodePemCertificate(aikPemCertificate);
                log.debug("AIK Certificate {}", decodedAikPemCertificate.getIssuerX500Principal().getName());
                
                // Need to verify if the AIK is signed by the trusted Privacy CA, which would also ensure that the EK is verified.
                byte[] privacyCAPemBytes;
                try (FileInputStream privacyCAPemFile = new FileInputStream(My.configuration().getPrivacyCaIdentityCacertsFile())) {
                    privacyCAPemBytes = IOUtils.toByteArray(privacyCAPemFile);
                }
                
                X509Certificate privacyCACert = X509Util.decodePemCertificate(new String(privacyCAPemBytes));
                log.debug("Privacy CA Certificate {}", privacyCACert.getIssuerX500Principal().getName());

                if (!isAikCertifiedByPrivacyCA(decodedAikPemCertificate, privacyCACert)) {
                    throw new CertificateException("The specified AIK certificate is not trusted.");
                }
                
                if (!CertifyKey.isCertifiedKeySignatureValid(tpmCertifyKey, tpmCertifyKeySignature, decodedAikPemCertificate.getPublicKey())) {
                    throw new CertificateException("The signature specified for the certifiy key does not match.");
                }
                
                boolean validatePublicKeyDigest = validatePublicKeyDigest(publicKeyModulus, tpmCertifyKey);
                if (!validatePublicKeyDigest) {
                    throw new Exception("Public key specified does not map to the public key digest in the TCG signing key certificate");
                }

                // Generate the TCG standard exponent to create the RSApublic key from the modulus specified.
                byte[] pubExp = new byte[3];
                pubExp[0] = (byte) (0x01 & 0xff);
                pubExp[1] = (byte) (0x00);
                pubExp[2] = (byte) (0x01 & 0xff);
                RSAPublicKey pubBk = TpmUtils.makePubKey(publicKeyModulus, pubExp);

                if (pubBk != null) {
                    log.debug("Successfully created the public key from the modulus specified");
                } else {
                    throw new Exception("Error during the creation of the public key from the modulus and exponent");
                }

                // first load the ca key
                byte[] combinedPrivateKeyAndCertPemBytes;
                try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
                    combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
                }
                
                PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
                X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
                X509Builder caBuilder = X509Builder.factory();
                X509Certificate bkCert = caBuilder
                        .commonName("CN=Signing_Key_Certificate")
                        .subjectPublicKey(pubBk)
                        .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                        .issuerPrivateKey(cakey)
                        .issuerName(cacert)
                        .keyUsageDigitalSignature()
                        .keyUsageNonRepudiation()
                        .extKeyUsageIsCritical()
                        .randomSerial()
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_OID, tpmCertifyKey)
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID, tpmCertifyKeySignature)
                        .build();

                if (bkCert != null) {
                    signingKeyPemCertificate = X509Util.encodePemCertificate(bkCert);
                } else {
                    throw new Exception("Error during creation of the MTW signed signing key certificate");
                }

                log.debug("Successfully created the MTW signed PEM certificate for signing key: {}.", X509Util.encodePemCertificate(bkCert));

            } else {
                throw new Exception("Invalid input specified or input value missing.");
            }
        } catch (Exception ex) {
            log.error("Error during MTW signed signing key certificate.", ex);
            throw new RepositoryCreateException();
        }
    }
    
}
