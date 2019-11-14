package rasecbot;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.VoiceChannelEditSpec;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {

    private static final String rasecbotToken = "NjQzMDc0OTQ0NzI0NjMxNTkz.XcgPIA.ZBtO3xe1yBvywcvCKRgq77cxS1c";
    private static final DiscordClient client = new DiscordClientBuilder(rasecbotToken).build();

    private static final String FREQUENT_CATEGORY_NAME = "Canales Uso Frecuente";
    private static final String INFREQUENT_CATEGORY_NAME = "Canales Uso Infrecuente";
    private static Snowflake frequentCategoryId;
    private static Snowflake infrequentCategoryId;

    public static void main(String[] args) {

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().map("r?voice-channels"::equals).orElse(false))
                .subscribe(message -> {
                            message.getGuild().block()
                                    .getChannels()
                                    .filter(channel -> Channel.Type.GUILD_VOICE.equals(channel.getType()))
                                    .toStream()
                                    .forEach(voiceChannel ->
                                        message.getChannel().block()
                                               .createMessage("s?channel " + voiceChannel.getName())
                                               .subscribe()
                                    );
                });


        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().isPresent() && msg.getContent().get().startsWith("r?move-frequents"))
                .subscribe(message -> {
                    final String[] params = message.getContent().get().split(":");
                    final Flux<GuildChannel> allChannels = message.getGuild().block().getChannels();

                    allChannels
                            .filter(channel -> Channel.Type.GUILD_CATEGORY.equals(channel.getType()) && channel.getName().contains(FREQUENT_CATEGORY_NAME))
                            .subscribe(channel -> frequentCategoryId = ((Category)channel).getId());

                    for (int i = 1; i < params.length; i++) {
                        int index = i;
                        allChannels
                                .filter(channel -> Channel.Type.GUILD_VOICE.equals(channel.getType()) && channel.getName().contains(params[index]))
                                .toStream()
                                .forEach(channel -> {
                                    ((VoiceChannel)channel).edit(spec -> spec.setParentId(frequentCategoryId)).subscribe();
                                });
                    }
                });

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().isPresent() && msg.getContent().get().startsWith("r?move-infrequents"))
                .subscribe(message -> {
                    final String[] params = message.getContent().get().split(":");
                    final Flux<GuildChannel> allChannels = message.getGuild().block().getChannels();

                    allChannels
                            .filter(channel -> Channel.Type.GUILD_CATEGORY.equals(channel.getType()) && channel.getName().contains(INFREQUENT_CATEGORY_NAME))
                            .subscribe(channel -> infrequentCategoryId = ((Category)channel).getId());

                    for (int i = 1; i < params.length; i++) {
                        int index = i;
                        allChannels
                                .filter(channel -> Channel.Type.GUILD_VOICE.equals(channel.getType()) && channel.getName().contains(params[index]))
                                .toStream()
                                .forEach(channel -> {
                                    ((VoiceChannel)channel).edit(spec -> spec.setParentId(infrequentCategoryId)).subscribe();
                                });
                    }
                });

        client.login().block();
    }




    private static String getStringFromFlux(Flux<String> list) {
        StringBuffer result = new StringBuffer();
        list.toStream().forEach(s -> result.append(s));
        return result.toString();
    }
}
