package de.damcraft.serverseeker.utils;

import de.damcraft.serverseeker.mixin.JoinMultiplayerScreenAccessor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;

public class MultiplayerScreenUtil {

    public static void addInfoToServerList(JoinMultiplayerScreen mps, ServerData info) {
        mps.getServers().add(info, false);
        mps.getServers().save();
        ((JoinMultiplayerScreenAccessor) mps).getServerSelectionList().updateOnlineServers(mps.getServers());
    }

    public static void addInfoToServerList(JoinMultiplayerScreen mps, ServerData info, boolean reload) {
        mps.getServers().add(info, false);
        if (reload) ((JoinMultiplayerScreenAccessor) mps).getServerSelectionList().updateOnlineServers(mps.getServers());
    }

    public static void addNameIpToServerList(JoinMultiplayerScreen mps, String name, String ip) {
        ServerData info = new ServerData(name, ip, ServerData.Type.OTHER);
        mps.getServers().add(info, false);
        ((JoinMultiplayerScreenAccessor) mps).getServerSelectionList().updateOnlineServers(mps.getServers());
        mps.getServers().save();
    }

    public static void addNameIpToServerList(JoinMultiplayerScreen mps, String name, String ip, boolean reload) {
        ServerData info = new ServerData(name, ip, ServerData.Type.OTHER);
        mps.getServers().add(info, false);
        if (reload) ((JoinMultiplayerScreenAccessor) mps).getServerSelectionList().updateOnlineServers(mps.getServers());
    }

    public static void reloadServerList(JoinMultiplayerScreen mps) {
        ((JoinMultiplayerScreenAccessor) mps).getServerSelectionList().updateOnlineServers(mps.getServers());
    }

    public static void saveList(JoinMultiplayerScreen mps) {
        mps.getServers().save();
    }
}
