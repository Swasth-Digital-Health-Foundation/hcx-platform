package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.service.HeaderAuditService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(Constants.VERSION_PREFIX)
public class AuditController {

	private final HeaderAuditService service;
	
	@Autowired
	public AuditController(HeaderAuditService service) {
		this.service =  service;
	}

    @PostMapping(Constants.AUDIT_SEARCH)
    public List<Map<String, Object>> search(@RequestBody final SearchRequestDTO dto) {
        return service.search(dto);
    }

}
