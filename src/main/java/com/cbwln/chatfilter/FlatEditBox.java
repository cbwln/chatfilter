package com.cbwln.chatfilter;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class FlatEditBox extends EditBox {
    private static final int PADDING = 4;

    private final int x, y, width, height;

    public FlatEditBox(Font font, int x, int y, int width, int height, Component message) {
        int textHeight = font.lineHeight - 1;
        int cursorWidth = font.width("_");

        super(font, x + PADDING, y + (height - textHeight) / 2, width - PADDING * 2 - cursorWidth, textHeight, message);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        setBordered(false);
        setTextColor(Palette.TEXT);
        setMaxLength(Integer.MAX_VALUE);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(x, y, x + width, y + height, Palette.CRUST);

        if (isFocused()) {
            graphics.fill(x, y, x + width, y + 1, Palette.LAVENDER);
            graphics.fill(x, y + height - 1, x + width, y + height, Palette.LAVENDER);
            graphics.fill(x, y, x + 1, y + height, Palette.LAVENDER);
            graphics.fill(x + width - 1, y, x + width, y + height, Palette.LAVENDER);
        }

        super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);

        if (isMouseOver(mouseX, mouseY)) {
            graphics.requestCursor(CursorTypes.IBEAM);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}