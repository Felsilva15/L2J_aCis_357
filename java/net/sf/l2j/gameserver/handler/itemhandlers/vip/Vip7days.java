package net.sf.l2j.gameserver.handler.itemhandlers.vip;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * VIP de 7 dias - concede 7 dias exatos de VIP a partir do momento do uso.
 */
public class Vip7days implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Vip7days.class.getName());
    private static final int VIP_DAYS = 7; // 7 days of VIP

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player)) return;

        Player activeChar = (Player) playable;

        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage("SYS: You cannot do this during the Olympiad.");
            return;
        }

        if (activeChar.isVip()) {
            activeChar.sendMessage("SYS: You are already VIP.");
            return;
        }

        // Consumes the item
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

        // Gets the current time and VIP expiration time
        long now = System.currentTimeMillis();
        long currentVipEnd = Math.max(now, VipManager.getInstance().getVipDuration(activeChar.getObjectId()));

        // Adds 7 days in milliseconds (7 * 24 * 60 * 60 * 1000)
        long additionalTime = VIP_DAYS * 24L * 60 * 60 * 1000;
        long newVipEnd = currentVipEnd + additionalTime;

        // Updates or adds VIP status for the player
        if (VipManager.getInstance().hasVipPrivileges(activeChar.getObjectId())) {
            VipManager.getInstance().updateVip(activeChar.getObjectId(), newVipEnd);
        } else {
            VipManager.getInstance().addVip(activeChar.getObjectId(), newVipEnd);
        }

        // Formats and shows the expiration date to the player
        String formattedDate = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newVipEnd));
        activeChar.sendPacket(new ExShowScreenMessage("Your VIP status expires at " + formattedDate + ".", 10000));
        activeChar.sendMessage("Your VIP status expires at " + formattedDate + ".");
    }
}
