package me.iangry.lobbysystem;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
    private final LobbySystem plugin;

    public PlayerJoinListener(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String votingWorldName = plugin.getConfig().getString("votingWorld");
        if (votingWorldName == null) return;
        World votingWorld = Bukkit.getWorld(votingWorldName);
        if (votingWorld == null) return;

        long playersInVotingWorldCount = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(votingWorld)).count();

        if (playersInVotingWorldCount >= LobbySystem.MINIMUM_PLAYERS && !plugin.isVotingOpen()) {
            Voting.startVoting();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String votingWorldName = plugin.getConfig().getString("votingWorld");
        if (votingWorldName == null) return;
        World votingWorld = Bukkit.getWorld(votingWorldName);
        if (votingWorld == null) return;

        long playersInVotingWorldCount = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(votingWorld)).count() - 1;

        if (playersInVotingWorldCount < LobbySystem.MINIMUM_PLAYERS && plugin.isVotingOpen()) {
            Voting.stopVoting();
        }
    }
}
