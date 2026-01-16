package pl.labmc.claims.tasks;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.Collection;

public class ParticleTask extends BukkitRunnable {
    
    private final LabClaims plugin;
    
    public ParticleTask(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        Collection<Claim> claims = plugin.getClaimData().getAllClaims();
        
        for (Claim claim : claims) {
            if (!claim.isShowParticles()) continue;
            
            // Sprawdź czy są gracze w pobliżu (optymalizacja)
            boolean hasNearbyPlayers = false;
            Location claimLoc = claim.getBlockLocation();
            
            if (claimLoc == null || claimLoc.getWorld() == null) continue;
            
            for (Player player : claimLoc.getWorld().getPlayers()) {
                if (player.getLocation().distance(claimLoc) < 100) {
                    hasNearbyPlayers = true;
                    break;
                }
            }
            
            if (!hasNearbyPlayers) continue;
            
            // Pokaż granice
            showClaimBorders(claim);
        }
    }
    
    private void showClaimBorders(Claim claim) {
        Location blockLoc = claim.getBlockLocation();
        if (blockLoc == null || blockLoc.getWorld() == null) return;
        
        int halfSize = claim.getSize() / 2;
        
        int minX = blockLoc.getBlockX() - halfSize;
        int maxX = blockLoc.getBlockX() + halfSize;
        int minZ = blockLoc.getBlockZ() - halfSize;
        int maxZ = blockLoc.getBlockZ() + halfSize;
        
        // Pobierz kolor z configu
        int red = plugin.getConfig().getInt("particles.color.red", 255);
        int green = plugin.getConfig().getInt("particles.color.green", 0);
        int blue = plugin.getConfig().getInt("particles.color.blue", 255);
        
        Particle.DustOptions dustOptions = new Particle.DustOptions(
            Color.fromRGB(red, green, blue), 1.0f
        );
        
        int density = plugin.getConfig().getInt("particles.density", 20);
        int heightInterval = plugin.getConfig().getInt("particles.height-interval", 5);
        
        // Rysuj 4 ściany na poziomie gracza
        int playerY = blockLoc.getBlockY();
        
        for (int y = playerY - 5; y <= playerY + 10; y += heightInterval) {
            // Północna ściana (minZ)
            for (int x = minX; x <= maxX; x += (maxX - minX) / density) {
                Location loc = new Location(blockLoc.getWorld(), x + 0.5, y + 0.5, minZ + 0.5);
                blockLoc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
            }
            
            // Południowa ściana (maxZ)
            for (int x = minX; x <= maxX; x += (maxX - minX) / density) {
                Location loc = new Location(blockLoc.getWorld(), x + 0.5, y + 0.5, maxZ + 0.5);
                blockLoc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
            }
            
            // Zachodnia ściana (minX)
            for (int z = minZ; z <= maxZ; z += (maxZ - minZ) / density) {
                Location loc = new Location(blockLoc.getWorld(), minX + 0.5, y + 0.5, z + 0.5);
                blockLoc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
            }
            
            // Wschodnia ściana (maxX)
            for (int z = minZ; z <= maxZ; z += (maxZ - minZ) / density) {
                Location loc = new Location(blockLoc.getWorld(), maxX + 0.5, y + 0.5, z + 0.5);
                blockLoc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
            }
        }
        
        // Rogi (pionowe linie)
        for (int y = playerY - 5; y <= playerY + 10; y++) {
            // Róg 1 (minX, minZ)
            Location corner1 = new Location(blockLoc.getWorld(), minX + 0.5, y + 0.5, minZ + 0.5);
            blockLoc.getWorld().spawnParticle(Particle.REDSTONE, corner1, 1, dustOptions);
            
            // Róg 2 (maxX, minZ)
            Location corner2 = new Location(blockLoc.getWorld(), maxX + 0.5, y + 0.5, minZ + 0.5);
            blockLoc.getWorld().spawnParticle(Particle.REDSTONE, corner2, 1, dustOptions);
            
            // Róg 3 (minX, maxZ)
            Location corner3 = new Location(blockLoc.getWorld(), minX + 0.5, y + 0.5, maxZ + 0.5);
            blockLoc.getWorld().spawnParticle(Particle.REDSTONE, corner3, 1, dustOptions);
            
            // Róg 4 (maxX, maxZ)
            Location corner4 = new Location(blockLoc.getWorld(), maxX + 0.5, y + 0.5, maxZ + 0.5);
            blockLoc.getWorld().spawnParticle(Particle.REDSTONE, corner4, 1, dustOptions);
        }
    }
}
