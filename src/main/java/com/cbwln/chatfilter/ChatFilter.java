package com.cbwln.chatfilter;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
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
            CommandNode<FabricClientCommandSource> chatfilter = dispatcher.register(ClientCommands.literal("chatfilter").executes(ChatFilter::openScreen));
            dispatcher.register(ClientCommands.literal("cf").executes(ChatFilter::openScreen).redirect(chatfilter));
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
