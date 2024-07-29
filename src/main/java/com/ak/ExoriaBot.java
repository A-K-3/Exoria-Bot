package com.ak;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ExoriaBot {
    private static final Logger log = LoggerFactory.getLogger(ExoriaBot.class);
    private static int connecteds = 0;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ExoriaBot <host> <port> |<iterations>| <sleepTime>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int iterations;
        int sleepTime;

        if (args.length == 3) {
            iterations = Integer.MAX_VALUE; // Infinite loop
            sleepTime = Integer.parseInt(args[2]);
        } else {
            iterations = Integer.parseInt(args[2]);
            sleepTime = Integer.parseInt(args[3]);
        }

        String proxiesFilePath = "./proxies.txt";

        File proxiesFile = new File(proxiesFilePath);
        if (!proxiesFile.exists()) {
            try {
                proxiesFile.createNewFile();
                System.out.println("Proxies file not found. Created empty proxies file at " + proxiesFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        ArrayList<ProxyInfo> proxies = (ArrayList<ProxyInfo>) ProxyLoader.loadProxies(proxiesFile);

        if (proxies.isEmpty()) {
            System.out.println("No proxies found in " + proxiesFilePath);
            return;
        }

        Thread mainThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Thread thread = new Thread(() -> {
                    MinecraftClient.login(UserGenerator.generateRandomUser(), proxies.get((int) (Math.random() * proxies.size())), host, port);
                });
                thread.start();
            }
        });

        mainThread.start();
    }
}