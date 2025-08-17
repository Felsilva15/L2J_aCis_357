package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public class AdminDeleteItemAll implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_deleteitemall"
	};

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (!activeChar.getAccessLevel().isGm())
			return false;

		StringTokenizer st = new StringTokenizer(command);
		String actualCommand = st.nextToken();

		if (actualCommand.equals("admin_deleteitemall"))
		{
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Usage: //delete_item_all <itemId>");
				return false;
			}

			try
			{
				int itemId = Integer.parseInt(st.nextToken());
				int totalDeleted = 0;
				int playersAffected = 0;

				for (Player player : World.getInstance().getPlayers())
				{
					if (player == null || player.isGM()) // <-- pula GMs
						continue;

					boolean foundItem = false;

					// Inventário
					for (ItemInstance item : player.getInventory().getItems())
					{
						if (item != null && item.getItemId() == itemId)
						{
							player.getInventory().destroyItem("AdminDeleteAll", item, player, null);
							totalDeleted += item.getCount();
							foundItem = true;
						}
					}

					// Armazém
					for (ItemInstance item : player.getWarehouse().getItems())
					{
						if (item != null && item.getItemId() == itemId)
						{
							player.getWarehouse().destroyItem("AdminDeleteAll", item, player, null);
							totalDeleted += item.getCount();
							foundItem = true;
						}
					}

					if (foundItem)
					{
						playersAffected++;
						player.sendPacket(new ItemList(player, true));
					}
				}

				activeChar.sendMessage("✔ Deletado o item ID " + itemId + " de " + playersAffected + " jogadores (exceto GMs). Total de itens removidos: " + totalDeleted + ".");
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Invalid itemId. Usage: //delete_item_all <itemId>");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("An error occurred while executing the command.");
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
