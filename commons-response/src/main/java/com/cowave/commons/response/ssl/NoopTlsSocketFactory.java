/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.response.ssl;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 *
 * @author shanhuiming
 *
 */
public class NoopTlsSocketFactory extends SSLSocketFactory implements X509TrustManager, X509KeyManager {

    private final SSLSocketFactory sslSocket;

    public NoopTlsSocketFactory() throws Exception {
		// 忽略证书
        TrustManager[] trustAllCertificates = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
					// 接受所有证书
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // 忽略客户端证书校验
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // 忽略服务器证书校验
                }
            }
        };
        SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, trustAllCertificates, new SecureRandom());
		sslSocket = sslcontext.getSocketFactory();
	}

    @Override
	public String[] getDefaultCipherSuites() {
		return null;
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return null;
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return sslSocket.createSocket(s, host, port, autoClose);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return sslSocket.createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return sslSocket.createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		return sslSocket.createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return sslSocket.createSocket(address, port, localAddress, localPort);
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return null;
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return null;
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return null;
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return null;
	}

	@Override
    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
