package fuzs.easyanvils.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ComponentDecomposer {
    private static final Style EMPTY = Style.EMPTY.withColor(ChatFormatting.WHITE.getColor()).withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false);

    public static String toFormattedString(Component component) {
        StringBuilder builder = new StringBuilder();
        component.visit((Style style, String string) -> {
            builder.append(applyLegacyFormatting(string, style));
            return Optional.empty();
        }, Style.EMPTY);
        return builder.toString();
    }

    private static String applyLegacyFormatting(String string, Style style) {
        return toLegacyFormatting(style).stream()
                .map(ChatFormatting::toString)
                .reduce(String::concat)
                .map(formattings -> formattings.concat(string).concat(ChatFormatting.RESET.toString()))
                .orElse(string);
    }

    private static List<ChatFormatting> toLegacyFormatting(Style style) {
        List<ChatFormatting> formattings = Lists.newArrayList();
        if (style.isEmpty()) return formattings;
        TextColor textColor = style.getColor();
        if (textColor != null) {
            ChatFormatting chatFormatting = ChatFormatting.getByName(textColor.toString());
            if (chatFormatting != null) {
                formattings.add(chatFormatting);
            }
        }
        if (style.isBold()) formattings.add(ChatFormatting.BOLD);
        if (style.isItalic()) formattings.add(ChatFormatting.ITALIC);
        if (style.isUnderlined()) formattings.add(ChatFormatting.UNDERLINE);
        if (style.isStrikethrough()) formattings.add(ChatFormatting.STRIKETHROUGH);
        if (style.isObfuscated()) formattings.add(ChatFormatting.OBFUSCATED);
        return ImmutableList.copyOf(formattings);
    }

    public static Component toFormattedComponent(@Nullable String value) {
        return toComponentEntries(value).stream()
                .map(entry -> Component.literal(entry.getValue()).withStyle(entry.getStyle()))
                .reduce(MutableComponent::append)
                .orElse(Component.empty());
    }

    public static String removeLast(@Nullable String value, int amount) {
        Deque<ComponentEntry> componentEntries = toComponentEntries(value);
        for (int i = 0; i < amount; i++) {
            ComponentEntry componentEntry = componentEntries.peekLast();
            if (componentEntry != null) {
                if (!componentEntry.getValue().isEmpty()) {
                    componentEntry.updateValue(s -> s.substring(0, s.length() - 1));
                }
                if (componentEntry.getValue().isEmpty()) {
                    componentEntries.pollLast();
                }
            }
        }
        return componentEntries.stream().map(entry -> applyLegacyFormatting(entry.getValue(), entry.getStyle())).collect(Collectors.joining());
    }

    private static Deque<ComponentEntry> toComponentEntries(@Nullable String value) {
        Deque<ComponentEntry> values = Lists.newLinkedList();
        if (value == null) return values;
        AtomicBoolean resetStyle = new AtomicBoolean(true);
        StringDecomposer.iterateFormatted(value, EMPTY, (int index, Style style, int codePoint) -> {
            ComponentEntry last = values.peekLast();
            if (last != null && last.getStyle().equals(style)) {
                last.updateValue(s -> s + Character.toString(codePoint));
            } else {
                values.offerLast(new ComponentEntry(codePoint, style));
            }
            if (style != EMPTY) {
                resetStyle.set(false);
            }
            return true;
        });
        // when no formatting codes have been specified fall back to empty style so that vanilla italic name for renamed /
        // blue name for enchanted will apply; this preserves vanilla behavior
        if (resetStyle.get()) {
            values.forEach(ComponentEntry::resetStyle);
        }
        return values;
    }

    public static int getStringLength(String value) {
        return toFormattedComponent(value).getString().length();
    }

    private static class ComponentEntry {
        private String value;
        private Style style;

        public ComponentEntry(int codePoint, Style style) {
            this.value = Character.toString(codePoint);
            this.style = style;
        }

        public String getValue() {
            return this.value;
        }

        public void updateValue(UnaryOperator<String> operator) {
            this.value = operator.apply(this.value);
        }

        public Style getStyle() {
            return this.style;
        }

        public void resetStyle() {
            this.style = Style.EMPTY;
        }
    }
}
