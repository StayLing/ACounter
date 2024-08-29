package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            File playerDataFile = new File(Acounter.getInstance().getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
            FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

            String title = event.getView().getTitle();
            if (title.equals(ChatColor.translateAlternateColorCodes('&', Acounter.getInstance().getConfigManager().getSettingsConfig().getString("Menu.Gui-Name", "Menu Title")))) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getItemMeta() != null) {
                    String itemName = clickedItem.getItemMeta().getDisplayName();
                    String comboCounterName = ChatColor.translateAlternateColorCodes('&', Acounter.getInstance().getConfigManager().getSettingsConfig().getString("Menu.Combo-Counter.Name", "&cUnnamed Item"));
                    String comboSoundName = ChatColor.translateAlternateColorCodes('&', Acounter.getInstance().getConfigManager().getSettingsConfig().getString("Menu.Combo-Sound.Name", "&cUnnamed Item"));

                    if (itemName.equals(comboCounterName)) {
                        boolean currentStatus = playerDataConfig.getBoolean("Settings.Combo-Counter", true);
                        playerDataConfig.set("Settings.Combo-Counter", !currentStatus);
                        String message = (currentStatus ? "combo_counter_disabled" : "combo_counter_enabled");
                        player.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + MenuManager.getMessage(message) + "\n ");
                    } else if (itemName.equals(comboSoundName)) {
                        boolean currentStatus = playerDataConfig.getBoolean("Settings.Combo-Sound", true);
                        playerDataConfig.set("Settings.Combo-Sound", !currentStatus);
                        String message = (currentStatus ? "combo_sound_disabled" : "combo_sound_enabled");
                        player.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + MenuManager.getMessage(message) + "\n ");
                    }

                    try {
                        playerDataConfig.save(playerDataFile);
                    } catch (IOException e) {
                        Bukkit.getLogger().severe(ChatColor.RED + "Player data file for " + player.getName() + " could not be saved!");
                    }

                    MenuManager.openSettingsMenu(player);
                }
            }
        }
    }
}
