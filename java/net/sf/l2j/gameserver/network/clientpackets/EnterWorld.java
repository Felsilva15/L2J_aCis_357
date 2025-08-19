package net.sf.l2j.gameserver.network.clientpackets;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.sf.l2j.Config;
import net.sf.l2j.ConnectionPool;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.events.ArenaTask;
import net.sf.l2j.events.CTF;
import net.sf.l2j.events.PartyZoneTask;
import net.sf.l2j.gameserver.Restart;
import net.sf.l2j.gameserver.communitybbs.Manager.MailBBSManager;
import net.sf.l2j.gameserver.data.CharTemplateTable;
import net.sf.l2j.gameserver.data.MapRegionTable.TeleportType;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedDonate;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CheckManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.instancemanager.VIPFreeHTML;
//import net.sf.l2j.gameserver.instancemanager.VIPFreeHTML;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.ClassMaster;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.Player.PunishLevel;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.OpenUrl;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.skills.AbnormalEffect;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;
import net.sf.l2j.gameserver.util.HWID;
import net.sf.l2j.util.Util;

import Dev.DollsSystem.DollsTable;
import Dev.Event.BossEvent.KTBEvent;
import Dev.Event.ChampionInvade.ChampionInvade;
import Dev.Event.DeathMatch.DMEvent;
import Dev.Event.TvT.TvTEvent;
import Dev.Event.TvTFortress.FOSEvent;
import Dev.InstanceFarm.TimeInstanceManager;
import hwid.Hwid;

public class EnterWorld extends L2GameClientPacket
{
	private static final String LOAD_PLAYER_QUESTS = "SELECT name,var,value FROM character_quests WHERE charId=?";
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		getClient().setState(GameClientState.IN_GAME);
		
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		
		if(activeChar.isGM() && Config.GM_VIEW_PL_ON){
		activeChar.setPlayerCont(true);
		activeChar.startContPlayers();
		}
		
		AnnouncementData.getInstance().showAnnouncements(activeChar, false);
		activeChar.onPlayerEnter();
		TimeInstanceManager.onLogin(activeChar);
		if(Config.RESTART_BY_TIME_OF_DAY)
		{
			ShowNextRestart(activeChar);
		}
		
	//	IPLog.auditGMAction(activeChar.getName(), activeChar.getClient().getConnection().getInetAddress().getHostAddress(), activeChar.getHWID());
	//	HWID.auditGMAction(activeChar.getHWID(), activeChar.getName());
		HWID.auditGMAction(activeChar.getHWID(), activeChar.getName());
		Hwid.enterlog(activeChar, getClient());
		
		final int objectId = activeChar.getObjectId();
		
		if (activeChar.isGM())
			EnterGM(activeChar);
		
		if(activeChar.isGM())
		{
			if(Config.ENABLE_NAME_GMS_CHECK)
			{
				if (!Util.contains(Config.GM_NAMES, activeChar.getName()))
				{
					activeChar.setPunishLevel(PunishLevel.ACC, 0);
				}
			}	
		}
		
		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
			activeChar.setIsDead(true);
		
		
		// Clan checks.
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
			
			// Refresh player instance.
			clan.getClanMember(objectId).setPlayerInstance(activeChar);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(activeChar);
			final PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(activeChar);
			
			// Send packets to others members.
			for (Player member : clan.getOnlineMembers())
			{
				if (member == activeChar)
					continue;
				
				member.sendPacket(msg);
				member.sendPacket(update);
			}
			
			// Send a login notification to sponsor or apprentice, if logged.
			if (activeChar.getSponsor() != 0)
			{
				final Player sponsor = World.getInstance().getPlayer(activeChar.getSponsor());
				if (sponsor != null)
					sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			else if (activeChar.getApprentice() != 0)
			{
				final Player apprentice = World.getInstance().getPlayer(activeChar.getApprentice());
				if (apprentice != null)
					apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			
			// Add message at connexion if clanHall not paid.
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null && !clanHall.getPaid())
				activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final Siege siege = castle.getSiege();
				if (!siege.isInProgress())
					continue;
				
				final SiegeSide type = siege.getSide(clan);
				if (type == SiegeSide.ATTACKER)
					activeChar.setSiegeState((byte) 1);
				else if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER)
					activeChar.setSiegeState((byte) 2);
			}
			
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.sendPacket(new PledgeStatusChanged(clan));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SealType.STRIFE) != CabalType.NORMAL)
		{
			CabalType cabal = SevenSigns.getInstance().getPlayerCabal(objectId);
			if (cabal != CabalType.NORMAL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SealType.STRIFE))
					activeChar.addSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				else
					activeChar.addSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
			}
		}
		else
		{
			activeChar.removeSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
	
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setSpawnProtection(true);
		
		activeChar.spawnMe();
		
		if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
			activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR); 
		
		if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
			activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR); 
		
		if (Config.PCB_ENABLE)
			activeChar.showPcBangWindow();
		
		if ((ChampionInvade.is_started()) && (Config.CHAMPION_MESSAGE_ENABLED))
		{
			activeChar.sendPacket(new CreatureSay(0, 3, ".", "" + Config.CHAMPION_FARM_MESSAGE_TEXT + ":."));
		}
		TvTEvent.onLogin(activeChar);
		KTBEvent.onLogin(activeChar);
		DMEvent.onLogin(activeChar);
		FOSEvent.onLogin(activeChar);
		DollsTable.refreshAllRuneSkills(activeChar);
		
		if(Config.START_VIP && !activeChar.isVip())
			ThreadPool.schedule(new VIPFreeHTML(activeChar), 1);
		
		// Engage and notify partner.
		if (Config.ALLOW_WEDDING)
		{
			for (Entry<Integer, IntIntHolder> coupleEntry : CoupleManager.getInstance().getCouples().entrySet())
			{
				final IntIntHolder couple = coupleEntry.getValue();
				if (couple.getId() == objectId || couple.getValue() == objectId)
				{
					activeChar.setCoupleId(coupleEntry.getKey());
					break;
				}
			}
		}
		
		// activeChar.sendPacket(SevenSigns.getInstance().getCurrentPeriod().getMessageId());
		
		// if player is DE, check for shadow sense skill at night
		if (activeChar.getRace() == ClassRace.DARK_ELF && activeChar.getSkillLevel(294) == 1)
			activeChar.sendPacket(SystemMessage.getSystemMessage((GameTimeTaskManager.getInstance().isNight()) ? SystemMessageId.NIGHT_S1_EFFECT_APPLIES : SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(294));
		
		// Status eterno
		activeChar.restoreHeroStatus();
		activeChar.restoreVipStatus();
		activeChar.restoreAioStatus();
		
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new UserInfo(activeChar));
		activeChar.sendPacket(new HennaInfo(activeChar));
		activeChar.sendPacket(new FriendList(activeChar));
		// activeChar.queryGameGuard();
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendPacket(new ShortCutInit(activeChar));
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		// no broadcast needed since the player will already spawn dead to others
		if (activeChar.isAlikeDead())
			activeChar.sendPacket(new Die(activeChar));
		
		activeChar.updateEffectIcons();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.AUTO_LEARN_SKILLS)
			activeChar.checkAllowedSkills();
		
		activeChar.getInventory().reloadEquippedItems();
		activeChar.sendSkillList();
		
		// Load quests.
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement(LOAD_PLAYER_QUESTS);
			statement.setInt(1, objectId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				final String questName = rs.getString("name");
				
				// Test quest existence.
				final Quest quest = ScriptManager.getInstance().getQuest(questName);
				if (quest == null)
				{
					_log.warning("Quest: Unknown quest " + questName + " for player " + activeChar.getName());
					continue;
				}
				
				// Each quest get a single state ; create one QuestState per found <state> variable.
				final String var = rs.getString("var");
				if (var.equals("<state>"))
				{
					new QuestState(activeChar, quest, rs.getByte("value"));
					
					// Notify quest for enterworld event, if quest allows it.
					if (quest.getOnEnterWorld())
						quest.notifyEnterWorld(activeChar);
				}
				// Feed an existing quest state.
				else
				{
					final QuestState qs = activeChar.getQuestState(questName);
					if (qs == null)
					{
						_log.warning("Quest: Unknown quest state " + questName + " for player " + activeChar.getName());
						continue;
					}
					
					qs.setInternal(var, rs.getString("value"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Quest: could not insert char quest:", e);
		}
		
		activeChar.sendPacket(new QuestList(activeChar));
		
		// Unread mails make a popup appears.
		if (Config.ENABLE_COMMUNITY_BOARD && MailBBSManager.getInstance().checkUnreadMail(activeChar) > 0)
		{
			activeChar.sendPacket(SystemMessageId.NEW_MAIL);
			activeChar.sendPacket(new PlaySound("systemmsg_e.1233"));
			activeChar.sendPacket(ExMailArrived.STATIC_PACKET);
		}
		
		// Clan notice, if active.
		if (clan != null && clan.isNoticeEnabled())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/clan_notice.htm");
			html.replace("%clan_name%", clan.getName());
			html.replace("%notice_text%", clan.getNotice().replaceAll("\r\n", "<br>").replaceAll("action", "").replaceAll("bypass", ""));
			sendPacket(html);
		}
		
		if ((Config.ALLOW_MANUTENCAO) && (!activeChar.isGM()))
		{
			activeChar.sendPacket(new ExShowScreenMessage(Config.MANUTENCAO_TEXT, 23000, 2, true));
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/mods/manutencao.htm");
			html.replace("%name%", activeChar.getName());
			activeChar.sendPacket(html);
			activeChar.setIsParalyzed(true);
			activeChar.setManutencao(true);
			activeChar.startAbnormalEffect(2048);
			activeChar.startAbnormalEffect(AbnormalEffect.ROOT);
			ThreadPool.schedule(new CloseGame(activeChar, 10), 0L);
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		sendPacket(new SkillCoolTime(activeChar));
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		
		if ((!activeChar.isGM() && (!activeChar.isInSiege() || activeChar.getSiegeState() < 2) && activeChar.isInsideZone(ZoneId.SIEGE)) || (!activeChar.isInsideZone(ZoneId.PEACE) && Olympiad.getInstance().playerInStadia(activeChar)))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					activeChar.teleToLocation(TeleportType.TOWN);
				}
			}, 1000);
		}
		else if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
				}
			}, 1000);
		}
		else if (activeChar.getX() == 0 && activeChar.getY() == 0 && activeChar.getZ() == 0)
		{
			if (activeChar.isDead())
				activeChar.doRevive();
			
			_log.info("----------------------------------------------------------------------------");
			_log.info("[WARN]: " + activeChar.getName() + " / LocX=0, LocY=0, LocZ=0");
			_log.info("----------------------------------------------------------------------------");
			
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					activeChar.teleToLocation(83528, 148632, -3400, 50);
				}
			}, 1000);
			
		}
		
		if (Config.OLYMPIAD_PERIOD)
			Olympiad.getInstance().announcePeriod(activeChar);
		
		if (!activeChar.isGM())
			Announce(activeChar);
		
		if (Config.ADD_SKILL_NOBLES && !activeChar.isNoble())
			activeChar.addSkill(SkillTable.getInstance().getInfo(1323, 1), false);
		
		// Means that it's not ok multiBox situation, so logout
	//	if (!activeChar.checkMultiBox())
	//	{
	//		System.out.println("DUAL BOX: " + activeChar.getName() + " Disconnected..");
	//		activeChar.sendMessage("I'm sorry, but multibox is not allowed here.");
	//		activeChar.startAbnormalEffect(2048);
	//		activeChar.startAbnormalEffect(AbnormalEffect.ROOT);
	//		ThreadPool.schedule(new CloseGame(activeChar, 10), 0L);
	//	}
		if (!activeChar.checkMultiBox() && Config.ALLOW_DUALBOX)
		{
			activeChar.sendPacket(new ExShowScreenMessage("I'm sorry, but multibox is not allowed here, Disconnect 5 Segunds", 5000));
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{					
					activeChar.logout(true);
				}
			}, 5000);
			
		}
	//	if (Config.MULTIBOX_PROTECTION_ENABLED)
	//	{
	//		IPManager.getInstance().validBox(activeChar, Integer.valueOf(Config.MULTIBOX_PROTECTION_CLIENTS_PER_PC), World.getInstance().getPlayers(), Boolean.valueOf(true)); 	
	//	}
		
		if (activeChar.getFirstLog())
			onEnterFirst(activeChar);
		else if (activeChar.getSecondLog())
			onEnterSecond(activeChar);
		
//		if (Config.SERVER_NEWS)
//			ThreadPool.schedule(new NewsHtml(activeChar), 1);
		 if (Config.SERVER_NEWS)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
		//	html.setFile("data/html/servnews.htm");
			html.setFile("data/html/mods/menu/Info_Server.htm");
			sendPacket(html);
		}
		
		
		if (!Config.DISABLE_TUTORIAL)
			loadTutorial(activeChar); 
		
		if (Config.TALK_CHAT_ALL_CONFIG)
		{
			int _calcule = (int) arredondaValor(1, activeChar.getOnlineTime() / 60);
			
			if (_calcule < Config.TALK_CHAT_ALL_TIME)
			{
				long currentTime = System.currentTimeMillis();
				currentTime += (Config.TALK_CHAT_ALL_TIME - _calcule) * 60000;
				activeChar.setChatAllTimer(currentTime);
			}
		}
		
		ClassMaster.showQuestionMark(activeChar);
		
		if (Config.ALT_DISABLE_BOW_CLASSES)
			activeChar.removeBow();
		
		if (Config.DONATE_HTML && !activeChar.getFirstLog())
			VoicedDonate.showMainHtml(activeChar);
		
		activeChar.setPlayer(true);
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void EnterGM(Player activeChar)
	{
		if (Config.GM_STARTUP_INVISIBLE)
			activeChar.getAppearance().setInvisible();
		
		if (Config.GM_STARTUP_SILENCE)
			activeChar.setMessageRefusal(true);
		
		if (Config.GM_STARTUP_AUTO_LIST)
			AdminData.getInstance().addGm(activeChar, false);
		else
			AdminData.getInstance().addGm(activeChar, true);
		
		if (Config.GM_STARTUP)
		{
			if (Config.GM_STARTUP_INVULNERABLE)
				activeChar.setIsInvul(true);
			
			activeChar.doCast(SkillTable.getInstance().getInfo(7029, 4));
		}

		// Check Itens
		CheckManager.getInstance().loadData();
	}
	
	
	private static void loadTutorial(Player player) {
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player); 
	}
	
	@SuppressWarnings("deprecation")
	public static double arredondaValor(int casasDecimais, double valor)
	{
		BigDecimal decimal = new BigDecimal(valor);
		return decimal.setScale(casasDecimais, 3).doubleValue();
	}
	
	public static void onEnterFirst(Player activeChar)
	{
		activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
		activeChar.setCurrentCp(activeChar.getMaxCp());
		activeChar.setFirstLog(false);
		activeChar.updateFirstLog();
		
		if (Config.OPEN_URL_ENABLE)
			activeChar.sendPacket(new OpenUrl("" + Config.OPEN_URL_SITE + ""));
		
	}
	
	public static void onEnterSecond(Player activeChar)
	{
		activeChar.setSecondLog(false);
		activeChar.updateSecondLog();
		
		if (Config.OPEN_URL_ENABLE)
			activeChar.sendPacket(new OpenUrl("" + Config.OPEN_URL_SITE + ""));
		
	}
	
//	public static void Announce(Player activeChar)
//	{
//		if (Config.ANNOUNCE_HERO_ON_ENTER && activeChar.isHero())
//		{
//			boolean exists = false;
//			Connection con = null;
//			try
//			{
//				con = ConnectionPool.getConnection();
//				PreparedStatement stmt = con.prepareStatement("SELECT * FROM  heroes WHERE char_id=? ORDER BY char_id=?");
//				
//				stmt.setInt(1, activeChar.getObjectId());
//				stmt.setInt(2, activeChar.getCharId());
//				ResultSet rset = stmt.executeQuery();
//				stmt.execute();
//				if (rset.last())
//				{
//					exists = true;
//				}
//				rset.close();
//				stmt.close();
//				stmt = null;
//				rset = null;
//			}
//			catch (Exception e)
//			{
//			}
//			finally
//			{
//				L2DatabaseFactory.close(con);
//			}
//			
//			if (exists)
//			{
//				int classId = activeChar.getBaseClass();
//				String className = CharTemplateTable.getInstance().getClassNameById(classId);
//				Announcement.Announce("O Hero " + activeChar.getName() + " da classe " + className + " esta online!");
//			}
//			else if ((HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId()) || activeChar.isHero()) && Config.ANNOUNCE_HERO_ON_CUSTOM)
//				Announcement.Announce("O Hero " + activeChar.getName() + " esta online!");
//			
//		}
	public static void Announce(Player activeChar) {
	    if (Config.ANNOUNCE_HERO_ON_ENTER && activeChar.isHero()) {
	        boolean exists = false;
	        Connection con = null;
	        PreparedStatement stmt = null;
	        ResultSet rset = null;
	        
	        try {
	            con = ConnectionPool.getConnection();
	            String sql = "SELECT * FROM heroes WHERE char_id = ?";
	            stmt = con.prepareStatement(sql);
	            stmt.setInt(1, activeChar.getObjectId());
	            rset = stmt.executeQuery();
	            
	            if (rset.last()) { // se tem registro
	                exists = true;
	            }
	        } catch (Exception e) {
	            e.printStackTrace(); // Para ajudar a identificar erros
	        } finally {
	            try {
	                if (rset != null) rset.close();
	                if (stmt != null) stmt.close();
	                if (con != null) con.close(); // ConnectionPool.getConnection() geralmente retorna um pool que aceita close()
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        
	        if (exists) {
	            int classId = activeChar.getBaseClass();
	            String className = CharTemplateTable.getInstance().getClassNameById(classId);
	            Announcement.Announce("O Hero " + activeChar.getName() + " da classe " + className + " esta online!");
	        } else if ((HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId()) || activeChar.isHero()) && Config.ANNOUNCE_HERO_ON_CUSTOM) {
	            Announcement.Announce("O Hero " + activeChar.getName() + " esta online!");
	        }
	    }
	

		
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null)
			{
				if ((activeChar.getObjectId() == activeChar.getClan().getLeaderId() && Config.ANNOUNCE_HALL_OWNERS))
					Announcement.Announce(activeChar.getName() + ", dono da " + clanHall.getName() + " de " + clanHall.getLocation() + " esta online!");
			}
		}
		
		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
			if (activeChar.getClan() != null)
			{
				if (activeChar.getClan().getLeaderName().equals(activeChar.getName()))
				{
					if (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null)
						Announcement.Announce("O Lord " + activeChar.getName() + " dono do Castelo de " + CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getName() + " esta online!");
				}
			}
		}
		
		if (!activeChar.getFirstLog())
		{
		//	if (TvT.is_joining() && Config.SCREN_MSG)
		//		activeChar.sendPacket(new ExShowScreenMessage("TvT Event - Register Now!", 6000));
			 if (CTF.is_joining() && Config.SCREN_MSG)
				activeChar.sendPacket(new ExShowScreenMessage("CTF Event - Register Now!", 6000));
			else if (PartyZoneTask.is_started())
				activeChar.sendPacket(new ExShowScreenMessage(Config.PARTY_FARM_MESSAGE_TEXT, Config.PARTY_FARM_MESSAGE_TIME));
			else if (ArenaTask.is_started() && Config.ARENA_MESSAGE_ENABLED)
				activeChar.sendPacket(new ExShowScreenMessage(Config.ARENA_MESSAGE_TEXT, Config.ARENA_MESSAGE_TIME));
			
			
		}
		
	} 
	/**
	 * NOTE: RESTART_BY_TIME_OF_DAY = TRUE
	 *
	 * @param activeChar
	 */
	private static void ShowNextRestart(Player activeChar)
	{
		CreatureSay cs = new CreatureSay(0, Say2.GM, "World", "Next Restart: " + Restart.getInstance().getRestartNextTime());
		activeChar.sendPacket(cs);
	}
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}