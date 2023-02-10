
import { post } from '../service/APIService';
import axios from 'axios';

export const getParticipantSearch = async ({headers = {}}) => {
    var payload = { "filters": { "roles": { "eq": "payor" }, "status": { "eq": "Active" }, "supported_apis": { "eq": "onboarding" } } };
    return post("/participant/search", payload)
}

