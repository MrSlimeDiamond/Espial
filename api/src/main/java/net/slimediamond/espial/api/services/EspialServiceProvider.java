package net.slimediamond.espial.api.services;

import org.jetbrains.annotations.NotNull;

public final class EspialServiceProvider {

    private EspialServiceProvider() {
    }

    private static EspialService espialService;

    public static void offer(@NotNull final EspialService service) {
        assert espialService != null : "EspialService already provided";
        espialService = service;
    }

    public static EspialService get() {
        return espialService;
    }

}
