package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExoriaBot {
    private static final Logger log = LoggerFactory.getLogger(ExoriaBot.class);
    private static final String PROXIES_FILE_PATH = "./proxies.txt";

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

        List<ProxyInfo> proxies = ProxyLoader.loadProxies(PROXIES_FILE_PATH);
        if (proxies.isEmpty()) {
            log.error("No proxies found in " + PROXIES_FILE_PATH);
            return;
        }

        MinecraftClientManager.startMainThread(host, port, iterations, sleepTime, proxies);
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
}
