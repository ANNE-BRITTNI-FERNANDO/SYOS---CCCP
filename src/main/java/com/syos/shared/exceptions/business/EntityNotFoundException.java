package com.syos.shared.exceptions.business;

/**
 * Exception thrown when a requested entity is not found in the system.
 * 
 * This exception represents a business rule violation where an expected
 * entity does not exist in the data store.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class EntityNotFoundException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    private final String entityType;
    private final Object entityId;
    
    /**
     * Constructs a new entity not found exception.
     * 
     * @param entityType the type of entity that was not found
     * @param entityId the ID of the entity that was not found
     */
    public EntityNotFoundException(String entityType, Object entityId) {
        super(
            String.format("Entity of type '%s' with ID '%s' was not found", entityType, entityId),
            String.format("%s not found", entityType)
        );
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    /**
     * Constructs a new entity not found exception with custom message.
     * 
     * @param entityType the type of entity that was not found
     * @param entityId the ID of the entity that was not found
     * @param customMessage custom user-friendly message
     */
    public EntityNotFoundException(String entityType, Object entityId, String customMessage) {
        super(
            String.format("Entity of type '%s' with ID '%s' was not found", entityType, entityId),
            customMessage
        );
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    /**
     * Gets the type of entity that was not found.
     * 
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }
    
    /**
     * Gets the ID of the entity that was not found.
     * 
     * @return the entity ID
     */
    public Object getEntityId() {
        return entityId;
    }
}