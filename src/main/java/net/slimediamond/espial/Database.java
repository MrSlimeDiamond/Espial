package net.slimediamond.espial;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.ActionType;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.event.EspialPostInsertRecordEvent;
import net.slimediamond.espial.api.event.EspialPreInsertActionEvent;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.sponge.record.BlockRecordImpl;
import net.slimediamond.espial.sponge.record.EntityRecordImpl;
import net.slimediamond.espial.util.SpongeUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector3d;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Database {
  boolean logPlayerPosition;
  private Connection conn;

  private PreparedStatement insertAction;
  private PreparedStatement queryId;
  private PreparedStatement getBlockOwner;
  private PreparedStatement setRolledBack;
  private PreparedStatement insertNBTdata;
  private PreparedStatement getNBTdata;

  public Database() {
    this.logPlayerPosition = Espial.getInstance().getConfig().get().logPlayerPosition();
  }

  public void open(String connectionString) throws SQLException {
    Espial.getInstance().getLogger().info("Opening database...");
    conn = DriverManager.getConnection(connectionString);

    String sql;

    // Databases need to be made differently on different databases.
    if (connectionString.contains("sqlite")) {
      Espial.getInstance().getLogger().info("Detected database type: sqlite");
      sql = "CREATE TABLE IF NOT EXISTS blocklog (id INTEGER PRIMARY KEY AUTOINCREMENT, ";
    } else {
      // Probably MySQL/MariaDB or whatever. use a different statement

      Espial.getInstance().getLogger().info("Detected database type: MySQL/MariaDB");

      sql = "CREATE TABLE IF NOT EXISTS blocklog (id INT AUTO_INCREMENT PRIMARY KEY, ";
    }

    conn.prepareStatement(
            sql
                + "type TINYINT, "
                + "time TIMESTAMP, "
                + "player_uuid MEDIUMTEXT, "
                + "block_id TINYTEXT, "
                + "world TINYTEXT, "
                + "x INT, "
                + "y INT, "
                + "z INT, "
                + "player_x DOUBLE, "
                + "player_y DOUBLE, "
                + "player_z DOUBLE, "
                + "player_pitch DOUBLE, "
                + "player_yaw DOUBLE, "
                + "player_roll DOUBLE, "
                + "player_tool TINYTEXT, "
                + "rolled_back BOOLEAN NOT NULL DEFAULT FALSE"
                + ")")
        .execute();

    // Create the nbt table
    if (connectionString.contains("sqlite")) {
      sql = "CREATE TABLE IF NOT EXISTS nbt (id INTEGER PRIMARY KEY, data TEXT)";
    } else {
      sql = "CREATE TABLE IF NOT EXISTS nbt (id INT PRIMARY KEY, data TEXT)";
    }

    conn.prepareStatement(sql).execute();

    // Backwards compatibility
    // conn.prepareStatement("ALTER TABLE blocklog ADD COLUMN IF NOT EXISTS rolled_back BOOLEAN
    // DEFAULT FALSE").execute();

    insertAction =
        conn.prepareStatement(
            "INSERT INTO blocklog "
                + "(type, "
                + "time, "
                + "player_uuid, "
                + "block_id, "
                + "world, "
                + "x, y, z, "
                + "player_x, "
                + "player_y, "
                + "player_z, "
                + "player_pitch, "
                + "player_yaw, "
                + "player_roll, "
                + "player_tool, "
                + "rolled_back"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)",
            Statement.RETURN_GENERATED_KEYS);

    queryId = conn.prepareStatement("SELECT * FROM blocklog WHERE id = ?");
    getBlockOwner =
        conn.prepareStatement(
            "SELECT player_uuid FROM blocklog WHERE world = ? AND x = ? AND y = ? AND z = ? AND type = 1 ORDER BY time DESC LIMIT 1");
    setRolledBack = conn.prepareStatement("UPDATE blocklog SET rolled_back = ? WHERE id = ?");
    insertNBTdata = conn.prepareStatement("INSERT INTO nbt (id, data) VALUES (?, ?)");
    getNBTdata = conn.prepareStatement("SELECT data FROM nbt WHERE id = ?");

    Espial.getInstance().getLogger().info("Database loaded.");
  }

  /**
   * Submit a {@link Action} to be inserted into the database.
   *
   * @param action The action to submit
   * @return The record created
   * @throws SQLException If the database errors.
   * @throws JsonProcessingException If JSON errors.
   */
  public Optional<EspialRecord> submit(Action action) throws Exception {
    EspialPreInsertActionEvent event = new EspialPreInsertActionEvent(action.getActor(), action);
    Espial.getInstance()
        .getEspialService()
        .getEventManager()
        .callAll(event);

    if (event.isCancelled()) {
      return Optional.empty();
    }

    insertAction.setInt(1, action.getEventType().getId()); // Type
    insertAction.setTimestamp(2, Timestamp.from(Instant.now())); // Timestamp
    insertAction.setString(3, action.getActor().getUUID()); // Actor UUID

    // Block ID
    if (action instanceof BlockAction block) {
      insertAction.setString(4, block.getBlockId());
    } else if (action instanceof HangingDeathAction) {
      insertAction.setString(
          4,
          ((HangingDeathAction) action).getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted());
    } else if (action instanceof ItemFrameRemoveAction) {
      insertAction.setString(
          4,
          ((ItemFrameRemoveAction) action).getItemType().key(RegistryTypes.ITEM_TYPE).formatted());
    } else {
      insertAction.setString(4, ""); // apparently this can't be null?
    }

    insertAction.setString(5, action.getWorld()); // World
    insertAction.setInt(6, action.getX());
    insertAction.setInt(7, action.getY());
    insertAction.setInt(8, action.getZ());

    EspialActor actor = action.getActor();

    if (logPlayerPosition) {
      insertAction.setDouble(9, actor.getPosition().x());
      insertAction.setDouble(10, actor.getPosition().y());
      insertAction.setDouble(11, actor.getPosition().z());

      insertAction.setDouble(12, actor.getRotation().x());
      insertAction.setDouble(13, actor.getRotation().y());
      insertAction.setDouble(14, actor.getRotation().z());
    } else {
      insertAction.setNull(9, Types.DOUBLE);
      insertAction.setNull(10, Types.DOUBLE);
      insertAction.setNull(11, Types.DOUBLE);

      insertAction.setNull(12, Types.DOUBLE);
      insertAction.setNull(13, Types.DOUBLE);
      insertAction.setNull(14, Types.DOUBLE);
    }

    insertAction.setString(15, actor.getItem());

    insertAction.execute();

    ResultSet rs;

    try {
      rs = insertAction.getGeneratedKeys();
    } catch (SQLFeatureNotSupportedException e) {
      // Try to use another approach (this should work for sqlite)
      rs = conn.prepareStatement("SELECT last_insert_rowid()").executeQuery();
    }

    if (rs.next()) {
      int id = rs.getInt(1);

      if (action instanceof NBTStorable nbtStorable) {
        if (nbtStorable.getNBT().isPresent()) {
          this.setNBTdata(id, JsonNBTData.serialize(nbtStorable.getNBT().get()));
        }
      }

      EspialRecord record = this.queryId(id);

      Espial.getInstance()
          .getEspialService()
          .getEventManager()
          .callAll(new EspialPostInsertRecordEvent(actor, record));

      return Optional.of(record);
    } else {
      return Optional.empty();
    }
  }

  public List<EspialRecord> query(Query query) throws Exception {
    Timestamp timestamp =
        query.getTimestamp() == null
            ? Timestamp.from(Instant.ofEpochMilli(0))
            : query.getTimestamp();

    StringBuilder sql = new StringBuilder("SELECT * FROM blocklog WHERE world" +
            " = ? AND time > ?");

    if (query.getMax() == null) {
      sql.append(" AND x = ? AND y = ? AND z = ? ");
    } else {
      sql.append(" AND x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN " +
              "? and ? ");
    }

    List<UUID> uuids = query.getPlayerUUIDs();
    if (uuids != null && !uuids.isEmpty()) {
      List<String> players = new ArrayList<>();
      uuids.forEach(uuid -> players.add("\"" + uuid.toString() + "\""));
      sql.append(" AND player_uuid IN (").append(String.join(", ", players)).append(")");
    }

    List<String> blocks = query.getBlockIds();
    if (blocks != null && !blocks.isEmpty()) {
      List<String> quoted = new ArrayList<>();
      blocks.forEach(block -> quoted.add("\"" + block + "\""));
      sql.append(" AND block_id IN (").append(String.join(", ", quoted)).append(")");
    }

    List<EspialRecord> actions = new ArrayList<>();
    ResultSet rs;

    PreparedStatement statement = conn.prepareStatement(sql.toString());
    statement.setString(1, query.getMin().worldKey().formatted());
    statement.setTimestamp(2, timestamp);

    if (query.getMax() == null) {
      // single. Query coords
      statement.setInt(3, query.getMin().blockX());
      statement.setInt(4, query.getMin().blockY());
      statement.setInt(5, query.getMin().blockZ());

      rs = statement.executeQuery();
    } else {
      // Ranged lookup

      // Rearrange from smallest to biggest for things to actually get picked up
      int[] x = {query.getMin().blockX(), query.getMax().blockX()};
      int[] y = {query.getMin().blockY(), query.getMax().blockY()};
      int[] z = {query.getMin().blockZ(), query.getMax().blockZ()};

      // The smallest number would be at the start.
      Arrays.sort(x);
      Arrays.sort(y);
      Arrays.sort(z);


      statement.setInt(3, x[0]);
      statement.setInt(4, x[1]);

      statement.setInt(5, y[0]);
      statement.setInt(6, y[1]);

      statement.setInt(7, z[0]);
      statement.setInt(8, z[1]);

      rs = statement.executeQuery();
    }

    while (rs.next()) {
      actions.add(blockFromRs(rs));
    }

    if (query.getSort() == Sort.ID_ASCENDING) {
      actions.sort(Comparator.comparing(EspialRecord::getId));
    } else if (query.getSort() == Sort.ID_DESCENDING) {
      actions.sort(Comparator.comparing(EspialRecord::getId).reversed());
    } else if (query.getSort() == Sort.REVERSE_CHRONOLOGICAL) {
      actions.sort(Comparator.comparing(EspialRecord::getTimestamp).reversed());
    } else if (query.getSort() == Sort.CHRONOLOGICAL) {
      actions.sort(Comparator.comparing(EspialRecord::getTimestamp));
    }

    return actions;
  }

  public EspialRecord queryId(int id) throws Exception {
    queryId.setInt(1, id);

    ResultSet rs = queryId.executeQuery();

    if (rs.next()) {
      return this.blockFromRs(rs);
    }
    return null;
  }

  public Optional<User> getBlockOwner(String world, int x, int y, int z)
      throws SQLException, ExecutionException, InterruptedException {
    getBlockOwner.setString(1, world);
    getBlockOwner.setInt(2, x);
    getBlockOwner.setInt(3, y);
    getBlockOwner.setInt(4, z);

    ResultSet rs = getBlockOwner.executeQuery();
    if (rs.next()) {
      String playerUUID = rs.getString("player_uuid");
      try {
        UUID uuid = UUID.fromString(playerUUID);
        return Sponge.server().userManager().load(uuid).get();
      } catch (IllegalArgumentException e) {
        // likely to be an animal or something
        // TODO: Make some block owner object, then we can derive a name nicer
        return Optional.empty();
      }

    } else {
      return Optional.empty();
    }
  }

  private EspialRecord blockFromRs(ResultSet rs) throws Exception {
    int uid = rs.getInt("id");
    int type = rs.getInt("type");
    Timestamp timestamp = rs.getTimestamp("time");
    String playerUUID = rs.getString("player_uuid");
    String blockId = rs.getString("block_id");
    String world = rs.getString("world");
    String itemInHand = rs.getString("player_tool");

    int x = rs.getInt("x");
    int y = rs.getInt("y");
    int z = rs.getInt("z");

    double playerX = 0;
    double playerY = 0;
    double playerZ = 0;

    double playerPitch = 0;
    double playerYaw = 0;
    double playerRoll = 0;

    if (!playerUUID.equals("0")) {
      playerX = rs.getDouble("player_x");
      playerY = rs.getDouble("player_y");
      playerZ = rs.getDouble("player_z");

      playerPitch = rs.getDouble("player_pitch");
      playerYaw = rs.getDouble("player_yaw");
      playerRoll = rs.getDouble("player_roll");
    }

    double finalPlayerX = playerX;
    double finalPlayerY = playerY;
    double finalPlayerZ = playerZ;

    double finalPlayerRoll = playerRoll;
    double finalPlayerYaw = playerYaw;
    double finalPlayerPitch = playerPitch;

    boolean rolledBack = rs.getBoolean("rolled_back");

    UUID uuid;
    try {
      uuid = UUID.fromString(playerUUID);
    } catch (IllegalArgumentException e) {
      uuid = null;
    }

    boolean isPlayer = uuid != null;

    EspialActor actor =
        new EspialActor() {
          @Override
          public String getUUID() {
            return playerUUID;
          }

          @Override
          public boolean isPlayer() {
            return isPlayer;
          }

          @Override
          public @Nullable Vector3d getPosition() {
            if (logPlayerPosition) {
              return new Vector3d(finalPlayerX, finalPlayerY, finalPlayerZ);
            } else {
              return null;
            }
          }

          @Override
          public @Nullable Vector3d getRotation() {
            if (logPlayerPosition) {
              return new Vector3d(finalPlayerPitch, finalPlayerYaw, finalPlayerRoll);
            } else {
              return null;
            }
          }

          @Override
          public String getItem() {
            return itemInHand;
          }
        };

    EventType eventType = EventTypes.fromId(type);

    Action action;

    if (eventType != null) {
      if (eventType.getActionType().equals(ActionType.BLOCK)) {
        action =
            BlockAction.builder()
                .blockId(blockId)
                .actor(actor)
                .event(eventType)
                .world(world)
                .x(x)
                .y(y)
                .z(z)
                .withNBTData(getNBTdata(uid).orElse(null))
                .build();

        // Make a new BlockRecord
        return new BlockRecordImpl(uid, timestamp, rolledBack, action);
      }
      if (eventType.getActionType().equals(ActionType.HANGING_DEATH)) {
        EntityType<?> entityType = EntityTypes.registry().value(SpongeUtil.getResourceKey(blockId));

        action =
            HangingDeathAction.builder()
                .actor(actor)
                .entity(entityType)
                .event(eventType)
                .world(world)
                .x(x)
                .y(y)
                .z(z)
                .withNBTData(getNBTdata(uid).orElse(null))
                .build();

        return new EntityRecordImpl(uid, timestamp, rolledBack, action);
      } else if (eventType.getActionType().equals(ActionType.ITEM_FRAME_REMOVE)) {
        ItemType itemType = ItemTypes.registry().value(SpongeUtil.getResourceKey(blockId));

        action =
            ItemFrameRemoveAction.builder()
                .itemType(itemType)
                .actor(actor)
                .event(eventType)
                .world(world)
                .x(x)
                .y(y)
                .z(z)
                .build();

        return new EntityRecordImpl(uid, timestamp, rolledBack, action);
      } else {
        throw new Exception("Unsupported event type");
      }
    } else {
      throw new Exception("Event type cannot be null");
    }
  }

  public void setRolledBack(int id, boolean status) throws SQLException {
    setRolledBack.setBoolean(1, status);
    setRolledBack.setInt(2, id);
    setRolledBack.execute();
  }

  public void setNBTdata(int id, String data) throws SQLException {
    insertNBTdata.setInt(1, id);
    insertNBTdata.setString(2, data);
    insertNBTdata.execute();
  }

  public Optional<NBTData> getNBTdata(int id) throws SQLException, JsonProcessingException {
    getNBTdata.setInt(1, id);
    ResultSet rs = getNBTdata.executeQuery();

    if (rs.next()) {
      return Optional.of(JsonNBTData.deserialize(rs.getString("data")));
    }

    return Optional.empty();
  }
}
