package de.damcraft.serverseeker.mixin;

import de.damcraft.serverseeker.gui.GetInfoScreen;
import de.damcraft.serverseeker.gui.ServerSeekerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Unique
    private Button getInfoButton;

    protected JoinMultiplayerScreenMixin() {
        super(Component.empty());
    }

    @Inject(method = "init()V", at = @At("HEAD"))
    private void onInit(CallbackInfo info) {
        // Add a button which sets the current screen to the ServerSeekerScreen
        this.addRenderableWidget(
            Button.builder(
                Component.literal("ServerSeeker"),
                onPress -> {
                    if (this.minecraft == null) return;
                    this.minecraft.setScreenAndShow(new ServerSeekerScreen((JoinMultiplayerScreen) (Object) this));
                }
            )
                .pos(150, 3)
                .width(80)
                .build()
        );

        // Add a button to get the info of the selected server
        this.getInfoButton = this.addRenderableWidget(
            Button.builder(
                Component.literal("Get players"),
                onPress -> {
                    if (this.minecraft == null) return;
                    ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
                    if (entry != null) {
                        if (this.minecraft == null) return;
                        this.minecraft.setScreenAndShow(new GetInfoScreen((JoinMultiplayerScreen) (Object) this, entry));
                    }
                }
            )
                .pos(235, 3)
                .width(80)
                .build()
        );
    }

    @Inject(method = "onSelectedChange()V", at = @At("TAIL"))
    private void onSelectedChange(CallbackInfo info) {
        // Enable the button if a server is selected
        if (this.getInfoButton == null) return;
        ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
        this.getInfoButton.active = entry instanceof ServerSelectionList.OnlineServerEntry;
    }
}
