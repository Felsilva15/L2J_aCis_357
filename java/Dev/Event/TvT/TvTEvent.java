package Dev.Event.TvT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StringUtil;
import net.sf.l2j.gameserver.data.DoorTable;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.instancemanager.IPManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Party.MessageType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.RewardHolder;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.util.Broadcast;

import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvTFortress.FOSEvent;

public class TvTEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());
	/** html path **/
	private static final String htmlPath = "data/html/mods/events/tvt/";
	/** The teams of the TvTEvent */
	private static TvTEventTeam[] _teams = new TvTEventTeam[2];
	/** The state of the TvTEvent */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc */
	private static Npc _lastNpcSpawn = null;
	private static int lastAreaIndex = -1; // variável para guardar a última área usada

	public static void init()
	{
	    List<TvTAreasLoader.Area> areasList = TvTAreasLoader.getAreas();
	    if (areasList.isEmpty())
	    {
	        System.err.println("TvT: Nenhuma área carregada do XML! Evento não iniciado.");
	        return;
	    }

	    int index;
	    if (areasList.size() == 1)
	    {
	        index = 0;
	    }
	    else
	    {
	        do
	        {
	            index = Rnd.get(areasList.size());
	        }
	        while (index == lastAreaIndex);
	    }

	    lastAreaIndex = index;
	    _chosenArea = areasList.get(index);

	    // Aqui passa a lista de spawns diretamente
	    _teams[0] = new TvTEventTeam(_chosenArea.team1Name, _chosenArea.team1Spawns);
	    _teams[1] = new TvTEventTeam(_chosenArea.team2Name, _chosenArea.team2Spawns);

	    // _log.info("TvT: Evento iniciado com a área aleatória [" + index + "] - " + _chosenArea.team1Name + " vs " + _chosenArea.team2Name);
	    Broadcast.gameAnnounceToOnlinePlayers("The TvT Event has started in area: " + _chosenArea.name);


	}

	/**
	 * Converte string "x,y,z" para int[]
	 * @param coords 
	 * @return 
	 */
//	private static int[] parseCoords(String coords)
//	{
//	    String[] parts = coords.split(",");
//	    return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
//	}


	/**
	 * No instance of this class!
	 */
	private TvTEvent()
	{
	}
	
	/**
	 * Teams initializing
	 */
//	public static void init()
//	{
//		_teams[0] = new TvTEventTeam(TvTConfig.TVT_EVENT_TEAM_1_NAME, TvTConfig.TVT_EVENT_TEAM_1_COORDINATES);
//		_teams[1] = new TvTEventTeam(TvTConfig.TVT_EVENT_TEAM_2_NAME, TvTConfig.TVT_EVENT_TEAM_2_COORDINATES);
//	}
	
	/**
	 * Starts the participation of the TvTEvent
	 * 1. Get L2NpcTemplate by TvTProperties.TVT_EVENT_PARTICIPATION_NPC_ID
	 * 2. Try to spawn a new npc of it
	 * 
	 * @return boolean: true if success, otherwise false
	 */
	/*public static boolean startParticipation()
	{
		NpcTemplate tmpl = NpcTable.getInstance().getTemplate(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("TvTEventEngine: L2EventManager is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.getAmount();
			_npcSpawn.setHeading(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Event");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTEventEngine: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}*/
	public static boolean startParticipation()
	{
		NpcTemplate tmpl = NpcTable.getInstance().getTemplate(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
		//	_log.warning("TvTEventEngine[DMEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			_log.warning("TvTEventEngine: L2TvTEvent is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			//	_npcSpawn.setLocx(DMConfig.DM_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			//	_npcSpawn.setLocy(DMConfig.DM_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			//	_npcSpawn.setLocz(DMConfig.DM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setLoc(TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0], TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1],TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			//		_npcSpawn.getAmount();
			//	_npcSpawn.setHeading(DMConfig.DM_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			_npcSpawn.setRespawnState(false);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			//	_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.doSpawn(false);
			//	_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Event");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLocX(), _npcSpawn.getLocY(), _npcSpawn.getLocZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTEventEngine: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, Player> players)
	{
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (Player player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	/**
	 * Starts the TvTEvent fight
	 * 1. Set state EventState.STARTING
	 * 2. Close doors specified in configs
	 * 3. Abort if not enought participants(return false)
	 * 4. Set state EventState.STARTED
	 * 5. Teleport all participants to team spot
	 * 
	 * @return boolean: true if success, otherwise false
	 */
	public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Randomize and balance team distribution
		Map<Integer, Player> allParticipants = new HashMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		
		int balance[] =
		{
			0,
			0
		}, 
		priority = 0, highestLevelPlayerId;
		Player highestLevelPlayer;
		// XXX: allParticipants should be sorted by level instead of using highestLevelPcInstanceOf for every fetch
		while (!allParticipants.isEmpty())
		{
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Exiting if no more players
			if (allParticipants.isEmpty())
			{
				break;
			}
			// The other team gets one player
			// XXX: Code not dry
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		// Check for enought participants
		if ((_teams[0].getParticipatedPlayerCount() < TvTConfig.TVT_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < TvTConfig.TVT_EVENT_MIN_PLAYERS_IN_TEAMS))
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}
		
		// Fecha as portas da arena selecionada no XML
		TvTAreasLoader.Area area = getChosenArea(); // substitua por TvTEvent.getChosenArea() se necessário
		if (area != null && area.doorsToClose != null && !area.doorsToClose.isEmpty())
		{
			for (int doorId : area.doorsToClose)
			{
				Door door = DoorTable.getInstance().getDoor(doorId);
				if (door != null)
					door.closeMe();
			}
		}
		 
		// Set state STARTED
		setState(EventState.STARTED);
		
		// Iterate over all teams
		// Cria uma instância única para o evento TvT usando seu sistema
		Dev.Event.Tournament.Instance instance = Dev.Event.Tournament.InstanceManager.getInstance().createInstance();

		for (TvTEventTeam team : _teams)
		{
			for (Player playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					// Aplica sua instância custom no player
					playerInstance.setInstance(instance, true);

					// Teleporta o jogador (já vai na instância correta)
					new TvTEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}

		
		return true;
	}
	
	/**
	 * Calculates the TvTEvent reward
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding
	 * 2. Wait till teams are not at a tie anymore
	 * 3. Set state EvcentState.REWARDING
	 * 4. Reward team with more points
	 * 5. Show win html to wining team participants
	 * @param team 
	 * @param rewards 
	 */
//	public static String calculateRewards()
//	{
//		if (_teams[0].getPoints() == _teams[1].getPoints())
//		{
//			// Check if one of the teams have no more players left
//			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
//			{
//				// set state to rewarding
//				setState(EventState.REWARDING);
//				// return here, the fight can't be completed
//				return "Team vs Team: Event has ended. No team won due to inactivity!";
//			}
//			
//			// Both teams have equals points
//			sysMsgToAllParticipants("Event has ended, both teams have tied.");
//			if (TvTConfig.TVT_REWARD_TEAM_TIE)
//			{
//				rewardTeamWin(_teams[0]);
//				rewardTeamWin(_teams[1]);
//				return "Team vs Team: Event has ended with both teams tying.";
//			}
//			return "Team vs Team: Event has ended with both teams tying.";
//		}
//		
//		// Set state REWARDING so nobody can point anymore
//		setState(EventState.REWARDING);
//		
//		// Get team which has more points
//		//TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
//		//rewardTeam(team);
//		
//		rewardTeamWin(teamsResult()[0]);
//		rewardTeamLos(teamsResult()[1]);
//		
//		return "Team vs Team: Event finish! Team " + teamsResult()[0].getName() + " won with " + teamsResult()[0].getPoints() + " kills!";
//	}
	private static void rewardTeamWin(TvTEventTeam team, List<RewardHolder> rewards)
	{
	    if (rewards == null || rewards.isEmpty())
	        return;

	    for (Player playerInstance : team.getParticipatedPlayers().values())
	    {
	        if (playerInstance == null)
	            continue;

	        if (TvTConfig.TVT_REWARD_PLAYER && !team.onScoredPlayer(playerInstance.getObjectId()))
	            continue;

	        if (Config.ACTIVE_MISSION_TVT)
	        {
	            if (!playerInstance.check_obj_mission(playerInstance.getObjectId()))
	                playerInstance.updateMission();

	            if (!playerInstance.isTvTCompleted() && playerInstance.getTvTCont() < Config.MISSION_TVT_CONT)
	                playerInstance.setTvTCont(playerInstance.getTvTCont() + 1);
	        }

	        for (RewardHolder reward : rewards)
	        {
	            if (Rnd.get(100) <= reward.getRewardChance())
	            {
	                int amount = Rnd.get(reward.getRewardMin(), reward.getRewardMax());
	                if (amount <= 0)
	                    amount = 1;

	                if (playerInstance.isVip())
	                    amount = amount * Config.RATE_DROP_VIP;

	                playerInstance.addItem("TvT Reward", reward.getRewardId(), amount, playerInstance, true);
	            }
	        }

	        StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
	        statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());

	        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
	        // Certifique-se que htmlPath é inicializado, ou substitua por string direta:
	        npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward.htm"));

	        playerInstance.sendPacket(statusUpdate);
	        playerInstance.sendPacket(npcHtmlMessage);
	    }
	}


	public static TvTEventTeam [] teamsResult()
	{
		TvTEventTeam[] teams = new TvTEventTeam[2];
		if(_teams[0].getPoints() > _teams[1].getPoints())
		{
			teams[0] = _teams[0];
			teams[1] = _teams[1];
		}
		else
		{
			teams[0] = _teams[1];
			teams[1] = _teams[0];
		}
		return teams;
	}
	
//	private static void rewardTeamWin(TvTEventTeam team)
//	{
//		// Iterate over all participated player instances of the winning team
//		for (Player playerInstance : team.getParticipatedPlayers().values())
//		{
//			// Check for nullpointer
//			if (playerInstance == null)
//                continue;
//			
//            // Checks if the player scored points.
//			if (TvTConfig.TVT_REWARD_PLAYER && !team.onScoredPlayer(playerInstance.getObjectId()))
//				continue;
//
//		//	if (Config.ACTIVE_MISSION_TVT)
//		//	{							
//		//		if (!playerInstance.checkMissions(playerInstance.getObjectId()))
//		//			playerInstance.updateMission();
//
//		//		if (!(playerInstance.isTvTCompleted() || playerInstance.getTvTCont() >= Config.MISSION_TVT_COUNT))
//		//			playerInstance.setTvTCont(playerInstance.getTvTCont() + 1);
//		//	}
//			if (Config.ACTIVE_MISSION_TVT)
//			{
//				if (!playerInstance.check_obj_mission(playerInstance.getObjectId()))
//					playerInstance.updateMission(); 
//				if (!playerInstance.isTvTCompleted() && playerInstance.getTvTCont() < Config.MISSION_TVT_CONT)
//					playerInstance.setTvTCont(playerInstance.getTvTCont() + 1); 
//			} 
//
//    	//	for (RewardHolder reward : TvTConfig.TVT_EVENT_REWARDS_WIN)
//    	//	{
//    	//		if (Rnd.get(100) <= reward.getRewardChance())
//    	//		{
//		//			if (playerInstance.isVip())
//		//			{
//		//				if (reward.getRewardId() == 9500)
//		//					playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//		//				else if (reward.getRewardId() == 9501)
//		//					playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//		//				else
//		//					playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//		//			}
//    	//			else
//    	//				playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//    	//		}
//    	//	}
//    		for (RewardHolder reward : TvTConfig.TVT_EVENT_REWARDS_WIN)
//    		{
//    			if (Rnd.get(100) <= reward.getRewardChance())
//    			{
//    				if (playerInstance.isVip())
//    	    		{
//    	    			playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//    	    		}
//    	    		else
//    	    		{
//    					playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//    	    		}	
//    			}
//    		}
//    		
//    		
//			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
//			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
//			
//			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
//			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward.htm"));
//			playerInstance.sendPacket(statusUpdate);
//			playerInstance.sendPacket(npcHtmlMessage);
//		}
//	}
	
//	private static void rewardTeamLos(TvTEventTeam team)
//	{
//		// Iterate over all participated player instances of the winning team
//		for (Player playerInstance : team.getParticipatedPlayers().values())
//		{
//			// Check for nullpointer
//			if (playerInstance == null)
//                continue;
//			
//            // Checks if the player scored points.
//			if (TvTConfig.TVT_REWARD_PLAYER && !team.onScoredPlayer(playerInstance.getObjectId()))
//				continue;
//
//			if (Config.ACTIVE_MISSION_TVT)
//			{
//				if (!playerInstance.check_obj_mission(playerInstance.getObjectId()))
//					playerInstance.updateMission(); 
//				if (!playerInstance.isTvTCompleted() && playerInstance.getTvTCont() < Config.MISSION_TVT_CONT)
//					playerInstance.setTvTCont(playerInstance.getTvTCont() + 1); 
//			} 
//
//		/*	
//    		for (RewardHolder reward : TvTConfig.TVT_EVENT_REWARDS_LOS)
//    		{
//    			if (Rnd.get(100) <= reward.getRewardChance())
//    			{
//					if (playerInstance.isVip())
//					{
//						if (reward.getRewardId() == 9500)
//							playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//						else if (reward.getRewardId() == 9501)
//							playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//						else
//							playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//					}
//    				else
//    					playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//    			}
//    		}*/
//			for (RewardHolder reward : TvTConfig.TVT_EVENT_REWARDS_LOS)
//    		{
//    			if (Rnd.get(100) <= reward.getRewardChance())
//    			{
//    				if (playerInstance.isVip())
//    	    		{
//    	    			playerInstance.addItem("TvT Reward", reward.getRewardId(), (int) (Rnd.get(reward.getRewardMin(), reward.getRewardMax()) * Config.VIP_DROP_RATE), playerInstance, true);
//    	    		}
//    	    		else
//    	    		{
//    					playerInstance.addItem("TvT Reward", reward.getRewardId(), Rnd.get(reward.getRewardMin(), reward.getRewardMax()), playerInstance, true);
//    	    		}	
//    			}
//    		}
//
//			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
//			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
//			playerInstance.sendPacket(statusUpdate);
//		}
//	}
	
	/**
	 * Stops the TvTEvent fight
	 * 1. Set state EventState.INACTIVATING
	 * 2. Remove tvt npc from world
	 * 3. Open doors specified in configs
	 * 4. Teleport all participants back to participation npc location
	 * 5. Teams cleaning
	 * 6. Set state EventState.INACTIVE
	 */
	public static void stopFight()
	{
	    // Set state INACTIVATING
	    setState(EventState.INACTIVATING);
	    
	    // Unspawn event npc
	    unSpawnNpc();
	    
	    // Paralisar todos os jogadores imediatamente
	    paralyzeAll();
	    
	    // Abre as portas da área escolhida (via XML)
	    TvTAreasLoader.Area area = getChosenArea();
	    if (area != null && area.doorsToOpen != null && !area.doorsToOpen.isEmpty())
	    {
	        openDoors(area.doorsToOpen);
	    }
	    
	    // Itera sobre todos os times
	    for (TvTEventTeam team : _teams)
	    {
	        for (final Player playerInstance : team.getParticipatedPlayers().values())
	        {
	            if (playerInstance != null)
	            {
	                // Remove da instância do evento (coloca na instância 0 = mundo normal)
	                Dev.Event.Tournament.Instance defaultInstance = Dev.Event.Tournament.InstanceManager.getInstance().getInstance(0);
	                playerInstance.setInstance(defaultInstance, true);
	                
	                // Teleporta o jogador de volta com delay
	                if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title") || TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
	                {
	                    ThreadPool.schedule(() ->
	                    {
	                        playerInstance.clearPoints();
	                        
	                        if (playerInstance.getLastX() == 0 && playerInstance.getLastY() == 0)
	                            playerInstance.teleToLocation(
	                                (TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)) - 50,
	                                (TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)) - 50,
	                                TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], 0);
	                        else
	                            playerInstance.teleToLocation(playerInstance.getLastX(), playerInstance.getLastY(), playerInstance.getLastZ(), 0);
	                        
	                        playerInstance.setTeam(0);
	                        
	                        // Remove efeitos de paralisia APÓS o teleporte
	                //        playerInstance.stopAbnormalEffect(AbnormalEffect.ROOT);
	                        playerInstance.setIsParalyzed(false);
	                        playerInstance.setIsImmobilized(false);
	                        
	                        // Remove outras restrições
	                        playerInstance.abortAttack();
	                        playerInstance.abortCast();
	                        
	                        playerInstance.setLastCords(0, 0, 0);
	                    }, TvTConfig.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000);
	                }
	            }
	        }
	    }
	    
	    // Cleanup de times
	    _teams[0].cleanMe();
	    _teams[1].cleanMe();
	    
	    // Set state INACTIVE
	    setState(EventState.INACTIVE);
	}

	public static void paralyzeAll()
	{
	    for (TvTEventTeam team : _teams)
	    {
	        for (Player player : team.getParticipatedPlayers().values())
	        {
	            if (player != null && player.isOnline() && !player.isAlikeDead())
	            {
	           //     player.startAbnormalEffect(AbnormalEffect.ROOT);
	                player.abortAttack();
	                player.abortCast();
	                player.setIsParalyzed(true);
	                player.setIsImmobilized(true);
	            }
	        }
	    }
	}


	private static void rewardTeamLos(TvTEventTeam team, List<RewardHolder> rewards)
	{
	    if (rewards == null || rewards.isEmpty())
	        return;

	    for (Player playerInstance : team.getParticipatedPlayers().values())
	    {
	        if (playerInstance == null)
	            continue;

	        if (TvTConfig.TVT_REWARD_PLAYER && !team.onScoredPlayer(playerInstance.getObjectId()))
	            continue;

	        if (Config.ACTIVE_MISSION_TVT)
	        {
	            if (!playerInstance.check_obj_mission(playerInstance.getObjectId()))
	                playerInstance.updateMission();

	            if (!playerInstance.isTvTCompleted() && playerInstance.getTvTCont() < Config.MISSION_TVT_CONT)
	                playerInstance.setTvTCont(playerInstance.getTvTCont() + 1);
	        }

	        for (RewardHolder reward : rewards)
	        {
	            if (Rnd.get(100) <= reward.getRewardChance())
	            {
	                int amount = Rnd.get(reward.getRewardMin(), reward.getRewardMax());
	                if (amount <= 0)
	                    amount = 1;

	                if (playerInstance.isVip())
	                    amount = amount * Config.RATE_DROP_VIP;

	                playerInstance.addItem("TvT Reward", reward.getRewardId(), amount, playerInstance, true);
	            }
	        }

	        // Atualiza carga
	        StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
	        statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
	        playerInstance.sendPacket(statusUpdate);

	        // Envia feedback visual com HTML
	        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
	        npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward.htm"));
	        playerInstance.sendPacket(npcHtmlMessage);
	    }
	}


	/**
	 * Adds a player to a TvTEvent team
	 * 1. Calculate the id of the team in which the player should be added
	 * 2. Add the player to the calculated team
	 * 
	 * @param playerInstance as Player
	 * @return boolean: true if success, otherwise false
	 */
	public static synchronized boolean addParticipant(Player playerInstance)
	{
		// Check for nullpoitner
		if (playerInstance == null)
			return false;
		
		playerInstance.setLastCords(playerInstance.getX(), playerInstance.getY(), playerInstance.getZ());
		
		byte teamId = 0;
		
		// Check to which team the player should be added
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
			teamId = (byte) (Rnd.get(2));
		else
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	/**
	 * Removes a TvTEvent player from it's team
	 * 1. Get team id of the player
	 * 2. Remove player from it's team
	 * 
	 * @param playerObjectId
	 * @return boolean: true if success, otherwise false
	 */
	public static boolean removeParticipant(int playerObjectId)
	{
		// Get the teamId of the player
		byte teamId = getParticipantTeamId(playerObjectId);
		
		// Check if the player is participant
		if (teamId != -1)
		{
			// Remove the player from team
			_teams[teamId].removePlayer(playerObjectId);
			return true;
		}
		
		return false;
	}
	
	public static boolean payParticipationFee(Player activeChar)
	{
		int itemId = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[1];
		if (itemId == 0 || itemNum == 0)
			return true;
		
		if (activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemNum)
			return false;
		
		return activeChar.destroyItemByItemId("TvT Participation Fee", itemId, itemNum, _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = TvTConfig.TVT_EVENT_PARTICIPATION_FEE[1];
		
		if ((itemId == 0) || (itemNum == 0))
			return "-";
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}
	public static String calculateRewards()
	{
	    if (_teams[0].getPoints() == _teams[1].getPoints())
	    {
	        // Tie scenario
	        if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
	        {
	            setState(EventState.REWARDING);
	            return "Team vs Team: Event ended. No team won due to inactivity!";
	        }

	        sysMsgToAllParticipants("The event has ended with both teams tied.");

	        // Aplica a recompensa de empate para ambos os times
	        rewardTeamWin(_teams[0], _chosenArea.rewardsTie);
	        rewardTeamWin(_teams[1], _chosenArea.rewardsTie);

	        return "Team vs Team: Event ended in a tie between both teams.";
	    }

	    setState(EventState.REWARDING);

	    TvTEventTeam winningTeam = teamsResult()[0];
	    TvTEventTeam losingTeam = teamsResult()[1];

	    // Recompensa para times
	    rewardTeamWin(winningTeam, _chosenArea.rewardsWin);
	    rewardTeamLos(losingTeam, _chosenArea.rewardsLos);

	    // Recompensa extra para o jogador com mais kills
	    giveTopKillReward();

	    return "Team vs Team: Event finished! Team " + winningTeam.getName() + " won with " + winningTeam.getPoints() + " kills!";
	}

	private static void giveTopKillReward()
	{
	    if (_playerKills.isEmpty())
	        return;

	    int maxKills = -1;
	    int topKillerObjId = -1;

	    // Encontrar jogador com mais kills
	    for (Map.Entry<Integer, Integer> entry : _playerKills.entrySet())
	    {
	        if (entry.getValue() > maxKills)
	        {
	            maxKills = entry.getValue();
	            topKillerObjId = entry.getKey();
	        }
	    }

	    if (topKillerObjId == -1)
	        return;

	    Player topKiller = World.getInstance().getPlayer(topKillerObjId);

	    if (topKiller == null)
	        return;

	    // Enviar mensagem para o top killer
	    topKiller.sendMessage("You are the Top Killer in the Team vs Team event! Congratulations!");

	    // Dar recompensas definidas para top kills da área
	    for (RewardHolder reward : _chosenArea.topKillsRewards)
	    {
	        int min = reward.getRewardMin();
	        int max = reward.getRewardMax();
	        int count;

	        if (min == max)
	            count = min;
	        else
	            count = ThreadLocalRandom.current().nextInt(min, max + 1);

	        topKiller.addItem("TvT Top Killer Reward", reward.getRewardId(), count, topKiller, true);
	    }
	}





	private static TvTAreasLoader.Area _chosenArea;
	

	/**
	 * Send a SystemMessage to all participated players
	 * 1. Send the message to all players of team number one
	 * 2. Send the message to all players of team number two
	 * 
	 * @param message as String
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		CreatureSay cs = new CreatureSay(0, Say2.PARTY, "Team vs Team", message);
		
		for (Player playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}
		
		for (Player playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}
	}
	
	/**
	 * Close doors specified in configs
	 * @param doors
	 */
//	private static void closeDoors(List<Integer> doors)
//	{
//	    if (doors == null || doors.isEmpty())
//	        return;
//
//	    for (int doorId : doors)
//	    {
//	        L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
//
//	        if (doorInstance != null)
//	            doorInstance.closeMe();
//	    }
//	}

	
	/**
	 * Open doors specified in configs
	 * @param doors
	 */
	private static void openDoors(List<Integer> doors)
	{
	    if (doors == null || doors.isEmpty())
	        return;

	    for (int doorId : doors)
	    {
	        Door doorInstance = DoorTable.getInstance().getDoor(doorId);

	        if (doorInstance != null)
	            doorInstance.openMe();
	    }
	}

	
	/**
	 * UnSpawns the TvTEvent npc
	 */
/*	private static void unSpawnNpc()
	{
		// Delete the npc
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		// Stop respawning of the npc
	//	_npcSpawn.stopRespawn();
		_npcSpawn.setRespawnState(false);
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}*/
	private static void unSpawnNpc()
	{
		// Delete the npc
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		// Stop respawning of the npc
		_npcSpawn.doRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	/**
	 * Called when a player logs in 
	 * @param playerInstance as Player
	 */
	public static void onLogin(Player playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
			return;
	
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1)
			return;
		
		_teams[teamId].addPlayer(playerInstance);
		
	//	if (TvTConfig.ENABLE_TVT_INSTANCE)
	//	{
	//		playerInstance.noCarrierUnparalyze();
	//		playerInstance.setInstanceId(TvTConfig.TVT_INSTANCE_ID, true);
	//	}

		new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}
	
	/**
	 * Called when a player logs out
	 * 
	 * @param playerInstance as Player
	 */
	public static void onLogout(Player playerInstance)
	{
		if ((playerInstance != null) && (isStarting() || isStarted() || isParticipating()))
		{
		//	if (playerInstance.isNoCarrier())
		//		return;
			
			if (removeParticipant(playerInstance.getObjectId()))
			{
				//playerInstance.teleToLocation((TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)) - 50, (TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)) - 50, TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], 0);
				playerInstance.teleToLocation(playerInstance.getLastX(), playerInstance.getLastY(), playerInstance.getLastZ(), 0);
				playerInstance.setLastCords(0, 0, 0);
				playerInstance.setTeam(0);
			}
		}
	}
	
	/**
	 * Called on every bypass by npc of type L2TvTEventNpc
	 * Needs synchronization cause of the max player check
	 * 
	 * @param command as String
	 * @param playerInstance as Player
	 */
	public static synchronized void onBypass(String command, Player playerInstance)
	{
		if (playerInstance == null || !isParticipating())
			return;
		
		final String htmContent;
		
		if (command.equals("tvt_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = playerInstance.getLevel();
			
			if (playerInstance.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "CursedWeaponEquipped.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}			
			else if (playerInstance.isInArenaEvent())
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Tournament.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (OlympiadManager.getInstance().isRegistered(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Olympiad.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (playerInstance.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Karma.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (TvTConfig.DISABLE_ID_CLASSES.contains(playerInstance.getClassId().getId()))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Class.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if ((playerLevel < TvTConfig.TVT_EVENT_MIN_LVL) || (playerLevel > TvTConfig.TVT_EVENT_MAX_LVL))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(TvTConfig.TVT_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(TvTConfig.TVT_EVENT_MAX_LVL));
				}
			}
			else if ((_teams[0].getParticipatedPlayerCount() == TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() == TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "TeamsFull.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(TvTConfig.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
            else if (TvTConfig.TVT_EVENT_MULTIBOX_PROTECTION_ENABLE && onMultiBoxRestriction(playerInstance))
            {
                htmContent = HtmCache.getInstance().getHtm(htmlPath + "MultiBox.htm");
                if (htmContent != null)
                {
                    npcHtmlMessage.setHtml(htmContent);
                    npcHtmlMessage.replace("%maxbox%", String.valueOf(TvTConfig.TVT_EVENT_NUMBER_BOX_REGISTER));
                }
            }
            else if (playerInstance.isAio())
			{
            	playerInstance.sendMessage("AIO charactes are not allowed to participate in events.");
				return;
			}
            else if (playerInstance._inEventCTF)
			{
				playerInstance.sendMessage("You already participated in another event!");
				return;
			}
//            else if (playerInstance.isSellBuff())
//			{
//				playerInstance.sendMessage("Character Sell buffs No Registed in Events!");
//				return;
//			}
           
//			else if (LMEvent.isPlayerParticipant(playerInstance.getObjectId()))
//			{
//				playerInstance.sendMessage("You already participated in another event!");
//				return;
//			}
			else if (DMEvent.isPlayerParticipant(playerInstance.getObjectId()))
			{
				playerInstance.sendMessage("You already participated in another event!");
				return;
			}
			else if (FOSEvent.isPlayerParticipant(playerInstance.getObjectId()))
			{
				playerInstance.sendMessage("You already participated in another event!");
				return;
			}
			else if (KTBEvent.isPlayerParticipant(playerInstance.getObjectId()))
			{
				playerInstance.sendMessage("You already participated in another event!");
				return;
			}
			else if (!payParticipationFee(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(htmlPath + "ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if (addParticipant(playerInstance))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Registered.htm"));
			else
				return;
			
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			removeParticipant(playerInstance.getObjectId());
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Unregistered.htm"));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance
	 * 
	 * @param playerInstance
	 * @param targetedPlayerObjectId
	 * @return boolean: true if player is allowed to target, otherwise false
	 */
	public static boolean onAction(Player playerInstance, int targetedPlayerObjectId)
	{
		if (playerInstance == null || !isStarted())
			return true;
		
		if (playerInstance.isGM())
			return true;
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1)))
		{
			playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((playerTeamId != -1) && (targetedPlayerTeamId != -1) && (playerTeamId == targetedPlayerTeamId) && (playerInstance.getObjectId() != targetedPlayerObjectId) && !TvTConfig.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
		{
			playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every scroll use
	 * 
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use scroll, otherwise false
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_SCROLL_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every potion use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use potions, otherwise false
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_POTIONS_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every escape use(thanks to nbd)
	 * @param playerObjectId
	 * @return boolean: true if player is not in tvt event, otherwise false
	 */
	public static boolean onEscapeUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId))
			return false;
		
		return true;
	}
	
	/**
	 * Called on every summon item use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to summon by item, otherwise false
	 */
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !TvTConfig.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;
		
		return true;
	}
	// Mapa para armazenar kills por jogador (objId -> quantidade de kills)
	private static final Map<Integer, Integer> _playerKills = new HashMap<>();
	/**
	 * Is called when a player is killed
	 * 
	 * @param killerCharacter as Creature
	 * @param killedPlayerInstance as Player
	 */
	public static void onKill(Creature killerCharacter, Player killedPlayerInstance)
	{
	    if (killedPlayerInstance == null || !isStarted())
	        return;

	    byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());

	    if (killedTeamId == -1)
	        return;

	    // Teleportar jogador morto para ponto do time
	    new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);

	    if (killerCharacter == null)
	        return;

	    Player killerPlayerInstance = null;

	    if ((killerCharacter instanceof Pet) || (killerCharacter instanceof Summon))
	    {
	        killerPlayerInstance = ((Summon) killerCharacter).getOwner();

	        if (killerPlayerInstance == null)
	            return;
	    }
	    else if (killerCharacter instanceof Player)
	        killerPlayerInstance = (Player) killerCharacter;
	    else
	        return;

	    byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());

	    if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
	    {
	        TvTEventTeam killerTeam = _teams[killerTeamId];

	        // Aumenta pontos do time
	        killerTeam.increasePoints();

	        // Aumenta pontos individuais do time (se implementado)
	        killerTeam.increasePoints(killerPlayerInstance.getObjectId());

	        // ** Contabilizar kills do jogador para a recompensa Top Killer **
	        int currentKills = _playerKills.getOrDefault(killerPlayerInstance.getObjectId(), 0);
	        _playerKills.put(killerPlayerInstance.getObjectId(), currentKills + 1);

	        // Mensagens dependendo da config
	        if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pm"))
	        {
	            sysMsgToAllParticipants(killerPlayerInstance.getName() + " Hunted Player " + killedPlayerInstance.getName() + "!");
	        }
	        else if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("title"))
	        {
	            killerPlayerInstance.increasePointScore();
	            killerPlayerInstance.broadcastCharInfo();
	        }
	        else if (TvTConfig.TVT_EVENT_ON_KILL.equalsIgnoreCase("pmtitle"))
	        {
	            sysMsgToAllParticipants(killerPlayerInstance.getName() + " Hunted Player " + killedPlayerInstance.getName() + "!");
	            killerPlayerInstance.increasePointScore();
	            killerPlayerInstance.broadcastCharInfo();
	        }
	    }
	}

	
	/**
	 * Called on Appearing packet received (player finished teleporting)
	 * @param playerInstance
	 */
	public static void onTeleported(Player playerInstance)
	{
		if (!isStarted() || (playerInstance == null) || !isPlayerParticipant(playerInstance.getObjectId()))
			return;

		if (playerInstance.isMageClass())
		{
			if (TvTConfig.TVT_EVENT_MAGE_BUFFS != null && !TvTConfig.TVT_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (int i : TvTConfig.TVT_EVENT_MAGE_BUFFS.keySet())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, TvTConfig.TVT_EVENT_MAGE_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
		else
		{
			if (TvTConfig.TVT_EVENT_FIGHTER_BUFFS != null && !TvTConfig.TVT_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (int i : TvTConfig.TVT_EVENT_FIGHTER_BUFFS.keySet())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, TvTConfig.TVT_EVENT_FIGHTER_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
		removeParty(playerInstance);
	}
	
	/**
	 * @param source
	 * @param target
	 * @param skill
	 * @return true if player valid for skill
	 */
	public static final boolean checkForTvTSkill(Player source, Player target, L2Skill skill)
	{
		if (!isStarted())
			return true;

		// TvT is started
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		// both players not participating
		if (!isSourceParticipant && !isTargetParticipant)
			return true;

		// one player not participating
		if (!(isSourceParticipant && isTargetParticipant))
			return false;

		// players in the different teams?
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
				return false;
		}
		return true;
	}
	
	/**
	 * Sets the TvTEvent state
	 * 
	 * @param state as EventState
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	/**
	 * Is TvTEvent inactive?
	 * 
	 * @return boolean: true if event is inactive(waiting for next event cycle), otherwise false
	 */
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	/**
	 * Is TvTEvent in inactivating?
	 * 
	 * @return boolean: true if event is in inactivating progress, otherwise false
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;
		
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	/**
	 * Is TvTEvent in participation?
	 * 
	 * @return boolean: true if event is in participation progress, otherwise false
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	/**
	 * Is TvTEvent starting?
	 * 
	 * @return boolean: true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false
	 */
	public static boolean isStarting()
	{
		boolean isStarting;
		
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	/**
	 * Is TvTEvent started?
	 * 
	 * @return boolean: true if event is started, otherwise false
	 */
	public static boolean isStarted()
	{
		boolean isStarted;
		
		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}
		
		return isStarted;
	}
	
	/**
	 * Is TvTEvent rewarding?
	 * 
	 * @return boolean: true if event is currently rewarding, otherwise false
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;
		
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1
	 * @param playerObjectId
	 * @return byte: team name of the given playerName, if not in event -1
	 */
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	/**
	 * Returns the team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return TvTEventTeam: team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	/**
	 * Returns the enemy team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return TvTEventTeam: enemy team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null
	 * @param playerObjectId
	 * @return int[]: coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null);
	}
	
	/**
	 * Is given player participant of the event?
	 * @param playerObjectId
	 * @return boolean: true if player is participant, ohterwise false
	 */
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return false;
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	/**
	 * Returns participated player count
	 * 
	 * @return int: amount of players registered in the event
	 */
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return 0;
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names
	 * 
	 * @return String[]: names of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	/**
	 * Returns player count of both teams
	 * 
	 * @return int[]: player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount()
		};
	}
	
	/**
	 * Returns points count of both teams
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(), _teams[1].getPoints()
		};
	}
	
	public static void removeParty(Player activeChar)
	{
		if (activeChar.getParty() != null)
		{
			L2Party party = activeChar.getParty();
			party.removePartyMember(activeChar, MessageType.Left);
		}
	}
	
	public static List<Player> allParticipants()
	{
	    List<Player> players = new ArrayList<>();
	    players.addAll(_teams[0].getParticipatedPlayers().values());
	    players.addAll(_teams[1].getParticipatedPlayers().values());
	    return players;
	}


 //   public static boolean onMultiBoxRestriction(L2PcInstance activeChar)
  //  {
   // 	return HwidManager.getInstance().validBox(activeChar, TvTConfig.TVT_EVENT_NUMBER_BOX_REGISTER, allParticipants(), false);
    //}
    public static boolean onMultiBoxRestriction(Player activeChar)
	{
		return IPManager.getInstance().validBox(activeChar, TvTConfig.TVT_EVENT_NUMBER_BOX_REGISTER, allParticipants(), false);
	}
    
	public static class TvTAntiAfk
	{
		// Delay between location checks , Default 60000 ms (1 minute)
		private final int CheckDelay = 20000;

		private static List<String> TvTPlayerList = new ArrayList<>();

		private static String[] Splitter;
		private static int xx, yy, zz, SameLoc;
		private static Player _player;
		
		private TvTAntiAfk()
		{
			_log.info("TvTAntiAfk: Auto-Kick AFK in TVT System initiated.");
			ThreadPool.scheduleAtFixedRate(new AntiAfk(), 20000, CheckDelay);
		}
		
		private class AntiAfk implements Runnable
		{
			@Override
			public void run()
			{
				if (TvTEvent.isStarted())
				{
					synchronized (TvTEvent._teams)
					{
						// Iterate over all teams
						for (TvTEventTeam team : TvTEvent._teams)
						{
							// Iterate over all participated player instances in this team
							for (Player playerInstance : team.getParticipatedPlayers().values())
							{
								if (playerInstance != null && playerInstance.isOnline() && !playerInstance.isDead() /*&& !playerInstance.isPhantom() && !playerInstance.isGM()*/ && !playerInstance.isImmobilized() && !playerInstance.isParalyzed())
								{
									_player = playerInstance;
									AddTvTSpawnInfo(playerInstance.getName(),playerInstance.getX(),playerInstance.getY(),playerInstance.getZ());
								}
							}
						}
					}
				}
				else
				{
					TvTPlayerList.clear();
				}
			}
		}
		
//		private static void AddTvTSpawnInfo(String name, int _x, int _y, int _z)
//		{
//			if (!CheckTvTSpawnInfo(name))
//			{
//				String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
//				TvTPlayerList.add(temp);
//			}
//			else
//			{
//				Object[] elements = TvTPlayerList.toArray();
//				for(int i=0; i < elements.length ; i++)
//				{
//					Splitter = ((String) elements[i]).split(":");
//					String nameVal = Splitter[0];
//					if (name.equals(nameVal))
//					{
//						GetTvTSpawnInfo(name);
//						if (_x == xx && _y == yy && _z == zz && _player.isAttackingNow() == false && _player.isCastingNow() == false && _player.isOnline() == true && _player.isParalyzed() == false)
//						{
//							++SameLoc;
//							if (SameLoc >= 4)//Kick after 4 same x/y/z, location checks
//							{
//								//kick here
//								TvTPlayerList.remove(i);
//								onLogout(_player);
//								KickedMsg(_player);
//								return;
//							}
//							else
//							{
//								TvTPlayerList.remove(i);
//								String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":" + SameLoc;
//								TvTPlayerList.add(temp);
//								return;
//							}
//						}
//						TvTPlayerList.remove(i);
//						String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
//						TvTPlayerList.add(temp);
//					}
//				}
//			}
//		}
		private static void AddTvTSpawnInfo(String name, int _x, int _y, int _z)
		{
		    if (!CheckTvTSpawnInfo(name))
		    {
		        String temp = name + ":" + _x + ":" + _y + ":" + _z + ":1";
		        TvTPlayerList.add(temp);
		        return;
		    }

		    Object[] elements = TvTPlayerList.toArray();
		    for (int i = 0; i < elements.length; i++)
		    {
		        String[] Splitter = ((String) elements[i]).split(":");
		        String nameVal = Splitter[0];

		        if (name.equals(nameVal))
		        {
		            GetTvTSpawnInfo(name);

		            if (_x == xx && _y == yy && _z == zz
		                && !_player.isAttackingNow()
		                && !_player.isCastingNow()
		                && _player.isOnline()
		                && !_player.isParalyzed())
		            {
		                ++SameLoc;
		                if (SameLoc >= 4) // Kick after 4 same location checks
		                {
		                    TvTPlayerList.remove(i);
		                    onLogout(_player);
		                    KickedMsg(_player);
		                    return;
		                }

		                TvTPlayerList.remove(i);
		                String temp = name + ":" + _x + ":" + _y + ":" + _z + ":" + SameLoc;
		                TvTPlayerList.add(temp);
		                return;
		            }

		            TvTPlayerList.remove(i);
		            String temp = name + ":" + _x + ":" + _y + ":" + _z + ":1";
		            TvTPlayerList.add(temp);
		            return;
		        }
		    }
		}

		
		private static boolean CheckTvTSpawnInfo(String name)
		{
			Object[] elements = TvTPlayerList.toArray();
			for(int i=0; i < elements.length ; i++)
			{
				Splitter = ((String) elements[i]).split(":");
				String nameVal = Splitter[0];
				if (name.equals(nameVal))
					return true;
			}
			return false;
		}

		private static void GetTvTSpawnInfo(String name)
		{
			Object[] elements = TvTPlayerList.toArray();
			for(int i=0; i < elements.length ; i++)
			{
				Splitter = ((String) elements[i]).split(":");
				String nameVal = Splitter[0];
				if (name.equals(nameVal))
				{
					xx = Integer.parseInt(Splitter[1]);
					yy = Integer.parseInt(Splitter[2]);
					zz = Integer.parseInt(Splitter[3]);
					SameLoc = Integer.parseInt(Splitter[4]);
				}
			}
		}
		
	    private static void KickedMsg(Player playerInstance)
	    {
	    	playerInstance.sendPacket(new ExShowScreenMessage("You're kicked out of the TvT by staying afk!", 6000));
	    }
	    
		public static TvTAntiAfk getInstance()
		{
			return SingletonHolder._instance;
		}
		
		private static class SingletonHolder
		{
			protected static final TvTAntiAfk _instance = new TvTAntiAfk();
		}
	}

	public static TvTAreasLoader.Area getChosenArea()
	{
	    return _chosenArea;
	}

}