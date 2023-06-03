package me.iangry.lobbysystem;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scoreboard.*;

import java.util.*;

public class LobbySystem extends JavaPlugin implements CommandExecutor {
    public static final int MINIMUM_PLAYERS = 1;

    private Map<String, Integer> votes = new HashMap<>();
    private Set<UUID> voters = new HashSet<>();
    private boolean votingOpen = false;
    private Scoreboard scoreboard;

    private BossBar bossBar;
    private ScoreboardManager scoreboardManager;
    private Voting voting;

    @Override
    public void onEnable() {
        this.getCommand("vote").setExecutor(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        this.saveDefaultConfig();

        bossBar = Bukkit.createBossBar("Voting Time Remaining", BarColor.BLUE, BarStyle.SOLID);
        scoreboardManager = Bukkit.getScoreboardManager();
        voting = new Voting(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("vote")) {
            if (args.length == 0) {
                player.sendMessage("Please specify a map to vote for or use /vote addmap to add a map.");
                return true;
            }

            if (args[0].equalsIgnoreCase("addmap") && player.hasPermission("vote.addmap")) {
                return voting.handleAddMapCommand(player, args);
            }

            if (votingOpen) {
                return voting.handleVoteCommand(player, args);
            }
        }

        return false;
    }

    public void setVotingOpen(boolean votingOpen) {
        this.votingOpen = votingOpen;
    }

    public boolean isVotingOpen() {
        return votingOpen;
    }

    public Map<String, Integer> getVotes() {
        return votes;
    }

    public Set<UUID> getVoters() {
        return voters;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
}
