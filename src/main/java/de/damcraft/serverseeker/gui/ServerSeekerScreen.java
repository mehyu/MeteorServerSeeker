package de.damcraft.serverseeker.gui;

import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;

public class ServerSeekerScreen extends WindowScreen {
    private final JoinMultiplayerScreen multiplayerScreen;

    public ServerSeekerScreen(JoinMultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "ServerSeeker");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        add(theme.label("Powered by the MineScan API (data.minescan.xyz)")).expandX();

        WHorizontalList widgetList = add(theme.horizontalList()).expandX().widget();
        WButton newServersButton = widgetList.add(this.theme.button("Find new servers")).expandX().widget();
        WButton findPlayersButton = widgetList.add(this.theme.button("Search players")).expandX().widget();
        WButton cleanUpServersButton = widgetList.add(this.theme.button("Clean up")).expandX().widget();
        newServersButton.action = () -> {
            if (this.minecraft == null) return;
            this.minecraft.setScreenAndShow(new FindNewServersScreen(this.multiplayerScreen));
        };
        findPlayersButton.action = () -> {
            if (this.minecraft == null) return;
            this.minecraft.setScreenAndShow(new FindPlayerScreen(this.multiplayerScreen));
        };
        cleanUpServersButton.action = () -> {
            if (this.minecraft == null) return;
            clear();
            add(theme.label("Are you sure you want to clean up your server list?"));
            add(theme.label("This will remove all servers that start with \"ServerSeeker\""));
            WHorizontalList buttonList = add(theme.horizontalList()).expandX().widget();
            WButton backButton = buttonList.add(theme.button("Back")).expandX().widget();
            backButton.action = this::reload;
            WButton confirmButton = buttonList.add(theme.button("Confirm")).expandX().widget();
            confirmButton.action = this::cleanUpServers;
        };
    }

    public void cleanUpServers() {
        if (this.minecraft == null) return;

        for (int i = 0; i < this.multiplayerScreen.getServers().size(); i++) {
            ServerData server = this.multiplayerScreen.getServers().get(i);
            if (server.name.startsWith("ServerSeeker")) {
                this.multiplayerScreen.getServers().remove(server);
                i--;
            }
        }

        MultiplayerScreenUtil.saveList(multiplayerScreen);
        MultiplayerScreenUtil.reloadServerList(multiplayerScreen);

        minecraft.setScreenAndShow(this.multiplayerScreen);
    }
}
