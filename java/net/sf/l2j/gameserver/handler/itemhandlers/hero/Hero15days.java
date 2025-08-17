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

public class Hero15days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Hero15days.class.getName());
    private static final long HERO_DURATION_MS = 15L * 24 * 60 * 60 * 1000; // 15 days in milliseconds

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

        // Calculate new hero duration time
        final long now = System.currentTimeMillis();
        long currentEnd = Math.max(now, HeroManager.getInstance().getHeroDuration(activeChar.getObjectId()));
        long newEndTime = currentEnd + HERO_DURATION_MS;

        // Update or add hero time
        if (HeroManager.getInstance().hasHeroPrivileges(activeChar.getObjectId())) {
            HeroManager.getInstance().updateHero(activeChar.getObjectId(), newEndTime);
        } else {
            HeroManager.getInstance().addHero(activeChar.getObjectId(), newEndTime);
        }

        // Feedback to player
        String formatted = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newEndTime));
        activeChar.sendPacket(new ExShowScreenMessage("Your Hero status will expire on " + formatted + ".", 10000));
        activeChar.sendMessage("Your Hero status will expire on " + formatted + ".");
    }
}
