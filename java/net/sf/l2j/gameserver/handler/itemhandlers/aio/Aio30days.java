package net.sf.l2j.gameserver.handler.itemhandlers.aio;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAiox;
import net.sf.l2j.gameserver.instancemanager.AioManager;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sf.l2j.Config;

public class Aio30days implements IItemHandler {
  @Override
public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
    if (!(playable instanceof Player))
      return; 
    if (!Config.ENABLE_AIO_SYSTEM)
      return; 
    Player activeChar = (Player)playable;
    if (activeChar.isInOlympiadMode()) {
    	activeChar.sendMessage("SYS: You cannot do that.");
      return;
    } 
    if (activeChar.isAioEterno()) {
    	activeChar.sendMessage("SYS: You cannot do that.");
      return;
    } 
    if (AioManager.getInstance().hasAioPrivileges(activeChar.getObjectId())) {
    	activeChar.sendMessage("SYS: Please wait for your Aio Buffer time to finish.");
      return;
    } 

	playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	
	int mes = 30;
	
	if (AioManager.getInstance().hasAioPrivileges(activeChar.getObjectId()))
	{
		long _daysleft;
		final long now = Calendar.getInstance().getTimeInMillis();
		long duration = AioManager.getInstance().getAioDuration(activeChar.getObjectId());
		final long endDay = duration;
		
		_daysleft = ((endDay - now) / 86400000) + mes + 1;
		
		long end_day;
		final Calendar calendar = Calendar.getInstance();
		if (_daysleft >= 30)
		{
			while (_daysleft >= 30)
			{
				if (calendar.get(Calendar.MONTH) == 11)
					calendar.roll(Calendar.YEAR, true);
				calendar.roll(Calendar.MONTH, true);
				_daysleft -= 30;
			}
		}
		
		if (_daysleft < 30 && _daysleft > 0)
		{
			while (_daysleft > 0)
			{
				if (calendar.get(Calendar.DATE) == 28 && calendar.get(Calendar.MONTH) == 1)
					calendar.roll(Calendar.MONTH, true);
				if (calendar.get(Calendar.DATE) == 30)
				{
					if (calendar.get(Calendar.MONTH) == 11)
						calendar.roll(Calendar.YEAR, true);
					calendar.roll(Calendar.MONTH, true);
					
				}
				calendar.roll(Calendar.DATE, true);
				_daysleft--;
			}
		}
		
		end_day = calendar.getTimeInMillis();
		AioManager.getInstance().updateAio(activeChar.getObjectId(), end_day);
	}
	else
	{
		long end_day;
		final Calendar calendar = Calendar.getInstance();
		if (mes >= 30)
		{
			while (mes >= 30)
			{
				if (calendar.get(Calendar.MONTH) == 11)
					calendar.roll(Calendar.YEAR, true);
				calendar.roll(Calendar.MONTH, true);
				mes -= 30;
			}
		}
		
		if (mes < 30 && mes > 0)
		{
			while (mes > 0)
			{
				if (calendar.get(Calendar.DATE) == 28 && calendar.get(Calendar.MONTH) == 1)
					calendar.roll(Calendar.MONTH, true);
				if (calendar.get(Calendar.DATE) == 30)
				{
					if (calendar.get(Calendar.MONTH) == 11)
						calendar.roll(Calendar.YEAR, true);
					calendar.roll(Calendar.MONTH, true);
					
				}
				calendar.roll(Calendar.DATE, true);
				mes--;
			}
		}
		
		end_day = calendar.getTimeInMillis();
		AioManager.getInstance().addAio(activeChar.getObjectId(), end_day);
	}
	
	long _daysleft;
	final long now = Calendar.getInstance().getTimeInMillis();
	long duration = AioManager.getInstance().getAioDuration(activeChar.getObjectId());
	final long endDay = duration;
	_daysleft = ((endDay - now) / 86400000);
	if (_daysleft < 270)
	{
		activeChar.sendPacket(new ExShowScreenMessage("Your Aio privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".", 10000));
		activeChar.sendMessage("Your Aio privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
	}	
	
    if (Config.CHANGE_AIO_NAME)
        AdminAiox.nameChanger("[AIO]", activeChar); 
    if (Config.ALLOW_AIO_NCOLOR)
        activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR); 
      if (Config.ALLOW_AIO_TCOLOR)
        activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR); 

      activeChar.getStat().addExp(activeChar.getStat().getExpForLevel(81));
      
	activeChar.broadcastUserInfo();
	
  }
}
