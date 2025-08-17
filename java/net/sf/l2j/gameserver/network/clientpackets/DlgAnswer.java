package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.events.CTF;
import net.sf.l2j.events.TvT;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRecallAll;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedEvent;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;

import net.sf.l2j.Config;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.SoloBossEvent.SoloBoss;
import Dev.Event.TvTFortress.FOSEvent;

/**
 * @author Dezmond_snz Format: cddd
 */
public final class DlgAnswer extends L2GameClientPacket
{
	private int _messageId;
	private int _answer;
	private int _requesterId;
	
	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requesterId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_messageId == SystemMessageId.RESSURECTION_REQUEST_BY_S1.getId() || _messageId == SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED.getId())
			activeChar.reviveAnswer(_answer);
		else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			if (AdminRecallAll.isAdminSummoning == true && this._answer == 1) {
				activeChar.teleToLocation(AdminRecallAll.x, AdminRecallAll.y, AdminRecallAll.z, 250);
			}else {
				activeChar.teleportAnswer(this._answer, this._requesterId);
			} 
		}
		else if (Announcement.isSummoning == true && this._answer == 1) {
			activeChar.teleToLocation(Config.Tournament_locx, Config.Tournament_locy, Config.Tournament_locz, 125);
		} else if (Announcement.pvp_register == true && _answer == 1) {
			activeChar.teleToLocation(Config.pvp_locx, Config.pvp_locy, Config.pvp_locz, 125);
		}
		else if (_messageId == SystemMessageId.EVENT.getId())
		{
			if (Announcement.tvt_register == true && _answer == 1)
				VoicedEvent.JoinTvT(activeChar);
		}
		else if (SoloBoss.boss_teleporter == true && _answer == 1)
		{
			if (activeChar.isOff() || activeChar.isOffShop() || FOSEvent.isPlayerParticipant(activeChar.getObjectId()) && FOSEvent.isStarted() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() ||  CTF.is_started() && activeChar._inEventCTF || activeChar._inEventCTF || TvT.is_started() && activeChar._inEventTvT || activeChar._inEventTvT  || activeChar.isAio() || activeChar.isInsideZone(ZoneId.PVP_CUSTOM) || activeChar.isDead()  /*KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted()*/ || activeChar.isAlikeDead() || activeChar.isInArenaEvent() || activeChar.isArenaProtection() || activeChar.isOlympiadProtection() || activeChar.isInStoreMode() || activeChar.isRooted() || activeChar.getKarma() > 0 || activeChar.isInOlympiadMode() || activeChar.isFestivalParticipant() || activeChar.isArenaAttack() || activeChar.isInsideZone(ZoneId.BOSS) || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.PVP_CUSTOM) || activeChar.isInsideZone(ZoneId.SIEGE) || activeChar.isInsideZone(ZoneId.TOURNAMENT))
			{
				return;
			}
			activeChar.teleToLocation(Config.SOLO_BOSS_ID_ONE_LOC[0], Config.SOLO_BOSS_ID_ONE_LOC[1], Config.SOLO_BOSS_ID_ONE_LOC[2], 825);
		}	
		else if (_messageId == 1983 && Config.ALLOW_WEDDING)
			activeChar.engageAnswer(_answer);
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
			activeChar.activateGate(_answer, 1);
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
			activeChar.activateGate(_answer, 0);
	}
}