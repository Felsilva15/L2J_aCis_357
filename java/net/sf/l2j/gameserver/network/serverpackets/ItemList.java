package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Set;

public class ItemList extends L2GameServerPacket
{
	private final Set<ItemInstance> _items;
	private final boolean _showWindow;
	private final Player _activeChar;
	public ItemList(Player cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		_activeChar = cha;
	}
	private static boolean isHandpart(Item item)
	{
		if (item.getBodyPart() == Item.SLOT_R_HAND || item.getBodyPart() == Item.SLOT_LR_HAND)
			return true;

		return false;
	}
	@Override
	/*protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(_showWindow ? 0x01 : 0x00);
		writeH(_items.size());
		
		for (ItemInstance temp : _items)
		{
			Item item = temp.getItem();
			
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD((temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());
		}
	}*/
	protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(_showWindow ? 0x01 : 0x00);
		writeH(_items.size());
		
		for (ItemInstance temp : _items)
		{
			if (temp.getItem() == null)
				continue;
			
			Item item = temp.getItem();
			
		/*	writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD((temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());*/
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
						
			if (_activeChar.getFakeWeaponObjectId() > 0)
			{
				if (temp.getObjectId() == _activeChar.getFakeWeaponObjectId())
					writeH(0x01);
				else
					writeH(temp.isEquipped() ? 0x01 : 0x00);

			//	if (temp.getObjectId() == _activeChar.getFakeArmorObjectId())
			//		writeD(item.isFakeArmor() ? Item.SLOT_ALLDRESS : item.getBodyPart());
				if (temp.getObjectId() == _activeChar.getFakeWeaponObjectId() && item.getBodyPart() == Item.SLOT_R_HAND)
					writeD(item.isFakeWeapon() ? Item.SLOT_R_HAND : item.getBodyPart());
				else if (temp.getObjectId() == _activeChar.getFakeWeaponObjectId() && item.getBodyPart() == Item.SLOT_LR_HAND)
					writeD(item.isFakeWeapon() ? Item.SLOT_LR_HAND : item.getBodyPart());
				else if (isHandpart(item) && temp.isEquipped() && _activeChar.getFakeWeaponObjectId() > 0)
					writeD(99);
				else
					writeD(item.getBodyPart());
			}
			else
			{
				writeH(temp.isEquipped() ? 0x01 : 0x00);
				writeD(item.getBodyPart());
			}
			
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD((temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());
		}
	}
}