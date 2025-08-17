package net.sf.l2j.gameserver.handler.itemhandlers.vip;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * VIP for 15 days - grants exactly 15 days of VIP from the moment the item is used.
 */
public class Vip15days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Vip15days.class.getName());
    private static final int VIP_DAYS = 15;

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player)) {
            return;
        }

        Player activeChar = (Player) playable;

        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage("SYS: You cannot use this item during the Olympiad.");
            return;
        }

        if (activeChar.isVip()) {
            activeChar.sendMessage("SYS: You are already under VIP status.");
            return;
        }

        // Consume the VIP item
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

        // Base time: now or current VIP expiration, whichever is later
        long now = System.currentTimeMillis();
        long currentVipEnd = Math.max(now, VipManager.getInstance().getVipDuration(activeChar.getObjectId()));

        // Add VIP days
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentVipEnd);
        calendar.add(Calendar.DAY_OF_MONTH, VIP_DAYS);
        long newVipEnd = calendar.getTimeInMillis();

        // Apply new VIP time
        VipManager vipManager = VipManager.getInstance();
        if (vipManager.hasVipPrivileges(activeChar.getObjectId())) {
            vipManager.updateVip(activeChar.getObjectId(), newVipEnd);
        } else {
            vipManager.addVip(activeChar.getObjectId(), newVipEnd);
        }

        // Notify the player
        String formattedDate = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newVipEnd));
        String message = "Your VIP status expires at " + formattedDate + ".";
        activeChar.sendPacket(new ExShowScreenMessage(message, 10000));
        activeChar.sendMessage(message);
    }

}
