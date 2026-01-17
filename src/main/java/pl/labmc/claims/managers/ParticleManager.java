package pl.labmc.claims.managers;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleManager {
    
    private final LabClaims plugin;
    private final Map<UUID, BukkitRunnable> activeEffects;
    
    public ParticleManager(LabClaims plugin) {
        this.plugin = plugin;
        this.activeEffects = new HashMap<>();
    }
    
    public void showClaimBorders(Player player, Claim claim) {
        if (!claim.isShowParticles()) return;
        
        stopShowingBorders(player);
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || claim == null) {
                    cancel();
                    return;
                }
                
                if (!claim.isInClaim(player.getLocation())) {
                    cancel();
                    activeEffects.remove(player.getUniqueId());
                    return;
                }
                
                spawnBorderParticles(player, claim);
            }
        };
        
        task.runTaskTimer(plugin, 0L, 20L);
        activeEffects.put(player.getUniqueId(), task);
    }
    
    public void stopShowingBorders(Player player) {
        BukkitRunnable task = activeEffects.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    private void spawnBorderParticles(Player player, Claim claim) {
        String particleType = plugin.getConfig().getString("particles.type", "REDSTONE");
        int density = plugin.getConfig().getInt("particles.density", 5);
        int heightInterval = plugin.getConfig().getInt("particles.height-interval", 5);
        
        int minX = claim.getMinX();
        int maxX = claim.getMaxX();
        int minZ = claim.getMinZ();
        int maxZ = claim.getMaxZ();
        int playerY = player.getLocation().getBlockY();
        
        Particle particle = Particle.valueOf(particleType);
        Particle.DustOptions dustOptions = null;
        
        if (particle == Particle.REDSTONE) {
            int r = plugin.getConfig().getInt("particles.color.red", 255);
            int g = plugin.getConfig().getInt("particles.color.green", 0);
            int b = plugin.getConfig().getInt("particles.color.blue", 255);
            dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), 1.0f);
        }
        
        for (int y = playerY - 5; y <= playerY + 10; y += heightInterval) {
            for (int x = minX; x <= maxX; x += density) {
                Location loc1 = new Location(claim.getWorld(), x + 0.5, y, minZ + 0.5);
                Location loc2 = new Location(claim.getWorld(), x + 0.5, y, maxZ + 0.5);
                
                if (dustOptions != null) {
                    player.spawnParticle(particle, loc1, 1, 0, 0, 0, 0, dustOptions);
                    player.spawnParticle(particle, loc2, 1, 0, 0, 0, 0, dustOptions);
                } else {
                    player.spawnParticle(particle, loc1, 1);
                    player.spawnParticle(particle, loc2, 1);
                }
            }
            
            for (int z = minZ; z <= maxZ; z += density) {
                Location loc1 = new Location(claim.getWorld(), minX + 0.5, y, z + 0.5);
                Location loc2 = new Location(claim.getWorld(), maxX + 0.5, y, z + 0.5);
                
                if (dustOptions != null) {
                    player.spawnParticle(particle, loc1, 1, 0, 0, 0, 0, dustOptions);
                    player.spawnParticle(particle, loc2, 1, 0, 0, 0, 0, dustOptions);
                } else {
                    player.spawnParticle(particle, loc1, 1);
                    player.spawnParticle(particle, loc2, 1);
                }
            }
        }
    }
    
    public void stopAll() {
        for (BukkitRunnable task : activeEffects.values()) {
            task.cancel();
        }
        activeEffects.clear();
    }
}
