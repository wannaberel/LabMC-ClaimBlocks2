package pl.labmc.claims.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.commands.ClaimCommand;
import pl.labmc.claims.data.Claim;
import pl.labmc.claims.data.PlayerData;

import java.util.UUID;

public class ChatListener implements Listener {
    
    private final LabClaims plugin;
    
    public ChatListener(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        ClaimCommand claimCommand = (ClaimCommand) plugin.getCommand("claim").getExecutor();
        PlayerData playerData = claimCommand.getPlayerData();
        
        if (!playerData.isAwaitingInput(player.getUniqueId())) return;
        
        event.setCancelled(true);
        
        String input = event.getMessage();
        
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.colorize("&cAnulowano."));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        PlayerData.InputType inputType = playerData.getInputType(player.getUniqueId());
        UUID claimId = playerData.getClaimContext(player.getUniqueId());
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (inputType == PlayerData.InputType.CLAIM_RENAME) {
                handleRename(player, claimId, input, playerData);
            } else if (inputType == PlayerData.InputType.ADD_TRUST) {
                handleAddTrust(player, claimId, input, playerData);
            }
        });
    }
    
    private void handleRename(Player player, UUID claimId, String newName, PlayerData playerData) {
        Claim claim = plugin.getClaimData().getClaim(claimId);
        
        if (claim == null) {
            player.sendMessage(plugin.colorize("&cDzialka nie istnieje!"));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        if (newName.length() > 32) {
            player.sendMessage(plugin.colorize("&cNazwa jest za dluga! Max 32 znaki."));
            return;
        }
        
        claim.setClaimName(newName);
        plugin.getClaimData().saveClaims();
        plugin.getHologramManager().updateHologram(claim);
        
        String msg = plugin.getMessage("claim-renamed").replace("%name%", newName);
        player.sendMessage(msg);
        
        playerData.clearInput(player.getUniqueId());
    }
    
    private void handleAddTrust(Player player, UUID claimId, String targetName, PlayerData playerData) {
        Claim claim = plugin.getClaimData().getClaim(claimId);
        
        if (claim == null) {
            player.sendMessage(plugin.colorize("&cDzialka nie istnieje!"));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }
        
        if (claim.isTrusted(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("already-trusted"));
            playerData.clearInput(player.getUniqueId());
            return;
        }
        
        claim.addTrustedPlayer(target.getUniqueId());
        plugin.getClaimData().saveClaims();
        
        String msg = plugin.getMessage("player-trusted").replace("%player%", target.getName());
        player.sendMessage(msg);
        
        playerData.clearInput(player.getUniqueId());
    }
}
