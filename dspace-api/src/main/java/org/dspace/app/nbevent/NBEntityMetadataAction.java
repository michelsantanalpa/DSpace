/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.nbevent;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.nbevent.service.dto.MessageDto;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class NBEntityMetadataAction implements NBAction {
    private String relation;
    private String entityType;
    private Map<String, String> entityMetadata;

    @Autowired
    private InstallItemService installItemService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private WorkspaceItemService workspaceItemService;

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String[] splitMetadata(String metadata) {
        String[] result = new String[3];
        String[] split = metadata.split("\\.");
        result[0] = split[0];
        result[1] = split[1];
        if (split.length == 3) {
            result[2] = split[2];
        }
        return result;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Map<String, String> getEntityMetadata() {
        return entityMetadata;
    }

    public void setEntityMetadata(Map<String, String> entityMetadata) {
        this.entityMetadata = entityMetadata;
    }

    @Override
    public void applyCorrection(Context context, Item item, Item relatedItem, MessageDto message) {
        try {
            if (relatedItem != null) {
                link(context, item, relatedItem);
            } else {
                Collection collection = item.getOwningCollection();
                WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
                relatedItem = workspaceItem.getItem();
                if (StringUtils.isNotBlank(entityType)) {
                    itemService.addMetadata(context, relatedItem, "relationship", "type", null, null, entityType);
                }
                for (String key : entityMetadata.keySet()) {
                    String value = getValue(message, key);
                    if (StringUtils.isNotBlank(value)) {
                        String[] targetMetadata = splitMetadata(entityMetadata.get(key));
                        itemService.addMetadata(context, relatedItem, targetMetadata[0], targetMetadata[1],
                                targetMetadata[2], null, value);
                    }
                }
                installItemService.installItem(context, workspaceItem);
                itemService.update(context, relatedItem);
                link(context, item, relatedItem);
            }
        } catch (SQLException | AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }

    private void link(Context context, Item item, Item relatedItem) throws SQLException, AuthorizeException {
        EntityType project = entityTypeService.findByEntityType(context, entityType);
        RelationshipType relType = relationshipTypeService.findByEntityType(context, project).stream()
                .filter(r -> StringUtils.equals(r.getRightwardType(), relation)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No relationshipType named " + relation
                        + " was found for the entity type " + entityType
                        + ". A proper configuration is required to use the NBEntitiyMetadataAction."
                        + " If you don't manage funding in your repository please skip this topic in"
                        + " the oaire-nbevents.cfg"));
        // Create the relationship
        int leftPlace = relationshipService.findNextLeftPlaceByLeftItem(context, item);
        int rightPlace = relationshipService.findNextRightPlaceByRightItem(context, relatedItem);
        Relationship persistedRelationship = relationshipService.create(context, item, relatedItem,
                                                                        relType, leftPlace, rightPlace);
        relationshipService.update(context, persistedRelationship);
    }

    private String getValue(MessageDto message, String key) {
        if (StringUtils.equals(key, "acronym")) {
            return message.getAcronym();
        } else if (StringUtils.equals(key, "code")) {
            return message.getCode();
        } else if (StringUtils.equals(key, "funder")) {
            return message.getFunder();
        } else if (StringUtils.equals(key, "fundingProgram")) {
            return message.getFundingProgram();
        } else if (StringUtils.equals(key, "jurisdiction")) {
            return message.getJurisdiction();
        } else if (StringUtils.equals(key, "openaireId")) {
            return message.getOpenaireId();
        } else if (StringUtils.equals(key, "title")) {
            return message.getTitle();
        }
        return null;
    }
}
