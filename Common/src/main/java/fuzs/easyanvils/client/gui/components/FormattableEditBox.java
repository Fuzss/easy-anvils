package fuzs.easyanvils.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

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
        this.addFormatter((String formatterValue, int position) -> {
            List<FormattedCharSequence> list = Lists.newArrayList();
            FormattedStringDecomposer.LengthLimitedCharSink sink = new FormattedStringDecomposer.LengthLimitedCharSink(
                    formatterValue.length(),
                    position);
            // format the whole value, we need the formatting to apply correctly and not get interrupted by the cursor being placed in between a formatting code
            FormattedStringDecomposer.iterateFormatted(this.value, Style.EMPTY, (index, style, j) -> {
                if (sink.accept(index, style, j)) {
                    list.add((FormattedCharSink formattedCharSink) -> formattedCharSink.accept(index, style, j));
                }

                return true;
            });

            return FormattedCharSequence.composite(list);
        });
    }

    @Override
    public void setValue(String text) {
        if (this.filter.test(text)) {
            // custom max text length adjustments so we ignore formatting codes
            int aboveMaxLength = ComponentDecomposer.getStringLength(text) - this.maxLength;
            if (aboveMaxLength > 0) {
                this.value = ComponentDecomposer.removeLast(text, aboveMaxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd(false);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
        }
    }

    @Override
    public void insertText(String textToWrite) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        // use our custom check for allowed chars so '§' is permitted
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

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (!this.canConsumeInput()) {
            return false;
            // use our custom check for allowed chars so '§' is permitted
        } else if (FormattedStringDecomposer.isAllowedChatCharacter((char) characterEvent.codepoint())) {
            if (this.isEditable) {
                this.insertText(characterEvent.codepointAsString());
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int findClickedPositionInText(MouseButtonEvent event) {
        int i = Math.min(Mth.floor(event.x()) - this.textX, this.getInnerWidth());
        String string = FormattedStringDecomposer.plainHeadByWidth(this.font,
                this.value,
                this.displayPos,
                this.getInnerWidth(),
                Style.EMPTY);
        return this.displayPos + FormattedStringDecomposer.plainHeadByWidth(this.font, string, 0, i, Style.EMPTY)
                .length();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                ResourceLocation resourceLocation = SPRITES.get(this.isActive(), this.isFocused());
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        resourceLocation,
                        this.getX(),
                        this.getY(),
                        this.getWidth(),
                        this.getHeight());
            }

            int i = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            String string = FormattedStringDecomposer.plainHeadByWidth(this.font,
                    this.value,
                    this.displayPos,
                    this.getInnerWidth(),
                    Style.EMPTY);
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && bl;
            int k = this.textX;
            int l = Mth.clamp(this.highlightPos - this.displayPos, 0, string.length());
            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                FormattedCharSequence formattedCharSequence = this.applyFormat(string2, this.displayPos);
                guiGraphics.drawString(this.font, formattedCharSequence, k, this.textY, i, this.textShadow);
                k += this.font.width(formattedCharSequence) + 1;
            }

            boolean bl3 = this.cursorPos < ComponentDecomposer.getStringLength(this.value)
                    || ComponentDecomposer.getStringLength(this.value) >= this.getMaxLength();
            int m = k;
            if (!bl) {
                m = j > 0 ? this.textX + this.width : this.textX;
            } else if (bl3) {
                m = k - 1;
                k--;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                guiGraphics.drawString(this.font,
                        this.applyFormat(string.substring(j), this.cursorPos),
                        k,
                        this.textY,
                        i,
                        this.textShadow);
            }

            if (this.hint != null && string.isEmpty() && !this.isFocused()) {
                guiGraphics.drawString(this.font, this.hint, k, this.textY, i);
            }

            if (!bl3 && this.suggestion != null) {
                guiGraphics.drawString(this.font, this.suggestion, m - 1, this.textY, -8355712, this.textShadow);
            }

            if (l != j) {
                int n = this.textX + FormattedStringDecomposer.stringWidth(this.font, this.value.substring(0, l), 0);
                guiGraphics.textHighlight(Math.min(m, this.getX() + this.width),
                        this.textY - 1,
                        Math.min(n - 1, this.getX() + this.width),
                        this.textY + 1 + 9);
            }

            if (bl2) {
                if (bl3) {
                    guiGraphics.fill(m, this.textY - 1, m + 1, this.textY + 1 + 9, i);
                } else {
                    guiGraphics.drawString(this.font, "_", m, this.textY, i, this.textShadow);
                }
            }

            if (this.isHovered()) {
                guiGraphics.requestCursor(this.isEditable ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
            }
        }
    }

    @Override
    protected void updateTextPosition() {
        if (this.font != null) {
            String string = FormattedStringDecomposer.plainHeadByWidth(this.font,
                    this.value.substring(this.displayPos),
                    0,
                    this.getInnerWidth(),
                    Style.EMPTY);
            this.textX = this.getX() + (this.isCentered() ?
                    (this.getWidth() - FormattedStringDecomposer.stringWidth(this.font, string, 0)) / 2 :
                    (this.bordered ? 4 : 0));
            this.textY = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
        }
    }

    @Override
    protected void scrollTo(int position) {
        int i = this.value.length();
        if (this.displayPos > i) {
            this.displayPos = i;
        }

        int j = this.getInnerWidth();
        String string = FormattedStringDecomposer.plainHeadByWidth(this.font,
                this.value,
                this.displayPos,
                j,
                Style.EMPTY);
        int k = string.length() + this.displayPos;
        if (position == this.displayPos) {
            this.displayPos -= FormattedStringDecomposer.plainTailByWidth(this.font, this.value, j, Style.EMPTY)
                    .length();
        }

        if (position > k) {
            this.displayPos += position - k;
        } else if (position <= this.displayPos) {
            this.displayPos -= this.displayPos - position;
        }

        this.displayPos = Mth.clamp(this.displayPos, 0, i);
    }

    @Override
    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.getX() :
                this.getX() + FormattedStringDecomposer.stringWidth(this.font, this.value.substring(0, charNum), 0);
    }
}
