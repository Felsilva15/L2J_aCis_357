package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSEvent;

public final class RequestRestart extends L2GameClientPacket
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
		
		if (player.getActiveEnchantItem() != null || player.isLocked() || player.isInStoreMode())
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		if(DMEvent.isStarted() && DMEvent.isPlayerParticipant(player.getObjectId()) || DMEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't restart during Event.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		if(FOSEvent.isStarted() && FOSEvent.isPlayerParticipant(player.getObjectId()) || FOSEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't restart during Event.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		if (player._inEventCTF || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || KTBEvent.isPlayerParticipant(player.getObjectId()) && !player.isGM())
		{
			player.sendMessage("You can't restart during Event.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		if(TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(player.getObjectId()) || TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't logout during Event.");
			return;
		}
		if ((player.isInArenaEvent() || player.isArenaProtection()) && ArenaTask.is_started())
		{
			player.sendMessage("You cannot restart while in Tournament Event!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_RESTART) && !player.isGM() && !player.isInsideZone(ZoneId.PEACE))
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player) && !player.isGM())
		{
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized() && !player.isGM())
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		player.removeFromBossZone();
		
		// delete box from the world
		if (player._active_boxes != -1)
			player.decreaseBoxes();
		
		final L2GameClient client = getClient();
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		// removing player from the world
		player.deleteMe();
		
		client.setActiveChar(null);
		client.setState(GameClientState.AUTHED);
		
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}