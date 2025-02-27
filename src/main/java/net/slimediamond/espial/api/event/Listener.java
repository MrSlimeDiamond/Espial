package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.event.listener.EspialEventListener;

import java.lang.reflect.Method;

public class Listener {
    private Object clazz;
    private EspialEventListener eventListener;
    private Method method;

    public Listener(Object clazz, EspialEventListener eventListener, Method method) {
        this.clazz = clazz;
        this.eventListener = eventListener;
        this.method = method;
    }

    public Object getClazz() {
        return clazz;
    }

    public EspialEventListener getEventListener() {
        return eventListener;
    }

    public Method getMethod() {
        return method;
    }
}
