package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.ssapi.requests.WhereisRequest;
import de.damcraft.serverseeker.ssapi.responses.WhereisResponse;
import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import de.damcraft.serverseeker.utils.TimeUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.List;

import static de.damcraft.serverseeker.ServerSeeker.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FindPlayerScreen extends WindowScreen {
    private final JoinMultiplayerScreen multiplayerScreen;

    public enum NameOrUUID {
        Name,
        UUID
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<NameOrUUID> nameOrUUID = sg.add(new EnumSetting.Builder<NameOrUUID>()
        .name("name-or-uuid")
        .description("Whether to search by name or UUID.")
        .defaultValue(NameOrUUID.Name)
        .build()
    );

    private String searchText = "";
    private WLabel inputLabel;
    private WHorizontalList inputList;

    WContainer settingsContainer;

    public FindPlayerScreen(JoinMultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Players");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        inputList = add(theme.horizontalList()).expandX().widget();
        inputLabel = inputList.add(theme.label("Name: ")).widget();
        WTextBox inputBox = inputList.add(theme.textBox(searchText)).widget();
        inputBox.minWidth = 300;
        inputBox.action = () -> searchText = inputBox.get();

        add(theme.button("Find Player")).expandX().widget().action = () -> {

            WhereisRequest request = new WhereisRequest();

            switch (nameOrUUID.get()) {
                case Name -> request.setName(searchText);
                case UUID -> request.setUuid(searchText);
            }

            String jsonResponse = SmallHttp.get(request.url());

            WhereisResponse resp = gson.fromJson(jsonResponse, WhereisResponse.class);

            // Set error message if there is one
            if (resp.isError()) {
                clear();
                add(theme.label(resp.error)).expandX();
                return;
            }
            clear();

            List<WhereisResponse.ServerRecord> data = resp.records();
            if (data.isEmpty()) {
                add(theme.label("Not found")).expandX();
                return;
            }
            add(theme.label("Found " + data.size() + " servers:"));
            WTable table = add(theme.table()).widget();
            WButton addAllButton = table.add(theme.button("Add all")).expandX().widget();
            addAllButton.action = () -> addAllServers(data);

            table.row();
            table.add(theme.label("Server IP"));
            table.add(theme.label("Player name"));
            table.add(theme.label("Last seen"));

            table.row();
            table.add(theme.horizontalSeparator()).expandX();
            table.row();


            for (WhereisResponse.ServerRecord server : data) {
                String serverIP = server.ip + ":" + server.port;
                String playerName = server.name;
                String playerLastSeenFormatted = TimeUtil.format(server.lastSeen);

                int minWidth = (int)(mc.getWindow().getGuiScaledWidth() * 0.2);
                table.add(theme.label(serverIP)).minWidth(minWidth);
                table.add(theme.label(playerName)).minWidth(minWidth);
                table.add(theme.label(playerLastSeenFormatted)).minWidth(minWidth);

                WButton addServerButton = theme.button("Add Server");
                addServerButton.action = () -> {
                    ServerData info = new ServerData("ServerSeeker " + serverIP + " (Player: " + playerName + ")", serverIP, ServerData.Type.OTHER);
                    MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info);
                    addServerButton.visible = false;
                };

                HostAndPort hap = HostAndPort.fromString(serverIP);
                WButton joinServerButton = theme.button("Join Server");
                joinServerButton.action = () -> {
                    ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), new ServerAddress(hap.getHost(), hap.getPort()), new ServerData("a", hap.toString(), ServerData.Type.OTHER), false, null);
                };

                WButton serverInfoButton = theme.button("Server Info");
                serverInfoButton.action = () -> this.minecraft.setScreenAndShow(new ServerInfoScreen(serverIP));

                table.add(addServerButton);
                table.add(joinServerButton);
                table.add(serverInfoButton);
                table.row();
            }
        };
    }

    private void addAllServers(List<WhereisResponse.ServerRecord> records) {
        for (WhereisResponse.ServerRecord record : records) {
            String serverIP = record.ip + ":" + record.port;
            String playerName = record.name;
            ServerData info = new ServerData("ServerSeeker " + serverIP + " (Player: " + playerName + ")", serverIP, ServerData.Type.OTHER);
            MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info, false);
        }
        MultiplayerScreenUtil.saveList(multiplayerScreen);
        MultiplayerScreenUtil.reloadServerList(multiplayerScreen);
        if (minecraft == null) return;
        minecraft.setScreenAndShow(this.multiplayerScreen);
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);

        inputLabel.set(nameOrUUID.get() == NameOrUUID.Name ? "Name: " : "UUID: ");
    }
}
