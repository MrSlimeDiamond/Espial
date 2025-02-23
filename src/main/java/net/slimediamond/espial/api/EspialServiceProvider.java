package net.slimediamond.espial.api;

/**
 * An {@link EspialService} provider
 *
 * Usage:
 * <code>
 *     EspialService espialService = EspialServiceProvider.getEspialService();
 *
 *     // ... use EspialService ...
 * </code>
 */
public final class EspialServiceProvider {
    private static EspialService espialService;

    public static EspialService getEspialService() {
        return espialService;
    }

    public static void setEspialService(EspialService espialService) {
        EspialServiceProvider.espialService = espialService;
    }
}
