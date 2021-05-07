#!/usr/bin/env node

const process = require("process");
const yargs = require("yargs");
const axios = require("axios");
const { v4: uuidv4 } = require("uuid");
const jwt = require("njwt");
const qs = require("querystring");

const argv = yargs
  .option("client-id", {
    describe: "oauth client id",
  })
  .option("client-secret", {
    describe: "oauth client secret",
  })
  .option("scope", {
    describe: "space separated list of oauth scopes to request",
    type: "array",
  })
  .option("launch", {
    describe: "launch context (ICN)",
  })
  .option("authorization-url", {
    describe: "Oauth Authorization url.",
    default: "https://dev-api.va.gov/oauth2",
  })
  .demandOption(["client-id", "client-secret", "scope", "authorization-url"])
  .argv;

function getAssertion(clientId, clientSecret, audience) {
  let algorithm = "HS256";

  const claims = {
    aud: audience,
    iss: clientId,
    sub: clientId,
    jti: uuidv4(),
  };

  const token = jwt.create(claims, clientSecret, algorithm);
  token.setExpiration(new Date().getTime() + 60 * 1000);

  return token.compact();
}

function getToken(assertion) {
  let body = {
    grant_type: "client_credentials",
    client_assertion_type:
      "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
    client_assertion: assertion,
    scope: argv["scope"].join(" "),
  };
  if (argv["launch"]) {
    body.launch = argv["launch"];
  }

  return axios.post(`${argv["authorization-url"]}/token`, qs.stringify(body), {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/x-www-form-urlencoded",
    },
  });
}

function getAudience() {
  return axios.get(
    `${argv["authorization-url"]}/.well-known/openid-configuration`
  );
}

getAudience()
  .then((response) => {
    let assertion = getAssertion(
      argv["client-id"],
      argv["client-secret"],
      response.data.issuer + "/v1/token"
    );
    return getToken(assertion);
  })
  .then((response) => {
    console.log(JSON.stringify(response.data));
  })
  .catch((e) => {
    console.error(e);
    process.exit(1);
  });
