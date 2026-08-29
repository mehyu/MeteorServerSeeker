package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.country.Country;
import de.damcraft.serverseeker.country.CountrySetting;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.Server;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

import static de.damcraft.serverseeker.ServerSeeker.gson;

public class FindNewServersScreen extends WindowScreen {
    public static CompoundTag savedSettings;
    private int timer;
    public WButton findButton;
    private boolean threadHasFinished;
    private String threadError;
    private List<Server> threadServers;

    public enum Cracked {
        Any,
        Yes,
        No;

        public String toAuthmode() {
            return switch (this) {
                case Yes -> "offline";
                case No -> "online";
                default -> null;
            };
        }
    }

    public enum Version {
        Current,
        Any,
        VersionString;

        @Override
        public String toString() {
            return switch (this) {
                case Current -> "Current";
                case Any -> "Any";
                case VersionString -> "Version String";
            };
        }
    }

    public enum Sort {
        LastSeen,
        Players,
        Version;

        @Override
        public String toString() {
            return switch (this) {
                case LastSeen -> "lastseen";
                case Players -> "players";
                case Version -> "version";
            };
        }
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    WContainer settingsContainer;

    private final Setting<Cracked> crackedSetting = sg.add(new EnumSetting.Builder<Cracked>()
        .name("cracked")
        .description("Whether the server should be cracked or not")
        .defaultValue(Cracked.Any)
        .build()
    );

    private final Setting<Integer> minPlayersSetting = sg.add(new IntSetting.Builder()
        .name("minimum-online-players")
        .description("The minimum amount of online players the server should have")
        .defaultValue(1)
        .min(0)
        .noSlider()
        .build()
    );

    private final Setting<ServersRequest.Software> softwareSetting = sg.add(new EnumSetting.Builder<ServersRequest.Software>()
        .name("software")
        .description("The server software the servers should have")
        .defaultValue(ServersRequest.Software.Any)
        .build()
    );

    private final Setting<Version> versionSetting = sg.add(new EnumSetting.Builder<Version>()
        .name("version")
        .description("The version the servers should have")
        .defaultValue(Version.Current)
        .build()
    );

    private final Setting<String> versionStringSetting = sg.add(new StringSetting.Builder()
        .name("version-string")
        .description("The version string (e.g. 1.19.3) the servers should have")
        .defaultValue(SharedConstants.getCurrentVersion().name())
        .visible(() -> versionSetting.get() == Version.VersionString)
        .build()
    );

    private final Setting<Sort> sortSetting = sg.add(new EnumSetting.Builder<Sort>()
        .name("sort")
        .description("How the results should be sorted")
        .defaultValue(Sort.LastSeen)
        .build()
    );

    private final Setting<Country> countrySetting = sg.add(new CountrySetting.Builder()
        .name("country")
        .description("The country the server should be located in")
        .defaultValue(ServerSeeker.COUNTRY_MAP.get("UN"))
        .build()
    );

    JoinMultiplayerScreen multiplayerScreen;


    public FindNewServersScreen(JoinMultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find new servers");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        loadSettings();
        onClosed(this::saveSettings);
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings));
        add(theme.button("Reset all")).expandX().widget().action = this::resetSettings;
        findButton = add(theme.button("Find")).expandX().widget();
        findButton.action = () -> {
            ServersRequest request = new ServersRequest();

            request.setAuthmode(crackedSetting.get().toAuthmode());
            request.setMinPlayers(minPlayersSetting.get());
            request.setSoftware(softwareSetting.get());
            request.setSort(sortSetting.get().toString());

            switch (versionSetting.get()) {
                case Current -> request.setVersion(SharedConstants.getCurrentVersion().name());
                case VersionString -> request.setVersion(versionStringSetting.get());
            }

            if (!countrySetting.get().name.equalsIgnoreCase("any")) {
                request.setCountryCode(countrySetting.get().code);
            }

            this.locked = true;

            this.threadHasFinished = false;
            this.threadError = null;
            this.threadServers = null;


            MeteorExecutor.execute(() -> {
                String jsonResp = SmallHttp.get(request.url());

                ServersResponse resp = gson.fromJson(jsonResp, ServersResponse.class);

                // Set error message if there is one
                if (resp.isError()) {
                    this.threadError = resp.error;
                    this.threadHasFinished = true;
                    return;
                }
                this.threadServers = resp.servers;
                this.threadHasFinished = true;
            });
        };
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);

        if (threadHasFinished) handleThreadFinish();

        if (locked) {
            if (timer > 2) {
                findButton.set(getNext(findButton));
                timer = 0;
            }
            else {
                timer++;
            }
        }

        else if (!findButton.getText().equals("Find")) {
            findButton.set("Find");
        }
    }

    @Override
    protected void onClosed() {
        ServerSeeker.COUNTRY_MAP.values().forEach(Country::dispose);
    }

    private String getNext(WButton add) {
        return switch (add.getText()) {
            case "Find", "oo0" -> "ooo";
            case "ooo" -> "0oo";
            case "0oo" -> "o0o";
            case "o0o" -> "oo0";
            default -> "Find";
        };
    }

    private void handleThreadFinish() {
        this.threadHasFinished = false;
        this.locked = false;
        if (this.threadError != null) {
            clear();
            add(theme.label(this.threadError)).expandX();
            WButton backButton = add(theme.button("Back")).expandX().widget();
            backButton.action = this::reload;
            this.locked = false;
            return;
        }
        clear();
        List<Server> servers = this.threadServers;

        if (servers == null || servers.isEmpty()) {
            add(theme.label("No servers found")).expandX();
            WButton backButton = add(theme.button("Back")).expandX().widget();
            backButton.action = this::reload;
            this.locked = false;
            return;
        }
        add(theme.label("Found " + servers.size() + " servers")).expandX();
        WButton addAllButton = add(theme.button("Add all")).expandX().widget();
        addAllButton.action = () -> {
            for (Server server : servers) {
                String ip = server.address();

                // Add server to list
                MultiplayerScreenUtil.addNameIpToServerList(multiplayerScreen, "ServerSeeker " + ip, ip, false);
            }
            MultiplayerScreenUtil.saveList(multiplayerScreen);

            // Reload widget
            MultiplayerScreenUtil.reloadServerList(multiplayerScreen);

            // Close screen
            if (this.minecraft == null) return;
            minecraft.setScreen(this.multiplayerScreen);
        };

        WTable table = add(theme.table()).widget();

        table.add(theme.label("Server IP"));
        table.add(theme.label("Version"));
        table.add(theme.label("Players"));


        table.row();

        table.add(theme.horizontalSeparator()).expandX();
        table.row();


        for (Server server : servers) {
            final String serverIP = server.address();
            String serverVersion = server.version == null ? "Unknown" : server.version;

            table.add(theme.label(serverIP));
            table.add(theme.label(serverVersion));
            table.add(theme.label(server.onlinePlayers + "/" + server.maxPlayers));

            WButton addServerButton = theme.button("Add Server");
            addServerButton.action = () -> {
                ServerData info = new ServerData("ServerSeeker " + serverIP, serverIP, ServerData.Type.OTHER);
                MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info);
                addServerButton.visible = false;
            };

            WButton joinServerButton = theme.button("Join Server");
            HostAndPort hap = HostAndPort.fromString(serverIP);

            joinServerButton.action = ()
                -> ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), new ServerAddress(hap.getHost(), hap.getPort()), new ServerData("a", hap.toString(), ServerData.Type.OTHER), false, null);

            WButton serverInfoButton = theme.button("Server Info");
            serverInfoButton.action = () -> this.minecraft.setScreen(new ServerInfoScreen(serverIP));

            table.add(addServerButton);
            table.add(joinServerButton);
            table.add(serverInfoButton);

            table.row();
        }

        this.locked = false;
    }

    public void saveSettings() {
        savedSettings = sg.toTag();
    }

    public void loadSettings() {
        if (savedSettings == null) return;
        sg.fromTag(savedSettings);
    }

    public void resetSettings() {
        for (Setting<?> setting : sg) {
            setting.reset();
        }
        saveSettings();
        reload();
    }
}
