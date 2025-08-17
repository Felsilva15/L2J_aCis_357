package net.sf.l2j.gameserver.instancemanager;

import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCustom;
import net.sf.l2j.gameserver.model.actor.instance.Player;


public class NewsHtml implements Runnable {
  private Player _activeChar;
  
  public NewsHtml(Player activeChar) {
    _activeChar = activeChar;
  }
  
  @Override
public void run() {
    if (_activeChar.isOnline())
      AdminCustom.showNewsHtml(_activeChar); 
  }
}
