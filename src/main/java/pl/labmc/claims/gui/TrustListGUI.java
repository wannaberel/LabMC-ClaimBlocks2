package pl.labmc.claims.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.commands.ClaimCommand;
import pl.labmc.claims.data.Claim;
import pl.labmc.claims.data.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustListGUI implements Listener {
    
    private final LabClaims plugin;
    private final Player player;
    private final Claim claim;
    private final Inventory inventory;
    
    public TrustListGUI(LabClaims plugin, Player player, Claim claim) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        
        String title = plugin.colorize(plugin.getConfig().getString("gui.trust-list.title"))
                .replace("%claim_name%", claim.getClaimName());
        int size = plugin.getConfig().getInt("gui.trust-list.size", 54);
        this.inventory = Bukkit.createInventory(null, size, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }
    
    private void setupGUI() {
        fillBackground();
        
        List<UUID> trustedPlayers = claim.getTrustedPlayers();
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        
        for (int i = 0; i < trustedPlayers.size() && i < slots.length; i++) {
            UUID playerId = trustedPlayers.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            
            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                
                String name = plugin.getConfig().getString("gui.icons.trust-player.name")
                        .replace("%player%", offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown");
                meta.setDisplayName(plugin.colorize(name));
                
                List<String> lore = new ArrayList<>();
                for (String line : plugin.getConfig().getStringList("gui.icons.trust-player.lore")) {
                    lore.add(plugin.colorize(line));
                }
                meta.setLore(lore);
                
                playerHead.setItemMeta(meta);
            }
            
            inventory.setItem(slots[i], playerHead);
        }
        
        // Add Trust Button (slot 40)
        ItemStack addTrust = createItem(
            Material.LIME_DYE,
            plugin.getConfig().getString("gui.icons.add-trust.name"),
            plugin.getConfig().getStringList("gui.icons.add-trust.lore")
        );
        inventory.setItem(40, addTrust);
        
        // Back (slot 45)
        ItemStack back = createItem(
            Material.ARROW,
            plugin.getConfig().getString("gui.icons.back.name"),
            plugin.getConfig().getStringList("gui.icons.back.lore")
        );
        inventory.setItem(45, back);
        
        // Close (slot 49)
        ItemStack close = createItem(
            Material.BARRIER,
            plugin.getConfig().getString("gui.icons.close.name"),
            plugin.getConfig().getStringList("gui.icons.close.lore")
        );
        inventory.setItem(49, close);
    }
    
    private void fillBackground() {
        String materialName = plugin.getConfig().getString("gui.icons.filler.material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.valueOf(materialName);
        ItemStack filler = createItem(material, " ", new ArrayList<>());
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(plugin.colorize(name));
            
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(plugin.colorize(line));
            }
            meta.setLore(coloredLore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        event.setCancelled(true);
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        plugin.playSound(clicker, "gui-click");
        
        int slot = event.getSlot();
        
        if (slot == 40) {
            handleAddTrust(clicker);
        } else if (slot == 45) {
            clicker.closeInventory();
            ClaimPanelGUI panel = new ClaimPanelGUI(plugin, clicker, claim);
            panel.open();
        } else if (slot == 49) {
            clicker.closeInventory();
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            handleRemoveTrust(clicker, clicked);
        }
    }
    
    private void handleAddTrust(Player player) {
        ClaimCommand claimCommand = (ClaimCommand) plugin.getCommand("claim").getExecutor();
        PlayerData playerData = claimCommand.getPlayerData();
        
        playerData.setAwaitingInput(player, PlayerData.InputType.ADD_TRUST, claim.getClaimId());
        
        player.closeInventory();
        player.sendMessage(plugin.colorize("&eWpisz nick gracza ktorego chcesz dodac:"));
        player.sendMessage(plugin.colorize("&7(Wpisz 'cancel' aby anulowac)"));
    }
    
    private void handleRemoveTrust(Player player, ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta)) return;
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        OfflinePlayer target = meta.getOwningPlayer();
        
        if (target == null) return;
        
        claim.removeTrustedPlayer(target.getUniqueId());
        plugin.getClaimData().saveClaims();
        
        String msg = plugin.getMessage("player-untrusted").replace("%player%", target.getName());
        player.sendMessage(msg);
        
        player.closeInventory();
        TrustListGUI newGUI = new TrustListGUI(plugin, player, claim);
        newGUI.open();
    }
}
