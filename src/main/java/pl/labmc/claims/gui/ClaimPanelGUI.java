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
import pl.labmc.claims.commands.ClaimCommand;
import pl.labmc.claims.data.Claim;
import pl.labmc.claims.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class ClaimPanelGUI implements Listener {
    
    private final LabClaims plugin;
    private final Player player;
    private final Claim claim;
    private final Inventory inventory;
    
    public ClaimPanelGUI(LabClaims plugin, Player player, Claim claim) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        
        String title = plugin.colorize(plugin.getConfig().getString("gui.claim-panel.title"))
                .replace("%claim_name%", claim.getClaimName());
        int size = plugin.getConfig().getInt("gui.claim-panel.size", 54);
        this.inventory = Bukkit.createInventory(null, size, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }
    
    private void setupGUI() {
        fillBackground();
        
        // Upgrade (slot 13)
        if (plugin.getClaimManager().canUpgrade(claim.getTier())) {
            ItemStack upgrade = createUpgradeItem();
            inventory.setItem(13, upgrade);
        } else {
            ItemStack maxTier = createItem(
                Material.BARRIER,
                plugin.getConfig().getString("gui.icons.max-tier.name"),
                plugin.getConfig().getStringList("gui.icons.max-tier.lore")
            );
            inventory.setItem(13, maxTier);
        }
        
        // Rename (slot 20)
        ItemStack rename = createItem(
            Material.NAME_TAG,
            plugin.getConfig().getString("gui.icons.rename.name"),
            plugin.getConfig().getStringList("gui.icons.rename.lore")
        );
        String currentName = claim.getClaimName();
        List<String> renameLore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("gui.icons.rename.lore")) {
            renameLore.add(line.replace("%claim_name%", currentName));
        }
        rename = createItem(Material.NAME_TAG, 
            plugin.getConfig().getString("gui.icons.rename.name"), 
            renameLore);
        inventory.setItem(20, rename);
        
        // PvP Toggle (slot 22)
        ItemStack pvpToggle = createPvPToggle();
        inventory.setItem(22, pvpToggle);
        
        // Mob Spawning Toggle (slot 24)
        ItemStack mobToggle = createMobSpawningToggle();
        inventory.setItem(24, mobToggle);
        
        // Particles Toggle (slot 29)
        ItemStack particlesToggle = createParticlesToggle();
        inventory.setItem(29, particlesToggle);
        
        // Trust Manage (slot 31)
        ItemStack trustManage = createItem(
            Material.WRITABLE_BOOK,
            plugin.getConfig().getString("gui.icons.trust-manage.name"),
            plugin.getConfig().getStringList("gui.icons.trust-manage.lore")
        );
        List<String> trustLore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("gui.icons.trust-manage.lore")) {
            trustLore.add(line.replace("%trust_count%", String.valueOf(claim.getTrustedPlayers().size())));
        }
        trustManage = createItem(Material.WRITABLE_BOOK,
            plugin.getConfig().getString("gui.icons.trust-manage.name"),
            trustLore);
        inventory.setItem(31, trustManage);
        
        // Remove Claim (slot 33)
        ItemStack remove = createItem(
            Material.TNT,
            plugin.getConfig().getString("gui.icons.remove-claim.name"),
            plugin.getConfig().getStringList("gui.icons.remove-claim.lore")
        );
        inventory.setItem(33, remove);
        
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
    
    private ItemStack createUpgradeItem() {
        int nextTier = claim.getTier() + 1;
        int nextSize = plugin.getClaimManager().getSize(nextTier);
        double price = plugin.getClaimManager().getPrice(nextTier);
        
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("gui.icons.upgrade.lore")) {
            line = line.replace("%current_tier%", String.valueOf(claim.getTier()));
            line = line.replace("%next_tier%", String.valueOf(nextTier));
            line = line.replace("%price%", plugin.getEconomyManager().formatMoney(price));
            line = line.replace("%next_size%", String.valueOf(nextSize));
            lore.add(line);
        }
        
        return createItem(
            Material.EXPERIENCE_BOTTLE,
            plugin.getConfig().getString("gui.icons.upgrade.name"),
            lore
        );
    }
    
    private ItemStack createPvPToggle() {
        String path = claim.isPvpEnabled() ? "gui.icons.pvp-toggle.enabled" : "gui.icons.pvp-toggle.disabled";
        String materialName = plugin.getConfig().getString(path + ".material");
        Material material = Material.valueOf(materialName);
        
        return createItem(
            material,
            plugin.getConfig().getString(path + ".name"),
            plugin.getConfig().getStringList(path + ".lore")
        );
    }
    
    private ItemStack createMobSpawningToggle() {
        String path = claim.isMobSpawning() ? "gui.icons.mob-spawning-toggle.enabled" : "gui.icons.mob-spawning-toggle.disabled";
        String materialName = plugin.getConfig().getString(path + ".material");
        Material material = Material.valueOf(materialName);
        
        return createItem(
            material,
            plugin.getConfig().getString(path + ".name"),
            plugin.getConfig().getStringList(path + ".lore")
        );
    }
    
    private ItemStack createParticlesToggle() {
        String path = claim.isShowParticles() ? "gui.icons.particles-toggle.enabled" : "gui.icons.particles-toggle.disabled";
        String materialName = plugin.getConfig().getString(path + ".material");
        Material material = Material.valueOf(materialName);
        
        return createItem(
            material,
            plugin.getConfig().getString(path + ".name"),
            plugin.getConfig().getStringList(path + ".lore")
        );
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
        
        if (slot == 13 && clicked.getType() == Material.EXPERIENCE_BOTTLE) {
            handleUpgrade(clicker);
        } else if (slot == 20) {
            handleRename(clicker);
        } else if (slot == 22) {
            handlePvPToggle(clicker);
        } else if (slot == 24) {
            handleMobSpawningToggle(clicker);
        } else if (slot == 29) {
            handleParticlesToggle(clicker);
        } else if (slot == 31) {
            handleTrustManage(clicker);
        } else if (slot == 33) {
            handleRemove(clicker);
        } else if (slot == 45) {
            clicker.closeInventory();
            MainMenuGUI mainMenu = new MainMenuGUI(plugin, clicker);
            mainMenu.open();
        } else if (slot == 49) {
            clicker.closeInventory();
        }
    }
    
    private void handleUpgrade(Player player) {
        if (!plugin.getClaimManager().canUpgrade(claim.getTier())) {
            plugin.playSound(player, "error");
            return;
        }
        
        int nextTier = claim.getTier() + 1;
        double price = plugin.getClaimManager().getPrice(nextTier);
        
        if (!plugin.getEconomyManager().hasEnoughMoney(player, price)) {
            String msg = plugin.getMessage("not-enough-money").replace("%price%", String.valueOf(price));
            player.sendMessage(msg);
            plugin.playSound(player, "error");
            return;
        }
        
        plugin.getEconomyManager().withdrawMoney(player, price);
        plugin.getClaimManager().upgradeClaim(claim, nextTier);
        
        String msg = plugin.getMessage("claim-upgraded").replace("%tier%", String.valueOf(nextTier));
        player.sendMessage(msg);
        plugin.playSound(player, "claim-upgraded");
        
        player.closeInventory();
    }
    
    private void handleRename(Player player) {
        ClaimCommand claimCommand = (ClaimCommand) plugin.getCommand("claim").getExecutor();
        PlayerData playerData = claimCommand.getPlayerData();
        
        playerData.setAwaitingInput(player, PlayerData.InputType.CLAIM_RENAME, claim.getClaimId());
        
        player.closeInventory();
        player.sendMessage(plugin.colorize("&eWpisz nowa nazwe dzialki na czacie:"));
        player.sendMessage(plugin.colorize("&7(Wpisz 'cancel' aby anulowac)"));
    }
    
    private void handlePvPToggle(Player player) {
        claim.setPvpEnabled(!claim.isPvpEnabled());
        plugin.getClaimData().saveClaims();
        
        String msg = claim.isPvpEnabled() ? 
            plugin.getMessage("pvp-enabled") : 
            plugin.getMessage("pvp-disabled");
        player.sendMessage(msg);
        
        setupGUI();
        player.updateInventory();
    }
    
    private void handleMobSpawningToggle(Player player) {
        claim.setMobSpawning(!claim.isMobSpawning());
        plugin.getClaimData().saveClaims();
        
        String msg = claim.isMobSpawning() ? 
            plugin.getMessage("mob-spawning-enabled") : 
            plugin.getMessage("mob-spawning-disabled");
        player.sendMessage(msg);
        
        setupGUI();
        player.updateInventory();
    }
    
    private void handleParticlesToggle(Player player) {
        claim.setShowParticles(!claim.isShowParticles());
        plugin.getClaimData().saveClaims();
        
        String msg = claim.isShowParticles() ? 
            plugin.getMessage("particles-enabled") : 
            plugin.getMessage("particles-disabled");
        player.sendMessage(msg);
        
        setupGUI();
        player.updateInventory();
    }
    
    private void handleTrustManage(Player player) {
        player.closeInventory();
        TrustListGUI trustGUI = new TrustListGUI(plugin, player, claim);
        trustGUI.open();
    }
    
    private void handleRemove(Player player) {
        player.closeInventory();
        
        player.sendMessage(plugin.colorize("&4&lUWAGA!"));
        player.sendMessage(plugin.colorize("&cWpisz &e/claim remove &caby potwierdzic usuniecie dzialki!"));
        player.sendMessage(plugin.colorize("&7Nie otrzymasz zwrotu pieniedzy!"));
    }
}
