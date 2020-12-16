QuestionnaireResponse

```
{
  "resourceType" : "QuestionnaireResponse",
  "id" : "e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9",
  "meta" : {
    "tag" : [
      {
        "system" : "https://api.va.gov/services/pgd",
        "code" : "66a5960c-68ee-4689-88ae-4c7cccf7ca79",
        "display" : "VA GOV CLIPBOARD"
      }
    ]
  },
  "text" : {
    "status" : "generated",
    "div" : "<div><h1>Pre-Visit Questionnaire</h1></div>"
  },
  "status" : "completed",
  "authored" : "2020-08-20",
  "subject" : {
    "reference" : "Patient/1008596379V859838"
  },
  "author" : {
    "identifier" : {
      "system" : "https://veteran.apps.va.gov/appointments/v1",
      "value" : "202008211400983000084800000000000000"
    }
  },
  "questionnaire" : "Questionnaire/a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "item" : [
    {
      "linkId" : "01",
      "text" : "What is the reason for this appointment?",
      "answer" : [
        {
          "valueString" : "Eye is hurting"
        }
      ]
    },
    {
      "linkId" : "02",
      "text" : "Are there any additional details you’d like to share with your provider about this appointment?",
      "answer" : [
        {
          "valueString" : "cant look into the sun"
        }
      ]
    },
    {
      "linkId" : "03",
      "text" : "Are there any life events that are positively or negatively affecting your health?",
      "answer" : [
        {
          "valueString" : "no"
        }
      ]
    },
    {
      "linkId" : "04",
      "text" : "Do you have other questions you want to ask your provider?",
      "answer" : [
        {
          "valueString" : "Is this covered by my VA Benfits?"
        }
      ]
    }
  ]
}
```