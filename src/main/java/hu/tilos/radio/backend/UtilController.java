package hu.tilos.radio.backend;

import hu.tilos.radio.backend.status.StatusService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
public class UtilController {

    @Inject
    StatusService statusService;

    @RequestMapping(value = "api/v1/status/radio")
    public List<String> approve() {
        return statusService.getLiveSources();
    }


    @RequestMapping(value = "api/v1/status/radio.txt", produces = "text/plain")
    @ResponseBody
    public String approve(@PathVariable String id) {
        return String.join("\n", statusService.getLiveSources());
    }


}
