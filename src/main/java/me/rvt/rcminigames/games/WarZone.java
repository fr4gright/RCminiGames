package me.rvt.rcminigames.games;

import com.coloredcarrot.api.sidebar.Sidebar;
import com.coloredcarrot.api.sidebar.SidebarString;
import me.rvt.rcminigames.RCminiGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WarZone extends Thread {
    FileConfiguration config;
    Plugin plugin;

    Player p;
    String taggedBy;
    boolean isTagged = false,
            isProtected = false,
            update = true;
    int kills, taggedSeconds = 0, playersIn = 0;

    Sidebar newSidebar;

    public WarZone(Player p, FileConfiguration config) {
        this.p = p;
        this.config = config;
        this.plugin = getServer().getPluginManager().getPlugin("RCminiGames");

        kills = p.getStatistic(Statistic.PLAYER_KILLS);

        newSidebar = new Sidebar(config.getString("sidebar.title"),
                plugin, config.getInt("warzone.scoreBoardUpdate") * 20,
                new SidebarString(config.getString("sidebar.betweenLines")),
                new SidebarString(String.format(config.getString("sidebar.line1"), 0)),
                new SidebarString(String.format(config.getString("sidebar.line2"), playersIn)),
                new SidebarString(config.getString("sidebar.betweenLines")));

        playerTeleported();
    }

    public boolean getTagged() {
        return isTagged;
    }

    public boolean getProtected() {
        return isProtected;
    }

    public void setProtection(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public void stopUpdating() {
        update = false;
    }

    public void setPlayersIn(int playersIn) {
        this.playersIn = playersIn;
    }

    public void setAttacker(String taggedBy) {
        isTagged = true;
        this.taggedBy = taggedBy;
        taggedSeconds = config.getInt("warzone.tagDuration");
    }

    public String getPlayer() {
        return p.getName();
    }

    public void playerTeleported() {
        isProtected = true;
        int time = config.getInt("warzone.protection");
        p.setInvulnerable(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, time * 20, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time * 20, 1));
        protectionCountDown(time);
    }

    private void protectionCountDown(int time) {
        if (isProtected) {
            if (time > 0) {
                p.sendTitle(config.getString("message.title"),
                        config.getString("message.subtitle") + time--, 0, 30, 0);

                final int newTime = time;

                getServer().getScheduler().scheduleSyncDelayedTask(JavaPlugin.getProvidingPlugin(RCminiGames.class), () ->
                        protectionCountDown(newTime), 20);
            } else {
                readyForFight();
                p.sendTitle(config.getString("message.fight"), "", 10, 20, 20);
                newSidebar.showTo(p);
                this.start();
            }
        } else {
            readyForFight();
        }
    }

    private void readyForFight() {
        isProtected = false;
        p.setInvulnerable(false);
    }

    private void taggedCountDown() {
        if (taggedSeconds > 0)
            taggedSeconds--;
        else
            isTagged = false;
    }

    public void run() {
        List < SidebarString > lines = new ArrayList < > ();
        String trimmed;

        while (update) {
            lines.clear();

            lines.add(new SidebarString(config.getString("sidebar.betweenLines")));
            lines.add(new SidebarString(String.format(config.getString("sidebar.line1"),
                    p.getStatistic(Statistic.PLAYER_KILLS) - kills)));
            lines.add(new SidebarString(String.format(
                    config.getString("sidebar.line2"), playersIn)));

            if (isTagged) {
                trimmed = String.format(config.getString("sidebar.line4"), taggedBy);
                trimmed = trimmed.substring(0, Math.min(trimmed.length(), 16));

                lines.add(new SidebarString(" "));
                lines.add(new SidebarString(String.format(
                        config.getString("sidebar.line3"), taggedSeconds)));
                lines.add(new SidebarString(trimmed));
                taggedCountDown();
            }

            lines.add(new SidebarString(config.getString("sidebar.betweenLines")));

            newSidebar.setEntries(lines);

            try {
                Thread.sleep(config.getInt("warzone.scoreBoardUpdate") * 1000);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        newSidebar.hideFrom(p);
    }

    public void decapitate(){
        if(Math.random() > config.getDouble("warzone.head-chance")){
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setOwningPlayer(p);
            head.setItemMeta(headMeta);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    p.getWorld().dropItemNaturally(p.getLocation(), head));
        }
    }
}