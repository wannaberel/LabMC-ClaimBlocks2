package pl.labmc.claims.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;
import pl.labmc.claims.data.PlayerData;
import pl.labmc.claims.gui.ClaimPanelGUI;

import java.util.List;

public class ClaimBlockListener implements Listener {
    
    private final LabClaims plugin;
    
    public ClaimBlockListener(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (!plugin.getClaimManager().isClaimBlock(item)) return;
        
        int tier = plugin.getClaimManager().getClaimBlockTier(item);
        if (tier == -1) return;
        
        Location location = event.getBlock().getLocation();
        int size = plugin.getClaimManager().getSize(tier);
        
        if (plugin.getClaimData().hasOverlap(location, size)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("claim-overlap"));
            plugin.playSound(player, "error");
            return;
        }
        
        PlayerData playerData = new PlayerData(plugin);
        List<Claim> playerClaims = plugin.getClaimData().getPlayerClaims(player.getUniqueId());
        int limit = playerData.getPlayerClaimLimit(player);
        
        if (playerClaims.size() >= limit) {
            event.setCancelled(true);
            String msg = plugin.getMessage("claim-limit-reached")
                    .replace("%current%", String.valueOf(playerClaims.size()))
                    .replace("%max%", String.valueOf(limit));
            player.sendMessage(msg);
            plugin.playSound(player, "error");
            return;
        }
        
        Claim claim = plugin.getClaimManager().createClaim(player, location, tier);
        
        player.sendMessage(plugin.getMessage("claim-created"));
        player.sendMessage(plugin.colorize("&7Kliknij PPM na claim block aby otworzyc panel!"));
        
        plugin.playSound(player, "claim-created");
    }
    
    @EventHandler
    public void onClaimBlockInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (!event.hasBlock()) return;
        
        Player player = event.getPlayer();
        Location clickedLoc = event.getClickedBlock().getLocation();
        Material clickedType = event.getClickedBlock().getType();
        
        // Sprawdź czy to może być claim block (glazed terracotta)
        if (!clickedType.name().contains("GLAZED_TERRACOTTA")) return;
        
        // Znajdź claim na tej lokalizacji
        Claim claim = null;
        for (Claim c : plugin.getClaimData().getAllClaims()) {
            Location claimLoc = c.getBlockLocation();
            if (claimLoc.getWorld().equals(clickedLoc.getWorld()) &&
                claimLoc.getBlockX() == clickedLoc.getBlockX() &&
                claimLoc.getBlockY() == clickedLoc.getBlockY() &&
                claimLoc.getBlockZ() == clickedLoc.getBlockZ()) {
                claim = c;
                break;
            }
        }
        
        if (claim == null) return;
        
        event.setCancelled(true);
        
        // Sprawdź czy gracz jest właścicielem lub adminem
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            plugin.playSound(player, "error");
            return;
        }
        
        // Otwórz Panel Działki
        ClaimPanelGUI gui = new ClaimPanelGUI(plugin, player, claim);
        gui.open();
        plugin.playSound(player, "gui-click");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();
        
        // Znajdź claim dokładnie na tej lokalizacji
        Claim claim = null;
        for (Claim c : plugin.getClaimData().getAllClaims()) {
            Location claimLoc = c.getBlockLocation();
            if (claimLoc.getWorld().equals(blockLoc.getWorld()) &&
                claimLoc.getBlockX() == blockLoc.getBlockX() &&
                claimLoc.getBlockY() == blockLoc.getBlockY() &&
                claimLoc.getBlockZ() == blockLoc.getBlockZ()) {
                claim = c;
                break;
            }
        }
        
        if (claim == null) return;
        
        // Sprawdź permisje
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(player, "error");
            return;
        }
        
        // Nie dropuj bloku
        event.setDropItems(false);
        
        // Usuń claim
        plugin.getClaimManager().removeClaim(claim);
        player.sendMessage(plugin.getMessage("claim-removed"));
    }
}
