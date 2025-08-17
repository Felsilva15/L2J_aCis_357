package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.holder.RewardHolder;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;
import net.sf.l2j.gameserver.util.Broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import Dev.Event.SoloBossEvent.SoloBoss;
import Dev.Ranking.Clan.ClanRankingConfig;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class RaidBoss extends Monster
{
	private StatusEnum _raidStatus;
	private ScheduledFuture<?> _maintenanceTask;
	
	/**
	 * Constructor of L2RaidBossInstance (use Creature and L2NpcInstance constructor).
	 * <ul>
	 * <li>Call the Creature constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template L2NpcTemplate to apply to the NPC
	 */
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	private int _lastAnnouncedHpPercent = 100;
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
		
		if (Config.ANNOUNCE_GRANDBOSS_HP && getMaxHp() > 0)
		{
			int currentPercent = (int) ((getCurrentHp() / getMaxHp()) * 100);
			int[] thresholds = {80, 60, 40, 20, 10};
			
			// Se o boss regenerou vida acima do último limiar, reseta a variável
			if (currentPercent > _lastAnnouncedHpPercent)
				_lastAnnouncedHpPercent = 100;
			
			for (int threshold : thresholds)
			{
				if (currentPercent <= threshold && _lastAnnouncedHpPercent > threshold)
				{
					_lastAnnouncedHpPercent = threshold;
					Broadcast.gameAnnounceToOnlinePlayers("[Grand Boss]: " + getName() + " atingiu " + threshold + "% de vida!");
					break;
				}
			}
		}
	}
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
			{
				/*if (Config.SOLO_BOSS_EVENT && SoloBoss.is_started())
				{
					if (getNpcId() == Config.SOLO_BOSS_ID_ONE)
						ThreadPool.schedule(() -> SoloBoss.SpawnRaidTWO(), Rnd.get(2000, 3000));
					else if (getNpcId() == Config.SOLO_BOSS_ID_TWO)
						ThreadPool.schedule(() -> SoloBoss.SpawnRaidTHREE(), Rnd.get(2000, 3000));
					else if (getNpcId() == Config.SOLO_BOSS_ID_THREE)
						ThreadPool.schedule(() -> SoloBoss.SpawnRaidFOUR(), Rnd.get(2000, 3000));
					else if (getNpcId() == Config.SOLO_BOSS_ID_FOUR)
						ThreadPool.schedule(() -> SoloBoss.SpawnRaidFIVE(), Rnd.get(2000, 3000));
					else if (getNpcId() == Config.SOLO_BOSS_ID_FIVE)
					{
						ThreadPool.schedule(() -> SoloBoss.Finish_Event(), 1000);
						SoloBoss._started = false;
						SoloBoss._finish = true;
						SoloBoss._aborted = true;
					}
				}*/
				 if (Config.SOLO_BOSS_EVENT && SoloBoss.is_started())
			        {
			            // Variáveis para o próximo Boss ID e suas coordenadas
			            final int nextBossId;
			            final int[] nextBossLoc;

			            // Condicional para o controle de sequência dos spawns dos bosses
			            if (getNpcId() == Config.SOLO_BOSS_ID_ONE)
			            {
			                nextBossId = Config.SOLO_BOSS_ID_TWO;
			                nextBossLoc = Config.SOLO_BOSS_ID_TWO_LOC; // Localização do Boss 2

			                // Agendar o spawn do próximo boss (Boss 2) após o intervalo de 2 a 3 segundos
			                ThreadPool.schedule(() -> {
			                    SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
			                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
			            }
			            else if (getNpcId() == Config.SOLO_BOSS_ID_TWO)
			            {
			                nextBossId = Config.SOLO_BOSS_ID_THREE;
			                nextBossLoc = Config.SOLO_BOSS_ID_THREE_LOC; // Localização do Boss 3

			                // Agendar o spawn do próximo boss (Boss 3) após o intervalo de 2 a 3 segundos
			                ThreadPool.schedule(() -> {
			                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
			                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
			            }
			            else if (getNpcId() == Config.SOLO_BOSS_ID_THREE)
			            {
			                nextBossId = Config.SOLO_BOSS_ID_FOUR;
			                nextBossLoc = Config.SOLO_BOSS_ID_FOUR_LOC; // Localização do Boss 4

			                // Agendar o spawn do próximo boss (Boss 4) após o intervalo de 2 a 3 segundos
			                ThreadPool.schedule(() -> {
			                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
			                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
			            }
			            else if (getNpcId() == Config.SOLO_BOSS_ID_FOUR)
			            {
			                nextBossId = Config.SOLO_BOSS_ID_FIVE;
			                nextBossLoc = Config.SOLO_BOSS_ID_FIVE_LOC; // Localização do Boss 5

			                // Agendar o spawn do próximo boss (Boss 5) após o intervalo de 2 a 3 segundos
			                ThreadPool.schedule(() -> {
			                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
			                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
			            }
			            else if (getNpcId() == Config.SOLO_BOSS_ID_FIVE)
			            {
			                // Finaliza o evento quando o último Boss é morto
			                ThreadPool.schedule(() -> SoloBoss.Finish_Event(), 1000);
			                SoloBoss._started = false;
			                SoloBoss._finish = true;
			                SoloBoss._aborted = true;
			            }
			        }
				
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				broadcastPacket(new PlaySound("systemmsg_e.1209"));
				
		    //    if (player.getClan() != null && Config.RAID_INFO_IDS_LIST.contains(Integer.valueOf(getNpcId())))
		     //   {
		      //  	 player.getClan().addclanBossScore(1);
		       // }
		           
//				if (ClanRankingConfig.ENABLE_CLAN_RANKING)
//				{
//					if (player != null && ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.containsKey(Integer.valueOf(getNpcId()))) 
//					{
//						Clan clan = player.getClan();
//						if (clan != null) 
//						{
//							int points = ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.get(Integer.valueOf(getNpcId())).intValue();
//							clan.addRankingBossPoints(points);
//
//							if (ClanRankingConfig.ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL)
//								Broadcast.gameAnnounceToOnlinePlayers("Clan " + clan.getName() + " gained " + points + " points defeating a Raid!");
//						} 
//					} 
//				} 
				  if (ClanRankingConfig.ENABLE_CLAN_RANKING)
			        {
			            // Verificando diretamente a condição sem a checagem redundante de null para o player
			            if (ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.containsKey(Integer.valueOf(getNpcId()))) 
			            {
			                Clan clan = player.getClan(); // Aqui o player já deve ser garantido
			                if (clan != null) 
			                {
			                    int points = ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.get(Integer.valueOf(getNpcId())).intValue();
			                    clan.addRankingBossPoints(points);

			                    if (ClanRankingConfig.ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL)
			                        Broadcast.gameAnnounceToOnlinePlayers("Clan " + clan.getName() + " gained " + points + " points defeating a Raid!");
			                } 
			            } 
			        }
		        if (Config.ACTIVE_MISSION && Config.BOSS_LIST_MISSION.contains(Integer.valueOf(getTemplate().getNpcId())))
				{
					if (!player.check_obj_mission(player.getObjectId()))
						player.updateMission(); 
					if (!player.isRaidCompleted() && player.getRaidCont() < Config.MISSION_RAID_CONT)
						player.setRaidCont(player.getRaidCont() + 1);
				}
				final L2Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getPartyMembers())
					{
						RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
						if (member.isNoble())
							Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
				else
				{
					RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (player.isNoble())
						Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
				if(SoloBoss.is_started())
				{
					if ((getNpcId() == Config.SOLO_BOSS_ID_ONE) || (getNpcId() == Config.SOLO_BOSS_ID_TWO) || (getNpcId() == Config.SOLO_BOSS_ID_THREE) || (getNpcId() == Config.SOLO_BOSS_ID_FOUR) || (getNpcId() == Config.SOLO_BOSS_ID_FIVE))
					{
						for(Player character : World.getInstance().getPlayers())
						{
							character.sendPacket(new ExShowScreenMessage("[Solo Boss]: " + getName() + " was killed by " + player.getName(), 3 * 1000, ExShowScreenMessage.SMPOS.TOP_CENTER, true));
						}
						
					}
					
				}
			//	if (!player.isGM() && !(getNpcId() == Config.SOLO_BOSS_ID_ONE) && !(getNpcId() == Config.SOLO_BOSS_ID_TWO) && !(getNpcId() == Config.SOLO_BOSS_ID_THREE) && !(getNpcId() == Config.SOLO_BOSS_ID_FOUR) && !(getNpcId() == Config.SOLO_BOSS_ID_FIVE))
			//	{
			//		for (Player pl : World.getInstance().getPlayers())
			//		{
			//			if (player.getClan() != null)
			//				pl.sendChatMessage(0, Config.ANNOUNCE_ID, "Raid Boss ", getName() + " was killed by " + player.getName() + " of the clan " + player.getClan().getName());
			//			else
			//				pl.sendChatMessage(0, Config.ANNOUNCE_ID, "Raid Boss ", getName() + " was killed by " + player.getName());
			//			
			//			if(Config.EARTH_QUAKE)
			//			{
			//				pl.broadcastPacket(new ExRedSky(10));
			//				pl.broadcastPacket(new Earthquake(pl.getX(), pl.getY(), pl.getZ(), 14, 10));
			//			}
			//		}
			//	}
				
				if (Config.ANNOUNCE_RAIDBOS_KILL && !(getNpcId() == Config.SOLO_BOSS_ID_ONE) && !(getNpcId() == Config.SOLO_BOSS_ID_TWO) && !(getNpcId() == Config.SOLO_BOSS_ID_THREE) && !(getNpcId() == Config.SOLO_BOSS_ID_FOUR) && !(getNpcId() == Config.SOLO_BOSS_ID_FIVE))
				{
					if (player.getClan() != null)
						Broadcast.gameAnnounceToOnlinePlayers("Boss: " + getName() + " was killed by " + player.getName()+ " of the clan: " + player.getClan().getName());
					else
						Broadcast.gameAnnounceToOnlinePlayers("Boss: " + getName() + " was killed by " + player.getName());
				}
				
			//	ThreadPool.schedule(new Runnable()
			//	{
			//		@Override
			//		public void run()
				//	{
			//			for (Player pl : player.getKnownTypeInRadius(Player.class, 6000))
			//			{
			//				if (!pl.isInsideZone(ZoneId.PEACE))
			//					PvpFlagTaskManager.getInstance().add(pl, 60000);
			///			}
			//		}
			//	}, 3000);
				
				
				if (Config.ALLOW_AUTO_NOBLESS_FROM_BOSS && getNpcId() == Config.BOSS_ID)
				{
					if (player.getParty() != null)
					{
						for (Player member : player.getParty().getPartyMembers())
						{
							if (member.isNoble())
								member.sendMessage("Your party gained nobless status for defeating " + getName() + "!");
							else if (member.isInsideRadius(getX(), getY(), getZ(), Config.RADIUS_TO_RAID, false, false))
							{
								member.broadcastPacket(new SocialAction(member.getObjectId(), 16));
								member.setNoble(true, true);
								member.sendMessage("Your party gained nobless status for defeating " + getName() + "!");
							}
							else
							{
								member.sendMessage("Your party killed " + getName() + "! But you were to far away and earned nothing...");
							}
						}
						
					}
					else if (player.getParty() == null && !player.isNoble())
					{
						player.setNoble(true, true);
						player.sendMessage("You gained nobless status for defeating");
					}
				}
			}
		
		}	
		if(Config.ALLOW_FLAG_ONKILL_BY_ID)
		{
			updatePvpFlagById();
		}
		if (Config.SOLO_BOSS_EVENT)
		{
			rewardSoloEventPlayer();
		}
		if (!getSpawn().is_customBossInstance())
			RaidBossSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}

	private void updatePvpFlagById()
	{
		if (Config.NPCS_FLAG_LIST.contains(getNpcId()))
		{
			for (Player playerInRadius : getKnownTypeInRadius(Player.class, Config.NPCS_FLAG_RANGE)) 
			{
				final L2Party party = playerInRadius.getParty();
				if (party != null)
				{
					for (Player member : party.getPartyMembers())
					{
						PvpFlagTaskManager.getInstance().add(member, 60000);
					}
				}
				else
					PvpFlagTaskManager.getInstance().add(playerInRadius, 60000);
			}
		}		
	}

	/*private void rewardSoloEventPlayer()
	{
		if (getNpcId() == Config.SOLO_BOSS_ID_ONE)
		{
			List<String> _rewarded_hwid = new ArrayList<>();
			for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
			{
				String playerIp = player.getHWID();
				if (_rewarded_hwid.contains(playerIp)) 
					continue;
				
				_rewarded_hwid.add(player.getHWID());
				
		    	for (RewardHolder reward : Config.SOLO_BOSS_REWARDS_ONE) //1
	    		{
	    			if (Rnd.get(100) <= reward.getRewardChance())
	    			{
	    				if (player.isVip())
	    	    		{
	    					player.addItem("Solo Reward One", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()) * Config.RATE_DROP_VIP, player, true);
	    	    		}
	    	    		else
	    	    		{
	    	    			player.addItem("Solo Reward One", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
	    	    		}	
	    			}
	    		}

		    	MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
		    	player.broadcastPacket(MSU);
			}
		}
		else if (getNpcId() == Config.SOLO_BOSS_ID_TWO) //2
		{
			List<String> _rewarded_hwid = new ArrayList<>();
			for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
			{
				String playerIp = player.getHWID();
				if (_rewarded_hwid.contains(playerIp)) 
					continue;
				
				_rewarded_hwid.add(player.getHWID());
				
		    	for (RewardHolder reward : Config.SOLO_BOSS_REWARDS_TWO)
	    		{
	    			if (Rnd.get(100) <= reward.getRewardChance())
	    			{
	    				if (player.isVip())
	    	    		{
	    					player.addItem("Solo Reward TWO", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()) * Config.RATE_DROP_VIP, player, true);
	    	    		}
	    	    		else
	    	    		{
	    	    			player.addItem("Solo Reward TWO", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
	    	    		}	
	    			}
	    		}

		    	MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
		    	player.broadcastPacket(MSU);
			}
		}
		else if (getNpcId() == Config.SOLO_BOSS_ID_THREE) //3
		{
			List<String> _rewarded_hwid = new ArrayList<>();
			for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
			{
				String playerIp = player.getHWID();
				if (_rewarded_hwid.contains(playerIp)) 
					continue;
				
				_rewarded_hwid.add(player.getHWID());
				
		    	for (RewardHolder reward : Config.SOLO_BOSS_REWARDS_THREE)
	    		{
	    			if (Rnd.get(100) <= reward.getRewardChance())
	    			{
	    				if (player.isVip())
	    	    		{
	    					player.addItem("Solo Reward THREE", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()) * Config.RATE_DROP_VIP, player, true);
	    	    		}
	    	    		else
	    	    		{
	    	    			player.addItem("Solo Reward THREE", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
	    	    		}	
	    			}
	    		}

		    	MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
		    	player.broadcastPacket(MSU);
			}
		}
		else if (getNpcId() == Config.SOLO_BOSS_ID_FOUR) //4
		{
			List<String> _rewarded_hwid = new ArrayList<>();
			for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
			{
				String playerIp = player.getHWID();
				if (_rewarded_hwid.contains(playerIp)) 
					continue;
				
				_rewarded_hwid.add(player.getHWID());
				
		    	for (RewardHolder reward : Config.SOLO_BOSS_REWARDS_FOUR)
	    		{
	    			if (Rnd.get(100) <= reward.getRewardChance())
	    			{
	    				if (player.isVip())
	    	    		{
	    					player.addItem("Solo Reward FOUR", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()) * Config.RATE_DROP_VIP, player, true);
	    	    		}
	    	    		else
	    	    		{
	    	    			player.addItem("Solo Reward FOUR", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
	    	    		}	
	    			}
	    		}

		    	MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
		    	player.broadcastPacket(MSU);
			}
		}
		else if (getNpcId() == Config.SOLO_BOSS_ID_FIVE) //5
		{
			List<String> _rewarded_hwid = new ArrayList<>();
			for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
			{
				String playerIp = player.getHWID();
				if (_rewarded_hwid.contains(playerIp)) 
					continue;
				
				_rewarded_hwid.add(player.getHWID());
				
		    	for (RewardHolder reward : Config.SOLO_BOSS_REWARDS_FIVE)
	    		{
	    			if (Rnd.get(100) <= reward.getRewardChance())
	    			{
	    				if (player.isVip())
	    	    		{
	    					player.addItem("Solo Reward FIVE", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()) * Config.RATE_DROP_VIP, player, true);
	    	    		}
	    	    		else
	    	    		{
	    	    			player.addItem("Solo Reward FIVE", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
	    	    		}	
	    			}
	    		}

		    	MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
		    	player.broadcastPacket(MSU);
			}
		}
	}*/
	private static final Map<Integer, List<RewardHolder>> rewardsMap = new HashMap<>();

	static
	{
	    rewardsMap.put(Config.SOLO_BOSS_ID_ONE, Config.SOLO_BOSS_REWARDS_ONE);
	    rewardsMap.put(Config.SOLO_BOSS_ID_TWO, Config.SOLO_BOSS_REWARDS_TWO);
	    rewardsMap.put(Config.SOLO_BOSS_ID_THREE, Config.SOLO_BOSS_REWARDS_THREE);
	    rewardsMap.put(Config.SOLO_BOSS_ID_FOUR, Config.SOLO_BOSS_REWARDS_FOUR);
	    rewardsMap.put(Config.SOLO_BOSS_ID_FIVE, Config.SOLO_BOSS_REWARDS_FIVE);
	}

	private void rewardSoloEventPlayer()
	{
	    int npcId = getNpcId();

	    // Verifica se o NPC é um Solo Boss válido usando o Map
	    if (rewardsMap.containsKey(npcId))
	    {
	        List<String> rewardedHWID = new ArrayList<>();

	        // Itera sobre os jogadores próximos
	        for (Player player : getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
	        {
	            String playerIp = player.getHWID();

	            // Evita recompensar o mesmo jogador duas vezes
	            if (rewardedHWID.contains(playerIp)) continue;
	            rewardedHWID.add(playerIp);

	            // Aplica recompensas para o jogador
	            applyRewardsToPlayer(player, npcId);
	        }
	    }
	}

	// Aplica as recompensas para o jogador dependendo do NPC
	private static void applyRewardsToPlayer(Player player, int npcId) {
	    List<RewardHolder> rewards = rewardsMap.get(npcId);

	    // Itera sobre as recompensas
	    for (RewardHolder reward : rewards) {
	        if (Rnd.get(100) <= reward.getRewardChance()) {
	            // Define a quantidade de recompensa
	            int quantity = Rnd.get(reward.getRewardMin(), reward.getRewardMax());

	            // Se o jogador for VIP, aplica o VIP_DROP_RATE
	            if (player.isVip()) {
	                quantity = quantity * Config.RATE_DROP_VIP;
	            }

	            // Adiciona a recompensa ao inventário do jogador
	            player.addItem("Solo Reward", reward.getRewardId(), quantity, player, true);
	        }
	    }

	    // Transmite o uso de magia
	    broadcastSkillUse(player);
	}

	// Transmite a magia de recompensa para o jogador
	private static void broadcastSkillUse(Player player)
	{
	    MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
	    player.broadcastPacket(MSU);
	}
	
	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		super.deleteMe();
	}
	
	/**
	 * Spawn minions.<br>
	 * Also if boss is too far from home location at the time of this check, teleport it to home.
	 */
	//@Override
//	protected void startMaintenanceTask()
//	{
//		super.startMaintenanceTask();
//		
//		_maintenanceTask = ThreadPool.scheduleAtFixedRate(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				// If the boss is dead, movement disabled, is Gordon or is in combat, return.
//				if (isDead() || isMovementDisabled() || getNpcId() == 29095 || isInCombat() || getNpcId() == Config.SOLO_BOSS_ID_ONE || getNpcId() == Config.SOLO_BOSS_ID_TWO || getNpcId() == Config.SOLO_BOSS_ID_THREE || getNpcId() == Config.SOLO_BOSS_ID_FOUR || getNpcId() == Config.SOLO_BOSS_ID_FIVE)
//					return;
//				
//				// Spawn must exist.
//				final L2Spawn spawn = getSpawn();
//				if (spawn == null)
//					return;
//				
//				// If the boss is above drift range (or 200 minimum), teleport him on his spawn.
//				if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
//					teleToLocation(spawn.getLoc(), 0);
//			}
//		}, 60000, 30000);
//	}
	
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Use 1 thread para o agendamento
    private final long EXECUTION_INTERVAL = 1000; // 1 segundo de intervalo para o recall
    private long lastExecutionTime = 0; // Controla a execução para evitar chamadas excessivas

    @Override
    protected void startMaintenanceTask() {
        super.startMaintenanceTask(); // Mantém a funcionalidade herdada

        // Agendamento da tarefa de manutenção
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkAndReturnToSpawn();
            }
        }, 0, EXECUTION_INTERVAL, TimeUnit.MILLISECONDS); // Inicia imediatamente e repete a cada 1 segundo
    }

    protected void checkAndReturnToSpawn() {
        long currentTime = System.currentTimeMillis();

        // Evita chamadas excessivas em períodos muito curtos
        if (currentTime - lastExecutionTime < EXECUTION_INTERVAL) {
            return; // Se o intervalo ainda não passou, não execute
        }

        lastExecutionTime = currentTime; // Atualiza o tempo da última execução

        // Verifica se o RaidBoss está morto ou se não pode se mover
        if (isDead() || isMovementDisabled()) {
            return;
        }

        // Checa se o boss é um "solo boss", e se for, não faz o recall
        if (getNpcId() == 29095 || getNpcId() == Config.SOLO_BOSS_ID_ONE ||
            getNpcId() == Config.SOLO_BOSS_ID_TWO || getNpcId() == Config.SOLO_BOSS_ID_THREE ||
            getNpcId() == Config.SOLO_BOSS_ID_FOUR || getNpcId() == Config.SOLO_BOSS_ID_FIVE) {
            return; // Não faz o recall para esses bosses
        }

        final L2Spawn spawn = getSpawn();
        if (spawn == null) {
            return;
        }

        // Verifica se o RaidBoss está fora do raio do spawn
        if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Config.RANGE_BOSS_LIMIT, true, false)) {
            teleToLocation(spawn.getLoc(), 0); // Teleporta o boss para o spawn
        }

        // Se o recall com HP completo for habilitado, realiza o healing
        if (Config.ENABLE_FULL_HP_RECALL_BOSS) {
            healFull(); // Restaura o HP completo do boss
        }
    }
    public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
    // Método para parar a tarefa quando necessário (por exemplo, ao morrer ou ao desativar a manutenção)
    protected void stopMaintenanceTask() {
        scheduler.shutdown(); // Para o agendador de tarefas
    }
	
	
	/*  protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	  {
	    NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
	    try
	    {
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(xPos, yPos, zPos, 0);
			spawn.setRespawnDelay(0);
			
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
	}*/
	  
	/*	public static void Announce(String text)
		{
			CreatureSay cs = new CreatureSay(0, Config.ANNOUNCE_ID, "", "" + text);
			
			for (Player player : World.getInstance().getPlayers())
			{
				if (player != null)
					if (player.isOnline())
						player.sendPacket(cs);
				
			}
			cs = null;
		}*/
	
	public StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	
	public void setRaidStatus(StatusEnum status)
	{
		_raidStatus = status;
	}
}