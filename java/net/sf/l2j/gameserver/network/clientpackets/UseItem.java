package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.events.CTF;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.ActionType;
import net.sf.l2j.gameserver.model.item.type.ArmorType;
import net.sf.l2j.gameserver.model.item.type.CrystalType;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import Dev.Event.BossEvent.KTBConfig;
import Dev.Event.BossEvent.KTBEvent;
import Dev.ItemsTime.TimedItemManager;

public final class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	
	public static class WeaponEquipTask implements Runnable
	{
		ItemInstance _item;
		Player _activeChar;
		
		public WeaponEquipTask(ItemInstance it, Player character)
		{
			_item = it;
			_activeChar = character;
		}
		
		@Override
		public void run()
		{
			_activeChar.useEquippableItem(_item, false);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
	
		
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}
		
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		
		
		if (activeChar.isGM())
			activeChar.sendMessage(item.getItem().getName() + " , ID: " + item.getItem().getItemId());
		
		if (item.getItem().getType2() == Item.TYPE2_QUEST)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		 // Check if the item is a timed item
		long remainingTime = TimedItemManager.getInstance().getRemainingTime(item.getObjectId());
		if (remainingTime > 0) 
		{
		    long days = remainingTime / (60 * 60 * 24);  // Dias
		    long hours = (remainingTime % (60 * 60 * 24)) / (60 * 60);  // Horas
		    long minutes = (remainingTime % (60 * 60)) / 60;  // Minutos
		    long seconds = remainingTime % 60;  // Segundos

		    // Envia mensagem com o tempo restante
		    activeChar.sendMessage("Your item will expire in " + days + " day(s), " + hours + " hour(s), " + minutes + " min(s), and " + seconds + " second(s).");
		} 
		else 
		{
		    // Só envia a mensagem de expiração se o item estiver na lista configurada
		    if (Config.LIST_TIMED_ITEMS.contains(item.getItemId()))
		    {
		        activeChar.sendMessage("This item has expired.");
		    }
		}
		if (activeChar.isAlikeDead() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid())
			return;
		
		if (!Config.KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
		{
			final IntIntHolder[] sHolders = item.getItem().getSkills();
			if (sHolders != null)
			{
				for (IntIntHolder sHolder : sHolders)
				{
					final L2Skill skill = sHolder.getSkill();
					if (skill != null && (skill.getSkillType() == L2SkillType.TELEPORT || skill.getSkillType() == L2SkillType.RECALL))
						return;
				}
			}
		}
		if (KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted())
		{
			if (KTBConfig.KTB_LISTID_RESTRICT.contains(Integer.valueOf(item.getItemId())))
			{
				activeChar.sendMessage("You can not use this item during KTB.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		if (Config.NOTALLOWEDUSELIGHT.contains(activeChar.getClassId().getId()) && !activeChar.isGM() && !activeChar.isInOlympiadMode())
		{
			if (item.getItemType() == ArmorType.LIGHT)
			{
				activeChar.sendMessage("this class can not use set light!");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (Config.NOTALLOWEDUSEHEAVY.contains(activeChar.getClassId().getId()) && !activeChar.isGM() && !activeChar.isInOlympiadMode())
		{
			if (item.getItemType() == ArmorType.HEAVY)
			{
				activeChar.sendMessage("this class can not use set heavy!");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (Config.ALT_DISABLE_BOW_CLASSES && !activeChar.isGM() && !activeChar.isInOlympiadMode())
		{
			if (item.getItem() instanceof Weapon && ((Weapon) item.getItem()).getItemType() == WeaponType.BOW)
			{
				if (Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendMessage("You are not allowed to equip this item.");
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (Config.ALT_DISABLE_BOW_CLASSES_OLY && !activeChar.isGM() && activeChar.isInOlympiadMode())
		{
			if (item.getItem() instanceof Weapon && ((Weapon) item.getItem()).getItemType() == WeaponType.BOW)
			{
				if (Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendMessage("You are not allowed to equip this item.");
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (activeChar.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().isInProgress() && Config.LISTID_RESTRICT.contains(item.getItemId()))
			{
				activeChar.sendMessage("Aguarde o fim da siege para utilizar esse item.");
				return;
			}
		}
		
		if (Config.WYVERN_PROTECTION && activeChar.isInsideZone(ZoneId.PEACE) && Config.LISTID_RESTRICT.contains(item.getItemId()))
		{
			activeChar.sendMessage("You can not use this item here within the city.");
			return;
		}
		//Fix nao summonar em dentro raidzone, party farm, bossZone e pvpzone
		if (activeChar.isInsideZone(ZoneId.PVP_CUSTOM) || activeChar.isInsideZone(ZoneId.PARTYZONE) || activeChar.isInsideZone(ZoneId.BOSS) || activeChar.isInsideZone(ZoneId.RAID))
		{
			if(item.getItemId() == 4422 || item.getItemId() == 4423 || item.getItemId() == 4424 || item.getItemId() == 8663)
			{
				activeChar.sendMessage("You can not use this item here PT Farm,BossZone, PvP Zone!");
				return;
			}
	
		}
	//	if (activeChar.isInsideZone(ZoneId.BOSS) && (item.getItemId() == 4422 || item.getItemId() == 4423 || item.getItemId() == 4424 || item.getItemId() == 8663))
	//	{
	//		activeChar.sendMessage("The use of mounts is prohibited in Zone Boss.");
	//		return;
	//	}
		
		if (activeChar.isInsideZone(ZoneId.PVP_CUSTOM) && Config.LISTID_RESTRICT.contains(item.getItemId()))
		{
			activeChar.sendMessage("You can not use this item here within the pvpzone.");
			return;
		}
		if (activeChar.isInsideZone(ZoneId.TIME_FARM) && (item.getItemId() == 4422 || item.getItemId() == 4423 || item.getItemId() == 4424 || item.getItemId() == 8663))
		{
			activeChar.sendMessage("You can not use this item here within the Time Zone.");
			return;
		}
		if ((item.getItemId() == 8663 || item.getItemId() == 4422 || item.getItemId() == 4423 || item.getItemId() == 4424) && ((activeChar._inEventCTF && (CTF.is_teleport() || CTF.is_started())) || activeChar.isArenaProtection()))
			return;
		
		if ((item.getItemId() == 1538 || item.getItemId() == 3958 || item.getItemId() == 5858 || item.getItemId() == 5859 || item.getItemId() == 9156) && ((activeChar._inEventCTF && CTF.is_started()) || activeChar.isArenaProtection() || activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.TOURNAMENT)))
		{
			activeChar.sendMessage("You can not use this item in Combat/Event mode..");
			return;
		}
		
		if (activeChar.isInArenaEvent() || activeChar.isArenaProtection())
		{
			if (Config.TOURNAMENT_LISTID_RESTRICT.contains(item.getItemId()))
			{
				activeChar.sendMessage("You can not use this item during Tournament Event.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (Config.FIGHTER_LISTID_RESTRICT.contains(item.getItemId()) && activeChar.isMageClass())
		{
			activeChar.sendMessage("Only fighter can equip this item.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (Config.MAGE_LISTID_RESTRICT.contains(item.getItemId()) && !activeChar.isMageClass())
		{
			activeChar.sendMessage("Only Mage can equip this item.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		/*
		 * The player can't use pet items if no pet is currently summoned. If a pet is summoned and player uses the item directly, it will be used by the pet.
		 */
		if (item.isPetItem())
		{
			// If no pet, cancels the use
			if (!activeChar.hasPet())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}
			
			final Pet pet = ((Pet) activeChar.getPet());
			
			if (!pet.canWear(item.getItem()))
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			if (pet.isDead())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}
			
			if (!pet.getInventory().validateCapacity(item))
			{
				activeChar.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!pet.getInventory().validateWeight(item, 1))
			{
				activeChar.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			activeChar.transferItem("Transfer", _objectId, 1, pet.getInventory(), pet);
			
			// Equip it, removing first the previous item.
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
			}
			else
			{
				pet.getInventory().equipPetItem(item);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
			}
			
			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}
		
		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(activeChar, activeChar, true))
				return;
		}
		
		if (!Config.OLLY_GRADE_A && item.getItem().getCrystalType() == CrystalType.S && (activeChar.isInOlympiadMode() || activeChar.isOlympiadProtection() || OlympiadManager.getInstance().isRegistered(activeChar)))
		{
			activeChar.sendMessage("[Olympiad]: Items Grade S cannot be used in Olympiad Event");
			return;
		}
		
	      if (item.getEnchantLevel() > Config.ALT_OLY_ENCHANT_LIMIT && (activeChar.isInOlympiadMode() || activeChar.isOlympiadProtection() || OlympiadManager.getInstance().isRegistered(activeChar)))
	      {
	         activeChar.sendMessage("Equipment with enchant level above +" + Config.ALT_OLY_ENCHANT_LIMIT + " can not be used while registered in olympiad.");
	         return;
	      }
	      
			if ((activeChar.isArena1x1() && !Config.TOUR_GRADE_A_1X1) || (activeChar.isArena2x2() && !Config.TOUR_GRADE_A_2X2) || (activeChar.isArena5x5() && !Config.TOUR_GRADE_A_5X5) || (activeChar.isArena9x9() && !Config.TOUR_GRADE_A_9X9) && item.getItem().getCrystalType() == CrystalType.S && !(item.getItem().getCrystalType() == CrystalType.NONE))
			{
				activeChar.sendMessage("Tournament: Items Grade S cannot be used in Tournament Event");
				return;
			}
			
		      if (item.getEnchantLevel() > Config.ALT_TOUR_ENCHANT_LIMIT && (activeChar.isArena1x1() && !Config.TOUR_GRADE_A_1X1) || (activeChar.isArena2x2() && !Config.TOUR_GRADE_A_2X2) || (activeChar.isArena5x5() && !Config.TOUR_GRADE_A_5X5) || (activeChar.isArena9x9() && !Config.TOUR_GRADE_A_9X9))
		      {
		         activeChar.sendMessage("Equipment with enchant level above +" + Config.ALT_TOUR_ENCHANT_LIMIT + " can not be used while registered in Tournament.");
		         return;
		      }
		
		if (item.isEquipable())
		{
			if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow() || (activeChar._inEventCTF && activeChar._haveFlagCTF && (item.getItem().getBodyPart() == Item.SLOT_LR_HAND || item.getItem().getBodyPart() == Item.SLOT_L_HAND || item.getItem().getBodyPart() == Item.SLOT_R_HAND)))
			{
				if (activeChar._inEventCTF && activeChar._haveFlagCTF)
					activeChar.sendMessage("This item can not be equipped when you have the flag.");
				else
					activeChar.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case Item.SLOT_LR_HAND:
				case Item.SLOT_L_HAND:
				case Item.SLOT_R_HAND:
				{
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					// Don't allow weapon/shield equipment if a cursed weapon is equipped
					if (activeChar.isCursedWeaponEquipped())
						return;
					
					break;
				}
			}
			
			if (activeChar.isCursedWeaponEquipped() && item.getItemId() == 6408) // Don't allow to put formal wear
				return;
			
			if (activeChar.getFakeWeaponObjectId() > 0 && activeChar.isCursedWeaponEquipped())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
				return;
			}
			
			if (activeChar.isAttackingNow() && item.isFakeWeapon())
			{
				activeChar.sendMessage("You can't change weapon skin while attacking.");
				return;
			}
			
			if (activeChar.isAttackingNow()){
				ThreadPool.schedule(() -> {
					final ItemInstance itemToTest = activeChar.getInventory().getItemByObjectId(_objectId);
					if(itemToTest == null)
						return;

					activeChar.useEquippableItem(itemToTest, false);
				}, activeChar.getAttackEndTime() - System.currentTimeMillis());
			}
			else
				if (item.isFakeWeapon())
				{
					if (activeChar.getFakeWeaponObjectId() == item.getObjectId())
					{
						activeChar.setFakeWeaponObjectId(0);
						activeChar.setFakeWeaponItemId(0);
						
						for (int s : FAKE_WEAPON_SKILLS)
						{
							final L2Skill skill = SkillTable.getInstance().getInfo(s, 1);
							if (skill != null)
							{
								activeChar.removeSkill(skill, false);
								activeChar.sendSkillList();
							}
						}
					}
					else
					{
						for (int s : FAKE_WEAPON_SKILLS)
						{
							final L2Skill skill = SkillTable.getInstance().getInfo(s, 1);
							if (skill != null)
							{
								activeChar.removeSkill(skill, false);
								activeChar.sendSkillList();
							}
						}

						activeChar.setFakeWeaponObjectId(item.getObjectId());
						activeChar.setFakeWeaponItemId(item.getItemId());
						
						if (activeChar.getFakeWeaponItemId() >= 30511 && activeChar.getFakeWeaponItemId() <= 30521)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(24502, 1);
							if (skill != null)
								activeChar.addSkill(skill, false);
						}
						
						if (activeChar.getFakeWeaponItemId() >= 30522 && activeChar.getFakeWeaponItemId() <= 30532)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(24503, 1);
							if (skill != null)
								activeChar.addSkill(skill, false);
						}
					}

					activeChar.broadcastUserInfo();
					activeChar.sendPacket(new ItemList(activeChar, false));
				}
				else
			{
				activeChar.useEquippableItem(item, true);
			}
		}
		else
		{
			if (activeChar.isCastingNow() && !(item.isPotion() || item.isElixir()))
				return;
			
			if (activeChar.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE)
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(activeChar, item, _ctrlPressed);
			
			for (Quest quest : item.getQuestEvents())
			{
				QuestState state = activeChar.getQuestState(quest.getName());
				if (state == null || !state.isStarted())
					continue;
				
				quest.notifyItemUse(item, activeChar, activeChar.getTarget());
			}
		}
	}
	public static final int[] FAKE_WEAPON_SKILLS =
	{
		24502,
		24503
	};
}