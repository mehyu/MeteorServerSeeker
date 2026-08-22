package de.damcraft.serverseeker.utils;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.hud.HistoricPlayersHud;
import de.damcraft.serverseeker.ssapi.requests.ServerInfoRequest;
import de.damcraft.serverseeker.ssapi.responses.Server;
import de.damcraft.serverseeker.ssapi.responses.ServerInfoResponse;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.damcraft.serverseeker.ServerSeeker.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HistoricPlayersUpdater {
    @EventHandler
    private static void onGameJoinEvent(GameJoinedEvent ignoredEvent) {
        // Run in a new thread
        new Thread(HistoricPlayersUpdater::update).start();
    }

    public static void update() {
        // If the Hud contains the HistoricPlayersHud, update the players
        List<HistoricPlayersHud> huds = new ArrayList<>();
        for (HudElement hudElement : Hud.get()) {
            if (hudElement instanceof HistoricPlayersHud && hudElement.isActive()) {
                huds.add((HistoricPlayersHud) hudElement);
            }
        }
        if (huds.isEmpty()) return;

        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) return;

        HostAndPort hap = HostAndPort.fromString(serverData.ip);
        String ip = hap.getHost();
        Integer port = hap.getPort();

        ServerInfoRequest request = new ServerInfoRequest();
        request.setIpPort(ip, port);

        String jsonResp = SmallHttp.get(request.url());

        ServerInfoResponse resp = gson.fromJson(jsonResp, ServerInfoResponse.class);

        Server server = gson.fromJson(SmallHttp.get("https://data.minescan.xyz/server/" + ip), Server.class);
        boolean cracked = !server.isError() && server.cracked() != null && server.cracked();

        for (HistoricPlayersHud hud : huds) {
            hud.players = Objects.requireNonNullElseGet(resp.players, List::of);
            hud.isCracked = cracked;
        }
    }
}
