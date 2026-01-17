public void saveClaims() {
    YamlConfiguration config = new YamlConfiguration();
    
    for (Map.Entry<UUID, Claim> entry : claims.entrySet()) {
        String key = "claims." + entry.getKey().toString();
        Claim claim = entry.getValue();
        
        config.set(key + ".owner", claim.getOwnerId().toString());
        config.set(key + ".owner-name", claim.getOwnerName());
        config.set(key + ".world", claim.getBlockLocation().getWorld().getName());
        config.set(key + ".x", claim.getBlockLocation().getBlockX());
        config.set(key + ".y", claim.getBlockLocation().getBlockY());
        config.set(key + ".z", claim.getBlockLocation().getBlockZ());
        config.set(key + ".tier", claim.getTier());
        config.set(key + ".name", claim.getClaimName());
        config.set(key + ".pvp", claim.isPvpEnabled());
        config.set(key + ".mob-spawning", claim.isMobSpawning());
        config.set(key + ".particles", claim.isShowParticles());
        
        // NOWE UPRAWNIENIA
        config.set(key + ".tnt-enabled", claim.isTntEnabled());
        config.set(key + ".chests-public", claim.isChestsPublic());
        config.set(key + ".villagers-public", claim.isVillagersPublic());
        config.set(key + ".entry-allowed", claim.isEntryAllowed());
        
        List<String> trustedList = new ArrayList<>();
        for (UUID trustedId : claim.getTrustedPlayers()) {
            trustedList.add(trustedId.toString());
        }
        config.set(key + ".trusted", trustedList);
    }
    
    try {
        config.save(dataFile);
    } catch (IOException e) {
        plugin.getLogger().severe("Nie mozna zapisac dzialek!");
        e.printStackTrace();
    }
}
