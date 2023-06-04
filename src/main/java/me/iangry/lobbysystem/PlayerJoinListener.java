package me.iangry.lobbysystem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
        GameState gameState = plugin.getGameState();

        if (votingWorldName == null) return;
        World votingWorld = Bukkit.getWorld(votingWorldName);
        if (votingWorld == null) return;

        if(gameState == GameState.VOTING_FINISHED) {
            // The winning map
            String winningMap = Voting.getWinningMap();

            // Getting the location of the winning map
            Location warpLocation = Location.deserialize(plugin.getConfig().getConfigurationSection("maps." + winningMap.toLowerCase()).getValues(true));

            // Teleport the player to the winning map
            event.getPlayer().teleport(warpLocation);
        } else {
            // Teleport to spawn as voting has started or not yet started
            event.getPlayer().teleport(votingWorld.getSpawnLocation());
        }

        long playersInVotingWorldCount = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(votingWorld)).count();

        if (playersInVotingWorldCount >= plugin.getMinimumPlayers() && !plugin.isVotingOpen()) {
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

        if (playersInVotingWorldCount < plugin.getMinimumPlayers() && plugin.isVotingOpen()) {
            Voting.stopVoting();
        }
    }
}
