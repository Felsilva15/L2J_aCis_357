package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;

import Dev.Ranking.Clan.ClanRankingConfig;

/**
 * This class manages all Grand Bosses.
 */
public final class GrandBoss extends Monster
{
	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public GrandBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
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
		
		private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);  // Use 1 thread para o agendamento
		private final long EXECUTION_INTERVAL = 1000; // 1 segundo de intervalo para o recall
		private long lastExecutionTime = 0; // Controla a execução para evitar chamadas excessivas
		
		@Override
		protected void startMaintenanceTask() {
			super.startMaintenanceTask();
			
			// Agendamento da tarefa
			scheduler.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					checkAndReturnToSpawn();
				}
			}, 0, EXECUTION_INTERVAL, TimeUnit.MILLISECONDS);  // Inicia imediatamente e repete a cada 1 segundo
		}
		
		protected void checkAndReturnToSpawn() {
			long currentTime = System.currentTimeMillis();
			
			// Evita chamadas excessivas em períodos muito curtos
			if (currentTime - lastExecutionTime < EXECUTION_INTERVAL) {
				return; // Se o intervalo ainda não passou, não execute
			}
			
			lastExecutionTime = currentTime; // Atualiza o tempo da última execução
			
			// Verifica se o boss está morto ou se não pode se mover
			if (isDead() || isMovementDisabled()) {
				return;
			}
			if (getNpcId() == 29066 || getNpcId() == 29067 || getNpcId() == 29068)
			{
	            return; // Não faz o recall para esses bosses
	        }
			final L2Spawn spawn = getSpawn();
			if (spawn == null) 
			{
				return;
			}
			
			// Verifica se o boss está fora do raio permitido para o spawn
			if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Config.RANGE_EPICBOSS, true, false)) {
				teleToLocation(spawn.getLoc(), 0);
			}
			
			// Se o recall com HP completo for habilitado, realiza o healing
			if (Config.ENABLE_FULL_HP_RECALL_EPIC) {
				healFull();
			}
		}
		
		// Método para parar a tarefa quando necessário (por exemplo, ao morrer ou ao desativar a manutenção)
		protected void stopMaintenanceTask() {
			scheduler.shutdown(); // Para o agendador de tarefas
		}
		public void healFull()
		{
			super.setCurrentHp(super.getMaxHp());
			super.setCurrentMp(super.getMaxMp());
		}
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		final Player player = killer.getActingPlayer();
		if (player != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			broadcastPacket(new PlaySound("systemmsg_e.1209"));
			
	  //      if (player.getClan() != null && Config.GRANDBOSS_INFO_IDS_LIST.contains(Integer.valueOf(getNpcId())))
	   //         player.getClan().addclanBossScore(1);
			
	        if (Config.ANNOUNCE_GRANDBOS_KILL)
			{
				if (player.getClan() != null)
					Broadcast.gameAnnounceToOnlinePlayers("Epic Boss: "+getName() +" was killed by " + player.getName()+ " of the clan: " + player.getClan().getName());
				else
					Broadcast.gameAnnounceToOnlinePlayers("Epic Boss: "+getName() +" was killed by " + player.getName());
			}
//	        if (ClanRankingConfig.ENABLE_CLAN_RANKING)
//			{
//				if (player != null && ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.containsKey(Integer.valueOf(getNpcId()))) 
//				{
//					Clan clan = player.getClan();
//					if (clan != null) 
//					{
//						int points = ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.get(Integer.valueOf(getNpcId())).intValue();
//						clan.addRankingBossPoints(points);
//
//						if (ClanRankingConfig.ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL)
//							Broadcast.gameAnnounceToOnlinePlayers("Clan " + clan.getName() + " gained " + points + " points defeating a Raid!");
//					} 
//				} 
//			}
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
		}
		
		return true;
	}
	
	@Override
	public boolean returnHome(boolean cleanAggro)
	{
		return false;
	}
}