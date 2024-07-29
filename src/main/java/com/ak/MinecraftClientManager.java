package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MinecraftClientManager {
    private static final Logger log = LoggerFactory.getLogger(MinecraftClientManager.class);

    public static void startMainThread(String host, int port, int iterations, int sleepTime, List<ProxyInfo> proxies) {
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
