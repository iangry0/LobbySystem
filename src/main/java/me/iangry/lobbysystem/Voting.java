package me.iangry.lobbysystem;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;



//

//ISSUE: ONLY WORKS WITH 1 PLAYER FOR SOME REASON, IF 2 JOIN THEN ONLY ONE SEES VOTING


//

public class Voting {
    private static final int VOTING_TIME_IN_SECONDS = 120;
    private static LobbySystem plugin;
    private static BukkitRunnable votingTimer;

    public Voting(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public static String getWinningMap() {
        int maxVotes = 0;
        List<String> topVotedMaps = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : plugin.getVotes().entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                topVotedMaps.clear();
                topVotedMaps.add(entry.getKey());
            } else if (entry.getValue() == maxVotes) {
                topVotedMaps.add(entry.getKey());
            }
        }

        if (maxVotes == 0) {
            // In case no votes were cast, select a random map
            Random rand = new Random();
            List<String> keys = new ArrayList<>(plugin.getVotes().keySet());
            return keys.get(rand.nextInt(keys.size()));
        }

        // In case of a tie, pick a random map from the top voted ones
        if (topVotedMaps.size() > 1) {
            Random rand = new Random();
            return topVotedMaps.get(rand.nextInt(topVotedMaps.size()));
        }

        // Return the only top voted map
        return topVotedMaps.get(0);
    }

    public static void startVoting() {
        String votingWorldName = plugin.getConfig().getString("votingWorld");

        if (votingWorldName == null || Bukkit.getWorld(votingWorldName) == null) {
            String votingworldnotfound = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("voting-world-not-found"));
            Bukkit.broadcastMessage(votingworldnotfound);
            return;
        }

        World votingWorld = Bukkit.getWorld(votingWorldName);
        long playerCountInVotingWorld = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(votingWorld)).count();

        if (playerCountInVotingWorld < plugin.getMinimumPlayers()) {
            String notenough = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("not-enough-players"));
            Bukkit.broadcastMessage(notenough);
            return;
        }

        plugin.setGameState(GameState.VOTE_STARTED);
        plugin.setVotingOpen(true);
        plugin.getVotes().clear();
        plugin.getVoters().clear();

        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        plugin.setScoreboard(scoreboard);

        String scoreboarddisplayname = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard-displayname"));
        Objective objective = scoreboard.registerNewObjective("votes", "dummy", scoreboarddisplayname);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
        if (mapsSection == null) {
            String nomaps = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("no-maps-found"));
            Bukkit.broadcastMessage(nomaps);
            return;
        }

        for (String key : mapsSection.getKeys(false)) {
            plugin.getVotes().put(key.toLowerCase(), 0);
            objective.getScore(key.toLowerCase()).setScore(0);
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getWorld().equals(votingWorld)) {
                onlinePlayer.setScoreboard(scoreboard);
                plugin.getBossBar().addPlayer(onlinePlayer);
            }
        }

        startVotingTimer();

        String votingstarted = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("voting-started").replace("%votingworld%", votingWorldName));
        Bukkit.broadcastMessage(votingstarted);
    }


    public static void stopVoting() {
        plugin.setVotingOpen(false);
        plugin.setGameState(GameState.VOTING_NOT_STARTED);
        votingTimer.cancel();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(plugin.getScoreboardManager().getNewScoreboard());
            plugin.getBossBar().removePlayer(onlinePlayer);
        }
    }


    public boolean handleVoteCommand(Player player, String[] args) {
        String mapNameInput = args[0];

        Optional<String> optMapName = plugin.getConfig().getConfigurationSection("maps").getKeys(false)
                .stream().filter(key -> key.equalsIgnoreCase(mapNameInput)).findFirst();

        if (!optMapName.isPresent()) {
            String invalidmap = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("invalid-map"));
            player.sendMessage(invalidmap);
            return true;
        }

        String mapName = optMapName.get();

        if (plugin.getVoters().contains(player.getUniqueId())) {
            String alreadyvoted = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("already-voted"));
            player.sendMessage(alreadyvoted);
            return true;
        }

        // Initialize vote count if necessary
        plugin.getVotes().putIfAbsent(mapName, 0);
        plugin.getVotes().put(mapName, plugin.getVotes().get(mapName) + 1);
        plugin.getVoters().add(player.getUniqueId());
        plugin.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(mapName).setScore(plugin.getVotes().get(mapName));

        String playervoted = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-voted-announcement")
                .replace("%player%", player.getName())
                .replace("%mapname%", mapName)
                .replace("%totalvotes%", String.valueOf(plugin.getVotes().get(mapName))));

        Bukkit.broadcastMessage(playervoted);

        return true;
    }


    public boolean handleAddMapCommand(Player player, String[] args) {
        if (args.length < 2) {
            String specifyname = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("specify-map"));
            player.sendMessage(specifyname);
            return true;
        }

        String mapName = args[1];

        ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
        if (mapsSection == null) {
            mapsSection = plugin.getConfig().createSection("maps");
        }

        if (mapsSection.getKeys(false).stream().anyMatch(key -> key.equalsIgnoreCase(mapName))) {
            String mapExists = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("map-exists"));
            player.sendMessage(mapExists);
            return true;
        }

        // Add the new map to the voting data
        plugin.getVotes().put(mapName.toLowerCase(), 0);
        plugin.getVoters().clear();
        plugin.getVoters().forEach(plugin.getVoters()::remove);

        // Update the scoreboard to include the new map
        Scoreboard scoreboard = plugin.getScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        objective.getScore(mapName).setScore(0);

        plugin.getConfig().set("maps." + mapName, player.getLocation().serialize());
        plugin.saveConfig();

        String addmap = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("add-map").replace("%map%", mapName));
        player.sendMessage(addmap);

        return true;
    }

    private static void startVotingTimer() {
        votingTimer = new BukkitRunnable() {
            int secondsRemaining = VOTING_TIME_IN_SECONDS;

            @Override
            public void run() {
                if (secondsRemaining == 30) {
                    String votingends30 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("voting-ends-30"));
                    Bukkit.broadcastMessage(votingends30);
                }

                if (secondsRemaining <= 10) {
                    String votingends = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("voting-ends-remaining")
                            .replace("%remaining%", String.valueOf(secondsRemaining)));
                    Bukkit.broadcastMessage(votingends);
                }


                String bossbar = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bossbar")
                        .replace("%remaining%", String.valueOf(secondsRemaining)));
                plugin.getBossBar().setProgress((double) secondsRemaining / VOTING_TIME_IN_SECONDS);
                plugin.getBossBar().setTitle(bossbar);

                if (secondsRemaining == 0) {
                    finishVoting();
                    this.cancel();
                }

                secondsRemaining--;
            }
        };

        votingTimer.runTaskTimer(plugin, 0, 20);
    }

    private static void finishVoting() {
        plugin.setVotingOpen(false);
        plugin.setGameState(GameState.VOTING_FINISHED);
        plugin.getBossBar().removeAll();

        String winningMap = getWinningMap();

        String winningmap = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("winning-map")
                .replace("%winningmap%", winningMap));
        Bukkit.broadcastMessage(winningmap);

        ConfigurationSection mapSection = plugin.getConfig().getConfigurationSection("maps." + winningMap.toLowerCase());
        if (mapSection != null) {
            Location warpLocation = Location.deserialize(mapSection.getValues(true));
            Bukkit.getOnlinePlayers().forEach(player -> player.teleport(warpLocation));
        } else {
            // Handle the case where the configuration section does not exist
            // For example, you can send a message to the console or broadcast an error message
            Bukkit.getLogger().warning("Configuration section for the winning map does not exist!");
        }

        // Reset the scoreboard for each player
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(plugin.getScoreboardManager().getNewScoreboard());
        }
    }
}