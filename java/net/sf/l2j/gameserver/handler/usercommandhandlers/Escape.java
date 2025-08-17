package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.events.CTF;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSEvent;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isSitting() || activeChar.isMovementDisabled() || activeChar.isOutOfControl() || activeChar.isInOlympiadMode() || activeChar.isInObserverMode() || activeChar.isFestivalParticipant() || activeChar.isInJail())
		{
			activeChar.sendPacket(SystemMessageId.NO_UNSTUCK_PLEASE_SEND_PETITION);
			return false;
		}
		if (!DMEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
			return false;
		}
		if (KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || FOSEvent.isPlayerParticipant(activeChar.getObjectId()) && FOSEvent.isStarted() || (CTF.is_started() && activeChar._inEventCTF) || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted())
		{
			activeChar.sendMessage("You may not use an escape skill in event.");
			return false;
		}
		if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
			return false;
		}
		if (!FOSEvent.onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendMessage("Your current state doesn't allow you to use the /unstuck command.");
			return false;
		}
		if (activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.TOURNAMENT))
		{
			activeChar.sendMessage("You cannot use this skill in Tournament Event/Zone.");
			return false;
		}
		
		activeChar.stopMove(null);
		
		// Official timer 5 minutes, for GM 1 second
		if (activeChar.isGM())
			activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
		else
		{
			activeChar.sendPacket(new PlaySound("systemmsg_e.809"));
			activeChar.doCast(SkillTable.getInstance().getInfo(2099, 1));
			activeChar.sendMessage("You use Escape: 30 secunds");
		}
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}