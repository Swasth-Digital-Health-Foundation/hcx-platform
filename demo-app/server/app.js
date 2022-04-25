var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
require('dotenv').config();
const cors = require('cors')
var debug = require('debug')('server');

var apiRouter = require('./routes/index');

var app = express();

app.use(cors());
app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'build')));

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'build', 'index.html'));
});
app.use('/v1', apiRouter);

app.use(function (req, res, next) {
  res.sendFile(path.join(__dirname, 'build', 'index.html'));
});

app.use(function (err, req, res, next) {
  debug('ERROR', err);
  if (err?.isAxiosError) {
    return res.status(err.status || 500).json(err?.response?.data);
  }
  res.status(err.status || 500).json(err);
});

module.exports = app;
