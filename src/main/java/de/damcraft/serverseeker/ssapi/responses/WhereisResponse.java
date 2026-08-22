package de.damcraft.serverseeker.ssapi.responses;

import java.util.ArrayList;
import java.util.List;

public class WhereisResponse {
    public String error;
    public String uuid;
    public String name;
    public String firstSeen;
    public String lastSeen;
    public Integer totalServers;
    public List<Record> servers;
    public Integer matches;
    public List<Player> players;

    public static class Player {
        public String uuid;
        public String name;
        public String firstSeen;
        public String lastSeen;
        public Integer totalServers;
        public List<Record> servers;
    }

    public static class Record {
        public String ip;
        public Integer port;
        public String firstSeen;
        public String lastSeen;
    }

    public static class ServerRecord {
        public String ip;
        public Integer port;
        public String name;
        public String lastSeen;
    }

    public boolean isError() {
        return error != null;
    }

    public List<ServerRecord> records() {
        List<ServerRecord> out = new ArrayList<>();
        if (players != null && !players.isEmpty()) {
            for (Player player : players) {
                if (player.servers == null) continue;
                for (Record r : player.servers) {
                    ServerRecord sr = new ServerRecord();
                    sr.ip = r.ip;
                    sr.port = r.port;
                    sr.lastSeen = r.lastSeen;
                    sr.name = player.name;
                    out.add(sr);
                }
            }
        } else if (servers != null) {
            for (Record r : servers) {
                ServerRecord sr = new ServerRecord();
                sr.ip = r.ip;
                sr.port = r.port;
                sr.lastSeen = r.lastSeen;
                sr.name = name;
                out.add(sr);
            }
        }
        return out;
    }
}
