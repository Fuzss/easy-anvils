package fuzs.easyanvils.client.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import fuzs.easyanvils.client.util.FormattedStringSplitter;
import fuzs.easyanvils.client.util.LengthLimitedCharSink;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.easyanvils.util.FormattedStringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension to {@link EditBox} that supports {@link net.minecraft.ChatFormatting} by allowing '§' to be used.
 */
public class FormattableEditBox extends EditBox {

    public FormattableEditBox(Font font, int x, int y, int width, int height, Component message) {
        this(font, x, y, width, height, null, message);
    }

    public FormattableEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
        super(font, x, y, width, height, editBox, message);
        // custom formatter for applying formatting codes directly to the text preview
        this.addFormatter((String displayText, int displayPos) -> {
            List<FormattedCharSequence> list = new ArrayList<>();
            FormattedCharSink sink = new LengthLimitedCharSink(displayText.length(), displayPos);
            // We apply the format to the whole value.
            // We need the formatting to apply correctly and not get interrupted by the cursor being placed in between a formatting code.
            FormattedStringDecomposer.iterateFormatted(this.value,
                    Style.EMPTY,
                    (int position, Style style, int codePoint) -> {
                        if (sink.accept(position, style, codePoint)) {
                            list.add((FormattedCharSink formattedCharSink) -> formattedCharSink.accept(position,
                                    style,
                                    codePoint));
                        }

                        return true;
                    });
            return FormattedCharSequence.composite(list);
        });
    }

    @Override
    public void setValue(String text) {
        // Custom text length handling so we ignore formatting codes.
        if (FormattedStringUtil.stringLength(text) > this.maxLength) {
            this.value = FormattedStringUtil.substring(text, 0, this.maxLength);
        } else {
            this.value = text;
        }

        this.moveCursorToEnd(false);
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(text);
    }

    @Override
    public void insertText(String textToWrite) {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        String string = FormattedStringUtil.filterText(textToWrite);
        // Delete the selected character range from the current value.
        StringBuilder stringBuilder = new StringBuilder(this.value).replace(start, end, "");
        String newValue = stringBuilder.toString();
        // Insert new characters one by one, checking after each if the value is still below the max allowed length.
        int insertionLength = 0;
        for (; insertionLength < string.length(); insertionLength++) {
            char character = string.charAt(insertionLength);
            // Special handling for surrogate pairs as done in the vanilla super method.
            if (Character.isHighSurrogate(character)) {
                if (insertionLength + 1 < string.length()) {
                    stringBuilder.insert(start + insertionLength, character);
                    insertionLength++;
                    stringBuilder.insert(start + insertionLength, string.charAt(insertionLength));
                } else {
                    break;
                }
            } else {
                stringBuilder.insert(start + insertionLength, character);
            }

            if (FormattedStringUtil.stringLength(stringBuilder.toString()) <= this.maxLength) {
                newValue = stringBuilder.toString();
            } else {
                break;
            }
        }

        this.value = newValue;
        this.setCursorPosition(start + insertionLength);
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(this.value);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (FormattedStringUtil.isAllowedChatCharacter(characterEvent)) {
            // Custom text length handling so we ignore formatting codes.
            if (this.isEditable) {
                this.insertText(characterEvent.codepointAsString());
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int findClickedPositionInText(MouseButtonEvent mouseButtonEvent) {
        int i = Math.min(Mth.floor(mouseButtonEvent.x()) - this.textX, this.getInnerWidth());
        String string = this.value;
        return this.displayPos + FormattedStringSplitter.plainSubstrByWidth(this.font.getSplitter(),
                string,
                i,
                this.displayPos).length();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                Identifier identifier = SPRITES.get(this.isActive(), this.isFocused());
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        identifier,
                        this.getX(),
                        this.getY(),
                        this.getWidth(),
                        this.getHeight());
            }

            int i = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            String string = FormattedStringSplitter.plainSubstrByWidth(this.font.getSplitter(),
                    this.value,
                    this.getInnerWidth(),
                    this.displayPos);
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && bl;
            int k = this.textX;
            int l = Mth.clamp(this.highlightPos - this.displayPos, 0, string.length());
            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                FormattedCharSequence formattedCharSequence = this.applyFormat(string2, this.displayPos);
                guiGraphics.text(this.font, formattedCharSequence, k, this.textY, i, this.textShadow);
                k += this.font.width(formattedCharSequence) + 1;
            }

            boolean bl3 = this.cursorPos < this.value.length()
                    || FormattedStringUtil.stringLength(this.value) >= this.getMaxLength();
            int m = k;
            if (!bl) {
                m = j > 0 ? this.textX + this.width : this.textX;
            } else if (bl3) {
                m = k - 1;
                k--;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                guiGraphics.text(this.font,
                        this.applyFormat(string.substring(j), this.cursorPos),
                        k,
                        this.textY,
                        i,
                        this.textShadow);
            }

            if (this.hint != null && string.isEmpty() && !this.isFocused()) {
                guiGraphics.text(this.font, this.hint, k, this.textY, i);
            }

            if (!bl3 && this.suggestion != null) {
                guiGraphics.text(this.font, this.suggestion, m - 1, this.textY, -8355712, this.textShadow);
            }

            if (l != j) {
                int n = this.textX + FormattedStringSplitter.width(this.font.getSplitter(), this.value.substring(0, l));
                guiGraphics.textHighlight(Math.min(m, this.getX() + this.width),
                        this.textY - 1,
                        Math.min(n - 1, this.getX() + this.width),
                        this.textY + 1 + 9,
                        this.invertHighlightedTextColor);
            }

            if (bl2) {
                if (bl3) {
                    guiGraphics.fill(m, this.textY - 1, m + 1, this.textY + 1 + 9, i);
                } else {
                    guiGraphics.text(this.font, "_", m, this.textY, i, this.textShadow);
                }
            }

            if (this.isHovered()) {
                guiGraphics.requestCursor(this.isEditable ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
            }
        }
    }

    @Override
    public void updateTextPosition() {
        if (this.font != null) {
            String string = FormattedStringSplitter.plainSubstrByWidth(this.font.getSplitter(),
                    this.value,
                    this.getInnerWidth(),
                    this.displayPos);
            this.textX = this.getX() + (this.isCentered() ?
                    (this.getWidth() - FormattedStringSplitter.width(this.font.getSplitter(), string)) / 2 :
                    (this.bordered ? 4 : 0));
            this.textY = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
        }
    }

    @Override
    public void scrollTo(int position) {
        if (this.font != null) {
            this.displayPos = Math.min(this.displayPos, this.value.length());
            int innerWidth = this.getInnerWidth();
            String string = FormattedStringSplitter.plainSubstrByWidth(this.font.getSplitter(),
                    this.value,
                    innerWidth,
                    this.displayPos);
            int k = string.length() + this.displayPos;
            if (position == this.displayPos) {
                this.displayPos -= FormattedStringSplitter.plainSubstrByWidth(this.font.getSplitter(),
                        this.value,
                        innerWidth,
                        true).length();
            }

            if (position > k) {
                this.displayPos += position - k;
            } else if (position <= this.displayPos) {
                this.displayPos -= this.displayPos - position;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
        }
    }

    @Override
    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.getX() :
                this.getX() + FormattedStringSplitter.width(this.font.getSplitter(), this.value.substring(0, charNum));
    }
}
