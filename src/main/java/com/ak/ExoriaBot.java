package com.ak;

import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectingEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpClientSession;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

public class ExoriaBot {
    private static final Logger log = LoggerFactory.getLogger(ExoriaBot.class);
    private static int connecteds = 0;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java ExoriaBot <host> <port> <iterations> <sleepTime>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int iterations = Integer.parseInt(args[2]);
        int sleepTime = Integer.parseInt(args[3]);
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

        Thread mainThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Thread thread = new Thread(() -> {
                    login(generateRandomUser(), proxies.get((int) (Math.random() * proxies.size())), host, port);
                });
                thread.start();
            }
        });

        mainThread.start();
    }

    private static String generateRandomUser() {
        String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return java.util.stream.IntStream.range(0, ThreadLocalRandom.current().nextInt(3, 17))
                .mapToObj(i -> String.valueOf(ALPHANUMERIC.charAt(ThreadLocalRandom.current().nextInt(ALPHANUMERIC.length()))))
                .collect(java.util.stream.Collectors.joining());
    }

    private static void login(String username, ProxyInfo PROXY, String host, int port) {
        MinecraftProtocol protocol = new MinecraftProtocol(username);
        SessionService sessionService = new SessionService();
        sessionService.setProxy(PROXY);

        Session client = new TcpClientSession(host, port, protocol, PROXY);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);

        System.out.println("Logging in as " + username + " with proxy " + PROXY.type() + " " + PROXY.address());

        client.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                log.info("Disconnected: {}" + event.getReason());
                client.connect(true);
            }

            @Override
            public void disconnecting(DisconnectingEvent event) {
                log.info("DisconnectingEvent: {}", event.getReason());
                client.connect(true);
            }

            @Override
            public void packetReceived(Session session, Packet packet) {
                if (packet instanceof ClientboundLoginPacket) {
                    log.info("Connected: " + username);
                    connecteds++;
                    while (session.isConnected()) {
                        session.send(new ServerboundChatPacket("Hallo", Instant.now().toEpochMilli(), 0L, null, 0, new BitSet()));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        client.connect();
        log.info("Conectados: " + connecteds);
    }
}