package com.example.web;

import com.example.blog.Blogger;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
            @QueryParam("title") @DefaultValue("") String title,
            @QueryParam("page") @DefaultValue("1") long page,
            @QueryParam("size") @DefaultValue("10") int size) {
        var data = this.blogger
                .allPosts(
                        "%" + title + "%",
                        PageRequest.ofPage(page, size, true)
                );

        return Response.ok(new PaginatedResult<>(data.content(), data.totalElements())).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        return this.blogger.byId(id)
                .map(p -> Response.ok(p).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
