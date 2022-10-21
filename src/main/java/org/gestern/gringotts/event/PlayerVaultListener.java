package org.gestern.gringotts.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.Language;
import org.gestern.gringotts.Permissions;
import org.gestern.gringotts.accountholder.AccountHolder;
import org.gestern.gringotts.accountholder.PlayerAccountHolder;

/**
 * This Vault listener handles vault creation events for player vaults.
 *
 * @author jast
 */
public class PlayerVaultListener implements Listener {

    @EventHandler
    public void calculateStartBalance(CalculateStartBalanceEvent event) {
        if (!event.holder.getType().equals("player")) {
            return;
        }

        event.startValue = Configuration.CONF.getCurrency().getCentValue(Configuration.CONF.startBalancePlayer);
    }

    @EventHandler
    public void vaultCreated(PlayerVaultCreationEvent event) {
        // some listener already claimed this event
        if (event.isValid()) {
            return;
        }

        // only interested in player vaults
        if (!event.getType().equals("player")) {
            return;
        }

        SignChangeEvent cause       = event.getCause();
        String          line2String = cause.getLine(2);

        if (line2String == null) {
            return;
        }

        Player player = cause.getPlayer();

        if (!Permissions.CREATE_VAULT_PLAYER.isAllowed(player)) {
            player.sendMessage(Language.LANG.vault_noVaultPerm);

            return;
        }

        AccountHolder owner;

        if (line2String.length() > 0 && Permissions.CREATE_VAULT_ADMIN.isAllowed(player)) {
            // attempting to create account for other player
            owner = Gringotts.instance.getAccountHolderFactory().get("player", line2String);

            if (owner == null) {
                return;
            }
        } else {
            // regular vault creation for self
            owner = new PlayerAccountHolder(player);
        }

        event.setOwner(owner);
        event.setValid(true);
    }
}
