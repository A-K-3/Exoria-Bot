package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProxyLoader {

    private static final Logger log = LoggerFactory.getLogger(ProxyLoader.class);

    public static List<ProxyInfo> loadProxies(String filePath) {
        Path proxiesPath = Paths.get(filePath);
        if (Files.notExists(proxiesPath)) {
            try {
                Files.createFile(proxiesPath);
                log.info("Proxies file not found. Created empty proxies file at " + filePath);
            } catch (IOException e) {
                log.error("Error creating proxies file.", e);
            }
            return new ArrayList<>();
        }
        return ProxyLoader.loadProxies(proxiesPath);
    }

    private static List<ProxyInfo> loadProxies(Path proxiesPath) {
        List<ProxyInfo> proxies = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(proxiesPath)) {
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
                    log.warn("Unknown protocol in line: " + line);
                    continue;
                }

                String[] parts = line.split(":");
                if (parts.length != 2) {
                    log.warn("Invalid proxy format in line: " + line);
                    continue;
                }

                try {
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);

                    ProxyInfo.Type type = ProxyInfo.Type.valueOf(protocol);
                    ProxyInfo proxy = new ProxyInfo(type, ip, port);
                    proxies.add(proxy);
                } catch (NumberFormatException e) {
                    log.warn("Invalid port number in line: " + line);
                }
            }
        } catch (IOException e) {
            log.error("Error reading proxies file: ", e);
        }

        return proxies;
    }
}
