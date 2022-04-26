const axios = require('axios');

const hcxInstance = axios.create({
    baseURL: process.env.HCX_UPSTREAM,
    headers: {
        'Content-Type': 'application/json',
        'Authorization': process.env.AUTH_TOKEN
    }
});

module.exports = { hcxInstance };