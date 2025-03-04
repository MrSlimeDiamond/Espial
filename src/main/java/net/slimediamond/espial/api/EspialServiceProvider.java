package net.slimediamond.espial.api;

/**
 * An {@link EspialService} provider
 *
 * <p>Usage: <code>
 *     EspialService espialService = EspialServiceProvider.getEspialService();
 *
 *     // ... use EspialService ...
 * </code>
 *
 * @author SlimeDiamond
 */
public final class EspialServiceProvider {
  private static EspialService espialService;

  /**
   * Get the {@link EspialService}
   * @return Espial Service
   */
  public static EspialService getEspialService() {
    return espialService;
  }

  /**
   * Set the Espial Service (dangerous!)
   *
   * @param espialService The service to set it to
   */
  public static void setEspialService(EspialService espialService) {
    EspialServiceProvider.espialService = espialService;
  }
}
