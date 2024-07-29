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
import java.util.stream.Collectors;

public class ProxyLoader {

    private static final Logger log = LoggerFactory.getLogger(ProxyLoader.class);

    public static List<ProxyInfo> loadProxies(String filePath) {
        Path proxiesPath = Paths.get(filePath);
        if (Files.notExists(proxiesPath)) {
            try {
                Files.createFile(proxiesPath);
                log.info("Proxies file not found. Created empty proxies file at {}", filePath);
            } catch (IOException e) {
                log.error("Error creating proxies file.", e);
            }
            return new ArrayList<>();
        }
        return loadProxiesFromFile(proxiesPath);
    }

    private static List<ProxyInfo> loadProxiesFromFile(Path proxiesPath) {
        try (BufferedReader br = Files.newBufferedReader(proxiesPath)) {
            return br.lines()
                    .map(ProxyLoader::parseProxyLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error reading proxies file: ", e);
            return new ArrayList<>();
        }
    }

    private static ProxyInfo parseProxyLine(String line) {
        String protocol = determineProtocol(line);
        if (protocol.isEmpty()) {
            log.warn("Unknown protocol in line: {}", line);
            return null;
        }

        String[] parts = line.split(":");
        if (parts.length != 2) {
            log.warn("Invalid proxy format in line: {}", line);
            return null;
        }

        try {
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            ProxyInfo.Type type = ProxyInfo.Type.valueOf(protocol);
            return new ProxyInfo(type, ip, port);
        } catch (NumberFormatException e) {
            log.warn("Invalid port number in line: {}", line);
            return null;
        }
    }

    private static String determineProtocol(String line) {
        if (line.startsWith("http://")) {
            return "HTTP";
        } else if (line.startsWith("socks5://")) {
            return "SOCKS5";
        } else if (line.startsWith("socks4://")) {
            return "SOCKS4";
        } else {
            return "";
        }
    }
}
