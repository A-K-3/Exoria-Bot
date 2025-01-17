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

    public static void login(final String username, final ProxyInfo proxy, final String host, final int port) {
        MinecraftProtocol protocol = new MinecraftProtocol(username);
        SessionService sessionService = new SessionService();
        sessionService.setProxy(proxy);

        Session client = new TcpClientSession(host, port, protocol, proxy);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);

        log.info("Logging in as {} with proxy {} {}", username, proxy.type(), proxy.address());

        client.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                if (!event.getReason().toString().contains("Connection timed out")) {
                    log.info("Disconnected: {}", event.getReason());
                }
            }

            @Override
            public void disconnecting(DisconnectingEvent event) {
                if (!event.getReason().toString().contains("Connection timed out")) {
                    log.info("Disconnecting: {}", event.getReason());
                }
            }

            @Override
            public void packetReceived(Session session, Packet packet) {
                if (packet instanceof ClientboundLoginPacket) {
                    log.info("Connected: {}", username);
                    session.send(new ServerboundChatPacket("Hallo", Instant.now().toEpochMilli(), 0L, null, 0, new BitSet()));
                    incrementConnecteds();
                }
            }
        });

        client.connect();
        log.info("Total connected clients: {}", getConnecteds());
    }

    private static synchronized void incrementConnecteds() {
        connecteds++;
    }

    public static synchronized int getConnecteds() {
        return connecteds;
    }
}
