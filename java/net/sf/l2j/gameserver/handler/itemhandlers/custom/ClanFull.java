package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Bluur
 */
public class ClanFull implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player player = (Player) playable;
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("SYS: Voce nao pode fazer isso.");
			return;
		}
		
		if (player.isClanLeader())
		{
			if (player.getClan().getLevel() == 8)
			{
				player.sendMessage("Your clan is already Level 8!");
				return;
			}
			
			for (int i = 370; i <= 391; i++)
			{
				player.getClan().addNewSkill(SkillTable.getInstance().getInfo(i, SkillTable.getInstance().getMaxLevel(i)));
			}
			player.getClan().changeLevel(8);
			player.getClan().addReputationScore(300000);
			player.sendPacket(new ExShowScreenMessage("Now your clan is lvl 8 Full Skill!", 10000, 0x02, true));
			player.destroyItem("", item.getObjectId(), 1, null, true);
		}
		else
			player.sendMessage("Only leaders of the clans can use this item!");
	}
}