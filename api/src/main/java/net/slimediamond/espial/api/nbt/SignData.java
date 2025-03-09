package net.slimediamond.espial.api.nbt;

import java.util.List;

/**
 * The data stored on a sign
 *
 * @author SlimeDiamond
 */
public interface SignData {
  /**
   * The front text of a sign, stored as serialized
   * {@link net.kyori.adventure.text.Component}s
   *
   * @return Sign front text
   */
  List<String> getFrontText();

  /**
   * The back text of a sign, stored as serialized
   * {@link net.kyori.adventure.text.Component}s
   *
   * @return Sign back text
   */
  List<String> getBackText();
}
