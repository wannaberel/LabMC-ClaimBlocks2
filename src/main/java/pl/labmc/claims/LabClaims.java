package pl.labmc.claims;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.labmc.claims.commands.ClaimCommand;
import pl.labmc.claims.data.ClaimData;
import pl.labmc.claims.listeners.ChatListener;
import pl.labmc.claims.listeners.ClaimBlockListener;
import pl.labmc.claims.listeners.CraftingListener;
import pl.labmc.claims.listeners.ProtectionListener;
import pl.labmc.claims.managers.ClaimManager;
import pl.labmc.claims.managers.EconomyManager;
import pl.labmc.claims.managers.HologramManager;
import pl.labmc.claims.tasks.ParticleTask;

public class LabClaims extends JavaPlugin {
    
    private static LabClaims instance;
    private ClaimManager claimManager;
    private EconomyManager economyManager;
    private HologramManager hologramManager;
    private ClaimData claimData;
    private Economy economy;
    private ParticleTask particleTask;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        if (!setupEconomy()) {
            getLogger().severe("Vault nie znalezione! Plugin wymaga Vault i ekonomii!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        this.claimData = new ClaimData(this);
        this.claimManager = new ClaimManager(this);
        this.economyManager = new EconomyManager(this);
        this.hologramManager = new HologramManager(this);
        
        getCommand("claim").setExecutor(new ClaimCommand(this));
        
        getServer().getPluginManager().registerEvents(new ClaimBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // NOWE: Rejestracja craftingu
        CraftingListener craftingListener = new CraftingListener(this);
        craftingListener.registerRecipes();
        
        claimData.loadClaims();
        hologramManager.loadAllHolograms();
        
        // NOWE: Uruchom task dla particles (co 2 sekundy = 40 ticków)
        this.particleTask = new ParticleTask(this);
        this.particleTask.runTaskTimer(this, 40L, 40L);
        
        getLogger().info("================================");
        getLogger().info("LabClaims plugin wlaczony!");
        getLogger().info("System dzialek aktywny dla LabMC.pl");
        getLogger().info("Crafting ClaimBlock Tier 1 dodany!");
        getLogger().info("Particles granic dzialek aktywne!");
        getLogger().info("================================");
    }
    
    @Override
    public void onDisable() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        
        if (claimData != null) {
            claimData.saveClaims();
        }
        
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        
        getLogger().info("LabClaims plugin zostal wylaczony!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public static LabClaims getInstance() {
        return instance;
    }
    
    public ClaimManager getClaimManager() {
        return claimManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    public ClaimData getClaimData() {
        return claimData;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "&d[&5LabClaims&d] &f");
        String message = getConfig().getString("messages." + path, "&cBrak wiadomosci: " + path);
        return colorize(prefix + message);
    }
    
    public String colorize(String text) {
        return text.replace("&", "§");
    }
    
    public void playSound(org.bukkit.entity.Player player, String soundPath) {
        if (!getConfig().contains("sounds." + soundPath)) return;
        
        try {
            String soundName = getConfig().getString("sounds." + soundPath + ".sound");
            float volume = (float) getConfig().getDouble("sounds." + soundPath + ".volume", 1.0);
            float pitch = (float) getConfig().getDouble("sounds." + soundPath + ".pitch", 1.0);
            
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            // Ignore
        }
    }
}
