package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.events.TvT;
import net.sf.l2j.gameserver.handler.ITutorialHandler;
import net.sf.l2j.gameserver.handler.TutorialHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCustom;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMission;
import net.sf.l2j.gameserver.instancemanager.BotsPreventionManager;
import net.sf.l2j.gameserver.instancemanager.BotsPvPPreventionManager;
import net.sf.l2j.gameserver.model.actor.instance.ClassMaster;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.logging.Level;

import net.sf.l2j.Config;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	String _bypass;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_bypass.equalsIgnoreCase("close"))
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			return;
		}
		
		BotsPreventionManager.Link(player, _bypass);
		
		BotsPvPPreventionManager.Link(player, _bypass);
		TvT.Link(player, _bypass);

		AdminCustom.NewsLink(player, _bypass);
		AdminCustom.NewsLink2(player, _bypass);
		AdminCustom.NewsLink3(player, _bypass);
		AdminCustom.NewsLink4(player, _bypass);
		AdminCustom.NewsLink5(player, _bypass);
		ClassMaster.onTutorialLink(player, _bypass);
	    AdminCustom.onVIPLink(player, this._bypass);
		
	    if (Config.ACTIVE_MISSION)
	    	VoicedMission.linkMission(player, this._bypass); 
	    
		if (_bypass.startsWith("-h"))
		{
			_bypass = _bypass.substring(2);
			
			if (_bypass.startsWith("_"))
				_bypass = _bypass.substring(1);
		}

		final ITutorialHandler handler = TutorialHandler.getInstance().getHandler(_bypass);

		if (handler != null)
		{
			String command = _bypass;
			String params = "";
			if (_bypass.indexOf("_") != -1)
			{
				command = _bypass.substring(0, _bypass.indexOf("_"));
				params = _bypass.substring(_bypass.indexOf("_")+1, _bypass.length());
			}
			handler.useLink(command, player, params);
		}
		else
		{
			if (Config.DEBUG)
				_log.log(Level.WARNING, getClient() + " sent not handled RequestTutorialLinkHtml: [" + _bypass + "]");
		}
		
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent(_bypass, null, player);
	}
}