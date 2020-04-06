package me.rvt.rcminigames.games;

import me.rvt.rcminigames.RCminiGames;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class Maze {
    FileConfiguration config;
    List< String > playerData = new ArrayList< >();

    public Maze(FileConfiguration config)
    {
        this.config = config;
    }

    public void interactEvent(PlayerInteractEvent e)
    {
        if (e.getClickedBlock() != null) {
            Player p = e.getPlayer();
            Block block = e.getClickedBlock();
            if (block.getLocation().equals(config.getLocation("maze.small.enter"))) {
                onPlayerEnter(p);
                return;
            }

            if (block.getLocation().equals(config.getLocation("maze.big.exit"))) {
                p.sendMessage(config.getString("message.prefix") + config.getString("message.finished"));
                onPlayerFinish(p);
            }
        }
    }

    public void onPlayerExit(Player p) {
        if (playerData.contains(p.getDisplayName())) {
            teleportPlayer(p, config.getLocation("maze.small.exit"));
            playerData.remove(p.getDisplayName());
        }
    }

    private void onPlayerFinish(Player p){
        if (playerData.contains(p.getDisplayName())) {
            teleportPlayer(p, config.getLocation("maze.small.finish"));
            playerData.remove(p.getDisplayName());
        }
    }

    private void onPlayerEnter(Player p) {
        teleportPlayer(p, config.getLocation("maze.big.enter"));
        playerData.add(p.getDisplayName());
    }

    private void teleportPlayer(Player p, Location loc){
        p.setGameMode(GameMode.CREATIVE);
        p.teleport(loc);
        getServer().getScheduler().scheduleSyncDelayedTask(JavaPlugin.getProvidingPlugin(RCminiGames.class), () ->
                p.setGameMode(GameMode.SURVIVAL), config.getInt("gmSwitchDelay"));
    }

    public void blocKCommands(PlayerCommandPreprocessEvent e)
    {
        Player p = e.getPlayer();

        if (playerData.contains(p.getDisplayName())) {
            if (!e.getMessage().toLowerCase().equals("/maze exit")) {
                e.setCancelled(true);
                p.sendMessage(config.getString("message.prefix") + config.getString("message.disabled"));
            }
        }
    }
}
