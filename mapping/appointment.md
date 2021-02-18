# QuestionnaireResponse and Appointment

Example for saving a questionnaire-response that references a FHIR appointment

### Appointment

- Backed by CDW
- `https://sandbox-api.va.gov/services/fhir/v0/r4/Appointment/I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000`

```
{
  "resourceType" : "Appointment",
  "id" : "I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000",
  "status" : "cancelled",
  "cancelationReason" : {
    "coding" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/appointment-cancellation-reason",
        "display" : "OTHER"
      }
    ],
    "text" : "OTHER"
  },
  "appointmentType" : {
    "coding" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v2-0276",
        "display" : "WALKIN"
      }
    ],
    "text" : "WALKIN"
  },
  "description" : "Used for veterans with a service connected % of 50% and greater.",
  "start" : "2017-05-26T07:00:00Z",
  "end" : "2017-05-26T07:20:00Z",
  "minutesDuration" : 20,
  "created" : "2017-05-25T16:00:00Z",
  "participant" : [
    {
      "actor" : {
        "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Location/I2-XPW2ECZK2LTNSPLNVKISWC5QZABOVEBZD5V2CKFRVEPAU5CNZMJQ0000",
        "display" : "PROSTHETIC CONSULTS"
      },
      "status" : "accepted"
    },
    {
      "actor" : {
        "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Patient/1011537977V693883",
        "display" : "TEST,PATIENT ONE"
      },
      "status" : "accepted"
    }
  ]
}
```

Appointment has reference to location (clinic), which in turn references managing organization (facility):

```
{
  "resourceType" : "Location",
  "id" : "I2-XPW2ECZK2LTNSPLNVKISWC5QZABOVEBZD5V2CKFRVEPAU5CNZMJQ0000",
  "identifier" : [
    {
      "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier",
      "value" : "vha_688"
    },
    {
      "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier",
      "value" : "vha_688_3343"
    }
  ],
  "status" : "active",
  "name" : "PROSTHETIC CONSULTS",
  "description" : "RM 1F03",
  "mode" : "instance",
  "type" : [
    {
      "coding" : [
        {
          "display" : "CLINIC"
        }
      ],
      "text" : "CLINIC"
    }
  ],
  "telecom" : [
    {
      "system" : "phone",
      "value" : "8550-3888"
    }
  ],
  "address" : {
    "text" : "1501 ROXAS BLVD PASAY CITY, METRO MANILA PH 96515-1100",
    "line" : [
      "1501 ROXAS BLVD"
    ],
    "city" : "PASAY CITY, METRO MANILA",
    "state" : "PH",
    "postalCode" : "96515-1100"
  },
  "physicalType" : {
    "coding" : [
      {
        "display" : "RM 1F03"
      }
    ],
    "text" : "RM 1F03"
  },
  "managingOrganization" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Organization/I2-5R65QGBHXIK5FVLBMFXDK6CDNY000000",
    "display" : "MANILA-RO"
  }
}
```


```
{
  "resourceType" : "Organization",
  "id" : "I2-5R65QGBHXIK5FVLBMFXDK6CDNY000000",
  "identifier" : [
    {
      "system" : "http://hl7.org/fhir/sid/us-npi",
      "value" : "1215124896"
    },
    {
      "system" : "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier",
      "value" : "vha_688"
    }
  ],
  "active" : true,
  "name" : "MANILA-RO",
  "address" : [
    {
      "text" : "1501 ROXAS BLVD PASAY CITY, METRO MANILA PH 96515-1100",
      "line" : [
        "1501 ROXAS BLVD"
      ],
      "city" : "PASAY CITY, METRO MANILA",
      "state" : "PH",
      "postalCode" : "96515-1100"
    }
  ]
}
```

### QuestionnaireResponse

- `https://sandbox-api.va.gov/services/pgd/v0/r4/QuestionnaireResponse/e4601c4c-34bd-4ecc-ba2a-ce39502ed6b9`
- `subject` is appointment reference
- `source` is patient reference

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
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Appointment/I2-ZGHFNASLCRYERJF3R5KUT2JTEQAASLOKBKX5BCCJWT7U7G2DBEWQ0000"
  },
  "authored" : "2020-08-20",
  "source" : {
    "reference" : "https://sandbox-api.va.gov/services/fhir/v0/r4/Patient/1011537977V693883"
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

### Example Searches

Search `Location` by facility:

```
Location?identifier=vha_688
```

Search `Location` by clinic:

```
Location?identifier=vha_688_3343
```

Search `Appointment` by patient and start time:

```
Appointment?patient=1011537977V693883&date=eq2017-05-26
```

To search `Appointment` by clinic, search by `Location` first:

```
Location?identifier=vha_688_3343
extract location ID from bundle
Appointment?location=I2-LOCATIONID
```

Search `QuestionnaireResponse` by patient and authored date:

```
QuestionnaireResponse?source=1011537977V693883&authored=2020-08-20
```

To search QuestionnaireResponse by appointment details, search `Location` and `Appointment` first:

```
Location?identifier=vha_688_3343
Extract location ID from bundle
Appointment?patient=1011537977V693883&date=eq2017-05-26&location=I2-LOCATIONID
Extract appointment ID from bundle
QuestionnaireResponse?source=1011537977V693883&subject=I2-APPOINTMENTID
```
