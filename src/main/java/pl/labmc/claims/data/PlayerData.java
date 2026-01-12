package pl.labmc.claims.data;

import org.bukkit.entity.Player;
import pl.labmc.claims.LabClaims;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    
    private final LabClaims plugin;
    private final Map<UUID, String> awaitingInput;
    private final Map<UUID, InputType> inputType;
    private final Map<UUID, UUID> claimContext;
    
    public PlayerData(LabClaims plugin) {
        this.plugin = plugin;
        this.awaitingInput = new HashMap<>();
        this.inputType = new HashMap<>();
        this.claimContext = new HashMap<>();
    }
    
    public void setAwaitingInput(Player player, InputType type, UUID claimId) {
        awaitingInput.put(player.getUniqueId(), "");
        inputType.put(player.getUniqueId(), type);
        claimContext.put(player.getUniqueId(), claimId);
    }
    
    public boolean isAwaitingInput(UUID playerId) {
        return awaitingInput.containsKey(playerId);
    }
    
    public InputType getInputType(UUID playerId) {
        return inputType.get(playerId);
    }
    
    public UUID getClaimContext(UUID playerId) {
        return claimContext.get(playerId);
    }
    
    public void clearInput(UUID playerId) {
        awaitingInput.remove(playerId);
        inputType.remove(playerId);
        claimContext.remove(playerId);
    }
    
    public int getPlayerClaimLimit(Player player) {
        if (player.hasPermission("labclaims.unlimited")) {
            return Integer.MAX_VALUE;
        }
        
        if (player.hasPermission("labclaims.limit.10")) {
            return 10;
        }
        if (player.hasPermission("labclaims.limit.5")) {
            return 5;
        }
        if (player.hasPermission("labclaims.limit.3")) {
            return 3;
        }
        if (player.hasPermission("labclaims.limit.1")) {
            return 1;
        }
        
        return 1;
    }
    
    public enum InputType {
        CLAIM_RENAME,
        ADD_TRUST
    }
}
