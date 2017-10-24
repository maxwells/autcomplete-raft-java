package com.autocomplete;

import com.autocomplete.coordination.commands.PutCommand;
import com.autocomplete.coordination.queries.GetWithPrefixQuery;
import com.google.common.collect.Lists;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.client.CopycatClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("autocomplete")
public class AutocompleteResource {
    private static Logger LOG = Logger.getLogger(AutocompleteResource.class.getName());
    private static CopycatClient client;

    public AutocompleteResource() {
        if (client == null) {
            // will figure out DI later
            List<Address> cluster = Lists.newArrayList(System.getenv("HOSTS").split(" "))
                    .stream()
                    .map(Address::new)
                    .collect(Collectors.toList());

            CopycatClient client = CopycatClient.builder()
                    .withTransport(new NettyTransport())
                    .build();
            client.serializer().register(PutCommand.class);
            client.serializer().register(GetWithPrefixQuery.class);

            client.connect(cluster).join();
            AutocompleteResource.client = client;
        }
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{item}")
    public Response addItem(@PathParam("item") String item, @QueryParam("weight") Integer weight) {
        Boolean response = client.submit(new PutCommand(item, weight)).join();

        if (response) {
            return Response.
                    ok().
                    build();
        } else {
            return Response
                    .notModified()
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{prefix}")
    public Response getMatchingItems(@PathParam("prefix") String prefix) {
        LOG.info("getting matching items for " + prefix);
        String[] prefixes = client.submit(new GetWithPrefixQuery(prefix)).join();

        return Response.ok(prefixes).build();
    }
}
