package com.codetaylor.mc.artisanworktables.modules.toolbox.block;

import com.codetaylor.mc.artisanworktables.modules.toolbox.tile.TileEntityToolbox;
import com.codetaylor.mc.artisanworktables.modules.worktables.ModuleWorktables;
import com.codetaylor.mc.artisanworktables.modules.worktables.Util;
import com.codetaylor.mc.artisanworktables.modules.worktables.tile.spi.TileEntityBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockToolboxBase
    extends Block {

  private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);

  public BlockToolboxBase(Material materialIn) {

    super(materialIn);
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {

    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {

    return false;
  }

  @Override
  public BlockFaceShape getBlockFaceShape(
      IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face
  ) {

    return BlockFaceShape.UNDEFINED;
  }

  @Override
  public AxisAlignedBB getBoundingBox(
      IBlockState state, IBlockAccess source, BlockPos pos
  ) {

    return AABB;
  }

  @Override
  public boolean onBlockActivated(
      World worldIn,
      BlockPos pos,
      IBlockState state,
      EntityPlayer playerIn,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ
  ) {

    if (worldIn.isRemote) {
      return true;
    }

    playerIn.openGui(ModuleWorktables.MOD_INSTANCE, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
    return true;
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {

    return true;
  }

  @Override
  public boolean removedByPlayer(
      IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest
  ) {

    // Delay the destruction of the TE until after BlockMortar#getDrops is called. We need
    // access to the TE while creating the dropped item in order to serialize it.
    return willHarvest || super.removedByPlayer(state, world, pos, player, false);
  }

  @Override
  public void getDrops(
      NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune
  ) {

    drops.clear();

    TileEntity tileEntity = world.getTileEntity(pos);

    if (tileEntity != null
        && tileEntity instanceof TileEntityToolbox) {

      boolean dropAllItems = !this.keepContentsWhenBroken();

      if (dropAllItems) {
        drops.addAll(((TileEntityToolbox) tileEntity).getBlockBreakDrops());

      } else {
        ItemStack itemStack = this.getBlockAsItemStack();
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound teCompound = new NBTTagCompound();
        tileEntity.writeToNBT(teCompound);
        compound.setTag("BlockEntityTag", teCompound);
        itemStack.setTagCompound(compound);
        drops.clear();
        drops.add(itemStack);
      }
    }
  }

  @Override
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {

    return null;
  }

  @Override
  public void harvestBlock(
      World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack
  ) {

    super.harvestBlock(worldIn, player, pos, state, te, stack);
    worldIn.setBlockToAir(pos);
  }

  @Override
  public void breakBlock(World world, BlockPos pos, IBlockState state) {

    if (!world.isRemote) {
      List<TileEntityBase> joinedTables = Util.getJoinedTables(new ArrayList<>(), world, pos, null);

      for (TileEntityBase table : joinedTables) {
        table.onJoinedBlockBreak(pos);
      }
    }

    super.breakBlock(world, pos, state);
  }

  protected abstract boolean keepContentsWhenBroken();

  protected abstract ItemStack getBlockAsItemStack();
}
