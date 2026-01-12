package pl.labmc.claims.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.ArrayList;
import java.util.List;

public class MainMenuGUI implements Listener {
    
    private final LabClaims plugin;
    private final Player player;
    private final Inventory inventory;
    
    public MainMenuGUI(LabClaims plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        
        String title = plugin.colorize(plugin.getConfig().getString("gui.main-menu.title"));
        int size = plugin.getConfig().getInt("gui.main-menu.size", 54);
        this.inventory = Bukkit.createInventory(null, size, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }
    
    private void setupGUI() {
        fillBackground();
        
        List<Claim> playerClaims = plugin.getClaimData().getPlayerClaims(player.getUniqueId());
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        
        for (int i = 0; i < playerClaims.size() && i < slots.length; i++) {
            Claim claim = playerClaims.get(i);
            ItemStack claimItem = createClaimItem(claim);
            inventory.setItem(slots[i], claimItem);
        }
        
        ItemStack close = createItem(
            Material.BARRIER,
            plugin.getConfig().getString("gui.icons.close.name"),
            plugin.getConfig().getStringList("gui.icons.close.lore")
        );
        inventory.setItem(49, close);
    }
    
    private ItemStack createClaimItem(Claim claim) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String name = plugin.getConfig().getString("gui.icons.claim-info.name", "&d&l%claim_name%");
            name = name.replace("%claim_name%", claim.getClaimName());
            meta.setDisplayName(plugin.colorize(name));
            
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfig().getStringList("gui.icons.claim-info.lore")) {
                line = line.replace("%owner%", claim.getOwnerName());
                line = line.replace("%tier%", String.valueOf(claim.getTier()));
                line = line.replace("%size%", String.valueOf(claim.getSize()));
                line = line.replace("%location%", 
                    claim.getBlockLocation().getBlockX() + ", " + 
                    claim.getBlockLocation().getBlockY() + ", " + 
                    claim.getBlockLocation().getBlockZ());
                lore.add(plugin.colorize(line));
            }
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
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
        
        if (event.getSlot() == 49) {
            clicker.closeInventory();
            return;
        }
        
        if (clicked.getType() == Material.PLAYER_HEAD) {
            List<Claim> playerClaims = plugin.getClaimData().getPlayerClaims(player.getUniqueId());
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
            
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == event.getSlot() && i < playerClaims.size()) {
                    Claim claim = playerClaims.get(i);
                    clicker.closeInventory();
                    
                    ClaimPanelGUI panel = new ClaimPanelGUI(plugin, clicker, claim);
                    panel.open();
                    break;
                }
            }
        }
    }
}
