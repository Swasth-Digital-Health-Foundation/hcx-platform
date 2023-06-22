import { get, post } from './APIService';
import axios from 'axios';


export const userInvite = async ({email,participant_code,role,invited_by}) => {
    var payload = {email,participant_code,role,invited_by}
    return post("/onboard/user/invite", payload);
}

export const userInviteAccept = async (jwt_token,user) => {
    var payload = {jwt_token, user}
    return post("/onboard/user/invite/accept", payload);
}

export const userInviteReject = async (jwt_token, user) => {
    var payload = {jwt_token, user}
    return post("/onboard/user/invite/accept", payload);
}


export const userCreate = async (user_name, email, mobile, tenant_roles, created_by) => {
    var payload = {user_name, email, mobile, tenant_roles, created_by}
    return post("/user/create", payload);
}

export const serachUser = async (email) => {
    var payload = { "filters": { "email": { "contains":  email} } };
    return post("/user/search", payload);
}

export const getAllUser = async () => {
    var payload = { "filters": {} };
    return post("/user/search", payload);
}


export const userUpdate = async (request) => {
    var payload = request
    return post("/user/update", payload);
}


export const userDelete = async (user_id) => {
    var payload = {user_id}
    return post("/user/delete", payload);
}

export const userAdd = async (participant_code, users) => {
    var payload = {participant_code, users}
    return post("participant/user/add", payload);
}


export const userRemove = async (participant_code, users) => {
    var payload = {participant_code, users}
    return post("participant/user/remove", payload);
}


export const reverifyLink = async (primary_email, primary_mobile, participant_code, participant_name) => {
    var payload = {primary_email, primary_mobile, participant_code, participant_name}
    return post("/participant/verification/link/send", payload);
}







