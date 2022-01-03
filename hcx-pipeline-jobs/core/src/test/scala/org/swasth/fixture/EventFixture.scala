package org.swasth.fixture

object EventFixture {

  val SAMPLE_EVENT_1: String =
    """{"actor":{"type":"User","id":"bc3be7ae-ad2b-4dee-ac4c-220c7db146b2"},"eid":"INTERACT",
      |"edata":{"type":"OTHER","subtype":"sheen-animation-ended","id":"library","pageid":"library","extra":{"pos":[]}},
      |"ver":"3.0","syncts":1.579564974098E12,"@timestamp":"2020-01-21T00:02:54.098Z","ets":1.579143065071E12,
      |"context":{"cdata":[],"env":"home","channel":"505c7c48ac6dc1edc9b08f21db5a571d",
      |"pdata":{"id":"prod.sunbird.portal","pid":"sunbird.app","ver":"2.3.144"},"sid":"df936f82-e982-41ec-8412-70d414458272",
      |"did":"758e054a400f20f7677f2def76427dc13ad1f837"},
      |"mid":"321a6f0c-10c6-4cdc-9893-207bb64fea50","type":"events","object":{"id":"do_9574","type":"content",
      |"version":"","rollup":{}}}""".stripMargin


  val SAMPLE_EVENT_2: String =
    """
      |{"ver":"3.0","eid":"SHARE","ets":1577278681178,"actor":{"type":"User","id":"7c3ea1bb-4da1-48d0-9cc0-c4f150554149"},"context":{"channel":"505c7c48ac6dc1edc9b08f21db5a571d","pdata":{"id":"prod.sunbird.desktop","pid":"sunbird.app","ver":"2.3.162"},"env":"app","sid":"82e41d87-e33f-4269-aeae-d56394985599","did":"1b17c32bad61eb9e33df281eecc727590d739b2b"},"edata":{"dir":"In","type":"File","items":[{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_312785709424099328114191","type":"CONTENT","ver":"1","params":[{"transfers":0,"size":21084308}]},{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_31277435209002188818711","type":"CONTENT","ver":"18","params":[{"transfers":12,"size":"123"}]},{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_31278794857559654411554","type":"TextBook","ver":"1"}]},"object":{"id":"do_312528116260749312248818","type":"TextBook","version":"10","rollup":{}},"mid":"02ba33e5-15fe-4ec5-b32.1084308E760-3d03429fae84","syncts":1577278682630,"@timestamp":"2019-12-25T12:58:02.630Z","type":"events"}
      |""".stripMargin

  val SAMPLE_EVENT_3: String =
    """
      |{"ver":"3.0","eid":"SHARE", "actor":{"type":"User","id":"7c3ea1bb-4da1-48d0-9cc0-c4f150554149"},"context":{"channel":"505c7c48ac6dc1edc9b08f21db5a571d","pdata":{"id":"prod.sunbird.desktop","pid":"sunbird.app","ver":"2.3.162"},"env":"app","sid":"82e41d87-e33f-4269-aeae-d56394985599","did":"1b17c32bad61eb9e33df281eecc727590d739b2b"},"edata":{"dir":"In","type":"File","items":[{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_312785709424099328114191","type":"CONTENT","ver":"1","params":[{"transfers":0,"size":21084308}]},{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_31277435209002188818711","type":"CONTENT","ver":"18","params":[{"transfers":12,"size":"123"}]},{"origin":{"id":"1b17c32bad61eb9e33df281eecc727590d739b2b","type":"Device"},"id":"do_31278794857559654411554","type":"TextBook","ver":"1"}]},"object":{"id":"do_312528116260749312248818","type":"TextBook","version":"10","rollup":{}},"mid":"02ba33e5-15fe-4ec5-b32.1084308E760-3d03429fae84","type":"events"}
      |""".stripMargin

  val customConfig =
    """
      |kafka {
      |  map.input.topic = "local.telemetry.map.input"
      |  map.output.topic = "local.telemetry.map.output"
      |  event.input.topic = "local.telemetry.event.input"
      |  event.output.topic = "local.telemetry.event.output"
      |  string.input.topic = "local.telemetry.string.input"
      |  string.output.topic = "local.telemetry.string.output"
      |  broker-servers = "localhost:9093"
      |  zookeeper = "localhost:2183"
      |  groupId = "pipeline-preprocessor-group"
      |  auto.offset.reset = "earliest"
      |  producer {
      |     max-request-size = 102400
      |     batch.size = 8192
      |     linger.ms = 1
      |  }
      |}
      |
      |task {
      |  parallelism = 2
      |  consumer.parallelism = 1
      |  checkpointing.compressed = true
      |  checkpointing.interval = 60000
      |  checkpointing.pause.between.seconds = 30000
      |  metrics.window.size = 100 # 3 min
      |  restart-strategy.attempts = 1 # retry once
      |  restart-strategy.delay = 1000 # in milli-seconds
      |}
      |
      |redisdb.connection.timeout = 30000
      |
      |redis {
      |  host = 127.0.0.1
      |  port = 6341
      |  database {
      |    duplicationstore.id = 12
      |    key.expiry.seconds = 3600
      |  }
      |}
      |
      |redis-meta {
      |  host = 127.0.0.1
      |  port = 6341
      |}
      |
      |postgress {
      |    host = localhost
      |    port = 5432
      |    maxConnection = 2
      |    user = "postgres"
      |    password = "postgres"
      |}
      |job {
      |  enable.distributed.checkpointing = true
      |  statebackend {
      |    blob {
      |      storage {
      |        account = "blob.storage.account"
      |        container = "telemetry-container"
      |        checkpointing.dir = "flink-jobs"
      |      }
      |    }
      |    base.url = "hdfs://testpath"
      |  }
      |}
    """.stripMargin

  val SAMPLE_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/coverageeligibility/check","status":"Submitted"}""".stripMargin

  val SAMPLE_WRONG_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"12345","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"67890","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/coverageeligibility/check","status":"Submitted"}""".stripMargin

  val SAMPLE_WRONG_SENDER_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"67890","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/coverageeligibility/check","status":"Submitted"}""".stripMargin

  val SAMPLE_WRONG_RECIPIENT_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"12345","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/coverageeligibility/check","status":"Submitted"}""".stripMargin

  val SAMPLE_INVALID_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/coverageeligibility.check","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/coverageeligibility/on_check",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.coverageeligibility/check",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_PREAUTH_SUBMIT_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth/submit","status":"Submitted"}""".stripMargin

  val SAMPLE_PREAUTH_SEARCH_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth/search","status":"Submitted"}""".stripMargin

  val SAMPLE_VALID_PREAUTH_SUBMIT_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth/submit","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/preauth/on_submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/preauth/submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_PREAUTH_SUBMIT_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth.submit","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/preauth/on_submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.preauth/submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_VALID_PREAUTH_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth/search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/preauth/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/preauth/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_PREAUTH_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/preauth.search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/preauth/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.preauth/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_PAYMENTS_REQUEST_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice/request","status":"Submitted"}""".stripMargin

  val SAMPLE_PAYMENTS_SEARCH_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice/search","status":"Submitted"}""".stripMargin

  val SAMPLE_VALID_PAYMENTS_REQUEST_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice/request","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/on_request",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/request",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_PAYMENTS_REQUEST_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice.request","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/on_request",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.paymentnotice/request",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_VALID_PAYMENTS_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice/search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_PAYMENTS_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/paymentnotice.search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/paymentnotice/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.paymentnotice/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_CLAIMS_SUBMIT_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim/submit","status":"Submitted"}""".stripMargin

  val SAMPLE_CLAIMS_SEARCH_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim/search","status":"Submitted"}""".stripMargin

  val SAMPLE_VALID_CLAIMS_SUBMIT_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim/submit","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/claim/on_submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/claim/submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_CLAIMS_SUBMIT_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim.submit","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/claim/on_submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.claim/submit",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_VALID_CLAIMS_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim/search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/claim/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1/claim/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin

  val SAMPLE_INVALID_CLAIMS_SEARCH_ACTION_EVENT: String =
    """{"ets":1637320447257,"headers":{"jose":{"alg":"RSA-OAEP","enc":"A256GCM"},
      |"protocol":{"x-hcx-recipient_code":"c4f5d97d-fe55-4322-aefe-69345268e4eb","x-hcx-request_id":"26b1060c-1e83-4600-9612-ea31e0ca5091",
      |"x-hcx-timestamp":"2021-10-27T20:35:52.636+0530","x-hcx-sender_code":"4193d6c2-5af9-4868-bf47-4356ca6d785b","x-hcx-correlation_id":"5e934f90-111d-4f0b-b016-c22d820674e1",
      |"x-hcx-status":"request.initiate"},"domain":{"request_amount":120000}},"mid":"761dfc11-1870-4981-b33d-16254a104a9d",
      |"action":"/v1/claim.search","status":"Submitted",
      |"cdata":{"sender":{"participant_code":"12345","signing_cert_path":"","roles":"admin","encryption_cert":"",
      |"endpoint_url":"http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080/v1/claim/on_search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"},
      |"recipient":{"participant_code":"67890","signing_cert_path":"urn:isbn:0-476-27557-4","roles":"admin","encryption_cert":"urn:isbn:0-4234",
      |"endpoint_url":"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v1.claim/search",
      |"participant_name":"Test Provider","hfr_code":"0001","status":"Created"}}}""".stripMargin
}
