/// usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:7.1.0
//DEPS org.slf4j:slf4j-simple:2.0.12

import io.javalin.Javalin;

public class JavalinServer {
    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.concurrency.useVirtualThreads = true;

            config.routes.get("/", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.post("/posts", ctx -> {
                ctx.status(201);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/posts", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.query("/posts", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/posts/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.put("/posts/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.delete("/posts/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/comments/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/albums/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/photos/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/todos/{id}", ctx -> {
                ctx.status(200);
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
            config.routes.get("/exit", ctx -> {
                System.out.println("Exit.");
                System.exit(0);
            });
            config.routes.get("/rendezvous", ctx -> {
                System.out.println(ctx.body());
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            });
        }).start(7070);
    }
}