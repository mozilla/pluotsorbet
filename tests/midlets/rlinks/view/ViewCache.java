/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.view;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * A view cache based on weak references. Weak references are very likely to
 * be garbage collected, to the lifetime of a view stored in a cache is not
 * going to be long. However, it will still make it quite a bit more
 * comfortable for the user to change between views as the views do not need
 * to be populated and/or re-created every time.
 */
public class ViewCache {

    /**
     * The data structure used to contain the WeakReferences to actual
     * objects.
     */
    private Hashtable cache = new Hashtable();

    /**
     * Determine whether the object with the given key is contained in the
     * cache.
     * 
     * The null-checking is handled here for convenience.
     * 
     * @param key Object key
     * @return True if object can be found in cache, false otherwise
     */
    public boolean contains(Object key) {
        return cache.containsKey(key) && ((WeakReference) cache.get(key)).get() != null;
    }

    /**
     * Get a view with the given key.
     *
     * @param key Object key
     * @return Cached view corresponding the given key, or null if no view was
     * found in the cache with the given key
     */
    public BaseFormView get(Object key) {
        if (!cache.containsKey(key)) {
            return null;
        }
        Object value;
        WeakReference w = (WeakReference) cache.get(key);
        if (w != null && (value = w.get()) != null) {
            return (BaseFormView) value;
        }
        return null;
    }

    /**
     * Put a view in the cache.
     * 
     * @param key Key to cache the view under
     * @param value The view to cache
     */
    public void put(Object key, BaseFormView value) {
        cache.put(key, new WeakReference(value));
    }

}
