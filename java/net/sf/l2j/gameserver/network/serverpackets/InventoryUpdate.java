package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInfo;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemState;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Advi
 */
public class InventoryUpdate extends L2GameServerPacket
{
	private List<ItemInfo> _items;
	
	public InventoryUpdate()
	{
		_items = new ArrayList<>();
	}
	
	public InventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}
	
	public void addItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item));
	}
	
	public void addNewItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.ADDED));
	}
	
	public void addModifiedItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.MODIFIED));
	}
	
	public void addRemovedItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.REMOVED));
	}
	
	public void addItems(List<ItemInstance> items)
	{
		if (items != null)
			for (ItemInstance item : items)
				if (item != null)
					_items.add(new ItemInfo(item));
	}
	private static boolean isHandpart(Item item)
	{
		if (item.getBodyPart() == Item.SLOT_R_HAND || item.getBodyPart() == Item.SLOT_LR_HAND)
			return true;
		
		return false;
	}
	@Override
	protected final void writeImpl()
	{
		writeC(0x27);
		writeH(_items.size());
		
		for (ItemInfo temp : _items)
		{
			Item item = temp.getItem();
			
			Player activeChar = World.getInstance().getPlayer(temp.getOwnerId());
			
		/*	writeH(temp.getChange().ordinal());
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(item.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeH(temp.getEquipped());
			writeD(item.getBodyPart());
			writeH(temp.getEnchant());
			writeH(temp.getCustomType2());
			writeD(temp.getAugmentationBoni());
			writeD(temp.getMana());*/
			writeH(temp.getChange().ordinal());
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(item.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			
			if (activeChar != null)
			{
				if (activeChar.getFakeWeaponObjectId() > 0)
				{
					if (temp.getObjectId() == activeChar.getFakeWeaponObjectId())
						writeH(0x01);
					else
						writeH(temp.getEquipped());
					
					//		if (temp.getObjectId() == activeChar.getFakeArmorObjectId())
					//		writeD(item.isFakeArmor() ? Item.SLOT_ALLDRESS : item.getBodyPart());
					if (temp.getObjectId() == activeChar.getFakeWeaponObjectId() && item.getBodyPart() == Item.SLOT_R_HAND)
						writeD(item.isFakeWeapon() ? Item.SLOT_R_HAND : item.getBodyPart());
					else if (temp.getObjectId() == activeChar.getFakeWeaponObjectId() && item.getBodyPart() == Item.SLOT_LR_HAND)
						writeD(item.isFakeWeapon() ? Item.SLOT_LR_HAND : item.getBodyPart());
					else if (isHandpart(item) && temp.getEquipped() == 1 && activeChar.getFakeWeaponObjectId() > 0)
						writeD(99);
					else
						writeD(item.getBodyPart());
				}
				else
				{
					writeH(temp.getEquipped());
					writeD(item.getBodyPart());
				}
			}
			else
			{
				writeH(temp.getEquipped());
				writeD(item.getBodyPart());
			}
			
			writeH(temp.getEnchant());
			writeH(temp.getCustomType2());
			writeD(temp.getAugmentationBoni());
			writeD(temp.getMana());
		}
	}
}