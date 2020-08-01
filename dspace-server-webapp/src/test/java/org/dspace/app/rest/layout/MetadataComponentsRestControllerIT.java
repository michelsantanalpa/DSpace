/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.layout;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import org.dspace.app.rest.builder.CrisLayoutBoxBuilder;
import org.dspace.app.rest.builder.CrisLayoutFieldBuilder;
import org.dspace.app.rest.builder.EntityTypeBuilder;
import org.dspace.app.rest.model.patch.AddOperation;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.RemoveOperation;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.EntityType;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutField;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This test class verify the REST Services for the Layout Metadata Component functionality
 * (endpoint /api/layout/metadatacomponents/<:string>)
 * 
 * @author Danilo Di Nuzzo (danilo dot dinuzzo at 4science dot it)
 *
 */
public class MetadataComponentsRestControllerIT extends AbstractControllerIntegrationTest {

    @Autowired
    private MetadataSchemaService mdss;

    @Autowired
    private MetadataFieldService mfss;

    @Test
    public void getMetadataComponent() throws Exception {
        context.turnOffAuthorisationSystem();
        // Create entity type Publication
        EntityType eType = EntityTypeBuilder.createEntityTypeBuilder(context, "Publication").build();
        // get metadata field
        MetadataSchema schema = mdss.find(context, "dc");
        MetadataField isbn = mfss.findByElement(context, schema, "identifier", "isbn");
        MetadataField uri = mfss.findByElement(context, schema, "identifier", "uri");
        MetadataField abs = mfss.findByElement(context, schema, "description", "abstract");
        MetadataField provenance = mfss.findByElement(context, schema, "description", "provenance");
        MetadataField sponsorship = mfss.findByElement(context, schema, "description", "sponsorship");
        MetadataField extent = mfss.findByElement(context, schema, "format", "extent");
        // Create boxes
        CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                .withShortname("box-shortname-one")
                .build();
        CrisLayoutField fieldIsbn = CrisLayoutFieldBuilder.createField(context, isbn, 0, 0)
                .withBundle("BUNDLE ISBN")
                .withLabel("LABEL ISBN")
                .withRendering("RENDERIGN ISBN")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutField fieldUri = CrisLayoutFieldBuilder.createField(context, uri, 0, 1)
                .withBundle("BUNDLE URI")
                .withLabel("LABEL URI")
                .withRendering("RENDERIGN URI")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutField fieldAbs = CrisLayoutFieldBuilder.createField(context, abs, 1, 0)
                .withBundle("BUNDLE ABS")
                .withLabel("LABEL ABS")
                .withRendering("RENDERIGN ABS")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutField fieldProvenance = CrisLayoutFieldBuilder.createField(context, provenance, 1, 1)
                .withBundle("BUNDLE PROVENANCE")
                .withLabel("LABEL PROVENANCE")
                .withRendering("RENDERIGN PROVENANCE")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutField fieldSponsorship = CrisLayoutFieldBuilder.createField(context, provenance, 1, 2)
                .withBundle("BUNDLE SPRONSORSHIP")
                .withLabel("LABEL SPRONSORSHIP")
                .withRendering("RENDERIGN SPRONSORSHIP")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutField fieldExtent = CrisLayoutFieldBuilder.createField(context, extent, 2, 0)
                .withBundle("BUNDLE EXTENT")
                .withLabel("LABEL EXTENT")
                .withRendering("RENDERIGN EXTENT")
                .withStyle("STYLE")
                .withType("TYPE")
                .build();
        CrisLayoutBox box = CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                .withShortname("box-shortname-two")
                .addField(fieldIsbn)
                .addField(fieldUri)
                .addField(fieldAbs)
                .addField(fieldProvenance)
                .addField(fieldSponsorship)
                .addField(fieldExtent)
                .addMetadataSecurityField(isbn)
                .addMetadataSecurityField(uri)
                .addMetadataSecurityField(abs)
                .addMetadataSecurityField(provenance)
                .addMetadataSecurityField(sponsorship)
                .addMetadataSecurityField(extent)
                .build();
        CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                .withShortname("box-shortname-three")
                .build();
        context.restoreAuthSystemState();
        // Test WS endpoint
        getClient().perform(get("/api/layout/metadatacomponents/" + box.getShortname()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.id", Matchers.is(box.getShortname())))
            .andExpect(jsonPath("$.rows.length()", Matchers.is(3)))
            .andExpect(jsonPath("$.rows[0].fields.length()", Matchers.is(2)))
            .andExpect(jsonPath("$.rows[1].fields.length()", Matchers.is(3)))
            .andExpect(jsonPath("$.rows[2].fields.length()", Matchers.is(1)));
    }

    @Test
    public void patchAddMetadataTest() throws Exception {
        context.turnOffAuthorisationSystem();
        EntityType eType = EntityTypeBuilder.createEntityTypeBuilder(context, "Person").build();

        CrisLayoutBox box = CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                                                .withShortname("box-shortname-test")
                                                .build();

        context.restoreAuthSystemState();
        String authToken = getAuthToken(admin.getEmail(), password);

        List<Operation> operations = new ArrayList<Operation>();
        List<Map<String, String>> metadataValues = new ArrayList<Map<String, String>>();
        Map<String, String> values = new HashMap<String, String>();
        values.put("metadata", "dc.contibutor.author");
        values.put("label", "Author");
        values.put("fieldType", "metadata");
        metadataValues.add(values);
        operations.add(new AddOperation("/rows/0/fields/0", metadataValues));

        String patchBody = getPatchContent(operations);
        getClient(authToken).perform(patch("/api/layout/metadatacomponents/box-shortname-test")
                .content(patchBody)
                .contentType(MediaType.APPLICATION_JSON_PATCH_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].fields.length()", Matchers.is(1)))
                .andExpect(jsonPath("$", Matchers.allOf(
                        hasJsonPath("$.id", is("box-shortname-test")),
                        hasJsonPath("$.type", is("metadatacomponent")),
                        hasJsonPath("$.rows[0].fields[0].metadata", is("dc.contibutor.author")),
                        hasJsonPath("$.rows[0].fields[0].label", is("Author")),
                        hasJsonPath("$.rows[0].fields[0].fieldType", is("metadata"))
                       )));

    }

    @Test
    public void patchRemoveMetadataTest() throws Exception {
        context.turnOffAuthorisationSystem();

        EntityType eType = EntityTypeBuilder.createEntityTypeBuilder(context, "Person").build();

        MetadataSchema schema = mdss.find(context, "dc");
        MetadataField contributor = mfss.findByElement(context, schema, "contributor", "author");

        CrisLayoutField fieldContributor = CrisLayoutFieldBuilder.createField(context, contributor, 0, 0)
                                                                 .withLabel("Author")
                                                                 .withRendering("")
                                                                 .withStyle("STYLE")
                                                                 .withType("metadata")
                                                                 .build();

        CrisLayoutBox box = CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                .withShortname("box-shortname-test")
                .addField(fieldContributor)
                .build();

        context.restoreAuthSystemState();
        String authToken = getAuthToken(admin.getEmail(), password);

        List<Operation> operations = new ArrayList<Operation>();
        operations.add(new RemoveOperation("/rows/0/fields/0"));

        String patchBody = getPatchContent(operations);
        getClient(authToken).perform(patch("/api/layout/metadatacomponents/mybox")
                .content(patchBody)
                .contentType(MediaType.APPLICATION_JSON_PATCH_JSON))
                            .andExpect(status().isNoContent());

        getClient(authToken).perform(get("/api/layout/metadatacomponents/" + box.getShortname()))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$.id", Matchers.is(box.getShortname())))
                            .andExpect(jsonPath("$.rows.length()", Matchers.is(3)))
                            .andExpect(jsonPath("$.rows[0].fields.length()", Matchers.is(0)));
    }

    @Test
    public void patchAddMetadataNotExistTest() throws Exception {
        context.turnOffAuthorisationSystem();

        EntityType eType = EntityTypeBuilder.createEntityTypeBuilder(context, "Person").build();

        CrisLayoutBox box = CrisLayoutBoxBuilder.createBuilder(context, eType, true, 0, true)
                                                .withShortname("box-shortname-test")
                                                .build();

        context.restoreAuthSystemState();
        String authToken = getAuthToken(admin.getEmail(), password);

        List<Operation> operations = new ArrayList<Operation>();
        List<Map<String, String>> metadataValues = new ArrayList<Map<String, String>>();
        Map<String, String> values = new HashMap<String, String>();
        values.put("metadata", "dc.not.exist");
        values.put("label", "wrong metadata");
        values.put("fieldType", "metadata");
        metadataValues.add(values);
        operations.add(new AddOperation("/rows/0/fields/0", metadataValues));

        String patchBody = getPatchContent(operations);
        getClient(authToken).perform(patch("/api/layout/metadatacomponents/mybox")
                .content(patchBody)
                .contentType(MediaType.APPLICATION_JSON_PATCH_JSON))
                            .andExpect(status().isUnprocessableEntity());

    }
}