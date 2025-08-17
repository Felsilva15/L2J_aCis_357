package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAdmin;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAiox;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAugment;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBalancer;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBan;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBanHWID;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBlock;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBookmark;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBuffs;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBuscarDrop;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCamera;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminChatManager;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminClanFull;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCreateItem;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCustom;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDelete;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDeleteAll;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDeleteItemAll;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDoorControl;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditNpc;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEffects;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEnchant;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminExpSp;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGeoEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGiran;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGm;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGmChat;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHeal;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHelpPage;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHero;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminInventory;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminInvul;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKick;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKnownlist;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminLevel;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminLocPhantom;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMaintenance;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMammon;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminManor;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMenu;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMovieMaker;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminOlympiad;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPForge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPetition;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPledge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPolymorph;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPvPEvent;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRecallAll;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRes;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSearch;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSendDonate;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShop;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSiege;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSkill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSpawn;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTarget;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTeleport;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVip;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminZone;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminZoneCreation;

import Dev.Event.BossEvent.AdminKTBEvent;
import Dev.Event.ChampionInvade.AdminChampionInvade;
import Dev.Event.DeathMatch.AdminDMEvent;
import Dev.Event.SoloBossEvent.AdminSoloBoss;
import Dev.Event.TvT.AdminTvTEvent;
import Dev.Event.TvTFortress.AdminFOSEvent;

public class AdminCommandHandler
{
	private final Map<Integer, IAdminCommandHandler> _datatable = new HashMap<>();
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected AdminCommandHandler()
	{	
		registerAdminCommandHandler(new AdminTvTEvent());
		registerAdminCommandHandler(new AdminPvPEvent());
		registerAdminCommandHandler(new AdminFOSEvent());
		registerAdminCommandHandler(new AdminChampionInvade());
		registerAdminCommandHandler(new AdminDeleteItemAll());
		registerAdminCommandHandler(new AdminDMEvent());
		registerAdminCommandHandler(new AdminDeleteAll());
		registerAdminCommandHandler(new AdminBuscarDrop());
		registerAdminCommandHandler(new AdminKTBEvent());
		registerAdminCommandHandler(new AdminSoloBoss());
		registerAdminCommandHandler(new AdminVip());
		registerAdminCommandHandler(new AdminAiox());
		registerAdminCommandHandler(new AdminLocPhantom());
		registerAdminCommandHandler(new AdminBalancer());
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminBookmark());
		registerAdminCommandHandler(new AdminBuffs());
		registerAdminCommandHandler(new AdminCamera());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminGeoEngine());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminKnownlist());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminMaintenance());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminMovieMaker());
		registerAdminCommandHandler(new AdminOlympiad());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminRideWyvern());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminZone());
		
		//custom
		registerAdminCommandHandler(new AdminZoneCreation());
		registerAdminCommandHandler(new AdminInventory());
		registerAdminCommandHandler(new AdminBlock());
		registerAdminCommandHandler(new AdminBanHWID());
		registerAdminCommandHandler(new AdminClanFull());
		registerAdminCommandHandler(new AdminHero());
		registerAdminCommandHandler(new AdminSendDonate());
		registerAdminCommandHandler(new AdminAugment());
		registerAdminCommandHandler(new AdminCustom());
		registerAdminCommandHandler(new AdminCTFEngine());
		registerAdminCommandHandler(new AdminGiran());
		registerAdminCommandHandler(new AdminChatManager());
		registerAdminCommandHandler(new AdminRecallAll());
		registerAdminCommandHandler(new AdminSearch());

	}
	
	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		for (String id : handler.getAdminCommandList())
			_datatable.put(id.hashCode(), handler);
	}
	
	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}