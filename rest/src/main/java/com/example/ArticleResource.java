package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonPatch;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("articles")
@RequestScoped
public class ArticleResource {

    @Inject
    ArticleRepository repository;

    @Inject
    Jsonb jsonb;

    @GET
    public Response getArticles() {
        return Response.ok(repository.findAll()).build();
    }

    @GET
    @Path("{id}")
    public Response getArticle(@PathParam("id") Integer id) {
        return Response.ok(repository.findById(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createArticle(Article article) {
        var saved = repository.save(article);
        return Response.created(URI.create("/articles/" + saved.id())).build();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response saveOrUpdateAllArticles(JsonPatch patch) {
        var all = repository.findAll();
        var result = patch.apply(Json.createReader(new StringReader(jsonb.toJson(all))).readArray());
        List<Article> articles = jsonb.fromJson(
                jsonb.toJson(result), // to json string
                new ArrayList<Article>(){}.getClass().getGenericSuperclass()
        );
        articles.forEach(repository::save);

        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response updateArticle(@PathParam("id") Integer id, JsonPatch patch) {
        var target = repository.findById(id);
        var patchedResult = patch.apply(Json.createReader(new StringReader(jsonb.toJson(target))).readObject());
        var article = jsonb.fromJson(jsonb.toJson(patchedResult), Article.class);
        repository.save(article);

        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_MERGE_PATCH_JSON) // added in 4.0
    //@Consumes("application/merge-patch+json")
    public Response mergeArticle(@PathParam("id") Integer id, JsonMergePatch patch) {
        var targetArticle = repository.findById(id);
        var mergedResult = patch.apply(Json.createReader(new StringReader(jsonb.toJson(targetArticle))).readObject());
        var article = jsonb.fromJson(jsonb.toJson(mergedResult), Article.class);
        repository.save(article);

        return Response.noContent().build();
    }
}
