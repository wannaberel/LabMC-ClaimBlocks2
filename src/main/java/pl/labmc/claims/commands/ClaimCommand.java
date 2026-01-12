package pl.labmc.claims.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.labmc.claims.LabClaims;
import pl.labmc.claims.data.Claim;
import pl.labmc.claims.data.PlayerData;
import pl.labmc.claims.gui.AdminPanelGUI;
import pl.labmc.claims.gui.MainMenuGUI;

import java.util.List;

public class ClaimCommand implements CommandExecutor {
    
    private final LabClaims plugin;
    private final PlayerData playerData;
    
    public ClaimCommand(LabClaims plugin) {
        this.plugin = plugin;
        this.playerData = new PlayerData(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cTylko gracze moga uzyc tej komendy!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            MainMenuGUI gui = new MainMenuGUI(plugin, player);
            gui.open();
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "buy":
                handleBuy(player, args);
                break;
                
            case "info":
                handleInfo(player);
                break;
                
            case "trust":
                handleTrust(player, args);
                break;
                
            case "untrust":
                handleUntrust(player, args);
                break;
                
            case "remove":
                handleRemove(player);
                break;
                
            case "admin":
                handleAdmin(player);
                break;
                
            default:
                player.sendMessage(plugin.colorize("&cUzycie: /claim [buy/info/trust/untrust/remove/admin]"));
                break;
        }
        
        return true;
    }
    
    private void handleBuy(Player player, String[] args) {
        if (!player.hasPermission("labclaims.buy")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUzycie: /claim buy <tier>"));
            player.sendMessage(plugin.colorize("&7Dostepne tiery: 1, 2, 3, 4, 5"));
            return;
        }
        
        int tier;
        try {
            tier = Integer.parseInt(args[1]);
            if (tier < 1 || tier > 5) {
                player.sendMessage(plugin.colorize("&cTier musi byc miedzy 1 a 5!"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.colorize("&cNieprawidlowy tier!"));
            return;
        }
        
        double price = plugin.getClaimManager().getPrice(tier);
        
        if (!plugin.getEconomyManager().hasEnoughMoney(player, price)) {
            String msg = plugin.getMessage("not-enough-money").replace("%price%", String.valueOf(price));
            player.sendMessage(msg);
            return;
        }
        
        plugin.getEconomyManager().withdrawMoney(player, price);
        
        ItemStack claimBlock = plugin.getClaimManager().createClaimBlock(tier);
        player.getInventory().addItem(claimBlock);
        
        player.sendMessage(plugin.colorize("&aKupiles claim block tier " + tier + " za &e" + price + "$"));
        player.sendMessage(plugin.colorize("&7Postaw go na ziemi aby stworzyc dzialke!"));
        
        plugin.playSound(player, "claim-created");
    }
    
    private void handleInfo(Player player) {
        Claim claim = plugin.getClaimData().getClaimAt(player.getLocation());
        
        if (claim == null) {
            player.sendMessage(plugin.getMessage("not-in-claim"));
            return;
        }
        
        player.sendMessage(plugin.colorize("&7&m-------------------"));
        player.sendMessage(plugin.colorize("&d&lInformacje o Dzialce"));
        player.sendMessage(plugin.colorize("&7Nazwa: &f" + claim.getClaimName()));
        player.sendMessage(plugin.colorize("&7Wlasciciel: &f" + claim.getOwnerName()));
        player.sendMessage(plugin.colorize("&7Tier: &f" + claim.getTier()));
        player.sendMessage(plugin.colorize("&7Rozmiar: &f" + claim.getSize() + "x" + claim.getSize()));
        player.sendMessage(plugin.colorize("&7PvP: " + (claim.isPvpEnabled() ? "&aWlaczone" : "&cWylaczone")));
        player.sendMessage(plugin.colorize("&7Spawn mobow: " + (claim.isMobSpawning() ? "&aWlaczony" : "&cWylaczony")));
        player.sendMessage(plugin.colorize("&7Zaufani: &f" + claim.getTrustedPlayers().size()));
        player.sendMessage(plugin.colorize("&7&m-------------------"));
    }
    
    private void handleTrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUzycie: /claim trust <gracz>"));
            return;
        }
        
        Claim claim = plugin.getClaimData().getClaimAt(player.getLocation());
        
        if (claim == null) {
            player.sendMessage(plugin.getMessage("not-in-claim"));
            return;
        }
        
        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }
        
        if (claim.isTrusted(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("already-trusted"));
            return;
        }
        
        claim.addTrustedPlayer(target.getUniqueId());
        plugin.getClaimData().saveClaims();
        
        String msg = plugin.getMessage("player-trusted").replace("%player%", target.getName());
        player.sendMessage(msg);
    }
    
    private void handleUntrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUzycie: /claim untrust <gracz>"));
            return;
        }
        
        Claim claim = plugin.getClaimData().getClaimAt(player.getLocation());
        
        if (claim == null) {
            player.sendMessage(plugin.getMessage("not-in-claim"));
            return;
        }
        
        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }
        
        if (!claim.isTrusted(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("not-trusted"));
            return;
        }
        
        claim.removeTrustedPlayer(target.getUniqueId());
        plugin.getClaimData().saveClaims();
        
        String msg = plugin.getMessage("player-untrusted").replace("%player%", target.getName());
        player.sendMessage(msg);
    }
    
    private void handleRemove(Player player) {
        Claim claim = plugin.getClaimData().getClaimAt(player.getLocation());
        
        if (claim == null) {
            player.sendMessage(plugin.getMessage("not-in-claim"));
            return;
        }
        
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("not-claim-owner"));
            return;
        }
        
        plugin.getClaimManager().removeClaim(claim);
        player.sendMessage(plugin.getMessage("claim-removed"));
    }
    
    private void handleAdmin(Player player) {
        if (!player.hasPermission("labclaims.admin")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return;
        }
        
        AdminPanelGUI gui = new AdminPanelGUI(plugin, player);
        gui.open();
    }
    
    public PlayerData getPlayerData() {
        return playerData;
    }
}
