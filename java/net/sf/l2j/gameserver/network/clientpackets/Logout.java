package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSEvent;

public final class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getActiveEnchantItem() != null || player.isLocked())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if(FOSEvent.isStarted() && FOSEvent.isPlayerParticipant(player.getObjectId()) || FOSEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't logout during Event.");
			return;
		}
		// Check if player is in Event
		if (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()) || TvTEvent.isPlayerParticipant(player.getObjectId()) || player._inEventCTF || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || KTBEvent.isPlayerParticipant(player.getObjectId()) && !player.isGM())
		{
			player.sendMessage("You can't logout during Event.");
			return;
		}
		
//		if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE))
//			{
//				player.closeNetConnection(true);
//				return;
//			}
		
		if ((player.isInArenaEvent() || player.isArenaProtection()) && ArenaTask.is_started())
		{
			player.sendMessage("You cannot logout while in Tournament Event!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_RESTART) && !player.isGM() && !player.isInsideZone(ZoneId.PEACE))
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player) && !player.isGM())
		{
			player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if(DMEvent.isStarted() && DMEvent.isPlayerParticipant(player.getObjectId()) || DMEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't logout during Event.");
			return;
		}
		if (player.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized() && !player.isGM())
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		FOSEvent.onLogout(player);
		DMEvent.onLogout(player);
		KTBEvent.onLogout(player);
		player.removeFromBossZone();
		player.logout();
	}
}