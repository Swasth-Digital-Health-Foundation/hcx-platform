import { post } from './APIService';

const env = process.env.REACT_APP_ENV;

let applicantSearchAPI = '';

if (env === 'poc') {
    applicantSearchAPI = "/applicant/search?fields=communication,sponsors,onboard_validation_properties";
} else {
    applicantSearchAPI = "/applicant/search?fields=communication,sponsors,onboard_validation_properties,mockparticipants"
}

export const getParticipantSearch = async ({headers = {}}) => {
    var payload = { "filters": { "roles": { "eq": "payor" }, "status": { "eq": "Active" }, "supported_apis": { "eq": "onboarding" } } };
    return post("/participant/search", payload);
}

export const getParticipant = async (email:any) => {
    var payload = { "filters": { "primary_email": { "eq":  email} } };
    return post(applicantSearchAPI, payload);
}

export const getParticipantByCode = async (code:any) => {
    var payload = { "filters": { "participant_code": { "eq":  code} } };
    return post(applicantSearchAPI, payload);
}

export const generateClientSecret = async (code:any, token="") => {
    var payload = { "participant_code": code};
    return post("/onboard/applicant/password/generate", payload, {}, token);
}