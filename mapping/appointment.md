# QuestionnaireResponse and Appointment

Example for saving a questionnaire-response with a reference to a FHIR appointment.

### Appointment

- `https://api.va.gov/services/fhir/v0/r4/Appointment/202008211400983000084800000000000000`
- This assumes clinics are also available as FHIR locations

```
{
  "resourceType" : "Appointment",
  "id" : "example",
  "text" : {
    "status" : "generated",
    "div" : "<div xmlns=\"http://www.w3.org/1999/xhtml\">Brian MRI results discussion</div>"
  },
  "status" : "booked",
  "serviceCategory" : [
    {
      "coding" : [
        {
          "system" : "http://example.org/service-category",
          "code" : "gp",
          "display" : "General Practice"
        }
      ]
    }
  ],
  "serviceType" : [
    {
      "coding" : [
        {
          "code" : "52",
          "display" : "General Discussion"
        }
      ]
    }
  ],
  "specialty" : [
    {
      "coding" : [
        {
          "system" : "http://snomed.info/sct",
          "code" : "394814009",
          "display" : "General practice"
        }
      ]
    }
  ],
  "appointmentType" : {
    "coding" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v2-0276",
        "code" : "FOLLOWUP",
        "display" : "A follow up visit from a previous appointment"
      }
    ]
  },
  "reasonReference" : [
    {
      "reference" : "Condition/example",
      "display" : "Severe burn of left ear"
    }
  ],
  "priority" : 5,
  "description" : "Discussion on the results of your recent MRI",
  "start" : "2013-12-10T09:00:00Z",
  "end" : "2013-12-10T11:00:00Z",
  "created" : "2013-10-10",
  "comment" : "Further expand on the results of the MRI and determine the next actions that may be appropriate.",
  "basedOn" : [
    {
      "reference" : "ServiceRequest/myringotomy"
    }
  ],
  "participant" : [
    {
      "actor" : {
        "reference" : "Patient/1008596379V859838",
        "display" : "Peter James Chalmers"
      },
      "required" : "required",
      "status" : "accepted"
    },
    {
      "type" : [
        {
          "coding" : [
            {
              "system" : "http://terminology.hl7.org/CodeSystem/v3-ParticipationType",
              "code" : "ATND"
            }
          ]
        }
      ],
      "actor" : {
        "reference" : "Practitioner/I2-6NVSMKEGQKNB3KRDXBGE7NRIEY000000",
        "display" : "Dr Adam Careful"
      },
      "required" : "required",
      "status" : "accepted"
    },
    {
      "actor" : {
        "reference" : "Location/534-12975",
        "display" : "NORTH CHARLESTON VA CBOC, PRIMARY CARE/MEDICINE"
      },
      "required" : "required",
      "status" : "accepted"
    }
  ]
}
```

### QuestionnaireResponse

- `https://sandbox-api.va.gov/services/pgd/v0/r4/QuestionnaireResponse/e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9`
- `subject` is used for appointment, not patient
- `source` is used for patient
- Search by appointment parameters with [chaining](https://www.hl7.org/fhir/search.html#chaining):
    - `QuestionnaireResponse?source=1008596379V859838&authored=2020-08-20&subject.actor=534-12975`
- Implementation notes
    - During save, PGD will internally retrieve the corresponding appointment to index its participant fields
    - What is the desired behavior if appointment API is unavailable, or the appointment can not be found?
    - Save the QuestionnaireResponse anyway and try to index the appointment again later?

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
  "questionnaire" : "Questionnaire/a4edd60a-f142-4547-8a9e-02a6bba76bcc",
  "status" : "completed",
  "subject" : {
    "reference" : "https://api.va.gov/services/fhir/v0/r4/Appointment/202008211400983000084800000000000000"
  },
  "authored" : "2020-08-20",
  "source" : {
    "reference" : "https://api.va.gov/services/fhir/v0/r4/Patient/1008596379V859838"
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

