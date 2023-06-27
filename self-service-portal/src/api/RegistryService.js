
import { post } from '../api/APIService';
import axios from 'axios';

export const getParticipantSearch = async ({headers = {}}) => {
    var payload = { "filters": { "roles": { "eq": "payor" }, "status": { "eq": "Active" }, "supported_apis": { "eq": "onboarding" } } };
    return post("/participant/search", payload);
}

export const getParticipant = async (email) => {
    var payload = { "filters": { "primary_email": { "eq":  email} } };
    return post("/applicant/search?fields=communication,sponsors,onboard_validation_properties", payload);
}

export const getParticipantByCode = async (code) => {
    var payload = { "filters": { "participant_code": { "eq":  code} } };
    return post("/applicant/search?fields=communication,sponsors,onboard_validation_properties", payload);
}
