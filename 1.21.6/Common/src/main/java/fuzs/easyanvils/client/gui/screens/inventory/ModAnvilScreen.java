package fuzs.easyanvils.client.gui.screens.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.FormattableEditBox;
import fuzs.easyanvils.client.gui.components.FormattingGuideWidget;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.client.ServerboundRenameItemMessage;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilScreen extends AnvilScreen {
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

    public ModAnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component);
        this.titleLabelY = 8;
    }

    @Override
    protected void subInit() {
        if (EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            this.name = new FormattableEditBox(this.font,
                    this.leftPos + 62,
                    this.topPos + 24,
                    103,
                    12,
                    AnvilBlockEntity.REPAIR_COMPONENT);
        } else {
            this.name = new EditBox(this.font,
                    this.leftPos + 62,
                    this.topPos + 24,
                    103,
                    12,
                    AnvilBlockEntity.REPAIR_COMPONENT);
        }
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addRenderableWidget(this.name);
        this.name.setEditable(this.menu.getSlot(0).hasItem());
        if (EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            this.addRenderableWidget(new FormattingGuideWidget(this.leftPos + this.imageWidth - 7,
                    this.topPos + this.titleLabelY,
                    this.font));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() == this.name && this.isDragging() && button == 0) {
            return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void onNameChanged(String input) {
        Slot slot = this.menu.getSlot(0);
        if (!slot.hasItem()) return;
        if (!slot.getItem().has(DataComponents.CUSTOM_NAME) && input.equals(slot.getItem()
                .getHoverName()
                .getString())) {
            input = "";
        }

        if (this.menu.setItemName(input)) {
            MessageSender.broadcast(new ServerboundRenameItemMessage(input));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // copied from AbstractContainerScreen super
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0XFF404040, false);
        guiGraphics.drawString(this.font,
                this.playerInventoryTitle,
                this.inventoryLabelX,
                this.inventoryLabelY,
                0XFF404040,
                false);
        int enchantmentLevelCost = this.menu.getCost();
        if (enchantmentLevelCost > 0) {
            int textColor = 0XFF80FF20;
            Component component;
            if (this.isTooExpensive(enchantmentLevelCost)) {
                component = TOO_EXPENSIVE_TEXT;
                textColor = 0XFFFF6060;
            } else if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("container.repair.cost", enchantmentLevelCost);
                if (!this.menu.getSlot(2).mayPickup(this.minecraft.player)) {
                    textColor = 0XFFFF6060;
                }
            }

            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                guiGraphics.fill(k - 2, 67, this.imageWidth - 8, 79, 0X4F000000);
                guiGraphics.drawString(this.font, component, k, 69, textColor);
            }
        }
    }

    private boolean isTooExpensive(int repairCost) {
        // allow for custom max repair cost
        int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).costs.tooExpensiveLimit;
        return (maxAnvilRepairCost != -1 && repairCost >= maxAnvilRepairCost || repairCost == -1)
                && !this.minecraft.player.hasInfiniteMaterials();
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 0) {
            this.name.setValue(stack.isEmpty() ? "" : ComponentDecomposer.toFormattedString(stack.getHoverName()));
            this.name.setEditable(!stack.isEmpty());
            this.setFocused(this.name);
        }
    }
}
