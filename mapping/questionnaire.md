Questionnaire

```
{
  "resourceType" : "Questionnaire",
  "id" : "a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "meta" : {
    "tag" : [
      {
        "system" : "https://api.va.gov/services/pgd",
        "code" : "66a5960c-68ee-4689-88ae-4c7cccf7ca79",
        "display" : "VA GOV CLIPBOARD"
      }
    ]
  },
  "title" : "VA GOV Pre-Visit Agenda Questionnaire",
  "status" : "active",
  "publisher" : "clipboard.gov.va",
  "useContext" : [
    {
      "code": {
        "system": "http://terminology.hl7.org/CodeSystem/usage-context-type",
        "code": "venue"
      },
      "valueReference": {
        "identifier": {
          "system": "http://va.gov/.../foo-facility",
          "value": "some-facility-id-123"
        }
      }
    },
    {
      "code": {
        "system": "http://terminology.hl7.org/CodeSystem/usage-context-type",
        "code": "venue"
      },
      "valueReference": {
        "identifier": {
          "system": "http://va.gov/.../foo-clinic",
          "value": "12334568274"
        }
      }
    },
    {
      "code": {
        "system": "http://terminology.hl7.org/CodeSystem/usage-context-type",
        "code": "venue"
      },
      "valueReference": {
        "reference": "http://va.gov/.../clipboard/.../Organization/12334568274"
      }
    }
  ],
  "item" : [
    {
      "linkId" : "01",
      "type" : "string",
      "text" : "What is the reason for this appointment?"
    },
    {
      "linkId" : "02",
      "type" : "text",
      "text" : "Are there any additional details you’d like to share with your provider about this appointment?"
    },
    {
      "linkId" : "03",
      "type" : "text",
      "text" : "Are there any life events that are positively or negatively affecting your health?"
    },
    {
      "linkId" : "04",
      "type" : "text",
      "text" : "Do you have other questions you want to ask your provider?"
    }
  ]
}
```
