package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.logging.Level;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.events.CTF;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.data.xml.SummonItemData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.AgathionNpc;
import net.sf.l2j.gameserver.model.actor.instance.ChristmasTree;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSEvent;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		if (!DMEvent.onItemSummon(playable.getObjectId()) || !DMEvent.onItemSummon(playable.getObjectId()) || TvTEvent.onItemSummon(playable.getObjectId()) )
 			return;
		if (activeChar.isInObserverMode())
			return;
		
		if (activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.TOURNAMENT))
		{
			activeChar.sendMessage("You can not do this in Tournament Event");
			return;
		}
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;
		
		final IntIntHolder sitem = SummonItemData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.getValue() > 0)
		{
			activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return;
		}
		if (!KTBEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in KTB Event");
			return;
		}
		if (!DMEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in DM Event");
			return;
		}
		if (!FOSEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in FOS Event");
			return;
		}
		if (CTF.is_started() && activeChar._inEventCTF && !Config.CTF_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			activeChar.sendPacket(af);
			return;
		}

		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		final int npcId = sitem.getId();
		if (npcId == 0)
			return;
		
		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		activeChar.stopMove(null);
		
		switch (sitem.getValue())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					for (ChristmasTree ch : activeChar.getKnownTypeInRadius(ChristmasTree.class, 1200))
					{
						if (npcTemplate.getNpcId() == ChristmasTree.SPECIAL_TREE_ID)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return;
						}
					}
					
					if (sitem.getId() == 5560 || sitem.getId() == 5561)
					{
						if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false))
						{
							final L2Spawn spawn = new L2Spawn(npcTemplate);
							spawn.setLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
							spawn.setRespawnState(false);
							
							final Npc npc = spawn.doSpawn(true);
							npc.setTitle(activeChar.getName());
							npc.setIsRunning(false); // broadcast info
						}
					}
					else
					{
						
						if ((activeChar.getAgathionId() > 0))
						{
							activeChar.unSummonAgathion();
							activeChar.sendMessage("Agathion is Disabled.");
							activeChar.lostAgathionSkills();
							activeChar.setActiveAgathion(false);
						}
						else
						{
							final L2Spawn spawn = new L2Spawn(npcTemplate);
						//	spawn.setLoc(activeChar.getX() + 25, activeChar.getY() + 35, activeChar.getZ() + 20, -1);
							final double angle = Math.toRadians(Rnd.get(60, 120)) * (Rnd.nextBoolean() ? 1 : -1); // Apenas lados (esquerdo ou direito)
							final int offsetX = (int) (70 * Math.cos(angle)); // Distância horizontal para atrás
							final int offsetY = (int) (70 * Math.sin(angle)); // Distância vertical para atrás
							spawn.setLoc(activeChar.getX() + offsetX, activeChar.getY() + offsetY, activeChar.getZ() + 20, -1);
							
							final Npc npc = spawn.doSpawn(true);
							if (npc instanceof AgathionNpc)
							{
								((AgathionNpc) npc).broadcastNpcInfo(2);
								npc.setShowSummonAnimation(true);
								((AgathionNpc) npc).setOwner(activeChar);
								npc.setIsInvul(true);
								npc.setRunning();
								activeChar.setAgathion(((AgathionNpc) npc));
								activeChar.setAgathionId(npc.getNpcId());
								((AgathionNpc) npc).startAgathionTask();
								((AgathionNpc) npc).setTargetable(false);
								((AgathionNpc) npc).setFollowStatus(true);
								activeChar.setActiveAgathion(true);
								activeChar.rewardAgathionSkills();
								activeChar.sendMessage("Agathion is Enabled.");
							}
						}	
					}
				}
				
				catch (Exception e)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				final WorldObject oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(GaugeColor.BLUE, 5000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				activeChar.setIsCastingNow(true);
				
				ThreadPool.schedule(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getId(), item.getObjectId(), true);
				break;
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final ItemInstance _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(Player activeChar, NpcTemplate npcTemplate, ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);
				
				// check for summon item validity
				if (_item == null || _item.getOwnerId() != _activeChar.getObjectId() || _item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
					return;
				
				// Owner has a pet listed in world.
				if (World.getInstance().getPet(_activeChar.getObjectId()) != null)
					return;
				
				// Add the pet instance to world.
				final Pet pet = Pet.restore(_item, _npcTemplate, _activeChar);
				if (pet == null)
					return;
				
				World.getInstance().addPet(_activeChar.getObjectId(), pet);
				
				_activeChar.setPet(pet);
				
				pet.setRunning();
				pet.setTitle(_activeChar.getName());
				pet.spawnMe();
				pet.startFeed();
				pet.setFollowStatus(true);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
}
