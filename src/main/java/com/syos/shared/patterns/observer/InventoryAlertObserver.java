package com.syos.shared.patterns.observer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Observer that collects inventory alerts for display in UI.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class InventoryAlertObserver implements Observer {
    
    private final Queue<InventoryEvent> alertEvents = new ConcurrentLinkedQueue<>();
    private final int maxAlerts;
    
    public InventoryAlertObserver(int maxAlerts) {
        this.maxAlerts = maxAlerts;
    }
    
    public InventoryAlertObserver() {
        this(50); // Default max 50 alerts
    }
    
    @Override
    public void update(InventoryEvent event) {
        // Only store alert-worthy events
        if (isAlertEvent(event)) {
            addAlert(event);
        }
    }
    
    @Override
    public String getObserverName() {
        return "Inventory Alert Observer";
    }
    
    /**
     * Add alert event and maintain max limit
     */
    private void addAlert(InventoryEvent event) {
        alertEvents.offer(event);
        
        // Remove oldest alerts if we exceed max
        while (alertEvents.size() > maxAlerts) {
            alertEvents.poll();
        }
    }
    
    /**
     * Check if event is alert-worthy
     */
    private boolean isAlertEvent(InventoryEvent event) {
        switch (event.getEventType()) {
            case STOCK_LOW:
            case STOCK_OUT:
            case BATCH_EXPIRED:
            case BATCH_NEAR_EXPIRY:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get all current alerts
     */
    public Queue<InventoryEvent> getAlerts() {
        return new ConcurrentLinkedQueue<>(alertEvents);
    }
    
    /**
     * Get alert count
     */
    public int getAlertCount() {
        return alertEvents.size();
    }
    
    /**
     * Clear all alerts
     */
    public void clearAlerts() {
        alertEvents.clear();
    }
    
    /**
     * Get latest alerts (most recent first)
     */
    public String[] getLatestAlerts(int count) {
        InventoryEvent[] events = alertEvents.toArray(new InventoryEvent[0]);
        int actualCount = Math.min(count, events.length);
        String[] alerts = new String[actualCount];
        
        // Get most recent alerts (from end of array)
        for (int i = 0; i < actualCount; i++) {
            alerts[i] = events[events.length - actualCount + i].getDescription();
        }
        
        return alerts;
    }
}