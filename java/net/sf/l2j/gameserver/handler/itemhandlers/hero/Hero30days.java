package net.sf.l2j.gameserver.handler.itemhandlers.hero;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class Hero30days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Hero30days.class.getName());
    private static final int HERO_DAYS = 30; // Duration in days

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player))
            return;

        Player activeChar = (Player) playable;

        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead()) {
            activeChar.sendMessage("SYS: You cannot use this item right now.");
            return;
        }

        if (activeChar.isHero()) {
            activeChar.sendMessage("SYS: You already have Hero status.");
            return;
        }

        // Consume the item
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

        final long now = System.currentTimeMillis();
        long currentEnd = Math.max(now, HeroManager.getInstance().getHeroDuration(activeChar.getObjectId()));

        // Add 30 days in milliseconds
        long additionalTime = HERO_DAYS * 24L * 60 * 60 * 1000;
        long newEndTime = currentEnd + additionalTime;

        // Update or add Hero
        if (HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId())) {
            HeroManager.getInstance().updateHero(activeChar.getObjectId(), newEndTime);
        } else {
            HeroManager.getInstance().addHero(activeChar.getObjectId(), newEndTime);
        }

        // Message to the player
        String formatted = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newEndTime));
        activeChar.sendPacket(new ExShowScreenMessage("Your Hero status will expire on " + formatted + ".", 10000));
        activeChar.sendMessage("Your Hero status will expire on " + formatted + ".");
    }
}
