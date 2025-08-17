package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import Dev.Event.BossEvent.KTBEvent;

public class BossEventStart extends Folk
{
	private static final String ktbhtmlPath = "data/html/mods/BossEvent/";

	public BossEventStart(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		KTBEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;

		 if (KTBEvent.isParticipating())
		{
			final boolean isParticipant = KTBEvent.isPlayerParticipant(playerInstance.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(ktbhtmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(ktbhtmlPath + "RemoveParticipation.htm");

			if (htmContent != null)
			{
				int PlayerCounts = KTBEvent.getPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				if (!isParticipant)
					npcHtmlMessage.replace("%fee%", KTBEvent.getParticipationFee());

				playerInstance.sendPacket(npcHtmlMessage);
			}
		}

		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}