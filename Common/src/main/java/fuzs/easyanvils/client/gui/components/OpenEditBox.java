package fuzs.easyanvils.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * a copy of {@link EditBox} that supports formatting codes
 * <p>extend EditBox to allow for better compatibility
 */
public class OpenEditBox extends EditBox {
    private final Font font;
    /**
     * Has the current text being edited on the textbox.
     */
    String value = "";
    private int maxLength = 32;
    private int frame;
    private boolean bordered = true;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isFocused, keyTyped will process the keys.
     */
    private boolean isEditable = true;
    private boolean shiftPressed;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    int displayPos;
    int cursorPos;
    /**
     * other selection position, maybe the same as the cursor
     */
    int highlightPos;
    private int textColor = 14737632;
    private int textColorUneditable = 7368816;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    /**
     * Called to check if the text is valid
     */
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (String formatterValue, Integer position) -> {
        List<FormattedCharSequence> list = Lists.newArrayList();
        FormattedStringDecomposer.LengthLimitedCharSink sink = new FormattedStringDecomposer.LengthLimitedCharSink(formatterValue.length(), position);
        // format the whole value, we need the formatting to apply correctly and not get interrupted by the cursor being placed in between a formatting code
        FormattedStringDecomposer.iterateFormatted(this.value, Style.EMPTY, (index, style, j) -> {
            if (sink.accept(index, style, j)) {
                list.add(formattedCharSink -> formattedCharSink.accept(index, style, j));
            }
            return true;
        });
        return FormattedCharSequence.composite(list);
    };
    private long lastClickTime;
    private int lastClickButton;
    private boolean doubleClick;
    private int doubleClickHighlightPos;
    private int doubleClickCursorPos;
    public TypeActionManager typeActionManager = new TypeActionManager();

    public OpenEditBox(Font font, int i, int j, int k, int l, Component component) {
        this(font, i, j, k, l, null, component);
    }

    public OpenEditBox(Font font, int i, int j, int k, int l, @Nullable OpenEditBox editBox, Component component) {
        super(font, i, j, k, l, editBox, component);
        this.font = font;
        if (editBox != null) {
            this.setValue(editBox.getValue());
        }

    }

    @Override
    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    @Override
    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> textFormatter) {
        this.formatter = textFormatter;
    }

    /**
     * Increments the cursor counter
     */
    @Override
    public void tick() {
        ++this.frame;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    /**
     * Sets the text of the textbox, and moves the cursor to the end.
     */
    @Override
    public void setValue(String text) {
        if (this.filter.test(text)) {
            int aboveMaxLength = ComponentDecomposer.getStringLength(text) - this.maxLength;
            if (aboveMaxLength > 0) {
                this.value = ComponentDecomposer.removeLast(text, aboveMaxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
        }
    }

    /**
     * Returns the contents of the textbox
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    @Override
    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public boolean hasHighlighted() {
        return this.highlightPos != this.cursorPos;
    }

    @Override
    public void setFilter(Predicate<String> validator) {
        this.filter = validator;
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
     */
    @Override
    public void insertText(String textToWrite) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        String string = FormattedStringDecomposer.filterText(textToWrite);
        String string3 = new StringBuilder(this.value).replace(i, j, string).toString();
        int stringLength = ComponentDecomposer.getStringLength(string3) - this.maxLength;
        if (stringLength > 0) {
            string = ComponentDecomposer.removeLast(textToWrite, stringLength);
        }
        String string2 = new StringBuilder(this.value).replace(i, j, string).toString();
        if (this.filter.test(string2)) {
            this.value = string2;
            int l = string.length();
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String newText) {
        this.typeActionManager.trySave(this);
        if (this.responder != null) {
            this.responder.accept(newText);
        }
    }

    private void deleteText(int count) {
        if (Screen.hasControlDown()) {
            if (count < 0) this.deleteChars(-this.cursorPos);
        } else if (Screen.hasAltDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    @Override
    public void deleteWords(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(num) - this.cursorPos);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    @Override
    public void deleteChars(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(num);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String string = new StringBuilder(this.value).delete(j, k).toString();
                    if (this.filter.test(string)) {
                        this.value = string;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    @Override
    public int getWordPosition(int numWords) {
        return this.getWordPosition(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private int getWordPosition(int n, int pos) {
        return this.getWordPosition(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private int getWordPosition(int numWords, int pos, boolean skipConsecutiveSpaces) {
        int i = pos;
        boolean backwards = numWords < 0;
        int skippedWords = Math.abs(numWords);

        for (int k = 0; k < skippedWords; ++k) {
            if (!backwards) {
                int l = this.value.length();
                while (skipConsecutiveSpaces && i == pos && i < l && !isWordChar(this.value.charAt(i))) {
                    ++i;
                    pos++;
                }

                while (i < l && isWordChar(this.value.charAt(i))) {
                    ++i;
                }
            } else {
                while (skipConsecutiveSpaces && i == pos && i > 0 && !isWordChar(this.value.charAt(i - 1))) {
                    --i;
                    pos--;
                }

                while (i > 0 && isWordChar(this.value.charAt(i - 1))) {
                    --i;
                }
            }
        }

        return i;
    }

    private static boolean isWordChar(char charAt) {
        // from Owo Lib, thanks!
        return charAt == '_' || Character.isAlphabetic(charAt) || Character.isDigit(charAt);
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    @Override
    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    /**
     * Sets the current position of the cursor.
     */
    @Override
    public void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    @Override
    public void setCursorPosition(int pos) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length());
        this.scrollTo(this.cursorPos);
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    @Override
    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    @Override
    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public static boolean isUndo(int keyCode) {
        return keyCode == InputConstants.KEY_Y && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isRedo(int keyCode) {
        return keyCode == InputConstants.KEY_Y && Screen.hasControlDown() && Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else if (isUndo(keyCode)) {
                this.typeActionManager.undo(this);

                return true;
            } else if (isRedo(keyCode)) {
                this.typeActionManager.redo(this);

                return true;
            } else {
                boolean canMove;
                switch (keyCode) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        canMove = true;
                        if (!this.shiftPressed && this.hasHighlighted()) {
                            this.setCursorPosition(Math.max(this.getCursorPosition(), this.highlightPos));
                            this.setHighlightPos(this.getCursorPosition());
                            canMove = false;
                        }

                        if (Screen.hasControlDown()) {
                            this.moveCursorToEnd();
                        } else if (Screen.hasAltDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else if (canMove) {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        canMove = true;
                        if (!this.shiftPressed && this.hasHighlighted()) {
                            this.setCursorPosition(Math.min(this.getCursorPosition(), this.highlightPos));
                            this.setHighlightPos(this.getCursorPosition());
                            canMove = false;
                        }

                        if (Screen.hasControlDown()) {
                            this.moveCursorToStart();
                        } else if (Screen.hasAltDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else if (canMove) {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    @Override
    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (FormattedStringDecomposer.isAllowedChatCharacter(codePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(codePoint));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int i = Mth.floor(mouseX) - this.getX();
        if (this.bordered) {
            i -= 4;
        }

        this.shiftPressed = Screen.hasShiftDown();
        String string = FormattedStringDecomposer.plainHeadByWidth(this.font, this.value, this.displayPos, this.getInnerWidth(), Style.EMPTY);
        this.moveCursorTo(FormattedStringDecomposer.plainHeadByWidth(this.font, string, 0, i, Style.EMPTY).length() + this.displayPos);

        long millis = Util.getMillis();
        boolean tripleClick = this.doubleClick;
        this.doubleClick = millis - this.lastClickTime < 250L;
        if (this.doubleClick) {
            if (tripleClick) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
            } else {
                this.shiftPressed = false;
                this.doubleClickHighlightPos = this.getWordPosition(1, this.getCursorPosition(), false);
                this.moveCursorTo(this.doubleClickHighlightPos);
                this.shiftPressed = true;
                this.doubleClickCursorPos = this.getWordPosition(-1, this.getCursorPosition(), false);
                this.moveCursorTo(this.doubleClickCursorPos);
                this.shiftPressed = Screen.hasShiftDown();
            }
        }

        this.lastClickTime = millis;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                if (this.clicked(mouseX, mouseY)) {
                    int i = Mth.floor(mouseX) - this.getX();
                    if (this.bordered) {
                        i -= 4;
                    }

                    if (this.doubleClick) {
                        String string = FormattedStringDecomposer.plainHeadByWidth(this.font, this.value, this.displayPos, this.getInnerWidth(), Style.EMPTY);
                        int mousePosition = FormattedStringDecomposer.plainHeadByWidth(this.font, string, 0, i, Style.EMPTY).length() + this.displayPos;
                        this.shiftPressed = false;
                        this.moveCursorTo(Math.max(this.doubleClickHighlightPos, this.getWordPosition(1, mousePosition, false)));
                        this.shiftPressed = true;
                        this.moveCursorTo(Math.min(this.doubleClickCursorPos, this.getWordPosition(-1, mousePosition, false)));
                        this.shiftPressed = Screen.hasShiftDown();
                    } else {
                        this.shiftPressed = true;
                        String string = FormattedStringDecomposer.plainHeadByWidth(this.font, this.value, this.displayPos, this.getInnerWidth(), Style.EMPTY);
                        this.moveCursorTo(FormattedStringDecomposer.plainHeadByWidth(this.font, string, 0, i, Style.EMPTY).length() + this.displayPos);
                        this.shiftPressed = Screen.hasShiftDown();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = this.isFocused() ? -1 : -6250336;
                guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, i);
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
            }

            int i = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String string = FormattedStringDecomposer.plainHeadByWidth(this.font, this.value, this.displayPos, this.getInnerWidth(), Style.EMPTY);
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && this.frame / 6 % 2 == 0 && bl;
            int l = this.bordered ? this.getX() + 4 : this.getX();
            int m = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int n = l;
            if (k > string.length()) {
                k = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                n = guiGraphics.drawString(this.font, this.formatter.apply(string2, this.displayPos), l, m, i);
            }

            boolean bl3 = this.cursorPos < this.value.length() || ComponentDecomposer.getStringLength(this.value) >= this.getMaxLength();
            int o = n;
            if (!bl) {
                o = j > 0 ? l + this.width : l;
            } else if (!string.isEmpty()) {
                o = n - 1;
                --n;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                guiGraphics.drawString(this.font, this.formatter.apply(string.substring(j), this.cursorPos), n, m, i);
            }

            if (!bl3 && this.suggestion != null) {
                guiGraphics.drawString(this.font, this.suggestion, (o - 1), m, -8355712);
            }

            if (bl2 && k == j) {
                if (!string.isEmpty()) {
                    guiGraphics.fill(RenderType.guiOverlay(), o, m - 1, o + 1, m + 1 + 9, -3092272);
                } else {
                    guiGraphics.drawString(this.font, "_", o, m, i);
                }
            }

            if (k != j) {
                int p = l + FormattedStringDecomposer.stringWidth(this.font, this.value.substring(0, this.highlightPos), this.displayPos);
                this.renderHighlight(guiGraphics, o, m - 1, p - 1, m + 1 + 9);
            }
        }
    }

    /**
     * Draws the blue selection box.
     */
    private void renderHighlight(GuiGraphics guiGraphics, int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        if (endX > this.getX() + this.width) {
            endX = this.getX() + this.width;
        }

        if (startX > this.getX() + this.width) {
            startX = this.getX() + this.width;
        }

        guiGraphics.fill(RenderType.guiTextHighlight(), startX, startY, endX, endY, -16776961);
    }

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the current text will be trimmed.
     */
    @Override
    public void setMaxLength(int length) {
        this.maxLength = length;
        if (this.value.length() > length) {
            this.value = this.value.substring(0, length);
            this.onValueChange(this.value);
        }

    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    private int getMaxLength() {
        return this.maxLength;
    }

    /**
     * returns the current position of the cursor
     */
    @Override
    public int getCursorPosition() {
        return this.cursorPos;
    }

    /**
     * Gets whether the background and outline of this text box should be drawn (true if so).
     */
    private boolean isBordered() {
        return this.bordered;
    }

    /**
     * Sets whether or not the background and outline of this text box should be drawn.
     */
    @Override
    public void setBordered(boolean enableBackgroundDrawing) {
        this.bordered = enableBackgroundDrawing;
    }

    /**
     * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabled.
     */
    @Override
    public void setTextColor(int color) {
        this.textColor = color;
    }

    /**
     * Sets the color to use for text in this text box when this text box is disabled.
     */
    @Override
    public void setTextColorUneditable(int color) {
        this.textColorUneditable = color;
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return this.visible && this.isEditable ? super.nextFocusPath(focusNavigationEvent) : null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
    }

    @Override
    public void setFocused(boolean bl) {
        if (this.canLoseFocus || bl) {
            super.setFocused(bl);
            if (bl) {
                this.frame = 0;
            }

        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be typed in.
     */
    @Override
    public void setEditable(boolean enabled) {
        this.isEditable = enabled;
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    @Override
    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    @Override
    public void setHighlightPos(int position) {
        this.highlightPos = Mth.clamp(position, 0, this.value.length());
        this.scrollTo(this.highlightPos);
    }

    protected void scrollTo(int position) {
        if (this.font != null) {
            int i = this.value.length();
            if (this.displayPos > i) {
                this.displayPos = i;
            }

            int j = this.getInnerWidth();
            String string = FormattedStringDecomposer.plainHeadByWidth(this.font, this.value, this.displayPos, j, Style.EMPTY);
            int k = string.length() + this.displayPos;
            if (position == this.displayPos) {
                this.displayPos -= FormattedStringDecomposer.plainTailByWidth(this.font, this.value, j, Style.EMPTY).length();
            }

            if (position > k) {
                this.displayPos += position - k;
            } else if (position <= this.displayPos) {
                this.displayPos -= this.displayPos - position;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, i);
        }
    }

    /**
     * Sets whether this text box loses focus when something other than it is clicked.
     */
    @Override
    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    /**
     * returns true if this textbox is visible
     */
    @Override
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets whether this text box is visible
     */
    @Override
    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.getX() : this.getX() + FormattedStringDecomposer.stringWidth(this.font, this.value.substring(0, charNum), 0);
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
