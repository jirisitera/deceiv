package com.japicraft.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ItemUtilities {
    public static final Key EMPTY_KEY = Key.key("intentionally_empty");

    public static void applyItemCooldown(Player player, ItemStack item, int cooldown) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        player.setCooldown(item, cooldown);
    }

    public static boolean isHoldingItem(PlayerInventory inventory, String modelName) {
        return isItemWithModel(inventory.getItemInMainHand(), modelName) || isItemWithModel(inventory.getItemInOffHand(), modelName);
    }

    public static boolean isItemWithModel(ItemStack item, String modelName) {
        Key model = item.getData(DataComponentTypes.ITEM_MODEL);
        return model != null && model.value().equals(modelName);
    }
}
