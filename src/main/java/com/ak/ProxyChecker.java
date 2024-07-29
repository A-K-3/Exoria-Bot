package com.ak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyChecker {

    public static boolean isProxyWorking(String proxyHost, int proxyPort) {
        try {

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            URL url = new URL("http://test.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(10000); // 5 seconds timeout
            connection.connect();
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            System.out.println("Proxy is not working: " + e.getMessage());
            return false;
        }
    }
}
