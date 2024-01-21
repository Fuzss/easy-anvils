package fuzs.easyanvils.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
                .map(entry -> new TextComponent(entry.string().get()).withStyle(entry.style()))
                .reduce(MutableComponent::append)
                .orElse(new TextComponent(""));
    }

    public static String removeLast(@Nullable String value, int amount) {
        Deque<ComponentEntry> componentEntries = toComponentEntries(value);
        for (int i = 0; i < amount; i++) {
            ComponentEntry componentEntry = componentEntries.peekLast();
            if (componentEntry != null) {
                if (!componentEntry.string().get().isEmpty()) {
                    componentEntry.string().updateAndGet(s -> s.substring(0, s.length() - 1));
                }
                if (componentEntry.string().get().isEmpty()) {
                    componentEntries.pollLast();
                }
            }
        }
        return componentEntries.stream().map(entry -> applyLegacyFormatting(entry.string().get(), entry.style())).collect(Collectors.joining());
    }

    private static Deque<ComponentEntry> toComponentEntries(@Nullable String value) {
        Deque<ComponentEntry> values = Lists.newLinkedList();
        if (value == null) return values;
        StringDecomposer.iterateFormatted(value, EMPTY, (i, style, j) -> {
            ComponentEntry last = values.peekLast();
            if (last != null && last.style().equals(style)) {
                last.string().updateAndGet(s -> s + Character.toString(j));
            } else {
                values.offerLast(new ComponentEntry(new AtomicReference<>(Character.toString(j)), style));
            }
            return true;
        });
        return values;
    }

    public static int getStringLength(String value) {
        return toFormattedComponent(value).getString().length();
    }

    private record ComponentEntry(AtomicReference<String> string, Style style) {

    }
}
