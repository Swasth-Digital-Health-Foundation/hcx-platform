const fs = require('fs');
const path = require('path');
var debug = require('debug')('server');
const { v4: uuidv4 } = require('uuid');
var createError = require('http-errors');

const { hcxInstance } = require('../util/axios');
const { encrypt, decrypt } = require('../util/jose');
const checkPayload = require('../resources/jsons/coverage_eligibility_check.json');
const { default: axios } = require('axios');

const privateKey = fs.readFileSync(path.join(__dirname, '..', 'resources', 'keys', 'x509-private-key.pem'), { encoding: 'utf-8' });

/**
 * @description - POST Route to check the eligibility.
 * @param {*} req
 * @param {*} res
 * @param {*} next
 * @return {*} 
 */
const coverageCheck = async (req, res, next) => {
    console.log("RECIPIENT_CODE and SENDER_CODE",process.env.RECIPIENT_CODE, process.env.SENDER_CODE);
    const { name, gender, index, error_code, error_code_message, sender_code = process.env.SENDER_CODE , recipient_code } = req.body;
    if (!recipient_code) return next(createError(400, 'Recipient Code is mandatory'));
    console.log("sender code", recipient_code," " ,index);
    const headers = {
        "x-hcx-recipient_code": recipient_code,
        "x-hcx-request_id": "059020c7-ec9a-43c3-88cb-63979db3e58d",
        "x-hcx-timestamp": new Date().toISOString(),
        "x-hcx-sender_code": sender_code,
        "x-hcx-correlation_id": uuidv4(),
        "enc": "A256GCM",
        "x-hcx-workflow_id": "29c06e68-83a9-4340-b002-ba3b8af6ff9f",
        "alg": "RSA-OAEP-256",
        "x-hcx-api_call_id": uuidv4(),
        "x-hcx-status": "request.queued",
        "x-hcx-delay": "2000",
        ...(error_code && {
            "x-hcx-status_test": "response.error",
            "x-hcx-error_details_test": { code: error_code, message: error_code_message || error_code, trace: '' }
        })
    }

    if(index !== 1){
        headers["x-hcx-recipient_code"] = process.env.RECIPIENT_CODE;
    }

    console.log("headers",headers);

    // map the name and gender to the HCXRequest
    const patientResource = checkPayload.entry.find(e => e.resource.resourceType === 'Patient');
    if (patientResource) {
        patientResource.resource.gender = gender;
        const patientName = patientResource.resource.name;
        if (Array.isArray(patientName) && patientName[0]) {
            patientName[0].text = name;
        }
    }

    debug('coverageCheck-checkPayload', JSON.stringify(checkPayload));

    const payload = await encrypt({ headers, payload: checkPayload, cert: privateKey });
    const data = JSON.stringify({ payload })

    //GETTING THE TOKEN
    var axios = require('axios');
    var qs = require('qs');
    var data1 = qs.stringify({
        'client_id': 'registry-frontend',
        'username': process.env.hcx_username,
        'password': process.env.hcx_password,
        'grant_type': 'password' 
    });
    var config = {
        method: 'post',
        url: process.env.token_url,
        headers: { 
            'content-type': 'application/x-www-form-urlencoded'
        },
        data : data1
    };

    console.log("token generation call  ", process.env.token_url);
    await axios(config)
    .then(function (response) {
    console.log("Bearer " + response.data.access_token);    
    hcxInstance.defaults.headers['Authorization'] = "Bearer " + response.data.access_token;
    })
    .catch(function (error) {
    console.log(error);
    });
    
    
    var config = { method: 'post', url: 'v1/coverageeligibility/check', data };
    debug('coverageCheck-payload', config);
    try {
        const response = await hcxInstance(config);
        debug('coverageCheck-success', response?.data);
        return res.json({
            request: checkPayload,
            acknowledgement: response?.data
        });
    } catch (error) {
        return next(createError(error?.response?.status || 500, error));
    }
}
/**
 * @description on check implementation. Decrypts the payload and sends to the client via socket connection
 * @param {*} req
 * @param {*} res
 * @param {*} next
 * @return {*} 
 */
const onCoverageCheck = async (req, res, next) => {

    let app = require('../app');
    const io = app && app.get('io');
    const requestBody = req.body;
    debug('onCoverageCheck-req-body', requestBody);

    const { payload } = requestBody;
    debug('onCoverageCheck-payload', payload);

    try {
        let response = requestBody;

        if (payload) {
            const decryptedPayload = await decrypt({ cert: privateKey, payload });
            response = JSON.parse(Buffer.from(decryptedPayload.plaintext).toString());
            debug('onCoverageCheck-payload-decrypt', response);
        }

        debug('onCoverageCheck-success', response);
        io && io.emit('acknowledgement', response);
        return res.json(response);
    } catch (error) {
        debug('coverageCheck-fail', error);
        return next(createError(500, error));
    }
}


module.exports = { coverageCheck, onCoverageCheck }