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

import java.time.Instant;
import java.util.BitSet;

public class MinecraftClient {
    private static final Logger log = LoggerFactory.getLogger(MinecraftClient.class);
    private static int connecteds = 0;

    public static void login(String username, ProxyInfo PROXY, String host, int port) {
        MinecraftProtocol protocol = new MinecraftProtocol(username);
        SessionService sessionService = new SessionService();
        sessionService.setProxy(PROXY);

        Session client = new TcpClientSession(host, port, protocol, PROXY);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);

        System.out.println("Logging in as " + username + " with proxy " + PROXY.type() + " " + PROXY.address());

        client.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                if (!event.getReason().toString().contains("Connection timed out")) {
                    log.info("Disconnected: {}" + event.getReason());
                }
            }

            @Override
            public void disconnecting(DisconnectingEvent event) {
                if (!event.getReason().toString().contains("Connection timed out")) {
                    log.info("Disconnecting: {}" + event.getReason());
                }
            }

            @Override
            public void packetReceived(Session session, Packet packet) {
                if (packet instanceof ClientboundLoginPacket) {
                    log.info("Connected: " + username);
                    session.send(new ServerboundChatPacket("Hallo", Instant.now().toEpochMilli(), 0L, null, 0, new BitSet()));
                    connecteds++;
                }
            }
        });

        client.connect();
        log.info("Conectados: " + connecteds);
    }
}