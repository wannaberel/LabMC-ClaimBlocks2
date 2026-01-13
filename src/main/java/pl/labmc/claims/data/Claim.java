package pl.labmc.claims.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Claim {
    
    private final UUID claimId;
    private final UUID ownerId;
    private String ownerName;
    private Location blockLocation;
    private int tier;
    private String claimName;
    private boolean pvpEnabled;
    private boolean mobSpawning;
    private boolean showParticles;
    private List<UUID> trustedPlayers;
    
    // NOWE UPRAWNIENIA
    private boolean tntEnabled;
    private boolean chestsPublic;
    private boolean villagersPublic;
    private boolean entryAllowed;
    
    public Claim(UUID claimId, UUID ownerId, String ownerName, Location blockLocation, int tier) {
        this.claimId = claimId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.blockLocation = blockLocation;
        this.tier = tier;
        this.claimName = "Dzialka gracza " + ownerName;
        this.pvpEnabled = false;
        this.mobSpawning = true;
        this.showParticles = true;
        this.trustedPlayers = new ArrayList<>();
        
        // Domyślne wartości nowych uprawnień
        this.tntEnabled = false;
        this.chestsPublic = false;
        this.villagersPublic = false;
        this.entryAllowed = true;
    }
    
    public UUID getClaimId() {
        return claimId;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public Location getBlockLocation() {
        return blockLocation;
    }
    
    public void setBlockLocation(Location location) {
        this.blockLocation = location;
    }
    
    public int getTier() {
        return tier;
    }
    
    public void setTier(int tier) {
        this.tier = tier;
    }
    
    public String getClaimName() {
        return claimName;
    }
    
    public void setClaimName(String claimName) {
        this.claimName = claimName;
    }
    
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
    
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
    
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }
    
    public boolean isShowParticles() {
        return showParticles;
    }
    
    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }
    
    public List<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }
    
    public void addTrustedPlayer(UUID playerId) {
        if (!trustedPlayers.contains(playerId)) {
            trustedPlayers.add(playerId);
        }
    }
    
    public void removeTrustedPlayer(UUID playerId) {
        trustedPlayers.remove(playerId);
    }
    
    public boolean isTrusted(UUID playerId) {
        return trustedPlayers.contains(playerId);
    }
    
    // NOWE METODY DLA UPRAWNIEŃ
    public boolean isTntEnabled() {
        return tntEnabled;
    }
    
    public void setTntEnabled(boolean tntEnabled) {
        this.tntEnabled = tntEnabled;
    }
    
    public boolean isChestsPublic() {
        return chestsPublic;
    }
    
    public void setChestsPublic(boolean chestsPublic) {
        this.chestsPublic = chestsPublic;
    }
    
    public boolean isVillagersPublic() {
        return villagersPublic;
    }
    
    public void setVillagersPublic(boolean villagersPublic) {
        this.villagersPublic = villagersPublic;
    }
    
    public boolean isEntryAllowed() {
        return entryAllowed;
    }
    
    public void setEntryAllowed(boolean entryAllowed) {
        this.entryAllowed = entryAllowed;
    }
    
    public int getSize() {
        return pl.labmc.claims.LabClaims.getInstance().getConfig().getInt("tiers." + tier + ".size", 16);
    }
    
    public boolean isInClaim(Location location) {
        if (blockLocation == null || location == null) return false;
        if (!blockLocation.getWorld().equals(location.getWorld())) return false;
        
        int size = getSize();
        int halfSize = size / 2;
        
        int claimX = blockLocation.getBlockX();
        int claimZ = blockLocation.getBlockZ();
        
        int locX = location.getBlockX();
        int locZ = location.getBlockZ();
        
        return locX >= (claimX - halfSize) && locX <= (claimX + halfSize) &&
               locZ >= (claimZ - halfSize) && locZ <= (claimZ + halfSize);
    }
    
    public World getWorld() {
        return blockLocation != null ? blockLocation.getWorld() : null;
    }
    
    public int getMinX() {
        int halfSize = getSize() / 2;
        return blockLocation.getBlockX() - halfSize;
    }
    
    public int getMaxX() {
        int halfSize = getSize() / 2;
        return blockLocation.getBlockX() + halfSize;
    }
    
    public int getMinZ() {
        int halfSize = getSize() / 2;
        return blockLocation.getBlockZ() - halfSize;
    }
    
    public int getMaxZ() {
        int halfSize = getSize() / 2;
        return blockLocation.getBlockZ() + halfSize;
    }
}
