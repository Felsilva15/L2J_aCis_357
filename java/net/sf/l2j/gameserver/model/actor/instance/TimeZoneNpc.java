package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;

import Dev.InstanceFarm.TimeInstanceManager;
import Dev.InstanceFarm.TimeInstanceRemainTaskManager;

public class TimeZoneNpc extends Folk
{	
	public TimeZoneNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		boolean pass = false;
		
		if (command.startsWith("timepass"))
		{
			if (Config.TIME_INSTANCE_BLOCK_CLASS_LIST.contains(player.getClassId().getId()))
			{
				player.sendMessage("Your class is not allowed in Time Instance Zone.");
				return;
			}
			if (player.getMountType() == 2 || player.getMountType() == 1)
			{
				player.sendMessage("Your Mounted Pets in Entrance Zone.");
				return;
			}
			if (player.TimeInstanceAvaiable())
				pass = true;
			else
			{
				if (player.getInventory().getInventoryItemCount(Config.TIME_INSTANCE_ITEM_ID_TO_ACESS, 0) >= 1)
				{
					pass = true;
					
					player.getInventory().destroyItemByItemId("TI", Config.TIME_INSTANCE_ITEM_ID_TO_ACESS, 1, player, null);
					
					TimeInstanceManager.updatePlayerTime(player);
					TimeInstanceManager.broadcastTimer(player);
				}
				else
					player.sendMessage("You don't have the necessary item to go to the Time Instance Zone.");
			}
			
			/*if (pass)
			{
				int spot = Integer.parseInt(command.substring(8).trim());
				
				new TimeInstanceRemainTaskManager(player);
				
				if (spot == 1)
					if(Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
					{
						MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
						player.broadcastPacket(MSU);
						player.sendPacket(MSU);
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								//Teleport Loading
								player.sendPacket(new ExPCCafePointInfo(player, 0, false, 99981, false)); 

								player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_1, 0);
								// you cannot teleport to village that is in siege
							}
						}, 0);
					}
					else
					{
						player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_1, 0);	
					}
				else if (spot == 2)
					if(Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
					{
						MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
						player.broadcastPacket(MSU);
						player.sendPacket(MSU);
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								//Teleport Loading
								player.sendPacket(new ExPCCafePointInfo(player, 0, false, 99981, false)); 

								player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_2, 0);
								// you cannot teleport to village that is in siege
							}
						}, 0);
					}
					else
					{
						player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_2, 0);
					}
				else if (spot == 3)
					if(Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
					{
						MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
						player.broadcastPacket(MSU);
						player.sendPacket(MSU);
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								//Teleport Loading
								player.sendPacket(new ExPCCafePointInfo(player, 0, false, 99981, false)); 

								player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_3, 0);
								// you cannot teleport to village that is in siege
							}
						}, 0);
					}
					else
					{
						player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_3, 0);
					}
				else if (spot == 4)
					if(Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
					{
						MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
						player.broadcastPacket(MSU);
						player.sendPacket(MSU);
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								//Teleport Loading
								player.sendPacket(new ExPCCafePointInfo(player, 0, false, 99981, false)); 

								player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_4, 0);
								// you cannot teleport to village that is in siege
							}
						}, 0);
					}
					else
					{
						player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_4, 0);
					}
				//	else
				//		player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_1, 0); nao usado
			}
		}*/
			if (pass)
			{
			    int spot = Integer.parseInt(command.substring(8).trim());

			    new TimeInstanceRemainTaskManager(player);

			    // Criar um ExecutorService (apenas uma vez, pode ser colocado em uma classe que gerencie o pool de threads)
			    ExecutorService executor = Executors.newCachedThreadPool();

			    Runnable teleportTask = new Runnable()
			    {
			        @Override
			        public void run()
			        {
			            // Enviar a mensagem de carregamento
			            player.sendPacket(new ExPCCafePointInfo(player, 0, false, 99981, false));

			            // Realizar o teletransporte
			            if (spot == 1)
			                player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_1, 0);
			            else if (spot == 2)
			                player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_2, 0);
			            else if (spot == 3)
			                player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_3, 0);
			            else if (spot == 4)
			                player.teleToLocation(Config.TIME_INSTANCE_AREA_LOC_4, 0);
			        }
			    };

			    if (spot >= 1 && spot <= 4) 
			    {
			        if (Config.OPEN_EFFECT_CLASSIC_TELEPORTER_FARM)
			        {
			            // Criar e enviar o efeito de magia de teletransporte
			            MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
			            player.broadcastPacket(MSU);
			            player.sendPacket(MSU);

			            // Enviar a tarefa para o Executor
			            executor.submit(teleportTask);
			        }
			        else
			        {
			            // Caso o efeito de teletransporte não esteja habilitado, teletransporta imediatamente
			            player.teleToLocation(spot == 1 ? Config.TIME_INSTANCE_AREA_LOC_1 : 
			                                  (spot == 2 ? Config.TIME_INSTANCE_AREA_LOC_2 : 
			                                  (spot == 3 ? Config.TIME_INSTANCE_AREA_LOC_3 : 
			                                  Config.TIME_INSTANCE_AREA_LOC_4)), 0);
			        }
			    }

			    // Fechar o executor após todas as tarefas serem enviadas (se for o último uso)
			    executor.shutdown();
			    try {
			        // Espera até que todas as tarefas sejam concluídas ou um tempo limite
			        if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
			            executor.shutdownNow(); // Se as tarefas não terminarem, forçar o shutdown
			        }
			    } catch (InterruptedException e) {
			        executor.shutdownNow(); // Em caso de interrupção, força o shutdown
			    }
			}
		}

		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/mods/InstanceFarm/Main.htm");
		html.replace("%objectId%", String.valueOf(player.getTargetId()));
		player.sendPacket(html);
	}
}