package me.iangry.lobbysystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {
    private final LobbySystem plugin;

    public VoteCommand(LobbySystem plugin) {
        this.plugin = plugin;
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
                return new Voting(plugin).handleAddMapCommand(player, args);
            }

            if (plugin.isVotingOpen()) {
                return new Voting(plugin).handleVoteCommand(player, args);
            }
        }

        return false;
    }
}
