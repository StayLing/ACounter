package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {

    public static void openSettingsMenu(Player player) {
        FileConfiguration settingsConfig = Acounter.getInstance().getConfigManager().getSettingsConfig();
        String menuTitle = ChatColor.translateAlternateColorCodes('&', settingsConfig.getString("Menu.Gui-Name", "Menu title is not available"));
        int guiRows = settingsConfig.getInt("Menu.Gui-Rows", 1);
        int menuSize = guiRows * 9;
        Inventory menu = Bukkit.createInventory(null, menuSize, menuTitle);

        if (settingsConfig.getBoolean("Menu.Fill-Item.Status", false)) {
            String fillItemName = ChatColor.translateAlternateColorCodes('&', settingsConfig.getString("Menu.Fill-Item.Name", "&r"));
            Material fillMaterial = Material.valueOf(settingsConfig.getString("Menu.Fill-Item.Material", "GRAY_STAINED_GLASS_PANE").toUpperCase());
            ItemStack fillItem = new ItemStack(fillMaterial);
            ItemMeta fillMeta = fillItem.getItemMeta();
            if (fillMeta != null) {
                fillMeta.setDisplayName(fillItemName);
                fillItem.setItemMeta(fillMeta);
            }
            for (int i = 0; i < menuSize; i++) {
                menu.setItem(i, fillItem);
            }
        }

        loadMenuItem(player, menu, "Combo-Counter");
        loadMenuItem(player, menu, "Combo-Sound");

        player.openInventory(menu);
    }

    private static void loadMenuItem(Player player, Inventory menu, String itemKey) {
        FileConfiguration settingsConfig = Acounter.getInstance().getConfigManager().getSettingsConfig();
        String itemName = ChatColor.translateAlternateColorCodes('&', settingsConfig.getString("Menu." + itemKey + ".Name", "&cUnnamed Item"));
        Material material = Material.valueOf(settingsConfig.getString("Menu." + itemKey + ".Material", "BARRIER").toUpperCase());
        int slot = settingsConfig.getInt("Menu." + itemKey + ".Slot", -1);
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(itemName);

            List<String> lore = settingsConfig.getStringList("Menu." + itemKey + ".Lore");
            if (!lore.isEmpty()) {
                File playerDataFile = new File(Acounter.getInstance().getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
                FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

                boolean comboCounter = playerDataConfig.getBoolean("Settings.Combo-Counter", true);
                boolean comboSound = playerDataConfig.getBoolean("Settings.Combo-Sound", true);

                List<String> updatedLore = lore.stream()
                        .map(line -> line
                                .replace("{C-Status}", getMessage(comboCounter ? "true_message" : "false_message"))
                                .replace("{CS-Status}", getMessage(comboSound ? "true_message" : "false_message")))
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());

                meta.setLore(updatedLore);
            }

            item.setItemMeta(meta);
        }

        if (slot >= 0 && slot < menu.getSize()) {
            menu.setItem(slot, item);
        } else {
            menu.addItem(item);
        }
    }

    public static String getMessage(String path) {
        String message = Acounter.getInstance().getConfigManager().getLanguageConfig().getString(path, "").trim(); // Trim to remove extra spaces
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
