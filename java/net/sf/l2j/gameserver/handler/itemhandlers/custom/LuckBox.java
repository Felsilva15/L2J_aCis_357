package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;

public class LuckBox implements IItemHandler {

    private static final String EMPTY_MESSAGE = "Ohh noo! Your lucky box is empty.";

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player))
            return;

        Player activeChar = (Player) playable;

        if (isIneligibleForUse(activeChar)) {
            activeChar.sendMessage("You cannot do that.");
            return;
        }

        int result = Rnd.get(100);

        if (result < 50 || Config.LUCKY_BOX_REWARDS.isEmpty()) {
            activeChar.sendMessage(EMPTY_MESSAGE);
        } else {
            int[] reward = Config.LUCKY_BOX_REWARDS.get(Rnd.get(Config.LUCKY_BOX_REWARDS.size()));
            activeChar.addItem("Luck Box", reward[0], reward[1], activeChar, true);
        }

        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0));
    }

    private static boolean isIneligibleForUse(Player activeChar) {
        return activeChar.isOlympiadProtection() || activeChar.isInCombat() ||
               activeChar.isInOlympiadMode() || activeChar.isDead();
    }
}
