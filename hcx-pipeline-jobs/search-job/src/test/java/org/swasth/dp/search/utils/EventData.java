package org.swasth.dp.search.utils;

public interface EventData {

    String EVENT_ROUTER_DATA = "{\"headers\":{\"protocol\":{\"enc\":\"A256GCM\",\"alg\":\"RSA-OAEP\",\"x-hcx-sender_code\":\"1-80500cdd-2dec-4d60-bd1b-8f9d83f497ff\",\"x-hcx-recipient_code\":\"hcx-gateway-code\",\"x-hcx-workflow_id\":\"1e83-460a-4f0b-b016-c22d820674e1\",\"x-hcx-request_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5091\",\"x-hcx-status\":\"request.initiate\",\"x-hcx-debug_flag\":\"Info\",\"x-hcx-error_details\":{\"error.code\":\"bad.input\",\"error.message\":\"Provider code not found\",\"trace\":\"\"},\"x-hcx-debug_details\":{\"error.code\":\"bad.input\",\"error.message\":\"Provider code not found\",\"trace\":\"\"},\"x-hcx-search\":{\"filters\":{\"senders\":[\"1-80500cdd-2dec-4d60-bd1b-8f9d83f497ff\"],\"receivers\":[\"1-93f908ba-b579-453e-8b2a-56022afad275\"],\"entity_types\":[\"preauth\",\"claim\"],\"workflow_ids\":[],\"case_ids\":[],\"entity_status\":[\"claims.completed\",\"claims.rejected\"]},\"time_period\":24}}},\"ets\":1640587993601,\"mid\":\"200c6dac-b259-4d35-b176-370fb092d7b0\",\"action\":\"/v1/hcx/search\",\"status\":\"submitted\"}";
}
