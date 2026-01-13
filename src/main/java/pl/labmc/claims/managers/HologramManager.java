package pl.labmc.claims.managers;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramManager {
    
    private final LabClaims plugin;
    private final Map<UUID, List<ArmorStand>> holograms;
    
    public HologramManager(LabClaims plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
    }
    
    public void createHologram(Claim claim) {
        if (!plugin.getConfig().getBoolean("hologram.enabled", true)) return;
        
        removeHologram(claim);
        
        List<ArmorStand> stands = new ArrayList<>();
        Location blockLoc = claim.getBlockLocation();
        
        if (blockLoc == null || blockLoc.getWorld() == null) {
            plugin.getLogger().warning("Nie mozna stworzyc hologramu - nieprawidlowa lokalizacja dla claima: " + claim.getClaimId());
            return;
        }
        
        double height = plugin.getConfig().getDouble("hologram.height", 2.5);
        List<String> lines = plugin.getConfig().getStringList("hologram.lines");
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = replacePlaceholders(line, claim);
            
            Location spawnLoc = blockLoc.clone().add(0.5, height + (lines.size() - i - 1) * 0.25, 0.5);
            
            try {
                ArmorStand stand = (ArmorStand) blockLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setCustomNameVisible(true);
                stand.setCustomName(plugin.colorize(line));
                stand.setMarker(true);
                stand.setInvulnerable(true);
                stand.setPersistent(true);
                
                stands.add(stand);
            } catch (Exception e) {
                plugin.getLogger().warning("Blad tworzenia armor stand dla hologramu: " + e.getMessage());
            }
        }
        
        if (!stands.isEmpty()) {
            holograms.put(claim.getClaimId(), stands);
        }
    }
    
    public void updateHologram(Claim claim) {
        removeHologram(claim);
        createHologram(claim);
    }
    
    public void removeHologram(Claim claim) {
        if (claim == null || claim.getClaimId() == null) return;
        
        List<ArmorStand> stands = holograms.get(claim.getClaimId());
        if (stands != null) {
            for (ArmorStand stand : stands) {
                try {
                    if (stand != null && stand.isValid() && !stand.isDead()) {
                        stand.remove();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Blad usuwania armor stand: " + e.getMessage());
                }
            }
            holograms.remove(claim.getClaimId());
        }
    }
    
    public void loadAllHolograms() {
        for (Claim claim : plugin.getClaimData().getAllClaims()) {
            createHologram(claim);
        }
        plugin.getLogger().info("Zaladowano hologramy dla " + plugin.getClaimData().getAllClaims().size() + " dzialek");
    }
    
    public void removeAllHolograms() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                try {
                    if (stand != null && stand.isValid() && !stand.isDead()) {
                        stand.remove();
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        holograms.clear();
    }
    
    private String replacePlaceholders(String text, Claim claim) {
        text = text.replace("%claim_name%", claim.getClaimName());
        text = text.replace("%owner%", claim.getOwnerName());
        text = text.replace("%tier%", String.valueOf(claim.getTier()));
        text = text.replace("%size%", String.valueOf(claim.getSize()));
        return text;
    }
}
