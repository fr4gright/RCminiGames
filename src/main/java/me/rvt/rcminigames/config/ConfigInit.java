package me.rvt.rcminigames.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConfigInit {
    private FileConfiguration config;

    public ConfigInit(Plugin plugin)
    {
        loadConfig(plugin);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void loadConfig(Plugin plugin) {
        File conf = new File(plugin.getDataFolder(), "config.yml");

        config = YamlConfiguration.loadConfiguration(conf);

        if (!config.contains("maze")) {

            init();

            try {
                config.save(conf);
            } catch (IOException var3) {
                System.out.println("[RCminiGames] Unable to save config!");
            }
        }
    }

    private void init() {
        Location smallMazeEnter = new Location(Bukkit.getWorld("world"),22, 68, 32);
        Location smallMazeFinish = new Location(Bukkit.getWorld("world"),22.549, 68, 30.121);
        Location smallMazeExit = new Location(Bukkit.getWorld("world"),22, 68, 52);
        Location bigMazeEnter = new Location(Bukkit.getWorld("pvp"),-34.900, 4, 7.625);
        Location bigMazeExit = new Location(Bukkit.getWorld("pvp"),-49, 4, -31);

        config.set("maze.small.enter", smallMazeEnter);
        config.set("maze.small.finish", smallMazeFinish);
        config.set("maze.small.exit", smallMazeExit);
        config.set("maze.big.enter", bigMazeEnter);
        config.set("maze.big.exit", bigMazeExit);
        config.set("maze.gmSwitchDelay", 1);
        config.set("warzone.res", "WarZone");
        config.set("warzone.protection", 5);
        config.set("warzone.tagDuration", 15);
        config.set("warzone.penalty", true);
        config.set("warzone.scoreBoardUpdate", 1);

        config.set("message.prefix", ChatColor.WHITE + "" + ChatColor.BOLD + "["
                + ChatColor.AQUA + ChatColor.BOLD + "RC" +  ChatColor.WHITE + ChatColor.BOLD + "]["
                + ChatColor.GREEN + ChatColor.BOLD + "MiniGames" +  ChatColor.WHITE + ChatColor.BOLD + "]"
                + ChatColor.RESET + " ");

        config.set("message.disabled", ChatColor.RED + "Commands are disabled!");
        config.set("message.commands", ChatColor.RED + "Available commands: " + ChatColor.GREEN + "/maze exit");
        config.set("message.finished", ChatColor.GREEN + "Congratulations! You just completed the Maze! " +
                "Now, take your reward and use any command to exit.");
        config.set("message.inFight", ChatColor.RED + "NOPE! You are still in a fight!");
        config.set("message.warn", ChatColor.BOLD + "Leaving the game while tagged? That's an instant "+ ChatColor.RED +
                ChatColor.BOLD + "DEATH" + ChatColor.RESET + ChatColor.BOLD + "!");
        config.set("message.reloaded", ChatColor.GREEN + "Config reloaded!");

        config.set("message.title", ChatColor.RED + "" + ChatColor.BOLD + "WarZone");
        config.set("message.subtitle", "Protection: " + ChatColor.RED + "" + ChatColor.BOLD);
        config.set("message.fight", ChatColor.RED + "FIGHT!");

        config.set("sidebar.title", ChatColor.RED + "" + ChatColor.BOLD + "WarZone");
        config.set("sidebar.line1", ChatColor.BOLD + "KILLS: " + ChatColor.GREEN + ChatColor.BOLD + "%d");
        config.set("sidebar.line2", ChatColor.BOLD + "TAGGED: " + ChatColor.GREEN + "" + ChatColor.BOLD + "%d");
        config.set("sidebar.line3", ChatColor.RED + "" + ChatColor.BOLD + "%s");
    }
}