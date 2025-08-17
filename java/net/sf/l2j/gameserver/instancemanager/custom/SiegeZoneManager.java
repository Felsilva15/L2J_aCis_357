package net.sf.l2j.gameserver.instancemanager.custom;

import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.ConnectionPool;
import net.sf.l2j.commons.concurrent.ThreadPool;

public class SiegeZoneManager
{
    private static final Logger _log = Logger.getLogger(SiegeZoneManager.class.getName());

	public SiegeZoneManager()
    {
        _log.log(Level.INFO, "SiegeZoneManager - Loaded.");
    }
	
    private static boolean checkPlayersKickTask(Player activeChar, Integer numberBox)
    {
        Map<String, List<Player>> map = new HashMap<>();
        
        if (activeChar != null)
        {
        	for (Player player : World.getInstance().getPlayers())
        	{
        	//	if (!player.isInsideZone(ZoneId.RAID_ZONE) || player.getClient().getConnection().getInetAddress().getHostAddress() == null)
        		if (!player.isInsideZone(ZoneId.SIEGE) || player.getHWID() == null)
        			continue;
				String ip1 = activeChar.getHWID();
				String ip2 = player.getHWID();
        		
        	//	String ip1 = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
			//	String ip2 = player.getClient().getConnection().getInetAddress().getHostAddress();

				if (ip1.equals(ip2))
				{
					if (map.get(ip1) == null)
						map.put(ip1, new ArrayList<Player>());

					map.get(ip1).add(player);

					if (map.get(ip1).size() > numberBox)
						return true;
				}
        	}
        }
        return false;
    }
    
    public boolean checkPlayersArea(Player activeChar, Integer numberBox, Boolean forcedTeleport)
    {
        if (checkPlayersKickTask(activeChar, numberBox))
        {
            if (forcedTeleport)
            {
            	activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS","Allowed only " + numberBox + " Client in Siege Zone!"));
				for (Player allgms : AdminData.getInstance().getAllGms(true))
				{
					if (!activeChar.isGM())
						allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double HWID]", activeChar.getName() +" in Siege Zone!"));
				}
            //	RandomTeleport(activeChar);
				ThreadPool.schedule(() -> RandomTeleport(activeChar), 2000);

            }
            return true;
        }
        return false;
    }
    
    private static boolean checkPlayersKickTask_ip(Player activeChar, Integer numberBox, Collection<Player> world)
    {
        Map<String, List<Player>> ipMap = new HashMap<>();
        for (Player player : world)
        {
            if (!player.isInsideZone(ZoneId.SIEGE) || player.getHWID() == null || player.getClient().isDetached())
                continue;
			String ip = activeChar.getHWID();
			String playerIp = player.getHWID();
			
			if (IPProtection(playerIp))
			{
			    if (ipMap.get(ip) == null)
			        ipMap.put(ip, new ArrayList<Player>());
			    
			    ipMap.get(ip).add(player);
			    
			    if (ipMap.get(ip).size() >= numberBox)
			        return true;
			}
        }
        return false;
    }

    public boolean checkPlayersArea_ip(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedLogOut)
    {
        if (checkPlayersKickTask_ip(activeChar, numberBox, world))
        {
            if (forcedLogOut)
            {
            	activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS"," Double box not allowed in Siege Zone!"));
				for (Player allgms : AdminData.getInstance().getAllGms(true))
				{
					if (!activeChar.isGM())
						allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double IP]", activeChar.getName() +" in Siege Zone!"));
				}
            //	RandomTeleport(activeChar);
				ThreadPool.schedule(() -> RandomTeleport(activeChar), 2000);

            }
            return true;
        }
        return false;
    }

	public static synchronized boolean IPProtection(final String ip)
	{
		boolean result = true;
		try (final Connection con = ConnectionPool.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT ip FROM ip_protect_siegezone WHERE ip=?");
			statement.setString(1, ip);
			final ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println("SelectorHelper -> IPProtection: " + e.getMessage());
		}
		return result;
	}
    
    //Giran Coord's
	public void RandomTeleport(Player activeChar)
	{
		switch (Rnd.get(5))
		{
		    case 0:
		    {
		    	int x = 82533 + Rnd.get(100);
		    	int y = 149122 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 1:
		    {
		    	int x = 82571 + Rnd.get(100);
		    	int y = 148060 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3467, 0);
		    	break;
		    }
		    case 2:
		    {
		    	int x = 81376 + Rnd.get(100);
		    	int y = 148042 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 3:
		    {
		    	int x = 81359 + Rnd.get(100);
		    	int y = 149218 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 4:
		    {
		    	int x = 82862 + Rnd.get(100);
		    	int y = 148606 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
	    }
	}
	
    private static class SingletonHolder
    {
        protected static final SiegeZoneManager _instance = new SiegeZoneManager();
    }

    public static final SiegeZoneManager getInstance()
    {
        return SingletonHolder._instance;
    }

    public boolean checkClanArea(Player activeChar, Integer numberBox, Boolean forcedTeleport)
    {
        if (checkClanAreaKickTask(activeChar, numberBox))
        {
            if (forcedTeleport)
            {
            	activeChar.sendPacket(new ExShowScreenMessage("Allowed only " + numberBox + " clans members on this area!", 6 * 1000));
          //  	RandomTeleport(activeChar);
            	ThreadPool.schedule(() -> RandomTeleport(activeChar), 2000);

            }
            return true;
        }
        return false;
    }
    private static boolean checkClanAreaKickTask(Player activeChar, Integer numberBox)
    {
        Map<String, List<Player>> zergMap = new HashMap<>();
        
        Clan clan = activeChar.getClan();
        
        if (clan != null)
        {
        	for (Player player : clan.getOnlineMembers())
        	{
        //		if (!player.isInsideZone(ZoneId.RAID_ZONE) || player.getClan() == null)
        		if (!player.isInsideZone(ZoneId.SIEGE) || player.getClan() == null)
        			continue;
				String zerg1 = activeChar.getClan().getName();
				String zerg2 = player.getClan().getName();

				if (zerg1.equals(zerg2))
				{
					if (zergMap.get(zerg1) == null)
						zergMap.put(zerg1, new ArrayList<Player>());

					zergMap.get(zerg1).add(player);

					if (zergMap.get(zerg1).size() > numberBox)
						return true;
				}
        	}
        }
        return false;
    }
    public boolean checkAllyArea(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedTeleport)
    {
        if (checkAllyAreaKickTask(activeChar, numberBox, world))
        {
            if (forcedTeleport)
            {
            	activeChar.sendPacket(new ExShowScreenMessage("Allowed only " + numberBox + " ally members on this area!", 6 * 1000));
        //    	RandomTeleport(activeChar);
            	ThreadPool.schedule(() -> RandomTeleport(activeChar), 2000);

            }
            return true;
        }
        return false;
    }

    private static boolean checkAllyAreaKickTask(Player activeChar, Integer numberBox, Collection<Player> world)
    {
    	Map<String, List<Player>> zergMap = new HashMap<>();
    	
        if (activeChar.getAllyId() != 0)
        {
    	for (Player player : world)
    	{
		//	if (!player.isInsideZone(ZoneId.RAID_ZONE) || player.getAllyId() == 0)
    		if (!player.isInsideZone(ZoneId.SIEGE) || player.getAllyId() == 0)
				continue;
			String zerg1 = activeChar.getClan().getAllyName();
			String zerg2 = player.getClan().getAllyName();

			if (zerg1.equals(zerg2))
			{
				if (zergMap.get(zerg1) == null)
					zergMap.put(zerg1, new ArrayList<Player>());

				zergMap.get(zerg1).add(player);

				if (zergMap.get(zerg1).size() > numberBox)
					return true;
			}
    	}
        }
    	return false;
    }
}