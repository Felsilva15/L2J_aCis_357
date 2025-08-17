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
 * VIP de 24 horas (1 dia) - concede exatamente 24 horas de VIP a partir do uso do item.
 */
public class Vip24h implements IItemHandler {

    protected static final Logger LOGGER = Logger.getLogger(Vip24h.class.getName());
    private static final int VIP_HOURS = 24;

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player)) return;

        Player activeChar = (Player) playable;

        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage("SYS: You cannot do this during the Olympiad.");
            return;
        }

        if (activeChar.isVip()) {
            activeChar.sendMessage("SYS: You are already under VIP status.");
            return;
        }

        // Consume the item
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

        long now = System.currentTimeMillis();
        long currentVipEnd = Math.max(now, VipManager.getInstance().getVipDuration(activeChar.getObjectId()));

        // Add 24 hours
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentVipEnd);
        calendar.add(Calendar.HOUR_OF_DAY, VIP_HOURS);
        long newVipEnd = calendar.getTimeInMillis();

        if (VipManager.getInstance().hasVipPrivileges(activeChar.getObjectId())) {
            VipManager.getInstance().updateVip(activeChar.getObjectId(), newVipEnd);
        } else {
            VipManager.getInstance().addVip(activeChar.getObjectId(), newVipEnd);
        }

        String formattedDate = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newVipEnd));
        activeChar.sendPacket(new ExShowScreenMessage("Your VIP status will expire on " + formattedDate + ".", 10000));
        activeChar.sendMessage("Your VIP status will expire on " + formattedDate + ".");
    }

}
