package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.service.HeaderAuditService;

import java.util.List;


@RestController
public class AuditController {

	private final HeaderAuditService service;
	
	@Autowired
	public AuditController(HeaderAuditService service) {
		this.service =  service;
	}

    @PostMapping(Constants.AUDIT_SEARCH)
    public List<HeaderAudit> search(@RequestBody final SearchRequestDTO dto) {
        return service.search(dto);
    }

}
