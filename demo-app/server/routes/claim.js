var express = require('express');
var router = express.Router();
const { onClaimSubmit, claimSubmit } = require('../controller/claim');

router.post('/submit', claimSubmit);
router.post('/on_submit', onClaimSubmit);

module.exports = router;
