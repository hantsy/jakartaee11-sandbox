package com.example;

import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@RequestScoped
@Path("/posts")
public class PostResources {

    @Inject
    Blogger blogger;

    @GET
    @Path("")
    public Response getAll(
            @QueryParam("title") String title,
            @QueryParam("page") long page,
            @QueryParam("size") int size) {
        var data = this.blogger
                .allPosts(
                        "%" + title + "%",
                        PageRequest.ofPage(page, size, true)
                );

        return Response.ok(data).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return this.blogger.byId(id)
                .map(p -> Response.ok(p).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
