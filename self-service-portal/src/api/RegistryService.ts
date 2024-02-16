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

export const getPayorList = async () => {
    var payload = { "filters": { "roles": { "eq":  "payor"} }};
    return post(applicantSearchAPI, payload);
}


export const generateClientSecret = async (code:any, token="") => {
    var payload = { "participant_code": code};
    return post("/onboard/applicant/password/generate", payload, {}, token);
}

export const generateAPIKey = async (user:string, participant:string, token="") => {
    var payload = {"user_id":user, "participant_code":participant}
    return post("/api-access/secret/generate", payload, {}, token);
}

export const approvePayorList = async(token = "") => {
    var payload = {"filters" : {}}
    return post('/applicant/search?fields=identity_verification', payload, {} , token);
}

export const approvePayor = async(participant_code: string, status: string ,token = "") => {
    var payload = {"participant_code":participant_code, "status":status}
    return post('/participant/verify/identity', payload, {} , token);
}
