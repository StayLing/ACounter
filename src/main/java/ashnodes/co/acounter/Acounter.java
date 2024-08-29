package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Acounter extends JavaPlugin {

    private static Acounter instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);

        VersionChecker versionChecker = new VersionChecker(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ACounterExpansion(this).register();
        } else {
            getLogger().warning("PlaceholderAPI plugin not found! Some features may not work.");
        }

        getCommand("wmc").setExecutor(new SettingsCommand());

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new ComboCounterManager(this), this);
    }

    public static Acounter getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FileConfiguration getPlayerData(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return null;
        }
        UUID playerUUID = player.getUniqueId();
        File playerDataFile = new File(getDataFolder() + "/playerdata", playerUUID.toString() + ".yml");

        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void savePlayerData(String playerName, FileConfiguration playerData) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        File playerDataFile = new File(getDataFolder() + "/playerdata", playerUUID.toString() + ".yml");

        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
