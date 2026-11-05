package com.cbwln.chatfilter;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterScreen extends Screen {
    private static final int WIDTH = 319;
    private static final int HEIGHT = 239;

    private static final int PAD = 4;
    private static final int GAP = 2;
    private static final int ROW = 20;
    private static final int TEXT = 8;
    private static final int BAR = 2;

    private static final int ROWS = (HEIGHT - (PAD * 2) - ROW) / (ROW + GAP);

    private int x, y;
    private int scroll;
    private int textWidth;
    private FilterConfig.Type type = FilterConfig.Type.values()[0];
    private EditBox input;
    private FlatButton addButton;
    private final Button[] buttons = new Button[ROWS];

    public FilterScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        x = (width - WIDTH) / 2;
        y = (height - HEIGHT) / 2;

        int rowY = y + HEIGHT - PAD - ROW;

        int typeWidth = 0;
        for (FilterConfig.Type type : FilterConfig.Type.values()) {
            typeWidth = Math.max(typeWidth, font.width(type.label));
        }
        typeWidth += 12;
        int addWidth = 40;
        int inputWidth = WIDTH - (PAD * 2) - typeWidth - addWidth - (GAP * 2);

        int typeX = x + PAD;
        int inputX = typeX + typeWidth + GAP;
        int addX = inputX + inputWidth + GAP;
        int removeX = x + WIDTH - PAD - BAR - GAP - ROW;

        textWidth = removeX - GAP - (x + PAD);

        addRenderableWidget(new FlatButton(typeX, rowY, typeWidth, ROW, Component.literal(type.label), btn -> {
            FilterConfig.Type[] types = FilterConfig.Type.values();
            type = types[(type.ordinal() + 1) % types.length];
            btn.setMessage(Component.literal(type.label));
            updateAddButton();
        }));

        input = new FlatEditBox(font, inputX, rowY, inputWidth, ROW, Component.empty());
        input.setResponder(value -> updateAddButton());
        addRenderableWidget(input);

        addButton = addRenderableWidget(new FlatButton(addX, rowY, addWidth, ROW, Component.literal("Add"), btn -> addRule()));

        for (int i = 0; i < ROWS; i++) {
            int row = i;
            int removeY = y + PAD + row * (ROW + GAP);

            buttons[row] = addRenderableWidget(new RemoveButton(removeX, removeY, ROW, ROW, Component.literal("X"), btn -> ChatFilter.config.remove(scroll + row)));
        }

        updateAddButton();
    }

    @Override
    public void resize(final int width, final int height) {
        String oldInput = input.getValue();
        int focusedIndex = children().indexOf(getFocused());
        init(width, height);
        input.setValue(oldInput);
        if (focusedIndex >= 0) {
            setFocused(children().get(focusedIndex));
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (input.isFocused() && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            addRule();
            return true;
        }

        return super.keyPressed(event);
    }

    private boolean regexInvalid() {
        if (type != FilterConfig.Type.REGEX) return false;
        try {
            Pattern.compile(input.getValue());
            return false;
        } catch (PatternSyntaxException e) {
            return true;
        }
    }

    private void updateAddButton() {
        addButton.active = !input.getValue().isBlank() && !regexInvalid();
    }

    private void addRule() {
        if (!addButton.active) return;

        ChatFilter.config.add(type, input.getValue());
        scroll = maxScroll();
        input.setValue("");
    }

    private int maxScroll() {
        return Math.max(0, ChatFilter.config.rules().size() - ROWS);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        List<FilterConfig.Rule> rules = ChatFilter.config.rules();
        int max = maxScroll();
        scroll = Math.min(scroll, max);

        int end = Math.min(rules.size(), scroll + ROWS);

        for (int i = 0; i < ROWS; i++) {
            buttons[i].visible = scroll + i < end;
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int rowY = y + PAD;
        for (int i = scroll; i < end; i++) {
            FilterConfig.Rule rule = rules.get(i);

            String prefix = rule.type().label + " ";
            int prefixWidth = font.width(prefix);

            String value = rule.text();
            int valueWidth = textWidth - prefixWidth;
            if (font.width(value) > valueWidth) {
                value = font.plainSubstrByWidth(value, valueWidth - font.width("...")) + "...";
            }

            int textY = rowY + (ROW - TEXT) / 2;

            graphics.text(font, prefix, x + PAD, textY, Palette.SUBTEXT0);
            graphics.text(font, value, x + PAD + prefixWidth, textY, Palette.TEXT);

            rowY += ROW + GAP;
        }

        if (max > 0) {
            int trackHeight = ROWS * (ROW + GAP) - GAP;
            int thumbHeight = Math.max(8, trackHeight * ROWS / rules.size());
            int thumbY = y + PAD + (trackHeight - thumbHeight) * scroll / max;
            int barX = x + WIDTH - PAD - BAR;

            graphics.fill(barX, thumbY, barX + BAR, thumbY + thumbHeight, Palette.OVERLAY2);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, Palette.OVERLAY0);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, Palette.BASE);
    }
}