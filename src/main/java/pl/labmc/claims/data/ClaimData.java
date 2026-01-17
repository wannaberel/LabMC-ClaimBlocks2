package pl.labmc.claims.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.labmc.claims.LabClaims;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimData {
    
    private final LabClaims plugin;
    private final Map<UUID, Claim> claims;
    private final Map<UUID, List<UUID>> playerClaims;
    private final File dataFile;
    
    public ClaimData(LabClaims plugin) {
        this.plugin = plugin;
        this.claims = new HashMap<>();
        this.playerClaims = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "claims.yml");
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void loadClaims() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        if (!config.contains("claims")) return;
        
        for (String key : config.getConfigurationSection("claims").getKeys(false)) {
            try {
                UUID claimId = UUID.fromString(key);
                UUID ownerId = UUID.fromString(config.getString("claims." + key + ".owner"));
                String ownerName = config.getString("claims." + key + ".owner-name");
                
                String worldName = config.getString("claims." + key + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                int x = config.getInt("claims." + key + ".x");
                int y = config.getInt("claims." + key + ".y");
                int z = config.getInt("claims." + key + ".z");
                Location location = new Location(world, x, y, z);
                
                int tier = config.getInt("claims." + key + ".tier");
                
                Claim claim = new Claim(claimId, ownerId, ownerName, location, tier);
                claim.setClaimName(config.getString("claims." + key + ".name", "Dzialka gracza " + ownerName));
                claim.setPvpEnabled(config.getBoolean("claims." + key + ".pvp", false));
                claim.setMobSpawning(config.getBoolean("claims." + key + ".mob-spawning", true));
                claim.setShowParticles(config.getBoolean("claims." + key + ".particles", true));
                
                // Wczytaj nowe uprawnienia
                claim.setTntEnabled(config.getBoolean("claims." + key + ".tnt-enabled", false));
                claim.setChestsPublic(config.getBoolean("claims." + key + ".chests-public", false));
                claim.setVillagersPublic(config.getBoolean("claims." + key + ".villagers-public", false));
                claim.setEntryAllowed(config.getBoolean("claims." + key + ".entry-allowed", true));
                
                List<String> trustedList = config.getStringList("claims." + key + ".trusted");
                for (String trustedId : trustedList) {
                    claim.addTrustedPlayer(UUID.fromString(trustedId));
                }
                
                claims.put(claimId, claim);
                playerClaims.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(claimId);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Blad wczytywania claima: " + key);
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("Wczytano " + claims.size() + " dzialek");
    }
    
    public void saveClaims() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, Claim> entry : claims.entrySet()) {
            String key = "claims." + entry.getKey().toString();
            Claim claim = entry.getValue();
            
            config.set(key + ".owner", claim.getOwnerId().toString());
            config.set(key + ".owner-name", claim.getOwnerName());
            config.set(key + ".world", claim.getBlockLocation().getWorld().getName());
            config.set(key + ".x", claim.getBlockLocation().getBlockX());
            config.set(key + ".y", claim.getBlockLocation().getBlockY());
            config.set(key + ".z", claim.getBlockLocation().getBlockZ());
            config.set(key + ".tier", claim.getTier());
            config.set(key + ".name", claim.getClaimName());
            config.set(key + ".pvp", claim.isPvpEnabled());
            config.set(key + ".mob-spawning", claim.isMobSpawning());
            config.set(key + ".particles", claim.isShowParticles());
            
            // Zapisz nowe uprawnienia
            config.set(key + ".tnt-enabled", claim.isTntEnabled());
            config.set(key + ".chests-public", claim.isChestsPublic());
            config.set(key + ".villagers-public", claim.isVillagersPublic());
            config.set(key + ".entry-allowed", claim.isEntryAllowed());
            
            List<String> trustedList = new ArrayList<>();
            for (UUID trustedId : claim.getTrustedPlayers()) {
                trustedList.add(trustedId.toString());
            }
            config.set(key + ".trusted", trustedList);
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie mozna zapisac dzialek!");
            e.printStackTrace();
        }
    }
    
    public void addClaim(Claim claim) {
        claims.put(claim.getClaimId(), claim);
        playerClaims.computeIfAbsent(claim.getOwnerId(), k -> new ArrayList<>()).add(claim.getClaimId());
        saveClaims();
    }
    
    public void removeClaim(UUID claimId) {
        Claim claim = claims.remove(claimId);
        if (claim != null) {
            List<UUID> playerClaimList = playerClaims.get(claim.getOwnerId());
            if (playerClaimList != null) {
                playerClaimList.remove(claimId);
            }
        }
        saveClaims();
    }
    
    public Claim getClaim(UUID claimId) {
        return claims.get(claimId);
    }
    
    public Claim getClaimAt(Location location) {
        for (Claim claim : claims.values()) {
            if (claim.isInClaim(location)) {
                return claim;
            }
        }
        return null;
    }
    
    public List<Claim> getPlayerClaims(UUID playerId) {
        List<Claim> result = new ArrayList<>();
        List<UUID> claimIds = playerClaims.get(playerId);
        if (claimIds != null) {
            for (UUID claimId : claimIds) {
                Claim claim = claims.get(claimId);
                if (claim != null) {
                    result.add(claim);
                }
            }
        }
        return result;
    }
    
    public Collection<Claim> getAllClaims() {
        return claims.values();
    }
    
    public boolean hasOverlap(Location location, int size) {
        int halfSize = size / 2;
        
        for (Claim claim : claims.values()) {
            if (!claim.getWorld().equals(location.getWorld())) continue;
            
            int claimHalfSize = claim.getSize() / 2;
            
            int newMinX = location.getBlockX() - halfSize;
            int newMaxX = location.getBlockX() + halfSize;
            int newMinZ = location.getBlockZ() - halfSize;
            int newMaxZ = location.getBlockZ() + halfSize;
            
            int claimMinX = claim.getBlockLocation().getBlockX() - claimHalfSize;
            int claimMaxX = claim.getBlockLocation().getBlockX() + claimHalfSize;
            int claimMinZ = claim.getBlockLocation().getBlockZ() - claimHalfSize;
            int claimMaxZ = claim.getBlockLocation().getBlockZ() + claimHalfSize;
            
            if (newMinX <= claimMaxX && newMaxX >= claimMinX &&
                newMinZ <= claimMaxZ && newMaxZ >= claimMinZ) {
                return true;
            }
        }
        
        return false;
    }
}
