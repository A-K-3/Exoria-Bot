package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProxyLoader {

    private static final Logger log = LoggerFactory.getLogger(ProxyLoader.class);


    public static List<ProxyInfo> loadProxies(String filePath) {
        File proxiesFile = new File(filePath);
        if (!proxiesFile.exists()) {
            try {
                if (proxiesFile.createNewFile()) {
                    log.info("Proxies file not found. Created empty proxies file at " + filePath);
                }
            } catch (IOException e) {
                log.error("Error creating proxies file.", e);
            }
            return new ArrayList<>();
        }
        return ProxyLoader.loadProxies(proxiesFile);
    }

    private static List<ProxyInfo> loadProxies(File proxiesFile) {
        List<ProxyInfo> proxies = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(proxiesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String protocol = "";
                if (line.startsWith("http://")) {
                    protocol = "HTTP";
                    line = line.substring(7);
                } else if (line.startsWith("socks5://")) {
                    protocol = "SOCKS5";
                    line = line.substring(9);
                } else if (line.startsWith("socks4://")) {
                    protocol = "SOCKS4";
                    line = line.substring(9);
                } else {
                    protocol = "SOCKS5";
                }

                String[] parts = line.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);

                ProxyInfo.Type type = ProxyInfo.Type.valueOf(protocol);
                ProxyInfo proxy = new ProxyInfo(type, ip, port);
                proxies.add(proxy);
            }
        } catch (IOException e) {
            System.out.println("Error reading proxies file: " + e.getMessage());
        }

        return proxies;
    }


}