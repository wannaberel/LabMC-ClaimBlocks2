package pl.labmc.claims.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import pl.labmc.claims.LabClaims;

public class CraftingListener {
    
    private final LabClaims plugin;
    
    public CraftingListener(LabClaims plugin) {
        this.plugin = plugin;
    }
    
    public void registerRecipes() {
        // Crafting dla ClaimBlock Tier 1
        ItemStack claimBlock = plugin.getClaimManager().createClaimBlock(1);
        
        NamespacedKey key = new NamespacedKey(plugin, "claim_block_tier_1");
        ShapedRecipe recipe = new ShapedRecipe(key, claimBlock);
        
        // Wzór craftingu:
        // D E D
        // E G E
        // D E D
        // D = Diamond, E = Emerald, G = Gold Block
        
        recipe.shape("DED", "EGE", "DED");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        
        try {
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Zarejestrowano crafting dla ClaimBlock Tier 1!");
        } catch (Exception e) {
            plugin.getLogger().warning("Nie udalo sie zarejestrowac craftingu: " + e.getMessage());
        }
    }
}
