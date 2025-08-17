package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.Config;
import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.events.CTF;
import net.sf.l2j.events.PartyZoneTask;
import net.sf.l2j.events.manager.CTFEventManager;
import net.sf.l2j.events.manager.PvPEventNext;
import net.sf.l2j.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.ArenaEvent;
import net.sf.l2j.gameserver.MissionReset;
import net.sf.l2j.gameserver.PartyFarmEvent;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossInfoManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.StatsSet;

import Dev.Event.BossEvent.KTBConfig;
import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.BossEvent.KTBManager;
import Dev.Event.ChampionInvade.ChampionInvade;
import Dev.Event.ChampionInvade.InitialChampionInvade;
import Dev.Event.DeathMatch.DMConfig;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.DeathMatch.DMManager;
import Dev.Event.LastMan.CheckNextEvent;
import Dev.Event.SoloBossEvent.InitialSoloBossEvent;
import Dev.Event.SoloBossEvent.SoloBoss;
import Dev.Event.TvT.TvTConfig;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSConfig;
import Dev.Event.TvTFortress.FOSEvent;
import Dev.Event.TvTFortress.FOSManager;

/**
 * @author Christian
 *
 */
public class TeleportBook implements IItemHandler
{

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()) && TvTEvent.isStarted() || CTF.is_started() && activeChar._inEventCTF ||  activeChar.isInOlympiadMode() || activeChar.getPvpFlag() > 0 || activeChar.isMoving() || activeChar.isDead() || AttackStanceTaskManager.getInstance().isInAttackStance(activeChar) || activeChar.isCursedWeaponEquipped() || activeChar.isInArenaEvent() || OlympiadManager.getInstance().isRegistered(activeChar) || activeChar.getKarma() > 0 || activeChar.isInObserverMode() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || activeChar.isArenaAttack() || activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.SIEGE) || activeChar.isInsideZone(ZoneId.PVP_CUSTOM)  || activeChar.isInJail())
		{
			activeChar.sendMessage("You can not Action NOW.");
			return;
		}
		
		BookTeleport(activeChar);
	}
	public static void BookTeleport(Player activeChar)
	{
		if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()) && TvTEvent.isStarted() || CTF.is_started() && activeChar._inEventCTF ||  activeChar.isInOlympiadMode() || activeChar.getPvpFlag() > 0 || activeChar.isMoving() || activeChar.isDead() || AttackStanceTaskManager.getInstance().isInAttackStance(activeChar) || activeChar.isCursedWeaponEquipped() || activeChar.isInArenaEvent() || OlympiadManager.getInstance().isRegistered(activeChar) || activeChar.getKarma() > 0 || activeChar.isInObserverMode() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || activeChar.isArenaAttack() || activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.SIEGE) || activeChar.isInsideZone(ZoneId.PVP_CUSTOM)  || activeChar.isInJail())
		{
			activeChar.sendMessage("You can not Action NOW.");
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/BookTeleport/Main.htm");
	
		if(Config.SOLO_BOSS_EVENT)
		{
			if(SoloBoss.is_started())
				html.replace("%bossSolo%", "In Progress");
			else	
				html.replace("%bossSolo%", InitialSoloBossEvent.getInstance().getRestartNextTime().toString() );
		}
	//	html.replace("%lmTime%", CheckNextEvent.getInstance().getNextLMTime());	
		
		//BossEvent na html EventsTime.
		if(KTBConfig.KTB_EVENT_ENABLED)
		{
			if(KTBEvent.isStarted())	
				html.replace("%ktbTime%", "In Progress");
			else
				html.replace("%ktbTime%", KTBManager.getInstance().getNextTime());
		}
	
		if(DMConfig.DM_EVENT_ENABLED)
		{
			if(DMEvent.isStarted())	
				html.replace("%DMTime%", "In Progress");
			else
				html.replace("%DMTime%", DMManager.getInstance().getNextTime());
		}
		
		//Party farm EventsTime.
		if(Config.START_AUTO_PARTY){
			if(PartyZoneTask.is_started())	
			html.replace("%partyfarm%", "In Progress");
			else	
			html.replace("%partyfarm%", PartyFarmEvent.getInstance().getNextTime().toString() );
		}
		if(TvTConfig.TVT_EVENT_ENABLED)
		{
			if (TvTEvent.isStarted())
			{
				html.replace("%tvt%", "In Progress");
			}
			else
			{
				html.replace("%tvt%", CheckNextEvent.getInstance().getNextTvTTime());
			}
		}
		if(Config.CTF_EVENT_ENABLED){
			if(CTF.is_inProgress())	
			html.replace("%ctf%", "In Progress");
			else
			html.replace("%ctf%", CTFEventManager.getInstance().getNextTime().toString() );
		}
		if(FOSConfig.FOS_EVENT_ENABLED)
		{
			if(FOSEvent.isStarted())	
				html.replace("%FOSTime%", "In Progress");
			else
				html.replace("%FOSTime%", FOSManager.getInstance().getNextTime());
		}
		if(Config.PVP_EVENT_ENABLED)
		{
			if(PvPEvent.getInstance().isActive())	
				html.replace("%pvp%", "In Progress");
			else	
				html.replace("%pvp%", PvPEventNext.getInstance().getNextTime().toString() );
		}
//		//MISSION Na htmTime.
		if(Config.ACTIVE_MISSION)
		{
			html.replace("%mission%", MissionReset.getInstance().getNextTime().toString() );
		}	
		
//		//CTF na html EventsTime.
		if(Config.CHAMPION_FARM_BY_TIME_OF_DAY)
		{
			if(ChampionInvade.is_started())
				html.replace("%champion%", "In Progress");
			else	
				html.replace("%champion%", InitialChampionInvade.getInstance().getRestartNextTime().toString() );
		}
//		//Tournament na html EventsTime.
		if(Config.TOURNAMENT_EVENT_TIME)
		{
			if(ArenaTask.is_started())	
				html.replace("%arena%", "In Progress");
			else	
				html.replace("%arena%", ArenaEvent.getInstance().getNextTime().toString() );
		}
		html.replace("%fafurions%", fafurions());
		html.replace("%palibati%", palibati());
		html.replace("%beast%", beast());
		html.replace("%plague%", plague());
		html.replace("%water%", water());
		html.replace("%krokian%", krokian());
		html.replace("%olkuth%", olkuth());
		html.replace("%glaki%", glaki());
		html.replace("%ocean%", ocean());
		html.replace("%taik%", taik());
		html.replace("%lord%", lord());
		html.replace("%barakiel%", barakiel());
		html.replace("%hekaton%", hekaton());
		html.replace("%brakki%", brakki());
		html.replace("%tayr%", tayr());
		html.replace("%moss%", moss());
		html.replace("%horus%", horus());
		html.replace("%shadith%", shadith());
		html.replace("%orfen%", orfen());
		html.replace("%core%", core());
		html.replace("%sailren%", sailren());
		html.replace("%zaken%", zaken());
		html.replace("%baium%", baium());
		html.replace("%antharas%", antharas());
		html.replace("%valakas%", valakas());
		html.replace("%frintezza%", frintezza());
		html.replace("%queen%", queen());
		activeChar.sendPacket(html);
	}
	public static String fafurions()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : fafurions)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String palibati()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : palibati)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String beast()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : beast)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String plague()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : plague)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String water()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : water)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String krokian()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : krokian)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String olkuth()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : olkuth)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String glaki()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : glaki)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String ocean()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : ocean)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String taik()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : taik)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String lord()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : lord)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String barakiel()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : barakiel)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String hekaton()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : hekaton)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String brakki()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : brakki)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String tayr()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : tayr)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String shadith()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : shadith)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String moss()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : moss)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String horus()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : horus)
		{
			long delay = 0;
			if (NpcTable.getInstance().getTemplate(boss).isType("RaidBoss"))
			{
				delay = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(boss);
				
			}
			else
				continue;
			
			if (delay <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String orfen()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : orfen)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String core()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : core)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String queen()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : queen)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String baium()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : baium)
		{
			
			StatsSet infobaium = GrandBossManager.getInstance().getStatsSet(boss);
			long tempbaium = infobaium.getLong("respawn_time");
			
			if (tempbaium <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String antharas()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : antharas)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String valakas()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : valakas)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String frintezza()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : frintezza)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String sailren()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : sailren)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	
	public static String zaken()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int boss : zaken)
		{
			StatsSet infogrand = GrandBossManager.getInstance().getStatsSet(boss);
			long tempgrand = infogrand.getLong("respawn_time");
			
			if (tempgrand <= System.currentTimeMillis())
			{
		        sb.append("<font color=\"9CC300\">Alive</font>");
			}
			else
			{
				sb.append("<font color=\"ff4d4d\">Dead</font>");
			}
		}
		return sb.toString();
	}
	private static final int[] fafurions =
	{
		25198
	};
	
	private static final int[] palibati =
	{
		25252
	};
	
	private static final int[] beast =
	{
		25269
	};
	
	private static final int[] plague =
	{
		25523
	};
	
	private static final int[] water =
	{
		25199
	};
	
	private static final int[] krokian =
	{
		25202
	};
	
	private static final int[] olkuth =
	{
		25244
	};
	
	private static final int[] glaki =
	{
		25245
	};
	
	private static final int[] ocean =
	{
		25205
	};
	
	private static final int[] taik =
	{
		25256
	};
	
	private static final int[] lord =
	{
		25407
	};
	
	private static final int[] barakiel =
	{
		25325
	};
	private static final int[] hekaton =
	{
		25299
	};
	
	private static final int[] brakki =
	{
		25305
	};
	
	private static final int[] tayr =
	{
		25302
	};
	
	private static final int[] shadith =
	{
		25309
	};
	private static final int[] moss =
	{
		25312
	};
	private static final int[] horus =
	{
		25315
	};
	private static final int[] orfen =
	{
		29014
	};
	
	private static final int[] core =
	{
		29006
	};
	
	private static final int[] queen =
	{
		29001
	};
	
	private static final int[] zaken =
	{
		29022
	};
	
	private static final int[] sailren =
	{
		29065
	};
	
	private static final int[] frintezza =
	{
        29045
	};
	
	private static final int[] valakas =
	{
		29028
	};
	
	private static final int[] antharas =
	{
		29019
	};
	
	private static final int[] baium =
	{
		29020
	};
}
