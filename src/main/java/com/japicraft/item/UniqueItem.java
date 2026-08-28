package com.japicraft.item;

import com.japicraft.game.Role;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum UniqueItem {
    REVOLVER("revolver", Role.DETECTIVE),
    DAGGER("dagger", Role.MURDERER);
    private static final Map<String, UniqueItem> MODEL_CACHE = new HashMap<>();

    static {
        for (UniqueItem unique : values()) {
            MODEL_CACHE.put(unique.model, unique);
        }
    }

    private final String model;
    private final Role role;
    private ItemAbility ability;

    UniqueItem(@NotNull String model, @NotNull Role role) {
        this.model = model;
        this.role = role;
    }

    @Nullable
    public static UniqueItem fromItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        Key model = item.getData(DataComponentTypes.ITEM_MODEL);
        if (model == null) {
            return null;
        }
        return MODEL_CACHE.get(model.value());
    }

    @Subst("default")
    public String getModel() {
        return model;
    }

    public Role getRole() {
        return role;
    }

    public ItemAbility getAbility() {
        return ability;
    }

    public void setAbility(ItemAbility ability) {
        this.ability = ability;
    }

    public boolean compare(ItemStack item) {
        Key itemModel = item.getData(DataComponentTypes.ITEM_MODEL);
        return itemModel != null && itemModel.value().equals(model);
    }
}
