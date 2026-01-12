package pl.labmc.claims.listeners;

import org.bukkit.Location;
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
        Location blockLoc = event.getClickedBlock().getLocation();
        
        Claim claim = plugin.getClaimData().getClaimAt(blockLoc);
        if (claim == null) return;
        
        if (!claim.getBlockLocation().equals(blockLoc)) return;
        
        if (!plugin.getClaimManager().isClaimBlock(new ItemStack(event.getClickedBlock().getType()))) return;
        
        event.setCancelled(true);
        
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            return;
        }
        
        ClaimPanelGUI gui = new ClaimPanelGUI(plugin, player, claim);
        gui.open();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();
        
        Claim claim = plugin.getClaimData().getClaimAt(blockLoc);
        if (claim == null) return;
        
        if (!claim.getBlockLocation().equals(blockLoc)) return;
        
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(player, "error");
            return;
        }
        
        event.setDropItems(false);
        
        plugin.getClaimManager().removeClaim(claim);
        player.sendMessage(plugin.getMessage("claim-removed"));
    }
}
