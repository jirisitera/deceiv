package com.japicraft.ui;

import com.japicraft.Deceiv;
import com.japicraft.game.Role;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.List;

public class InformationInterface {
    public static final String DIALOG_ID = "info";
    public static final Key DIALOG_KEY = Key.key(Deceiv.PLUGIN_ID, InformationInterface.DIALOG_ID);
    private static final int MAX_WIDTH = 500;

    public PrioritizedLifecycleEventHandlerConfiguration<BootstrapContext> get() {
        return RegistryEvents.DIALOG.compose().newHandler(event -> event.registry().register(
                DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, InformationInterface.DIALOG_ID)), builder -> builder
                    .base(DialogBase.builder(Component.text("Role Cheatsheet").color(Role.MURDERER.getColor()))
                        .externalTitle(Component.text("Role Cheatsheet").color(Role.MURDERER.getColor()))
                        .body(List.of(DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.text("Innocent")),
                            DialogBody.plainMessage(Component.text("Your only goal is to survive. Good luck."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.text("All you have is your wits. Well, that, and an occasional revolver with one bullet."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.text("Murderer")),
                            DialogBody.plainMessage(Component.text("Your goal is to kill everyone, except other Murderers."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.text("You have a throwable dagger, which is also pretty good at stabbing nearby players."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.text("Detective")),
                            DialogBody.plainMessage(Component.text("Your goal is to survive or to kill all Murderers."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.text("You have a trustworthy revolver with unlimited ammo and a considerable reload time."), MAX_WIDTH),
                            DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.empty()),
                            DialogBody.plainMessage(Component.empty())))
                        .build()
                    )
                    .type(DialogType.notice(ActionButton.builder(InterfaceUtilities.CLOSE_BUTTON).build()))
            )
        );
    }
}
