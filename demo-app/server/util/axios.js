const axios = require('axios');
console.log("pppp",process.env.HCX_UPSTREAM, process.env.AUTH_TOKEN);
const hcxInstance = axios.create({
    baseURL: process.env.HCX_UPSTREAM,
    headers: {
        'Content-Type': 'application/json',
        'Authorization': process.env.AUTH_TOKEN
    }
});

module.exports = { hcxInstance };