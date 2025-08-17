package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;

import java.util.Collection;

/**
 * This class handles the admin command: 
 * - deleteall <npc_id> = deletes all NPCs with the specified ID.
 * - deleteall (without id) = deletes all NPCs with the same ID as the targeted NPC.
 * This will delete the NPCs and their spawns permanently.
 */
public class AdminDeleteAll implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS =
    {
        "admin_deleteall", // General command to delete NPCs by ID or by target
    };
    
    @Override
    public boolean useAdminCommand(String command, Player activeChar)
    {
        if (command.startsWith("admin_deleteall"))
        {
            String[] commandParts = command.split(" ");
            
            // Check if the command includes an NPC ID
            if (commandParts.length == 2)
            {
                try
                {
                    int npcId = Integer.parseInt(commandParts[1]);
                    deleteNpcById(npcId, activeChar);
                }
                catch (NumberFormatException e)
                {
                    activeChar.sendMessage("Invalid NPC ID format.");
                }
            }
            // If no ID provided, try deleting all NPCs with the same ID as the targeted NPC
            else
            {
                deleteNpcByTarget(activeChar);
            }
        }
        return true;
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private void deleteNpcById(int npcId, Player activeChar)
    {
        // Get all objects in the world (NPCs, Players, Pets, etc.)
        Collection<WorldObject> objects = World.getInstance().getObjects();
        int deletedCount = 0;

        // Loop through all objects and filter only NPCs to delete those matching the npcId
        for (WorldObject object : objects)
        {
            if (object instanceof Npc)
            {
                Npc npc = (Npc) object;
                if (npc.getNpcId() == npcId) // Check if the NPC matches the given ID
                {
                    // Try to get the associated spawn of the NPC
                    L2Spawn spawn = npc.getSpawn();

                    // If spawn exists, mark it as non-respawnable
                    if (spawn != null)
                    {
                        spawn.setRespawnState(false); // Disable respawn
                        // Remove the spawn from the SpawnTable permanently
                        SpawnTable.getInstance().deleteSpawn(spawn, true); // Pass true to permanently delete from DB
                    }

                    npc.deleteMe(); // Delete the NPC from the world
                    deletedCount++;
                }
            }
        }

        // Send message to the admin about the number of deleted NPCs
        if (deletedCount > 0)
        {
            activeChar.sendMessage("Deleted " + deletedCount + " NPC(s) with ID " + npcId + " permanently.");
        }
        else
        {
            activeChar.sendMessage("No NPCs found with ID " + npcId + ".");
        }
    }

    private void deleteNpcByTarget(Player activeChar)
    {
        // Get the NPC that the admin is currently targeting
        Npc targetNpc = (Npc) activeChar.getTarget();

        // Check if the target is an NPC
        if (targetNpc != null)
        {
            int npcId = targetNpc.getNpcId(); // Get the NPC ID of the targeted NPC
            Collection<WorldObject> objects = World.getInstance().getObjects();
            int deletedCount = 0;

            // Loop through all objects in the world and delete all NPCs with the same ID as the targeted NPC
            for (WorldObject object : objects)
            {
                if (object instanceof Npc)
                {
                    Npc npc = (Npc) object;
                    if (npc.getNpcId() == npcId) // Check if the NPC matches the ID of the targeted NPC
                    {
                        L2Spawn spawn = npc.getSpawn();

                        // If spawn exists, mark it as non-respawnable
                        if (spawn != null)
                        {
                            spawn.setRespawnState(false); // Disable respawn
                            // Remove the spawn from the SpawnTable permanently
                            SpawnTable.getInstance().deleteSpawn(spawn, true); // Pass true to permanently delete from DB
                        }

                        npc.deleteMe(); // Delete the NPC from the world
                        deletedCount++;
                    }
                }
            }

            // Send message to the admin about the number of deleted NPCs
            if (deletedCount > 0)
            {
                activeChar.sendMessage("Deleted " + deletedCount + " NPC(s) with ID " + npcId + " (matching target) permanently.");
            }
            else
            {
                activeChar.sendMessage("No NPCs found with ID " + npcId + " (matching target).");
            }
        }
        else
        {
            activeChar.sendMessage("You need to target an NPC to delete all NPCs with the same ID.");
        }
    }
}
