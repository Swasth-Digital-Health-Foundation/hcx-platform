package org.swasth.common.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION_PREFIX = "/v1";

    // Health APIs
    public static final String HEALTH = "/health";
    public static final String SERVICE_HEALTH = "/service/health";

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK = "/coverageeligibility/check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK = "/coverageeligibility/on_check";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT = "/preauth/submit";
    public static final String PRE_AUTH_ONSUBMIT = "/preauth/on_submit";
    public static final String CLAIM_SUBMIT = "/claim/submit";
    public static final String CLAIM_ONSUBMIT = "/claim/on_submit";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST = "/paymentnotice/request";
    public static final String PAYMENT_NOTICE_ONREQUEST = "/paymentnotice/on_request";

    //Status Search APIs
    public static final String HCX_STATUS = "/hcx/status";
    public static final String HCX_ONSTATUS = "/hcx/on_status";

    // Search APIs
    public static final String HCX_SEARCH = "/hcx/search";
    public static final String HCX_ON_SEARCH = "/hcx/on_search";

    //Communication APIs
    public static final String COMMUNICATION_REQUEST = "/communication/request";
    public static final String COMMUNICATION_ONREQUEST = "/communication/on_request";

    //Predetermination APIs
    public static final String PREDETERMINATION_SUBMIT = "/predetermination/submit";
    public static final String PREDETERMINATION_ONSUBMIT = "/predetermination/on_submit";

    // Notification APIs
    public static final String NOTIFICATION_SUBSCRIBE = "/notification/subscribe";
    public static final String NOTIFICATION_UNSUBSCRIBE = "/notification/unsubscribe";
    public static final String NOTIFICATION_ON_SUBSCRIBE = "/notification/on_subscribe";
    public static final String NOTIFICATION_SUBSCRIPTION_LIST = "/notification/subscription/list";
    public static final String NOTIFICATION_LIST = "/notification/topic/list";
    public static final String NOTIFICATION_NOTIFY = "/notification/notify";
    public static final String NOTIFICATION_SUBSCRIPTION_UPDATE = "/notification/subscription/update";

    // Audit APIs
    public static final String AUDIT_SEARCH = "/audit/search";
    public static final String AUDIT_NOTIFICATION_SEARCH = "/audit/notification/search";
    public static final String AUDIT_ONBOARD_SEARCH = "/audit/onboard/search";

    // Supportability APIs
    public static final String REQUEST_RETRY = "/request/retry/{mid}";

    // Registry Participant APIs
    public static final String PARTICIPANT_CREATE = "/participant/create";
    public static final String PARTICIPANT_SEARCH = "/participant/search";
    public static final String PARTICIPANT_READ = "/participant/read/{participantCode}";
    public static final String PARTICIPANT_UPDATE = "/participant/update";
    public static final String SPONSORS = "sponsors";
    public static final String COMMUNICATION = "communication";
    public static final String MOCK_PARTICIPANT = "mockparticipant";
    public static final String FORMSTATUS = "status";
    public static final String PARTICIPANT_DELETE = "/participant/delete";
    public static final String PARTICIPANT_VERIFY = "/participant/verify";
    public static final String APPLICANT_GET_INFO = "/applicant/getinfo";
    public static final String APPLICANT_SEARCH = "/applicant/search";
    public static final String PARTICIPANT_VERIFICATION_LINK_SEND = "/participant/verification/link/send";
    public static final String PARTICIPANT_ONBOARD_UPDATE = "/participant/onboard/update";
    public static final String PARTICIPANT_VERIFY_IDENTITY = "/participant/verify/identity";
    public static final String APPLICANT_VERIFY = "/applicant/verify";
    public static final String PARTICIPANT = "participant";
    public static final String VERIFIER_CODE = "verifier_code";
    public static final String RESULT = "result";
    public static final String ONBOARD_THROUGH_JWT = "onboard-through-jwt";
    public static final String ONBOARD_THROUGH_VERIFIER = "onboard-through-verifier";
    public static final List<String> ONBOARD_FOR_PROVIDER = Arrays.asList(ONBOARD_THROUGH_JWT, ONBOARD_THROUGH_VERIFIER);
    public static final String ROLE = "role";
    public static final String LINK_VERIFICATION = "linkVerification";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String HEALTHY = "healthy";
    public static final String NAME = "name";
    public static final String KAFKA = "kafka";
    public static final String POSTGRESQL = "postgreSQL";
    public static final String REDIS ="redis";
    public static final String ELASTICSEARCH = "elasticSearch";
    public static final String CHECKS = "checks";

    //event generator props
    public static final String MID = "mid";
    public static final String DATA = "data";
    public static final String PAYLOAD = "payload";
    public static final String ETS = "ets";
    public static final String ACTION = "action";
    public static final String HEADERS = "headers";
    public static final String JOSE="jose";
    public static final String PROTOCOL = "protocol";
    public static final String DOMAIN = "domain";
    public static final String RETRY_COUNT = "retryCount";
    public static final String RETRY_INDEX = "retryIndex";
    public static final String EID = "eid";
    public static final String AUDIT = "AUDIT";
    public static final String SENDER_ROLE = "senderRole";
    public static final String RECIPIENT_ROLE = "recipientRole";
    public static final String AUDIT_TIMESTAMP = "auditTimeStamp";
    public static final String UPDATED_TIME = "updatedTimestamp";
    public static final String REQUEST_TIME = "requestTimeStamp";
    public static  final String OBJECT = "object";
    public static  final String CDATA = "cdata";
    public static  final String EDATA = "edata";
    public static  final String ID = "id";
    public static  final String TYPE = "type";
    public static final String TYP = "typ";
    public static  final String AUDIT_STATUS = "status";
    public static  final String PREV_STATUS = "prevStatus";
    public static  final String PROPS = "props";
    public static  final String CREATED = "Created";

    //Request props
    public static final String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    public static final String ERROR_HEADERS_MANDATORY = "plainrequest.headers.mandatory";
    public static final String ERROR_HEADERS_OPTIONAL = "plainrequest.headers.optional";
    public static final String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    public static final String REDIRECT_HEADERS_MANDATORY = "redirect.headers.mandatory";
    public static final String REDIRECT_HEADERS_OPTIONAL = "redirect.headers.optional";
    public static final String JOSE_HEADERS = "headers.jose";
    public static final String DOMAIN_HEADERS = "headers.domain";
    public static final String HCX_SENDER_CODE = "x-hcx-sender_code";
    public static final String HCX_RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String SENDER_NAME = "senderName";
    public static final String RECIPIENT_NAME = "recipientName";
    public static final String SENDERDETAILS = "senderDetails";
    public static final String RECIPIENTDETAILS = "recipientDetails";
    public static final String SENDER_PRIMARY_EMAIL = "senderPrimaryEmail";
    public static final String RECIPIENT_PRIMARY_EMAIL = "recipientPrimaryEmail";
    public static final String TAGS = "tags";
    public static final String API_CALL_ID = "x-hcx-api_call_id";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String TIMESTAMP = "x-hcx-timestamp";
    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final String STATUS = "x-hcx-status";
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String NOTIFICATION_HEADERS = "x-hcx-notification_headers";
    public static final String NOTIFICATION_TIMESTAMP = "timestamp";
    public static final String NOTIFICATION_CORRELATION_ID = "correlation_id";
    public static final String ALG = "alg";
    public static final String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";
    public static final String ENDPOINT_URL = "endpoint_url";
    public static final String HCX_NOT_ALLOWED_URLS = "hcx.urls.notallowed";

    public static final String HEADER_AUDIT = "hcx_audit";
    public static final String PAYOR = "payor";
    public static final String PROVIDER = "provider";
    public static final String AGENCY_TPA = "agency.tpa";
    public static final String MEMBER_ISNP = "member.isnp";
    public static final String ROLES = "roles";
    public static final String SCHEME_CODE = "scheme_code";
    public static final String PARTICIPANT_CODE = "participant_code";
    public static final String PARTICIPANT_NAME = "participant_name";
    public static final String PARTICIPANT_ROLE = "participant_role";
    public static final String PRIMARY_EMAIL = "primary_email";
    public static final String PRIMARY_MOBILE = "primary_mobile";
    public static final String SIGNING_CERT_PATH = "signing_cert_path";
    public static final String ENCRYPTION_CERT = "encryption_cert";
    public static final String ENCRYPTION_CERT_EXPIRY = "encryption_cert_expiry";
    public static final String SIGNING_CERT_PATH_EXPIRY = "sigining_cert_expiry";
    public static final String PUBLIC_KEY = "public_key";
    public static final String PRIVATE_KEY = "private_key";
    public static final String PASSWORD = "password";
    public static final String CHILD_PARTICIPANT_CODE = "child_participant_code";
    public static final String PARENT_PARTICIPANT_CODE = "parent_participant_code";
    public static final String ADMIN_ROLE = "HIE/HIO.HCX";
    public static final String MOCK_PROVIDER = "mock_provider";
    public static final String MOCK_PAYOR = "mock_payor";
    public static final String EQUAL_OPERATION = "eq";
    public static final String OSID = "osid";
    public static final String AUTHORIZATION = "Authorization";
    public static final String FILTERS = "filters";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String SEARCH = "search";
    public static final String INVITE = "invite";
    public static final String START_DATETIME = "start_datetime";
    public static final String STOP_DATETIME = "stop_datetime";
    public static final String SEARCH_REQ ="x-hcx-search";
    public static final String SEARCH_RESP = "x-hcx-search_response";
    public static final List<String> SEARCH_REQ_KEYS = Arrays.asList("filters","time_period");
    public static final String SEARCH_FILTERS = "filters";
    public static final List<String>  SEARCH_FILTER_KEYS = Arrays.asList("senders","receivers","entity_types","workflow_ids","case_ids","entity_status");
    public static final String SEARCH_FILTERS_RECEIVER = "receivers";
    public static final List<String> SEARCH_RES_KEYS = Arrays.asList("count","entity_counts");
    public static final String HCX_REGISTRY_CODE = "hcx-registry-code";
    public static final String ERROR_STATUS = "response.error";
    public static final String REDIRECT_STATUS = "response.redirect";
    public static final String COMPLETE_STATUS = "response.complete";
    public static final String PARTIAL_STATUS = "response.partial";
    public static final String QUEUED_STATUS = "request.queued";
    public static final String DISPATCHED_STATUS = "request.dispatched";
    public static final String RETRY_STATUS = "request.retry";
    public static final String RETRY_PROCESSING_STATUS = "request.retry.processing";
    //Notification constants
    public static final String ACTIVE = "Active";
    public static final String INACTIVE = "Inactive";
    public static final String PENDING = "pending";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String SUBSCRIPTION_REQUEST_ID = "subscription_request_id";
    public static final String NOTIFICATION_HEADERS_MANDATORY = "notification.headers.mandatory";
    public static final String NOTIFICATION_HEADERS_OPTIONAL = "notification.headers.optional";
    public static final String PRIORITY = "priority";
    public static final String CATEGORY = "category";
    public static final String BROADCAST = "Broadcast";
    public static final String WORKFLOW = "Workflow";
    public static final String NETWORK = "Network";
    public static final String TOPIC_CODE = "topic_code";
    public static final List<String> ALLOWED_NOTIFICATION_FILTER_PROPS = Arrays.asList(CATEGORY, PRIORITY);
    public static final String RECIPIENT = "Recipient";
    public static final String RECIPIENT_CODES = "recipient_codes";
    public static final String RECIPIENT_ROLES = "recipient_roles";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String RECIPIENTS = "recipients";
    public static final String RECIPIENT_TYPE = "recipient_type";
    public static final String NOTIFICATION_DATA = "notification_data";
    public static final String ALLOWED_RECIPIENTS = "allowed_recipients";
    public static final String ALLOWED_SENDERS = "allowed_senders";
    public static final String SENDER_CODE = "sender_code";
    public static final String MESSAGE = "message";
    public static final String SENDER_LIST = "sender_list";
    public static final String SUBSCRIPTION_MAP = "subscription_map";
    public static final String SUBSCRIPTION_STATUS = "subscription_status";
    public static final String RECIPIENT_CODE = "recipient_code";
    public static final String IS_DELEGATED = "is_delegated";
    public static final String EXPIRY = "expiry";
    public static final List<String> ALLOWED_SUBSCRIPTION_STATUS = Arrays.asList(ACTIVE, INACTIVE);
    public static final List<String> SUBSCRIPTION_UPDATE_PROPS = Arrays.asList(SUBSCRIPTION_STATUS, EXPIRY, IS_DELEGATED);
    public static final String RS256 = "RS256";
    public static final String SUBSCRIPTION = "subscription";
    public static final List<String> ALLOWED_RECIPIENT_TYPE = Arrays.asList(Constants.PARTICIPANT_CODE, Constants.PARTICIPANT_ROLE, Constants.SUBSCRIPTION);

    //Constants from api-gateway
    // Entity types
    public static final String NOTIFICATION = "notification";
    public static final String PAYMENT = "paymentnotice";

    public static final int PROTOCOL_PAYLOAD_LENGTH = 5;
    public static final int NOTIFICATION_PAYLOAD_LENGTH = 3;
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String X_JWT_SUB_HEADER = "X-jwt-sub";
    public static final String REDIRECT_TO = "x-hcx-redirect_to";
    public static final List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    public static final List<String> REQUEST_STATUS_VALUES = Arrays.asList("request.queued", "request.dispatched");
    public static final List<String> ERROR_DETAILS_VALUES = Arrays.asList("code","message","trace");
    public static final String REQUESTED_TIME = "requestTimeStamp";
    public static final String BLOCKED = "Blocked";
    public static final String OS_OWNER = "osOwner";
    public static final String ERROR_RESPONSE = "response.error";
    public static final List<String> RECIPIENT_ERROR_VALUES = Arrays.asList("ERR_INVALID_ENCRYPTION", "ERR_INVALID_PAYLOAD", "ERR_WRONG_DOMAIN_PAYLOAD", "ERR_INVALID_DOMAIN_PAYLOAD", "ERR_SENDER_NOT_SUPPORTED", "ERR_SERVICE_UNAVAILABLE", "ERR_DOMAIN_PROCESSING","ERR_MANDATORY_HEADER_MISSING",
            "ERR_INVALID_API_CALL_ID",
            "ERR_INVALID_CORRELATION_ID",
            "ERR_INVALID_TIMESTAMP",
            "ERR_INVALID_REDIRECT_TO",
            "ERR_INVALID_STATUS",
            "ERR_INVALID_DEBUG_FLAG",
            "ERR_INVALID_ERROR_DETAILS",
            "ERR_INVALID_DEBUG_DETAILS",
            "ERR_INVALID_WORKFLOW_ID");
    public static final String REGISTRY_STATUS = "status";
    public static final String SENDER = "Sender";
    public static final List<String> OPERATIONAL_ENTITIES = Arrays.asList(NOTIFICATION, COMMUNICATION, REGISTRY_STATUS);
    public static final List<String> EXCLUDE_ENTITIES = Arrays.asList(COMMUNICATION, PAYMENT, NOTIFICATION);
    public static final List<String> RESPONSE_STATUS_VALUES = Arrays.asList(COMPLETE_STATUS, PARTIAL_STATUS, ERROR_RESPONSE, REDIRECT_STATUS);
    public static final List<String> ALLOWED_ENTITIES_ERROR_AUDIT_CREATION = Arrays.asList("coverageeligibility", "preauth", "claim", PAYMENT, COMMUNICATION, "predetermination", REGISTRY_STATUS, SEARCH);
    public static final String JWT_TOKEN = "jwt_token";
    public static final String VERIFICATION_TOKEN = "verification_token";
    public static final String JWT = "jwt";
    public static final String MOBILE = "mobile";
    public static final String SUB = "sub";
    public static final String ISS = "iss";
    public static final String SPONSOR_CODE = "sponsor_code";
    public static final String APPLICANT_CODE = "applicant_code";
    public static final String APPLICANT_NAME = "applicant_name";
    public static final String ADDITIONALVERIFICATION = "additionalVerification";
    public static final String TIME_STAMP = "timestamp";
    public static final String APPLICANT_EMAIL = "applicant_email";
    public static final String EMAIL_VERIFIED = "email_verified";
    public static final String PHONE_VERIFIED = "phone_verified";
    public static final String SUCCESSFUL = "successful";
    public static final String FAILED = "failed";
    public static final String ATTEMPT_COUNT = "attempt_count";
    public static final String REJECTED = "rejected";
    public static final String ACCEPTED = "accepted";
    public static final String IDENTITY_VERIFICATION = "identity_verification";

    public static final String COMMUNICATION_VERIFICATION = "communication_verification";
    public static final String COMMENTS = "comments";
    public static final List<String> ALLOWED_ONBOARD_STATUS = Arrays.asList(REJECTED, ACCEPTED);
    public static final String STAGING = "staging";

    public static final String ACTUAL = "actual";

    // JWT token properties
   public static final String JTI = "jti";
   public static final String IAT = "iat";
   public static final String EXP = "exp";


   public static final String PARTICIPANTS = "participants";
   public static final String ERROR = "error";
   public static final String REQUEST_LOG = "Processing request :: action: {} :: request: {}";
   public static final String ONBOARD = "ONBOARD";

   public static final String OTP_REGENERATE_COUNT = "otp_regenerate_count";
   public static final String LAST_REGENERATE_DATE = "last_regenerate_date";
   public static final String RESPONSE_OBJ = "response_obj";
}