package net.jo.http;

import java.security.cert.CertificateException;
        import java.security.cert.X509Certificate;

        import javax.net.ssl.X509TrustManager;

public class SSLTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
