package com.japicraft.command;

import com.japicraft.Deceiv;
import com.japicraft.manager.GameManager;
import com.japicraft.manager.KnifeManager;
import com.japicraft.manager.RevolverManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ApexCommand {
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(Deceiv.PLUGIN_ID)
            .then(Commands.literal("items")
                .executes(ApexCommand::items)
            )
            .then(Commands.literal("start")
                .executes(ApexCommand::start)
            );
        return command.build();
    }

    private static int items(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getExecutor();
        if (player == null) {
            return 0;
        }
        // give items
        KnifeManager.give(player);
        RevolverManager.give(player);
        player.sendMessage(Component.text("Gave ")
            .append(player.name())
            .append(Component.text(" all special " + Deceiv.PLUGIN_ID + " items."))
        );
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> ctx) {
        GameManager.startRound();
        return 1;
    }
}
