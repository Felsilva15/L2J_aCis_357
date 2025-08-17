package net.sf.l2j.gameserver.handler.usercommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.ConnectionPool;

import net.sf.l2j.gameserver.data.CharTemplateTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.CloseUtil;

/**
 * Support for /olympiadstat command Added by kamy
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		109
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
			return false;
		}
		
		OlympiadPoints(activeChar);
		
		return true;
	}
	
	private static final String OLYMPIAD_POINTS = "SELECT olympiad_points, competitions_done, competitions_won, competitions_lost FROM olympiad_nobles WHERE char_Id=?";
	
	public static void OlympiadPoints(Player activeChar)
	{
		StatsSet playerStat = Olympiad.getNobleStats(activeChar.getObjectId());
		if (playerStat == null)
		{
			Olympiad.getInstance().announcePeriod(activeChar);
			return;
		}
		
		int points = 0;
		int done = 0;
		int win = 0;
		int lost = 0;
		
		Connection con = null;
		
		try
		{			
			con = ConnectionPool.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_POINTS);
			statement.setInt(1, activeChar.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				points = rset.getInt("olympiad_points");
				done = rset.getInt("competitions_done");
				win = rset.getInt("competitions_won");
				lost = rset.getInt("competitions_lost");
			}
			
			activeChar.sendMessage("========<Olympiad>========");
			int classId = activeChar.getBaseClass();
			String className = CharTemplateTable.getInstance().getClassNameById(classId);
			
			activeChar.sendMessage("Classe Base: " + className);
			activeChar.sendMessage("Match(es): " + done + " | Win(s): " + win + " | Defeat(s): " + lost);
			activeChar.sendMessage("Olympiad Points: " + points);
			activeChar.sendMessage("=======================");
			
			Olympiad.getInstance().announcePeriod(activeChar);
			
			rset.close();
			statement.close();
			statement = null;
			rset = null;
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();			
		}
		finally
		{
			CloseUtil.close(con);
		}
		
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}