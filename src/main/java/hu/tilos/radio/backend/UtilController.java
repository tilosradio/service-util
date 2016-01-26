package hu.tilos.radio.backend;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import hu.tilos.radio.backend.status.StatusService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UtilController {

    @Inject
    StatusService statusService;

    @HystrixCommand(fallbackMethod = "emptyList")
    @RequestMapping(value = "api/v1/status/radio")
    public List<String> approve() {
        return statusService.getLiveSources();
    }

    @HystrixCommand
    @RequestMapping(value = "api/v1/status/radio.txt", produces = "text/plain")
    @ResponseBody
    public String approve(@PathVariable String id) {
        return String.join("\n", statusService.getLiveSources());
    }

    public List<String> emptyList() {
        return new ArrayList<String>();
    }

}
