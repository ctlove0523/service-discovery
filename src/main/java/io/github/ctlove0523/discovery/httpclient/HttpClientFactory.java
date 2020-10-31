package io.github.ctlove0523.discovery.httpclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFactory {
	private static final Logger log = LoggerFactory.getLogger(HttpClientFactory.class);

	public HttpClientFactory() {
		throw new UnsupportedOperationException("can not create object of HttpClientFactory");
	}

	private static final OkHttpClient HTTPS_CLIENT = new OkHttpClient.Builder()
			.retryOnConnectionFailure(true)
			.callTimeout(5, TimeUnit.SECONDS)
			.readTimeout(5,TimeUnit.SECONDS)
			.addInterceptor(new ErrorInterceptor())
			.sslSocketFactory(getSslContext().getSocketFactory(),(X509TrustManager)getTrustManger())
			.hostnameVerifier(new HostnameVerifier() {
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			})
			.protocols(Arrays.asList(Protocol.HTTP_1_1,Protocol.HTTP_2))
			.build();


	public static OkHttpClient getHttpsClient() {
		return HTTPS_CLIENT;
	}

	private static SSLContext getSslContext() {
		SSLContext context = null;
		try {
			context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] {getTrustManger()}, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			log.error("not support ssl algorithm",e);
		} catch (KeyManagementException e) {
			log.error("key manager exception", e);
		}

		return context;
	}

	private static TrustManager getTrustManger() {
		return new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
	}
}
