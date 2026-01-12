package pl.labmc.claims.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProtectionListener implements Listener {
    
    private final LabClaims plugin;
    private final Map<UUID, UUID> lastClaimLocation;
    
    public ProtectionListener(LabClaims plugin) {
        this.plugin = plugin;
        this.lastClaimLocation = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("protection.block-break", true)) return;
        
        Player player = event.getPlayer();
        
        if (!plugin.getClaimManager().canPlayerBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(player, "error");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfig().getBoolean("protection.block-place", true)) return;
        
        Player player = event.getPlayer();
        
        if (plugin.getClaimManager().isClaimBlock(event.getItemInHand())) {
            return;
        }
        
        if (!plugin.getClaimManager().canPlayerBuild(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(player, "error");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("protection.block-interact", true)) return;
        if (!event.hasBlock()) return;
        
        Player player = event.getPlayer();
        Material blockType = event.getClickedBlock().getType();
        
        List<String> protectedBlocks = plugin.getConfig().getStringList("protection.protected-interactions");
        
        boolean isProtected = false;
        for (String materialName : protectedBlocks) {
            try {
                if (blockType == Material.valueOf(materialName) || blockType.name().contains(materialName)) {
                    isProtected = true;
                    break;
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid material
            }
        }
        
        if (!isProtected) return;
        
        if (!plugin.getClaimManager().canPlayerBuild(player, event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(player, "error");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Entity victim = event.getEntity();
        
        Claim claim = plugin.getClaimData().getClaimAt(victim.getLocation());
        if (claim == null) return;
        
        if (victim instanceof Player) {
            if (!claim.isPvpEnabled()) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.colorize("&cPvP jest wylaczone na tej dzialce!"));
                plugin.playSound(attacker, "error");
                return;
            }
        }
        
        if (!plugin.getConfig().getBoolean("protection.entity-damage", true)) return;
        
        if (!plugin.getClaimManager().canPlayerBuild(attacker, victim.getLocation())) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getMessage("protection-message"));
            plugin.playSound(attacker, "error");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getConfig().getBoolean("protection.explosions", true)) return;
        
        event.blockList().removeIf(block -> {
            Claim claim = plugin.getClaimData().getClaimAt(block.getLocation());
            return claim != null;
        });
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;
        
        Claim claim = plugin.getClaimData().getClaimAt(event.getLocation());
        if (claim == null) return;
        
        if (!claim.isMobSpawning()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Claim claimFrom = plugin.getClaimData().getClaimAt(event.getFrom());
        Claim claimTo = plugin.getClaimData().getClaimAt(event.getTo());
        
        if (claimFrom == claimTo) return;
        
        if (claimTo != null) {
            UUID lastClaim = lastClaimLocation.get(player.getUniqueId());
            if (lastClaim == null || !lastClaim.equals(claimTo.getClaimId())) {
                String msg = plugin.getMessage("entered-claim")
                        .replace("%name%", claimTo.getClaimName())
                        .replace("%owner%", claimTo.getOwnerName());
                player.sendMessage(msg);
                lastClaimLocation.put(player.getUniqueId(), claimTo.getClaimId());
            }
        } else {
            if (claimFrom != null) {
                player.sendMessage(plugin.getMessage("left-claim"));
                lastClaimLocation.remove(player.getUniqueId());
            }
        }
    }
}
