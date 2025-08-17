package net.sf.l2j.gameserver.handler.tutorialhandlers;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.handler.ITutorialHandler;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;

import Dev.AutoFarm.AutofarmManager;

/**
 * @author Chris
 *
 */
public class Autofarm implements ITutorialHandler
{
	private static final String[] LINK_COMMANDS =
	{
    	"start"
	};

	@Override
	public boolean useLink(String command, Player player, String params)
	{
		if (command.startsWith("start"))
			handleCommands(player, params);
		
		return true;
	}
	
	public static void handleCommands(Player player, String command)
	{
  	
		showAutoFarm(player);

		if (command.startsWith("inc_radius"))
		{
			player.setRadius(player.getRadius() + 400);
			showAutoFarm(player);
		}
		if (command.startsWith("SkillsSelected"))
		{
			ShowSkillsAutoFarm(player);
		}
		if (command.startsWith("enableAntiKs"))
		{
			player.setAntiKsProtection(!player.isAntiKsProtected());
			showAutoFarm(player);
		}
		if (command.startsWith("enableAssistParty"))
		{
			player.setAssistParty(!player.isAssistParty());
			showAutoFarm(player);
		}
		if (command.startsWith("dec_radius"))
		{
			player.setRadius(player.getRadius() - 400);
			showAutoFarm(player);
		}
    		
		if (command.startsWith("inc_page"))
		{
			player.setPage(player.getPage() + 1);
			showAutoFarm(player);
		}
		
		if (command.startsWith("dec_page"))
		{
			player.setPage(player.getPage() - 1);
			showAutoFarm(player);
		}
		
		if (command.startsWith("inc_heal"))
		{
			player.setHealPercent(player.getHealPercent() + 10);
			showAutoFarm(player);
		}

        if (command.startsWith("dec_heal"))
		{
			player.setHealPercent(player.getHealPercent() - 10);
			showAutoFarm(player);
		}
		
		if (command.startsWith("enableAutoFarm"))
		{
			if(Config.ENABLE_COMMAND_VIP_AUTOFARM)
        	{
        		if(!player.isVip())
        		{
        			VoicedMenu.showMenuHtml(player);
        			player.sendMessage("You are not VIP member.");
        			return;
        		}
        	}
			if(Config.NO_USE_FARM_IN_PEACE_ZONE)
	    	{
	    		if(player.isInsideZone(ZoneId.PEACE))
	    		{
	    			player.sendMessage("No Use Auto farm in Peace Zone.");
	    			AutofarmManager.INSTANCE.stopFarm(player);
	    			player.setAutoFarm(false);
	    			player.broadcastUserInfo();
	    			return;
	    		}
	    	}
	    	if (player.isAutoFarm())
	    	{
				AutofarmManager.INSTANCE.stopFarm(player);
				player.setAutoFarm(false);
	    	}
	    	else
	    	{
	    		AutofarmManager.INSTANCE.startFarm(player);
	    		player.setAutoFarm(true);
	    	}

	    	showAutoFarm(player);
		}
		
		if (command.startsWith("enableBuffProtect"))
		{
			if(Config.ENABLE_COMMAND_VIP_AUTOFARM)
        	{
        		if(!player.isVip())
        		{
        			VoicedMenu.showMenuHtml(player);
        			player.sendMessage("You are not VIP member.");
        			return;
        		}
        	}
	        
			player.setNoBuffProtection(!player.isNoBuffProtected());
			showAutoFarm(player);
		}
		if (command.startsWith("close"))
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}

   public static void showAutoFarm(Player activeChar)
	{
		if(Config.ENABLE_COMMAND_VIP_AUTOFARM)
    	{
    		if(!activeChar.isVip())
    		{
    			VoicedMenu.showMenuHtml(activeChar);
    			activeChar.sendMessage("You are not VIP member.");
    			return;
    		}
    	}
		String msg = HtmCache.getInstance().getHtm("data/html/mods/menu/AutoFarm.htm");
		
		msg = msg.replace("%player%", activeChar.getName());
		msg = msg.replace("%page%", StringUtil.formatNumber(activeChar.getPage() + 1));
		msg = msg.replace("%heal%", StringUtil.formatNumber(activeChar.getHealPercent()));
		msg = msg.replace("%radius%", StringUtil.formatNumber(activeChar.getRadius()));
		msg = msg.replace("%noBuff%", activeChar.isNoBuffProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		
		//Botao
		msg = msg.replace("%button%", activeChar.isAutoFarm() ? "value=\"Stop\" action=\"link -h_start_enableAutoFarm\"" : "value=\"Start\" action=\"link -h_start_enableAutoFarm\"");
		//Auto Farm ativar
		msg = msg.replace("%autofarm%", activeChar.isAutoFarm() ? "<font color=00FF00>Active</font>" : "<font color=FF0000>Inactive</font>");
		
		//Botao anti ks players auto farm
		msg = msg.replace("%antiKs%", activeChar.isAntiKsProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		
		//botao assist party
		msg = msg.replace("%enableAssistParty%", activeChar.isAssistParty() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
		
		//Novo
		activeChar.sendPacket(new TutorialShowHtml(msg));
	}
/*	public static void showAutoFarm(Player activeChar) {
	    if (Config.ENABLE_COMMAND_VIP_AUTOFARM) {
	        if (!activeChar.isVip()) {
	            VoicedMenu.showMenuHtml(activeChar);
	            activeChar.sendMessage("You are not VIP member.");
	            return;
	        }
	    }

	    StringBuilder htmlContent = new StringBuilder();

	    htmlContent.append("<html><body>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");
	    htmlContent.append("<table width=296 bgcolor=000000 cellpadding=1 cellspacing=1>");
	    htmlContent.append("<tr><td height=40 width=40><img src=\"icon.skill0003\" width=32 height=32></td>");
	    htmlContent.append("<td width=256><font color=\"LEVEL\">Premium</font> users have no limit of using this system.<br1>Common users Necessary VIP.</td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // Priorities
	    htmlContent.append("<table cellspacing=1 cellpadding=1 width=296 bgcolor=000000>");
	    htmlContent.append("<tr><td width=53><font color=\"B09878\">Priorities:</font></td>");
	    htmlContent.append("<td width=243><font color=\"B8860B\">1st 'Low Life' that using F9, F10, F11 & F12</font></td></tr>");
	    htmlContent.append("<tr><td width=53></td><td width=243><font color=\"LEVEL\">2nd 'Chance' that using F5, F6, F7 & F8</font></td></tr>");
	    htmlContent.append("<tr><td width=53></td><td width=243><font color=\"9932CC\">3rd 'Attack' that using F1, F2, F3 & F4</font></td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // Percentage of HP to use 'Low Life' skills
	    htmlContent.append("<table width=296 bgcolor=000000 cellspacing=0 cellpadding=0>");
	    htmlContent.append("<tr><td width=216 align=left><font color=\"B8860B\">Percentage of HP to use 'Low Life' skills:</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_dec_heal\" back=L2UI_CH3.prev1_down fore=L2UI_CH3.prev1 width=16 height=16></td>");
	    htmlContent.append("<td width=40 align=center><font color=\"B8860B\">").append(activeChar.getHealPercent()).append("%</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_inc_heal\" back=L2UI_CH3.next1_down fore=L2UI_CH3.next1 width=16 height=16></td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // Target Range for Monsters
	    htmlContent.append("<table width=296 bgcolor=000000 cellspacing=0 cellpadding=0>");
	    htmlContent.append("<tr><td width=216 align=left><font color=\"836FFF\">Target Range for Monsters:</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_dec_radius\" back=L2UI_CH3.prev1_down fore=L2UI_CH3.prev1 width=16 height=16></td>");
	    htmlContent.append("<td width=40 align=center><font color=\"836FFF\">").append(activeChar.getRadius()).append("</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_inc_radius\" back=L2UI_CH3.next1_down fore=L2UI_CH3.next1 width=16 height=16></td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // Shortcut Page in Auto Farm
	    htmlContent.append("<table width=296 bgcolor=000000 cellspacing=0 cellpadding=0>");
	    htmlContent.append("<tr><td width=216 align=left><font color=\"LEVEL\">Shortcut Page in Auto Farm:</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_dec_page\" back=L2UI_CH3.prev1_down fore=L2UI_CH3.prev1 width=16 height=16></td>");
	    htmlContent.append("<td width=40 align=center><font color=\"LEVEL\">").append(StringUtil.formatNumber(activeChar.getPage() + 1)).append("</font></td>");
	    htmlContent.append("<td width=20 align=center><button action=\"link -h_start_inc_page\" back=L2UI_CH3.next1_down fore=L2UI_CH3.next1 width=16 height=16></td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // System current state
	    htmlContent.append("<table cellspacing=2 cellpadding=0 width=296 bgcolor=000000>");
	    htmlContent.append("<tr><td width=130 align=center><font color=B09878>System current state:</font></td>");
	    htmlContent.append("<td width=70 align=center>[ ").append(activeChar.isAutoFarm() ? "<font color=00FF00>Active</font>" : "<font color=FF0000>Inactive</font>").append(" ]</td>");
	    htmlContent.append("<td width=93 align=center><button value=\"").append(activeChar.isAutoFarm() ? "Stop" : "Start").append("\" action=\"link -h_start_enableAutoFarm\" width=75 height=21 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal></td></tr>");
	    htmlContent.append("</table>");
	    htmlContent.append("<img src=\"L2UI.SquareWhite\" width=300 height=1>");

	    // Buff Protection, Anti KS, Assist Party (Checkboxes)
	    htmlContent.append("<table width=296 bgcolor=000000>");

	    // Checkbox Buff Protection
	    htmlContent.append("<tr><td width=23 align=right>");
	    if (activeChar.isNoBuffProtected()) {
	        htmlContent.append("<button action=\"link -h_start_enableBuffProtect\" width=12 height=12 back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked>");
	    } else {
	        htmlContent.append("<button action=\"link -h_start_enableBuffProtect\" width=12 height=12 back=L2UI.CheckBox fore=L2UI.CheckBox>");
	    }
	    htmlContent.append("</td><td width=266 align=left>Disabled farming system when you are out of buffs</td></tr>");

	    // Checkbox Anti KS Protection
	    htmlContent.append("<tr><td width=23 align=right>");
	    if (activeChar.isAntiKsProtected()) {
	        htmlContent.append("<button action=\"link -h_start_enableAntiKs\" width=12 height=12 back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked>");
	    } else {
	        htmlContent.append("<button action=\"link -h_start_enableAntiKs\" width=12 height=12 back=L2UI.CheckBox fore=L2UI.CheckBox>");
	    }
	    htmlContent.append("</td><td width=266 align=left>Activated Anti KS Monsters other players</td></tr>");

	    // Checkbox Assist Party
	    htmlContent.append("<tr><td width=23 align=right>");
	    if (activeChar.isAssistParty()) {
	        htmlContent.append("<button action=\"link -h_start_enableAssistParty\" width=12 height=12 back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked>");
	    } else {
	        htmlContent.append("<button action=\"link -h_start_enableAssistParty\" width=12 height=12 back=L2UI.CheckBox fore=L2UI.CheckBox>");
	    }
	    htmlContent.append("</td><td width=266 align=left>Activated Assist leader Party Auto Farm</td></tr>");

	    htmlContent.append("</table>");

	    // Fechar
	    htmlContent.append("<table width=315 height=35 bgcolor=000000><tr><td><center><button value=\"Close\" action=\"link -h_start_close\" width=95 height=20 back=\"L2UI_ch3.Bigbutton_down\" fore=\"L2UI_ch3.Bigbutton\"></center></td></tr></table>");

	    htmlContent.append("</body></html>");

	    // Envia o conteúdo gerado para o jogador
	    activeChar.sendPacket(new TutorialShowHtml(htmlContent.toString()));
	}*/



    public static void ShowSkillsAutoFarm(Player activeChar)
   	{
    	NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/AutoFarmSkills.htm"); 
		activeChar.sendPacket(html);
   	}
    
    
	@Override
	public String[] getLinkList()
	{
		return LINK_COMMANDS;
	}
}
