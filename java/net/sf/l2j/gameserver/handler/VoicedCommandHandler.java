/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Repair;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedAutoPotion;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedAutofarm;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedBanking;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedBossSpawn;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedCastles;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedClanNotice;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedColor;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedDonate;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedEnchant;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedEvent;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedLuckyDice;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMission;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedPassword;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedPlayersCont;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedRanking;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedReport;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedStatus;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedTrySkin;

import Dev.Community.MarketPlace.CommandMarketPlace;
import Dev.Event.BossEvent.KTBConfig;
import Dev.Event.BossEvent.VoicedEventKTB;
import Dev.Event.DeathMatch.DMConfig;
import Dev.Event.DeathMatch.VoicedDMEvent;
import Dev.Event.TvT.TvTConfig;
import Dev.Event.TvT.VoicedTvTEvent;
import Dev.Event.TvTFortress.FOSConfig;
import Dev.Event.TvTFortress.VoicedFOSEvent;

public class VoicedCommandHandler
{
	private final Map<Integer, IVoicedCommandHandler> _datatable = new HashMap<>();
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected VoicedCommandHandler()
	{
		if(Config.ENABLE_AUTO_FARM_COMMAND)
		{
			registerHandler(new VoicedAutofarm());	
		}
		if (TvTConfig.ALLOW_TvT_COMMANDS)
		{
			registerHandler(new VoicedTvTEvent());
		}
    	if(KTBConfig.ALLOW_EVENT_KTB_COMMANDS)
    	{
    		registerHandler(new VoicedEventKTB());	
    	}
		if(DMConfig.ALLOW_DM_COMMANDS)
		{
			registerHandler(new VoicedDMEvent());
		}
		if(Config.ENABLE_AUCTION_COMMUNITY)
		{
			registerHandler(new CommandMarketPlace());
		}
		if(FOSConfig.ALLOW_TVTFOS_COMMANDS)
		{
			registerHandler(new VoicedFOSEvent());
		}
		registerHandler(new VoicedAutoPotion());
		registerHandler(new VoicedPassword());
		registerHandler(new VoicedEnchant());
		
		registerHandler(new VoicedTrySkin());
		registerHandler(new VoicedRanking());
		registerHandler(new VoicedReport());
		registerHandler(new VoicedMenu());
		registerHandler(new VoicedBossSpawn());
		registerHandler(new VoicedCastles());
		registerHandler(new VoicedClanNotice());
		registerHandler(new VoicedColor());
		registerHandler(new VoicedEvent());
		registerHandler(new VoicedPlayersCont());
		registerHandler(new VoicedBanking());
		registerHandler(new VoicedDonate());
		registerHandler(new Repair());		
		registerHandler(new VoicedMission());
		registerHandler(new VoicedLuckyDice());
		registerHandler(new VoicedStatus());	
	}
	
	public void registerHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		
		for (int i = 0; i < ids.length; i++)
			_datatable.put(ids[i].hashCode(), handler);
	}
	
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		
		if (voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}
