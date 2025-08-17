package net.sf.l2j.gameserver.instancemanager;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.L2GameClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IPManager
{
    private static final Logger _log = Logger.getLogger(IPManager.class.getName());

    public IPManager()
    {
        _log.log(Level.INFO, "IPManager - Loaded.");
    }

 /*   private static boolean multiboxKickTask(Player activeChar, Integer numberBox, Collection<Player> world)
    {
        Map<String, List<Player>> ipMap = new HashMap<String, List<Player>>();
        for (Player player : world)
        {
            if (player.getClient() == null || player.getClient().isDetached())
                continue;
            else
            {
                String ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
                String playerIp = player.getClient().getConnection().getInetAddress().getHostAddress();
                
                if (ip.equals(playerIp))
                {
                    if (ipMap.get(ip) == null)
                        ipMap.put(ip, new ArrayList<Player>());
                    
                    ipMap.get(ip).add(player);
                    
                    if (ipMap.get(ip).size() >= numberBox)
                        return true;
                }
            }
        }
        return false;
    }*/
    private static boolean multiboxKickTask(Player activeChar, Integer numberBox, Collection<Player> world)
    {
        Map<String, List<Player>> ipMap = new HashMap<String, List<Player>>();
        for (Player player : world)
        {
            if (player.getHWID() == null || player.getClient().isDetached())
                continue;
			String ip = activeChar.getHWID();
			String playerIp = player.getHWID();
			
			if (ip.equals(playerIp))
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

    public boolean validBox(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedLogOut)
    {
        if (multiboxKickTask(activeChar, numberBox, world))
        {
            if (forcedLogOut)
            {
                L2GameClient client = activeChar.getClient();
                _log.warning("Multibox Protection: " + client.getHWID() + " was trying to use over " + numberBox + " clients!");
              //  Util.handleIllegalPlayerAction(activeChar, "Multibox Protection: " + client.getConnection().getInetAddress().getHostAddress() + " was trying to use over " + numberBox + " clients!", 1);
                activeChar.sendMessage("You break the limit of " + 1 + " box per IP.");
            }
            return true;
        }
        return false;
    }
    
    private static class SingletonHolder
    {
        protected static final IPManager _instance = new IPManager();
    }

    public static final IPManager getInstance()
    {
        return SingletonHolder._instance;
    }
}