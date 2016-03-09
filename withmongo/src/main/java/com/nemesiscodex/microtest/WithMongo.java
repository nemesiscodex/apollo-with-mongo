package com.nemesiscodex.microtest;

import com.mongodb.MongoClient;
import com.nemesiscodex.microtest.entities.Employee;
import com.spotify.apollo.Environment;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.route.Route;
import com.typesafe.config.Config;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.List;

public final class WithMongo {

    private static Datastore datastore;

    public static void main(String... args) throws LoadingException {
        HttpService.boot(WithMongo::init, "with-mongo", args);
    }

    static void init(Environment environment) {

        initMongoDatasource(environment.config());

        initEmployees();

        environment.routingEngine()
                .registerAutoRoute(Route.sync("GET", "/", rc -> "hello world"))
                .registerAutoRoute(Route.sync("GET", "/employee", WithMongo::getEmployees));

    }

    public static Response<List<Employee>> getEmployees(RequestContext rc) {

        final Query<Employee> query = datastore.createQuery(Employee.class);
        return Response.forPayload(query.asList()).withHeader("Content-Type", "application/json");

    }

    private static void initMongoDatasource(Config config) {
        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.mapPackage("com.nemesiscodex.microtest.entities");

        // create mongo cliente (with-mongo.conf)
        MongoClient mongoClient = new MongoClient(config.getString("mongo.domain"), config.getInt("mongo.port"));

        // create the Datastore
        datastore = morphia.createDatastore(mongoClient, "employees");
        datastore.ensureIndexes();
    }

    private static void initEmployees() {
        final Employee elmer = new Employee("Elmer Fudd", 50000.0);
        datastore.save(elmer);

        final Employee daffy = new Employee("Daffy Duck", 40000.0);
        datastore.save(daffy);

        final Employee pepe = new Employee("Pepé Le Pew", 25000.0);
        datastore.save(pepe);

        elmer.setDirectReports(new ArrayList<>());

        elmer.getDirectReports().add(daffy);
        elmer.getDirectReports().add(pepe);

        datastore.save(elmer);
    }

}