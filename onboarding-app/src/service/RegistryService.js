
import { sendData } from '../service/APIService';
import axios from 'axios';

export const getParticipantSearch = async ({headers = {}}) => {

    var payload = { "filters": { "roles": { "eq": "payor" }, "status": { "eq": "Active" } } };
    return sendData("/participant/search", payload)

}

