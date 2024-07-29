package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExoriaBot {
    private static final Logger log = LoggerFactory.getLogger(ExoriaBot.class);
    private static final String PROXIES_FILE_PATH = "./proxies.txt";
    private static int connecteds = 0;

    public static void main(String[] args) {
        if (args.length < 3) {
            log.error("Usage: java ExoriaBot <host> <port> |<iterations>| <sleepTime>");
            return;
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            log.error("Port must be a number.");
            return;
        }

        int[] params = parseArguments(args);
        int iterations = params[0];
        int sleepTime = params[1];

        List<ProxyInfo> proxies = loadProxies(PROXIES_FILE_PATH);
        if (proxies.isEmpty()) {
            log.error("No proxies found in " + PROXIES_FILE_PATH);
            return;
        }

        startMainThread(host, port, iterations, sleepTime, proxies);
    }

    private static int[] parseArguments(String[] args) {
        int iterations;
        int sleepTime;
        try {
            if (args.length == 3) {
                iterations = Integer.MAX_VALUE; // Infinite loop
                sleepTime = Integer.parseInt(args[2]);
            } else {
                iterations = Integer.parseInt(args[2]);
                sleepTime = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException e) {
            log.error("Iterations and sleepTime must be numbers.");
            throw new IllegalArgumentException("Iterations and sleepTime must be numbers.", e);
        }
        return new int[]{iterations, sleepTime};
    }

    private static List<ProxyInfo> loadProxies(String filePath) {
        File proxiesFile = new File(filePath);
        if (!proxiesFile.exists()) {
            try {
                if (proxiesFile.createNewFile()) {
                    log.info("Proxies file not found. Created empty proxies file at {}", filePath);
                }
            } catch (IOException e) {
                log.error("Error creating proxies file.", e);
            }
            return new ArrayList<>();
        }
        return ProxyLoader.loadProxies(proxiesFile);
    }

    private static void startMainThread(String host, int port, int iterations, int sleepTime, List<ProxyInfo> proxies) {
        Thread mainThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.warn("Main thread interrupted.", e);
                    Thread.currentThread().interrupt();
                    return;
                }

                Thread thread = new Thread(() -> {
                    try {
                        MinecraftClient.login(UserGenerator.generateRandomUser(), proxies.get((int) (Math.random() * proxies.size())), host, port);
                    } catch (Exception e) {
                        log.error("Error during login.", e);
                    }
                });
                thread.start();
            }
        });
        mainThread.start();
    }
}
