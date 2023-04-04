package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${audit.hcxIndex}")
	public String hcxIndex;

	@Value("${audit.onboardIndex}")
	public String onboardIndex;

    @PostMapping(Constants.AUDIT_SEARCH)
    public List<Map<String, Object>> hcxAuditSearch(@RequestBody AuditSearchRequest request) {
        return service.search(request, Constants.AUDIT_SEARCH, hcxIndex);
    }

	@PostMapping(Constants.AUDIT_NOTIFICATION_SEARCH)
	public List<Map<String, Object>> notificationAuditSearch(@RequestBody AuditSearchRequest request) {
		return service.search(request, Constants.AUDIT_NOTIFICATION_SEARCH, hcxIndex);
	}

	@PostMapping(Constants.AUDIT_ONBOARD_SEARCH)
	public List<Map<String, Object>> onboardAuditSearch(@RequestBody AuditSearchRequest request) {
		return service.search(request, Constants.AUDIT_ONBOARD_SEARCH, onboardIndex);
	}

}
