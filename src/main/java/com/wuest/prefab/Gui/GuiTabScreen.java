package com.wuest.prefab.Gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wuest.prefab.Gui.Controls.GuiTab;
import com.wuest.prefab.Gui.Controls.GuiTabTray;
import com.wuest.prefab.Structures.Gui.GuiStructure;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * @author WuestMan
 */
public class GuiTabScreen extends GuiStructure {
    protected GuiTabTray Tabs;

    public GuiTabScreen() {
        super("TabScreen");
        this.Tabs = new GuiTabTray();
    }

    /**
     * Processes when this tab is clicked.
     *
     * @param tab The tab which was clicked.
     */
    protected void tabClicked(GuiTab tab) {
    }

    protected GuiTab getSelectedTab() {
        return this.Tabs.GetSelectedTab();
    }

    @Override
    public void init() {
        this.Tabs.GetTabs().clear();
        this.children.add(this.Tabs);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        // Draw the default labels and buttons.
        super.render(mouseX, mouseY, partialTicks);

        // Draw the tabs.
        this.Tabs.DrawTabs(this.minecraft, mouseX, mouseY);
    }

    public void buttonClicked(Button button)
    {
        // This does nothing on purpose.
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean returnValue = false;

        if (mouseButton == 0) {
            // This handles the button presses.
            returnValue = super.mouseClicked(mouseX, mouseY, mouseButton);

            if (returnValue) {
                // Handle the tab clicking.
                ArrayList<GuiTab> guiTabs = this.Tabs.GetTabs();

                for (GuiTab tab : guiTabs) {
                    if (tab.mouseClicked(mouseX, mouseY, mouseButton)) {
                        tab.playDownSound(this.minecraft.getSoundHandler());
                        this.tabClicked(tab);
                        break;
                    }
                }
            }
        }

        return returnValue;
    }
}
