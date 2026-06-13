package com.example.askaimod.mixin;

import com.example.askaimod.api.AiApiClient;
import com.example.askaimod.api.ChatMessage;
import com.example.askaimod.api.SystemPromptBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(AbstractCommandBlockEditScreen.class)
public abstract class CommandBlockScreenMixin {
    @Shadow
    @Final
    protected EditBox commandEdit;

    @Shadow
    protected int width;

    @Shadow
    protected int height;

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Unique
    private Button askAiButton;

    @Inject(method = "addExtraControls", at = @At("TAIL"))
    private void addAiButton(CallbackInfo ci) {
        askAiButton = Button.builder(Component.literal("AI"), btn -> onAiButtonClick())
                .bounds(this.width / 2 + 100, this.height / 4 + 120, 40, 20)
                .build();
        addRenderableWidget(askAiButton);
    }

    @Unique
    private void onAiButtonClick() {
        askAiButton.active = false;
        askAiButton.setMessage(Component.literal("..."));

        List<ChatMessage> messages = List.of(
                new ChatMessage("system", SystemPromptBuilder.build()),
                new ChatMessage("user", "Help me with this command block. Current command:\n"
                        + commandEdit.getValue()
                        + "\n\nSuggest improvements or explain what this command does.")
        );

        AiApiClient.sendMessage(messages).thenAcceptAsync(response -> {
            askAiButton.active = true;
            askAiButton.setMessage(Component.literal("AI"));

            String extractedCommand = extractCommand(response);
            if (extractedCommand != null) {
                commandEdit.setValue(extractedCommand);
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§7[Ask AI] Command updated. Review before applying."), false);
            }
        }, Minecraft.getInstance());
    }

    @Unique
    private String extractCommand(String response) {
        Pattern codeBlockPattern = Pattern.compile("```(?:\\w+)?\\n(.*?)```", Pattern.DOTALL);
        Matcher matcher = codeBlockPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).strip();
        }

        String[] lines = response.split("\n");
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.startsWith("/")) {
                return trimmed;
            }
        }

        return null;
    }
}
