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
 * VIP de 30 dias - concede 30 dias exatos de VIP a partir do momento do uso.
 * @author MeGaPacK
 */
public class Vip30days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Vip30days.class.getName());
    private static final int VIP_DAYS = 30;

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player)) {
            return;
        }

        Player activeChar = (Player) playable;

        // Check if the player is in Olympiad mode
        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage("SYS: You cannot use this item during the Olympiad.");
            return;
        }

        // Check if the player already has VIP status
        if (activeChar.isVip()) {
            activeChar.sendMessage("SYS: You already have VIP status.");
            return;
        }

        // Consume the VIP item
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
            activeChar.sendMessage("SYS: Failed to consume the item.");
            return;
        }

        // Define the base date (now or current VIP end, whichever is greater)
        long now = System.currentTimeMillis();
        long currentVipEnd = Math.max(now, VipManager.getInstance().getVipDuration(activeChar.getObjectId()));

        // Add days to the calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentVipEnd);
        calendar.add(Calendar.DAY_OF_MONTH, VIP_DAYS);
        long newVipEnd = calendar.getTimeInMillis();

        // Apply the new VIP duration
        VipManager vipManager = VipManager.getInstance();
        int objectId = activeChar.getObjectId();

        if (vipManager.hasVipPrivileges(objectId)) {
            vipManager.updateVip(objectId, newVipEnd);
        } else {
            vipManager.addVip(objectId, newVipEnd);
        }

        // Send the formatted expiration date to the player
        String formattedDate = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newVipEnd));
        String message = "Your VIP status will expire on " + formattedDate + ".";
        activeChar.sendPacket(new ExShowScreenMessage(message, 10000));
        activeChar.sendMessage("SYS: " + message);
    }
}
