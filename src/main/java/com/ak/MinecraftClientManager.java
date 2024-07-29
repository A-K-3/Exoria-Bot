package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MinecraftClientManager {
    private static final Logger log = LoggerFactory.getLogger(MinecraftClientManager.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startMainThread(String host, int port, int iterations, int sleepTime, List<ProxyInfo> proxies) {
        if (proxies == null || proxies.isEmpty()) {
            log.warn("No proxies available.");
            return;
        }

        Runnable task = () -> {
            for (int i = 0; i < iterations; i++) {
                scheduler.schedule(() -> startClient(host, port, proxies), (long) i * sleepTime, TimeUnit.MILLISECONDS);
            }
        };

        Thread mainThread = new Thread(task);
        mainThread.start();
    }

    private static void startClient(String host, int port, List<ProxyInfo> proxies) {
        ProxyInfo proxy = proxies.get((int) (Math.random() * proxies.size()));
        try {
            MinecraftClient.login(UserGenerator.generateRandomUser(), proxy, host, port);
        } catch (Exception e) {
            log.error("Error during login.", e);
        }
    }
}
