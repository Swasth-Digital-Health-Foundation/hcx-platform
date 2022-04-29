var express = require('express');
var router = express.Router();
const { onPreAuthSubmit, preAuthSubmit } = require('../controller/preAuth');

router.post('/submit', preAuthSubmit);
router.post('/on_submit', onPreAuthSubmit);

module.exports = router;
