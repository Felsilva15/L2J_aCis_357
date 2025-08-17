package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.logging.LogManager;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;
import net.sf.l2j.Team;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;
import net.sf.l2j.commons.util.SysUtil;
import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.events.manager.CTFEventManager;
import net.sf.l2j.events.pvpevent.PvPEventManager;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.BufferTable;
import net.sf.l2j.gameserver.data.CharTemplateTable;
import net.sf.l2j.gameserver.data.DoorTable;
import net.sf.l2j.gameserver.data.IconTable;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.MapRegionTable;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.data.PlayerNameTable;
import net.sf.l2j.gameserver.data.RecipeTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTreeTable;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.data.sql.BookmarkTable;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.ServerMemoTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.data.xml.AugmentationData;
import net.sf.l2j.gameserver.data.xml.DressMeData;
import net.sf.l2j.gameserver.data.xml.FishData;
import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.data.xml.HerbDropData;
import net.sf.l2j.gameserver.data.xml.MultisellData;
import net.sf.l2j.gameserver.data.xml.NewbieBuffData;
import net.sf.l2j.gameserver.data.xml.PkColorTable;
import net.sf.l2j.gameserver.data.xml.PvpColorTable;
import net.sf.l2j.gameserver.data.xml.SoulCrystalData;
import net.sf.l2j.gameserver.data.xml.SpellbookData;
import net.sf.l2j.gameserver.data.xml.StaticObjectData;
import net.sf.l2j.gameserver.data.xml.SummonItemData;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.data.xml.WalkerRouteData;
import net.sf.l2j.gameserver.datatables.xml.RouletteData;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ChatHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AioManager;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.AutoSpawnManager;
import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CharacterKillingManager;
import net.sf.l2j.gameserver.instancemanager.ChatBanManager;
import net.sf.l2j.gameserver.instancemanager.ChatGlobalManager;
import net.sf.l2j.gameserver.instancemanager.ChatHeroManager;
import net.sf.l2j.gameserver.instancemanager.CheckManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.FishingChampionshipManager;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.instancemanager.IPManager;
import net.sf.l2j.gameserver.instancemanager.MovieMakerManager;
import net.sf.l2j.gameserver.instancemanager.OlyClassDamageManager;
import net.sf.l2j.gameserver.instancemanager.PartyZoneManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossInfoManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidZoneManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.instancemanager.SoloZoneManager;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.instancemanager.custom.SiegeZoneManager;
import net.sf.l2j.gameserver.instancemanager.games.MonsterRace;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.model.vehicles.BoatGiranTalking;
import net.sf.l2j.gameserver.model.vehicles.BoatGludinRune;
import net.sf.l2j.gameserver.model.vehicles.BoatInnadrilTour;
import net.sf.l2j.gameserver.model.vehicles.BoatRunePrimeval;
import net.sf.l2j.gameserver.model.vehicles.BoatTalkingGludin;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GamePacketHandler;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;
import net.sf.l2j.gameserver.taskmanager.MovementTaskManager;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;
import net.sf.l2j.gameserver.taskmanager.RandomAnimationTaskManager;
import net.sf.l2j.gameserver.taskmanager.ShadowItemTaskManager;
import net.sf.l2j.gameserver.taskmanager.WaterTaskManager;
import net.sf.l2j.gameserver.taskmanager.custom.TaskManager;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.util.DeadLockDetector;
import net.sf.l2j.util.IPv4Filter;

import Dev.BossTimeRespawn.TimeEpicBossManager;
import Dev.BossTimeRespawn.TimeRaidBossManager;
import Dev.Community.MarketPlace.AuctionTableCommunity;
import Dev.DollsSystem.DollsTable;
import Dev.Event.BossEvent.KTBConfig;
import Dev.Event.BossEvent.KTBManager;
import Dev.Event.ChampionInvade.InitialChampionInvade;
import Dev.Event.DeathMatch.DMConfig;
import Dev.Event.DeathMatch.DMManager;
import Dev.Event.SoloBossEvent.InitialSoloBossEvent;
import Dev.Event.Tournament.InstanceManager;
import Dev.Event.TvT.TvTAreasLoader;
import Dev.Event.TvT.TvTConfig;
import Dev.Event.TvT.TvTManager;
import Dev.Event.TvTFortress.FOSConfig;
import Dev.Event.TvTFortress.FOSManager;
import Dev.InstanceFarm.TimeInstanceManager;
import Dev.ItemsTime.TimedItemManager;
import Dev.OfflineSystem.OfflinePlayerData;
import Dev.OfflineSystem.OfflineStoresData;
import Dev.Ranking.Clan.ClanRankingConfig;
import Dev.Ranking.Clan.ClanRankingManager;
import hwid.Hwid;
import phantom.PhantomNameManager;
import phantom.PhantomTitleManager;
import phantom.Phantom_Farm;
import phantom.Phantom_Town;

public class GameServer
{
	private static final CLogger LOGGER = new CLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	
	private static GameServer _gameServer;
	
//	public static void main(String[] args) throws Exception
//	{
//		// Cria o scheduler como membro estático da classe
//		scheduler = Executors.newScheduledThreadPool(1);
//		
//		scheduler.scheduleAtFixedRate(() -> {
//			printStatus();
//		}, 0, 1, TimeUnit.MINUTES);
//		_gameServer = new GameServer();
//		
//		// Se quiser registrar shutdown hook pra fechar o scheduler:
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			if (scheduler != null && !scheduler.isShutdown())
//			{
//				scheduler.shutdown();
//			}
//		}));
//	}
	public static void main(String[] args) throws Exception
	{
		_gameServer = new GameServer();
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();
		
		// Create log folder
		new File("./log").mkdir();
		new File("./log/chat").mkdir();
		new File("./log/console").mkdir();
		new File("./log/error").mkdir();
		new File("./log/gmaudit").mkdir();
		new File("./log/item").mkdir();
		new File("./data/crests").mkdirs();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(Config.LOGGING)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		StringUtil.printSection("aCis");
		Team.info();
		// Initialize config
		Config.loadGameServer();
		
		// Factories
		XMLDocumentFactory.getInstance();
		ConnectionPool.init();
		
		ThreadPool.init();
		
		StringUtil.printSection("IdFactory");
		IdFactory.getInstance();
		
		StringUtil.printSection("World");
		GameTimeController.getInstance();
		World.getInstance();
		MapRegionTable.getInstance();
		AnnouncementData.getInstance();
		ServerMemoTable.getInstance();
		TaskManager.getInstance();
		TimeInstanceManager.getInstance();
		TimedItemManager.getInstance();
		AuctionTableCommunity.getInstance();
		
		StringUtil.printSection("Instance Manager");
		InstanceManager.getInstance();
		
		StringUtil.printSection("Skills");
		SkillTable.getInstance();
		SkillTreeTable.getInstance();
		
		StringUtil.printSection("Items");
		ItemTable.getInstance();
		SummonItemData.getInstance();
		HennaData.getInstance();
		BuyListManager.getInstance();
		MultisellData.getInstance();
		RecipeTable.getInstance();
		ArmorSetData.getInstance();
		FishData.getInstance();
		SpellbookData.getInstance();
		SoulCrystalData.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		IconTable.getInstance();
		RouletteData.getInstance();
		DollsTable.getInstance();
		// Siege Reward Manager - Seth

		StringUtil.printSection("Admins");
		AdminData.getInstance();
		BookmarkTable.getInstance();
		MovieMakerManager.getInstance();
		PetitionManager.getInstance();
		
		StringUtil.printSection("Characters");
		CharTemplateTable.getInstance();
		PlayerNameTable.getInstance();
		NewbieBuffData.getInstance();
		TeleportLocationData.getInstance();
		HtmCache.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		RaidBossPointsManager.getInstance();
		
		StringUtil.printSection("Community server");
		if (Config.ENABLE_COMMUNITY_BOARD) // Forums has to be loaded before clan data
			ForumsBBSManager.getInstance().initRoot();
		else
			LOGGER.info("Community server is disabled.");
		
		StringUtil.printSection("Clans");
		CrestCache.getInstance();
		ClanTable.getInstance();
		AuctionManager.getInstance();
		ClanHallManager.getInstance();
		
		StringUtil.printSection("Geodata & Pathfinding");
		GeoEngine.getInstance();
		
		StringUtil.printSection("Zones");
		ZoneManager.getInstance();
		
		StringUtil.printSection("Task Managers");
		AttackStanceTaskManager.getInstance();
		DecayTaskManager.getInstance();
		GameTimeTaskManager.getInstance();
		ItemsOnGroundTaskManager.getInstance();
		MovementTaskManager.getInstance();
		PvpFlagTaskManager.getInstance();
		RandomAnimationTaskManager.getInstance();
		ShadowItemTaskManager.getInstance();
		WaterTaskManager.getInstance();
	//	CheckManager.getInstance();
		ChatBanManager.getInstance();
		CheckManager.getInstance();
		HeroManager.getInstance();
		VipManager.getInstance();
		AioManager.getInstance();
		ChatGlobalManager.getInstance();
		ChatHeroManager.getInstance();
		
		StringUtil.printSection("Castles");
		CastleManager.getInstance();
		
		StringUtil.printSection("Seven Signs");
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		
		StringUtil.printSection("Manor Manager");
		CastleManorManager.getInstance();
		
		StringUtil.printSection("NPCs");
		RaidBossInfoManager.getInstance();
		BufferTable.getInstance();
		HerbDropData.getInstance();
		NpcTable.getInstance();
		WalkerRouteData.getInstance();
		DoorTable.getInstance().spawn();
		StaticObjectData.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		GrandBossManager.getInstance();
		DayNightSpawnManager.getInstance();
		DimensionalRiftManager.getInstance();
		ClanRankingConfig.load();
		ClanRankingManager.getInstance();
		TimeRaidBossManager.getInstance();
		TimeEpicBossManager.getInstance();
		
		StringUtil.printSection("OfflineShop Started");
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineStoresData.getInstance().restoreOfflineTraders();
		}
		
		StringUtil.printSection("OfflinePlayers Started");
		if (Config.ENABLE_OFFLINE_PLAYER && Config.RESTORE_OFFLINERS_PLAYERS)
		{
			OfflinePlayerData.getInstance().restoreOfflineTraders();
		}
		
		StringUtil.printSection("Olympiads & Heroes");
		OlympiadGameManager.getInstance();
		Olympiad.getInstance();
		Hero.getInstance();
		
		StringUtil.printSection("Four Sepulchers");
		FourSepulchersManager.getInstance().init();
		
		StringUtil.printSection("Quests & Scripts");
		ScriptManager.getInstance();
		
		if (Config.ALLOW_BOAT)
		{
			BoatManager.getInstance();
			BoatGiranTalking.load();
			BoatGludinRune.load();
			BoatInnadrilTour.load();
			BoatRunePrimeval.load();
			BoatTalkingGludin.load();
		}
		
		StringUtil.printSection("Events");
		
		if (Config.ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionshipManager.getInstance();
		
		MonsterRace.getInstance();
		
		if (Config.PCB_ENABLE)
		{
			ThreadPool.scheduleAtFixedRate(PcPoint.getInstance(), Config.PCB_INTERVAL * 1000, Config.PCB_INTERVAL * 1000);	
		}
			
		TvTAreasLoader.load();
		//tvt
		
		if ((Config.SOLO_BOSS_EVENT))
		{
			LOGGER.info("[Start Solo Boss Event]: Enabled");
			InitialSoloBossEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			LOGGER.info("Solo Boss Event is disabled.");
		}
		TvTConfig.init();
		TvTManager.getInstance();
		if (Config.CTF_EVENT_ENABLED)
		{
			CTFEventManager.getInstance().startCTFEventRegistration();
			CTFEventManager.getInstance().StartCalculationOfNextCtfEventTime();
			if (Config.CTF_EVENT_ENABLED)
				LOGGER.info("CTF Event is enabled.");
		}
		else
			LOGGER.info("CTF Event is disabled.");
		
		
		KTBConfig.init();
		KTBManager.getInstance();
		DMConfig.init();
		DMManager.getInstance();
		FOSConfig.init();
		FOSManager.getInstance();
		if (Config.ACTIVE_MISSION)
		{
			MissionReset.getInstance().StartNextEventTime();
		} 
		else 
		{
			LOGGER.info("Mission Reset: desativado...");
		}
	    
		if (Config.TOURNAMENT_EVENT_TIME)
		{
			LOGGER.info("Tournament Event is enabled.");
			ArenaEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else if (Config.TOURNAMENT_EVENT_START)
		{
			LOGGER.info("Tournament Event is enabled.");
			ArenaTask.spawnNpc1();
			ArenaTask.spawnNpc2();
		}
		else
			LOGGER.info("Tournament Event is disabled");
		
		if (Config.CKM_ENABLED)
			CharacterKillingManager.getInstance().init();
		
		if ((Config.START_PARTY))
		{
			LOGGER.info("Start Spawn "+Config.NAME_EVENT+": Enabled");
			PartyFarmEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			LOGGER.info("Party Farm is disabled.");
		}
		if ((Config.CHAMPION_FARM_BY_TIME_OF_DAY))
		{
			LOGGER.info("[Champion Invade Event]: Enabled");
			InitialChampionInvade.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			LOGGER.info("Champion Invade Event is disabled.");
		}
		if(Config.PVP_EVENT_ENABLED)
		{
			PvPEventManager.getInstance();
		}
		else
		{
			LOGGER.info("PvP Event disabled.");
		}

		StringUtil.printSection("Others");
		
		if (Config.ENABLE_PVP_COLOR)
			PvpColorTable.getInstance();
		
		if (Config.ENABLE_PK_COLOR)
			PkColorTable.getInstance();
		
		if (Config.RESET_DAILY_ENABLED)
			ResetDaily.getInstance().StartReset();
		else
			LOGGER.info("Reset Daily: desativado.");
		
		if (Config.RESTART_BY_TIME_OF_DAY)
			Restart.getInstance().StartCalculationOfNextRestartTime();
		else
			LOGGER.info("Restart System: disabled...");
		
		StringUtil.printSection("Balance System");
		OlyClassDamageManager.loadConfig();
		BalanceLoad.LoadEm();
		
		StringUtil.printSection("Protection");
		PartyZoneManager.getInstance();
		RaidZoneManager.getInstance();
		SoloZoneManager.getInstance();
		SiegeZoneManager.getInstance();
	    IPManager.getInstance();
		
		StringUtil.printSection("Phantom Players");
		PhantomNameManager.INSTANCE.initialise();
		PhantomTitleManager.INSTANCE.initialise();
	    
	    if (Config.ALLOW_PHANTOM_PLAYERS) {
	        Phantom_Town.init();
	      } else {
	    	LOGGER.info("Town Phantom: desativado...");
	      }
	    
	    if (Config.ALLOW_PHANTOM_PLAYERS_FARM) {
	        Phantom_Farm.init();
	      } else {
	    	LOGGER.info("Phantom Farm: desativado...");
	      }
		StringUtil.printSection("DressMe Manager");
	    DressMeData.getInstance();
	    
	    StringUtil.printSection("ProtectGuard Loading...");
	    {
	    	Hwid.Init();
	    }
		
		StringUtil.printSection("Handlers");
		LOGGER.info("AutoSpawnHandler: Loaded {} handlers.", AutoSpawnManager.getInstance().size());
		LOGGER.info("Loaded {} admin command handlers.", AdminCommandHandler.getInstance().size());
		LOGGER.info("Loaded {} chat handlers.", ChatHandler.getInstance().size());
		LOGGER.info("Loaded {} item handlers.", ItemHandler.getInstance().size());
		LOGGER.info("Loaded {} skill handlers.", SkillHandler.getInstance().size());
		LOGGER.info("Loaded {} user command handlers.", UserCommandHandler.getInstance().size());
		LOGGER.info("Loaded {} voiced command handlers.", +VoicedCommandHandler.getInstance().size());
		
		StringUtil.printSection("System");
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		ForumsBBSManager.getInstance();
		
		if (Config.DEADLOCK_DETECTOR)
		{
			LOGGER.info("Deadlock detector is enabled. Timer: {}s.", Config.DEADLOCK_CHECK_INTERVAL);
			
			final DeadLockDetector deadDetectThread = new DeadLockDetector();
			deadDetectThread.setDaemon(true);
			deadDetectThread.start();
		}
		else
			LOGGER.info("Deadlock detector is disabled.");
		
		System.gc();
		
		LOGGER.info("Gameserver has started, used memory: {} / {} Mo.", SysUtil.getUsedMemory(), SysUtil.getMaxMemory());
		LOGGER.info("Maximum allowed players: {}.", Config.MAXIMUM_ONLINE_USERS);
		
		LOGGER.info("Server Loaded in " + (System.currentTimeMillis() - serverLoadStart) / 1000 + " seconds");
		
		ServerStatus.getInstance();
		
		StringUtil.printSection("Login");
		LoginServerThread.getInstance().start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final L2GamePacketHandler handler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, handler, handler, handler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (Exception e)
			{
				LOGGER.error("The GameServer bind address is invalid, using all available IPs.", e);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to open server socket.", e);
			System.exit(1);
		}
		_selectorThread.start();
	}
	
	public static GameServer getInstance()
	{
		return _gameServer;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
}