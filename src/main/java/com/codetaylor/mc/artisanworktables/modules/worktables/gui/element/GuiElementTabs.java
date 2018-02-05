package com.codetaylor.mc.artisanworktables.modules.worktables.gui.element;

import com.codetaylor.mc.artisanworktables.ReferenceTexture;
import com.codetaylor.mc.artisanworktables.modules.worktables.ModuleWorktables;
import com.codetaylor.mc.artisanworktables.modules.worktables.gui.GuiTabOffset;
import com.codetaylor.mc.artisanworktables.modules.worktables.network.SPacketWorktableTab;
import com.codetaylor.mc.artisanworktables.modules.worktables.tile.spi.TileEntityWorktableBase;
import com.codetaylor.mc.athenaeum.gui.GuiContainerBase;
import com.codetaylor.mc.athenaeum.gui.element.GuiElementBase;
import com.codetaylor.mc.athenaeum.gui.element.IGuiElementClickable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GuiElementTabs
    extends GuiElementBase
    implements IGuiElementClickable {

  private static final int TAB_WIDTH = 24;
  private static final int TAB_SPACING = 2;
  private static final int TAB_CURRENT_OFFSET = 1;
  private static final int TAB_HEIGHT = 21;
  private static final int TAB_LEFT_OFFSET = 4;
  private static final int TAB_ITEM_HORIZONTAL_OFFSET = 4;
  private static final int TAB_ITEM_VERTICAL_OFFSET = 4;

  public static boolean RECALCULATE_TAB_OFFSETS = false;
  private static final GuiTabOffset GUI_TAB_OFFSET = new GuiTabOffset(0);

  private final TileEntityWorktableBase worktable;

  public GuiElementTabs(
      GuiContainerBase guiBase,
      TileEntityWorktableBase worktable,
      int elementWidth
  ) {

    super(guiBase, 0, -TAB_HEIGHT, elementWidth, TAB_HEIGHT);
    this.worktable = worktable;

    if (RECALCULATE_TAB_OFFSETS) {
      RECALCULATE_TAB_OFFSETS = false;
      this.calculateInitialTabOffset(worktable, GUI_TAB_OFFSET);
    }
  }

  @Override
  public void drawBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    int x = this.elementXModifiedGet();
    int y = this.elementYModifiedGet();

    int tabX = x + TAB_LEFT_OFFSET;
    int tabY = y;

    List<TileEntityWorktableBase> actualJoinedTables = this.worktable.getJoinedTables(new ArrayList<>());
    List<TileEntityWorktableBase> joinedTables = this.getJoinedTableOffsetView(
        actualJoinedTables,
        GUI_TAB_OFFSET.getOffset(),
        this.worktable.getMaximumDisplayedTabCount()
    );

    this.textureBind(ReferenceTexture.RESOURCE_LOCATION_GUI_ELEMENTS);

    // draw arrows

    if (GUI_TAB_OFFSET.getOffset() > 0) {
      // draw left button
      Gui.drawModalRectWithCustomSizedTexture(
          this.elementXModifiedGet() + TAB_LEFT_OFFSET + TAB_ITEM_HORIZONTAL_OFFSET - 18,
          tabY,
          TAB_WIDTH,
          this.worktable.getWorktableGuiTabTextureYOffset() * TAB_HEIGHT,
          8,
          TAB_HEIGHT,
          ReferenceTexture.TEXTURE_GUI_ELEMENTS_WIDTH,
          ReferenceTexture.TEXTURE_GUI_ELEMENTS_HEIGHT
      );
    }

    int maximumDisplayedTabCount = this.worktable.getMaximumDisplayedTabCount();

    if (GUI_TAB_OFFSET.getOffset() + maximumDisplayedTabCount < actualJoinedTables.size()) {
      // draw right button
      Gui.drawModalRectWithCustomSizedTexture(
          this.elementXModifiedGet() + this.elementWidthModifiedGet() - 12,
          tabY,
          TAB_WIDTH + 8,
          this.worktable.getWorktableGuiTabTextureYOffset() * TAB_HEIGHT,
          8,
          TAB_HEIGHT,
          ReferenceTexture.TEXTURE_GUI_ELEMENTS_WIDTH,
          ReferenceTexture.TEXTURE_GUI_ELEMENTS_HEIGHT
      );
    }

    // draw tabs

    for (TileEntityWorktableBase joinedTable : joinedTables) {
      int textureY = joinedTable.getWorktableGuiTabTextureYOffset() * TAB_HEIGHT;

      if (joinedTable == this.worktable) {
        //this.drawTexturedModalRect(tabX, tabY + TAB_CURRENT_OFFSET, 0, textureY, TAB_WIDTH, TAB_HEIGHT);
        Gui.drawModalRectWithCustomSizedTexture(
            tabX,
            tabY + TAB_CURRENT_OFFSET,
            0,
            textureY,
            TAB_WIDTH,
            TAB_HEIGHT,
            ReferenceTexture.TEXTURE_GUI_ELEMENTS_WIDTH,
            ReferenceTexture.TEXTURE_GUI_ELEMENTS_HEIGHT
        );

      } else {
        //this.drawTexturedModalRect(tabX, tabY, 0, textureY, TAB_WIDTH, 21);
        Gui.drawModalRectWithCustomSizedTexture(
            tabX,
            tabY,
            0,
            textureY,
            TAB_WIDTH,
            TAB_HEIGHT,
            ReferenceTexture.TEXTURE_GUI_ELEMENTS_WIDTH,
            ReferenceTexture.TEXTURE_GUI_ELEMENTS_HEIGHT
        );
      }

      tabX += TAB_WIDTH + TAB_SPACING;
    }

    // draw tab icons
    tabX = x + TAB_LEFT_OFFSET + TAB_ITEM_HORIZONTAL_OFFSET;
    tabY = y + TAB_ITEM_VERTICAL_OFFSET;

    RenderHelper.enableGUIStandardItemLighting();

    for (TileEntityWorktableBase joinedTable : joinedTables) {
      IBlockState blockState = joinedTable.getWorld().getBlockState(joinedTable.getPos());
      blockState = blockState.getBlock().getActualState(blockState, this.worktable.getWorld(), joinedTable.getPos());
      ItemStack itemStack = joinedTable.getItemStackForTabDisplay(blockState);

      if (joinedTable == this.worktable) {
        this.guiBase.getItemRender().renderItemAndEffectIntoGUI(itemStack, tabX, tabY + TAB_CURRENT_OFFSET);

      } else {
        this.guiBase.getItemRender().renderItemAndEffectIntoGUI(itemStack, tabX, tabY);
      }

      tabX += TAB_WIDTH + TAB_SPACING;
    }
  }

  @Override
  public void drawForegroundLayer(int mouseX, int mouseY) {
    //
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    if (mouseButton != 0) {
      return;
    }

    List<TileEntityWorktableBase> actualJoinedTables = this.worktable.getJoinedTables(new ArrayList<>());
    List<TileEntityWorktableBase> joinedTables = this.getJoinedTableOffsetView(
        actualJoinedTables,
        GUI_TAB_OFFSET.getOffset(),
        this.worktable.getMaximumDisplayedTabCount()
    );

    int yMin = this.elementYModifiedGet();
    int yMax = yMin + TAB_HEIGHT;

    for (int i = 0; i < joinedTables.size(); i++) {
      int xMin = this.elementXModifiedGet() + TAB_ITEM_HORIZONTAL_OFFSET + (TAB_WIDTH + TAB_SPACING) * i;
      int xMax = xMin + TAB_WIDTH;

      if (mouseX <= xMax
          && mouseX >= xMin
          && mouseY <= yMax
          && mouseY >= yMin) {
        TileEntityWorktableBase table = joinedTables.get(i);
        BlockPos pos = table.getPos();
        ModuleWorktables.PACKET_SERVICE.sendToServer(new SPacketWorktableTab(
            pos.getX(),
            pos.getY(),
            pos.getZ()
        ));
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
      }
    }

    int maximumDisplayedTabCount = this.worktable.getMaximumDisplayedTabCount();

    if (GUI_TAB_OFFSET.getOffset() > 0) {
      // check for left button click
      int xMin = this.elementXModifiedGet() + TAB_LEFT_OFFSET + TAB_ITEM_HORIZONTAL_OFFSET - 18;
      int xMax = xMin + 8;

      if (mouseX <= xMax
          && mouseX >= xMin
          && mouseY <= yMax
          && mouseY >= yMin) {
        int tabOffset = GUI_TAB_OFFSET.getOffset() - maximumDisplayedTabCount;
        GUI_TAB_OFFSET.setOffset(Math.max(0, tabOffset));
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
      }
    }

    if (GUI_TAB_OFFSET.getOffset() + maximumDisplayedTabCount < actualJoinedTables.size()) {
      // check for right button click
      int xMin = this.elementXModifiedGet() + this.elementWidthModifiedGet() - 12;
      int xMax = xMin + 8;

      if (mouseX <= xMax
          && mouseX >= xMin
          && mouseY <= yMax
          && mouseY >= yMin) {
        GUI_TAB_OFFSET.setOffset(Math.min(
            actualJoinedTables.size() - maximumDisplayedTabCount,
            GUI_TAB_OFFSET.getOffset() + maximumDisplayedTabCount
        ));
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
      }
    }
  }

  /**
   * Sets the initial tab offset when the gui is opened.
   *
   * @param worktableBase the worktable
   * @param guiTabOffset  the offset
   */
  private void calculateInitialTabOffset(TileEntityWorktableBase worktableBase, GuiTabOffset guiTabOffset) {

    int maximumDisplayedTabCount = worktableBase.getMaximumDisplayedTabCount();
    guiTabOffset.setOffset(0);

    List<TileEntityWorktableBase> actualJoinedTables = worktableBase.getJoinedTables(new ArrayList<>());

    boolean tabInView = false;

    while (!tabInView && !actualJoinedTables.isEmpty()) {
      List<TileEntityWorktableBase> joinedTables = this.getJoinedTableOffsetView(
          actualJoinedTables,
          guiTabOffset.getOffset(),
          worktableBase.getMaximumDisplayedTabCount()
      );

      for (TileEntityWorktableBase joinedTable : joinedTables) {

        if (joinedTable == worktableBase) {
          tabInView = true;
          break;
        }
      }

      if (!tabInView) {
        guiTabOffset.setOffset(Math.min(
            actualJoinedTables.size() - maximumDisplayedTabCount,
            guiTabOffset.getOffset() + maximumDisplayedTabCount
        ));
      }
    }
  }

  private List<TileEntityWorktableBase> getJoinedTableOffsetView(
      List<TileEntityWorktableBase> list,
      int offset,
      int maximumDisplayedTabCount
  ) {

    List<TileEntityWorktableBase> result = new ArrayList<>(maximumDisplayedTabCount);

    if (offset + maximumDisplayedTabCount > list.size()) {
      offset = list.size() - maximumDisplayedTabCount;
    }

    if (offset < 0) {
      offset = 0;
    }

    int limit = Math.min(list.size(), offset + maximumDisplayedTabCount);

    for (int i = offset; i < limit; i++) {
      result.add(list.get(i));
    }

    return result;
  }

}
