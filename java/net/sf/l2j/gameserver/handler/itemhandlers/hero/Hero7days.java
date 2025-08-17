package net.sf.l2j.gameserver.handler.itemhandlers.hero;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * Hero7days: Garante 7 dias de Hero, independente do dia/mês atual.
 */
public class Hero7days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Hero7days.class.getName());
    private static final int HERO_DAYS = 7; // Exact number of days

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player)) {
            return;
        }

        Player activeChar = (Player) playable;

        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead()) {
            activeChar.sendMessage("SYS: You cannot use this item right now.");
            return;
        }

        // Check if player is already a Hero
        if (activeChar.isHero()) {
            activeChar.sendMessage("SYS: You already have Hero status.");
            return;
        }

        // Consume the item
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
            activeChar.sendMessage("SYS: Failed to consume the item.");
            return;
        }

        final long now = System.currentTimeMillis();
        long currentEnd = Math.max(now, HeroManager.getInstance().getHeroDuration(activeChar.getObjectId()));

        // Add 7 days (7 * 24 * 60 * 60 * 1000 ms)
        long additionalTime = HERO_DAYS * 24L * 60 * 60 * 1000;
        long newEndTime = currentEnd + additionalTime;

        // Apply Hero status
        if (HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId())) {
            HeroManager.getInstance().updateHero(activeChar.getObjectId(), newEndTime);
        } else {
            HeroManager.getInstance().addHero(activeChar.getObjectId(), newEndTime);
        }

        // Show expiration time to the player
        String formatted = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newEndTime));
        String message = "Your Hero status will expire on " + formatted + ".";
        activeChar.sendPacket(new ExShowScreenMessage(message, 10000));
        activeChar.sendMessage("SYS: " + message);
    }
}
