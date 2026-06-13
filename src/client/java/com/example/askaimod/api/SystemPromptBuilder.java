package com.example.askaimod.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.GameType;

public class SystemPromptBuilder {
    public static String build() {
        Minecraft client = Minecraft.getInstance();
        StringBuilder sb = new StringBuilder();

        sb.append("You are a Minecraft command assistant.\n");
        sb.append("Current context:\n");
        sb.append("- Version: 1.21.11 (Java Edition)\n");

        if (client.gameMode != null) {
            GameType gameType = client.gameMode.getPlayerMode();
            sb.append("- Gamemode: ").append(gameType != null ? gameType.getName() : "unknown").append("\n");
        }

        boolean isSingleplayer = client.isLocalServer();
        sb.append("- World type: ").append(isSingleplayer ? "singleplayer" : "multiplayer").append("\n");

        LocalPlayer player = client.player;
        if (player != null) {
            boolean isOperator = isSingleplayer
                    || player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
            sb.append("- Operator: ").append(isOperator).append("\n");
        }

        sb.append("\nWhen providing commands, always wrap them in a code block starting with /.\n");
        sb.append("Explain what each command does briefly.\n");

        return sb.toString();
    }
}
