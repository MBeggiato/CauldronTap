package com.cauldrontap;

import java.util.Optional;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

/** Chest placement relative to a dispenser's facing. */
public enum ChestOutputPosition {

  /** Opposite the direction the dispenser faces. */
  BACK("back"),

  /** Same direction the dispenser faces. */
  FRONT("front"),

  /** Below the dispenser. */
  DOWN("down"),

  /** Above the dispenser. */
  UP("up"),

  /** To the left of the dispenser (relative to its facing). */
  LEFT("left"),

  /** To the right of the dispenser (relative to its facing). */
  RIGHT("right");

  private final String configKey;

  ChestOutputPosition(String configKey) {
    this.configKey = configKey;
  }

  /**
   * Parses a config value.
   *
   * @param value raw config string
   * @return matching position, or empty if unknown
   */
  public static Optional<ChestOutputPosition> parse(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String normalized = value.trim().toLowerCase();
    for (ChestOutputPosition position : values()) {
      if (position.configKey.equals(normalized)) {
        return Optional.of(position);
      }
    }
    return Optional.empty();
  }

  /**
   * Resolves this position to a block face offset from the dispenser block.
   *
   * @param directional dispenser block data
   * @return block face to {@link org.bukkit.block.Block#getRelative(BlockFace)}
   */
  public BlockFace toBlockFace(Directional directional) {
    BlockFace facing = directional.getFacing();
    return switch (this) {
      case BACK -> facing.getOppositeFace();
      case FRONT -> facing;
      case DOWN -> BlockFace.DOWN;
      case UP -> BlockFace.UP;
      case LEFT -> horizontalLeft(facing);
      case RIGHT -> horizontalRight(facing);
    };
  }

  private static BlockFace horizontalLeft(BlockFace facing) {
    return switch (facing) {
      case NORTH -> BlockFace.WEST;
      case SOUTH -> BlockFace.EAST;
      case EAST -> BlockFace.NORTH;
      case WEST -> BlockFace.SOUTH;
      case UP -> BlockFace.WEST;
      case DOWN -> BlockFace.WEST;
      default -> facing;
    };
  }

  private static BlockFace horizontalRight(BlockFace facing) {
    return switch (facing) {
      case NORTH -> BlockFace.EAST;
      case SOUTH -> BlockFace.WEST;
      case EAST -> BlockFace.SOUTH;
      case WEST -> BlockFace.NORTH;
      case UP -> BlockFace.EAST;
      case DOWN -> BlockFace.EAST;
      default -> facing;
    };
  }
}
