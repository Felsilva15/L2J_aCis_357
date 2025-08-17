package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.data.xml.DressMeData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.DressMe;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import net.sf.l2j.gameserver.util.Broadcast;

import net.sf.l2j.commons.concurrent.ThreadPool;

public class Skins implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		final Player player = (Player) playable;
		
		if (!(playable instanceof Player))
			return;
		
		final DressMe dress = DressMeData.getInstance().getItemId(item.getItemId());
		final DressMe dress2 = DressMeData.getInstance().getItemId(0);
		if (dress == null)
			return;
		
		
		if (player.isDressMeEnabled())
		{
			player.setDress(dress2);
			player.setDressMeEnabled(false);
			player.setDressMeHelmEnabled(false);
			player.broadcastUserInfo();
			player.sendMessage("You have disabled skin.");
			
		}
		else
		{
			
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					playable.setIsParalyzed(false);
				}
			},3000);
			final WorldObject oldTarget = playable.getTarget();
			playable.setTarget(playable);		
			Broadcast.toSelfAndKnownPlayers(playable, new MagicSkillUse(playable, 1036, 1, 3000, 0));
			playable.setTarget(oldTarget);
			playable.sendPacket(new SetupGauge(GaugeColor.BLUE, 3000));	
			playable.setIsParalyzed(true);
			
			player.setDressMeEnabled(true);
			player.setDressMeHelmEnabled(true);
			player.setDress(dress);
			player.broadcastUserInfo();
			player.sendMessage("You have activated Skin.");
			
		}
	
	}
}