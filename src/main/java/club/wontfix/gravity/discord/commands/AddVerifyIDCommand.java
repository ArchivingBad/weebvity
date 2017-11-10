package club.wontfix.gravity.discord.commands;

import club.wontfix.gravity.Gravity;
import club.wontfix.gravity.discord.GravityCommand;
import club.wontfix.gravity.events.impl.actions.FreshVerifyIDEvent;
import club.wontfix.gravity.util.Util;
import com.darichey.discord.CommandContext;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.function.Consumer;

@GravityCommand(value = {"add", "adduser"}, allowPublic = false, description = "Adds a user to sudoers (limited)")
public class AddVerifyIDCommand implements Consumer<CommandContext> {

    @Override
    public void accept(CommandContext context) {
        if (context.getArgs().size() == 2) {
            String name = context.getArgs().get(0);
            boolean dev = Boolean.parseBoolean(context.getArgs().get(1));
            String verifyID = Util.generateVerifyID();

            if (Gravity.getInstance().getEasyDatabase().isVerifyIDBound(verifyID) ||
                    Gravity.getInstance().getEasyDatabase().isVerifyIDWaiting(verifyID)) {
                verifyID = Util.generateVerifyID();
                // ^  Probably will need changes as soon as we get to about 1k users  ^
            }

            FreshVerifyIDEvent freshEvent = new FreshVerifyIDEvent(name, verifyID, dev);
            Gravity.getInstance().getEventBus().post(freshEvent);
            if (!freshEvent.isCancelled()) {
                Gravity.getInstance().getEasyDatabase().registerWaitingVerifyID(name, verifyID, dev);

                IUser user = context.getGuild().getUsersByName(name, true).get(0);
                String finalVerifyID = verifyID;

                RequestBuffer.request(() -> new MessageBuilder(Gravity.getInstance().getDiscordBotManager().getDiscordClient())
                        .withChannel(context.getChannel())
                        .withEmbed(new EmbedBuilder()
                                .withAuthorName(name)
                                .withAuthorIcon(user.getAvatarURL())

                                .withDesc("**Success**! A new Verify ID has been generated.\n\n")
                                .appendDesc("**Name**: " + name + "\n")
                                .appendDesc("**Dev**: " + dev + "\n")
                                .appendDesc("**Verify ID**: " + finalVerifyID + "\n\n")

                                .appendDesc("It has been automatically added to the database.")
                                .appendDesc("Please input this Verify ID the next time when logging into Spartan.")

                                .withFooterText("Verify ID generated by " + context.getAuthor().getDisplayName(context.getGuild()))
                                .withColor(new Color(43, 115, 178))

                                .build())
                        .build());
            } else {
                Gravity.getInstance().getLogger().info("The action has been cancelled.");
            }
        } else {
            RequestBuffer.request(() -> new MessageBuilder(Gravity.getInstance().getDiscordBotManager().getDiscordClient())
                    .withChannel(context.getChannel())
                    .withContent("Usage: ~adduser <name> <true/false if dev>")
                    .build());
        }
    }

}