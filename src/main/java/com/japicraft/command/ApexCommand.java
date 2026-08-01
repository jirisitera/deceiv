package com.japicraft.command;

import com.japicraft.Deceiv;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ApexCommand {
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(Deceiv.PLUGIN_ID)
            .then(Commands.literal("items")
                .executes(ApexCommand::items)
            );
        return command.build();
    }

    private static int items(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        if (player == null) {
            return 0;
        }
        // large knife item
        ItemStack knifeLarge = ItemStack.of(Material.ECHO_SHARD);
        knifeLarge.setData(DataComponentTypes.ITEM_NAME, Component.text("Large Knife"));
        knifeLarge.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, "knife_large"));
        // small knife item
        ItemStack knifeSmall = ItemStack.of(Material.ECHO_SHARD);
        knifeSmall.setData(DataComponentTypes.ITEM_NAME, Component.text("Small Knife"));
        knifeSmall.setData(DataComponentTypes.ITEM_MODEL, Key.key(Deceiv.PLUGIN_ID, "knife_small"));
        // give items
        player.give(knifeLarge, knifeSmall);
        player.sendMessage(Component.text("Gave ")
            .append(player.name())
            .append(Component.text(" all special " + Deceiv.PLUGIN_ID + " items."))
        );
        return 1;
    }
}
