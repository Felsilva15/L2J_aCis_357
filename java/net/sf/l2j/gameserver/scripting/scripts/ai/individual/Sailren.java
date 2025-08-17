package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.templates.StatsSet;

import Dev.BossTimeRespawn.TimeEpicBossManager;

public class Sailren extends L2AttackableAIScript
{
	private static final L2BossZone SAILREN_LAIR = ZoneManager.getInstance().getZoneById(110015, L2BossZone.class);
	
	public static final int SAILREN = 29065;
	
	public static final byte DORMANT = 0; // No one has entered yet. Entry is unlocked.
	public static final byte WAITING = 1; // Someone has entered, triggering a 30 minute window for additional people to enter. Entry is unlocked.
	public static final byte FIGHTING = 2; // A group entered in the nest. Entry is locked.
	public static final byte DEAD = 3; // Sailren has been killed. Entry is locked.
	
	private static final int VELOCIRAPTOR = 22223;
	private static final int PTEROSAUR = 22199;
	private static final int TREX = 22217;
	
	private static final int DUMMY = 32110;
	private static final int CUBE = 32107;
	
	private static final long INTERVAL_CHECK = 300000L; // 5 minutos

	
	private static final SpawnLocation SAILREN_LOC = new SpawnLocation(27549, -6638, -2008, 0);
	
	private final List<Npc> _mobs = new CopyOnWriteArrayList<>();
	private static long _lastAttackTime = 0;
	
	public Sailren()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
		
		switch (GrandBossManager.getInstance().getBossStatus(SAILREN))
		{
			case DEAD: // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
				final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
				if (temp > 0)
					startQuestTimer("unlock", temp, null, null, false);
				else
					GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
				break;
				
			case WAITING: // Launch beginning timer.
				startQuestTimer("beginning", Config.WAIT_TIME_SAILREN, null, null, false);
				break;
			
			case FIGHTING:
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				
				final Npc sailren = addSpawn(SAILREN, loc_x, loc_y, loc_z, heading, false, 0, false);
				GrandBossManager.getInstance().addBoss((GrandBoss) sailren);
				_mobs.add(sailren);
				
				sailren.setCurrentHpMp(hp, mp);
				sailren.setRunning();
				
				// Don't need to edit _timeTracker, as it's initialized to 0.
				startQuestTimer("inactivity", INTERVAL_CHECK, null, null, true);
				break;
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
		addKillId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("beginning"))
		{
			// Dummy spawn used to cast the skill. Despawned after 26sec.
			final Npc temp = addSpawn(DUMMY, SAILREN_LOC, false, 26000, false);
			
			// Cast skill every 2,5sec.
			startQuestTimer("skill", 2500, temp, null, true);
			
			// Cinematic, meanwhile.
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(temp.getObjectId(), 60, 110, 30, 4000, 4000, 0, 65, 1, 0)); // 4sec
			
			startQuestTimer("camera_0", 3900, temp, null, false); // 3sec
			startQuestTimer("camera_1", 6800, temp, null, false); // 3sec
			startQuestTimer("camera_2", 9700, temp, null, false); // 3sec
			startQuestTimer("camera_3", 12600, temp, null, false); // 3sec
			startQuestTimer("camera_4", 15500, temp, null, false); // 3sec
			startQuestTimer("camera_5", 18400, temp, null, false); // 7sec
		}
		else if (event.equalsIgnoreCase("skill"))
			SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
		else if (event.equalsIgnoreCase("camera_0"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 100, 180, 30, 3000, 3000, 0, 50, 1, 0));
		else if (event.equalsIgnoreCase("camera_1"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 150, 270, 25, 3000, 3000, 0, 30, 1, 0));
		else if (event.equalsIgnoreCase("camera_2"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 360, 20, 3000, 3000, 10, 15, 1, 0));
		else if (event.equalsIgnoreCase("camera_3"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 450, 10, 3000, 3000, 0, 10, 1, 0));
		else if (event.equalsIgnoreCase("camera_4"))
		{
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 560, 0, 3000, 3000, 0, 10, 1, 0));
			
			GrandBossManager.getInstance().setBossStatus(SAILREN, FIGHTING);
			final Npc temp = addSpawn(SAILREN, SAILREN_LOC, false, 0, false);
			GrandBossManager.getInstance().addBoss((GrandBoss) temp);
			_mobs.add(temp);
			
			// Stop skill task.
			cancelQuestTimers("skill");
			SAILREN_LAIR.broadcastPacket(new MagicSkillUse(npc, npc, 5091, 1, 2500, 0));
			
			temp.broadcastPacket(new SocialAction(temp, 2));
		}
		else if (event.equalsIgnoreCase("camera_5"))
			SAILREN_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 70, 560, 0, 500, 7000, -15, 10, 1, 0));
		else if (event.equalsIgnoreCase("unlock"))
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
//		else if (event.equalsIgnoreCase("inactivity"))
//		{
//			// 10 minutes without any attack activity leads to a reset.
//			if ((System.currentTimeMillis() - _lastAttackTime) >= INTERVAL_CHECK)
//			{
//				// Set it dormant.
//				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
//				
//				// Delete all monsters and clean the list.
//				if (!_mobs.isEmpty())
//				{
//					for (Npc mob : _mobs)
//						mob.deleteMe();
//					
//					_mobs.clear();
//				}
//				
//				// Oust all players from area.
//				SAILREN_LAIR.oustAllPlayers();
//				
//				// Cancel inactivity task.
//				cancelQuestTimers("inactivity");
//			}
//		}
//		else if (event.equalsIgnoreCase("oust"))
//		{
//			// Oust all players from area.
//			SAILREN_LAIR.oustAllPlayers();
//		}
//		
//		return null;
//	}
		else if (event.equalsIgnoreCase("inactivity"))
		{
		    // Verifica se não há jogadores na zona
		    if (SAILREN_LAIR.getCharactersInside().isEmpty())
		    {
		        // Sem jogadores na zona, coloca o boss para dormir imediatamente
		        GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		        
		        // Deleta todos os monstros e limpa a lista
		        if (!_mobs.isEmpty())
		        {
		            for (Npc mob : _mobs)
		                mob.deleteMe();
		            
		            _mobs.clear();
		        }
		        
		        // Expulsa todos os jogadores da zona
		        SAILREN_LAIR.oustAllPlayers();
		        
		        // Cancela a tarefa de inatividade
		        cancelQuestTimers("inactivity");
		    }
		    // Caso não tenha jogadores, mas tenha ficado 10 minutos sem ataque
		    else if ((System.currentTimeMillis() - _lastAttackTime) >= INTERVAL_CHECK)
		    {
		        // Coloca o boss para dormir após 10 minutos sem ataques
		        GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		        
		        // Deleta todos os monstros e limpa a lista
		        if (!_mobs.isEmpty())
		        {
		            for (Npc mob : _mobs)
		                mob.deleteMe();
		            
		            _mobs.clear();
		        }
		        
		        // Expulsa todos os jogadores da zona
		        SAILREN_LAIR.oustAllPlayers();
		        
		        // Cancela a tarefa de inatividade
		        cancelQuestTimers("inactivity");
		    }

		}

	    // Retorna null para não propagar mais o evento
	    return null;
	}
	
	@Override
//	public String onKill(Npc npc, Player killer, boolean isPet)
//	{	
//		    if (npc.getNpcId() == SAILREN)
//				{
//					// Set Sailren as dead.
//					GrandBossManager.getInstance().setBossStatus(SAILREN, DEAD);
//					
//					// Spawn the Teleport Cube for 10min.
//					addSpawn(CUBE, npc, false, INTERVAL_CHECK, false);
//					
//					// Cancel inactivity task.
//					cancelQuestTimers("inactivity");
//					
//					long respawnTime;
//					if(Config.SAILREN_CUSTOM_SPAWN_ENABLED && Config.FindNext(Config.SAILREN_CUSTOM_SPAWN_TIMES) != null)
//			        {
//						respawnTime = Config.FindNext(Config.SAILREN_CUSTOM_SPAWN_TIMES).getTimeInMillis() - System.currentTimeMillis();
//					}
//			        else
//			        {
//						respawnTime = (long) Config.SPAWN_INTERVAL_SAILREN + Rnd.get(-Config.RANDOM_SPAWN_TIME_SAILREN, Config.RANDOM_SPAWN_TIME_SAILREN);
//						respawnTime *= 3600000;
//			        }
//					
//					startQuestTimer("oust", INTERVAL_CHECK, null, null, false);
//					startQuestTimer("unlock", respawnTime, null, null, false);
//					
//					// Save the respawn time so that the info is maintained past reboots.
//					final StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
//					info.set("respawn_time", System.currentTimeMillis() + respawnTime);
//					GrandBossManager.getInstance().setStatsSet(SAILREN, info);
//				}
//		
//		
//		return super.onKill(npc, killer, isPet);
//	}
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (!_mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(killer.getObjectId()))
			return null;

		switch (npc.getNpcId())
		{
			case VELOCIRAPTOR:
				// Once the 3 Velociraptors are dead, spawn a Pterosaur.
				if (_mobs.remove(npc) && _mobs.isEmpty())
				{
					final Npc temp = addSpawn(PTEROSAUR, SAILREN_LOC, false, 0, false);
					temp.setRunning();
					temp.getAI().setIntention(CtrlIntention.ATTACK, killer);
					_mobs.add(temp);
				}
				break;

			case PTEROSAUR:
				// Pterosaur is dead, spawn a Trex.
				if (_mobs.remove(npc))
				{
					final Npc temp = addSpawn(TREX, SAILREN_LOC, false, 0, false);
					temp.setRunning();
					temp.getAI().setIntention(CtrlIntention.ATTACK, killer);
					temp.broadcastNpcSay("?");
					_mobs.add(temp);
				}
				break;

			case TREX:
				// Trex is dead, wait 5min and spawn Sailren.
				if (_mobs.remove(npc))
					startQuestTimer("spawn", Config.WAIT_TIME_SAILREN, npc, killer, false);
				break;

			case SAILREN:
				if (_mobs.remove(npc))
				{
					// Set Sailren as dead.
					GrandBossManager.getInstance().setBossStatus(SAILREN, DEAD);

					// Spawn the Teleport Cube for 10min.
					addSpawn(CUBE, npc, false, INTERVAL_CHECK, false);

					// Cancel inactivity task.
					cancelQuestTimers("inactivity");

					// 🆕 Tempo via TimeEpicBossManager
					long respawnTime = TimeEpicBossManager.getInstance().getMillisUntilNextRespawn(SAILREN);

					if (respawnTime <= 0)
					{
						respawnTime = Config.SPAWN_INTERVAL_SAILREN * 3600000L;
						respawnTime += Rnd.get(-Config.RANDOM_SPAWN_TIME_SAILREN, Config.RANDOM_SPAWN_TIME_SAILREN) * 3600000L;
						_log.warning("TimeEpicBoss: No respawn configured for Sailren (" + SAILREN + "), using fallback.");
					}

					startQuestTimer("oust", INTERVAL_CHECK, null, null, false);
					startQuestTimer("unlock", respawnTime, null, null, false);

					// Save respawn time for persistence
					final StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
					info.set("respawn_time", System.currentTimeMillis() + respawnTime);
					GrandBossManager.getInstance().setStatsSet(SAILREN, info);
				}
				break;
		}

		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (!_mobs.contains(npc) || !SAILREN_LAIR.getAllowedPlayers().contains(attacker.getObjectId()))
			return null;
		
		// Actualize _timeTracker.
		_lastAttackTime = System.currentTimeMillis();
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	public static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && GrandBossManager._announce)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			switch (seconds)
			{
				case 3600: // 1 hour left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in " + seconds / 60 / 60 + " hour(s)!");
					break;
				case 1799: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in 30 minute(s) !");
					break;
				case 599: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in 10 minute(s) !");
					break;
				case 299: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in 5 minute(s) !");
					break;
				
				case 1500: // 25 minutes left
				case 1200: // 20 minutes left
				case 900: // 15 minutes left
				case 540: // 9 minutes left
				case 480: // 8 minutes left
				case 420: // 7 minutes left
				case 360: // 6 minutes left
				case 240: // 4 minutes left
				case 180: // 3 minutes left
				case 120: // 2 minutes left
				case 60: // 1 minute left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in " + seconds / 60 + " minute(s) !");
					break;
				case 30: // 30 seconds left
				case 15: // 15 seconds left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in " + seconds + " second(s) !");
					break;
				
				case 6: // 3 seconds left
				case 5: // 3 seconds left
				case 4: // 3 seconds left
				case 3: // 2 seconds left
				case 2: // 1 seconds left
					GrandBossManager.AnnounceGrandBoss("Spawn Sailren in " + (seconds - 1) + " second(s) !");
					break;
				
				case 1: // 1 seconds left
				{
					if (GrandBossManager._announce)
						GrandBossManager.AnnounceGrandBoss("Sailren Is alive, teleport to boss closed !");
					GrandBossManager._announce = false;
				}
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
				}
			}
		}
	}
}