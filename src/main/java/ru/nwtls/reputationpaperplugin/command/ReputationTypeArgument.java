package ru.nwtls.reputationpaperplugin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class ReputationTypeArgument<C> extends CommandArgument<C, ReputationCommand.ReputationType> {
    private ReputationTypeArgument(final boolean required, final @NonNull String name, final @NonNull String defaultValue, final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider, final @NonNull ArgumentDescription defaultDescription) {
        super(required, name, new ReputationTypeArgument.ReputationTypeParser<>(), defaultValue, ReputationCommand.ReputationType.class, suggestionsProvider, defaultDescription);
    }

    public static <C> ReputationTypeArgument.@NonNull Builder<C> builder(final @NonNull String name) {
        return new ReputationTypeArgument.Builder<>(name);
    }

    private static final class ReputationTypeParser<C> implements ArgumentParser<C, ReputationCommand.ReputationType> {

        @Override
        public @NonNull ArgumentParseResult<ReputationCommand.ReputationType> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            String input = inputQueue.peek();
            if (input == null) return ArgumentParseResult.failure(new NoInputProvidedException(PlayerArgument.PlayerParser.class, commandContext));

            ReputationCommand.ReputationType type = null;
            try {
                input = input.toLowerCase() + "_reputation";
                type = ReputationCommand.ReputationType.valueOf(input.toUpperCase());
            } catch (Exception ignored) {}
            if (type == null) return ArgumentParseResult.failure(new ReputationTypeParseException(input, commandContext));

            inputQueue.remove();
            return ArgumentParseResult.success(type);
        }

        public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<C> commandContext, final @NonNull String input) {
            List<String> output = new ArrayList<>();
            List<ReputationCommand.ReputationType> var4 = Arrays.stream(ReputationCommand.ReputationType.values()).toList();
            var4.forEach(t -> output.add(t.name().toLowerCase().replace("_reputation", "")));
            return output;
        }

        private static final class ReputationTypeParseException extends ParserException {
            public ReputationTypeParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
                super(ReputationTypeArgument.ReputationTypeParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, CaptionVariable.of("input", input));
            }
        }
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, ReputationCommand.ReputationType> {
        private Builder(@NonNull String name) {
            super(ReputationCommand.ReputationType.class, name);
        }

        public @NonNull ReputationTypeArgument<C> build() {
            return new ReputationTypeArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription());
        }
    }
}
