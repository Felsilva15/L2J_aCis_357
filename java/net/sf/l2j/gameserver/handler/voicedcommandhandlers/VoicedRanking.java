package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;

import Dev.Ranking.Clan.ClanRankingConfig;
import Dev.Ranking.Clan.TaskClanRankingReward;

public class VoicedRanking implements IVoicedCommandHandler {
	private static final String[] VOICED_COMMANDS = new String[] { "pvpevent","pvpEvent","pvp", "pks", "clan", "ranking", "5x5", "9x9" };
	public static final Logger _log = Logger.getLogger(VoicedRanking.class.getName());
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target) {
		if (command.equals("ranking")) {
			showRankingHtml(activeChar);
		}
		if ((command.equals("pvpEvent") || command.equals("pvpevent")) && Config.PVP_EVENT_ENABLED){
			PvPEvent.getTopHtml(activeChar); 
		}
		else if (command.equals("pvp")) {
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder();
			tb.append("<html>");
			tb.append("<body>");
			tb.append("<center>");
			tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32><br>");
			tb.append("<table border=\"1\" width=\"300\">");
			tb.append("<tr>");
			tb.append("<td><center>Rank</center></td>");
			tb.append("<td><center>Character</center></td>");
			tb.append("<td><center>Pvp's</center></td>");
			tb.append("<td><center>Status</center></td>");
			tb.append("</tr>");
			try (Connection con = ConnectionPool.getConnection()) {
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pvpkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				while (result.next()) {
					String status, pvps = result.getString("pvpkills");
					String name = result.getString("char_name");
					if (name.equals("WWWWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWW") || name.equals("WWWWWWWWWWW") || name.equals("WWWWWWWWWW") || name.equals("WWWWWWWWW") || name.equals("WWWWWWWW") || name.equals("WWWWWWW") || name.equals("WWWWWW")) {
						name = name.substring(0, 3) + "..";
					} else if (name.length() > 14) {
						name = name.substring(0, 14) + "..";
					} 
					pos++;
					String statu = result.getString("online");
					if (statu.equals("1")) {
						status = "<font color=00FF00>Online</font>";
					} else {
						status = "<font color=FF0000>Offline</font>";
					} 
					tb.append("<tr>");
					tb.append("<td><center><font color =\"AAAAAA\">" + pos + "</font></center></td>");
					tb.append("<td><center><font color=00FFFF>" + name + "</font></center></td>");
					tb.append("<td><center>" + pvps + "</center></td>");
					tb.append("<td><center>" + status + "</center></td>");
					tb.append("</tr>");
				} 
				statement.close();
				result.close();
			} catch (Exception exception) {}
			
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body>");
			tb.append("</html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
			
		} else if (command.equals("pks")) {
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder();
			tb.append("<html>");
			tb.append("<body>");
			tb.append("<center>");
			tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32><br>");
			tb.append("<table border=\"1\" width=\"300\">");
			tb.append("<tr>");
			tb.append("<td><center>Rank</center></td>");
			tb.append("<td><center>Character</center></td>");
			tb.append("<td><center>Pk's</center></td>");
			tb.append("<td><center>Status</center></td>");
			tb.append("</tr>");
			try (Connection con = ConnectionPool.getConnection()) {
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pkkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				while (result.next()) {
					String status, pks = result.getString("pkkills");
					String name = result.getString("char_name");
					if (name.equals("WWWWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWWW") || name.equals("WWWWWWWWWWWW") || name.equals("WWWWWWWWWWW") || name.equals("WWWWWWWWWW") || name.equals("WWWWWWWWW") || name.equals("WWWWWWWW") || name.equals("WWWWWWW") || name.equals("WWWWWW")) {
						name = name.substring(0, 3) + "..";
					} else if (name.length() > 14) {
						name = name.substring(0, 14) + "..";
					} 
					pos++;
					String statu = result.getString("online");
					if (statu.equals("1")) {
						status = "<font color=00FF00>Online</font>";
					} else {
						status = "<font color=FF0000>Offline</font>";
					} 
					tb.append("<tr>");
					tb.append("<td><center><font color =\"AAAAAA\">" + pos + "</font></center></td>");
					tb.append("<td><center><font color=00FFFF>" + name + "</font></center></td>");
					tb.append("<td><center>" + pks + "</center></td>");
					tb.append("<td><center>" + status + "</center></td>");
					tb.append("</tr>");
				} 
				statement.close();
				result.close();
			} catch (Exception exception) {}
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body>");
			tb.append("</html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		} 
//		if (command.equals("clan") && ClanRankingConfig.ENABLE_CLAN_RANKING)
//		{
//			NpcHtmlMessage htm = new NpcHtmlMessage(0);
//			StringBuilder tb = new StringBuilder("<html><head><title>Clan Ranking</title></head><body><table width=80><tr><td><button value=\"\" action=\"bypass voiced_ranking \" width=35 height=23 back=\"L2UI_CH3.calculate2_bs_down\" fore=\"L2UI_CH3.calculate2_bs\"></td><td> Back </td></tr></table><br><center>Next Rewarding: <font color=LEVEL>" + TaskClanRankingReward.getTimeToDate() +"</font><br1><table width=290><tr><td><center>Rank</center></td><td><center>Clan Name</center></td><td><center>Raid Point(s)</center></td><td><center>Castle Point(s)</center></td></tr>");
//			
//			try (Connection con = ConnectionPool.getConnection())
//			{
//				PreparedStatement statement = con.prepareStatement("SELECT clan_id, boss_points, siege_points FROM clan_points WHERE ((boss_points + siege_points)>0) ORDER BY (boss_points + siege_points) desc LIMIT 15");
//				ResultSet result = statement.executeQuery();
//				int pos = 0;
//				
//				while (result.next())
//				{
//					String raid = result.getString("boss_points");
//					String castle = result.getString("siege_points");
//					String owner = result.getString("clan_id");
//					pos += 1;
//					
//					PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner);
//					ResultSet result2 = charname.executeQuery();
//					
//					while (result2.next())
//					{
//						String clan_name = result2.getString("clan_name");
//						
//						if (clan_name.equals("WWWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWW") || clan_name.equals("WWWWWWWWWW") || clan_name.equals("WWWWWWWWW") || clan_name.equals("WWWWWWWW") || clan_name.equals("WWWWWWW") || clan_name.equals("WWWWWW"))
//							clan_name = clan_name.substring(0, 3) + "..";
//						else if (clan_name.length() > 14)
//							clan_name = clan_name.substring(0, 14) + "..";
//						
//						tb.append("<tr><td><center>" + pos + "</center></td><td><center><font color=LEVEL>" + clan_name + "</font></center></td><td><center><font color=00FFFF>" + raid + "</font></center></td><td><center><font color=00FF00>" + castle + "</font></center></td></tr>");
//					}
//					charname.close();
//				}
//				statement.close();
//				result.close();
//				con.close();
//			}
//			catch (Exception e)
//			{
//				_log.warning("Error: could not restore clan_points ranking data info: " + e);
//				e.printStackTrace();
//			}   
//			tb.append("</table>");
//			tb.append("</body></html>");
//			
//			htm.setHtml(tb.toString());
//			activeChar.sendPacket(htm);
//		}
		else if (command.equals("5x5")) 
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder();
			tb.append("<html>");
			tb.append("<body>");
			tb.append("<center>");
			tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32><br>");
			tb.append("<table border=\"1\" width=\"300\">");
			tb.append("<tr>");
			tb.append("<td><center>Rank</center></td>");
			tb.append("<td><center>Clan</center></td>");
			tb.append("<td><center>Leader</center></td>");
			tb.append("<td><center>5x5 Win</center></td>");
			tb.append("</tr>");
			try (Connection con = ConnectionPool.getConnection()) {
				PreparedStatement statement = con.prepareStatement("SELECT clan_id,clan_name,ally_name,5x5_win FROM clan_data WHERE 5x5_win>0 order by 5x5_win desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				String leader_name = "N/A";
				while (result.next()) {
					int clan_id = result.getInt("clan_id");
					String clan_name = result.getString("clan_name");
					if (clan_name.equals("WWWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWW") || clan_name.equals("WWWWWWWWWW") || clan_name.equals("WWWWWWWWW") || clan_name.equals("WWWWWWWW") || clan_name.equals("WWWWWWW") || clan_name.equals("WWWWWW")) {
						clan_name = clan_name.substring(0, 3) + "..";
					} else if (clan_name.length() > 14) {
						clan_name = clan_name.substring(0, 14) + "..";
					} 
					String ally_name = result.getString("ally_name");
					if (ally_name == null)
						ally_name = "N/A"; 
					if (ally_name.equals("WWWWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWW") || ally_name.equals("WWWWWWWWWW") || ally_name.equals("WWWWWWWWW") || ally_name.equals("WWWWWWWW") || ally_name.equals("WWWWWWW") || ally_name.equals("WWWWWW")) {
						ally_name = ally_name.substring(0, 3) + "..";
					} else if (ally_name.length() > 6) {
						ally_name = ally_name.substring(0, 6) + "..";
					} 
					Clan owner = ClanTable.getInstance().getClan(clan_id);
					if (owner != null)
						leader_name = owner.getLeaderName(); 
					if (leader_name.equals("WWWWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWW") || leader_name.equals("WWWWWWWWWW") || leader_name.equals("WWWWWWWWW") || leader_name.equals("WWWWWWWW") || leader_name.equals("WWWWWWW") || leader_name.equals("WWWWWW")) {
						leader_name = leader_name.substring(0, 3) + "..";
					} else if (leader_name.length() > 10) {
						leader_name = leader_name.substring(0, 10) + "..";
					} 
					String win = result.getString("5x5_win");
					pos++;
					tb.append("<tr><td><center>" + pos + "</center></td><td><center>" + clan_name + "</center></td><td><center>" + leader_name + "</center></td><td><center>" + win + "</center></td></tr>");
				} 
				statement.close();
				result.close();
			} catch (Exception exception) {}
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body>");
			tb.append("</html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		} else if (command.equals("9x9")) {
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder();
			tb.append("<html>");
			tb.append("<body>");
			tb.append("<center>");
			tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32><br>");
			tb.append("<table border=\"1\" width=\"300\">");
			tb.append("<tr>");
			tb.append("<td><center>Rank</center></td>");
			tb.append("<td><center>Clan</center></td>");
			tb.append("<td><center>Leader</center></td>");
			tb.append("<td><center>9x9 Win</center></td>");
			tb.append("</tr>");
			try (Connection con = ConnectionPool.getConnection()) {
				PreparedStatement statement = con.prepareStatement("SELECT clan_id,clan_name,ally_name,9x9_win FROM clan_data WHERE 9x9_win>0 order by 9x9_win desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				String leader_name = "N/A";
				while (result.next()) {
					int clan_id = result.getInt("clan_id");
					String clan_name = result.getString("clan_name");
					if (clan_name.equals("WWWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWW") || clan_name.equals("WWWWWWWWWW") || clan_name.equals("WWWWWWWWW") || clan_name.equals("WWWWWWWW") || clan_name.equals("WWWWWWW") || clan_name.equals("WWWWWW")) {
						clan_name = clan_name.substring(0, 3) + "..";
					} else if (clan_name.length() > 14) {
						clan_name = clan_name.substring(0, 14) + "..";
					} 
					String ally_name = result.getString("ally_name");
					if (ally_name == null)
						ally_name = "N/A"; 
					if (ally_name.equals("WWWWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWWW") || ally_name.equals("WWWWWWWWWWW") || ally_name.equals("WWWWWWWWWW") || ally_name.equals("WWWWWWWWW") || ally_name.equals("WWWWWWWW") || ally_name.equals("WWWWWWW") || ally_name.equals("WWWWWW")) {
						ally_name = ally_name.substring(0, 3) + "..";
					} else if (ally_name.length() > 6) {
						ally_name = ally_name.substring(0, 6) + "..";
					} 
					Clan owner = ClanTable.getInstance().getClan(clan_id);
					if (owner != null)
						leader_name = owner.getLeaderName(); 
					if (leader_name.equals("WWWWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWWW") || leader_name.equals("WWWWWWWWWWW") || leader_name.equals("WWWWWWWWWW") || leader_name.equals("WWWWWWWWW") || leader_name.equals("WWWWWWWW") || leader_name.equals("WWWWWWW") || leader_name.equals("WWWWWW")) {
						leader_name = leader_name.substring(0, 3) + "..";
					} else if (leader_name.length() > 10) {
						leader_name = leader_name.substring(0, 10) + "..";
					} 
					String win = result.getString("9x9_win");
					pos++;
					tb.append("<tr><td><center>" + pos + "</center></td><td><center>" + clan_name + "</center></td><td><center>" + leader_name + "</center></td><td><center>" + win + "</center></td></tr>");
				} 
				statement.close();
				result.close();
			} catch (Exception exception) {}
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body>");
			tb.append("</html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		if (command.equals("clan") && ClanRankingConfig.ENABLE_CLAN_RANKING)
		{
		    NpcHtmlMessage htm = new NpcHtmlMessage(0);
		    StringBuilder tb = new StringBuilder("<html><head><title>Clan Ranking</title></head><body><table width=80><tr><td><button value=\"\" action=\"bypass voiced_ranking \" width=35 height=23 back=\"L2UI_CH3.calculate2_bs_down\" fore=\"L2UI_CH3.calculate2_bs\"></td><td> Back </td></tr></table><br><center>Next Rewarding: <font color=LEVEL>" + TaskClanRankingReward.getTimeToDate() +"</font><br1><table width=290><tr><td><center>Rank</center></td><td><center>Clan Name</center></td><td><center>Raid Point(s)</center></td></tr>");
		    
		    try (Connection con = ConnectionPool.getConnection())
		    {
		        // Modificando a consulta para não considerar siege_points
		        PreparedStatement statement = con.prepareStatement("SELECT clan_id, boss_points FROM clan_points WHERE boss_points > 0 ORDER BY boss_points DESC LIMIT 15");
		        ResultSet result = statement.executeQuery();
		        int pos = 0;
		        
		        while (result.next())
		        {
		            String raid = result.getString("boss_points");
		            String owner = result.getString("clan_id");
		            pos += 1;
		            
		            // Garantir que o ResultSet result2 seja fechado corretamente
		            try (PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner);
		                 ResultSet result2 = charname.executeQuery())
		            {
		                while (result2.next())
		                {
		                    String clan_name = result2.getString("clan_name");
		                    
		                    // Truncar nomes de clãs muito longos
		                    if (clan_name.equals("WWWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWW") || clan_name.equals("WWWWWWWWWW") || clan_name.equals("WWWWWWWWW") || clan_name.equals("WWWWWWWW") || clan_name.equals("WWWWWWW") || clan_name.equals("WWWWWW"))
		                        clan_name = clan_name.substring(0, 3) + "..";
		                    else if (clan_name.length() > 14)
		                        clan_name = clan_name.substring(0, 14) + "..";
		                    
		                    // Exibir apenas raid points (boss_points)
		                    tb.append("<tr><td><center>" + pos + "</center></td><td><center><font color=LEVEL>" + clan_name + "</font></center></td><td><center><font color=00FFFF>" + raid + "</font></center></td></tr>");
		                }
		            }
		        }
		        statement.close();
		        result.close();
		    }
		    catch (Exception e)
		    {
		        _log.warning("Error: could not restore clan_points ranking data info: " + e);
		        e.printStackTrace();
		    }   
		    tb.append("</table>");
		    tb.append("</body></html>");
		    
		    htm.setHtml(tb.toString());
		    activeChar.sendPacket(htm);
		}
		return true;
	}
	
	private static void showRankingHtml(Player activeChar) {
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Ranking.htm");
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getVoicedCommandList() {
		return VOICED_COMMANDS;
	}
}
