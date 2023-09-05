
import { post } from './APIService';

export const getParticipantSearch = async ({headers = {}}) => {
    var payload = { "filters": { "roles": { "eq": "payor" }, "status": { "eq": "Active" }, "supported_apis": { "eq": "onboarding" } } };
    return post("/participant/search", payload);
}

export const getParticipant = async (email: any) => {
    var payload = { "filters": { "primary_email": { "eq":  email} } };
    return post("/applicant/search?fields=communication,sponsors,onboard_validation_properties,mockparticipants", payload);
}

export const getParticipantByCode = async (code: any) => {
    var payload = { "filters": { "participant_code": { "eq":  code} } };
    return post("/applicant/search?fields=communication,sponsors,onboard_validation_properties,mockparticipants", payload);
}

export const generateClientSecret = async (code: any, token="") => {
    var payload = { "participant_code": code};
    return post("/onboard/applicant/password/generate", payload, {}, token);
}
