package com.cbwln.chatfilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RemoveButton extends Button {
    public RemoveButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;

        graphics.text(font, message, getX() + (getWidth() - (font.width(message) - 1) + 1) / 2, getY() + (getHeight() - (font.lineHeight - 1)) / 2, isHoveredOrFocused() ? Palette.RED : Palette.TEXT);
    }
}
