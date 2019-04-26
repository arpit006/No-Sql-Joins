package com.navis.nosqljoin.framework.controller;

import com.navis.nosqljoin.framework.service.ServiceBase;
import lombok.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@RestController
@RequestMapping("/nosqljoin")
public class ControllerBase<T> {

    private final ServiceBase<String> serviceBase;

    public ControllerBase(ServiceBase<String> inServiceBase) {
        serviceBase = inServiceBase;
    }

    @NonNull
    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    public T save(@RequestBody String inJson) {
        return (T) serviceBase.save(inJson);
    }
}
