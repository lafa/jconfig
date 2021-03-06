/**
 * 
 */
package org.commons.jconfig.serializers;

import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeData;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author jaikit
 * 
 */
public class CompositeDataExtractor implements Extractor {

    /*
     * (non-Javadoc)
     * 
     * @see
     * common.config.serializers.Extractor#extractObject(com.yahoo
     * common.config.serializers.ObjectToJsonConverter, java.lang.Object)
     */
    @Override
    public JsonElement extractObject(ObjectToJsonConverter pConverter, Object pValue) throws AttributeNotFoundException {
        CompositeData cd = (CompositeData) pValue;

        JsonObject ret = new JsonObject();
        for (String key : cd.getCompositeType().keySet()) {
            ret.add(key, pConverter.extractObject(cd.get(key)));
        }
        return ret;
    }

}
