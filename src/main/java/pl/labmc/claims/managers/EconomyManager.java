package pl.labmc.claims.managers;

import org.bukkit.entity.Player;
import pl.labmc.claims.LabClaims;

public class EconomyManager {
    
    private final LabClaims plugin;
    
    public EconomyManager(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    public boolean hasEnoughMoney(Player player, double amount) {
        return plugin.getEconomy().has(player, amount);
    }
    
    public void withdrawMoney(Player player, double amount) {
        plugin.getEconomy().withdrawPlayer(player, amount);
    }
    
    public void depositMoney(Player player, double amount) {
        plugin.getEconomy().depositPlayer(player, amount);
    }
    
    public double getBalance(Player player) {
        return plugin.getEconomy().getBalance(player);
    }
    
    public String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}
