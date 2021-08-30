# QuestionnaireResponse

[base] URL is `https://sandbox-api.va.gov/services/pgd/v0/r4`

**Create new resource:**

Include FHIR Resource in request body in json format

`POST [base]/QuestionnaireResponse`

(resource is saved with a server-generated UUID)

**Update existing resource:**

Include FHIR Resource in request body in json format

`PUT [base]/QuestionnaireResponse/e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9`

**Read:**

`GET [base]/QuestionnaireResponse/e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9`

**[Search](https://www.hl7.org/fhir/r4/questionnaireresponse.html#search) by authored date:**

`GET [base]/QuestionnaireResponse?authored=2013-02-19T14:15:00-05:00`

**Search by source (patient):**

`GET [base]/QuestionnaireResponse?source=1008596379V859838`

**Search by subject (appointment ID):**

`GET [base]/QuestionnaireResponse?subject=I2-SLRRT64GFGJAJGX62Q55NSQV44VEE4ZBB7U7YZQVVGKJGQ4653IQ0000`

```
{
  "resourceType" : "QuestionnaireResponse",
  "id" : "e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9",
  "text" : {
    "status" : "generated",
    "div" : "<div><h1>Pre-Visit Questionnaire</h1></div>"
  },
  "questionnaire" : "Questionnaire/a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "status" : "completed",
  "subject" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Appointment/I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000"
  },
  "authored" : "2020-08-20",
  "source" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Patient/1008596379V859838"
  },
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
      "text" : "Are there any additional details you'd like to share with your provider about this appointment?",
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
