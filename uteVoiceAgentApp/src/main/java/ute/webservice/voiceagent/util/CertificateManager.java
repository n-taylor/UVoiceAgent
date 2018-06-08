package ute.webservice.voiceagent.util;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ute.webservice.voiceagent.dao.LocationDAO;

/**
 * This class manages the certificates and SSLContext that the activities in this
 * application will use.
 */

public class CertificateManager {
    /**
     * A singleton SSLContext. It is set in getSSLContext.
     */
    private static SSLContext sslContext = null;

    private static SSLContext sslContextEBC = null;
    private static SSLContext sslContextPark = null;

    private static CertificateFactory cf = null;
    private static Certificate ca;

    /**
     * Gets the current SSLContext for the given campus. If one has not been set yet, creates a new SSLContext
     */
    public static SSLContext getSSlContextByCampus(Context context, String fileName, int campus){
        if (campus == LocationDAO.PARK && sslContextPark != null)
            return sslContextPark;
        else if (campus == LocationDAO.EBC && sslContextEBC != null)
            return sslContextEBC;
        else{
            SSLContext toSet = null;
            System.out.println("working:"+System.getProperty("user.dir"));
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            //CertificateFactory cf = null;
            try {
                cf = CertificateFactory.getInstance("X.509");
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = null;
            try {
                InputStream is = context.getResources().getAssets().open(fileName);
                caInput = new BufferedInputStream(is);
//                caInput = new BufferedInputStream(activity.getBaseContext().getAssets().open("mse-park.crt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } catch (CertificateException e) {
                e.printStackTrace();
            } finally {
                try {
                    caInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance(keyStoreType);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            try {
                keyStore.load(null, null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            try {
                keyStore.setCertificateEntry("ca", ca);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = null;
            try {
                tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                tmf.init(keyStore);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            // Create an SSLContext that uses our TrustManager

            try {
                toSet = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                toSet.init(null, tmf.getTrustManagers(), null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            if (campus == LocationDAO.EBC)
                sslContextEBC = toSet;
            else if (campus == LocationDAO.PARK)
                sslContextPark = toSet;
        }
        return (campus == LocationDAO.EBC) ? sslContextEBC : sslContextPark;
    }

    /**
     * Gets the current SSLContext. If one has not been set yet, creates a new SSLContext and
     * @param context
     * @return
     */
    public static SSLContext getSSlContext(Context context, String fileName){
        if (sslContext != null)
            return sslContext;
        else{
            System.out.println("working:"+System.getProperty("user.dir"));
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            //CertificateFactory cf = null;
            try {
                cf = CertificateFactory.getInstance("X.509");
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = null;
            try {
                InputStream is = context.getResources().getAssets().open(fileName);
                caInput = new BufferedInputStream(is);
//                caInput = new BufferedInputStream(activity.getBaseContext().getAssets().open("mse-park.crt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } catch (CertificateException e) {
                e.printStackTrace();
            } finally {
                try {
                    caInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance(keyStoreType);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            try {
                keyStore.load(null, null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            try {
                keyStore.setCertificateEntry("ca", ca);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = null;
            try {
                tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                tmf.init(keyStore);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            // Create an SSLContext that uses our TrustManager

            try {
                sslContext = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                sslContext.init(null, tmf.getTrustManagers(), null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }
}
