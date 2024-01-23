package fuzs.easyanvils.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * An extension to {@link EditBox} mainly featuring additional text selection features.
 */
public class AdvancedEditBox extends EditBox {
    protected long lastClickTime;
    protected boolean doubleClick;
    protected int doubleClickHighlightPos;
    protected int doubleClickCursorPos;

    public AdvancedEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public AdvancedEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
        super(font, x, y, width, height, editBox, message);
    }

    @Override
    protected void deleteText(int count) {
        // delete entire words or everything until edit box beginning / end based on held modifier key
        if (Screen.hasControlDown()) {
            if (count < 0) this.deleteChars(-this.cursorPos);
        } else if (Screen.hasAltDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    @Override
    protected int getWordPosition(int numWords, int pos, boolean skipConsecutiveSpaces) {
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
        // break skipping on more than just spaces, from Owo Lib, thanks!
        return charAt == '_' || Character.isAlphabetic(charAt) || Character.isDigit(charAt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isActive() && this.isFocused()) {
            switch (keyCode) {
                case InputConstants.KEY_RIGHT:
                    // when text is selected and the cursor is moved without selecting new text,
                    // make it jump to either the beginning or end of the selection
                    boolean allowedToMoveRight = true;
                    if (!Screen.hasShiftDown() && this.highlightPos != this.cursorPos) {
                        this.setCursorPosition(Math.max(this.getCursorPosition(), this.highlightPos));
                        this.setHighlightPos(this.getCursorPosition());
                        allowedToMoveRight = false;
                    }
                    // select entire words or everything until edit box beginning / end based on held modifier key
                    if (Screen.hasControlDown()) {
                        this.moveCursorToEnd(Screen.hasShiftDown());
                    } else if (Screen.hasAltDown()) {
                        this.moveCursorTo(this.getWordPosition(1), Screen.hasShiftDown());
                    } else if (allowedToMoveRight) {
                        this.moveCursor(1, Screen.hasShiftDown());
                    }

                    return true;
                case InputConstants.KEY_LEFT:
                    // when text is selected and the cursor is moved without selecting new text,
                    // make it jump to either the beginning or end of the selection
                    boolean allowedToMoveLeft = true;
                    if (!Screen.hasShiftDown() && this.highlightPos != this.cursorPos) {
                        this.setCursorPosition(Math.min(this.getCursorPosition(), this.highlightPos));
                        this.setHighlightPos(this.getCursorPosition());
                        allowedToMoveLeft = false;
                    }
                    // select entire words or everything until edit box beginning / end based on held modifier key
                    if (Screen.hasControlDown()) {
                        this.moveCursorToStart(Screen.hasShiftDown());
                    } else if (Screen.hasAltDown()) {
                        this.moveCursorTo(this.getWordPosition(-1), Screen.hasShiftDown());
                    } else if (allowedToMoveLeft) {
                        this.moveCursor(-1, Screen.hasShiftDown());
                    }

                    return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);

        long millis = Util.getMillis();
        boolean tripleClick = this.doubleClick;
        this.doubleClick = millis - this.lastClickTime < 250L;
        if (this.doubleClick) {
            if (tripleClick) {
                // triple click to select all text in the edit box
                this.moveCursorToEnd(false);
                this.setHighlightPos(0);
            } else {
                // double click to select the clicked word
                // highlight positions is right selection boundary, cursor position is left selection boundary
                this.doubleClickHighlightPos = this.getWordPosition(1, this.getCursorPosition(), false);
                this.moveCursorTo(this.doubleClickHighlightPos, false);
                this.doubleClickCursorPos = this.getWordPosition(-1, this.getCursorPosition(), false);
                this.moveCursorTo(this.doubleClickCursorPos, true);
            }
        }

        this.lastClickTime = millis;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            int i = Mth.floor(mouseX) - this.getX();
            if (this.bordered) {
                i -= 4;
            }

            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            int mousePosition = this.font.plainSubstrByWidth(string, i).length() + this.displayPos;

        if (this.doubleClick) {
            // double click drag across text to select individual words
            // dragging outside the edit box will select everything until beginning / end
            if (this.clicked(mouseX, mouseY)) {
                int rightBoundary = this.getWordPosition(1, mousePosition, false);
                this.moveCursorTo(Math.max(this.doubleClickHighlightPos, rightBoundary), false);
                int leftBoundary = this.getWordPosition(-1, mousePosition, false);
                this.moveCursorTo(Math.min(this.doubleClickCursorPos, leftBoundary), true);
            } else {
                if (mousePosition > this.doubleClickHighlightPos) {
                    this.moveCursorToEnd(false);
                } else {
                    this.moveCursorTo(this.doubleClickHighlightPos, false);
                }
                if (mousePosition < this.doubleClickCursorPos) {
                    this.moveCursorToStart(true);
                } else {
                    this.moveCursorTo(this.doubleClickCursorPos, true);
                }
            }
        } else {
            // drag across text to select individual letters
            // dragging outside the edit box will select everything until beginning / end
            if (this.clicked(mouseX, mouseY)) {
                this.moveCursorTo(mousePosition, true);
            } else if (this.highlightPos < mousePosition) {
                this.moveCursorToEnd(true);
            } else {
                this.moveCursorToStart(true);
            }
        }
    }
}
