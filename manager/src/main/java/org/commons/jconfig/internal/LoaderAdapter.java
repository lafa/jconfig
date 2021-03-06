/**
 * 
 */
package org.commons.jconfig.internal;

import java.lang.reflect.Method;

import org.commons.jconfig.annotations.ConfigLoaderAdapter;
import org.commons.jconfig.config.ConfigLoaderAdapterID;


/**
 * @author jaikit
 *
 */
public class LoaderAdapter {
    private final Class<?> configClazz;
    private final ConfigLoaderAdapterID adapterId;
    /**
     * @param resource
     */
    public LoaderAdapter(Class<?> resource) {
       configClazz = resource; 
       ConfigLoaderAdapter anno = configClazz.getAnnotation(ConfigLoaderAdapter.class);
       if(anno == null) {
           adapterId = ConfigLoaderAdapterID.JSON_AUTOCONF;
       } else {
           adapterId = anno.adapter();
       }
    }
    
    public String getConfigLoaderAdapter() {
        return adapterId.getUri();
    }
    
    /**
     * @return
     * @throws SecurityException 
     */
    public Method getGetMethod() throws SecurityException {
        Class<?>[] params = new Class<?>[0]; 
        try {
            return LoaderAdapter.class.getMethod("getConfigLoaderAdapter", params );
        } catch (NoSuchMethodException e) {
            // never happens
        }
        return null;
    }

    /**
     * @return
     */
    public Method getSetMethod() throws SecurityException {
        return null;
    }

}
