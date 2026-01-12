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
        double height = plugin.getConfig().getDouble("hologram.height", 2.5);
        
        List<String> lines = plugin.getConfig().getStringList("hologram.lines");
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = replacePlaceholders(line, claim);
            
            Location spawnLoc = blockLoc.clone().add(0.5, height + (lines.size() - i - 1) * 0.25, 0.5);
            
            ArmorStand stand = (ArmorStand) blockLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(plugin.colorize(line));
            stand.setMarker(true);
            stand.setInvulnerable(true);
            
            stands.add(stand);
        }
        
        holograms.put(claim.getClaimId(), stands);
    }
    
    public void updateHologram(Claim claim) {
        createHologram(claim);
    }
    
    public void removeHologram(Claim claim) {
        List<ArmorStand> stands = holograms.remove(claim.getClaimId());
        if (stands != null) {
            for (ArmorStand stand : stands) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }
    }
    
    public void loadAllHolograms() {
        for (Claim claim : plugin.getClaimData().getAllClaims()) {
            createHologram(claim);
        }
    }
    
    public void removeAllHolograms() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
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
