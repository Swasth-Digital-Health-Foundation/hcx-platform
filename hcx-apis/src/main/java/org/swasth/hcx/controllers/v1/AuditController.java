package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.service.AuditService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(Constants.VERSION_PREFIX)
public class AuditController {

	@Autowired
	private AuditService service;

    @PostMapping(Constants.AUDIT_SEARCH)
    public List<Map<String, Object>> auditSearch(@RequestBody AuditSearchRequest request) {
        return service.search(request, Constants.AUDIT_SEARCH);
    }

	@PostMapping(Constants.AUDIT_NOTIFICATION_SEARCH)
	public List<Map<String, Object>> notificationAuditSearch(@RequestBody AuditSearchRequest request) {
		return service.search(request, Constants.AUDIT_NOTIFICATION_SEARCH);
	}

}
