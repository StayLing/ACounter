package ashnodes.co.acounter;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public class ACounterExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;

    public ACounterExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "wmc";
    }

    @Override
    public String getAuthor() {
        return "StayLing";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        ConfigManager configManager = Acounter.getInstance().getConfigManager();
        FileConfiguration languageConfig = configManager.getLanguageConfig();


        File playerDataFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        if (!playerDataFile.exists()) {
            return languageConfig.getString("false_message", "&cFalse");
        }

        FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        if (identifier.equals("ccstatus")) {
            boolean comboCounter = playerDataConfig.getBoolean("Settings.Combo-Counter", false);
            return comboCounter
                    ? languageConfig.getString("true_message", "&aTrue")
                    : languageConfig.getString("false_message", "&cFalse");
        }

        if (identifier.equals("csstatus")) {
            boolean comboSound = playerDataConfig.getBoolean("Settings.Combo-Sound", false);
            return comboSound
                    ? languageConfig.getString("true_message", "&aTrue")
                    : languageConfig.getString("false_message", "&cFalse");
        }

        return null;
    }
}
