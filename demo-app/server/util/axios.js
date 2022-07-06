const axios = require('axios');
console.log("pppp",process.env.HCX_UPSTREAM);
const hcxInstance = axios.create({
    baseURL: process.env.HCX_UPSTREAM,
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer'
    }
});

module.exports = { hcxInstance };