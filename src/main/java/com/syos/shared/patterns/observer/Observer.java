package com.syos.shared.patterns.observer;

/**
 * Observer interface for the Observer pattern.
 * 
 * Classes that implement this interface can be notified
 * when specific events occur in the system.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface Observer {
    
    /**
     * Called when an event occurs that this observer is interested in.
     * 
     * @param event The event that occurred
     */
    void update(InventoryEvent event);
    
    /**
     * Get the observer's name for identification.
     * 
     * @return Observer name
     */
    String getObserverName();
}