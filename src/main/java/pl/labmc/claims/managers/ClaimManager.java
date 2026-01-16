package pl.labmc.claims.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimManager {
    
    private final LabClaims plugin;
    
    public ClaimManager(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    public ItemStack createClaimBlock(int tier) {
        String materialName = plugin.getConfig().getString("tiers." + tier + ".block", "YELLOW_GLAZED_TERRACOTTA");
        Material material = Material.valueOf(materialName);
        
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String name = plugin.getConfig().getString("tiers." + tier + ".name", "&e&lDzialka Tier " + tier);
            meta.setDisplayName(plugin.colorize(name));
            
            List<String> lore = new ArrayList<>();
            lore.add(plugin.colorize("&7Tier: &f" + tier));
            lore.add(plugin.colorize("&7Rozmiar: &f" + getSize(tier) + "x" + getSize(tier)));
            lore.add(plugin.colorize("&7Ochrona od bedrocka do nieba"));
            lore.add("");
            lore.add(plugin.colorize("&ePostaw na ziemi aby stworzyc dzialke!"));
            
            meta.setLore(lore);
            
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public boolean isClaimBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        if (!meta.hasEnchant(Enchantment.LURE)) return false;
        
        for (int tier = 1; tier <= 5; tier++) {
            String materialName = plugin.getConfig().getString("tiers." + tier + ".block");
            if (materialName != null && item.getType() == Material.valueOf(materialName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int getClaimBlockTier(ItemStack item) {
        if (!isClaimBlock(item)) return -1;
        
        for (int tier = 1; tier <= 5; tier++) {
            String materialName = plugin.getConfig().getString("tiers." + tier + ".block");
            if (materialName != null && item.getType() == Material.valueOf(materialName)) {
                return tier;
            }
        }
        
        return -1;
    }
    
    public int getSize(int tier) {
        return plugin.getConfig().getInt("tiers." + tier + ".size", 16);
    }
    
    public double getPrice(int tier) {
        return plugin.getConfig().getDouble("tiers." + tier + ".price", 1000.0);
    }
    
    public Claim createClaim(Player player, Location location, int tier) {
        UUID claimId = UUID.randomUUID();
        Claim claim = new Claim(claimId, player.getUniqueId(), player.getName(), location, tier);
        
        String defaultName = plugin.getConfig().getString("defaults.claim-name", "Dzialka gracza %player%");
        defaultName = defaultName.replace("%player%", player.getName());
        claim.setClaimName(defaultName);
        
        claim.setPvpEnabled(plugin.getConfig().getBoolean("defaults.pvp-enabled", false));
        claim.setMobSpawning(plugin.getConfig().getBoolean("defaults.mob-spawning", true));
        claim.setShowParticles(plugin.getConfig().getBoolean("defaults.show-particles", true));
        
        plugin.getClaimData().addClaim(claim);
        
        plugin.getHologramManager().createHologram(claim);
        
        return claim;
    }
    
    public boolean canUpgrade(int currentTier) {
        return currentTier < 5;
    }
    
    public void upgradeClaim(Claim claim, int newTier) {
        Location oldLocation = claim.getBlockLocation();
        
        claim.setTier(newTier);
        
        oldLocation.getBlock().setType(getMaterial(newTier));
        
        plugin.getClaimData().saveClaims();
        
        plugin.getHologramManager().updateHologram(claim);
    }
    
    public Material getMaterial(int tier) {
        String materialName = plugin.getConfig().getString("tiers." + tier + ".block", "YELLOW_GLAZED_TERRACOTTA");
        return Material.valueOf(materialName);
    }
    
    public void removeClaim(Claim claim) {
        // NAPRAWIONE: Najpierw usuń hologram
        plugin.getHologramManager().removeHologram(claim);
        
        // Usuń blok działki
        Location blockLoc = claim.getBlockLocation();
        if (blockLoc != null && blockLoc.getBlock() != null) {
            blockLoc.getBlock().setType(Material.AIR);
        }
        
        // Usuń claim z danych (to zapisze zmiany)
        plugin.getClaimData().removeClaim(claim.getClaimId());
        
        plugin.getLogger().info("Usunieto dzialke: " + claim.getClaimName() + " (ID: " + claim.getClaimId() + ")");
    }
    
    public boolean canPlayerBuild(Player player, Location location) {
        Claim claim = plugin.getClaimData().getClaimAt(location);
        
        if (claim == null) return true;
        
        if (player.hasPermission("labclaims.bypass")) return true;
        
        if (claim.getOwnerId().equals(player.getUniqueId())) return true;
        
        if (claim.isTrusted(player.getUniqueId())) return true;
        
        return false;
    }
}
