package net.slimediamond.espial.sponge.event;

import net.slimediamond.espial.api.event.AbstractEvent;
import net.slimediamond.espial.api.event.EventManager;
import net.slimediamond.espial.api.event.Listener;
import net.slimediamond.espial.api.event.listener.EspialEventListener;

import java.util.*;

public class EventManagerImpl implements EventManager {
  public Map<Class<? extends AbstractEvent>, Listener> listeners = new LinkedHashMap<>();

  @Override
  public void callAll(AbstractEvent event) {
    for (Map.Entry<Class<? extends AbstractEvent>, Listener> entry : listeners.entrySet()) {
      Class<? extends AbstractEvent> eventClass = entry.getKey();
      Listener listener = entry.getValue();
      if (eventClass.isAssignableFrom(event.getClass())) {
        try {
          listener.getMethod().invoke(listener.getClazz(), event);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Override
  public void register(Object listener) {
    Arrays.stream(listener.getClass().getMethods())
        .filter(method -> method.isAnnotationPresent(EspialEventListener.class))
        .forEach(
            method -> {
              // Is a valid event listener
              Class<?> hookClass = method.getParameterTypes()[0];
              register(
                  hookClass,
                  new Listener(listener, method.getAnnotation(EspialEventListener.class), method));
            });
  }

  private void register(Class<?> hookClass, Listener listener) {
    listeners.put((Class<? extends AbstractEvent>) hookClass, listener);
  }
}
