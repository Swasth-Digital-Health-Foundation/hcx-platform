import { post } from './APIService';


export const userInvite = async ({email,participant_code,role,invited_by}: {
    email: any;
    participant_code: any;
    role: any;
    invited_by: any;
}) => {
    var payload = {email,participant_code,role,invited_by}
    return post("/onboard/user/invite", payload);
}

export const userInviteAccept = async (jwt_token: any,user: any) => {
    var payload = {jwt_token, user}
    return post("/onboard/user/invite/accept", payload);
}

export const userInviteReject = async (jwt_token: any, user: any) => {
    var payload = {jwt_token, user}
    return post("/onboard/user/invite/reject", payload);
}


export const userCreate = async (user_name: any, email: any, mobile: any, tenant_roles: any, created_by: any) => {
    var payload = {user_name, email, mobile, tenant_roles, created_by}
    return post("/user/create", payload);
}

export const serachUser = async (email: any) => {
    var payload = { "filters": { "email": { "contains":  email} } };
    return post("/user/search", payload);
}

export const getAllUser = async (token="") => {
    var payload = { "filters": {} };
    if(token != ""){
        console.log("not blank ", token);
        return post("/user/search", payload,  {} ,token);
    }else{
        return post("/user/search", payload);
    }
}

export const getLinkedUsers = async (code: any) => {
    var payload = {
        "filters": {
          "tenant_roles":{
              "participant_code": {
                  "eq":code
                  }
          }
        }
    }
    return post("/user/search", payload);
}


export const userUpdate = async (request: any) => {
    var payload = request
    return post("/user/update", payload);
}


export const userDelete = async (user_id: any) => {
    var payload = {user_id}
    return post("/user/delete", payload);
}

export const userAdd = async (participant_code: any, users: any) => {
    var payload = {participant_code, users}
    return post("participant/user/add", payload);
}


export const userRemove = async (participant_code: any, users: any) => {
    var payload = {participant_code, users}
    return post("/participant/user/remove", payload);
}


export const reverifyLink = async (payload: any) => {
    return post("/participant/verification/link/send", payload);
}







