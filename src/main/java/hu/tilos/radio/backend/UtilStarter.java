package hu.tilos.radio.backend;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import hu.tilos.radio.backend.spark.GuiceConfigurationListener;
import hu.tilos.radio.backend.spark.JsonTransformer;
import hu.tilos.radio.backend.spark.SparkDefaults;
import hu.tilos.radio.backend.status.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import spark.ResponseTransformer;

import java.util.List;

import static spark.Spark.get;

public class UtilStarter {

    private static final Logger LOG = LoggerFactory.getLogger(UtilStarter.class);

    private Gson gson;

    static Injector injector;

    private FiniteDuration timeout;

    @Inject
    StatusService statusService;

    @Inject
    @Configuration(name = "port.util")
    private int portComment;

    public static void main(String[] args) {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new GuiceConfigurationListener());
                bind(Integer.class).toInstance(1);


            }
        });
        injector.getInstance(UtilStarter.class).run();

    }

    private void run() {
        timeout = Duration.create(5, "seconds");

        LOG.info("Starting new deployment");

        SparkDefaults spark = new SparkDefaults(portComment, injector);

        JsonTransformer jsonResponse = spark.getJsonTransformer();
        get("/api/v1/status/radio", (req, res) -> statusService.getLiveSources(), jsonResponse);

        get("/api/v1/status/radio.txt", (req, res) -> statusService.getLiveSources(), new ResponseTransformer() {
            @Override
            public String render(Object model) throws Exception {
                StringBuilder result = new StringBuilder();
                List<String> list = (List<String>) model;
                for (String line : list) {
                    result.append(line + "\n");
                }
                return result.toString();
            }
        });


    }


}
