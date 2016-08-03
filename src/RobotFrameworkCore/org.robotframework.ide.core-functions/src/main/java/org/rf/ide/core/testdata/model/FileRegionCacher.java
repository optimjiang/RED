/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.Beta;

/**
 * Please considerate to move this logic to global region cacher, in case of refactor API will be
 * made
 * 
 * @author wypych
 */
@Beta
public class FileRegionCacher<T> {

    private final Set<IRegionCacheable<T>> cache = new HashSet<IRegionCacheable<T>>(0);

    public void register(final IRegionCacheable<T> newCacheable) {
        cache.add(newCacheable);
    }

    public void unregister(final IRegionCacheable<T> removeCacheable) {
        cache.remove(removeCacheable);
    }

    public List<IRegionCacheable<T>> findByOffset(final int offset) {
        final List<IRegionCacheable<T>> inPosition = new ArrayList<IRegionCacheable<T>>(0);

        if (offset > FilePosition.NOT_SET) {
            for (final IRegionCacheable<T> cacheElement : cache) {
                if (cacheElement.getRegion().isInside(offset)) {
                    inPosition.add(cacheElement);
                }
            }
        }

        return inPosition;
    }

    public void clearCache() {
        cache.clear();
    }
}
