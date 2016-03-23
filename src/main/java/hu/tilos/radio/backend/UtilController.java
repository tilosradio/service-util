package hu.tilos.radio.backend;

import hu.tilos.radio.backend.stat.ArchiveStat;
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

    @Inject
    ArchiveStat archiveStat;

    @RequestMapping(value = "api/v1/status/radio")
    public List<String> status() {
        return statusService.getLiveSources();
    }


    @RequestMapping(value = "api/v1/status/radio.txt", produces = "text/plain")
    @ResponseBody
    public String statusTxt() {
        return String.join("\n", statusService.getLiveSources());
    }

    @RequestMapping(value = "api/v1/util/download-stat/{date}", produces = "text/plain")
    @ResponseBody
    public String approve(@PathVariable String date) {
        return archiveStat.run(date);
    }
}
