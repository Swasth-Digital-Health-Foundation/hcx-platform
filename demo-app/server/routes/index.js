var express = require('express');
var router = express.Router();
const coverageRouter = require('./coverage');
const claimRouter = require('./claim');
const preAuthRouter = require('./preAuth');

router.use('/coverageeligibility', coverageRouter);
router.use('/claim', claimRouter);
router.use('/preauth', preAuthRouter);

module.exports = router;
