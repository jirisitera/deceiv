package com.japicraft.dialog;

import com.japicraft.Deceiv;
import com.japicraft.ui.InterfaceUtilities;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.List;

public class ShopDialog {
    public static final String DIALOG_ID = "shop";
    public static final Key DIALOG_KEY = Key.key(Deceiv.PLUGIN_ID, ShopDialog.DIALOG_ID);

    public PrioritizedLifecycleEventHandlerConfiguration<BootstrapContext> get() {
        return RegistryEvents.DIALOG.compose().newHandler(event -> event.registry().register(
                DialogKeys.create(Key.key(Deceiv.PLUGIN_ID, ShopDialog.DIALOG_ID)), builder -> builder
                    .base(DialogBase.builder(Component.text("Item Shop"))
                        .body(List.of(
                                DialogBody.plainMessage(Component.empty()),
                                DialogBody.plainMessage(Component.empty()),
                                DialogBody.plainMessage(Component.empty()),
                                DialogBody.plainMessage(Component.text("Number of Murder Points")),
                                DialogBody.plainMessage(Component.empty())
                            )
                        )
                        .build()
                    )
                    .type(DialogType.multiAction(
                        List.of(ActionButton.builder(Component.text("Purchase Grenade")).action(DialogAction.customClick(Key.key(Deceiv.PLUGIN_ID, "purchase_grenade"), null)).build(),
                            ActionButton.builder(Component.text("Purchase Body Bag")).action(DialogAction.customClick(Key.key(Deceiv.PLUGIN_ID, "purchase_body_bag"), null)).build()
                        ), ActionButton.builder(InterfaceUtilities.CLOSE_BUTTON).build(), 2)
                    )
            )
        );
    }
}
