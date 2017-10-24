package com.autocomplete;

import com.autocomplete.coordination.commands.PutCommand;
import com.autocomplete.coordination.queries.GetWithPrefixQuery;
import com.autocomplete.model.Autocompleter;
import com.google.common.collect.Lists;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:" + System.getenv("HTTP_PORT") + "/myapp/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        Map<String, Object> props = new HashMap<>();

        final ResourceConfig rc = new ResourceConfig()
                .packages("com.autocomplete")
                .register(GsonMessageBodyHandler.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    private static void startAutocompleter(Address address, File file, List<Address> cluster) {
        Transport transport = NettyTransport.builder()
                .withThreads(4)
                .build();

        Storage storage = Storage.builder()
                .withDirectory(file)
                .withStorageLevel(StorageLevel.DISK)
                .build();

        CopycatServer server = CopycatServer
                .builder(address)
                .withStateMachine(Autocompleter::new)
                .withTransport(transport)
                .withStorage(storage)
                .build();

        server.serializer().register(PutCommand.class);
        server.serializer().register(GetWithPrefixQuery.class);

        server.onStateChange((state) -> System.out.println("new state: " + state.name()));

        System.out.println("Bootstrapping!");
        server.bootstrap(cluster).thenRun(() -> System.out.println("Bootstrapped!"));
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        List<Address> cluster = Lists.newArrayList(System.getenv("HOSTS").split(" "))
                .stream()
                .map(Address::new)
                .collect(Collectors.toList());
        startAutocompleter(new Address(System.getenv("SELF")), new File(System.getenv("DATA_PATH")), cluster);

        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
}

