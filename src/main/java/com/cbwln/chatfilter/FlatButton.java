package com.cbwln.chatfilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class FlatButton extends Button {
    private final int textColor;

    public FlatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        this(x, y, width, height, message, onPress, Palette.TEXT);
    }

    public FlatButton(int x, int y, int width, int height, Component message, OnPress onPress, int textColor) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.textColor = textColor;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;

        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), !active ? Palette.SURFACE0 : isHoveredOrFocused() ? Palette.SURFACE2 : Palette.SURFACE1);
        graphics.text(font, message, getX() + (getWidth() - (font.width(message) - 1) + 1) / 2, getY() + (getHeight() - (font.lineHeight - 1)) / 2, active ? textColor : Palette.SUBTEXT0);
    }
}
