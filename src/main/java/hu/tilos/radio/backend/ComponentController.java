package hu.tilos.radio.backend;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ComponentController {

    @Inject
    private DiscoveryClient discoveryClient;


    @RequestMapping(value = "api/v1/util/components")
    public List<ServiceInfo> approve() {
        List<ServiceInfo> result = new ArrayList<>();
        for (String serviceName : discoveryClient.getServices()) {
            for (ServiceInstance service : discoveryClient.getInstances(serviceName)) {


                ServiceInfo serviceInfo = new ServiceInfo();
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    serviceInfo = restTemplate.getForObject("http://" + service.getHost() + ":" + service.getPort() + "/info", ServiceInfo.class);
                } catch (Exception ex) {
                    System.err.println("Can't access info for " + serviceName);
                }
                serviceInfo.setName(serviceName);
                result.add(serviceInfo);

            }
        }
        return result;

    }


}
