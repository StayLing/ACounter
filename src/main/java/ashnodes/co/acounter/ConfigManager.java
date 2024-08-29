package ashnodes.co.acounter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration settingsConfig;
    private FileConfiguration languageConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        // Load settings.yml
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            settingsFile.getParentFile().mkdirs();
            plugin.saveResource("settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        // Load language.yml
        File languageFile = new File(plugin.getDataFolder(), "language.yml");
        if (!languageFile.exists()) {
            languageFile.getParentFile().mkdirs();
            plugin.saveResource("language.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }

    public void reloadConfigs() {
        loadConfigs();
    }
}
