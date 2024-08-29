package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker implements Listener {

    private final Plugin plugin;
    private final String currentVersion;
    private final String resourceUrl = "https://api.spigotmc.org/legacy/update.php?resource=118886";
    private final String updateLink = "https://www.spigotmc.org/resources/acounter.118886/";
    private String latestVersion;
    private boolean isVersionCheckerEnabled;

    public VersionChecker(Plugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        FileConfiguration settingsConfig = ((Acounter) plugin).getConfigManager().getSettingsConfig();
        isVersionCheckerEnabled = settingsConfig.getBoolean("Version-Checker", true);

        if (isVersionCheckerEnabled) {
            checkForUpdates();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(resourceUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                latestVersion = in.readLine();
                in.close();

                if (latestVersion != null && !latestVersion.equalsIgnoreCase(currentVersion)) {
                    plugin.getLogger().warning("The new version of the ACounter plugin you use on your server has been released! (" + latestVersion + ")" + "\n");
                    plugin.getLogger().warning("The version you are currently using: (" + currentVersion + ")" + "\n" + "New version: (" + latestVersion + ")" + "\n");
                    plugin.getLogger().warning(updateLink + " You can update from the link!" + "\n");
                } else {
                    plugin.getLogger().info("You are using the latest version of the ACounter(" + currentVersion + ") plugin!");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking version, you may be running your server in offline mode! " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isVersionCheckerEnabled) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isOp() && latestVersion != null && !latestVersion.equalsIgnoreCase(currentVersion)) {
            FileConfiguration langConfig = ((Acounter) plugin).getConfigManager().getLanguageConfig();
            String prefix = ChatColor.translateAlternateColorCodes('&', langConfig.getString("prefix", "&fACounter §x&c&6&d&e&f&1›> "));

            String prefixWithSpace = prefix + " ";

            player.sendMessage("\n" + prefixWithSpace + "The new version of the ACounter plugin you use on your server has been released!" + "\n§r");
            player.sendMessage(" §f• §7The version you are currently using: §c(" + currentVersion + ")" + "\n" + "\n" + " §f• §7New version: §a(" + latestVersion + ")" + "\n" + "\n§r");
            player.sendMessage("§e" + updateLink +"" + "\n§r");
        }
    }
}
