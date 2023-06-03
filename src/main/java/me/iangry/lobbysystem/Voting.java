package me.iangry.lobbysystem;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

public class Voting {
    private static final int VOTING_TIME_IN_SECONDS = 120;
    private static LobbySystem plugin;
    private static BukkitRunnable votingTimer;

    public Voting(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public static void startVoting() {
        String votingWorldName = plugin.getConfig().getString("votingWorld");

        if (votingWorldName == null || Bukkit.getWorld(votingWorldName) == null) {
            Bukkit.broadcastMessage("The voting world specified in the config is not valid. Voting cannot start.");
            return;
        }

        World votingWorld = Bukkit.getWorld(votingWorldName);
        long playerCountInVotingWorld = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(votingWorld)).count();

        if (playerCountInVotingWorld < LobbySystem.MINIMUM_PLAYERS) {
            Bukkit.broadcastMessage("Not enough players in the voting world to start voting.");
            return;
        }

        plugin.setVotingOpen(true);
        plugin.getVotes().clear();
        plugin.getVoters().clear();

        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        plugin.setScoreboard(scoreboard);

        Objective objective = scoreboard.registerNewObjective("votes", "dummy", "Map Votes");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String key : plugin.getConfig().getConfigurationSection("maps").getKeys(false)) {
            plugin.getVotes().put(key, 0);
            objective.getScore(key).setScore(0);
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getWorld().equals(votingWorld)) {
                onlinePlayer.setScoreboard(scoreboard);
                plugin.getBossBar().addPlayer(onlinePlayer);
            }
        }

        startVotingTimer();

        Bukkit.broadcastMessage("Voting has started in " + votingWorldName + "! Use /vote (map) to vote.");
    }

    public static void stopVoting() {
        plugin.setVotingOpen(false);
        votingTimer.cancel();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(plugin.getScoreboardManager().getNewScoreboard());
            plugin.getBossBar().removePlayer(onlinePlayer);
        }
    }

    public boolean handleVoteCommand(Player player, String[] args) {
        String mapName = args[0];

        if (!plugin.getConfig().isConfigurationSection("maps." + mapName)) {
            player.sendMessage("Invalid map. Please vote for a valid map.");
            return true;
        }

        if (plugin.getVoters().contains(player.getUniqueId())) {
            player.sendMessage("You have already voted.");
            return true;
        }

        plugin.getVotes().put(mapName, plugin.getVotes().get(mapName) + 1);
        plugin.getVoters().add(player.getUniqueId());
        plugin.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(mapName).setScore(plugin.getVotes().get(mapName));

        Bukkit.broadcastMessage(player.getName() + " has voted for " + mapName + "! Current votes: " + plugin.getVotes().get(mapName));

        return true;
    }

    public boolean handleAddMapCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Please specify a name for the map.");
            return true;
        }

        String mapName = args[1];
        plugin.getConfig().set("maps." + mapName, player.getLocation().serialize());
        plugin.saveConfig();

        player.sendMessage("Map '" + mapName + "' has been added at your current location.");

        return true;
    }

    private static void startVotingTimer() {
        votingTimer = new BukkitRunnable() {
            int secondsRemaining = VOTING_TIME_IN_SECONDS;

            @Override
            public void run() {
                if (secondsRemaining == 30) {
                    Bukkit.broadcastMessage("Voting ends in 30 seconds.");
                }

                if (secondsRemaining <= 10) {
                    Bukkit.broadcastMessage("Voting ends in " + secondsRemaining + " seconds...");
                }

                plugin.getBossBar().setProgress((double) secondsRemaining / VOTING_TIME_IN_SECONDS);
                plugin.getBossBar().setTitle("§3§lVoting Time Remaining: §b" + secondsRemaining + "s");

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
        plugin.getBossBar().removeAll();

        String winningMap = plugin.getVotes().entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

        if (plugin.getVotes().get(winningMap) == 0) {
            Random rand = new Random();
            List<String> keys = new ArrayList<>(plugin.getVotes().keySet());
            winningMap = keys.get(rand.nextInt(keys.size()));
        }

        Bukkit.broadcastMessage("Voting has ended! The winning map is " + winningMap + "!");
        Location warpLocation = Location.deserialize(plugin.getConfig().getConfigurationSection("maps." + winningMap).getValues(true));
        Bukkit.getOnlinePlayers().forEach(player -> player.teleport(warpLocation));

        // Reset the scoreboard for each player
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(plugin.getScoreboardManager().getNewScoreboard());
        }
    }
}
