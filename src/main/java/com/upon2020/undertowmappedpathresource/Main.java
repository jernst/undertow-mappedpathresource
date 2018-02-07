//
// 
//

package com.upon2020.undertowmappedpathresource;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Main {
    public static void main(
            String [] args )
    {
        Map<String,Path> mappings = new HashMap<>();
        mappings.put( "/foo", Paths.get( System.getProperty("user.home") + "/.bashrc" ));
        
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(
                        new ResourceHandler(
                                new MappedPathResourceManager( mappings )))
                .build();
        server.start();
    }
}
