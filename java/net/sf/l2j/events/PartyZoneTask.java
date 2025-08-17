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
package net.sf.l2j.events;

import net.sf.l2j.gameserver.PartyFarmEvent;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCustom;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.Config;

public abstract class PartyZoneTask
{
	/** The _in progress. */
	public static boolean _started = false;
	public static boolean _aborted = false;
	
	public static void SpawnEvent()
	{
		spawnMonsters();
		
		Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Special Rewards.");
		Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Teleport Now!");
		Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Duration: " + Config.PARTYZONE_TIME + " minute(s)!");
		
		_aborted = false;
		_started = true;
		
		waiter(Config.PARTYZONE_TIME * 60 * 1000); // minutes for event time
		
		if (!_aborted)
			finishEvent();
	}
	public static void spawnMonsters()
	{
	//	openSpoilDoors();
	//	closeSpoilDoors();
		
		int[] coord;
		for (int i = 0; i < Config.MONSTER_LOCS_COUNT; i++)
		{
			coord = Config.MONSTER_LOCS[i];
			monsters.add(spawnNPC(coord[0], coord[1], coord[2], Config.monsterId));
		}
	}
	/*	private static void closeSpoilDoors()
	{
		if (Config.CLOSE_DOORS_PARTY_FARM)
		{
			for (int i : Config.PARTY_CLOSE_DOORS)
			{
				Door door = DoorTable.getInstance().getDoor(i);
				door.closeMe();
			}
		}
	}
	
	private static void openSpoilDoors()
	{
		if (Config.OPEN_DOORS_PARTY_FARM)
		{
			for (int i : Config.PARTY_OPEN_DOORS)
			{
				Door door = DoorTable.getInstance().getDoor(i);
				door.openMe();
			}
		}
	}*/
	
	protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		final NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(xPos, yPos, zPos, 0);
			spawn.setRespawnDelay(Config.PARTY_FARM_MONSTER_DALAY);
			
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			
			spawn.setRespawnState(true);
			spawn.doSpawn(false);
			spawn.getNpc().isAggressive();
			spawn.getNpc().decayMe();
			spawn.getNpc().spawnMe(spawn.getNpc().getX(), spawn.getNpc().getY(), spawn.getNpc().getZ());
			spawn.getNpc().broadcastPacket(new MagicSkillUse(spawn.getNpc(), spawn.getNpc(), 1034, 1, 1, 1));
			return spawn;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static List<L2Spawn> monsters = new CopyOnWriteArrayList<>();
	
	protected static void unSpawnMonsters()
	{
		for (L2Spawn s : monsters)
		{
			if (s == null)
			{
				monsters.remove(s);
				continue;
			}
			
			s.getNpc().deleteMe();
			s.setRespawnState(false);
			SpawnTable.getInstance().deleteSpawn(s, true);
			monsters.remove(s);
		}
	}
	public static void finishEvent()
	{
		Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Event Finished!");
		//PartyFarm.unSpawnMonsters();
		unSpawnMonsters();
		
		_started = false;
		
		if (!AdminCustom._partytask_manual)
			PartyFarmEvent.getInstance().StartCalculationOfNextEventTime();
		else
			AdminCustom._partytask_manual = false;
		
		PartyFarmEvent.getInstance().getNextTime();
		
	}
	
	/**
	 * Checks if is _started.
	 * @return the _started
	 */
	public static boolean is_started()
	{
		return _started;
	}
	
	/**
	 * Waiter.
	 * @param interval the interval
	 */
	/*protected static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			switch (seconds)
			{
				case 3600: // 1 hour left
					
					if (_started)
					{
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Special Rewards.");
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Teleport Now!");
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" " + seconds / 60 / 60 + " hour(s) till event finish!");
					}
					break;
				case 1800: // 30 minutes left
				case 900: // 15 minutes left
				case 600: // 10 minutes left
				case 300: // 5 minutes left
				case 240: // 4 minutes left
				case 180: // 3 minutes left
				case 120: // 2 minutes left
				case 60: // 1 minute left
					// removeOfflinePlayers();
					
					if (_started)
					{
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Special Rewards.");
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" Teleport Now!");
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" " + seconds / 60 + " minute(s) till event finish!");
					}
					break;
				case 30: // 30 seconds left
				case 15: // 15 seconds left
				case 10: // 10 seconds left
				case 3: // 3 seconds left
				case 2: // 2 seconds left
				case 1: // 1 seconds left
					if (_started)
						Announcement.AnnounceEvents(""+Config.NAME_EVENT+" " + seconds + " second(s) till event finish!");
					
					break;
			}
			
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
					if(Config.DEBUG)
					ie.printStackTrace();
				}
			}
		}
	}*/
	
	
	protected static void waiter(long interval)
	{
	    long startWaiterTime = System.currentTimeMillis();
	    int seconds = (int) (interval / 1000L);

	    // Loop principal até o evento terminar ou ser abortado
	    while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
	    {
	        seconds--;

	        // Verifica e faz o broadcast conforme o tempo restante
	        if (_started)
	        {
	            // Caso o tempo restante seja uma hora
	            if (seconds == 3600)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 / 60 + " hour(s) till event finish!");
	            }
	            // Caso o tempo restante seja em minutos significativos
	            else if (seconds == 60 || seconds == 120 || seconds == 180 || seconds == 240 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 + " minute(s) till event finish!");
	            }
	            // Caso o tempo restante seja em segundos críticos
	            else if (seconds == 1 || seconds == 2 || seconds == 3 || seconds == 10 || seconds == 15 || seconds == 30)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds + " second(s) till event finish!");
	            }
	        }

	        // Espera por 1 segundo antes de continuar o loop
	        try
	        {
	            Thread.sleep(1000L);  // Espera de 1 segundo
	        }
	        catch (InterruptedException ie)
	        {
	            if (Config.DEBUG_PATH)
	                ie.printStackTrace();
	        }
	    }
	}
}