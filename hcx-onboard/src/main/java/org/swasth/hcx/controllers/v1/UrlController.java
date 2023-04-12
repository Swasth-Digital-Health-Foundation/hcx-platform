package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.services.UrlService;


@RestController
public class UrlController {

    @Autowired
    UrlService urlService;

    @GetMapping("/url/{id}")
    public ResponseEntity<Void> getLongUrl(@PathVariable String id) throws Exception {
        String longUrl = urlService.getLongurl(id);
        if (longUrl != null) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header("Location", longUrl)
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
