package ashnodes.co.acounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ComboCounterManager implements Listener {

    private final Acounter plugin;
    private final Map<Player, Integer> playerComboCount = new HashMap<>();

    public ComboCounterManager(Acounter plugin) {
        this.plugin = plugin;
    }

    public void handleCriticalHit(Player player) {
        if (!isComboCounterEnabled(player)) {
            return;
        }

        int comboCount = playerComboCount.getOrDefault(player, 0) + 1;
        playerComboCount.put(player, comboCount);

        FileConfiguration langConfig = plugin.getConfigManager().getLanguageConfig();
        ConfigurationSection actionBarSection = langConfig.getConfigurationSection("ActionBars.Combo-Counter");

        if (actionBarSection != null) {
            String message = null;
            String soundEffect = "BLOCK_NOTE_BLOCK_PLING";
            float pitch = 1.0f;

            if (actionBarSection.contains(String.valueOf(comboCount))) {
                message = ChatColor.translateAlternateColorCodes('&', actionBarSection.getString(comboCount + ".Message", ""));
                soundEffect = actionBarSection.getString(comboCount + ".Sound-Effect", "BLOCK_NOTE_BLOCK_PLING");
                pitch = (float) actionBarSection.getDouble(comboCount + ".Pitch", 1.0);
            }

            if (message == null && actionBarSection.contains("Final")) {
                message = ChatColor.translateAlternateColorCodes('&', actionBarSection.getString("Final.Message", ""));
                soundEffect = actionBarSection.getString("Final.Sound-Effect", "BLOCK_NOTE_BLOCK_PLING");
                pitch = (float) actionBarSection.getDouble("Final.Pitch", 1.0);
            }

            if (message != null) {
                sendActionBar(player, message);

                if (isComboSoundEnabled(player)) {
                    try {
                        player.playSound(player.getLocation(), Sound.valueOf(soundEffect), 1.0f, pitch);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid sound effect: " + soundEffect);
                    }
                }
            }
        } else {
            plugin.getLogger().warning("ActionBars.Combo-Counter section not found in language configuration.");
        }
    }

    private void sendActionBar(Player player, String message) {
        try {
            if (Bukkit.getVersion().contains("1.18")) {
                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
                Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
                Object handle = getHandleMethod.invoke(craftPlayerClass.cast(player));
                Class<?> playerClass = handle.getClass().getSuperclass();
                Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
                Class<?> chatMessageTypeClass = Class.forName("net.minecraft.network.chat.ChatMessageType");
                Class<?> chatComponentTextClass = Class.forName("net.minecraft.network.chat.ChatComponentText");

                Object chatComponentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
                Object chatMessageType = chatMessageTypeClass.getEnumConstants()[2]; // Action bar type

                Object packet = packetPlayOutChatClass.getConstructor(chatComponentTextClass, chatMessageTypeClass).newInstance(chatComponentText, chatMessageType);
                Method sendPacketMethod = playerClass.getMethod("b", packetPlayOutChatClass); // Method to send packets
                sendPacketMethod.invoke(handle, packet);
            } else {
                player.sendActionBar(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    private void updateTitleAndSubtitle(Player player, Entity entity) {
        if (!isComboCounterEnabled(player)) {
            return;
        }

        FileConfiguration langConfig = plugin.getConfigManager().getLanguageConfig();
        if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
            LivingEntity livingEntity = (LivingEntity) entity;
            double health = livingEntity.getHealth();
            int heartsLeft = (int) Math.ceil(health / 2.0);

            String subtitleTemplate = langConfig.getString("heart", "");

            if (!subtitleTemplate.isEmpty()) {
                String titleMessage = "";
                String subtitleMessage = ChatColor.translateAlternateColorCodes('&', subtitleTemplate.replace("{heart}", String.valueOf(heartsLeft)));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendTitle(titleMessage, subtitleMessage, 10, 40, 10);
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity entity = event.getEntity();

            if (entity instanceof Player || entity instanceof ArmorStand) {
                boolean isCriticalHit = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getDamage() > 1.0;

                if (isCriticalHit) {
                    handleCriticalHit(player);
                    updateTitleAndSubtitle(player, entity);
                } else {
                    resetComboSeries(player);   
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player killedPlayer = (Player) entity;
            Player killer = killedPlayer.getKiller();
            if (killer != null) {
                resetComboSeries(killer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        resetComboSeries(player);
    }

    private void resetComboSeries(Player player) {
        if (playerComboCount.containsKey(player)) {
            playerComboCount.remove(player);

            FileConfiguration langConfig = plugin.getConfigManager().getLanguageConfig();
            String seriesOverMessage = ChatColor.translateAlternateColorCodes('&', langConfig.getString("ActionBars.Warn.Series-Over.Message", "&cYou lost your combo series!"));
            String soundEffect = langConfig.getString("ActionBars.Warn.Series-Over.Sound-Effect", "BLOCK_ANVIL_LAND");
            float pitch = (float) langConfig.getDouble("ActionBars.Warn.Series-Over.Pitch", 2.0);

            sendActionBar(player, seriesOverMessage);

            if (isComboSoundEnabled(player)) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundEffect), 1.0f, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound effect: " + soundEffect);
                }
            }
        }
    }

    private boolean isComboCounterEnabled(Player player) {
        FileConfiguration playerData = plugin.getPlayerData(player.getName());
        return playerData.getBoolean("Settings.Combo-Counter", true);
    }

    private boolean isComboSoundEnabled(Player player) {
        FileConfiguration playerData = plugin.getPlayerData(player.getName());
        return playerData.getBoolean("Settings.Combo-Sound", true);
    }
}
