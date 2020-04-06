package me.rvt.rcminigames;

import com.bekvon.bukkit.residence.Residence;
import me.rvt.rcminigames.config.ConfigInit;
import me.rvt.rcminigames.games.Maze;
import me.rvt.rcminigames.games.WarZone;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class RCminiGames extends JavaPlugin implements Listener {

    private static Residence res;
    private FileConfiguration config;
    private Maze maze;
    private List < Object > fighters = new ArrayList < > ();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("maze").setExecutor(this);
        this.getCommand("minigames").setExecutor(this);
        initConfig();

        res = (Residence) Bukkit.getServer().getPluginManager().getPlugin("Residence");
        maze = new Maze(config);
    }

    private void initConfig() {
        config = new ConfigInit(this).getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length > 0) {
                String arg = args[0].toLowerCase();

                switch (cmd.getName()) {
                    case "maze":
                        if (arg.equals("exit")) {
                            maze.onPlayerExit(p);
                        }
                        break;
                    case "minigames":
                        if (arg.equals("reload") && p.isOp()) {
                                initConfig();
                                p.sendMessage(config.getString("message.prefix") +
                                        config.getString("message.reloaded"));
                        }
                }
            } else {
                p.sendMessage(config.getString("message.prefix") +
                        config.getString("message.commands"));
            }
        }
        return true;
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        maze.interactEvent(e);
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent e) {
        maze.blocKCommands(e);

        if (isInRes(e.getPlayer())) {
            for (Object o: fighters) {
                if (((WarZone) o).getPlayer().equals(e.getPlayer().getName()) &&
                        ((WarZone) o).getTagged()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(config.getString("message.prefix") +
                            config.getString("message.inFight"));
                    return;
                }
            }
        }
    }

    @EventHandler
    private void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
        ||  e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Player p = e.getPlayer();

            getServer().getScheduler().scheduleSyncDelayedTask(JavaPlugin.getProvidingPlugin(RCminiGames.class), () ->
                    onFighterJoin(p), 1);
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        for (Object o: fighters) {
            if (((WarZone) o).getPlayer().equals(p.getName())) {
                ((WarZone) o).removeScoreboard();
                fighters.remove(o);
                return;
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        maze.onPlayerExit(p);

        for (Object o: fighters) {
            if (((WarZone) o).getPlayer().equals(p.getName()) && ((WarZone) o).getTagged()) {
                fighters.remove(o);
                p.setHealth(0);
                return;
            }
        }
    }

    @EventHandler
    private void onPlayerDamagePlayer(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && isInRes((Player) e.getEntity())) {
            Player victim = (Player) e.getEntity();

            if (e.getDamager() instanceof Player && isInRes((Player) e.getDamager())) {
                Player attacker = (Player) e.getDamager();
                for (Object o: fighters) {
                    if (((WarZone) o).getPlayer().equals(attacker.getName())) {
                        if (((WarZone) o).getProtected()) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                for (Object o: fighters) {
                    if (((WarZone) o).getPlayer().equals(victim.getName())) {
                        ((WarZone) o).setAttacker(attacker.getName());
                        return;
                    }
                }
            }
        }
    }

    private boolean isInRes(Player p) {
        if (res.getResidenceManager().getByLoc(p) != null)
            return res.getResidenceManager().getByLoc(p).getResidenceName().equals(config.getString("warzone.res"));
        else return false;
    }

    private void onFighterJoin(Player p) {
        if (isInRes(p)) {
            fighters.add(new WarZone(p, config, this));
            if(config.getBoolean("warzone.penalty") && !p.isOp()){
                p.sendMessage(config.getString("message.prefix") + config.getString("message.warn"));
            }
        } else {
            if (!fighters.isEmpty()) {
                for (Object o: fighters) {
                    if (((WarZone) o).getPlayer().equals(p.getName())) {
                        ((WarZone) o).setProtection(false);
                        ((WarZone) o).removeScoreboard();
                        fighters.remove(o);
                        return;
                    }
                }
            }
        }
    }
}