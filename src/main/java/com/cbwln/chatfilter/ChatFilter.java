package com.cbwln.chatfilter;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatFilter implements ClientModInitializer {
    public static final String MOD_ID = "chatfilter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static FilterConfig config;

    @Override
    public void onInitializeClient() {
        config = FilterConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
            dispatcher.register(ClientCommands.literal("chatfilter").executes(ChatFilter::openScreen));
        });
    }

    private static int openScreen(CommandContext<FabricClientCommandSource> context) {
        Minecraft.getInstance().submit(() -> Minecraft.getInstance().gui.setScreen(new FilterScreen()));
        return 1;
    }

    public static boolean matches(String text) {
        for (FilterConfig.Rule rule : config.rules()) {
            boolean hit = switch (rule.type()) {
                case CONTAINS -> text.contains(rule.text());
                case STARTS_WITH -> text.startsWith(rule.text());
                case REGEX -> rule.matches(text);
            };

            if (hit) return true;
        }
        return false;
    }
}
