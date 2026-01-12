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
import java.util.Collection;
import java.util.List;

public class AdminPanelGUI implements Listener {
    
    private final LabClaims plugin;
    private final Player player;
    private final Inventory inventory;
    private int page;
    private final List<Claim> allClaims;
    
    public AdminPanelGUI(LabClaims plugin, Player player) {
        this(plugin, player, 0);
    }
    
    public AdminPanelGUI(LabClaims plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.allClaims = new ArrayList<>(plugin.getClaimData().getAllClaims());
        
        String title = plugin.colorize(plugin.getConfig().getString("gui.admin-panel.title"));
        int size = plugin.getConfig().getInt("gui.admin-panel.size", 54);
        this.inventory = Bukkit.createInventory(null, size, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }
    
    private void setupGUI() {
        fillBackground();
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int itemsPerPage = slots.length;
        int startIndex = page * itemsPerPage;
        
        for (int i = 0; i < itemsPerPage && (startIndex + i) < allClaims.size(); i++) {
            Claim claim = allClaims.get(startIndex + i);
            ItemStack claimItem = createClaimItem(claim);
            inventory.setItem(slots[i], claimItem);
        }
        
        // Statistics (slot 4)
        ItemStack stats = createStatsItem();
        inventory.setItem(4, stats);
        
        // Previous Page (slot 45)
        if (page > 0) {
            ItemStack prev = createItem(
                Material.ARROW,
                "&e&lPoprzednia Strona",
                List.of("&7Strona " + page)
            );
            inventory.setItem(45, prev);
        }
        
        // Next Page (slot 53)
        if ((page + 1) * itemsPerPage < allClaims.size()) {
            ItemStack next = createItem(
                Material.ARROW,
                "&e&lNastepna Strona",
                List.of("&7Strona " + (page + 2))
            );
            inventory.setItem(53, next);
        }
        
        // Close (slot 49)
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
            meta.setDisplayName(plugin.colorize("&d&l" + claim.getClaimName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(plugin.colorize("&7Wlasciciel: &f" + claim.getOwnerName()));
            lore.add(plugin.colorize("&7Tier: &f" + claim.getTier()));
            lore.add(plugin.colorize("&7Rozmiar: &f" + claim.getSize() + "x" + claim.getSize()));
            lore.add(plugin.colorize("&7Lokalizacja: &f" + 
                claim.getBlockLocation().getBlockX() + ", " + 
                claim.getBlockLocation().getBlockY() + ", " + 
                claim.getBlockLocation().getBlockZ()));
            lore.add(plugin.colorize("&7PvP: " + (claim.isPvpEnabled() ? "&aWlaczone" : "&cWylaczone")));
            lore.add(plugin.colorize("&7Spawn mobow: " + (claim.isMobSpawning() ? "&aWlaczony" : "&cWylaczony")));
            lore.add(plugin.colorize("&7Zaufani: &f" + claim.getTrustedPlayers().size()));
            lore.add("");
            lore.add(plugin.colorize("&eLewy klik - Teleportuj"));
            lore.add(plugin.colorize("&cPrawy klik - Usun dzialke"));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createStatsItem() {
        Collection<Claim> claims = plugin.getClaimData().getAllClaims();
        
        int tier1 = 0, tier2 = 0, tier3 = 0, tier4 = 0, tier5 = 0;
        
        for (Claim claim : claims) {
            switch (claim.getTier()) {
                case 1: tier1++; break;
                case 2: tier2++; break;
                case 3: tier3++; break;
                case 4: tier4++; break;
                case 5: tier5++; break;
            }
        }
        
        List<String> lore = new ArrayList<>();
        lore.add(plugin.colorize("&7Laczna liczba dzialek: &f" + claims.size()));
        lore.add("");
        lore.add(plugin.colorize("&eTier 1: &f" + tier1));
        lore.add(plugin.colorize("&aTier 2: &f" + tier2));
        lore.add(plugin.colorize("&bTier 3: &f" + tier3));
        lore.add(plugin.colorize("&dTier 4: &f" + tier4));
        lore.add(plugin.colorize("&cTier 5: &f" + tier5));
        
        return createItem(Material.PAPER, "&d&lStatystyki Dzialek", lore);
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
        
        if (slot == 45 && page > 0) {
            clicker.closeInventory();
            AdminPanelGUI prevPage = new AdminPanelGUI(plugin, clicker, page - 1);
            prevPage.open();
        } else if (slot == 53) {
            int itemsPerPage = 21;
            if ((page + 1) * itemsPerPage < allClaims.size()) {
                clicker.closeInventory();
                AdminPanelGUI nextPage = new AdminPanelGUI(plugin, clicker, page + 1);
                nextPage.open();
            }
        } else if (slot == 49) {
            clicker.closeInventory();
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            handleClaimClick(clicker, event);
        }
    }
    
    private void handleClaimClick(Player player, InventoryClickEvent event) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int itemsPerPage = slots.length;
        int startIndex = page * itemsPerPage;
        
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == event.getSlot() && (startIndex + i) < allClaims.size()) {
                Claim claim = allClaims.get(startIndex + i);
                
                if (event.isLeftClick()) {
                    player.teleport(claim.getBlockLocation());
                    player.sendMessage(plugin.colorize("&aTeleportowano do dzialki: &f" + claim.getClaimName()));
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    plugin.getClaimManager().removeClaim(claim);
                    player.sendMessage(plugin.colorize("&cUsunieto dzialke: &f" + claim.getClaimName()));
                    player.closeInventory();
                    
                    AdminPanelGUI newGUI = new AdminPanelGUI(plugin, player, page);
                    newGUI.open();
                }
                break;
            }
        }
    }
}
