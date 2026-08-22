package de.damcraft.serverseeker.gui;

import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.ssapi.requests.ServerInfoRequest;
import de.damcraft.serverseeker.ssapi.responses.Server;
import de.damcraft.serverseeker.ssapi.responses.ServerInfoResponse;
import de.damcraft.serverseeker.utils.TimeUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

import java.util.List;

import static de.damcraft.serverseeker.ServerSeeker.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GetInfoScreen extends WindowScreen {
    ServerSelectionList.Entry entry;

    public GetInfoScreen(JoinMultiplayerScreen multiplayerScreen, ServerSelectionList.Entry entry) {
        super(GuiThemes.get(), "Get players");
        this.parent = multiplayerScreen;
        this.entry = entry;
    }

    @Override
    public void initWidgets() {
        if (entry == null) {
            add(theme.label("No server selected"));
            return;
        }

        if (!(entry instanceof ServerSelectionList.OnlineServerEntry)) {
            add(theme.label("No server selected"));
            return;
        }
        ServerData serverInfo = ((ServerSelectionList.OnlineServerEntry) entry).getServerData();
        if (serverInfo == null) {
            add(theme.label("No server selected"));
            return;
        }
        String address = serverInfo.ip;

        // Check if the server matches the regex for ip(:port)
        if (!address.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?$")) {
            add(theme.label("You can only get player info for servers with an IP address"));
            return;
        }
        String ip = address.split(":")[0];
        int port = address.split(":").length > 1 ? Integer.parseInt(address.split(":")[1]) : 25565;

        add(theme.label("Loading..."));

        ServerInfoRequest request = new ServerInfoRequest();
        request.setIpPort(ip, port);

        String jsonResp = SmallHttp.get(request.url());

        ServerInfoResponse resp = gson.fromJson(jsonResp, ServerInfoResponse.class);

        // Set error message if there is one
        if (resp.isError()) {
            clear();
            add(theme.label(resp.error)).expandX();
            return;
        }

        clear();
        List<ServerInfoResponse.Player> players = resp.players;
        if (players == null || players.isEmpty()) {
            add(theme.label("No records of players found.")).expandX();
            return;
        }

        // Determine whether the server is cracked from the server metadata
        Server server = gson.fromJson(SmallHttp.get("https://data.minescan.xyz/server/" + ip), Server.class);
        Boolean cracked = server.isError() ? null : server.cracked();

        if (cracked != null && !cracked) {
            add(theme.label("Attention: The server is NOT cracked!")).expandX();
            add(theme.label("")).expandX();
        } else if (cracked == null) {
            add(theme.label("Attention: The server's auth mode is unknown!")).expandX();
            add(theme.label("")).expandX();
        }

        String playersLabel = players.size() == 1 ? " player:" : " players:";
        add(theme.label("Found " + players.size() + playersLabel));

        WTable table = add(theme.table()).widget();

        table.add(theme.label("Name "));
        table.add(theme.label("Last seen "));
        table.add(theme.label("Login (cracked)"));
        table.row();

        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        for (ServerInfoResponse.Player player : players) {
            String name = player.name;
            String lastSeenFormatted = TimeUtil.format(player.lastSeen);

            table.add(theme.label(name + " "));
            table.add(theme.label(lastSeenFormatted + " "));

            if (mc.getUser().getName().equals(name)) {
                table.add(theme.label("Logged in")).expandCellX();
            } else {

                WButton loginButton = table.add(theme.button("Login")).widget();
                // Check if the user is currently logged in
                if (mc.getUser().getName().equals(name)) {
                    loginButton.visible = false;
                }

                // Log in the user
                loginButton.action = () -> {
                    loginButton.visible = false;
                    if (this.minecraft == null) return;
                    // Check if the account already exists
                    boolean exists = false;
                    for (Account<?> account : Accounts.get()) {
                        if (account instanceof CrackedAccount && account.getUsername().equals(name)) {
                            account.login();
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        CrackedAccount account = new CrackedAccount(name);
                        account.login();
                        Accounts.get().add(account);
                    }
                    onClose();
                };
            }
            table.row();
        }
    }
}
