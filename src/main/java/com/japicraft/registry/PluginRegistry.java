package com.japicraft.registry;

import com.japicraft.Deceiv;
import com.japicraft.command.ApexCommand;
import com.japicraft.container.GameContainer;
import com.japicraft.dialog.DialogManager;
import com.japicraft.event.EventCancelService;
import com.japicraft.hook.Hook;
import com.japicraft.item.ItemManager;
import com.japicraft.packet.PacketService;
import com.japicraft.player.AnimationManager;
import com.japicraft.player.ChatManager;
import com.japicraft.player.InteractManager;
import com.japicraft.ui.NametagManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

public class PluginRegistry {
    public void initialize() {
        ArrayList<LiteralCommandNode<CommandSourceStack>> commands = new ArrayList<>();
        ArrayList<Listener> events = new ArrayList<>();
        // check for plugin hooks
        checkHooksAs(events);
        // load managers
        GameContainer gameContainer = new GameContainer();
        AnimationManager animationManager = new AnimationManager();
        // collect commands
        commands.add(new ApexCommand(gameContainer).build());
        // collect events
        events.add(gameContainer);
        events.add(animationManager);
        events.add(new ChatManager());
        events.add(new EventCancelService());
        events.add(new DialogManager());
        events.add(new InteractManager(gameContainer));
        events.add(new ItemManager(animationManager));
        events.add(new PacketService());
        events.add(new NametagManager(animationManager));
        // register listeners
        registerCommands(commands);
        registerEvents(events);
    }

    public void checkHooksAs(ArrayList<Listener> events) {
        for (Hook hook : Hook.values()) {
            hook.setAvailable(Deceiv.getPlugin().getServer().getPluginManager().isPluginEnabled(hook.getName()));
            if (hook.isAvailable()) {
                try {
                    events.add(hook.getListener().getConstructor(Deceiv.class).newInstance(Deceiv.getPlugin()));
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
                Deceiv.logger().atInfo().log(Component.text("Hooked into " + hook.getName() + " successfully. " + hook.getPurpose() + " are now available."));
            } else {
                Deceiv.logger().atWarn().log(Component.text("Could not hook into " + hook.getName() + ", continuing without it. " + hook.getPurpose() + " will not be available."));
            }
        }
    }

    public void registerCommands(Collection<LiteralCommandNode<CommandSourceStack>> commands) {
        Deceiv.getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, (ReloadableRegistrarEvent<Commands> event) -> {
            for (LiteralCommandNode<CommandSourceStack> command : commands) {
                event.registrar().register(command);
            }
        });
    }

    public void registerEvents(Collection<Listener> events) {
        for (Listener event : events) {
            Deceiv.getPlugin().getServer().getPluginManager().registerEvents(event, Deceiv.getPlugin());
        }
    }
}
