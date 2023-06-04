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

    private int minimumPlayers;

    private Map<String, Integer> votes = new HashMap<>();
    private Set<UUID> voters = new HashSet<>();
    private boolean votingOpen = false;
    private Scoreboard scoreboard;

    private BossBar bossBar;
    private ScoreboardManager scoreboardManager;
    private Voting voting;

    private GameState gameState = GameState.VOTING_NOT_STARTED;

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }


    @Override
    public void onEnable() {
        this.getCommand("vote").setExecutor(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        saveDefaultConfig();
        minimumPlayers = getConfig().getInt("minimum-players", 2);
        bossBar = Bukkit.createBossBar("Voting Time Remaining", BarColor.BLUE, BarStyle.SOLID);
        scoreboardManager = Bukkit.getScoreboardManager();
        voting = new Voting(this);
    }

    public int getMinimumPlayers() {
        return minimumPlayers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String playeronly = ChatColor.translateAlternateColorCodes('&', getConfig().getString("player-only"));
            sender.sendMessage(playeronly);
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("vote")) {
            if (args.length == 0) {
                String specifymap = ChatColor.translateAlternateColorCodes('&', getConfig().getString("specify-map-name"));

                player.sendMessage(specifymap);
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
