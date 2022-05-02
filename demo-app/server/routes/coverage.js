var express = require('express');
var router = express.Router();
const { coverageCheck, onCoverageCheck} = require('../controller/eligibility');

router.post('/check', coverageCheck);
router.post('/on_check', onCoverageCheck);

module.exports = router;
