//
// Copyright (C) 1998 and later, Johannes Ernst. All rights reserved. License: see package.
//

package com.upon2020.undertowmappedpathresource;

import io.undertow.UndertowMessages;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.ETag;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 *
 */
public class MappedPathResourceManager implements ResourceManager
{
    private static final Logger log = Logger.getLogger(PathResourceManager.class.getName());

    private static final long DEFAULT_TRANSFER_MIN_SIZE = 1024;
    private static final ETagFunction NULL_ETAG_FUNCTION = new ETagFunction() {
        @Override
        public ETag generate(Path path) {
            return null;
        }
    };

    /**
     * Maps requested paths to Paths to files.
     */
    private Map<String,Path> mapping;

    /**
     * Size to use direct FS to network transfer (if supported by OS/JDK) instead of read/write
     */
    private final long transferMinSize;

    private final ETagFunction eTagFunction;

    public MappedPathResourceManager(final Map<String,Path> mapping) {
        this(mapping, DEFAULT_TRANSFER_MIN_SIZE);
    }

    protected MappedPathResourceManager(long transferMinSize) {
        this.transferMinSize = transferMinSize;
        this.eTagFunction = NULL_ETAG_FUNCTION;
    }

    public MappedPathResourceManager(final Map<String,Path> mapping, long transferMinSize) {
        this(builder()
                .setMapping(mapping)
                .setTransferMinSize(transferMinSize));
    }

    private MappedPathResourceManager(Builder builder) {
        if (builder.mapping == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("mapping");
        }
        this.mapping = builder.mapping;
        this.transferMinSize = builder.transferMinSize;
        this.eTagFunction = builder.eTagFunction;
    }

    public MappedPathResourceManager setMapping(final Map<String,Path> mapping) {
        if (mapping == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("mapping");
        }
        this.mapping = mapping;
        return this;
    }

    public Resource getResource(final String p) {
        Path file = mapping.get( p );
        if( file == null ) {
            log.tracef("Failed to get path resource %s from mapped path resource manager", p);
            return null;
        }
        
        return new MappedPathResource(file, this, p, eTagFunction.generate(file));
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public synchronized void registerResourceChangeListener(ResourceChangeListener listener) {
    }


    @Override
    public synchronized void removeResourceChangeListener(ResourceChangeListener listener) {
    }

    public long getTransferMinSize() {
        return transferMinSize;
    }

    @Override
    public synchronized void close() throws IOException {
    }

    public interface ETagFunction {

        /**
         * Generates an {@link ETag} for the provided {@link Path}.
         *
         * @param path Path for which to generate an ETag
         * @return ETag representing the provided path, or null
         */
        ETag generate(Path path);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Map<String,Path> mapping;
        private long transferMinSize = DEFAULT_TRANSFER_MIN_SIZE;
        private ETagFunction eTagFunction = NULL_ETAG_FUNCTION;

        private Builder() {
        }

        public Builder setMapping(Map<String,Path> mapping) {
            this.mapping = mapping;
            return this;
        }

        public Builder setTransferMinSize(long transferMinSize) {
            this.transferMinSize = transferMinSize;
            return this;
        }

        public Builder setETagFunction(ETagFunction eTagFunction) {
            this.eTagFunction = eTagFunction;
            return this;
        }

        public ResourceManager build() {
            return new MappedPathResourceManager(this);
        }
    }
}
