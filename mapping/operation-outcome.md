# Operation Outcome

Any error from the server will return a FHIR
[https://www.hl7.org/fhir/r4/operationoutcome.html](OperationOutcome). 
Example:

```
{
  "id": "e5f6b5e2-c9d3-4635-9e49-6f110829ca77",
  "resourceType": "OperationOutcome",
  "text": {
    "status": "additional",
    "div": "<div>Failure: /r4/Questionnaire/5555555555</div>"
  },
  "extension": [
    {
      "url": "timestamp",
      "valueInstant": "2020-12-17T18:54:10.192788Z"
    },
    {
      "url": "type",
      "valueString": "NotFound"
    },
    {
      "url": "message",
      "valueString": "FKhs8QaNT3mVXJqbC3AeVDTp4FBGenYs8J4hBTmWK6vm+NP4BA=="
    },
    {
      "url": "request",
      "valueString": "/r4/Questionnaire/31413"
    }
  ],
  "issue": [
    {
      "severity": "fatal",
      "code": "not-found"
    }
  ]
}
```