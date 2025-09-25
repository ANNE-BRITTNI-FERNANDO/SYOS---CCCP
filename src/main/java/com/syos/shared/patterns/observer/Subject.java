package com.syos.shared.patterns.observer;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Subject interface and implementation for the Observer pattern.
 * 
 * Manages observers and notifies them of inventory events.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface Subject {
    
    /**
     * Add an observer to be notified of events.
     */
    void addObserver(Observer observer);
    
    /**
     * Remove an observer from notifications.
     */
    void removeObserver(Observer observer);
    
    /**
     * Notify all observers of an event.
     */
    void notifyObservers(InventoryEvent event);
}

/**
 * Implementation of Subject for inventory events.
 */
class InventoryEventPublisher implements Subject {
    
    private static final Logger LOGGER = Logger.getLogger(InventoryEventPublisher.class.getName());
    
    // Thread-safe list for observers
    private final List<Observer> observers = new CopyOnWriteArrayList<>();
    
    @Override
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            LOGGER.info("Observer added: " + observer.getObserverName());
        }
    }
    
    @Override
    public void removeObserver(Observer observer) {
        if (observers.remove(observer)) {
            LOGGER.info("Observer removed: " + observer.getObserverName());
        }
    }
    
    @Override
    public void notifyObservers(InventoryEvent event) {
        if (event == null) {
            return;
        }
        
        LOGGER.info("Notifying " + observers.size() + " observers of event: " + event.getEventType());
        
        for (Observer observer : observers) {
            try {
                observer.update(event);
            } catch (Exception e) {
                LOGGER.warning("Error notifying observer " + observer.getObserverName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Get list of registered observers
     */
    public List<Observer> getObservers() {
        return new ArrayList<>(observers);
    }
    
    /**
     * Get observer count
     */
    public int getObserverCount() {
        return observers.size();
    }
}