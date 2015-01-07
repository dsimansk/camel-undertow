package org.apache.camel.component.undertow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Simansky | dsimansk@redhat.com
 */
public class UndertowUtils {

    public static void appendHeader(Map<String, Object> headers, String key, Object value) {
        if (headers.containsKey(key)) {
            Object existing = headers.get(key);
            List<Object> list;
            if (existing instanceof List) {
                list = (List<Object>) existing;
            } else {
                list = new ArrayList<Object>();
                list.add(existing);
            }
            list.add(value);
            value = list;
        }

        headers.put(key, value);
    }
}
