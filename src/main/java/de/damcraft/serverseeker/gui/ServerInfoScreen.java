package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.ssapi.requests.ServerInfoRequest;
import de.damcraft.serverseeker.ssapi.responses.Server;
import de.damcraft.serverseeker.ssapi.responses.ServerInfoResponse;
import de.damcraft.serverseeker.utils.TimeUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.List;

import static de.damcraft.serverseeker.ServerSeeker.gson;

public class ServerInfoScreen extends WindowScreen {
    private final String serverIp;

    public ServerInfoScreen(String serverIp) {
        super(GuiThemes.get(), "Server Info: " + serverIp);
        this.serverIp = serverIp;
    }

    @Override
    public void initWidgets() {
        add(theme.label("Fetching server info..."));
        HostAndPort hap = HostAndPort.fromString(serverIp);
        String ip = hap.getHost();
        Integer port = hap.getPort();

        Server server = gson.fromJson(SmallHttp.get("https://data.minescan.xyz/server/" + ip), Server.class);

        if (server.isError()) {
            clear();
            add(theme.label(server.error)).expandX();
            return;
        }

        ServerInfoRequest request = new ServerInfoRequest();
        request.setIpPort(ip, port);
        ServerInfoResponse who = gson.fromJson(SmallHttp.get(request.url()), ServerInfoResponse.class);

        clear();

        Boolean cracked = server.cracked();
        String description = server.motd == null ? "" : server.motd;
        int onlinePlayers = server.onlinePlayers;
        int maxPlayers = server.maxPlayers;
        String version = server.version == null ? "Unknown" : server.version;
        String lastSeen = TimeUtil.format(server.lastSeen);
        String software = server.software == null ? "Unknown" : server.software;
        List<ServerInfoResponse.Player> players = who.players == null ? List.of() : who.players;

        WTable dataTable = add(theme.table()).widget();
        WTable playersTable = add(theme.table()).expandX().widget();

        dataTable.add(theme.label("Cracked: "));
        dataTable.add(theme.label(cracked == null ? "Unknown" : cracked.toString()));
        dataTable.row();

        dataTable.add(theme.label("Description: "));
        if (description.length() > 100) description = description.substring(0, 100) + "...";
        description = description.replace("\n", "\\n");
        description = description.replace("§r", "");
        dataTable.add(theme.label(description));
        dataTable.row();

        dataTable.add(theme.label("Online Players (last scan): "));
        dataTable.add(theme.label(String.valueOf(onlinePlayers)));
        dataTable.row();

        dataTable.add(theme.label("Max Players: "));
        dataTable.add(theme.label(String.valueOf(maxPlayers)));
        dataTable.row();

        dataTable.add(theme.label("Last Seen: "));
        dataTable.add(theme.label(lastSeen));
        dataTable.row();

        dataTable.add(theme.label("Version: "));
        dataTable.add(theme.label(version));
        dataTable.row();

        dataTable.add(theme.label("Software: "));
        dataTable.add(theme.label(software));

        playersTable.add(theme.label(""));
        playersTable.row();
        playersTable.add(theme.label("Players:"));
        playersTable.row();


        playersTable.add(theme.label("Name ")).expandX();
        playersTable.add(theme.label("Last seen ")).expandX();
        playersTable.row();


        playersTable.add(theme.horizontalSeparator()).expandX();
        playersTable.row();

        for (ServerInfoResponse.Player player : players) {
            String name = player.name;
            String lastSeenFormatted = TimeUtil.format(player.lastSeen);

            playersTable.add(theme.label(name + " ")).expandX();
            playersTable.add(theme.label(lastSeenFormatted + " ")).expandX();
            playersTable.row();
        }
        WButton joinServerButton = add(theme.button("Join this Server")).expandX().widget();
        joinServerButton.action = ()
            -> ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), new ServerAddress(hap.getHost(), hap.getPort()), new ServerData("a", hap.toString(), ServerData.Type.OTHER), false, null);
    }
}
