package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File playerDataFile = new File(Acounter.getInstance().getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        if (!playerDataFile.exists()) {
            createPlayerDataFile(player, playerDataFile);
        }
    }

    private void createPlayerDataFile(Player player, File playerDataFile) {
        playerDataFile.getParentFile().mkdirs();
        FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        // Write data
        playerDataConfig.set("Information.Player-Name", player.getName());
        playerDataConfig.set("Information.First-Entry-Date", getCurrentDateTime());

        playerDataConfig.set("Settings.Combo-Counter", true);
        playerDataConfig.set("Settings.Combo-Sound", true);

        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(ChatColor.RED + "Player data file for " + player.getName() + " could not be saved!");
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date());
    }
}