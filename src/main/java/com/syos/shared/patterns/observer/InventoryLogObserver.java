package com.syos.shared.patterns.observer;

import java.util.logging.Logger;

/**
 * Observer that logs inventory events to system log.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class InventoryLogObserver implements Observer {
    
    private static final Logger LOGGER = Logger.getLogger(InventoryLogObserver.class.getName());
    
    @Override
    public void update(InventoryEvent event) {
        String logMessage = String.format("[%s] %s: %s", 
            event.getTimestamp(), 
            event.getEventType(), 
            event.getDescription());
        
        switch (event.getEventType()) {
            case STOCK_OUT:
            case BATCH_EXPIRED:
                LOGGER.warning(logMessage);
                break;
            case STOCK_LOW:
            case BATCH_NEAR_EXPIRY:
                LOGGER.info("ALERT: " + logMessage);
                break;
            default:
                LOGGER.info(logMessage);
        }
    }
    
    @Override
    public String getObserverName() {
        return "Inventory Log Observer";
    }
}