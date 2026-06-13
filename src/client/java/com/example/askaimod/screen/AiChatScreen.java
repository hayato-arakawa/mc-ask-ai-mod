package com.example.askaimod.screen;

import com.example.askaimod.api.AiApiClient;
import com.example.askaimod.api.ChatMessage;
import com.example.askaimod.api.SystemPromptBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiChatScreen extends Screen {
    private static final int PADDING = 10;
    private static final int INPUT_HEIGHT = 20;
    private static final int SEND_BUTTON_WIDTH = 50;

    private EditBox inputField;
    private Button sendButton;

    private final List<Component> responseLines = new ArrayList<>();
    private int scrollOffset = 0;

    private boolean isLoading = false;

    public AiChatScreen() {
        super(Component.literal("AI Chat"));
    }

    @Override
    protected void init() {
        int inputY = this.height - INPUT_HEIGHT - PADDING;
        int inputWidth = this.width - PADDING * 2 - SEND_BUTTON_WIDTH - 5;

        inputField = new EditBox(this.font, PADDING, inputY, inputWidth, INPUT_HEIGHT,
                Component.literal("Ask AI..."));
        inputField.setMaxLength(256);
        addRenderableWidget(inputField);

        sendButton = Button.builder(Component.literal("Send"), btn -> sendMessage())
                .bounds(this.width - PADDING - SEND_BUTTON_WIDTH, inputY, SEND_BUTTON_WIDTH, INPUT_HEIGHT)
                .build();
        addRenderableWidget(sendButton);

        setInitialFocus(inputField);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int contentTop = PADDING;
        int contentBottom = this.height - INPUT_HEIGHT - PADDING * 2 - 5;

        graphics.fill(PADDING, contentTop, this.width - PADDING, contentBottom, 0x44000000);

        int y = contentTop + PADDING - scrollOffset;
        for (Component line : responseLines) {
            if (y + 10 > contentTop && y - 10 < contentBottom) {
                graphics.drawString(this.font, line, PADDING + 5, y, 0xFFFFFF, false);
            }
            y += this.font.lineHeight + 2;
        }

        if (isLoading) {
            graphics.drawString(this.font, Component.literal("Thinking..."), PADDING + 5,
                    contentTop + PADDING - scrollOffset + responseLines.size() * (this.font.lineHeight + 2),
                    0x888888, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalHeight = responseLines.size() * (this.font.lineHeight + 2);
        int viewHeight = this.height - INPUT_HEIGHT - PADDING * 2 - 5 - PADDING * 2;
        int maxScroll = Math.max(0, totalHeight - viewHeight);

        scrollOffset = (int) Math.clamp(scrollOffset - scrollY * 10, 0, maxScroll);
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) {
            sendMessage();
            return true;
        }
        return super.keyPressed(event);
    }

    private void sendMessage() {
        String text = inputField.getValue().trim();
        if (text.isEmpty() || isLoading) return;

        responseLines.add(Component.literal("> " + text)
                .setStyle(Style.EMPTY.withColor(0x55FF55)));

        inputField.setValue("");
        isLoading = true;

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", SystemPromptBuilder.build()));
        messages.add(new ChatMessage("user", text));

        AiApiClient.sendMessage(messages).thenAcceptAsync(response -> {
            isLoading = false;
            renderClickableResponse(response);
        }, Minecraft.getInstance());
    }

    private void renderClickableResponse(String response) {
        responseLines.add(Component.literal(""));

        Pattern codeBlockPattern = Pattern.compile("```(?:\\w+)?\\n(.*?)```", Pattern.DOTALL);
        Matcher matcher = codeBlockPattern.matcher(response);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textBefore = response.substring(lastEnd, matcher.start());
                responseLines.add(Component.literal(textBefore));
            }

            String code = matcher.group(1).strip();
            String command = code.startsWith("/") ? code : "/" + code;
            MutableComponent codeComponent = Component.literal(code)
                    .setStyle(Style.EMPTY
                            .withColor(0xAAAAAA)
                            .withClickEvent(new ClickEvent.SuggestCommand(command))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.literal("Click to insert into chat"))));
            responseLines.add(codeComponent);

            lastEnd = matcher.end();
        }

        if (lastEnd < response.length()) {
            String remaining = response.substring(lastEnd);
            String[] lines = remaining.split("\n");
            for (String line : lines) {
                String trimmed = line.strip();
                if (trimmed.startsWith("/")) {
                    MutableComponent cmdComponent = Component.literal(trimmed)
                            .setStyle(Style.EMPTY
                                    .withColor(0xAAAAAA)
                                    .withClickEvent(new ClickEvent.SuggestCommand(trimmed))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.literal("Click to insert into chat"))));
                    responseLines.add(cmdComponent);
                } else {
                    responseLines.add(Component.literal(trimmed));
                }
            }
        }

        int totalHeight = responseLines.size() * (this.font.lineHeight + 2);
        int viewHeight = this.height - INPUT_HEIGHT - PADDING * 2 - 5 - PADDING * 2;
        scrollOffset = Math.max(0, totalHeight - viewHeight);
    }
}
