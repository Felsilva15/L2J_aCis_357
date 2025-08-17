package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Party.MessageType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import Dev.InstanceFarm.TimeInstanceManager;
import Dev.InstanceFarm.TimeInstanceRemainTaskManager;
import Dev.InstanceFarm.TimeInstanceTeleportTaskManager;

public class L2TimeInstanceZone extends L2SpawnZone
{	
	public L2TimeInstanceZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.TIME_FARM, true);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			if (Config.TIME_INSTANCE_BLOCK_CLASS_LIST.contains(activeChar.getClassId().getId()))
			{
				activeChar.sendMessage("Your class is not allowed in Time Instance Zone.");
				activeChar.teleToLocation(81337, 148093, -3473, 0);
				return;
			}
			if (activeChar.getMountType() == 1 || activeChar.getMountType() == 2)
			{
				ThreadPool.schedule(() -> {
					activeChar.sendMessage("Desmounted Pet In Time Zone!");
					activeChar.broadcastUserInfo();
					// Se quiser desmontar o player, adicione aqui o método, ex:
					activeChar.dismount();
				}, 2000);
				
			}
			if (!TimeInstanceManager.checkPlayerTime(activeChar))
			{
				activeChar.setTimeInstanceAvaiable(false);
				activeChar.setIsInTimeInstance(false);
				activeChar.sendMessage("Your time in the Time Instance Zone has expired.");
				
				if (Config.TIME_INSTANCE_SCREEN_MESSAGE)
					activeChar.sendPacket(new ExShowScreenMessage("Your time in the Time Instance Zone has expired.", 6000));
				
				activeChar.setTimeInstanceMobs(0);
				
				activeChar.startAbnormalEffect(0x0800);
				activeChar.setIsParalyzed(true);
				activeChar.startParalyze();				
				activeChar.broadcastPacket(new StopMove(activeChar));
				
				new TimeInstanceTeleportTaskManager(activeChar);
			}
			else
			{
				activeChar.setIsInTimeInstance(true);
				activeChar.sendMessage("You have entered the Time Instance Zone.");
				
				if (Config.TIME_INSTANCE_SCREEN_MESSAGE)
					activeChar.sendPacket(new ExShowScreenMessage("You have " + TimeInstanceManager.getPlayerTime(activeChar) + " minutes left in Time Instance area.", 6000));
				
				if (!Config.TIME_INSTANCE_ALLOW_PARTY && activeChar.isInParty())
					activeChar.getParty().removePartyMember(activeChar, MessageType.Expelled);
				
				if (Config.TIME_INSTANCE_FLAG_ZONE)
				{
					if (activeChar.getPvpFlag() > 0)
						PvpFlagTaskManager.getInstance().remove(activeChar);
					
					activeChar.updatePvPFlag(1);
				}
				
				new TimeInstanceRemainTaskManager(activeChar);
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.TIME_FARM, false);
	

		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			activeChar.setIsInTimeInstance(false);
			activeChar.sendMessage("You left the Time Instance Zone.");
			
			if (Config.TIME_INSTANCE_FLAG_ZONE)
				PvpFlagTaskManager.getInstance().add(activeChar, 20000);
		}
	}
	
	@Override
	public void onDieInside(Creature character)
	{
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
	}
}