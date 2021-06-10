package com.globalnest.network;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSSocketFactory extends SSLSocketFactory implements LayeredSocketFactory{
	private SSLSocketFactory internalSSLSocketFactory;

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if(socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }

	@Override
	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort,
			HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		// TODO Auto-generated method stub
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
	    int soTimeout = HttpConnectionParams.getSoTimeout(params);

	    InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
	    SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

	    if ((localAddress != null) || (localPort > 0)) {
	        // we need to bind explicitly
	        if (localPort < 0) {
	            localPort = 0; // indicates "any"
	        }
	        InetSocketAddress isa = new InetSocketAddress(localAddress,
	                localPort);
	        sslsock.bind(isa);
	    }

	    sslsock.connect(remoteAddress, connTimeout);
	    sslsock.setSoTimeout(soTimeout);
	    return sslsock;
	}

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return true;
	}
}
