package ashnodes.co.acounter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class SettingsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("wmc")) {
            if (args.length == 0) {
                List<String> commandList = Acounter.getInstance().getConfigManager().getLanguageConfig().getStringList("command_list");
                for (String message : commandList) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
                return false;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                long startTime = System.currentTimeMillis();
                Acounter.getInstance().getConfigManager().reloadConfigs();
                long reloadTime = System.currentTimeMillis() - startTime;

                String reloadMessage = Acounter.getInstance().getConfigManager().getLanguageConfig().getString("reload_message", "&aPlugin reloaded in &f{ms}ms");
                reloadMessage = reloadMessage.replace("{ms}", String.valueOf(reloadTime));

                String prefix = MenuManager.getMessage("prefix");
                String formattedMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + reloadMessage) + "\n ";
                sender.sendMessage(formattedMessage);
                return true;
            }

            if (args[0].equalsIgnoreCase("menu")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Acounter.getInstance().getConfigManager().reloadConfigs();

                    String menuStatus = Acounter.getInstance().getConfigManager().getSettingsConfig().getString("Menu.Status", "disabled");
                    if ("enabled".equalsIgnoreCase(menuStatus)) {
                        MenuManager.openSettingsMenu(player);
                    } else {
                        player.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + MenuManager.getMessage("menu_disabled") + "\n ");
                    }
                    return true;
                } else {
                    sender.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + MenuManager.getMessage("player_only") + "\n ");
                    return false;
                }
            }

            if (args[0].equalsIgnoreCase("user") && args.length == 4) {
                if (sender.isOp()) {
                    String targetPlayerName = args[1];
                    String settingType = args[2];
                    String newValue = args[3];

                    // Validate settingType and newValue
                    if (!"Combo-Counter".equalsIgnoreCase(settingType) && !"Combo-Sound".equalsIgnoreCase(settingType)) {
                        sender.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + ChatColor.translateAlternateColorCodes('&', "&cUsage: /wmc user <player> <Combo-Counter/Combo-Sound> <true/false>") + "\n ");
                        return false;
                    }

                    if (!"true".equalsIgnoreCase(newValue) && !"false".equalsIgnoreCase(newValue)) {
                        sender.sendMessage("\n" + MenuManager.getMessage("prefix") + " " + ChatColor.translateAlternateColorCodes('&', "&cUsage: /wmc user <player> <Combo-Counter/Combo-Sound> <true/false>") + "\n ");
                        return false;
                    }

                    // Fetch and update the player's data
                    FileConfiguration playerData = Acounter.getInstance().getPlayerData(targetPlayerName);
                    if (playerData == null) {
                        // Retrieve and format the player data not found message from language.yml
                        String playerDataNotFoundMessage = Acounter.getInstance().getConfigManager().getLanguageConfig().getString("data_not_found", "&f{player}'s &cdata not found!")
                                .replace("{player}", targetPlayerName);

                        String prefix = MenuManager.getMessage("prefix");
                        String formattedMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + playerDataNotFoundMessage) + "\n ";
                        sender.sendMessage(formattedMessage);
                        return false;
                    }

                    if ("Combo-Counter".equalsIgnoreCase(settingType)) {
                        playerData.set("Settings.Combo-Counter", Boolean.parseBoolean(newValue));
                    } else if ("Combo-Sound".equalsIgnoreCase(settingType)) {
                        playerData.set("Settings.Combo-Sound", Boolean.parseBoolean(newValue));
                    }

                    Acounter.getInstance().savePlayerData(targetPlayerName, playerData);

                    String updateSettingsMessage = Acounter.getInstance().getConfigManager().getLanguageConfig().getString("update_settings", "&f{settings} &7setting for &f{player} &7has been updated to &f{value}!");
                    updateSettingsMessage = updateSettingsMessage
                            .replace("{settings}", settingType)
                            .replace("{player}", targetPlayerName)
                            .replace("{value}", newValue);

                    String prefix = MenuManager.getMessage("prefix");
                    String formattedMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + updateSettingsMessage) + "\n ";
                    sender.sendMessage(formattedMessage);
                    return true;
                } else {

                    String noPermissionMessage = Acounter.getInstance().getConfigManager().getLanguageConfig().getString("no_permission", "&cYou do not have permission to use this command.");
                    String prefix = MenuManager.getMessage("prefix");
                    String formattedMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + noPermissionMessage) + "\n ";
                    sender.sendMessage(formattedMessage);
                    return false;
                }
            }

            if (args[0].equalsIgnoreCase("discord")) {
                String discordMessage = "&fClick to join our Discord server! " + "&f ▪ &ehttps://discord.gg/xyRMtVURw4&r";

                String prefix = MenuManager.getMessage("prefix");
                String formattedMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + discordMessage) + "\n ";
                sender.sendMessage(formattedMessage);
                return true;
            }


            String argumentNotFoundMessage = Acounter.getInstance().getConfigManager().getLanguageConfig().getString("argument_not_found", "&cThe argument &f{arg} &cis not found in the plugin!")
                    .replace("{arg}", args[0]);

            String prefix = MenuManager.getMessage("prefix");
            String formattedArgumentNotFoundMessage = "\n" + ChatColor.translateAlternateColorCodes('&', prefix + " " + argumentNotFoundMessage) + "\n ";
            sender.sendMessage(formattedArgumentNotFoundMessage);
            return false;
        }
        return false;
    }
}
