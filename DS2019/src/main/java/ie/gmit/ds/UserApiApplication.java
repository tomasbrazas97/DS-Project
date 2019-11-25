package ie.gmit.ds;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class UserApiApplication extends Application<UserApiConfig> {

    public static void main(String[] args) throws Exception {
        new UserApiApplication().run(args);
    }

    public void run(UserApiConfig userApiConfig, Environment environment) throws Exception {

        final UserResource resource = new UserResource();
        environment.jersey().register(resource);
        
        final HealthCheckImpl healthCheck = new HealthCheckImpl();
        environment.healthChecks().register("testing", healthCheck);
    }
    
}