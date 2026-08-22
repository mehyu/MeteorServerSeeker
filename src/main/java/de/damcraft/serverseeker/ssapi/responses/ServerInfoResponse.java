package de.damcraft.serverseeker.ssapi.responses;

import java.util.ArrayList;
import java.util.List;

public class ServerInfoResponse {
    public String error;
    public ServerRef server;
    public List<Player> players;
    public Integer count;

    public static class ServerRef {
        public String ip;
        public Integer port;
    }

    public static class Player {
        public String uuid;
        public String name;
        public String firstSeen;
        public String lastSeen;
    }

    public boolean isError() {
        return error != null;
    }
}
