package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Format {
  /* No initialization */
  private Format() {}

  // TODO: Configurable values
  public static final boolean SHOW_DATE_IN_LOOKUP = false;
  public static final NamedTextColor THEME_COLOR = NamedTextColor.GREEN;
  public static final NamedTextColor TEXT_COLOR = NamedTextColor.WHITE;
  public static final NamedTextColor TITLE_COLOR = NamedTextColor.GOLD;
  public static final NamedTextColor NAME_COLOR = NamedTextColor.WHITE;
  public static final NamedTextColor STACK_COLOR = NamedTextColor.WHITE;
  public static final NamedTextColor INFO_COLOR = NamedTextColor.WHITE;
  public static final NamedTextColor PADDING_COLOR = NamedTextColor.GRAY;
  public static final NamedTextColor DATE_COLOR = NamedTextColor.DARK_GRAY;
  public static final NamedTextColor ITEM_COLOR = NamedTextColor.GREEN;
  public static final NamedTextColor SPREAD_ITEM_COLOR = NamedTextColor.WHITE;
  public static final NamedTextColor ACTION_COLOR = THEME_COLOR;
  public static final NamedTextColor HINT_COLOR = NamedTextColor.BLUE;
  public static final NamedTextColor HOVER_HINT_COLOR = HINT_COLOR;
  public static final NamedTextColor HOVER_TEXT_COLOR = NamedTextColor.GRAY;
  public static final NamedTextColor ERROR_COLOR = NamedTextColor.RED;

  public static final Component PADDING = Component.text("=").color(PADDING_COLOR);

  // Character pixel widths based on Minecraft's font
  // https://chatgpt.com/share/67bb92d9-34cc-8012-ab3e-ab6df408b3ca
  private static final int DEFAULT_WIDTH = 6;
  private static final java.util.Map<Character, Integer> CHAR_WIDTHS =
      java.util.Map.of('i', 3, 'l', 3, '.', 3, ' ', 4, 'M', 9, 'W', 9, '@', 9);

  public static Component prefix = Component.text("Espial › ").color(THEME_COLOR);

  public static Component component(Component component) {
    return prefix.append(component);
  }

  public static Component component(TextComponent.Builder builder) {
    return component(builder.build());
  }

  public static Component text(String text) {
    return prefix.append(Component.text(text).color(TEXT_COLOR));
  }

  public static Component error(String text) {
    return component(Component.text(text).color(ERROR_COLOR));
  }

  public static Component noBlockFound() {
    return error("Could not find a block. Maybe move closer?");
  }

  public static Component playersOnly() {
    return error("Only players can run this command.");
  }

  public static Component defaults(String defaults) {
    return component(Component.text("Defaults used: " + defaults).color(NamedTextColor.GRAY));
  }

  public static Component title(String text) {
    return Component.text().append(prefix).append(Component.text(text).color(TITLE_COLOR)).build();
  }

  public static Component chip(String text, NamedTextColor color) {
    return Component.text()
        .append(Component.text("[").color(PADDING_COLOR))
        .append(Component.text(text).color(color))
        .append(Component.text("] ").color(PADDING_COLOR))
        .build();
  }

  public static String date(Date date) {
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
    return dateFormat.format(new Date(date.getTime()));
  }

  // ChatGPT...
  public static Component truncate(Component component) {
    int maxWidth = 320;
    String ellipsis = " ...";
    int ellipsisWidth = getPixelWidth(ellipsis);
    int currentWidth = 0;
    List<Component> result = new ArrayList<>();

    for (Segment seg : flatten(component)) {
      String text = seg.text;
      Style style = seg.style;
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        int cw = CHAR_WIDTHS.getOrDefault(c, DEFAULT_WIDTH);
        if (currentWidth + cw > maxWidth - ellipsisWidth) {
          if (!builder.isEmpty()) result.add(Component.text(builder.toString(), style));
          result.add(Component.text(ellipsis, style));
          return Component.join(JoinConfiguration.noSeparators(), result)
              .hoverEvent(HoverEvent.showText(component));
        }

        builder.append(c);
        currentWidth += cw;
      }
      if (!builder.isEmpty()) result.add(Component.text(builder.toString(), style));
    }
    return Component.join(JoinConfiguration.noSeparators(), result);
  }

  private static List<Segment> flatten(Component comp) {
    List<Segment> segments = new ArrayList<>();
    if (comp instanceof TextComponent) {
      TextComponent tc = (TextComponent) comp;
      if (!tc.content().isEmpty()) {
        segments.add(new Segment(tc.content(), tc.style()));
      }
    }
    for (Component child : comp.children()) {
      segments.addAll(flatten(child));
    }
    return segments;
  }

  private static int getPixelWidth(String text) {
    int width = 0;
    for (char c : text.toCharArray()) {
      width += CHAR_WIDTHS.getOrDefault(c, DEFAULT_WIDTH);
    }
    return width;
  }

  public static Component getDisplayName(Action action) {
    String uuidString = action.getActor().getUUID();

    if (uuidString.equals("0")) {
      return Component.text("(server)").color(NAME_COLOR).decorate(TextDecoration.ITALIC);
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      return Component.text()
          .append(Component.text("("))
          .append(Component.text(uuidString))
          .append(Component.text(")"))
          .build()
          .color(NAME_COLOR)
          .decorate(TextDecoration.ITALIC);
    }

    Optional<User> user;
    try {
      user = Sponge.server().userManager().load(uuid).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    return user.map(value -> Component.text(value.name()).color(NAME_COLOR))
        .orElseGet(() -> Component.text(uuidString).color(NAME_COLOR));
  }

  public static List<Component> generateLookupContents(List<EspialRecord> records, boolean spread) {
    List<Component> contents = new ArrayList<>();

    if (spread) {
      records.forEach(
          record -> {
            Component displayName = getDisplayName(record.getAction());
            TextComponent.Builder msg = Component.text();

            String formattedDate = Format.date(record.getTimestamp());

            if (SHOW_DATE_IN_LOOKUP) {
              msg.append(Component.text(formattedDate).color(DATE_COLOR)).append(Component.space());
            }

            msg.append(displayName)
                .append(Component.space())
                .append(
                    makeHoverableAction(record.getAction().getEventType(), true)
                        .color(ACTION_COLOR))
                .append(Component.space())
                .append(getItemDisplayName(record).color(SPREAD_ITEM_COLOR))
                .clickEvent(ClickEvent.runCommand("/espial inspect " + record.getId()))
                .hoverEvent(
                    HoverEvent.showText(
                        component(
                            Component.text()
                                .append(Component.newline())
                                .append(
                                    Component.text("Click to teleport!").color(HOVER_HINT_COLOR))
                                .append(Component.newline())
                                .append(Component.text("Internal ID: ").color(HOVER_HINT_COLOR))
                                .append(Component.text(record.getId()).color(HOVER_TEXT_COLOR))
                                .append(Component.newline())
                                .append(Component.text("Item in hand: ").color(HOVER_HINT_COLOR))
                                .append(
                                    Component.text(record.getAction().getActor().getItem())
                                        .color(HOVER_TEXT_COLOR))
                                .append(Component.newline())
                                .append(Component.text(formattedDate).color(HOVER_TEXT_COLOR))
                                .build())));

            if (record.getAction() instanceof NBTStorable nbt) {
              nbt.getNBT()
                  .flatMap(NBTDataParser::parseNBT)
                  .ifPresent(
                      component -> {
                        msg.append(
                            Component.text(" (...)")
                                .color(NamedTextColor.GRAY)
                                .hoverEvent(
                                    HoverEvent.showText(
                                        component(
                                            Component.text()
                                                .color(NamedTextColor.WHITE)
                                                .append(component)))));
                      });
            }

            if (record.isRolledBack()) {
              msg.decorate(TextDecoration.STRIKETHROUGH);
            }
            contents.add(msg.build());
          });
    } else {
      // Grouped output in reverse chronological order
      Map<BlockTracker, Integer> groupedBlocks = new HashMap<>();
      Map<BlockTracker, Long> latestTimes = new HashMap<>();

      records.forEach(
          record -> {
            Component displayName = getDisplayName(record.getAction());

            BlockTracker key =
                new BlockTracker(
                    displayName,
                    record.getAction().getEventType(),
                    getItemDisplayName(record).color(ITEM_COLOR));
            groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
            long time = record.getTimestamp().getTime();
            latestTimes.put(key, Math.max(latestTimes.getOrDefault(key, 0L), time));
          });

      List<Map.Entry<BlockTracker, Integer>> sortedEntries =
          new ArrayList<>(groupedBlocks.entrySet());
      sortedEntries.sort(
          (e1, e2) -> Long.compare(latestTimes.get(e2.getKey()), latestTimes.get(e1.getKey())));

      sortedEntries.forEach(
          entry -> {
            BlockTracker key = entry.getKey();
            int count = entry.getValue();
            contents.add(
                Component.text()
                    .append(key.name())
                    .append(Component.space())
                    .append(
                        makeHoverableAction(entry.getKey().eventType(), true).color(ACTION_COLOR))
                    .append(Component.space())
                    .append(Component.text((count > 1 ? count + "x " : "")).color(STACK_COLOR))
                    .append(entry.getKey().block().color(ITEM_COLOR))
                    .build());
          });
    }
    return contents;
  }

  public static Component makeHoverableAction(EventType eventType, boolean useVerb) {
    String text = useVerb ? eventType.getVerb() : eventType.getName();
    return Component.text(text)
        .hoverEvent(
            HoverEvent.showText(
                component(
                    Component.text()
                        .append(Component.text("Event").color(TITLE_COLOR))
                        .append(Component.newline())
                        .append(Component.text("ID: ").color(HOVER_HINT_COLOR))
                        .append(Component.text(eventType.getId()).color(HOVER_TEXT_COLOR))
                        .append(Component.newline())
                        .append(Component.text("Name: ").color(HOVER_HINT_COLOR))
                        .append(Component.text(eventType.getName()).color(HOVER_TEXT_COLOR))
                        .append(Component.newline())
                        .append(Component.text("Description: ").color(HOVER_HINT_COLOR))
                        .append(
                            Component.text(eventType.getDescription()).color(HOVER_TEXT_COLOR)))));
  }

  public static Component getItemDisplayName(EspialRecord record) {
    Component displayName =
        Component.text("(unknown)").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);

    if (record.getAction() instanceof BlockAction blockAction) {
      displayName = blockAction.getBlockType().asComponent();
    } else if (record.getAction() instanceof HangingDeathAction hangingDeathAction) {
      displayName = hangingDeathAction.getEntityType().asComponent();
    } else if (record.getAction() instanceof ItemFrameRemoveAction itemFrameRemoveAction) {
      displayName = itemFrameRemoveAction.getItemType().asComponent();
    }

    return displayName;
  }

  private record BlockTracker(Component name, EventType eventType, Component block) {}

  private record Segment(String text, Style style) {}
}
