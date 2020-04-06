package me.rvt.rcminigames.games;

import com.coloredcarrot.api.sidebar.Sidebar;
import com.coloredcarrot.api.sidebar.SidebarString;
import me.rvt.rcminigames.RCminiGames;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WarZone {
    FileConfiguration config;
    Plugin plugin;

    Player p;
    String taggedBy;
    boolean isTagged = false,
            isProtected = false,
            update = true;
    int kills, taggedSeconds = 0;

    Sidebar newSidebar;

    public WarZone(Player p, FileConfiguration config, Plugin plugin) {
        this.p = p;
        this.config = config;
        this.plugin = plugin;

        kills = p.getStatistic(Statistic.PLAYER_KILLS);

        newSidebar = new Sidebar(config.getString("sidebar.title"),
                plugin, 20, new SidebarString(" "), new SidebarString(
                        String.format(config.getString("sidebar.line1"), 0)));

        playerTeleported();
    }

    public boolean getTagged() {
        return isTagged;
    }

    public boolean getProtected(){
        return  isProtected;
    }

    public void setProtection(boolean isProtected){ this.isProtected = isProtected; }

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
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, time*20, 1));
        protectionCountDown(time);
    }

    private void protectionCountDown(int time) {
        if(isProtected){
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
                updateScoreboard();
                }
        }
        else{
            readyForFight();
        }
    }

    private void readyForFight(){
        isProtected = false;
        p.setInvulnerable(false);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    private void taggedCountDown() {
        if (isTagged) {
            if (taggedSeconds > 0) {
                taggedSeconds--;
            } else {
                isTagged = false;
            }
        }
    }

    private void updateScoreboard() {
        List<SidebarString> lines = new ArrayList<>();

        lines.add(new SidebarString(" "));
        lines.add(new SidebarString(String.format(config.getString("sidebar.line1"),
                p.getStatistic(Statistic.PLAYER_KILLS) - kills)));
        lines.add(new SidebarString(" "));

        if (isTagged) {
            lines.add(new SidebarString(String.format(config.getString("sidebar.line2"), taggedSeconds)));
            lines.add(new SidebarString(String.format(config.getString("sidebar.line3"), taggedBy)));
        }
        newSidebar.setEntries(lines);

        taggedCountDown();

        if(update)
            getServer().getScheduler().scheduleSyncDelayedTask(JavaPlugin.getProvidingPlugin(RCminiGames.class),
                this::updateScoreboard, config.getInt("warzone.scoreBoardUpdate") * 20);
    }

    public void removeScoreboard() {
        newSidebar.hideFrom(p);
        update = false;
    }
}