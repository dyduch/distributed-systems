var PROTO_PATH = './exchange.proto';
var fs = require('fs');
var parseArgs = require('minimist');
var path = require('path');
var _ = require('lodash');
var grpc = require('grpc');
var protoLoader = require('@grpc/proto-loader');
var packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async function timeout() {
    await sleep(5000);
  }
  

var eurExchangeRates = new Map();

var exchangerateservice = grpc.loadPackageDefinition(packageDefinition).exchangerateservice;

function getValueInCurrency(inCurrency, domestic) {
    if(domestic === "EUR"){
        return eurExchangeRates.get(inCurrency);
    }
    else {
        var valueInWanted = eurExchangeRates.get(inCurrency);
        var valueInDomestic = eurExchangeRates.get(domestic);
        return Number(valueInWanted/valueInDomestic).toFixed(2);
    }
  }

  function randomInRange(min, max) {
    return Math.random() < 0.5 ? ((1-Math.random()) * (max-min) + min) : (Math.random() * (max-min) + min);
  }

  function modifyRate(value, key, map) {
    value = value * randomInRange(0.95, 1.05);
    eurExchangeRates.set(key, Number.parseFloat(value.toFixed(2)));
  }

  function modifyRates(){
    eurExchangeRates.forEach(modifyRate);
    }

function getExchangeRates(call) {

    console.log("got request!");

    modifyRates();
    var domesticCurrency = call.request.domesticCurrency;
    var bankCurrencies = call.request.currencies;
    console.log("domesticCurrency " + domesticCurrency);
    console.log("currencies " + bankCurrencies);
    var value;
    var exchangeRate;
    var exchangeRateRs;

        _.each(bankCurrencies, function(currency) {

            timeout();

                value = getValueInCurrency(currency, domesticCurrency);
                console.log("VALUE: " + value);

                exchangeRate = {
                    rate: value,
                    currency: currency
                }


                exchangeRateRs = {
                    exchangeRate: exchangeRate
                }
                call.write(exchangeRateRs);
            })
    
        call.end();
}

function getServer() {
    var server = new grpc.Server();
    server.addProtoService(exchangerateservice.ExchangeRateService.service, {
        getExchangeRates: getExchangeRates
    });
    return server;
}

if (require.main === module) {
    // If this is run as a script, start a server on an unused port
    var routeServer = getServer();
    routeServer.bind('0.0.0.0:21370', grpc.ServerCredentials.createInsecure());

    eurExchangeRates.set("PLN", 4.28);
    eurExchangeRates.set("AUD", 1.59);
    eurExchangeRates.set("GBP", 0.85);
    eurExchangeRates.set("USD", 1.12);
    eurExchangeRates.set("CHF", 1.14);

    console.log("exc rates before start: " + eurExchangeRates.size);

    routeServer.start();
    console.log("server started");
    //modifyRates();
  }
  
  exports.getServer = getServer;