package com.myplex.myplex.events;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by Samir on 7/24/2014.
 */
public class ScopedBus {

    // See Otto's sample application for how BusProvider works. Any mechanism
    // for getting a singleton instance will work.
    private static ScopedBus _self;
    private final EventBus bus = EventBus.getDefault();
    private final Set<Object> objects = new HashSet<Object>();
    private boolean active = true;

    private ScopedBus() {

    }

    public static ScopedBus getInstance() {
        if (_self == null) {
            _self = new ScopedBus();
        }
        return _self;
    }

    public void register(Object obj) {
//        objects.add(obj);
        if (active && !bus.isRegistered(obj)) {
            bus.register(obj);
        }
    }

    public void unregister(Object obj) {
//        objects.remove(obj);
        if (active) {
            bus.unregister(obj);
        }
    }

    public void post(Object event) {
        bus.post(event);
    }

    public void paused() {
        active = false;
        for (Object obj : objects) {
            bus.unregister(obj);
        }
    }

    public void resumed() {
        active = true;
        for (Object obj : objects) {
                  bus.register(obj);
        }
    }

}
