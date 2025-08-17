package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.events.CTF;
import net.sf.l2j.events.PartyZoneTask;
import net.sf.l2j.events.TvT;
import net.sf.l2j.events.manager.CTFEventManager;
import net.sf.l2j.events.manager.PvPEventNext;
import net.sf.l2j.events.manager.TvTEventManager;
import net.sf.l2j.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.ArenaEvent;
import net.sf.l2j.gameserver.MissionReset;
import net.sf.l2j.gameserver.PartyFarmEvent;
import net.sf.l2j.gameserver.Restart;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.DressMeData;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.DressMe;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.OpenUrl;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.gameserver.taskmanager.AutoGoldBar;

import Dev.Event.BossEvent.KTBConfig;
import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.BossEvent.KTBManager;
import Dev.Event.ChampionInvade.ChampionInvade;
import Dev.Event.ChampionInvade.InitialChampionInvade;
import Dev.Event.DeathMatch.DMConfig;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.DeathMatch.DMManager;
import Dev.Event.SoloBossEvent.InitialSoloBossEvent;
import Dev.Event.SoloBossEvent.SoloBoss;
import Dev.Event.TvTFortress.FOSConfig;
import Dev.Event.TvTFortress.FOSEvent;
import Dev.Event.TvTFortress.FOSManager;
import Dev.InstanceFarm.TimeInstanceManager;

public class VoicedMenu implements IVoicedCommandHandler
{
	
	private static final String[] VOICED_COMMANDS =
	{
		"menu",
		"MENU",
		"partyon",
		"tradeon",
		"buffon",
		"messageon",
		"partyoff",
		"tradeoff",
		"buffoff",
		"messageoff",
		"setPartyRefuse",
		"setTradeRefuse",
		"setMessageRefuse",
		"setBuffProtection",
		"optimizeFPS",
		"info",
		"setAutoGb",
		"events",
		"bp_changedressmestatus",
		"mission",
		"info_url",
		"skins",
		"trySkin",
		"hair",
		"pin",
		"timeleft",
		"bp_changedressmestatus",
		"disable_Helm",
		"disable_skin",
		"setAutoGoldBar"
		
		
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		if (command.equals("menu") || command.equals("MENU"))
			showMenuHtml(activeChar);
		else if (command.equals("events") || command.equals("EVENTS"))
			showEventsTimes(activeChar);
		else if (command.equals("partyon"))
		{
			if (activeChar.isPartyInvProt())
				return false;
			
			activeChar.setIsPartyInvProt(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("timeleft") && Config.TIME_INSTANCE_SCREEN_MESSAGE)
		{
			//if (Config.TIME_INSTANCE_SCREEN_MESSAGE)
			//	activeChar.sendPacket(new ExShowScreenMessage("You have " + TimeInstanceManager.getPlayerTime(activeChar) + " minutes left in Time Instance area.", 6000));  
			activeChar.sendMessage("You have " + TimeInstanceManager.getPlayerTime(activeChar) + " minutes left in Time Instance area.");
		}
		else if (command.equals("setAutoGoldBar"))
		{  
			if (activeChar.isInAutoGoldBarMode())
				activeChar.setInAutoGoldBarMode(false);
			else
				activeChar.setInAutoGoldBarMode(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("skins"))
		{
			final DressMe dress = DressMeData.getInstance().getItemId(0);
			activeChar.setDress(dress);
			activeChar.broadcastUserInfo();
		}
		else if (command.equals("bp_changedressmestatus")) {
			if (activeChar.getDress() == null) {
				activeChar.sendMessage("you are not equipped with dressme");
			} else if (activeChar.isDressMeEnabled()) {
				activeChar.setDressMeEnabled(false);
				activeChar.broadcastUserInfo();
			} else {
				activeChar.setDressMeEnabled(true);
				activeChar.broadcastUserInfo();
			} 
			showMenuHtml(activeChar);
		}
		else if (command.equals("disable_Helm")){
			
			if(!activeChar.isVip())
			{
				activeChar.sendMessage("Exclusive command for Vip's");
			}
			
			DressMe dress = activeChar.getDress();
			
			if (activeChar.getDress() == null) {
				activeChar.sendMessage("you are not equipped with dressme.");
				return false;
			}
			if (activeChar.getDress() != null) {
				dress.setHairId(0);
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.equals("disable_skin")){
			
			if(!activeChar.isVip())
			{
				activeChar.sendMessage("Exclusive command for Vip's");
			}
			
			if (activeChar.getDress() == null) {
				activeChar.sendMessage("you are not equipped with dressme.");
				return false;
			}
			if (activeChar.getDress() != null) {
				activeChar.setDress(null);
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.equals("partyoff"))
		{
			if (!activeChar.isPartyInvProt())
				return false;
			
			activeChar.setIsPartyInvProt(false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("tradeon"))
		{
			if (activeChar.getTradeRefusal())
				return false;
			
			activeChar.setTradeRefusal(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("tradeoff"))
		{
			if (!activeChar.getTradeRefusal())
				return false;
			
			activeChar.setTradeRefusal(false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("messageon"))
		{
			if (activeChar.getMessageRefusal())
				return false;
			
			activeChar.setMessageRefusal(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("messageoff"))
		{
			if (!activeChar.getMessageRefusal())
				return false;
			
			activeChar.setMessageRefusal(false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("buffon"))
		{
			if (activeChar.isBuffProtected())
				return false;
			
			activeChar.useMagic(SkillTable.getInstance().getInfo(8000, 1), false, false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("buffoff"))
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(8000, 1), false, false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("setPartyRefuse"))
		{
			if (activeChar.isPartyInvProt())
				activeChar.setIsPartyInvProt(false);
			else
				activeChar.setIsPartyInvProt(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("setTradeRefuse"))
		{
			if (activeChar.getTradeRefusal())
				activeChar.setTradeRefusal(false);
			else
				activeChar.setTradeRefusal(true);
			showMenuHtml(activeChar);
		}
		else if (command.equals("optimizeFPS"))
		{
			if (activeChar.isOptimizeFPS()) {
				activeChar.setOptimizeFPS(false);
				activeChar.setDisableGlowWeapon(false);
				activeChar.broadcastUserInfoHiden();
				activeChar.broadcastUserInfo();
			} else {
				activeChar.setOptimizeFPS(true);
				activeChar.setDisableGlowWeapon(true);
				activeChar.broadcastUserInfoHiden();
				activeChar.broadcastUserInfo();
			} 
			showMenuHtml(activeChar);
		}
		else if (command.equals("setMessageRefuse"))
		{
			if (activeChar.getMessageRefusal())
				activeChar.setMessageRefusal(false);
			else
				activeChar.setMessageRefusal(true);
			
			showMenuHtml(activeChar);
		}
		else if (command.equals("setBuffProtection"))
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(8000, 1), false, false);
			showMenuHtml(activeChar);
		}
		else if (command.equals("info"))
		{
			showInfoHtml(activeChar);
		}
		else if (command.equals("mission"))
		{
			showMissionHtml(activeChar);
		}
		else if (command.startsWith("info_url")) {
			activeChar.sendPacket(new OpenUrl("" + Config.INFO_URL + ""));
		}
		else if (command.startsWith("hair")) {
			
			if(activeChar.getDress() == null){
				activeChar.sendMessage("You are not wearing a skin.");
				return false;
			}
			
			if (activeChar.getDress() != null){
				activeChar.getDress().setHairId(0);
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.equalsIgnoreCase("setAutoGb"))
		{
			if (activeChar.isAutoGb())
			{
				activeChar.setAutoGb(false);
				AutoGoldBar.getInstance().remove(activeChar);
			}
			else
			{
				activeChar.setAutoGb(true);
				AutoGoldBar.getInstance().add(activeChar);
			}
			VoicedMenu.showMenuHtml(activeChar);
		}
		
		else if (command.startsWith("trySkin")) {

		    // Remover a verificação VIP e permitir o comando para todos os jogadores.
		    
		    if (!activeChar.isInsideZone(ZoneId.TOWN)) {
		        activeChar.sendMessage("This command can only be used within a city.");
		        return false;
		    }
		    
		    if (activeChar.getDress() != null) {
		        activeChar.sendMessage("Wait, you are experiencing a skin.");
		        return false;
		    }
		    
		    StringTokenizer st = new StringTokenizer(command);
		    st.nextToken();  // Ignore o primeiro token, que é o comando 'trySkin'
		    int skinId = Integer.parseInt(st.nextToken());
		    
		    final DressMe dress = DressMeData.getInstance().getItemId(skinId);
		    final DressMe dress2 = DressMeData.getInstance().getItemId(0);
		    
		    if (dress != null) {
		        activeChar.setDress(dress);
		        activeChar.broadcastUserInfo();
		        ThreadPool.schedule(() -> {
		            activeChar.setDress(dress2);
		            activeChar.broadcastUserInfo();
		        }, 3000L);
		    }
		    else {
		        activeChar.sendMessage("Invalid skin.");
		        return false;
		    } 
		    
		    return true;
		}
		return false;
	}
	private static final String ACTIVED = "<font color=00FF00>ON</font>";
	private static final String DESATIVED = "<font color=FF0000>OFF</font>";
	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	
	public static void showEventsTimes(Player activeChar)
	{	
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/events.htm");
		html.replace("%name%", activeChar.getName());
		html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())) );
		html.replace("%time%", (new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis())) );
		if(Config.SOLO_BOSS_EVENT)
		{
			if(SoloBoss.is_started())
				html.replace("%bossSolo%", "In Progress");
			else	
				html.replace("%bossSolo%", InitialSoloBossEvent.getInstance().getRestartNextTime().toString() );
		}
		if(Config.CHAMPION_FARM_BY_TIME_OF_DAY)
		{
			if(ChampionInvade.is_started())
				html.replace("%champion%", "In Progress");
			else	
				html.replace("%champion%", InitialChampionInvade.getInstance().getRestartNextTime().toString() );
		}
		if(Config.PVP_EVENT_ENABLED){
			if(PvPEvent.getInstance().isActive())	
			html.replace("%pvp%", "In Progress");
			else	
			html.replace("%pvp%", PvPEventNext.getInstance().getNextTime().toString() );
		}
		if(Config.TVT_EVENT_ENABLED){
			if(TvT.is_inProgress())	
			html.replace("%tvt%", "In Progress");
			else
		    html.replace("%tvt%", TvTEventManager.getInstance().getNextTime().toString() );
		}
		if(DMConfig.DM_EVENT_ENABLED)
		{
			if(DMEvent.isStarted())	
				html.replace("%DMTime%", "In Progress");
			else
				html.replace("%DMTime%", DMManager.getInstance().getNextTime());
		}
		if(FOSConfig.FOS_EVENT_ENABLED)
		{
			if(FOSEvent.isStarted())	
				html.replace("%FOSTime%", "In Progress");
			else
				html.replace("%FOSTime%", FOSManager.getInstance().getNextTime());
		}
		if(KTBConfig.KTB_EVENT_ENABLED)
		{
			if(KTBEvent.isStarted())	
				html.replace("%ktbTime%", "In Progress");
			else
				html.replace("%ktbTime%", KTBManager.getInstance().getNextTime());
		}
		if(Config.CTF_EVENT_ENABLED){
			if(CTF.is_inProgress())	
			html.replace("%ctf%", "In Progress");
			else
			html.replace("%ctf%", CTFEventManager.getInstance().getNextTime().toString() );
		}
		if(Config.TOURNAMENT_EVENT_TIME){
			if(ArenaTask.is_started())	
			html.replace("%arena%", "In Progress");
			else	
			html.replace("%arena%", ArenaEvent.getInstance().getNextTime().toString() );
		}
		if(Config.ACTIVE_MISSION)
			html.replace("%mission%", MissionReset.getInstance().getNextTime().toString() );
		if(Config.RESTART_BY_TIME_OF_DAY)
			html.replace("%restart%", Restart.getInstance().getRestartNextTime().toString() );
		
		if(Config.START_AUTO_PARTY){
			if(PartyZoneTask.is_started())	
			html.replace("%partyfarm%", "In Progress");
			else	
			html.replace("%partyfarm%", PartyFarmEvent.getInstance().getNextTime().toString() );
		}
		long milliToEnd = Olympiad._period != 0 ? Olympiad.getMillisToValidationEnd() / 1000L : Olympiad.getMillisToOlympiadEnd() / 1000L;
		double countDown = (milliToEnd - milliToEnd % 60L) / 60L;
		int numMins = (int) Math.floor(countDown % 60D);
		countDown = (countDown - numMins) / 60D;
		int numHours = (int) Math.floor(countDown % 24D);
		int numDays = (int) Math.floor((countDown - numHours) / 24D);
		
		if (Olympiad._validationEnd == 0 || Olympiad._period == 0)
			html.replace("%olym%", "Olym over: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		else
			html.replace("%olym%", "Olym start: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		
		activeChar.sendPacket(html);
	}
	
	public static void showMenuHtml(Player activeChar)
	{		
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Menu.htm");
		html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())) );
		html.replace("%time%", (new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis())) );
		html.replace("%partyRefusal%", activeChar.isPartyInvProt() ? "checked" : "unable");
		html.replace("%tradeRefusal%", activeChar.getTradeRefusal() ? "checked" : "unable");
		html.replace("%messageRefusal%", activeChar.getMessageRefusal() ? "checked" : "unable");
		html.replace("%setAutoGb%", activeChar.isAutoGb() ? "checked" : "unable");
		html.replace("%dressme%", activeChar.isDressMeEnabled() ? "ON" : "OFF");
		//html.replace("%goldBar%", activeChar.isInAutoGoldBarMode() ? ACTIVED : DESATIVED);
	//	html.replace("%goldBar%", activeChar.isInAutoGoldBarMode() ? "checked" : "unable");
		html.replace("%buff%", activeChar.isBuffProtected() ? "checked" : "unable");
		html.replace("%optimize%", activeChar.isOptimizeFPS() ? "checked" : "unable");
		html.replace("%name%", activeChar.getName());

		if(Config.SOLO_BOSS_EVENT)
		{
			if(SoloBoss.is_started())
				html.replace("%bossSolo%", "In Progress");
			else	
				html.replace("%bossSolo%", InitialSoloBossEvent.getInstance().getRestartNextTime().toString() );
		}
		
		if(Config.PVP_EVENT_ENABLED){
			if(PvPEvent.getInstance().isActive())	
			html.replace("%pvp%", "In Progress");
			else	
			html.replace("%pvp%", PvPEventNext.getInstance().getNextTime().toString() );
		}
		if(Config.TVT_EVENT_ENABLED){
			if(TvT.is_inProgress())	
			html.replace("%tvt%", "In Progress");
			else
		    html.replace("%tvt%", TvTEventManager.getInstance().getNextTime().toString() );
		}
		if(DMConfig.DM_EVENT_ENABLED)
		{
			if(DMEvent.isStarted())	
				html.replace("%DMTime%", "In Progress");
			else
				html.replace("%DMTime%", DMManager.getInstance().getNextTime());
		}
		if(FOSConfig.FOS_EVENT_ENABLED)
		{
			if(FOSEvent.isStarted())	
				html.replace("%FOSTime%", "In Progress");
			else
				html.replace("%FOSTime%", FOSManager.getInstance().getNextTime());
		}
		if(KTBConfig.KTB_EVENT_ENABLED)
		{
			if(KTBEvent.isStarted())	
				html.replace("%ktbTime%", "In Progress");
			else
				html.replace("%ktbTime%", KTBManager.getInstance().getNextTime());
		}
		if(Config.CTF_EVENT_ENABLED){
			if(CTF.is_inProgress())	
			html.replace("%ctf%", "In Progress");
			else
			html.replace("%ctf%", CTFEventManager.getInstance().getNextTime().toString() );
		}
		if(Config.TOURNAMENT_EVENT_TIME){
			if(ArenaTask.is_started())	
			html.replace("%arena%", "In Progress");
			else	
			html.replace("%arena%", ArenaEvent.getInstance().getNextTime().toString() );
		}
		if(Config.ACTIVE_MISSION)
			html.replace("%mission%", MissionReset.getInstance().getNextTime().toString() );
		if(Config.RESTART_BY_TIME_OF_DAY)
			html.replace("%restart%", Restart.getInstance().getRestartNextTime().toString() );
		
		if(Config.START_AUTO_PARTY){
			if(PartyZoneTask.is_started())	
			html.replace("%partyfarm%", "In Progress");
			else	
			html.replace("%partyfarm%", PartyFarmEvent.getInstance().getNextTime().toString() );
		}
		long milliToEnd = Olympiad._period != 0 ? Olympiad.getMillisToValidationEnd() / 1000L : Olympiad.getMillisToOlympiadEnd() / 1000L;
		double countDown = (milliToEnd - milliToEnd % 60L) / 60L;
		int numMins = (int) Math.floor(countDown % 60D);
		countDown = (countDown - numMins) / 60D;
		int numHours = (int) Math.floor(countDown % 24D);
		int numDays = (int) Math.floor((countDown - numHours) / 24D);
		
		if (Olympiad._validationEnd == 0 || Olympiad._period == 0)
			html.replace("%olym%", "Olym over: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		else
			html.replace("%olym%", "Olym start: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		
		
		
		if (activeChar.isBuffProtected())
			html.replace("%html_buff%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setBuffProtection\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		else
			html.replace("%html_buff%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setBuffProtection\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		
		if (activeChar.isPartyInvProt())
			html.replace("%html_party%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setPartyRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		else
			html.replace("%html_party%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setPartyRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		
		if (activeChar.getTradeRefusal())
			html.replace("%html_trade%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setTradeRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		else
			html.replace("%html_trade%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setTradeRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		
		if (activeChar.getMessageRefusal())
			html.replace("%html_message%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setMessageRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		else
			html.replace("%html_message%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setMessageRefuse\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		
		if (activeChar.isDisableGlowWeapon())
			html.replace("%html_glow%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setDisableGlowWeapon\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		else
			html.replace("%html_glow%", "<td align=center width=5><button value=\"\" action=\"bypass voiced_setDisableGlowWeapon\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		
		if (activeChar.isDressMeEnabled()) {
			html.replace("%html_message%", "<td width=5><button value=\"\" action=\"bypass voiced_setDressMeEnabled\" width=16 height=16 back=\"L2UI_CH3.joypad_lock_down\" fore=\"L2UI_CH3.joypad_lock\"></td>");
		} else {
			html.replace("%html_message%", "<td width=5><button value=\"\" action=\"bypass voiced_setDressMeEnabled\" width=16 height=16 back=\"L2UI_CH3.joypad_unlock_down\" fore=\"L2UI_CH3.joypad_unlock\"></td>");
		} 
		
		activeChar.sendPacket(html);
	}
	
	public static void showInfoHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Info_Server.htm");
		html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
		
		long milliToEnd = Olympiad._period != 0 ? Olympiad.getMillisToValidationEnd() / 1000L : Olympiad.getMillisToOlympiadEnd() / 1000L;
		double countDown = (milliToEnd - milliToEnd % 60L) / 60L;
		int numMins = (int) Math.floor(countDown % 60D);
		countDown = (countDown - numMins) / 60D;
		int numHours = (int) Math.floor(countDown % 24D);
		int numDays = (int) Math.floor((countDown - numHours) / 24D);
		
		if (Olympiad._validationEnd == 0 || Olympiad._period == 0)
			html.replace("%olym%", "Olym over: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		else
			html.replace("%olym%", "Olym start: " + numDays + " day(s), " + numHours + " hour(s), " + numMins + " minute(s).");
		
		activeChar.sendPacket(html);
	}
	
	public static void showDonateHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Donate.htm");
		activeChar.sendPacket(html);
	}
	
	public static void showMissionHtml(Player activeChar)
	{
		
		StringBuilder sb = new StringBuilder();

		sb.append("<br>");
	    sb.append("<center><img src=\"L2UI.SquareGray\" width=300 height=1></center>");
	    sb.append("<table width=\"300\" bgcolor=\"000000\">");
	    sb.append("<tr>");
	    sb.append("<td><center>Server Time: <font color=\"ff4d4d\">"+ sdf.format(new Date(System.currentTimeMillis())) +"</font></center></td>");
	    sb.append("<td><center></center></td>");
	    if (Config.ACTIVE_MISSION)
	    sb.append("<td><center> Reset all Mission: <font color=\"ff4d4d\">"+ MissionReset.getInstance().getNextTime() +"</font></center></td>");
	    sb.append("</tr>");
	    sb.append("</table>");
	    sb.append("<center><img src=\"L2UI.SquareGray\" width=300 height=1></center>");
		
		
		sb.append("<table><tr><td height=7>");
		sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		sb.append("</td></tr></table>");
		
		if (Config.ACTIVE_MISSION_TVT)
			if (activeChar.isTvTCompleted() || activeChar.check_tvt_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">TvT Event x <font color=\"ff4d4d\">( " + Config.MISSION_TVT_CONT + " ) / ("+ activeChar.getTvTCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m tvt\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getTvTCont() >= Config.MISSION_TVT_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">TvT Event x <font color=\"ff4d4d\">( " + Config.MISSION_TVT_CONT + " ) / ("+ activeChar.getTvTCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m tvt\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">TvT Event x <font color=\"ff4d4d\">( " + Config.MISSION_TVT_CONT + " ) / ("+ activeChar.getTvTCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m tvt\">View</a></td>");
				
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		
		if (Config.ACTIVE_MISSION_PVP)
			if (activeChar.isPvPCompleted() || activeChar.check_pvp_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">PvP Event x <font color=\"ff4d4d\">( " + Config.MISSION_PVP_CONT + " ) / ("+ activeChar.getPvPCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m pvp\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getPvPCont() >= Config.MISSION_PVP_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">PvP Event x <font color=\"ff4d4d\">( " + Config.MISSION_PVP_CONT + " ) / ("+ activeChar.getPvPCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m pvp\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">PvP Event x <font color=\"ff4d4d\">( " + Config.MISSION_PVP_CONT + " ) / ("+ activeChar.getPvPCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m pvp\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} 
		
		if (Config.ACTIVE_MISSION_RAID)
			if (activeChar.isRaidCompleted() || activeChar.check_raid_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">RaidBoss x <font color=\"ff4d4d\">( " + Config.MISSION_RAID_CONT + " ) / ("+ activeChar.getRaidCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m raid\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getRaidCont() >= Config.MISSION_RAID_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">RaidBoss x <font color=\"ff4d4d\">( " + Config.MISSION_RAID_CONT + " ) / ("+ activeChar.getRaidCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m raid\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">RaidBoss x <font color=\"ff4d4d\">( " + Config.MISSION_RAID_CONT + " ) / ("+ activeChar.getRaidCont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m raid\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		
		if (Config.ACTIVE_MISSION_1X1)
			if (activeChar.is1x1Completed() || activeChar.check_1x1_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (1x1) x <font color=\"ff4d4d\">( " + Config.MISSION_1X1_CONT + " ) / ("+ activeChar.getTournament1x1Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 1x1\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getTournament1x1Cont() >= Config.MISSION_1X1_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (1x1) x <font color=\"ff4d4d\">( " + Config.MISSION_1X1_CONT + " ) / ("+ activeChar.getTournament1x1Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 1x1\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (1x1) x <font color=\"ff4d4d\">( " + Config.MISSION_1X1_CONT + " ) / ("+ activeChar.getTournament1x1Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 1x1\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		
		if (Config.ACTIVE_MISSION_2X2)
			if (activeChar.is2x2Completed() || activeChar.check_2x2_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (2x2) x <font color=\"ff4d4d\">( " + Config.MISSION_2X2_CONT + " ) / ("+ activeChar.getTournament2x2Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 2x2\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getTournament2x2Cont() >= Config.MISSION_2X2_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (2x2) x <font color=\"ff4d4d\">( " + Config.MISSION_2X2_CONT + " ) / ("+ activeChar.getTournament2x2Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 2x2\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (2x2) x <font color=\"ff4d4d\">( " + Config.MISSION_2X2_CONT + " ) / ("+ activeChar.getTournament2x2Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 2x2\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		if (Config.ACTIVE_MISSION_5X5)
			if (activeChar.is5x5Completed() || activeChar.check_5x5_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (5x5) x <font color=\"ff4d4d\">( " + Config.MISSION_5X5_CONT + " ) / ("+ activeChar.getTournament5x5Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 5x5\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getTournament5x5Cont() >= Config.MISSION_5X5_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (5x5) x <font color=\"ff4d4d\">( " + Config.MISSION_5X5_CONT + " ) / ("+ activeChar.getTournament5x5Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 5x5\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (5x5) x <font color=\"ff4d4d\">( " + Config.MISSION_5X5_CONT + " ) / ("+ activeChar.getTournament5x5Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 5x5\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		if (Config.ACTIVE_MISSION_9X9)
			if (activeChar.is9x9Completed() || activeChar.check_9x9_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (9x9) x <font color=\"ff4d4d\">( " + Config.MISSION_9X9_CONT + " ) / ("+ activeChar.getTournament9x9Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 9x9\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getTournament9x9Cont() >= Config.MISSION_9X9_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (9x9) x <font color=\"ff4d4d\">( " + Config.MISSION_9X9_CONT + " ) / ("+ activeChar.getTournament9x9Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 9x9\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Tour (9x9) x <font color=\"ff4d4d\">( " + Config.MISSION_9X9_CONT + " ) / ("+ activeChar.getTournament9x9Cont() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m 9x9\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		
		if (Config.ACTIVE_MISSION_PARTY_MOB)
			if (activeChar.isPartyMobCompleted() || activeChar.check_party_mob_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Party Zone x <font color=\"ff4d4d\">( " + Config.MISSION_PARTY_MOB_CONT + " ) / ("+ activeChar.getPartyMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m party_mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getPartyMonsterKills() >= Config.MISSION_PARTY_MOB_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Party Zone x <font color=\"ff4d4d\">( " + Config.MISSION_PARTY_MOB_CONT + " ) / ("+ activeChar.getPartyMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m party_mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Party Zone x <font color=\"ff4d4d\">( " + Config.MISSION_PARTY_MOB_CONT + " ) / ("+ activeChar.getPartyMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m party_mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}
		
		if (Config.ACTIVE_MISSION_MOB)
			if (activeChar.isMobCompleted() || activeChar.check_mob_hwid(activeChar.getHWID())) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Solo Zone x <font color=\"ff4d4d\">( " + Config.MISSION_MOB_CONT + " ) / ("+ activeChar.getMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"LEVEL\">Received</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else if (activeChar.getMonsterKills() >= Config.MISSION_MOB_CONT) {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Solo Zone x <font color=\"ff4d4d\">( " + Config.MISSION_MOB_CONT + " ) / ("+ activeChar.getMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ffffff\">Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			} else {
				sb.append("<table width=\"300\">");
				sb.append("<tr>");
				sb.append("<td width=\"150\" align=\"left\">Solo Zone x <font color=\"ff4d4d\">( " + Config.MISSION_MOB_CONT + " ) / ("+ activeChar.getMonsterKills() +")</font></td>");
				sb.append("<td width=\"50\" align=\"right\"><a action=\"bypass voiced_select_m mob\">View</a></td>");
				sb.append("<td width=\"100\" align=\"center\"><font color=\"ff0000\">Not Completed</font></td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table><tr><td height=7>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
				sb.append("</td></tr></table>");
			}  
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Mission.htm");
		html.replace("%mission%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
