package Dev.AutoFarm;

import net.sf.l2j.events.CTF;
import net.sf.l2j.events.TvT;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.tutorialhandlers.Autofarm;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.instance.Chest;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.type.ActionType;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import Dev.Event.BossEvent.KTBEvent;

public class AutofarmPlayerRoutine
{
	private final Player player;
	private Creature committedTarget = null;

	public AutofarmPlayerRoutine(Player player)
	{
		this.player = player;
	}

	public void executeRoutine()
	{
		if (player.isNoBuffProtected() && player.getAllEffects().length <= 8)
		{
			player.sendMessage("You don't have buffs to use autofarm.");
			AutofarmManager.INSTANCE.stopFarm(player);
			player.setAutoFarm(false);
			Autofarm.showAutoFarm(player);
			player.broadcastUserInfo();
			//AutoFarmCBBypasses.showAutoFarmBoard(player, "AutoFarm");
			return;
		}
		if (Config.ENABLE_DUALBOX_AUTOFARM)
		{
			AutoFarmIP(player, Config.NUMBER_BOX_IP_AUTOFARM, true);
		}
		if(player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.BOSS) || player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.RAID) || player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.BOSS) || TvT.is_started() && player._inEventTvT || CTF.is_started() && player._inEventCTF || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || player.isArenaAttack() || player.isInsideZone(ZoneId.PVP_CUSTOM))
	//	if(player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.BOSS) || player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.RAID) || player.getPvpFlag() > 0 && player.isInsideZone(ZoneId.BOSS) || TvT.is_started() && player._inEventTvT || CTF.is_started() && player._inEventCTF || player.isArenaAttack() || player.isInsideZone(ZoneId.PVP_CUSTOM))
		{
			player.sendMessage("You don't events to use autofarm or Flag.");
			AutofarmManager.INSTANCE.stopFarm(player);
			player.setAutoFarm(false);
			Autofarm.showAutoFarm(player);
			player.broadcastUserInfo();
			return;
		}
		if(Config.NO_USE_FARM_IN_PEACE_ZONE)
    	{
    		if(player.isInsideZone(ZoneId.PEACE))
    		{
    			player.sendMessage("No Use Auto farm in Peace Zone.");
    			AutofarmManager.INSTANCE.stopFarm(player);
    			player.setAutoFarm(false);
    			Autofarm.showAutoFarm(player);
    			player.broadcastUserInfo();
    			return;
    		}
    	}
		//Nao executar auto farm estando ja morto.
		if(player.isDead())
		{
			player.setAutoFarm(false);
			Autofarm.showAutoFarm(player);
			AutofarmManager.INSTANCE.stopFarm(player);
			player.broadcastUserInfo();
			return;
		}
		Monster monster = player.getTarget() instanceof Monster ? (Monster) player.getTarget() : null;
		if (monster != null && !GeoEngine.getInstance().canSeeTarget(player, monster))
		{
			monster = null;
			player.setTarget(monster);
		}
		if(!player.isMoving()) //Fix cancelar  funsoes quando usar click movimento.
		{
			assistParty();
			checkSpoil();
			targetEligibleCreature();
			attack();
			useAppropriateSpell();	
			checkManaPots();
			checkHealthPots();
		}
		
	}
	public boolean AutoFarmIP(Player activeChar, Integer numberBox, Boolean forcedTeleport)
	{
		if (CheckAutoFarmMetodo(activeChar, numberBox))
		{
			if (forcedTeleport)
			{
				activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS","Allowed only " + numberBox + " Client in Auto Farm!"));
				for (Player allgms : AdminData.getInstance().getAllGms(true))
				{
					if (!activeChar.isGM())
						allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double HWID]", activeChar.getName() +" in Auto Farm!"));
				}
				player.setAutoFarm(false);
				Autofarm.showAutoFarm(player);
				AutofarmManager.INSTANCE.stopFarm(player);
				player.broadcastUserInfo();
				return false;
			}
			return true;
		}
		return false;
	}
	private static boolean CheckAutoFarmMetodo(Player activeChar, Integer numberBox)
	{
		Map<String, List<Player>> map = new HashMap<>();
		
		if (activeChar != null)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				//	if (!player.isInsideZone(ZoneId.BOSS) || player.getClient().getConnection().getInetAddress().getHostAddress() == null)
				if (!player.isAutoFarm() || player.getHWID() == null)
					continue;
				String ip1 = activeChar.getHWID();
				String ip2 = player.getHWID();
				
				if (ip1.equals(ip2))
				{
					if (map.get(ip1) == null)
						map.put(ip1, new ArrayList<Player>());
					
					map.get(ip1).add(player);
					
					if (map.get(ip1).size() > numberBox)
						return true;
				}
			}
		}
		return false;
	}
	private void assistParty()
	{
		if(player.isInParty() && player.isAssistParty())
			if(!player.isPartyLeader())
				if (player.isInsideRadius(player.getParty().getLeader(), 1000, false, false))
				{
					if (player.getTarget() == null)
						player.getAI().setIntention(CtrlIntention.FOLLOW, player.getParty().getLeader());
					player.setTarget(player.getParty().getLeader().getTarget());
				}
	}

	private void checkHealthPots()
	{
		if (getHpPercentage() <= AutofarmConstants.useHpPotsPercentageThreshold)
		{
			if (player.getFirstEffect(AutofarmConstants.hpPotSkillId) != null)
			{
				return;
			}
			
			ItemInstance hpPots = player.getInventory().getItemByItemId(AutofarmConstants.hpPotItemId);
			if (hpPots != null)
			{
				useItem(hpPots);
			}
		}
	}
	
	private void checkManaPots()
	{
		
		if (getMpPercentage() <= AutofarmConstants.useMpPotsPercentageThreshold)
		{
			ItemInstance mpPots = player.getInventory().getItemByItemId(AutofarmConstants.mpPotItemId);
			if (mpPots != null)
			{
				useItem(mpPots);
			}
		}
	}
	
	private Double getMpPercentage()
	{
		return player.getCurrentMp() * 100.0f / player.getMaxMp();
	}
/*	private void attack() 
	{
		Boolean shortcutsContainAttack = shotcutsContainAttack();
		
		if (shortcutsContainAttack && player.getTarget() instanceof Monster) 
			physicalAttack();
	}*/
	private void attack() 
	{
		// Verifica se o jogador tem um alvo
		if (player.getTarget() instanceof Monster)
		{
			Monster target = (Monster) player.getTarget();
			
			// Verifica se o monstro é auto-atacável e o jogador pode vê-lo
			if (target.isAutoAttackable(player)) 
			{
				// Inicia o ataque físico se o monstro for auto-atacável e o jogador puder vê-lo
				physicalAttack();
			}
		}
	}

	private void useAppropriateSpell() 
	{
		L2Skill chanceSkill = nextAvailableSkill(getChanceSpells(), AutofarmSpellType.Chance);

		if (chanceSkill != null && player.getTarget() instanceof Monster && !(chanceSkill.getSkillType() == L2SkillType.SWEEP))
		{
			useMagicSkill(chanceSkill, false);
			return;
		}

		L2Skill lowLifeSkill = nextAvailableSkill(getLowLifeSpells(), AutofarmSpellType.LowLife);

		if (lowLifeSkill != null && !(lowLifeSkill.getSkillType() == L2SkillType.SWEEP)) 
		{
			useMagicSkill(lowLifeSkill, true);
			return;
		}

		L2Skill attackSkill = nextAvailableSkill(getAttackSpells(), AutofarmSpellType.Attack);

		if (attackSkill != null && player.getTarget() instanceof Monster && !(attackSkill.getSkillType() == L2SkillType.SWEEP)) 
		{
			useMagicSkill(attackSkill, false);
			return;
		}
	}

	/*public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType) 
	{
		for (Integer skillId : skillIds) 
		{
			L2Skill skill = player.getSkill(skillId);

			if (skill == null) 
				continue;
			
			if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
				continue;

			if (!player.checkDoCastConditions(skill)) 
				continue;
			
			if (isSpoil(skillId))
			{
				if (monsterIsAlreadySpoiled())
				{
					continue;
				}
				return skill;
			}
			if (spellType == AutofarmSpellType.Chance && getMonsterTarget() != null)
			{
				
				if (getMonsterTarget().getFirstEffect(skillId) == null) 
				{
					return skill;
				}
				continue;
			}

			if (spellType == AutofarmSpellType.LowLife && getHpPercentage() > player.getHealPercent()) 
				break;

			return skill;
		}

		return null;
	}*/
	public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType) 
	{
	    // Usado para registrar as habilidades já utilizadas
	    List<Integer> availableSkills = new ArrayList<>(skillIds);

	    // Loop até encontrar uma habilidade disponível
	    while (!availableSkills.isEmpty())
	    {
	        // Seleciona uma habilidade aleatória da lista disponível
	        int randomIndex = (int) (Math.random() * availableSkills.size());
	        Integer selectedSkillId = availableSkills.get(randomIndex);
	        
	        L2Skill skill = player.getSkill(selectedSkillId);

	        if (skill == null) 
	        {
	            availableSkills.remove(randomIndex); // Remove se a habilidade não for encontrada
	            continue;
	        }

	        // Ignorar habilidades do tipo SIGNET ou SIGNET_CASTTIME
	        if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
	        {
	            availableSkills.remove(randomIndex); // Remove essas habilidades
	            continue;
	        }

	        // Verificar se as condições de cast estão ok
	        if (!player.checkDoCastConditions(skill)) 
	        {
	            availableSkills.remove(randomIndex); // Remove habilidades que não podem ser lançadas
	            continue;
	        }

	        // Verificar se a habilidade pode ser usada para o spoil
	        if (isSpoil(selectedSkillId))
	        {
	            if (monsterIsAlreadySpoiled())
	            {
	                availableSkills.remove(randomIndex); // Remove se o monstro já foi spoileado
	                continue;
	            }
	            return skill;
	        }

	        // Para o tipo "Chance", verificar se o monstro alvo já tem o efeito
	        if (spellType == AutofarmSpellType.Chance && getMonsterTarget() != null)
	        {
	            if (getMonsterTarget().getFirstEffect(selectedSkillId) == null) 
	            {
	                return skill;
	            }
	            availableSkills.remove(randomIndex); // Remove se já tiver o efeito
	            continue;
	        }

	        // Para o tipo "LowLife", verificar a porcentagem de HP
	        if (spellType == AutofarmSpellType.LowLife && getHpPercentage() > player.getHealPercent()) 
	            break;

	        // Se passou por todas as verificações, usa a habilidade
	        availableSkills.remove(randomIndex); // Remove da lista de habilidades disponíveis após o uso
	        return skill;
	    }

	    // Se todas as habilidades foram usadas, reinicia a lista
	    return null;
	}
	private void checkSpoil()
	{
		if (canBeSweepedByMe() && getMonsterTarget().isDead())
		{
			L2Skill sweeper = player.getSkill(42);
			if (sweeper == null)
				return;
			
			useMagicSkill(sweeper, false);
		}
	}
	private boolean canBeSweepedByMe()
	{
		return getMonsterTarget() != null && getMonsterTarget().isDead() && getMonsterTarget().getSpoilerId() == player.getObjectId();
	}
	
	private boolean monsterIsAlreadySpoiled()
	{
		return getMonsterTarget() != null && getMonsterTarget().getSpoilerId() != 0;
	}
	
	private static boolean isSpoil(Integer skillId)
	{
		return skillId == 254 || skillId == 302;
	}
	private Double getHpPercentage() 
	{
		return player.getCurrentHp() * 100.0f / player.getMaxHp();
	}

	private List<Integer> getAttackSpells()
	{
		return getSpellsInSlots(AutofarmConstants.attackSlots);
	}

	private List<Integer> getSpellsInSlots(List<Integer> attackSlots) 
	{
		return Arrays.stream(player.getAllShortCuts()).filter(shortcut -> shortcut.getPage() == player.getPage() && shortcut.getType() == L2ShortCut.TYPE_SKILL && attackSlots.contains(shortcut.getSlot())).map(L2ShortCut::getId).collect(Collectors.toList());
	}

	private List<Integer> getChanceSpells()
	{
		return getSpellsInSlots(AutofarmConstants.chanceSlots);
	}

	private List<Integer> getLowLifeSpells()
	{
		return getSpellsInSlots(AutofarmConstants.lowLifeSlots);
	}
	
//	private boolean shotcutsContainAttack() 
//	{
//		//return Arrays.stream(player.getShortcutList().getShortcuts()).anyMatch(shortcut -> /*shortcut.getPage() == 0 && shortcut.getType() == ShortcutType.ACTION &&*/ shortcut.getId() == 2);
//		return Arrays.stream(player.getAllShortCuts()).anyMatch(shortcut -> shortcut.getPage() == player.getPage() && shortcut.getType() == L2ShortCut.TYPE_ACTION && shortcut.getId() == 2);
//	}

	private void castSpellWithAppropriateTarget(L2Skill skill, Boolean forceOnSelf)
	{
		if (forceOnSelf) 
		{
			WorldObject oldTarget = player.getTarget();
			player.setTarget(player);
			player.useMagic(skill, false, false);
			player.setTarget(oldTarget);
			return;
		}

		player.useMagic(skill, false, false);
	}

	private void physicalAttack()
	{
		if(!(player.getTarget() instanceof Monster)) 
		{
			return;
		}

		Monster target = (Monster)player.getTarget();
		if (!player.isMageClass())
		if (target.isAutoAttackable(player) && GeoEngine.getInstance().canSeeTarget(player, target))
		{
			if (GeoEngine.getInstance().canSeeTarget(player, target))
			{
				player.getAI().setIntention(CtrlIntention.ATTACK, target);
			}
		}
		else
		{
			if (target.isAutoAttackable(player) && GeoEngine.getInstance().canSeeTarget(player, target))
			if (!GeoEngine.getInstance().canSeeTarget(player, target))
				player.getAI().setIntention(CtrlIntention.FOLLOW, target);
		}
	}

	public void targetEligibleCreature() 
	{ 
		if (committedTarget != null && !(committedTarget != null)) 
		{            
			if(committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(player, committedTarget))
			{
				if(player.getClassId() == ClassId.SCAVENGER || (player.getClassId() == ClassId.BOUNTY_HUNTER) || (player.getClassId() == ClassId.FORTUNE_SEEKER))
				{
					ThreadPool.schedule(() ->committedTarget = null, 1000);
					ThreadPool.schedule(() ->player.setTarget(null), 1000);
					ThreadPool.schedule(() -> attack(), 1000);
					return;
				}
				committedTarget = null;
				player.setTarget(null);
				attack();
				return;
			}
			else if(!committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(player, committedTarget))
			{
				attack();
				return;
			}
			player.getAI().setIntention(CtrlIntention.FOLLOW, committedTarget);
			committedTarget = null;
			player.setTarget(null);
		}
		
		if (committedTarget instanceof Summon) 
			return;
		
	//	if (!(committedTarget instanceof L2MonsterInstance) && committedTarget != null) 
	//	{
	//		committedTarget = null;
	//		return;
	//	}
			
			
		List<Monster> targets = getKnownMonstersInRadius(player, player.getRadius(), creature -> GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !creature.isDead() && !(creature instanceof Chest) && !(creature instanceof RaidBoss) && !creature.isAgathion() && !(creature instanceof GrandBoss) && !(creature.isRaidMinion()) && !(player.isAntiKsProtected() && creature.getTarget() != null && creature.getTarget() != player && creature.getTarget() != player.getPet()));
		
		if (targets.isEmpty())
			return;

		Monster closestTarget = targets.stream().min((o1, o2) -> Integer.compare((int) Math.sqrt(player.getDistanceSq(o1)), (int) Math.sqrt(player.getDistanceSq(o2)))).get();

		committedTarget = closestTarget;
		player.setTarget(closestTarget);
		
	}

	public final static List<Monster> getKnownMonstersInRadius(Player player, int radius, Function<Monster, Boolean> condition)
	{
		final WorldRegion region = player.getRegion();
		if (region == null)
			return Collections.emptyList();

		final List<Monster> result = new ArrayList<>();

		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (!(obj instanceof Monster) || !MathUtil.checkIfInRange(radius, player, obj, true) || !condition.apply((Monster) obj))
					continue;

				result.add((Monster) obj);
			}
		}

		return result;
	}

	public Monster getMonsterTarget()
	{
		if(!(player.getTarget() instanceof Monster)) 
		{
			return null;
		}

		return (Monster)player.getTarget();
	}
	
	

	private void useMagicSkill(L2Skill skill, Boolean forceOnSelf)
	{
		if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (skill.isToggle() && player.isMounted())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

	//	if (player.isOutOfControl())
	//	{
	//		player.sendPacket(ActionFailed.STATIC_PACKET);
	//		return;
	//	}

		if (player.isAttackingNow())
			//player.getAI().setNextAction(new NextAction(AiEventType.EVT_READY_TO_ACT, CtrlIntention.CAST, () -> castSpellWithAppropriateTarget(skill, forceOnSelf)));
			  player.getAI().setNextAction(new NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.CAST, () -> castSpellWithAppropriateTarget(skill, forceOnSelf)));
			else 
			castSpellWithAppropriateTarget(skill, forceOnSelf);
	}

	public void useItem(ItemInstance item)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			return;
		}

		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}

		if (item == null)
			return;

		if (item.getItem().getType2() == Item.TYPE2_QUEST)
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}

		if (player.isAlikeDead() || player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAfraid())
			return;

		if (!Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
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

		if (player.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot)
		{
			player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if (item.isPetItem())
		{
			if (!player.hasPet())
			{
				player.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}

			final Pet pet = ((Pet) player.getPet());

			if (!pet.canWear(item.getItem()))
			{
				player.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}

			if (pet.isDead())
			{
				player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}

			if (!pet.getInventory().validateCapacity(item))
			{
				player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}

			if (!pet.getInventory().validateWeight(item, 1))
			{
				player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}

			player.transferItem("Transfer", item.getObjectId(), 1, pet.getInventory(), pet);

			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
			}
			else
			{
				pet.getInventory().equipPetItem(item);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
			}

			player.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}

		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(player, player, true))
				return;
		}

		if (item.isEquipable())
		{
			if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			{
				player.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				return;
			}

			switch (item.getItem().getBodyPart())
			{
			     case Item.SLOT_LR_HAND:
		       	 case Item.SLOT_L_HAND:
			     case Item.SLOT_R_HAND:
			     {
				       if (player.isMounted())
				       {
					       player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
					       return;
				       }

				       if (player.isCursedWeaponEquipped())
					       return;

				       break;
			     }
			}

			if (player.isCursedWeaponEquipped() && item.getItemId() == 6408)
				return;

			if (player.isAttackingNow())
				ThreadPool.schedule(() ->
				{
					final ItemInstance itemToTest = player.getInventory().getItemByObjectId(item.getObjectId());
					if (itemToTest == null)
						return;

					player.useEquippableItem(itemToTest, false);
				}, player.getAttackEndTime() - System.currentTimeMillis());
			else
				player.useEquippableItem(item, true);
		}
		else
		{
			if (player.isCastingNow() && !(item.isPotion() || item.isElixir()))
				return;

			if (player.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE)
			{
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				player.broadcastUserInfo();

				player.sendPacket(new ItemList(player, false));
				return;
			}

			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(player, item, false);

			for (Quest quest : item.getQuestEvents())
			{
				QuestState state = player.getQuestState(quest.getName());
				if (state == null || !state.isStarted())
					continue;

				quest.notifyItemUse(item, player, player.getTarget());
			}
		}
	}
}